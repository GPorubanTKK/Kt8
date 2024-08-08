import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class Compiler {
    fun compileGasm(filePath: File): Array<UByte> = compileGasm(BufferedReader(FileReader(filePath)).readText())
    fun compileGasm(gasm: String): Array<UByte> {
        val lines = gasm.lines()
        val mutLines = lines.toMutableList()
        val bytes = mutableListOf<UByte>()
        val opcodes = mapOf<String, UByte>(
            "" to 0u,
            "ADD" to 1u,
            "SUB" to 2u,
            "MUL" to 3u,
            "DIV" to 4u,
            "LDR" to 5u,
            "LDS" to 6u,
            "POP" to 7u,
            "SFR" to 8u,
            "RFS" to 9u,
            "MOV" to 10u,
            "JMP" to 11u,
            //12 JMP w no args
            "JNZ" to 13u,
            "JIZ" to 14u,
            "AND" to 15u,
            "OOR" to 16u,
            "XOR" to 17u,
            "CMP" to 18u,
            "CHR" to 19u,
            //20 CHR w no args
            "RED" to 21u,
            //22 RED w registers
            "WRT" to 23u,
            //24 WRT w registers
            "INC" to 25u,
            "DEC" to 26u,
            "LDA" to 28u
            //29 LDA with no args
        )
        mutLines.replaceAll { if(it.matches("(;.+)".toRegex())) "\n" else it } //remove full line comments
        mutLines.replaceAll { if(it.contains(';')) it.substring(0 ..< it.indexOf(';')) else it } //remove eol comments
        bytes += listOf(127u, mutLines.size.toUByte(), 0u)
        for(line in mutLines) {
            val data = line.trim().split(' ')
            bytes += when(data[0]) {
                "CHR" -> if(data.size == 1) 20u else 19u
                "RED" -> if(data.size == 2) 22u else 21u
                "WRT" -> if(data.size == 2) 24u else 23u
                "LDA" -> if(data.size == 1) 29u else 28u
                else -> opcodes[data[0]]!!
            }
            bytes += when(data[0]) {
                "JMP" -> listOf((data[1].toUByteOrTryChar() * 3u).toUByte(), 0u)
                "JNZ" -> listOf(data[1][0].code.toUByte(), (data[2].toUByteOrTryChar() * 3u).toUByte())
                "JIZ" -> listOf(data[1][0].code.toUByte(), (data[2].toUByteOrTryChar() * 3u).toUByte())
                else -> when(data.size) { //if it's a normal operator
                    1 -> listOf(0u, 0u) //write two null bytes
                    2 -> listOf(data[1].toUByteOrTryChar(), 0u)
                    3 -> listOf(data[1].toUByteOrTryChar(), data[2].toUByteOrTryChar())
                    else -> throw IllegalArgumentException("Invalid amount of arguments for $line")
                }
            }
        }
        println("ASM bytes $bytes")
        return bytes.toTypedArray()
    }
}

fun String.toUByteOrTryChar(): UByte {
    return try {
        if(length == 8)
            toUByte(2)
        else if(startsWith("x"))
            removePrefix("x").toUByte(16)
        else toUByte()
    } catch(_: NumberFormatException) {
        this[0].code.toUByte()
    }
}