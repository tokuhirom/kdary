# どのように実装したか

darts-clone を chatgpt の gpt-4-o で kotlin にまず変換しました。

最初に定義したプロンプトは以下のようなものです。

```
このファイルを kotlin にしてほしい。ビット幅は変更しないようにすること。
```

darts-clone のようなプロジェクトでは、ビット幅が変わると、辞書のサイズが変わってしまうため、ビット幅を変更しないようにすることが重要ですから、
このように定義したというワケ。

最初に定義されたファイルは、以下のようにいくつかの問題がありました。

    fun DARTS_THROW(msg: String): Nothing = throw Darts.Exception("$msg")

まずこのようなコードが生成されましたが、Darts.Exception が存在しないので作成する必要があります。
以下のように定義して、利用した方が良いでしょう。

    import kotlin.Exception
    class DartsException(message: String): Exception(message)

また、typealias はトップレベルで定義しないといけませんので、トップレベルで定義するようにする必要があります。

よって、以下のようにプロンプトを修正します。

```
c++ のコードを kotlin に変換してください。以下の点に注意してください。
- ビット幅は変更しないようにすること
- kotlin では typealias はトップレベルにしか定義できないことに注意すること
- DATS_THROW のために DartsException クラスを定義し、それを利用すること
- size_t は 64bit であることに注意すること
- keyset クラスも忘れずに生成すること
```

とかやってみたけど、どうもうまくいかないので手書きすることにした。

## 手書き

2024-07-06 に手書きを開始。

#define DARTS_VERSION "0.32" をベースにしている。
e40ce4627526985a7767444b6ed6893ab6ff8983 の時点。

さらっと 47行目まで実装した。

### DoubleArrayUnit

つづいて DoubleArrayUnit を実装。81行目まで実装。1931行目まであるので、まだまだ先は長い。

### Exception

Exception は DartsException という名前で定義。簡単にすんだので 106行目までクリア。

### DoubleArrayImpl

続いて DoubleArrayImpl。いよいよ本編という感じ。
C++ の char は kotlin では Char ではなく Byte だと思う。

一旦、class 宣言内に書いてある分だけさらっと実装。
配列は速度重視で List ではなく Array を使う。

ファイル操作については、kotlin/native や kotlin/js でも利用することを想定し、OkIO を使うことにする。

### DoubleArray

DoubleArray は、`DoubleArrayImpl<Int>` のことなので、typealias で OK

### AutoArray, AutoPool, AutoStack

これは実際、kotlin だと不要そうだが?

### BitVector

BitVector の実装。テストとかないので、テストとかは自分で書く。

### Keyset

これは必要そう

### DawgNode, DawgUnit, DawgBuilder, DoubleArrayBuilder

これも必要そう

これらを利用して、DoubleArrayImpl<A, B, T, C>::build を実装するということになるようだ。

## 型が合ってない問題

2024-07-07 に続きを実装。

一通り実装してみたが、 型が一部あってない部分があることがわかったので、全体的に見直していく。

以下を修正した。

- BitVector
- DawgNode
- DoubleArrayBuilderExtraUnit
- DoubleArrayBuilderUnit
- DoubleArrayUnit
- Keyset

以下は複雑なので後回し。

- DoubleArrayBuilder 
- DoubleArrayImpl

## 2024-07-08

DoubleArrayBuilder を全体的に見直していく。

buildFromKeyset が難しい。

Keyset はどう作るか?

UByteArray があったのでこれを活用することにした。
- DoubleArrayBuilder
が一通り見直し終わり。

DoubleArrayImpl は、まだ。
- build
- exactMatchSearch

DoubleArrayImpl を実装していく。
- traverse を言ったん対応した。
- commonPrefixSearch の対応が必要そう。

