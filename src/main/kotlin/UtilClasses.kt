/**
 * The exception that should be thrown in case of a compile-time error.
 * @author RandomLonelyDev
 * @since 1.0.0
 * @param msg An optional message to be shown when this exception is thrown
 * */
internal class GCompileException(msg: String = "") : Exception(msg)

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