package me.geso.kdary

class DawgBuilder {
    private val nodes = AutoPool<DawgNode>()
    private val units = AutoPool<DawgUnit>()
    private val labels = AutoPool<UCharType>()
    private val isIntersections = BitVector()
    private val table = AutoPool<IdType>()
    private val nodeStack = AutoStack<IdType>()
    private val recycleBin = AutoStack<IdType>()
    private var numStates: Int = 0

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

    fun intersectionId(id: IdType): IdType = isIntersections.rank(id) - 1u

    fun numIntersections(): Int = isIntersections.numOnes().toInt()

    fun size(): Int = units.size()

    fun init() {
        table.resize(INITIAL_TABLE_SIZE, 0u)

        appendNode()
        appendUnit()

        numStates = 1

        nodes[0].setLabel(0xFF.toUByte())
        nodeStack.push(0u)
    }

    fun finish() {
        flush(0u)

        units[0] = DawgUnit(nodes[0].unit())
//        units[0u] = nodes[0u].unit()
        labels[0] = nodes[0].label()

        nodes.clear()
        table.clear()
        nodeStack.clear()
        recycleBin.clear()

        isIntersections.build()
    }

    fun insert(
        key: String,
        length: Int,
        value: ValueType,
    ) {
        if (value < 0) {
            throw IllegalArgumentException("failed to insert key: negative value")
        } else if (length == 0) {
            throw IllegalArgumentException("failed to insert key: zero-length key")
        }

        var id = 0u
        var keyPos = 0

        while (keyPos <= length) {
            val childId = nodes[id.toInt()].child()
            if (childId == 0u) {
                break
            }

            val keyLabel = if (keyPos < length) key[keyPos].code.toUByte() else 0u
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
            val keyLabel = if (keyPos < length) key[keyPos].code.toUByte() else 0u
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
    }

    fun clear() {
        nodes.clear()
        units.clear()
        labels.clear()
        isIntersections.clear()
        table.clear()
        nodeStack.clear()
        recycleBin.clear()
        numStates = 0
    }

    private fun flush(id: IdType) {
        while (nodeStack.top() != id) {
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

            val (hashId, matchId) = findNode(nodeId)
            if (matchId != 0u) {
                isIntersections.set(matchId, true)
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
                val newMatchId = unitId + 1u
                table[hashId.toInt()] = newMatchId
                numStates++
            }

            i = nodeId
            while (i != 0u) {
                val next = nodes[i.toInt()].sibling()
                freeNode(i)
                i = next
            }

            nodes[nodeStack.top().toInt()].setChild(matchId)
        }
        nodeStack.pop()
    }

    private fun expandTable() {
        val tableSize = table.size() shl 1
        table.clear()
        table.resize(tableSize, 0u)

        for (i in 1 until units.size()) {
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
        var hashId = hashNode(nodeId) % table.size().toUInt()
        while (true) {
            val unitId = table[hashId.toInt()]
            if (unitId == 0u) {
                break
            }

            if (areEqual(nodeId, unitId)) {
                return hashId to unitId
            }

            hashId = (hashId + 1u) % table.size().toUInt()
        }
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

    private fun hashUnit(id: IdType): IdType {
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
        var hashValue: IdType = 0u
        var currentId = id
        while (currentId != 0u) {
            val unit = nodes[currentId.toInt()].unit()
            val label = nodes[currentId.toInt()].label()
            hashValue = hashValue xor hash((label.toUInt() shl 24) xor unit)
            currentId = nodes[currentId.toInt()].sibling()
        }
        return hashValue
    }

    fun appendNode(): IdType {
        val id: IdType
        if (recycleBin.isEmpty()) {
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
        return isIntersections.size() - 1u
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