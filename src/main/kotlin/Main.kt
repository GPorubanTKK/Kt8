fun main(args: Array<String>) {
    val compiler = Compiler()
    val memory = Ram(32768)
    val processor = Processor(memory) //create processor with 32kb ram
    memory.load(compiler.compileGasm(args[0]), 16384)
    processor.execute(16384)
}