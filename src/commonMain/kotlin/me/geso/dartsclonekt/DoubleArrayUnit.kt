package me.geso.dartsclonekt

// <DoubleArrayUnit> はダブル配列ユニットの型であり、実際には<IdType>のラッパーです。
@JvmInline
value class DoubleArrayUnit(
    val unit: IdType = 0u,
) {
    // hasLeaf() はユニットからすぐに派生したリーフユニットかどうかを返します（trueの場合）またはそうでない場合（false）。
    fun hasLeaf(): Boolean {
        return ((unit.toInt() shr 8) and 1) == 1
    }

    // value() はユニットに格納されている値を返します。したがって、value() はユニットがリーフユニットである場合にのみ利用可能です。
    fun value(): ValueType {
        return (unit and ((1u shl 31) - 1u)).toInt()
    }

    // label() はユニットに関連付けられたラベルを返します。リーフユニットは常に無効なラベルを返します。この機能のために、リーフユニットのlabel() はMSBが1の<id_type>を返します。
    fun label(): IdType {
        return unit and ((1u shl 31) or 0xFFu)
    }

    // offset() はユニットから派生したユニットへのオフセットを返します。
    fun offset(): IdType {
        val shiftedUnit = (unit shr 10).toInt()
        val shiftedMask = ((unit and (1u shl 9)) shr 6).toInt()
        return (shiftedUnit shl shiftedMask).toUInt()
    }
}
