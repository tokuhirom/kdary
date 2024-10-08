package io.github.tokuhirom.kdary.internal

internal class DawgBuilder {
    private val nodes = mutableListOf<DawgNode>()
    private val units = mutableListOf<DawgUnit>()
    private val labels = mutableListOf<UByte>()
    private val isIntersectionsBuilder = BitVectorBuilder()
    private val table = mutableListOf<IdType>()
    private val nodeStack = mutableListOf<Int>()
    private val recycleBin = mutableListOf<Int>()

    // TODO Int
    private var numStates: SizeType = 0u

    init {
        table.resize(INITIAL_TABLE_SIZE, 0u)
        check(recycleBin.isEmpty())

        nodes.add(DawgNode())

        appendUnit()
        numStates = 1u
        nodes[0].label = 0xFF.toUByte()
        nodeStack.add(0)
    }

    fun finish(): Dawg {
        flush(0)

        units[0] = DawgUnit(nodes[0].unit())
        labels[0] = nodes[0].label

        nodes.clear()
        table.clear()
        nodeStack.clear()
        recycleBin.clear()

        val isIntersections = isIntersectionsBuilder.build()

        return Dawg(units, labels, isIntersections)
    }

    fun insert(
        key: ByteArray,
        value: ValueType,
    ) {
        val length = key.size
        if (value < 0) {
            throw IllegalArgumentException("failed to insert key: negative value")
        } else if (length == 0) {
            throw IllegalArgumentException("failed to insert key: zero-length key")
        }

        var id = 0
        var keyPos = 0

        while (keyPos <= length) {
            val childId = nodes[id].child.toInt()
            if (childId == 0) {
                break
            }

            val keyLabel = if (keyPos < length) key[keyPos].toUByte() else 0u
            if (keyPos < length && keyLabel == 0.toUByte()) {
                throw IllegalArgumentException("failed to insert key: invalid null character")
            }

            val unitLabel = nodes[childId].label
            if (keyLabel < unitLabel) {
                throw IllegalArgumentException("failed to insert key: wrong key order")
            } else if (keyLabel > unitLabel) {
                nodes[childId].hasSibling = true
                flush(childId)
                break
            }
            id = childId
            keyPos++
        }

        if (keyPos > length) {
            return
        }

        while (keyPos <= length) {
            val keyLabel = if (keyPos < length) key[keyPos].toUByte() else 0u
            val childId = appendNode()

            if (nodes[id].child == 0u) {
                nodes[childId].isState = true
            }
            nodes[childId].sibling = nodes[id].child.toInt()
            nodes[childId].label = keyLabel
            nodes[id].child = childId.toUInt()
            nodeStack.add(childId)

            id = childId
            keyPos++
        }
        nodes[id].child = value.toIdType()
    }

    private fun flush(id: Int) {
        while (nodeStack[nodeStack.size - 1] != id) {
            val nodeId = nodeStack[nodeStack.size - 1]
            nodeStack.removeLast()

            if (numStates >= table.size.toSizeType() - (table.size.toSizeType() shr 2)) {
                expandTable()
            }

            var numSiblings = 0
            var i: Int = nodeId
            while (i != 0) {
                numSiblings++
                i = nodes[i].sibling
            }

            // TODO: matchId should be Int
            var (hashId, matchId) = findNode(nodeId)
            if (matchId != 0u) {
                isIntersectionsBuilder.set(matchId.toInt(), true)
            } else {
                var unitId = 0
                for (j in 0 until numSiblings) {
                    unitId = appendUnit()
                }
                i = nodeId
                while (i != 0) {
                    units[unitId] = DawgUnit(nodes[i].unit())
                    labels[unitId] = nodes[i].label
                    unitId--
                    i = nodes[i].sibling
                }
                matchId = unitId.toUInt() + 1u
                table[hashId.toInt()] = matchId
                numStates++
            }

            i = nodeId
            while (i != 0) {
                val next = nodes[i].sibling
                freeNode(i)
                i = next
            }

            nodes[nodeStack[nodeStack.size - 1]].child = matchId
        }
        nodeStack.removeLast()
    }

    private fun expandTable() {
        val tableSize = table.size shl 1
        table.clear()
        table.resize(tableSize, 0u)

        for (id in 1 until units.size) {
            if (labels[id] == 0.toUByte() || units[id].isState()) {
                val (hashId, _) = findUnit(id)
                table[hashId.toInt()] = id.toUInt()
            }
        }
    }

    private fun findUnit(id: Int): Pair<UInt, UInt> {
        var hashId = hashUnit(id) % table.size.toUInt()
        while (true) {
            val unitId = table[hashId.toInt()]
            if (unitId == 0u) {
                break
            }
            hashId = (hashId + 1u) % table.size.toUInt()
        }
        return hashId to 0u
    }

    private fun findNode(nodeId: Int): Pair<UInt, IdType> {
        var hashId = hashNode(nodeId) % table.size.toUInt()
        while (true) {
            val unitId = table[hashId.toInt()]
            if (unitId == 0u) {
                break
            }

            if (areEqual(nodeId, unitId)) {
                return hashId to unitId
            }

            hashId = (hashId + 1u) % table.size.toUInt()
        }
        return hashId to 0u
    }

    private fun areEqual(
        nodeId: Int,
        unitId: IdType,
    ): Boolean {
        var unitIdVar: Int = unitId.toInt()
        var i = nodes[nodeId].sibling
        while (i != 0) {
            if (!units[unitIdVar].hasSibling()) {
                return false
            }
            unitIdVar++
            i = nodes[i].sibling
        }
        if (units[unitIdVar].hasSibling()) {
            return false
        }

        i = nodeId
        while (i != 0) {
            if (nodes[i].unit() != units[unitIdVar].unit() ||
                nodes[i].label != labels[unitIdVar]
            ) {
                return false
            }
            unitIdVar--
            i = nodes[i].sibling
        }
        return true
    }

    private fun hashUnit(id: Int): IdType {
        var hashValue: IdType = 0u
        var currentId = id
        while (currentId != 0) {
            val unit = units[currentId].unit()
            val label = labels[currentId]
            hashValue = hashValue xor hash((label.toUInt() shl 24) xor unit)

            if (!units[currentId].hasSibling()) {
                break
            }
            currentId++
        }
        return hashValue
    }

    private fun hashNode(id: Int): IdType {
        var hashValue: IdType = 0u
        var currentId: Int = id
        while (currentId != 0) {
            val unit = nodes[currentId].unit()
            val label = nodes[currentId].label
            hashValue = hashValue xor hash((label.toUInt() shl 24) xor unit)
            currentId = nodes[currentId].sibling
        }
        return hashValue
    }

    private fun appendNode(): Int =
        if (recycleBin.isEmpty()) {
            val id = nodes.size
            nodes.add(DawgNode())
            id
        } else {
            val id = recycleBin[recycleBin.size - 1]
            nodes[id] = DawgNode()
            recycleBin.removeLast()
            id
        }

    private fun appendUnit(): Int {
        isIntersectionsBuilder.append()
        units.add(DawgUnit(0u))
        labels.add(0.toUByte())
        return isIntersectionsBuilder.size() - 1
    }

    private fun freeNode(id: Int) {
        recycleBin.add(id)
    }

    companion object {
        private const val INITIAL_TABLE_SIZE = 1 shl 10

        private fun hash(key: IdType): IdType {
            var key = key
            key = key.inv() + (key shl 15)
            key = key xor (key shr 12)
            key += (key shl 2)
            key = key xor (key shr 4)
            key *= 2057u
            key = key xor (key shr 16)
            return key
        }
    }
}
