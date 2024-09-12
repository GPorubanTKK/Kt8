/**
 * Tries to interpret a string first as a number, then as a character.
 * Examples:
 *  "00001000" -> 8
 *  "x08" -> 8
 *  "8" -> 8
 *  "R" -> 82
 *  @author RandomLonelyDev
 *  @since 1.0.0
 *  @param binPrefix The prefix denoting a binary number
 *  @param hexPrefix The prefix denoting a hex number
 *  @return An unsigned byte representing the numeric value of a string
 * */
internal fun String.toUByte(
    binPrefix: String,
    hexPrefix: String
): UByte {
    return if(startsWith(binPrefix))
            removePrefix(binPrefix).toUByte(2)
        else if(startsWith(hexPrefix))
            removePrefix(hexPrefix).toUByte(16)
        else toUByte(10)
}

/**
 * Determines how many bits a number is
 * @author RandomLonelyDev
 * @since 1.0.0
 * @param binPrefix The prefix denoting a binary number
 * @param hexPrefix The prefix denoting a hex number
 * @return The size of the number in bits
 * */
internal fun String.determineNumSize(binPrefix: String, hexPrefix: String): Int {
    return when {
        startsWith(binPrefix) -> removePrefix(binPrefix).length
        startsWith(hexPrefix) -> removePrefix(hexPrefix).length * 4
        else -> if(toInt(10) > 255) 16 else 8
    }
}

/**
 * Tries to interpret a string as a 16-bit short.
 *  @author RandomLonelyDev
 *  @since 1.1.0
 *  @param binPrefix The prefix denoting a binary number
 *  @param hexPrefix The prefix denoting a hex number
 *  @return An unsigned short representing the numeric value of a string
 *  @see String.toUByte(String,String)
 * */
internal fun String.toUShort(
    binPrefix: String,
    hexPrefix: String
): UShort {
    return when {
        startsWith(binPrefix) -> removePrefix(binPrefix).toUShort(2)
        startsWith(hexPrefix) -> removePrefix(hexPrefix).toUShort(16)
        else -> toUShort(10)
    }
}

/**
 * Turns an 8 or 16-bit value into an array of 8-bit values
 * @author RandomLonelyDev
 * @since 1.1.0
 * @param binPrefix The prefix denoting a binary number
 * @param hexPrefix The prefix denoting a hex number
 * @return An array of UBytes containing the number
 * */
internal fun String.toUBytes(
    binPrefix: String,
    hexPrefix: String
): Array<UByte> {
    return when(determineNumSize(binPrefix, hexPrefix)) {
        8 -> arrayOf(toUByte(binPrefix, hexPrefix), 0u)
        16 -> shortToTwoBytes(toUShort(binPrefix, hexPrefix))
        else -> throw NumberFormatException("Number of invalid size entered")
    }
}

/**
 * The string value of the ASCII character corresponding to this byte
 * @author RandomLonelyDev
 * @since 1.0.0
 * @return A string representing this byte's ASCII character
 * */
internal fun UByte.toStr() = toInt().toChar().toString()
