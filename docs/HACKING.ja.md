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

DoubleArrayImpl の実装が xor 周りが結構怪しい。c++ の方の型とあってない箇所がありそうなので、このへんは実際に動かしながらデバッグ
していくしかない。

よくわからんけど、DawgBuilderTest がこけてるので、これを先に対応しよう。
直した。

## 2024-07-09

DoubleArrayImplTest の実装を進める。test/test-darts.cc を参考にして、実装していく。 
まずは、exactMatchSearch が通ることを目指す。

```
failed to build double-array: wrong key order
java.lang.IllegalArgumentException: failed to build double-array: wrong key order
	at me.geso.kdary.DoubleArrayBuilder.arrangeFromKeyset-lK5RzIA(DoubleArrayBuilder.kt:298)
	at me.geso.kdary.DoubleArrayBuilder.buildFromKeyset--0U3RB0(DoubleArrayBuilder.kt:241)
	at me.geso.kdary.DoubleArrayBuilder.buildFromKeyset(DoubleArrayBuilder.kt:195)
```

arrangeFromKeyset が失敗しているので、これをデバッグする。

`DoubleArray.build()` の引数はソート済みである必要があるようだ。
std::set がソート済みであることを利用しているので、それを考慮する。

散々調べた結果、`DawgBuilder::flush` の中で、以下のようなコードがあった。

```kotlin
var (hashId, matchId) = findNode(nodeId)
if (matchId != 0u) {
    // snip
} else {
    val newMatchId = unitId + 1u
    table[hashId.toInt()] = newMatchId
}

nodes[nodeStack.top().toInt()].setChild(matchId)
```

ここで、 matchId への代入はスコープを抜けた後で使うので、新しく変数を定義せずに以下のようにしなくてはいけなかった。

```kotlin
matchId = unitId + 1u
table[hashId.toInt()] = matchId
```

こういう副作用があるコードの変換が chatgpt は苦手。

さて、次に `DoubleArrayImpl::exactMatchSearch` を検証する。

```
Index 1 out of bounds for length 1
java.lang.ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 1
	at kotlin.UByteArray.get-w2LRezQ(UByteArray.kt:29)
	at me.geso.kdary.DoubleArrayImpl.exactMatchSearch-vxpkxVs(DoubleArrayImpl.kt:357)
	at me.geso.kdary.DoubleArrayImpl.exactMatchSearch-vxpkxVs$default(DoubleArrayImpl.kt:305)
	at me.geso.kdary.DoubleArrayImplTest.testDic(DoubleArrayImplTest.kt:162)
```

というような例外が発生してしまう。

## 2024-07-10

Set に ByteArray を入れたときに、ユニークなオブジェクトと見なされない問題にはまる。

```kotlin
val setByteArray =
    setOf(
        "a".toByteArray(),
        "a".toByteArray(),
        "b".toByteArray(),
    )
println(setByteArray)
```

このような場合、3要素になってしまう。Java がそうなのでそうなんだけど、、Java を意識してないと死ぬ。

```
jshell> var s = new HashSet<byte[]>();
s ==> []

jshell> s.add(new byte[] {99});
$2 ==> true

jshell> s.add(new byte[] {99});
$3 ==> true

jshell> s.add(new byte[] {93});
$4 ==> true

jshell> s.size()
$5 ==> 3
```

`build() with keys, lengths and random values` を実装してみたが例外が発生して落ちる。謎。
TODO として後回しにする。

testTraverse を先にやって、後で、testCommonPrefixSearch をやることにしよう。

`UByteArray` は長さを持ってるので、`exactMatchSearch` で length パラメータを持つ必要がないので消す。
`length` が 0 を指定していると null 終端文字列として扱うのが darts-clone の仕様だけどこの部分は使わないようにする。

## 2024-07-11

UByteArray を外部インターフェースに露出させないのが良さそう。
UByteArray は Experiental なので。

DoubleArray.build() の numKeys は List<UByteArray> の size から取ればいいので削除。

DoubleArray.build 自体、Companion object に移した方が良いので移した。
これにより、DoubleArray は immutable なオブジェクトとなった。

Keyset に渡す lengths もいらないので削除。

BitVector の rank メソッドがバグってるのがそもそもの問題だということがわかったので修正。

commonPrefixSearch どうなる?
length パラメータは、ByteArray から長さとれるので不要。

## 2024-07-12

commonPrefixSearch が直ったのでよさそう。

## 2024-07-14

KMP なライブラリとするためには inline value class が JVM 以外では使えないので、
actual/expect にした。
DoubleArrayUnit を対象とする。

## 2024-07-15

okio 周りを整理。
あとは、DoubleArray クラスを KDary クラスに変更したほうがいいかも。
DoubleArrayIO のほうも同じく。

それができたらリリースでよさそう。
github actions での CI 設定も必要だ。

## 2024-07-16

github actions での CI を実施した。

maven central へのリリースを考える。
パッケージ名を、色々考える。

このテンプレートを参考にして設定していく.

https://github.com/Kotlin/multiplatform-library-template/tree/main

を参考にして勧める。

https://zenn.dev/atsushieno/articles/d066e757c9640f
https://github.com/atsushieno/multiplatform-library-template-nexus-publisher

これを参考にしないとうまくいかなそう。

手順通りに `gpg --gen-key` したら

```
We need to generate a lot of random bytes. It is a good idea to perform
some other action (type on the keyboard, move the mouse, utilize the
disks) during the prime generation; this gives the random number
generator a better chance to gain enough entropy.
gpg: agent_genkey failed: Screen or window too small
Key generation failed: Screen or window too small
```

となって進まなくなったので、`gpgconf --kill gpg-agent` したら進んだ。

`gpg --gen-key` がダメなので、`gpg --list-keys`

keyserver.ubuntu.com にキー登録しようとしたが、うまくいかないので以下のようにした。

    gpg --send-keys --keyserver hkps://keys.openpgp.org <KEY>

`OSSRH_USERNAME` については https://central.sonatype.com/publishing/io.github.tokuhirom/users を参考にして
`github_21084` のような user ID のようなのでこれを使ってみる。

## 2024-07-17

mkkdary, kdary という2つの CLI を実装。使い慣れている clikt を実装する。

## 2024-07-18

最長一致法による分かち書きを行うためのサンプルコードを追加。

これで一通りのやりたかったことは完成。

## 2024-07-19

とりあえず double array 部分は安定して動くようになったので、つぎは形態素解析機を書いてみよう。
というか、そのために double array を移植したわけで。。

CRF のコスト計算は面倒なので、ありものの mecab の辞書をそのまま使おう。

mecab の辞書の形式は以下のページに記載されている。

https://taku910.github.io/mecab/dic-detail.html

    表層形
    左文脈ID (単語を左から見たときの文脈 ID)
    右文脈ID (単語を右から見たときの文脈 ID)
    単語コスト (小さいほど出現しやすい)

一旦、JVM ターゲットで実装しその後でツール作る感じかなぁ、。。

mecab-ipadic が EUC-JP なので、JVM じゃないと絶妙にだるいかも。
一旦、JVM ターゲットで書いてみて、あとで文字コード周りは考えるかなぁ。


    鯛焼,1285,1285,5618,名詞,一般,*,*,*,*,鯛焼,タイヤキ,タイヤキ

## 2024-07-21

taku-ku san の本のサンプルコードを参照しながら書いていく。
シンプルなビタビアルゴリズムの実装なので、そんなに難しくない。

"東京都" を形態素解析する例を上げると、以下のように、同じ文字列でも複数の解釈があることに注意。

    都,1293,1293,12159,名詞,固有名詞,地域,一般,*,*,都,ミヤコ,ミヤコ
    都,1285,1285,7241,名詞,一般,*,*,*,*,都,ト,ト
    都,1285,1285,7595,名詞,一般,*,*,*,*,都,ミヤコ,ミヤコ
    都,1303,1303,9428,名詞,接尾,地域,*,*,*,都,ト,ト
    都,1291,1291,11341,名詞,固有名詞,人名,名,*,*,都,ミヤコ,ミヤコ
    都,1290,1290,11474,名詞,固有名詞,人名,姓,*,*,都,ミヤコ,ミヤコ
