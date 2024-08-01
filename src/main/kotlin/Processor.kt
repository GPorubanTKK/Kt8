import java.util.*

class Processor {
    private val stack = Stack<Byte>()
    private var programCounter = 1
    private val registers = mapOf(
        "A" to Register(), //A-F general purpose 8bit
        "B" to Register(),
        "C" to Register(),
        "D" to Register(),
        "E" to Register(),
        "F" to Register(),
    )

    fun execute(instructions: List<String>) {
        while (programCounter <= instructions.size) {
            val instruction = instructions[programCounter - 1]
            if(instruction.isBlank() || instruction.matches("(;.+)".toRegex())) {
                programCounter++
                continue
            }
            val data = instruction.split(" |(;.+)".toRegex()) //separate opcode and arguments from eol comments
            when (data[0]) { //opcode
                //arithmetic
                "ADD" -> add() //pop top two numbers from the stack, add them, then put the result back onto the stack
                "SUB" -> sub() //pop top two numbers from the stack, subtract them, then put the result back onto the stack
                "MUL" -> mult() //pop top two numbers from the stack, multiply them, then put the result back onto the stack
                "DIV" -> div() //pop top two numbers from the stack, divide them, then put the result back onto the stack
                //load/stack operations
                "LDR" -> load(data[1], data[2].toByte()) //loads a register with a value
                "LDS" -> push(data[1].toByte()) //put a number onto the stack
                "POP" -> pop() //pops a number off the top of the stack and discards it
                //register/stack transactions
                "SFR" -> from(data[1]) //push the value from a register onto the stack
                "RFS" -> to(data[1]) //pop the top value off the stack and put it into a register
                "MOV" -> mov(data[1], data[2]) //moves a value between registers (arg1 -> arg2)
                //Traversal
                "JMP" -> jump(data[1].toInt()) //jumps to the specified line number
                "JNZ" -> jnz(data[1], data[2].toInt()) //jumps to the specified line number if the specified register is not 0
                "JIZ" -> jiz(data[1], data[2].toInt()) //jumps to the specified line number if the specified register is 0
                //Boolean logic
                "AND" -> and() //pop top two numbers from the stack, bitwise and them, then put the result back onto the stack
                "POR" -> or() //pop top two numbers from the stack, bitwise or them, then put the result back onto the stack
                "XOR" -> xor() //pop top two numbers from the stack, bitwise xor them, then put the result back onto the stack
                //Misc
                "CMP" -> cmp() //compares the top two numbers from the stack.  if the first is >= the second, then a 1 is pushed, else a 0 is pushed
                "CHR" -> if (data.size > 1 && data[1].isNotBlank()) chr(data[1]) else chr()
                else -> throw Exception()
            }
            if (!listOf("JMP", "JNZ", "JIZ").contains(data[0])) programCounter++
        }
        println()
        for ((id, reg) in registers) println("Register $id contains ${reg.getValue()}")
    }

    private fun add(): Byte = stack.push((stack.pop() + stack.pop()).toByte()) //ADD
    private fun sub(): Byte = stack.push((stack.pop() - stack.pop()).toByte()) //SUB
    private fun mult(): Byte = stack.push((stack.pop() * stack.pop()).toByte()) //MUL
    private fun div(): Byte = stack.push((stack.pop() / stack.pop()).toByte()) //DIV
    private fun to(register: String) = registers[register]!!.setByByte(stack.pop()) //RFS
    private fun from(register: String): Byte = stack.push(registers[register]!!.getValue()) //SFR
    private fun load(register: String, value: Byte) = registers[register]!!.setByByte(value) //LDR
    private fun push(value: Byte): Byte = stack.push(value) //LDS
    private fun pop(): Byte = stack.pop() //POP
    private fun jump(lineNum: Int) { programCounter = lineNum } //JMP
    private fun jnz(register: String, lineNum: Int) = if (registers[register]!!.getValue().toInt() != 0) programCounter = lineNum else programCounter += 1 //JNZ
    private fun cmp() = stack.push(if(stack.pop().toInt() >= stack.pop().toInt()) 1 else 0)
    private fun mov(regFrom: String, regTo: String) = registers[regTo]!!.setByRegister(registers[regFrom]!!) //MOV
    private fun chr(reg: String) = print(registers[reg]!!.getValue().toInt().toChar()) //CHR
    private fun chr() = print(Char(stack.pop().toInt())) //CHR
    private fun and() = stack.push((stack.pop().toInt() and stack.pop().toInt()).toByte()) //AND
    private fun or() = stack.push((stack.pop().toInt() or stack.pop().toInt()).toByte()) //POR
    private fun xor() = stack.push((stack.pop().toInt() xor stack.pop().toInt()).toByte()) //XOR
    private fun jiz(register: String, lineNum: Int) = if (registers[register]!!.getValue().toInt() == 0) programCounter = lineNum else programCounter += 1 //JIZ
    class Register {
        private var registerValue: Byte = 0
        fun setByByte(value: Byte) { registerValue = value }
        fun setByRegister(other: Register) { registerValue = other.getValue() }
        fun getValue() = registerValue
    }
}