# Kt8
<h2>
    A software-defined 8bit processor
</h2>

The Kt8 is a 8 bit virtual computer.  It contains:
- 6 general purpose 8 bit registers (A-F) for inbuilt memory
- 2 8 bit registers representing one 16 bit address register (ADT and ADB) 
- Stack based
- Support for up to 64kb or memory with a 16 bit address bus
- A custom instruction set and assembler



<h2>
gASM  instructions
</h2>

- Specify comments with a semicolon and space preceding them ("_;comment")
- Numbers can be given as decimal, hex (begin hex bytes with x (xFF)), or binary
- To run code, give the file path to your source as a program argument when running

- Arithmetic operations
  - 
  - ADD <> <> add the top two bytes on the stack and push the result to the stack
  - SUB <> <> subtract the top two bytes on the stack and push the result to the stack
  - MUL <> <> multiply the top two bytes on the stack and push the result to the stack
  - DIV <> <> divide the top two bytes on the stack and push the result to the stack
- Boolean operations
  - 
  - AND <> <> bitwise and the top two bytes on the stack and push the result to the stack
  - OOR <> <> bitwise or the top two bytes on the stack and push the result to the stack
  - XOR <> <> bitwise xor the top two bytes on the stack and push the result to the stack
- Traversal operations
  - 
  - JMP <byte\> <> jump to the line number given
  - JMP <> <> jump to the line number equal to the top of the stack
  - JNZ <register\> <byte\> jump to the line number in the second argument if the register provided in the first IS NOT equal to zero.
  - JIZ <register\> <byte\> jump to the line number provided in the second argument if the register provided in the first IS equal to zero
- Memory operations
  - 
  - WRT <byte\> <byte\> write the top value on the stack to the memory location given by the two arguments.  the first is the top 8 bits of the address and second is the bottom 8
  - WRT <byte\> <> write the given value to the location in the address registers
  - RED <byte\> <byte\> read the value at the address given by the two arguments to the top of the stack.  the first is the top 8 bits of the address and second is the bottom 8
  - RED <register\> <> read the value at the address given by the address registers into the given register
- Miscellaneous operations
  -
  - POP <> <> pop the top of the stack and discard it
  - CMP <> <> compare the top two values on the stack.  If the top is greater than or equal to the second element, push a 1 or else 0 to the stack
  - CHR <> <> print the top of the stack as an ASCII character
  - CHR <register\> <> print the value of the given register as an ASCII character
- Register and stack transaction operations
  - 
  - RFS <register\> <> put the top of the stack into the given register
  - SFR <register\> <> put the byte in the given register onto the top of the stack
  - MOV <register\> <register\> move the value from the first register into the second
  - LDR <register\> <byte\> put the byte into the given register
  - LDS <byte\> put the byte onto the top of the stack


<h2>
Examples
</h2>

A while loop(line numbers added):

1| LDR A 10 ;put 10 into the A register

2| LDS 1 ;load 1 onto the stack

3| SFR A ;load the value in A(10) onto the stack 

4| SUB ;subtract them(A-1) and put the result onto the stack

5| RFS A ;put the result of the subtraction back into A

6| JNZ A 2 ;if A > 0, loop back to line 2