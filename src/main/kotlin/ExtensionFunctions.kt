/**
 * Tries to interpret a string first as a number, then as a character.
 * Examples:
 *  "00001000" -> 8
 *  "x08" -> 8
 *  "8" -> 8
 *  "R" -> 82
 *  @author RandomLonelyDev
 *  @since 1.0.0
 *  @return An unsigned byte representing the numeric value of a string, or a ascii code of the first element if the string is not a number
 * */
internal fun String.toUByte(): UByte {
    return try {
        if(startsWith("%"))
            toUByte(2)
        else if(startsWith("x"))
            removePrefix("x").toUByte(16)
        else toUByte(10)
    } catch(_: NumberFormatException) {
        this[0].code.toUByte()
    }
}

/**
 * The string value of the ASCII character corresponding to this byte
 * @author RandomLonelyDev
 * @since 1.0.0
 * @return A string representing this byte's ASCII character
 * */
internal fun UByte.toStr() = toInt().toChar().toString()
