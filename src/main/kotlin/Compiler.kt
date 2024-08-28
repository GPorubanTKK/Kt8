import java.io.BufferedReader
import java.io.File
import java.io.FileReader

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
        val opcodes = mapOf<String, List<UByte>>(
            //NO-OP
            "" to listOf(0u, 128u, 128u), //noop
        )
        mutLines.replaceAll { if(it.matches("(;.+)".toRegex())) "\n" else it } //remove full line comments
        mutLines.replaceAll { if(it.contains(';')) it.substring(0 ..< it.indexOf(';')) else it } //remove eol comments
        bytes += listOf(127u, mutLines.size.toUByte(), 0u)
        for(line in mutLines);
        return bytes.toTypedArray()
    }
}