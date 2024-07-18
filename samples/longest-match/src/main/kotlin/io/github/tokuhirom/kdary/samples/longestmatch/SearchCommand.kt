package io.github.tokuhirom.kdary.samples.longestmatch

import com.github.ajalt.clikt.core.CliktCommand
import io.github.tokuhirom.kdary.KDary
import io.github.tokuhirom.kdary.loadKDary

class SearchCommand : CliktCommand() {
    val kdaryDict: KDary = loadKDary("noun.kdary")

    override fun run() {
        listOf(
            "すもももももももものうち",
            "吾輩は猫である",
            "東京特許許可局",
            "「前任者が採用した技術スタックを勉強したくない」を略しては技術的負債ってよんでる人、案外多い",
            "どのセッションが面白かったっすか？",
            "FacebookはMetaが提供している。",
            "「ディマカイルス」は、ゲーム『ストリートファイター6』に登場するキャラクター、マリーザの必殺技の一つです。",
        ).forEach { example ->
            println(longestMatch(example).joinToString(" / "))
        }
        println("EOS")
    }

    private fun longestMatch(line: String): Sequence<String> =
        sequence {
            val lineBytes = line.toByteArray(Charsets.UTF_8)
            var begin = 0
            while (begin < lineBytes.size) {
                val key = lineBytes.copyOfRange(begin, lineBytes.size)
                val longestLength = getLength(key)
                val word = lineBytes.copyOfRange(begin, begin + longestLength).toString(Charsets.UTF_8)
                yield(word)
                begin += longestLength
            }
        }

    private fun getLength(key: ByteArray): Int {
        val result = kdaryDict.commonPrefixSearch(key)
        return result.maxOfOrNull {
            it.length
        } ?: if (isAlnum(key[0])) {
            consumeAlnum(key, 0)
        } else if (isKatakana(takeFirstCharacter(key))) {
            consumeKatakana(key, 0)
        } else {
            getUtf8CharLength(key, 0)
        }
    }

    private fun takeFirstCharacter(bytes: ByteArray): Char {
        val charLength = getUtf8CharLength(bytes, 0)
        val charBytes = bytes.copyOfRange(0, charLength)
        return String(charBytes)
            .chars()
            .findFirst()
            .asInt
            .toChar()
    }

    private fun isAlnum(byte: Byte): Boolean {
        val char = byte.toInt().toChar()
        return char.isLetterOrDigit()
    }

    private fun isKatakana(char: Char): Boolean = char in '\u30A0'..'\u30FF'

    private fun consumeAlnum(
        bytes: ByteArray,
        offset: Int,
    ): Int {
        var length = 0
        while (offset + length < bytes.size && isAlnum(bytes[offset + length])) {
            length++
        }
        return length
    }

    private fun consumeKatakana(
        bytes: ByteArray,
        offset: Int,
    ): Int {
        var length = 0
        while (offset + length < bytes.size) {
            val charLength = getUtf8CharLength(bytes, offset + length)
            val charBytes = bytes.copyOfRange(offset + length, offset + length + charLength)
            val char = charBytes.toString(Charsets.UTF_8).first()
            if (!isKatakana(char)) {
                break
            }
            length += charLength
        }
        return length
    }

    private fun getUtf8CharLength(
        bytes: ByteArray,
        offset: Int,
    ): Int {
        val byte = bytes[offset]
        return when {
            byte.toInt() and 0x80 == 0x00 -> 1
            byte.toInt() and 0xE0 == 0xC0 -> 2
            byte.toInt() and 0xF0 == 0xE0 -> 3
            byte.toInt() and 0xF8 == 0xF0 -> 4
            else -> throw IllegalArgumentException("Invalid UTF-8 byte sequence")
        }
    }
}
