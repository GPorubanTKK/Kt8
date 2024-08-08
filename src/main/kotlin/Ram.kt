class Ram(sizeInBytes: Int) {
    internal val memory = Array<UByte>(sizeInBytes) { 0u }
    fun load(bytes: Array<UByte>, startByte: Int) { for(byte in bytes.indices) memory[byte+startByte] = bytes[byte] }
    fun clear() = memory.fill(0u)
    fun getRange(from: UInt, to: UInt) = memory.copyOfRange(from.toInt(), to.toInt())
    operator fun get(index: Int) = memory[index]
    operator fun set(index: Int, value: UByte) { memory[index] = value }
}