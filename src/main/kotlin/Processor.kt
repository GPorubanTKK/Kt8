/**
 * The Kt8 virtual processor <3
 * @author RandomLonelyDev
 * @since 1.0.0
 * @param memory The virtual memory to use
 * @param stackRange The range in ram where the stack is housed
 * @param programMemory The range in memory where programs are loaded
 * @param outputStream Where console text and output is written (defaults to standard print and println functions)
 * @param onStackUpdate A function to be called whenever the stack is updated
 * @param onMemoryUpdate A function to be called whenever the memory is updated
 * */
class Processor(
    private val memory: Ram,
    stackRange: UIntRange = 0u .. 1024u,
    private val programMemory: UIntRange = 16384u ..< 32768u,
    private val outputStream: WriteTarget,
    private val onStackUpdate: (Int) -> Unit = {},
    private val onMemoryUpdate: (Int) -> Unit = { _ -> }
) {
    private val stack = Stack(memory, stackRange.last - stackRange.first, stackRange.first)

    /**
     * The memory location of the current instruction
     * */
    private var programCounter: UShort = 0u

    /**
     * Clears the stack and memory, resetting the processor
     * @author RandomLonelyDev
     * @since 1.0.0
     * */
    fun reset() {
        stack.reset()
        memory.clear()
        onMemoryUpdate(0)
        onStackUpdate(stack.ptr)
    }

    /**
     * Runs a program from memory
     * @author RandomLonelyDev
     * @since 1.0.0
     * @param start The memory location of the first byte of the program header
     * @param hangCondition A callback specifying an external method to pause execution of the program
     * */
    fun execute(
        start: UShort = programMemory.first.toUShort(),
        hangCondition: (UInt) -> Boolean = { _ -> false }
    ) {
        if(!programMemory.contains(start)) throw Exception("SEGFAULT")
        val readStart = (start + 3u).toUShort()
        val header = memory.getRange(start, readStart)
        if(header[0].toInt() != 127) throw Exception("INVALID HEADER ${header.toList()}")
        val readEnd = (header[1] * 3u) + readStart
        println("First Byte: $start, Start Read: $readStart, End Read: $readEnd")
        programCounter = readStart
        //execution code goes here
        outputStream.println("\n")
    }

    /**
     * Represents a holder of a single numeric value
     * @author RandomLonelyDev
     * @since 1.0.0
     * */
    internal abstract class Register<in T : Register<T, V>, V : Comparable<V>> {

        /**
         * The private mutable variable
         * */
        protected abstract var pValue: V

        /**
         * Set the value of the register by a raw value
         * @author RandomLonelyDev
         * @since 1.0.0
         * @param value The value to set the register to
         * */
        open fun setByValue(value: V) { pValue = value }

        /**
         * Set the value of the register by copying the value of another value
         * @author RandomLonelyDev
         * @since 1.0.0
         * @param other The register to use
         * */
        open fun setByRegister(other: T) { pValue = other.pValue }

        /**
         * Get the register's value
         * @author RandomLonelyDev
         * @since 1.0.0
         * */
        open fun getValue(): V = pValue
    }

    /**
     * An eight-bit register
     * @author RandomLonelyDev
     * @since 1.0.0
     * */
    internal class EightBitRegister : Register<EightBitRegister, UByte>() {
        override var pValue: UByte = 0u
    }

    /**
     * A sixteen-bit register
     * @author RandomLonelyDev
     * @since 1.0.0
     * */
    internal class SixteenBitRegister : Register<SixteenBitRegister, UShort>() {
        override var pValue: UShort = 0u
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
        internal open fun pop(): UByte {
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
        internal fun peek(locToRead: Int): UByte {
            if(locToRead.toUInt() >= startingLocation + size || locToRead.toUInt() < startingLocation) throw NullPointerException()
            return ram[locToRead]
        }
    }
}