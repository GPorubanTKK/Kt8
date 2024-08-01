import java.io.BufferedReader
import java.io.File
import java.io.FileReader
fun main(args: Array<String>) {
    val code = BufferedReader(FileReader(File(args[0]))).readLines()
    val processor = Processor()
    processor.execute(code)
}