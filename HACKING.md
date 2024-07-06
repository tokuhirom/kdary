# どのように実装したか

darts-clone を chatgpt の gpt-4-o で kotlin にまず変換しました。


    fun DARTS_THROW(msg: String): Nothing = throw Darts.Exception("$msg")

まずこのようなコードが生成されましたが、Darts.Exception が存在しないので作成。



