class Ram(sizeInBytes: Int) {
    val memory = Array<UByte>(sizeInBytes) { 0u }
    fun load(bytes: Array<UByte>, startByte: Int) { for(byte in bytes.indices) memory[byte+startByte] = bytes[byte] }
}