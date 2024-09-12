import java.lang.StringBuilder

/**
 * The exception that should be thrown in case of a compile-time error.
 * @author RandomLonelyDev
 * @since 1.0.0
 * @param msg An optional message to be shown when this exception is thrown
 * */
internal class GCompileException(msg: String = "") : Exception(msg)
/**
 * The exception that should be thrown in case of a runtime error
 * */
internal class GRuntimeException(msg: String = "") : Exception(msg)

/**
 * A simulated output stream which allows for custom redirects
 * @author RandomLonelyDev
 * @since 1.0.0
 * */
interface WriteTarget {
    /**
     * Prints a string to the output location
     * @author RandomLonelyDev
     * @since 1.0.0
     * @param str The string to print
     * */
    fun print(str: String) = kotlin.io.print(str)
    /**
     * Prints a string followed by a newline (\n) character to the output location
     * @author RandomLonelyDev
     * @since 1.0.0
     * @param str The string to print
     * */
    fun println(str: String) = print("$str\n")
    companion object
}

/**
 * A custom implementation of a first in last out data structure for use with the Kt8 virtual memory
 * @author RandomLonelyDev
 * @since 1.0.0
 * @param ram The virtual memory to use
 * @param size The size of the stack
 * @param startingLocation The first location in memory allocated to the stack
 * */
internal open class Stack(private val ram: Ram, private val size: UInt, private val startingLocation: UInt) {
    private var pointer = (startingLocation + size - 1u).toInt()

    /**
     * A public getter for the pointer
     * @author RandomLonelyDev
     * @since 1.0.0
     * */
    val ptr: Int get() = pointer

    /**
     * Put a value onto the top of the stack
     * @author RandomLonelyDev
     * @since 1.0.0
     * @param value The value to push
     * */
    internal open fun push(value: UByte) {
        checkPointer()
        ram[pointer--] = value
    }

    /**
     * Take a value off the top of the stack
     * @author RandomLonelyDev
     * @since 1.0.0
     * @return The top value of the stack
     * */
    internal open fun pop(): BitSpecificValue {
        checkPointer()
        val toReturn = ram[++pointer]
        ram[pointer] = 0u
        return toReturn
    }

    /**
     * Make sure the pointer remains inside the memory allocated for the stack
     * */
    private fun checkPointer() = if(pointer.toUInt() >= startingLocation + size || pointer.toUInt() < startingLocation) throw StackOverflowError() else Unit

    /**
     * Reset the pointer to the beginning of the stack
     * */
    internal fun reset() { pointer = (startingLocation + size - 1u).toInt() }

    /**
     * Read a value in the range of the stack memory without removing it
     * @author RandomLonelyDev
     * @since 1.0.0
     * @param locToRead The location inside the stack to read
     * @return The value at that location
     * */
    internal fun peek(locToRead: Int): BitSpecificValue {
        if(locToRead.toUInt() >= startingLocation + size || locToRead.toUInt() < startingLocation) throw NullPointerException()
        return ram[locToRead]
    }
}

class BitSpecificValue(private val bits: Array<Boolean> = Array(8) { false }) : Comparable<BitSpecificValue> {
    companion object {
        val ZERO
            get() = BitSpecificValue()
        fun of(byte: UByte): BitSpecificValue = BitSpecificValue().apply { setByByte(byte) }
    }

    //bits are highest to lowest
    init {
        require(bits.size == 8)
    }

    fun setBit(pos: Int, value: Boolean) {
        bits[pos] = value
    }

    fun setBit(position: Position, value: Boolean) {
        bits[position.ordinal] = value
    }

    fun getBit(pos: Int) = bits[pos]
    fun getBit(position: Position) = bits[position.ordinal]

    fun toByte(): UByte {
        var toReturn: UByte = 0u
        for(bitPos in bits.indices) {
            val bit = bits[bitPos]
            toReturn = (toReturn + if(bit) Position.entries[bitPos].value else 0u).toUByte()
        }
        return toReturn
    }

    fun setByByte(value: UByte) {
        var mutValue = value
        for(pos in bits.indices) {
            val bitValue = Position.entries[pos].value
            if(mutValue >= bitValue) {
                bits[pos] = true
                mutValue = (mutValue % bitValue).toUByte()
            }
        }
    }

    override fun compareTo(other: BitSpecificValue): Int = this.toByte().compareTo(other.toByte())

    enum class Position(internal val value: UByte) {
        `128`(128u), //N
        `64`(64u), //V
        `32`(32u), //-
        `16`(16u), //B
        `8`(8u), //D
        `4`(4u), //I
        `2`(2u), //Z
        `1`(1u) //C
    }

    override fun toString(): String {
        val builder = StringBuilder()
        for(bit in bits.indices) builder.append("Pos: ${Position.entries[bit]} Value: ${bits[bit]}\n")
        return builder.toString()
    }

    fun bitShiftRight(): Boolean = rotateByteRight(false)

    fun bitShiftLeft(): Boolean = rotateByteLeft(false)

    fun rotateByteRight(shiftIn: Boolean): Boolean {
        val toReturn = bits[7]
        for(i in 7 downTo 1) bits[i] = bits[i-1]
        bits[0] = shiftIn
        return toReturn
    }

    fun rotateByteLeft(shiftIn: Boolean): Boolean {
        val toReturn = bits[0]
        for(i in 0 until 7) bits[i] = bits[i+1]
        bits[7] = shiftIn
        return toReturn
    }

    fun plus(other: BitSpecificValue, carryIn: Boolean = false): Triple<BitSpecificValue, Boolean, Boolean> { //val, c flag, v flag
        val bitRepresentation = Array(8) { false }
        var carry = carryIn
        for(i in 7 downTo 0) {
            bitRepresentation[i] = (bits[i] xor other.bits[i]) xor carry
            carry = (bits[i] && other.bits[i]) || (carry && (bits[i] xor other.bits[i]))
        }
        return Triple(BitSpecificValue(bitRepresentation), carry, (!bits[0] && !other.bits[0] && bitRepresentation[0]) || (bits[0] && other.bits[0] && !bitRepresentation[0]))
    }

    //TODO: Significant debugging
    fun minus(other: BitSpecificValue, carryIn: Boolean = false): Triple<BitSpecificValue, Boolean, Boolean> { //val, c flag, v flag
        val bitRepresentation = Array(8) { false }
        var borrow = !carryIn
        for(i in 7 downTo 0) {
            bitRepresentation[i] = (bits[i] xor other.bits[i]) xor borrow
            borrow = (!bits[i] && other.bits[i]) || (!(bits[i] xor other.bits[i]) && borrow)
        }
        return Triple(BitSpecificValue(bitRepresentation), !borrow, (!bits[0] && !other.bits[0] && bitRepresentation[0]) || (bits[0] && other.bits[0] && !bitRepresentation[0]))
    }

    override fun equals(other: Any?): Boolean {
        if(other !is BitSpecificValue) return false //not a bsv
        for(i in bits.indices) if(bits[i] != other.bits[i]) return false //return if any bits do not match
        return true //it is bsv and all bits match
    }

    override fun hashCode(): Int {
        return bits.contentHashCode()
    }
}