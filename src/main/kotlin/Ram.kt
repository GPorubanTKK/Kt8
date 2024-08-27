/**
 * A virtual representation of memory as an array of bytes
 * @author RandomLonelyDev
 * @since 1.0.0
 * @param sizeInBytes The total size of the memory to simulate
 * */
class Ram(sizeInBytes: Int) {

    private val memory = Array<UByte>(sizeInBytes) { 0u }

    /**
     * Puts a copy of the byte array into the memory
     * @author RandomLonelyDev
     * @since 1.0.0
     * @param bytes The byte array to load
     * @param startByte The position of the first byte to load
     * */
    fun load(bytes: Array<UByte>, startByte: Int) {
        for(byte in bytes.indices) {
            val b = bytes[byte]
            val index = byte+startByte
            memory[index] = b
        }
    }

    /**
     * Resets the whole memory unit to 0's
     * @author RandomLonelyDev
     * @since 1.0.0
     * */
    fun clear() = memory.fill(0u)

    /**
     * Gets a copy of a range from memory
     * @author RandomLonelyDev
     * @since 1.0.0
     * @param from The start of the range (inclusive)
     * @param to The end of the range (exclusive)
     * @return A copy of a range from memory [to, from)
     * */
    fun getRange(from: UShort, to: UShort) = memory.copyOfRange(from.toInt(), to.toInt())

    /**
     * Gets a specific index in memory
     * @author RandomLonelyDev
     * @since 1.0.0
     * @param index The index to retrieve
     * @return The byte stored in the index
     * */
    operator fun get(index: Int) = memory[index]

    /**
     * Overwrites a specific index in memory with a new value
     * @author RandomLonelyDev
     * @since 1.0.0
     * @param index The index to overwrite
     * @param value The value to write
     * */
    operator fun set(index: Int, value: UByte) { memory[index] = value }
}