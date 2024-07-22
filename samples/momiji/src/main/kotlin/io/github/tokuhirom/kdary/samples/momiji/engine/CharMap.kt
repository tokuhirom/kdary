package io.github.tokuhirom.kdary.samples.momiji.engine

import java.io.File

class CharMap(
    charCategories: List<CharCategory>,
    codepointRanges: List<CodepointRange>,
) {
    private val categories: Map<String, CharCategory> =
        charCategories.associateBy {
            it.name
        }
    private val ranges: List<Pair<CharRange, String>> =
        codepointRanges
            .map {
                (it.start.toChar()..it.end.toChar()) to it.defaultCategory
            }.toList()

    fun resolve(char: Char): CharCategory? =
        ranges
            .firstOrNull {
                it.first.contains(char)
            }?.let {
                categories[it.second]!!
            }

    override fun toString(): String = "CharMap(categories=$categories, ranges=$ranges)"
}

/**
 * - CATEGORY_NAME: Name of category. you have to define DEFAULT class.
 * - INVOKE: 1/0:   always invoke unknown word processing, evan when the word can be found in the lexicon
 * - GROUP:  1/0:   make a new word by grouping the same chracter category
 * - LENGTH: n:     1 to n length new words are added
 */
data class CharCategory(
    val name: String,
    val alwaysInvoke: Int,
    val grouping: Int,
    val length: Int,
)

data class CodepointRange(
    val start: Int,
    val end: Int,
    val defaultCategory: String,
    val compatibleCategories: List<String> = listOf(),
)

fun parseCharDef(file: File): CharMap = parseCharDef(file.readText())

fun parseCharDef(src: String): CharMap {
    val categories = mutableListOf<CharCategory>()
    val codepoints = mutableListOf<CodepointRange>()

    src.split("\n").forEach { line ->
        val trimmedLine = line.trim().replace("#.*".toRegex(), "")

        // コメント行や空行をスキップ
        if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) return@forEach

        // カテゴリ定義のパース
        val categoryMatch = Regex("""^([A-Z]+)\s+(\d)\s+(\d)\s+(\d+)$""").matchEntire(trimmedLine)
        if (categoryMatch != null) {
            val (name, timing, grouping, length) = categoryMatch.destructured
            categories.add(CharCategory(name, timing.toInt(), grouping.toInt(), length.toInt()))
            return@forEach
        }

        // コードポイント定義のパース
        val codepointMatch = Regex("""^0x([0-9A-Fa-f]+)(?:\.\.0x([0-9A-Fa-f]+))?\s+([A-Z]+)(.*)$""").matchEntire(trimmedLine)
        if (codepointMatch != null) {
            val (start, end, defaultCategory, compatibleCategories) = codepointMatch.destructured
            val startInt = start.toInt(16)
            val endInt = if (end.isEmpty()) startInt else end.toInt(16)
            val compatibleList = compatibleCategories.trim().split("\\s+").filter { it.isNotEmpty() }
            codepoints.add(CodepointRange(startInt, endInt, defaultCategory, compatibleList))
        }
    }

    return CharMap(categories, codepoints)
}
