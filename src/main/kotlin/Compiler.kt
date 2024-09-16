import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import AddressMode.*

/**
 * Compiles raw gASM source code to Kt8 microcode
 * @author RandomLonelyDev
 * @since 1.0.0
 * */
class Compiler {
    /**
     * Compiles a gASM source file from path
     * @author RandomLonelyDev
     * @since 1.0.0
     * @param filePath The file object describing the location of the gASM source file
     * */
    fun compileGasm(filePath: File): Array<UByte> = compileGasm(BufferedReader(FileReader(filePath)).readText())
    /**
     * Compiles gASM from the provided string
     * @author RandomLonelyDev
     * @since 1.0.0
     * @param gasm The string containing gASM
     * */
    fun compileGasm(gasm: String): Array<UByte> {
        val lines = gasm.lines()
        val mutLines = lines.toMutableList()
        val bytes = mutableListOf<UByte>()
        val opcodes = mapOf<String, Map<AddressMode, UByte>>(
            "LDA" to mapOf(
                Absolute to 1u,
                AbsoluteWIndex to 2u,
                AbsoluteXIndex to 3u,
                Immediate to 4u
            ),
            "LDW" to mapOf(
                Absolute to 5u,
                AbsoluteXIndex to 6u,
                Immediate to 7u
            ),
            "LDX" to mapOf(
                Absolute to 8u,
                AbsoluteWIndex to 9u,
                Immediate to 10u
            ),
            "LDY" to mapOf(
                Absolute to 11u,
                AbsoluteXIndex to 12u,
                Immediate to 13u
            ),
            "LDZ" to mapOf(
                Absolute to 14u,
                AbsoluteWIndex to 15u,
                Immediate to 16u
            ),
            "STA" to mapOf(
                Absolute to 17u,
                AbsoluteWIndex to 18u,
                AbsoluteXIndex to 19u,
            ),
            "STW" to mapOf(Absolute to 20u),
            "STX" to mapOf(Absolute to 21u),
            "STY" to mapOf(Absolute to 22u),
            "STZ" to mapOf(Absolute to 23u),
            "JMP" to mapOf(
                Absolute to 24u,
                AbsoluteIndirect to 25u
            ),
            "JFN" to mapOf(Absolute to 26u),
            "RET" to mapOf(Implicit to 27u),
            "CLC" to mapOf(Implicit to 28u),
            "SEC" to mapOf(Implicit to 29u),
            "CLO" to mapOf(Implicit to 30u),
            "NOP" to mapOf(Implicit to 31u),
            "PHS" to mapOf(Implicit to 32u),
            "POS" to mapOf(Implicit to 33u),
            "PHA" to mapOf(Implicit to 34u),
            "POA" to mapOf(Implicit to 35u),
            "CMP" to mapOf(
                Absolute to 36u,
                AbsoluteWIndex to 37u,
                AbsoluteXIndex to 38u,
                Immediate to 39u
            ),
            "CPW" to mapOf(
                Absolute to 40u,
                Immediate to 41u
            ),
            "CPX" to mapOf(
                Absolute to 42u,
                Immediate to 43u
            ),
            "AND" to mapOf(
                Absolute to 44u,
                AbsoluteWIndex to 45u,
                AbsoluteXIndex to 46u,
                Immediate to 47u
            ),
            "ORA" to mapOf(
                Absolute to 48u,
                AbsoluteWIndex to 49u,
                AbsoluteXIndex to 50u,
                Immediate to 51u
            ),
            "XOR" to mapOf(
                Absolute to 52u,
                AbsoluteWIndex to 53u,
                AbsoluteXIndex to 54u,
                Immediate to 55u
            ),
            "ROL" to mapOf(
                Absolute to 56u,
                Accumulator to 57u,
                AbsoluteWIndex to 58u
            ),
            "ROR" to mapOf(
                Absolute to 59u,
                Accumulator to 60u,
                AbsoluteWIndex to 61u
            ),
            "BSR" to mapOf(
                Absolute to 62u,
                Accumulator to 63u,
                AbsoluteWIndex to 64u
            ),
            "BSL" to mapOf(
                Absolute to 65u,
                Accumulator to 66u,
                AbsoluteWIndex to 67u
            ),
            "ADD" to mapOf(
                Absolute to 68u,
                AbsoluteWIndex to 69u,
                AbsoluteXIndex to 70u,
                Immediate to 71u
            ),
            "SUB" to mapOf(
                Absolute to 72u,
                AbsoluteWIndex to 73u,
                AbsoluteXIndex to 74u,
                Immediate to 75u
            ),
            "INC" to mapOf(
                Absolute to 76u,
                AbsoluteWIndex to 77u
            ),
            "INW" to mapOf(Implicit to 78u),
            "INX" to mapOf(Implicit to 79u),
            "DEC" to mapOf(
                Absolute to 80u,
                AbsoluteWIndex to 81u
            ),
            "DEW" to mapOf(Implicit to 82u),
            "DEX" to mapOf(Implicit to 83u),
            "BCC" to mapOf(Relative to 84u),
            "BCS" to mapOf(Relative to 85u),
            "BNE" to mapOf(Relative to 86u),
            "BEQ" to mapOf(Relative to 87u),
            "BRP" to mapOf(Relative to 88u),
            "BRN" to mapOf(Relative to 89u),
            "MOV" to mapOf(RegisterCoded to 90u),
        )
        val regCodes = mapOf<String, UByte>(
            "a" to 1u,
            "w" to 2u,
            "x" to 3u,
            "y" to 4u,
            "z" to 5u
        )
        mutLines.replaceAll { if(it.matches("(;.+)".toRegex())) "\n" else it } //remove full line comments
        mutLines.replaceAll { if(it.contains(';')) it.substring(0 ..< it.indexOf(';')) else it } //remove eol comments
        bytes += 127u; bytes += shortToTwoBytes((mutLines.size * 3).toUShort())
        for(line in mutLines) {
            val split = line.split("[ ,]".toRegex())
            val (statement, arg) = split
            val strippedArg = arg.replace("[()#]".toRegex(), "") //remove () and # from (xarg) and #%arg
            val addressMode = when(split.size) {
                1 -> if(opcodes[statement]!!.containsKey(Implicit)) Implicit else Accumulator
                2 -> when {
                    arg.matches("\\(.+\\)".toRegex()) -> AbsoluteIndirect
                    arg[0] == '#' -> Immediate
                    strippedArg.determineNumSize("b", "x") == 16 -> Absolute
                    strippedArg.determineNumSize("b", "x") == 8 -> Relative
                    else -> throw GCompileException("Invalid address mode declaration")
                }
                3 -> when {
                        listOf("a", "w", "x", "y", "z").contains(split[1]) -> RegisterCoded
                        split[2] == "x" -> AbsoluteXIndex
                        split[2] == "w" -> AbsoluteWIndex
                        else -> throw GCompileException("Unknown relative register")
                    }
                else -> throw GCompileException("Invalid number of args provided")
            }
            bytes += opcodes[statement]!![addressMode]!! //the correct opcode for the address mode
            bytes += if(addressMode != RegisterCoded) strippedArg.toUBytes("b", "x")
            else arrayOf(regCodes[split[1]]!!, regCodes[split[2]]!!)
        }
        return bytes.toTypedArray()
    }
}

enum class AddressMode {
    Implicit, //i
    Accumulator, //A
    Immediate, //#
    Absolute, //a
    Relative, //r
    AbsoluteIndirect, //(a)
    AbsoluteWIndex, //_,w
    AbsoluteXIndex, //_,x
    RegisterCoded //rc,rc
}