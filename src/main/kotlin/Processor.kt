class Processor(
    private val memory: Ram,
    stackRange: UIntRange = 1u .. 1025u, //so stack ptr is not negative
    private val programMemory: UIntRange = 16384U ..< 32768U,
    private val outputStream: WriteTarget = object : WriteTarget { override fun print(str: String) = kotlin.io.print(str) }
) {
    private val stack = Stack(memory, stackRange.last - stackRange.first, stackRange.first)
    private var programCounter = 1u
    private val registers = mapOf(
        "A" to EightBitRegister(), //A-F general purpose 8bit
        "B" to EightBitRegister(),
        "C" to EightBitRegister(),
        "D" to EightBitRegister(),
        "E" to EightBitRegister(),
        "F" to EightBitRegister(),
        "R" to SixteenBitRegister()
    )
    fun execute(
        start: UInt = programMemory.first,
        hangCondition: (UInt) -> Boolean = { _ -> false }
    ) {
        if(!programMemory.contains(start)) throw Exception("SEGFAULT")
        val readStart = start + 3u
        val header = memory.getRange(start, readStart)
        if(header[0].toInt() != 127) throw Exception("INVALID HEADER ${header.toList()}")
        val readEnd = (header[1] * 3u) + readStart
        println("First Byte: $start, Start Read: $readStart, End Read: $readEnd")
        programCounter = readStart
            while (programCounter <= readEnd - 3u) {
                val cpy = memory.getRange(programCounter, programCounter + 3u)
                val (opcode, arg1, arg2) = cpy
                if(opcode.toInt() == 0) {
                    programCounter += 3u
                    continue
                }
                when(opcode.toInt()) { //opcode
                    //arithmetic done
                    1 -> add() //pop top two numbers from the stack, add them, then put the result back onto the stack
                    2 -> sub() //pop top two numbers from the stack, subtract them, then put the result back onto the stack
                    3 -> multiply() //pop top two numbers from the stack, multiply them, then put the result back onto the stack
                    4 -> div() //pop top two numbers from the stack, divide them, then put the result back onto the stack
                    //load/stack operations
                    5 -> load(arg1.toStr(), arg2) //loads a register with a value
                    6 -> push(arg1) //put a number onto the stack
                    7 -> pop() //pops a number off the top of the stack and discards it
                    //register/stack transactions
                    8 -> from(arg1.toStr()) //push the value from a register onto the stack
                    9 -> to(arg1.toStr()) //pop the top value off the stack and put it into a register
                    10 -> mov(arg1.toStr(), arg2.toStr()) //moves a value between registers (arg1 -> arg2)
                    //Traversal done
                    11 -> jump(arg1 + start) //jumps to the specified line number
                    12 -> jumpStack(start) //jumps to an offset from the program start
                    13 -> jnz(arg1.toStr(), arg2 + start) //jumps to the specified line number if the specified register is not 0
                    14 -> jiz(arg1.toStr(), arg2 + start) //jumps to the specified line number if the specified register is 0
                    //Boolean logic done
                    15 -> and() //pop top two numbers from the stack, bitwise and them, then put the result back onto the stack
                    16 -> or() //pop top two numbers from the stack, bitwise or them, then put the result back onto the stack
                    17 -> xor() //pop top two numbers from the stack, bitwise xor them, then put the result back onto the stack
                    //Misc done
                    18 -> cmp() //compares the top two numbers from the stack.  if the first is >= the second, then a 1 is pushed, else a 0 is pushed
                    19 -> chr(arg1.toStr()) //prints the value from the specified register to stdout
                    20 -> chr() //prints the top value on the stack to stdout
                    //Disk R/W done
                    21 -> read(decBytesToShort(arg1, arg2)) //read a specific memory position and put it on the stack
                    22 -> read(arg1.toStr()) //read the memory address in the address register and add it to the specified register
                    23 -> write(decBytesToShort(arg1, arg2)) //write the top value on the stack to the specified memory address
                    24 -> write(arg1) //write the specified value to the memory address specified in the address register
                    25 -> inc()
                    26 -> dec()
                    27 -> lda(decBytesToShort(arg1, arg2))
                    28 -> lda()
                    else -> throw Exception()
                }
                if(!listOf(11, 12, 13, 14).contains(opcode.toInt())) programCounter += 3u
                while(hangCondition(programCounter)) { println("Hanging...") }
            }
        outputStream.println("\n")
        for ((id, reg) in registers) outputStream.println("Register $id contains ${reg.getValue()}")
        outputStream.println("Stack ptr is ${stack.getPointerLocation()}")
    }

    private fun add() = stack.push((stack.pop() + stack.pop()).toUByte()) //ADD
    private fun sub() = stack.push((stack.pop() - stack.pop()).toUByte()) //SUB
    private fun multiply() = stack.push((stack.pop() * stack.pop()).toUByte()) //MUL
    private fun div() = stack.push((stack.pop() / stack.pop()).toUByte()) //DIV
    private fun to(register: String) = (registers[register]!! as EightBitRegister).setByValue(stack.pop()) //RFS
    private fun from(register: String)  = stack.push((registers[register]!! as EightBitRegister).getValue()) //SFR
    private fun load(register: String, value: UByte) = (registers[register]!! as EightBitRegister).setByValue(value) //LDR
    private fun push(value: UByte)  = stack.push(value) //LDS
    private fun pop() = stack.pop() //POP
    private fun jump(lineNum: UInt) { programCounter = lineNum } //JMP
    private fun jumpStack(start: UInt) { programCounter = stack.pop() + start } //JMP
    private fun jnz(register: String, lineNum: UInt) = if((registers[register]!! as EightBitRegister).getValue().toInt() != 0) programCounter = lineNum else programCounter += 3u //JNZ
    private fun cmp() = stack.push(if(stack.pop().toInt() >= stack.pop().toInt()) 1u else 0u)
    private fun mov(regFrom: String, regTo: String) = (registers[regTo]!! as EightBitRegister).setByRegister(registers[regFrom]!! as EightBitRegister) //MOV
    private fun chr(reg: String) = outputStream.print((registers[reg]!! as EightBitRegister).getValue().toInt().toChar().toString()) //CHR
    private fun chr() = outputStream.print(Char(stack.pop().toInt()).toString()) //CHR
    private fun and() = stack.push((stack.pop().toInt() and stack.pop().toInt()).toUByte()) //AND
    private fun or() = stack.push((stack.pop().toInt() or stack.pop().toInt()).toUByte()) //POR
    private fun xor() = stack.push((stack.pop().toInt() xor stack.pop().toInt()).toUByte()) //XOR
    private fun jiz(register: String, lineNum: UInt) = if ((registers[register]!! as EightBitRegister).getValue().toInt() == 0) programCounter = lineNum else programCounter += 3u //JIZ
    private fun read(addr: UShort) = stack.push(memory.memory[addr.toInt()]) //RED
    private fun read(register: String) = (registers[register]!! as EightBitRegister).setByValue(memory[(registers["R"]!! as SixteenBitRegister).getValue().toInt()]) //RED
    private fun write(addr: UShort) { memory[addr.toInt()] = stack.pop() } //WRT
    private fun write(value: UByte) { memory[(registers["R"]!! as SixteenBitRegister).getValue().toInt()] = value } //WRT
    private fun inc() {
        val register = (registers["R"]!! as SixteenBitRegister)
        register.setByValue((register.getValue() + 1u).toUShort())
    }
    private fun dec() {
        val register = (registers["R"]!! as SixteenBitRegister)
        register.setByValue((register.getValue() - 1u).toUShort())
    }
    private fun lda() = (registers["R"]!! as SixteenBitRegister).setByValue(decBytesToShort(stack.pop(), stack.pop()))
    private fun lda(value: UShort) = (registers["R"]!! as SixteenBitRegister).setByValue(value)
    abstract class Register<in T : Register<T, V>, V> {
        protected abstract var pValue: V
        open fun setByValue(value: V) { pValue = value }
        open fun setByRegister(other: T) { pValue = other.pValue }
        open fun getValue(): V = pValue
    }
    class EightBitRegister : Register<EightBitRegister, UByte>() {
        override var pValue: UByte = 0u
    }
    class SixteenBitRegister : Register<SixteenBitRegister, UShort>() {
        override var pValue: UShort = 0u
    }
    class Stack(ram: Ram, private val size: UInt, private val startingLocation: UInt) {
        private var pointer = startingLocation - 1u
        private val memory = ram.memory
        fun push(value: UByte) { checkPointer(); memory[(++pointer).toInt()] = value }
        fun pop(): UByte { checkPointer(); return memory[pointer--.toInt()] }
        fun getPointerLocation() = pointer
        private fun checkPointer() = if(pointer >= startingLocation + size) throw OutOfMemoryError() else Unit
    }
    private fun UByte.toStr() = toInt().toChar().toString()
    private fun decBytesToShort(top8: UByte, bottom8: UByte): UShort {
        return ((top8.toUInt() * 256u) + bottom8).toUShort()
    }
}

interface WriteTarget {
    fun print(str: String)
    fun println(str: String) = print("$str\n")
    companion object
}