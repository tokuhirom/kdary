package me.geso.kdary.internal

import me.geso.kdary.IdType

/**
 * Node of Directed Acyclic Word Graph (DAWG).
 */
internal class DawgNode {
    internal var child: IdType = 0u
    internal var sibling: IdType = 0u
    internal var label: UByte = 0u
    internal var isState: Boolean = false
    internal var hasSibling: Boolean = false

    fun unit(): IdType =
        if (label == 0.toUByte()) {
            (child shl 1) or (if (hasSibling) 1u else 0u)
        } else {
            (child shl 2) or (if (isState) 2u else 0u) or (if (hasSibling) 1u else 0u)
        }
}
