@file:OptIn(ExperimentalUnsignedTypes::class)

package me.geso.kdary

private fun debug(message: String) {
//    println("[D] $message")
}

class DawgBuilder {
    internal val nodes = AutoPool<DawgNode>()
    internal val units = AutoPool<DawgUnit>()
    internal val labels = AutoPool<UCharType>()
    internal val isIntersections = BitVector()
    private val table = AutoPool<IdType>()
    private val nodeStack = AutoStack<IdType>()
    private val recycleBin = AutoStack<IdType>()
    private var numStates: SizeType = 0u

    init {
        clear()
    }

    fun root(): IdType = 0u

    fun child(id: IdType): IdType = units[id.toInt()].child()

    fun sibling(id: IdType): IdType = if (units[id.toInt()].hasSibling()) (id + 1u) else 0u

    fun value(id: IdType): ValueType = units[id.toInt()].value()

    fun isLeaf(id: IdType): Boolean = label(id) == 0.toUByte()

    fun label(id: IdType): UCharType = labels[id.toInt()]

    fun isIntersection(id: IdType): Boolean = isIntersections[id]

    fun intersectionId(id: IdType): IdType = isIntersections.rank(id.toSizeType()) - 1u

    fun numIntersections(): Int = isIntersections.numOnes().toInt()

    fun size(): SizeType = units.size()

    fun init() {
        table.resize(INITIAL_TABLE_SIZE.toSizeType(), 0u)

        appendNode()
        appendUnit()

        numStates = 1u

        nodes[0].setLabel(0xFF.toUByte())
        nodeStack.push(0u)
    }

    fun finish() {
        flush(0u)

        units[0] = DawgUnit(nodes[0].unit())
        labels[0] = nodes[0].label()

        nodes.clear()
        table.clear()
        nodeStack.clear()
        recycleBin.clear()

        isIntersections.build()

        // 結局、units, labels, isIntersections, numStates が残る。
    }

    fun insert(
        key: UByteArray,
        length: SizeType,
        value: ValueType,
    ) {
        debug("insert(BEGIN $length)")
        if (value < 0) {
            throw IllegalArgumentException("failed to insert key: negative value")
        } else if (length == 0uL) {
            throw IllegalArgumentException("failed to insert key: zero-length key")
        }

        var id = 0u
        var keyPos: SizeType = 0uL

        while (keyPos <= length) {
            val childId = nodes[id.toInt()].child()
            if (childId == 0u) {
                break
            }

            val keyLabel = if (keyPos < length) key[keyPos.toInt()] else 0u
            if (keyPos < length && keyLabel == 0.toUByte()) {
                throw IllegalArgumentException("failed to insert key: invalid null character")
            }

            val unitLabel = nodes[childId.toInt()].label()
            if (keyLabel < unitLabel) {
                throw IllegalArgumentException("failed to insert key: wrong key order")
            } else if (keyLabel > unitLabel) {
                nodes[childId.toInt()].setHasSibling(true)
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
            val keyLabel = if (keyPos < length) key[keyPos.toInt()] else 0u
            val childId = appendNode()

            if (nodes[id.toInt()].child() == 0u) {
                nodes[childId.toInt()].setIsState(true)
            }
            nodes[childId.toInt()].setSibling(nodes[id.toInt()].child())
            nodes[childId.toInt()].setLabel(keyLabel)
            nodes[id.toInt()].setChild(childId)
            nodeStack.push(childId)

            id = childId
            keyPos++
        }
        nodes[id.toInt()].setValue(value)
        debug("insert(END $length)")
    }

    fun clear() {
        nodes.clear()
        units.clear()
        labels.clear()
        isIntersections.clear()
        table.clear()
        nodeStack.clear()
        recycleBin.clear()
        numStates = 0u
    }

    private fun flush(id: IdType) {
        debug("BEGIN flush($id)")
        while (nodeStack.top() != id) {
            debug("nodes[3].unit=${nodes[3].unit()}")
            val nodeId = nodeStack.top()
            nodeStack.pop()

            if (numStates >= table.size() - (table.size() shr 2)) {
                expandTable()
            }

            var numSiblings: IdType = 0u
            var i = nodeId
            while (i != 0u) {
                numSiblings++
                i = nodes[i.toInt()].sibling()
            }

            var (hashId, matchId) = findNode(nodeId)
            if (matchId != 0u) {
                isIntersections.set(matchId.toSizeType(), true)
            } else {
                var unitId: IdType = 0u
                for (j in 0 until numSiblings.toInt()) {
                    unitId = appendUnit()
                }
                i = nodeId
                while (i != 0u) {
                    units[unitId.toInt()] = DawgUnit(nodes[i.toInt()].unit())
                    labels[unitId.toInt()] = nodes[i.toInt()].label()
                    unitId--
                    i = nodes[i.toInt()].sibling()
                }
                matchId = unitId + 1u
                table[hashId.toInt()] = matchId
                numStates++
            }

            i = nodeId
            while (i != 0u) {
                val next = nodes[i.toInt()].sibling()
                freeNode(i)
                i = next
            }

            debug("nodeStack.top=${nodeStack.top()}, matchId=$matchId")
            nodes[nodeStack.top().toInt()].setChild(matchId)
        }
        nodeStack.pop()
        debug("END flush($id)")
    }

    private fun expandTable() {
        debug("BEGIN expandTable()")
        val tableSize = table.size() shl 1
        table.clear()
        table.resize(tableSize, 0u)

        for (i in 1uL until units.size()) {
            val id = i.toUInt()
            if (labels[id.toInt()] == 0.toUByte() || units[id.toInt()].isState()) {
                val (hashId, _) = findUnit(id)
                table[hashId.toInt()] = id
            }
        }
    }

    // 引数でポインタを受け取っていたところを多値を返すように変更
    private fun findUnit(id: IdType): Pair<UInt, UInt> {
        var hashId = hashUnit(id) % table.size().toUInt()
        while (true) {
            val unitId = table[hashId.toInt()]
            if (unitId == 0u) {
                break
            }
            hashId = (hashId + 1u) % table.size().toUInt()
        }
        return hashId to 0u
    }

    // 引数でポインタを受け取っていたところを多値を返すように変更
    private fun findNode(nodeId: IdType): Pair<UInt, IdType> {
        debug("BEGIN findNode($nodeId)")
        var hashId = hashNode(nodeId) % table.size().toUInt()
        debug("findNode, first hashId: $nodeId, $hashId")
        while (true) {
            debug("findNode, hashId=$hashId")
            val unitId = table[hashId.toInt()]
            if (unitId == 0u) {
                debug("unit_id=0 $hashId")
                break
            }

            if (areEqual(nodeId, unitId)) {
                debug("END findNode($nodeId) -> ($hashId, $unitId)")
                return hashId to unitId
            }

            hashId = (hashId + 1u) % table.size().toUInt()
        }
        debug("END findNode($nodeId) -> ($hashId, 0u)")
        return hashId to 0u
    }

    private fun areEqual(
        nodeId: IdType,
        unitId: IdType,
    ): Boolean {
        var unitIdVar = unitId
        var i = nodes[nodeId.toInt()].sibling()
        while (i != 0u) {
            if (!units[unitIdVar.toInt()].hasSibling()) {
                return false
            }
            unitIdVar++
            i = nodes[i.toInt()].sibling()
        }
        if (units[unitIdVar.toInt()].hasSibling()) {
            return false
        }

        i = nodeId
        while (i != 0u) {
            if (nodes[i.toInt()].unit() != units[unitIdVar.toInt()].unit() ||
                nodes[i.toInt()].label() != labels[unitIdVar.toInt()]
            ) {
                return false
            }
            unitIdVar--
            i = nodes[i.toInt()].sibling()
        }
        return true
    }

    internal fun hashUnit(id: IdType): IdType {
        var hashValue: IdType = 0u
        var currentId = id
        while (currentId != 0u) {
            val unit = units[currentId.toInt()].unit()
            val label = labels[currentId.toInt()]
            hashValue = hashValue xor hash((label.toUInt() shl 24) xor unit)

            if (!units[currentId.toInt()].hasSibling()) {
                break
            }
            currentId++
        }
        return hashValue
    }

    private fun hashNode(id: IdType): IdType {
        debug("BEGIN hash_node($id)")
        var hashValue: IdType = 0u
        var currentId = id
        while (currentId != 0u) {
            val unit = nodes[currentId.toInt()].unit()
            val label = nodes[currentId.toInt()].label()
            debug("xor(BEGIN)::: currentId=$currentId, $hashValue, $label, unit=$unit")
            hashValue = hashValue xor hash((label.toUInt() shl 24) xor unit)
            debug("xor(END)::: $hashValue")
            currentId = nodes[currentId.toInt()].sibling()
        }
        debug("hashNode($currentId)=$hashValue")
        return hashValue
    }

    private fun appendNode(): IdType {
        val id: IdType
        if (recycleBin.empty()) {
            id = nodes.size().toUInt()
            nodes.append(DawgNode())
        } else {
            id = recycleBin.top()
            nodes[id.toInt()] = DawgNode()
            recycleBin.pop()
        }
        return id
    }

    private fun appendUnit(): IdType {
        isIntersections.append()
        units.append(DawgUnit()) // Initialize with default constructor
        labels.append(0.toUByte()) // Initialize with default value
        return (isIntersections.size() - 1uL).toIdType()
    }

    private fun freeNode(id: IdType) {
        recycleBin.push(id)
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
