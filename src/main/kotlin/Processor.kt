class Processor(
    private val memory: Ram,
    stackRange: IntRange = 1 .. 1025, //so stack ptr is not negative
    private val programMemory: IntRange = 16384 ..< 32768
) {
    private val stack = Stack(memory, stackRange.last - stackRange.first, stackRange.first)
    private var programCounter = 1
    private val registers = mapOf(
        "A" to EightBitRegister(), //A-F general purpose 8bit
        "B" to EightBitRegister(),
        "C" to EightBitRegister(),
        "D" to EightBitRegister(),
        "E" to EightBitRegister(),
        "F" to EightBitRegister(),
        "ADT" to EightBitRegister(),
        "ADB" to EightBitRegister()
    )
    fun execute(start: Int) {
        if(!programMemory.contains(start)) throw Exception("SEGFAULT")
        val mem = memory.memory
        val readStart = start + 3
        val header = mem.copyOfRange(start, readStart)
        if(header[0].toInt() != 127) throw Exception("INVALID HEADER")
        val readEnd = (header[1] * 3u) + readStart.toUInt()
        println("First Byte: $start, Start Read: $readStart, End Read: $readEnd")
        programCounter = readStart
            while (programCounter <= (readEnd - 3u).toInt()) {
                val cpy = mem.copyOfRange(programCounter, programCounter + 3)
                //println("pc $programCounter")
                val (opcode, arg1, arg2) = cpy
                if(opcode.toInt() == 0) {
                    programCounter += 3
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
                    11 -> jump(arg1.toInt() + start) //jumps to the specified line number
                    12 -> jumpStack(start) //jumps to an offset from the program start
                    13 -> jnz(arg1.toStr(), arg2.toInt() + start) //jumps to the specified line number if the specified register is not 0
                    14 -> jiz(arg1.toStr(), arg2.toInt() + start) //jumps to the specified line number if the specified register is 0
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
                    else -> throw Exception()
                }
                if(!listOf(11, 12, 13).contains(opcode.toInt())) programCounter += 3
            }
        println()
        for ((id, reg) in registers) println("Register $id contains ${reg.getValue()}")
        for ((memLoc, value) in memory.memory.copyOfRange(stack.startingLocation, stack.startingLocation + stack.size).withIndex()) {
            println("Stack has byte $value at location $memLoc")
        }
        println("Stack ptr is ${stack.getPointerLocation()}")
    }

    private fun add() = stack.push((stack.pop() + stack.pop()).toUByte()) //ADD
    private fun sub() = stack.push((stack.pop() - stack.pop()).toUByte()) //SUB
    private fun multiply() = stack.push((stack.pop() * stack.pop()).toUByte()) //MUL
    private fun div() = stack.push((stack.pop() / stack.pop()).toUByte()) //DIV
    private fun to(register: String) = registers[register]!!.setByValue(stack.pop()) //RFS
    private fun from(register: String)  = stack.push(registers[register]!!.getValue()) //SFR
    private fun load(register: String, value: UByte) = registers[register]!!.setByValue(value) //LDR
    private fun push(value: UByte)  = stack.push(value) //LDS
    private fun pop() = stack.pop() //POP
    private fun jump(lineNum: Int) { programCounter = lineNum } //JMP
    private fun jumpStack(start: Int) { programCounter = stack.pop().toInt() + start } //JMP
    private fun jnz(register: String, lineNum: Int) = if(registers[register]!!.getValue().toInt() != 0) programCounter = lineNum else programCounter += 3 //JNZ
    private fun cmp() = stack.push(if(stack.pop().toInt() >= stack.pop().toInt()) 1u else 0u)
    private fun mov(regFrom: String, regTo: String) = registers[regTo]!!.setByRegister(registers[regFrom]!!) //MOV
    private fun chr(reg: String) = print(registers[reg]!!.getValue().toInt().toChar()) //CHR
    private fun chr() = print(Char(stack.pop().toInt())) //CHR
    private fun and() = stack.push((stack.pop().toInt() and stack.pop().toInt()).toUByte()) //AND
    private fun or() = stack.push((stack.pop().toInt() or stack.pop().toInt()).toUByte()) //POR
    private fun xor() = stack.push((stack.pop().toInt() xor stack.pop().toInt()).toUByte()) //XOR
    private fun jiz(register: String, lineNum: Int) = if (registers[register]!!.getValue().toInt() == 0) programCounter = lineNum else programCounter += 3 //JIZ
    private fun read(addr: UShort) = stack.push(memory.memory[addr.toInt()]) //RED
    private fun read(register: String) = registers[register]!!.setByValue(memory.memory[decBytesToShort(registers["ADT"]!!.getValue(), registers["ADB"]!!.getValue()).toInt()]) //RED
    private fun write(addr: UShort) { memory.memory[addr.toInt()] = stack.pop() } //WRT
    private fun write(value: UByte) { memory.memory[decBytesToShort(registers["ADT"]!!.getValue(), registers["ADB"]!!.getValue()).toInt()] = value } //WRT
    abstract class Register<T, V> {
        protected abstract var _value: V
        abstract fun setByValue(value: V)
        abstract fun setByRegister(other: T)
        abstract fun getValue(): V
    }
    class EightBitRegister : Register<EightBitRegister, UByte>() {
        override var _value: UByte = 0u
        override fun setByValue(value: UByte) { this._value = value }
        override fun setByRegister(other: EightBitRegister) { this._value = other._value }
        override fun getValue() = _value
    }
    class Stack(ram: Ram, val size: Int, val startingLocation: Int) {
        private var pointer = startingLocation - 1
        private val memory = ram.memory
        fun push(value: UByte) { checkPointer(); memory[++pointer] = value }
        fun pop(): UByte { checkPointer(); return memory[pointer--] }
        fun getPointerLocation() = pointer
        private fun checkPointer() = if(pointer >= startingLocation + size) throw OutOfMemoryError() else Unit
    }
    private fun UByte.toStr() = toInt().toChar().toString()
    private fun decBytesToShort(top8: UByte, bottom8: UByte): UShort {
        return ((top8.toUInt() * 256u) + bottom8).toUShort()
    }
}