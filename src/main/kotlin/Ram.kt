/**
 * A virtual representation of memory as an array of bytes
 * @author RandomLonelyDev
 * @since 1.0.0
 * @param sizeInBytes The total size of the memory to simulate
 * */
class Ram(sizeInBytes: Int) {

    private val memory = Array(sizeInBytes) { BitSpecificValue.of(0u) }

    /**
     * Puts a copy of the byte array into the memory
     * @author RandomLonelyDev
     * @since 1.0.0
     * @param bytes The byte array to load
     * @param startByte The position of the first byte to load
     * */
    fun load(bytes: Array<UByte>, startByte: Int) {
        for(i in bytes.indices) {
            val b = bytes[i]
            val index = i + startByte
            memory[index].setByByte(b)
        }
    }

    /**
     * Resets the whole memory unit to 0's
     * @author RandomLonelyDev
     * @since 1.0.0
     * */
    fun clear() = memory.forEach { it.setByByte(0u) }

    /**
     * Gets a copy of a range from memory
     * @author RandomLonelyDev
     * @since 1.0.0
     * @param from The start of the range (inclusive)
     * @param to The end of the range (exclusive)
     * @return A copy of a range from memory [to, from)
     * */
    fun getRange(from: UShort, to: UShort) = memory.copyOfRange(from.toInt(), to.toInt()).map(BitSpecificValue::toByte)

    /**
     * Gets a specific index in memory
     * @author RandomLonelyDev
     * @since 1.0.0
     * @param index The index to retrieve
     * @return The byte stored in the index
     * */
    operator fun get(index: UShort) = memory[index.toInt()]
    operator fun get(index: UByte) = memory[index.toInt()]

    /**
     * Overwrites a specific index in memory with a new value
     * @author RandomLonelyDev
     * @since 1.0.0
     * @param index The index to overwrite
     * @param value The value to write
     * */
    operator fun set(index: UShort, value: UByte) { memory[index.toInt()].setByByte(value) }
    operator fun set(index: UShort, value: BitSpecificValue) { memory[index.toInt()] = value }

    fun toList() = memory.asList()
}