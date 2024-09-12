import BitSpecificValue.Position

/**
 * The Kt8 virtual processor <3
 * @author RandomLonelyDev
 * @since 1.0.0
 * @param memory The virtual memory to use
 * @param stackRange The range in ram where the stack is housed
 * @param programMemory The range in memory where programs are loaded
 * @param outputStream Where console text and output is written (defaults to standard print and println functions)
 * @param onMemoryUpdate A function to be called whenever the memory is updated
 * */
class Processor(
    private val memory: Ram,
    stackRange: UIntRange = 0u .. 255u, //stack can only be one byte!!!
    private val programMemory: UIntRange = 16384u ..< 32768u,
    private val outputStream: WriteTarget,
    private val onMemoryUpdate: (Int) -> Unit = { _ -> }
) {
    private val stack = Stack(memory, stackRange.last - stackRange.first, stackRange.first)

    private val accumulator = BitSpecificRegister() // 1
    private val w = BitSpecificRegister() // 2
    private val x = BitSpecificRegister() // 3
    private val y = BitSpecificRegister() // 4
    private val z = BitSpecificRegister() // 5
    private val pc = SixteenBitRegister(programMemory.first.toUShort())
    private val sp = BitSpecificRegister().apply { setByValue(stackRange.last.toUByte()) }
    private val flags = BitSpecificRegister().apply {
        setBit(Position.`32`, true)
        setBit(Position.`4`, true)
    }

    private val regCodes = mapOf(
        1 to accumulator,
        2 to w,
        3 to x,
        4 to y,
        5 to z
    )

    /**
     * The size of each instruction in bytes
     * */
    private val instructionSize = 3u

    /**
     * Clears the stack and memory, resetting the processor
     * @author RandomLonelyDev
     * @since 1.0.0
     * */
    fun reset() {
        stack.reset()
        memory.clear()
        onMemoryUpdate(0)
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
        hangCondition: (UShort) -> Boolean = { _ -> false }
    ) {
        if(!programMemory.contains(start)) throw Exception("SEGFAULT")
        val readStart = (start + instructionSize).toUShort()
        val header = memory.getRange(start, readStart)
        if(header[0].toInt() != 127) throw Exception("INVALID HEADER ${header.toList()}")
        val readEnd = twoBytesToShort(header[1], header[2]) //the last byte containing program code
        println("First Byte: $start, Start Read: $readStart, End Read: $readEnd")
        pc.setByValue(readStart)
        while(pc.getValue() < readEnd - instructionSize) {
            var cond = hangCondition(pc.getValue())
            while(cond) { cond = hangCondition(pc.getValue()) }
            val (opcode, arg1, arg2) = memory.getRange(pc.getValue(), (pc.getValue() + instructionSize).toUShort())
            when(opcode.toInt()) {
                1 -> {
                    accumulator.setByValue(memory[twoBytesToShort(arg1, arg2)])
                    flags.setBit(Position.`128`, accumulator.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, accumulator.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                2 -> {
                    accumulator.setByValue(memory[arg1 + w.getAsByte()])
                    flags.setBit(Position.`128`, accumulator.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, accumulator.pValue == BitSpecificValue.ZERO)
                }
                3 -> {
                    accumulator.setByValue(memory[arg1 + x.getAsByte()])
                    flags.setBit(Position.`128`, accumulator.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, accumulator.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                4 -> {
                    accumulator.setByValue(arg1)
                    flags.setBit(Position.`128`, accumulator.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, accumulator.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                5 -> {
                    w.setByValue(memory[twoBytesToShort(arg1, arg2).toUInt()])
                    flags.setBit(Position.`128`, w.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, w.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                6 -> {
                    w.setByValue(memory[arg1 + x.getAsByte()])
                    flags.setBit(Position.`128`, w.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, w.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                7 -> {
                    w.setByValue(arg1)
                    flags.setBit(Position.`128`, w.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, w.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                8 -> {
                    x.setByValue(memory[twoBytesToShort(arg1, arg2).toUInt()])
                    flags.setBit(Position.`128`, x.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, x.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                9 -> {
                    x.setByValue(memory[arg1 + w.getAsByte()])
                    flags.setBit(Position.`128`, x.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, x.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                10 -> {
                    x.setByValue(arg1)
                    flags.setBit(Position.`128`, x.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, x.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                11 -> {
                    y.setByValue(memory[twoBytesToShort(arg1, arg2).toUInt()])
                    flags.setBit(Position.`128`, y.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, y.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                12 -> {
                    y.setByValue(memory[arg1 + x.getAsByte()])
                    flags.setBit(Position.`128`, y.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, y.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                13 -> {
                    y.setByValue(arg1)
                    flags.setBit(Position.`128`, y.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, y.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                14 -> {
                    z.setByValue(memory[twoBytesToShort(arg1, arg2).toUInt()])
                    flags.setBit(Position.`128`, z.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, z.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                15 -> {
                    z.setByValue(memory[arg1 + w.getAsByte()])
                    flags.setBit(Position.`128`, z.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, z.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                16 -> {
                    z.setByValue(arg1)
                    flags.setBit(Position.`128`, z.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, z.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                17 -> memory[twoBytesToShort(arg1, arg2).toUInt()] = accumulator.getAsByte()
                18 -> memory[arg1 + w.getAsByte()] = accumulator.getAsByte()
                19 -> memory[arg1 + x.getAsByte()] = accumulator.getAsByte()
                20 -> memory[twoBytesToShort(arg1, arg2).toUInt()] = w.getAsByte()
                21 -> memory[twoBytesToShort(arg1, arg2).toUInt()] = x.getAsByte()
                22 -> memory[twoBytesToShort(arg1, arg2).toUInt()] = y.getAsByte()
                23 -> memory[twoBytesToShort(arg1, arg2).toUInt()] = z.getAsByte()
                24 -> pc.setByValue(twoBytesToShort(arg1, arg2))
                25 -> {
                    val addr = twoBytesToShort(arg1, arg2)
                    pc.setByValue(twoBytesToShort(memory[addr].toByte(), memory[addr + 1u].toByte()))
                }
                26 -> {
                    val jumpTo = (twoBytesToShort(arg1, arg2) - instructionSize).toUShort()
                    pc.setByValue(jumpTo)
                    val (top8, bottom8) = shortToTwoBytes(jumpTo)
                    stack.push(bottom8)
                    stack.push(top8)
                }
                27 -> pc.setByValue(twoBytesToShort(stack.pop().toByte(), stack.pop().toByte()))
                28 -> flags.setBit(Position.`1`, false)
                29 -> flags.setBit(Position.`1`, true)
                30 -> flags.setBit(Position.`64`, false)
                31 -> {} //NO-OP
                32 -> stack.push(flags.getAsByte())
                33 -> flags.setByValue(stack.pop())
                34 -> stack.push(accumulator.getAsByte())
                35 -> {
                    accumulator.setByValue(stack.pop())
                    flags.setBit(Position.`128`, accumulator.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, accumulator.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                36 -> cmp(accumulator, memory[twoBytesToShort(arg1, arg2)])
                37 -> cmp(accumulator, memory[twoBytesToShort(arg1, arg2) + w.getAsByte()])
                38 -> cmp(accumulator, memory[twoBytesToShort(arg1, arg2) + x.getAsByte()])
                39 -> cmp(accumulator, arg1)
                40 -> cmp(w, memory[twoBytesToShort(arg1, arg2)])
                41 -> cmp(w, arg1)
                42 -> cmp(x, memory[twoBytesToShort(arg1, arg2)])
                43 -> cmp(x, arg1)
                44 -> and(accumulator, memory[twoBytesToShort(arg1, arg2)])
                45 -> and(accumulator, memory[twoBytesToShort(arg1, arg2) + w.getAsByte()])
                46 -> and(accumulator, memory[twoBytesToShort(arg1, arg2) + x.getAsByte()])
                47 -> and(accumulator, arg1)
                48 -> or(accumulator, memory[twoBytesToShort(arg1, arg2)])
                49 -> or(accumulator, memory[twoBytesToShort(arg1, arg2) + w.getAsByte()])
                50 -> or(accumulator, memory[twoBytesToShort(arg1, arg2) + x.getAsByte()])
                51 -> or(accumulator, arg1)
                52 -> xor(accumulator, memory[twoBytesToShort(arg1, arg2)])
                53 -> xor(accumulator, memory[twoBytesToShort(arg1, arg2) + w.getAsByte()])
                54 -> xor(accumulator, memory[twoBytesToShort(arg1, arg2) + x.getAsByte()])
                55 -> xor(accumulator, arg1)
                56 -> {
                    flags.setBit(Position.`1`, memory[twoBytesToShort(arg1, arg2)].rotateByteLeft(flags.getBit(Position.`1`)))
                    flags.setBit(Position.`128`, memory[twoBytesToShort(arg1, arg2)].getBit(Position.`128`)) //set negative flag
                }
                57 -> {
                    flags.setBit(Position.`1`, accumulator.getValue().rotateByteLeft(flags.getBit(Position.`1`)))
                    flags.setBit(Position.`128`, accumulator.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, accumulator.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                58 -> {
                    flags.setBit(Position.`1`, memory[twoBytesToShort(arg1, arg2) + w.getAsByte()].rotateByteLeft(flags.getBit(Position.`1`)))
                    flags.setBit(Position.`128`, memory[twoBytesToShort(arg1, arg2) + w.getAsByte()].getBit(Position.`128`)) //set negative flag
                }
                59 -> {
                    memory[twoBytesToShort(arg1, arg2)].rotateByteRight(flags.getBit(Position.`1`))
                    flags.setBit(Position.`128`, memory[twoBytesToShort(arg1, arg2)].getBit(Position.`128`)) //set negative flag
                }
                60 -> {
                    accumulator.getValue().rotateByteRight(flags.getBit(Position.`1`))
                    flags.setBit(Position.`128`, accumulator.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, accumulator.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                61 -> {
                    memory[twoBytesToShort(arg1, arg2) + w.getAsByte()].rotateByteRight(flags.getBit(Position.`1`))
                    flags.setBit(Position.`128`, memory[twoBytesToShort(arg1, arg2) + w.getAsByte()].getBit(Position.`128`)) //set negative flag
                }
                62 -> {
                    val loc = memory[twoBytesToShort(arg1, arg2)]
                    flags.setBit(Position.`1`, loc.bitShiftRight())
                    flags.setBit(Position.`128`, accumulator.getBit(Position.`128`)) //set negative flag
                }
                63 -> {
                    flags.setBit(Position.`1`, accumulator.getValue().bitShiftRight())
                    flags.setBit(Position.`128`, accumulator.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, accumulator.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                64 -> {
                    val loc = memory[twoBytesToShort(arg1, arg2) + w.getAsByte()]
                    flags.setBit(Position.`1`, loc.bitShiftRight())
                    flags.setBit(Position.`128`, loc.getBit(Position.`128`)) //set negative flag
                }
                65 -> {
                    val loc = memory[twoBytesToShort(arg1, arg2)]
                    flags.setBit(Position.`1`, loc.bitShiftLeft())
                    flags.setBit(Position.`128`, loc.getBit(Position.`128`)) //set negative flag
                }
                66 -> {
                    flags.setBit(Position.`1`, accumulator.getValue().bitShiftLeft())
                    flags.setBit(Position.`128`, accumulator.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, accumulator.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                67 -> {
                    val loc = memory[twoBytesToShort(arg1, arg2) + w.getAsByte()]
                    flags.setBit(Position.`1`, loc.bitShiftLeft())
                    flags.setBit(Position.`128`, loc.getBit(Position.`128`)) //set negative flag
                }
                //Everything above is good
                68 -> {
                    add(accumulator, memory[twoBytesToShort(arg1, arg2)])
                    flags.setBit(Position.`128`, accumulator.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, accumulator.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                69 -> {
                    add(accumulator, memory[twoBytesToShort(arg1, arg2) + w.getAsByte()])
                    flags.setBit(Position.`128`, accumulator.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, accumulator.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                70 -> {
                    add(accumulator, memory[twoBytesToShort(arg1, arg2) + x.getAsByte()])
                    flags.setBit(Position.`128`, accumulator.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, accumulator.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                71 -> {
                    add(accumulator, BitSpecificValue.of(arg1))
                    flags.setBit(Position.`128`, accumulator.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, accumulator.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                72 -> {
                    sub(accumulator, memory[twoBytesToShort(arg1, arg2)])
                    flags.setBit(Position.`128`, accumulator.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, accumulator.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                73 -> {
                    sub(accumulator, memory[twoBytesToShort(arg1, arg2) + w.getAsByte()])
                    flags.setBit(Position.`128`, accumulator.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, accumulator.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                74 -> {
                    sub(accumulator, memory[twoBytesToShort(arg1, arg2) + x.getAsByte()])
                    flags.setBit(Position.`128`, accumulator.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, accumulator.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                75 -> {
                    sub(accumulator, BitSpecificValue.of(arg1))
                    flags.setBit(Position.`128`, accumulator.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, accumulator.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                76 -> {
                    val loc = twoBytesToShort(arg1, arg2)
                    val (result, _, _) = memory[loc].plus(BitSpecificValue.of(1u))
                    memory[loc] = result
                    flags.setBit(Position.`128`, result.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, result == BitSpecificValue.ZERO) //set zero flag
                }
                77 -> {
                    val loc = twoBytesToShort(arg1, arg2) + w.getAsByte()
                    val (result, _, _) = memory[loc].plus(BitSpecificValue.of(1u))
                    memory[loc] = result
                    flags.setBit(Position.`128`, result.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, result == BitSpecificValue.ZERO) //set zero flag
                }
                78 -> {
                    val (result, _, _) = w.pValue.plus(BitSpecificValue.of(1u))
                    w.setByValue(result)
                    flags.setBit(Position.`128`, result.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, result == BitSpecificValue.ZERO) //set zero flag
                }
                79 -> {
                    val (result, _, _) = x.pValue.plus(BitSpecificValue.of(1u))
                    x.setByValue(result)
                    flags.setBit(Position.`128`, result.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, result == BitSpecificValue.ZERO) //set zero flag
                }
                80 -> {
                    val loc = twoBytesToShort(arg1, arg2)
                    val (result, _, _) = memory[loc].minus(BitSpecificValue.of(1u))
                    memory[loc] = result
                    flags.setBit(Position.`128`, result.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, result == BitSpecificValue.ZERO) //set zero flag
                }
                81 -> {
                    val loc = twoBytesToShort(arg1, arg2) + w.getAsByte()
                    val (result, _, _) = memory[loc].minus(BitSpecificValue.of(1u))
                    memory[loc] = result
                    flags.setBit(Position.`128`, result.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, result == BitSpecificValue.ZERO) //set zero flag
                }
                82 -> {
                    val (result, _, _) = w.pValue.minus(BitSpecificValue.of(1u))
                    w.setByValue(result)
                    flags.setBit(Position.`128`, result.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, result == BitSpecificValue.ZERO) //set zero flag
                }
                83 -> {
                    val (result, _, _) = x.pValue.minus(BitSpecificValue.of(1u))
                    x.setByValue(result)
                    flags.setBit(Position.`128`, result.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, result == BitSpecificValue.ZERO) //set zero flag
                }
                //Everything below is good
                84 -> if(!flags.getBit(Position.`1`)) pc.setByValue((pc.getValue() + arg1 - 3u).toUShort())
                85 -> if(flags.getBit(Position.`1`)) pc.setByValue((pc.getValue() + arg1 - 3u).toUShort())
                86 -> if(!flags.getBit(Position.`2`)) pc.setByValue((pc.getValue() + arg1 - 3u).toUShort())
                87 -> if(flags.getBit(Position.`2`)) pc.setByValue((pc.getValue() + arg1 - 3u).toUShort())
                88 -> if(!flags.getBit(Position.`128`)) pc.setByValue((pc.getValue() + arg1 - 3u).toUShort())
                89 -> if(flags.getBit(Position.`128`)) pc.setByValue((pc.getValue() + arg1 - 3u).toUShort())
                90 -> {
                    val des = regCodes[arg1.toInt()]!!
                    des.setByRegister(regCodes[arg2.toInt()]!!)
                    flags.setBit(Position.`128`, des.getBit(Position.`128`)) //set negative flag
                    flags.setBit(Position.`2`, des.pValue == BitSpecificValue.ZERO) //set zero flag
                }
                else -> throw GRuntimeException("Unknown Instruction")
            }
            pc.setByValue((pc.getValue() + 3u).toUShort())
            onMemoryUpdate(pc.getValue().toInt())
        }
        outputStream.println("Done.")
        outputStream.println("A: ${accumulator.pValue} W: ${w.pValue} X: ${x.pValue} Y: ${y.pValue} Z: ${z.pValue} PC: ${pc.pValue} SP: ${sp.pValue} FLAGS: ${flags.pValue}")
    }

    internal fun cmp(register: BitSpecificRegister, valueToCompare: UByte) = when(register.getAsByte().compareTo(valueToCompare)) {
        1 -> {
            flags.setBit(Position.`128`, false)
            flags.setBit(Position.`2`, false)
            flags.setBit(Position.`1`, true)
        }
        0 -> {
            flags.setBit(Position.`128`, false)
            flags.setBit(Position.`2`, true)
            flags.setBit(Position.`1`, true)
        }
        -1 -> {
            flags.setBit(Position.`128`, true)
            flags.setBit(Position.`2`, false)
            flags.setBit(Position.`1`, false)
        }
        else -> Unit
    }
    internal fun cmp(register: BitSpecificRegister, valueToCompare: BitSpecificValue) = cmp(register, valueToCompare.toByte())
    internal fun and(register: BitSpecificRegister, valueToAnd: UByte) {
        val result = register.getAsByte() and valueToAnd
        register.setByValue(result)
        flags.setBit(Position.`128`, register.getBit(Position.`128`)) //set negative flag
        flags.setBit(Position.`2`, register.pValue == BitSpecificValue.ZERO) //set zero flag
    }
    internal fun and(register: BitSpecificRegister, valueToAnd: BitSpecificValue) = and(register, valueToAnd.toByte())
    internal fun or(register: BitSpecificRegister, valueToOr: UByte) {
        val result = register.getAsByte() or valueToOr
        register.setByValue(result)
        flags.setBit(Position.`128`, register.getBit(Position.`128`)) //set negative flag
        flags.setBit(Position.`2`, register.pValue == BitSpecificValue.ZERO) //set zero flag
    }
    internal fun or(register: BitSpecificRegister, valueToOr: BitSpecificValue) = or(register, valueToOr.toByte())
    internal fun xor(register: BitSpecificRegister, valueToXor: UByte) {
        val result = register.getAsByte() xor valueToXor
        register.setByValue(result)
        flags.setBit(Position.`128`, register.getBit(Position.`128`)) //set negative flag
        flags.setBit(Position.`2`, register.pValue == BitSpecificValue.ZERO) //set zero flag
    }
    internal fun xor(register: BitSpecificRegister, valueToXor: BitSpecificValue) = or(register, valueToXor.toByte())
    internal fun add(register: BitSpecificRegister, value: BitSpecificValue) {
        val (result, carry, overflow) = register.pValue.plus(value, flags.getBit(Position.`1`))
        register.setByValue(result)
        flags.setBit(Position.`1`, carry)
        flags.setBit(Position.`64`, overflow)
    }
    internal fun sub(register: BitSpecificRegister, value: BitSpecificValue) {
        val (result, carry, overflow) = register.pValue.minus(value, flags.getBit(Position.`1`))
        register.setByValue(result)
        flags.setBit(Position.`1`, carry)
        flags.setBit(Position.`64`, overflow)
    }
}