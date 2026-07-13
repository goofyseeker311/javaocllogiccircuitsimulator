# MISC K64 Chip

MISC compute chip contains 64k cores, total of 32GB register nvsram and 8TB memory nvsram.
Each core contains 64k local 64-bit ram registers, and mapped rom, ram, touch-display ram.
Every instruction uses/operates on full 64-bit register values always, and runs in 1 cycle.
Every integer instruction uses two's complement signed long integer operations.
Instruction high bits can contain specific simple variations of instructions, and vector duplicates.
Each 64-bit instruction is formed from 16-bit [regX regY regZ insT] parameters.
insT parameter is formed from 8-4-4-bit [vecN insV insO] parameters.
Estimated logic transistors per core is 1M making 64k cores about 64 billion.
Estimated ram transistors per core is 8million 512KB and 256billion total 32GB.
Estimated compute 64-bit teraops at 5GHz x 8-vector per core is 40gops and 2560tops total.

Logisim evolution v4.1.0 used for circuit illustrations and functional models: https://github.com/logisim-evolution/logisim-evolution

GNU Octave 11.1.0 used for generic math and generating circuit constants: https://octave.org

HxD - Hex Editor and Disk Editor: https://mh-nexus.de/en/hxd/

<img width="3840" height="2112" alt="muxrisccore120" src="https://github.com/user-attachments/assets/6610129d-0d8c-4ca5-bcc9-9b5aa5ddeff3" />
<img width="3840" height="2112" alt="muxrisccore120a" src="https://github.com/user-attachments/assets/4feca124-6560-4023-a94d-d473ca89cd30" />
<img width="3840" height="2112" alt="muxrisccore120b" src="https://github.com/user-attachments/assets/13223f90-8d6d-4490-b380-65ee644b207f" />
<img width="3840" height="2112" alt="microfpgamux11" src="https://github.com/user-attachments/assets/94a30e13-19f2-4139-ace9-8e971c280713" />
<img width="3840" height="2112" alt="microfpgamux11a" src="https://github.com/user-attachments/assets/cd161100-2d86-44a3-a655-48f662db4a90" />
<img width="3840" height="2112" alt="microfpgamux11b" src="https://github.com/user-attachments/assets/196386ae-b823-4ae1-8455-d9856306a3cc" />

---


MISC core-gate instruction set architecture:
```

Opcode | Instruction                      | Description
----------------------------------------------------------------------------------------------------
any    | Raw Data                         | any raw data
         ##                                 direct data line 64-bit value
0      | Flow Control                     | generic flow control
         nop [] //                          insV=0 no operation sleep constant regYZ cycles, comment
         jmp                                insV=1 unconditional jump to regX
         jmpc, jmpc32, jmpc16, jmpc8        insV=2-5 jump to regX if regY 64/32/16/8-bit is not zero
         ldi, ldi32, ldi16, ldi8            insV=6-9 load regX 1x32/2x32/4x16/8x8-bit constant regYZ
         clk, rnd, core, time               insV=A-D integer counter, random, core info, global time
         memr, memw                         insV=E-F load/store regX from/to shared memory[regY]
1      | cmpXY       | Compare Values     | clear regX to 0, set to 1 if regY comp[insV] regZ
         cmpezXY                            insV=0 integer regY equal to zero
         cmplzXY                            insV=1 integer regY less than zero
         fcmpezXY                           insV=2 float regY equal to zero
         fcmplzXY                           insV=3 float regY less than zero
         finfXYZ                            insV=4 float is infinity
         cmpeXY                             insV=5 integer regY equal to regZ
         cmplXY                             insV=6 integer regY less than regZ
         fcmpeXY                            insV=7 float regY equal to regZ
         fcmplXY                            insV=8 float regY less than regZ
         fnanXYZ                            insV=9 float is not-a-number
         shlXYZ                             insV=A bitwise shift left regZ bits
         shrXYZ                             insV=B bitwise shift right regZ bits
         sharXYZ                            insV=C bitwise shift arithmetic right regZ bits
         rotlXYZ                            insV=D bitwise rotate left regZ bits
         rotrXYZ                            insV=E bitwise rotate right regZ bits
2      | bitXYZ      | ALU Bit Operation  | store bitwise op[insV] regY regZ to regX
         copyXYZ                            insV=0 bitwise copy
         notXYZ                             insV=1 bitwise not
         orXYZ                              insV=2 bitwise or
         andXYZ                             insV=3 bitwise and
         nandXYZ                            insV=4 bitwise nand
         norXYZ                             insV=5 bitwise nor
         xorXYZ                             insV=6 bitwise xor
         xnorXYZ                            insV=7 bitwise xnor
         ii32                               insV=A convert 64-bit integer to 2x 32-bit integer
         i32i16                             insV=B convert 2x 32-bit integer to 4x 16-bit integer
         i16i8XYZ                           insV=C convert 4x 16-bit integer to 8x 8-bit integer
         i32i                               insV=D convert 32-bit integer to 64-bit integer
         i16i32                             insV=E convert 2x 16-bit integer to 2x 32-bit integer
         i8i16XYZ                           insV=F convert 4x 8-bit integer to 4x 16-bit integer
3      | intXYZ      | ALU Int/BitA Ops   | store integer bitwise op[insV] regY regZ to regX
         addXYZ                             insV=0 integer add
         subXYZ                             insV=1 integer subtract
         mulXYZ                             insV=2 integer multiply
         divXYZ                             insV=3 integer divide
         negXYZ                             insV=4 integer negate
         addoXYZ                            insV=5 integer add overflow bit
         subbXYZ                            insV=6 integer subtract borrow bit
         muloXYZ                            insV=7 integer multiply overflow
         divrXYZ                            insV=8 integer divide remainder
         copycXYZ                           insV=9 bitwise conditional copy if regZ is not zero
         loneXYZ                            insV=A bitwise lowest one bit, -1 if not found
         honeXYZ                            insV=B bitwise highest one bit, -1 if not found
         lzeroXYZ                           insV=C bitwise lowest zero bit, -1 if not found
         hzeroXYZ                           insV=D bitwise highest zero bit, -1 if not found
         onesXYZ                            insV=E bitwise count of one bits
         fexpXYZ                            insV=F float exponential
4      | flpXYZ      | ALU Flp Operation  | store float op[insV] regY regZ to regX
         faddXYZ                            insV=0 float add
         fsubXYZ                            insV=1 float subtract
         fmulXYZ                            insV=2 float multiply
         fdivXYZ                            insV=3 float divide
         fnegXYZ                            insV=4 float negate
         fsinXYZ                            insV=5 float sine
         ftanXYZ                            insV=6 float tangent
         fcosXYZ                            insV=7 float cosine
         flogXYZ                            insV=8 float logarithm
         fpowXYZ                            insV=9 float power
         fasinXYZ                           insV=A float arcsine
         fatanXYZ                           insV=B float arctangent
         facosXYZ                           insV=C float arccosine
         fsqrtXYZ                           insV=D float square root
         fminXYZ                            insV=E float min
         fmaxXYZ                            insV=F float max
5      | convXYZ     | ALU Conv Vector    | store conversion op[insV] regY regZ to regX
         ff32                               insV=0 convert 64-bit float to 2x 32-bit float
         f32f16                             insV=1 convert 2x 32-bit float to 4x 16-bit float
         f16f8                              insV=2 convert 4x 16-bit float to 8x 8-bit float
         f8f16                              insV=3 convert 4x 8-bit float to 4x 16-bit float
         f16f32                             insV=4 convert 2x 16-bit float to 2x 32-bit float
         f32f                               insV=5 convert 32-bit float to 64-bit float
         fexpXYZ                            insV=6 float natural logarithm
         fexp32XYZ                          insV=7 2x 32-bit float natural logarithm
         fexp16XYZ                          insV=8 4x 16-bit float natural logarithm
         fexp8XYZ                           insV=9 8x 8-bit float natural logarithm
         fitfXYZ                            insV=A integer to float
         ftinXYZ                            insV=B float to integer nearest
         ftidXYZ                            insV=C float to integer round down
         ftiuXYZ                            insV=D float to integer round up
         ftitXYZ                            insV=E float to integer truncate
         fabsXYZ                            insV=F float abs
6      | cmpvecXY    | Comp Zero Vector   | vector clear regX to 0, set to 1 if regY comp[insV]
         cmpez32XY                          insV=0 2x 32-bit integer regY equal to zero
         cmplz32XY                          insV=1 2x 32-bit integer regY less than zero
         fcmpez32XY                         insV=2 2x 32-bit float regY equal to zero
         fcmplz32XY                         insV=3 2x 32-bit float regY less than zero
         finf32XYZ                          insV=4 2x 32-bit float is infinity
         cmpez16XY                          insV=5 4x 16-bit integer regY equal to zero
         cmplz16XY                          insV=6 4x 16-bit integer regY less than zero
         fcmpez16X                          insV=7 4x 16-bit float regY equal to zero
         fcmplz16X                          insV=8 4x 16-bit float regY less than zero
         finf16XYZ                          insV=9 4x 16-bit float is infinity
         cmpez8XY                           insV=A 8x 8-bit integer regY equal to zero
         cmplz8XY                           insV=B 8x 8-bit integer regY less than zero
         fcmpez8XY                          insV=C 8x 8-bit float regY equal to zero
         fcmplz8XY                          insV=D 8x 8-bit float regY less than zero
         finf8XYZ                           insV=E 8x 8-bit float is infinity
7      | cmpvecXY    | Comp Value Vector  | vector clear regX to 0, set to 1 if regY comp[insV]
         cmpez32XY                          insV=0 2x 32-bit integer regY equal to regZ
         cmplz32XY                          insV=1 2x 32-bit integer regY less than regZ
         fcmpez32XY                         insV=2 2x 32-bit float regY equal to regZ
         fcmplz32XY                         insV=3 2x 32-bit float regY less than regZ
         fnan32XYZ                          insV=4 2x 32-bit float is not-a-number
         cmpez16XY                          insV=5 4x 16-bit integer regY equal to regZ
         cmplz16XY                          insV=6 4x 16-bit integer regY less than regZ
         fcmpez16X                          insV=7 4x 16-bit float regY equal to regZ
         fcmplz16X                          insV=8 4x 16-bit float regY less than regZ
         fnan16XYZ                          insV=9 4x 16-bit float is not-a-number
         cmpez8XY                           insV=A 8x 8-bit integer regY equal to regZ
         cmplz8XY                           insV=B 8x 8-bit integer regY less than regZ
         fcmpez8XY                          insV=C 8x 8-bit float regY equal to regZ
         fcmplz8XY                          insV=D 8x 8-bit float regY less than regZ
         fnan8XYZ                           insV=E 8x 8-bit float is not-a-number
         fexp32XYZ                          insV=F 2x 32-bit float exponential
8      | bitvecXYZ   | ALU Bit Vector     | vector store bitwise op[insV] regY regZ to regX
         shl32XYZ                           insV=0 2x 32-bit shift left regZ bits
         shr32XYZ                           insV=1 2x 32-bit shift right regZ bits
         shar32XYZ                          insV=2 2x 32-bit shift arithmetic right regZ bits
         rotl32XYZ                          insV=3 2x 32-bit rotate left regZ bits
         rotr32XYZ                          insV=4 2x 32-bit rotate right regZ bits
         shl16XYZ                           insV=5 4x 16-bit shift left regZ bits
         shr16XYZ                           insV=6 4x 16-bit shift right regZ bits
         shar16XYZ                          insV=7 4x 16-bit shift arithmetic right regZ bits
         rotl16XYZ                          insV=8 4x 16-bit rotate left regZ bits
         rotr16XYZ                          insV=9 4x 16-bit rotate right regZ bits
         shl8XYZ                            insV=A 8x 8-bit shift left regZ bits
         shr8XYZ                            insV=B 8x 8-bit shift right regZ bits
         shar8XYZ                           insV=C 8x 8-bit shift arithmetic right regZ bits
         rotl8XYZ                           insV=D 8x 8-bit rotate left regZ bits
         rotr8XYZ                           insV=E 8x 8-bit rotate right regZ bits
         fexp16XYZ                          insV=F 4x 16-bit float exponential
9      | intvecXYZ   | ALU Int Vector     | vector store integer op[insV] regY regZ to regX
         add32XYZ                           insV=0 2x 32-bit integer add
         sub32XYZ                           insV=1 2x 32-bit integer subtract
         mul32XYZ                           insV=2 2x 32-bit integer multiply
         div32XYZ                           insV=3 2x 32-bit integer divide
         neg32XYZ                           insV=4 2x 32-bit integer negate
         add16XYZ                           insV=5 4x 16-bit integer add
         sub16XYZ                           insV=6 4x 16-bit integer subtract
         mul16XYZ                           insV=7 4x 16-bit integer multiply
         div16XYZ                           insV=8 4x 16-bit integer divide
         neg16XYZ                           insV=9 4x 16-bit integer negate
         add8XYZ                            insV=A 8x 8-bit integer add
         sub8XYZ                            insV=B 8x 8-bit integer subtract
         mul8XYZ                            insV=C 8x 8-bit integer multiply
         div8XYZ                            insV=D 8x 8-bit integer divide
         neg8XYZ                            insV=E 8x 8-bit integer negate
         fexp8XYZ                           insV=F 8x 8-bit float exponential
A      | intvecXYZ   | ALU Int Vector 2   | vector store integer op[insV] regY regZ to regX
         addo32XYZ                          insV=0 2x 32-bit integer add overflow bit
         subb32XYZ                          insV=1 2x 32-bit integer subtract borrow bit
         mulo32XYZ                          insV=2 2x 32-bit integer multiply overflow
         divr32XYZ                          insV=3 2x 32-bit integer divide remainder
         copyc32XYZ                         insV=4 2x 32-bit conditional copy if regZ not zero
         addo16XYZ                          insV=5 4x 16-bit integer add overflow bit
         subb16XYZ                          insV=6 4x 16-bit integer subtract borrow bit
         mulo16XYZ                          insV=7 4x 16-bit integer multiply overflow
         divr16XYZ                          insV=8 4x 16-bit integer divide remainder
         copyc16XYZ                         insV=9 4x 16-bit conditional copy if regZ not zero
         addo8XYZ                           insV=A 8x 8-bit integer add overflow bit
         subb8XYZ                           insV=B 8x 8-bit integer subtract borrow bit
         mulo8XYZ                           insV=C 8x 8-bit integer multiply overflow
         divr8XYZ                           insV=D 8x 8-bit integer divide remainder
         copyc8XYZ                          insV=E 8x 8-bit conditional copy if regZ not zero
         fmin32XYZ                          insV=F 2x 32-bit float min
B      | intvecXYZ   | ALU Bit Vector 2   | vector store bitwise op[insV] regY regZ to regX
         lone32XYZ                          insV=0 2x 32-bit lowest one bit, -1 if not found
         hone32XYZ                          insV=1 2x 32-bit highest one bit, -1 if not found
         lzero32XYZ                         insV=2 2x 32-bit lowest zero bit, -1 if not found
         hzero32XYZ                         insV=3 2x 32-bit highest zero bit, -1 if not found
         ones32XYZ                          insV=4 2x 32-bit count of one bits
         lone16XYZ                          insV=5 4x 16-bit lowest one bit, -1 if not found
         hone16XYZ                          insV=6 4x 16-bit highest one bit, -1 if not found
         lzero16XYZ                         insV=7 4x 16-bit lowest zero bit, -1 if not found
         hzero16XYZ                         insV=8 4x 16-bit highest zero bit, -1 if not found
         ones16XYZ                          insV=9 4x 16-bit count of one bits
         lone8XYZ                           insV=A 8x 8-bit lowest one bit, -1 if not found
         hone8XYZ                           insV=B 8x 8-bit highest one bit, -1 if not found
         lzero8XYZ                          insV=C 8x 8-bit lowest zero bit, -1 if not found
         hzero8XYZ                          insV=D 8x 8-bit highest zero bit, -1 if not found
         ones8XYZ                           insV=E 8x 8-bit count of one bits
         fmax32XYZ                          insV=F 2x 32-bit float max
C      | flpvecXYZ   | ALU Flp Vector     | vector store float op[insV] regY regZ to regX
         fadd32XYZ                          insV=0 2x 32-bit float add
         fsub32XYZ                          insV=1 2x 32-bit float subtract
         fmul32XYZ                          insV=2 2x 32-bit float multiply
         fdiv32XYZ                          insV=3 2x 32-bit float divide
         fneg32XYZ                          insV=4 2x 32-bit float negate
         fadd16XYZ                          insV=5 4x 16-bit float add
         fsub16XYZ                          insV=6 4x 16-bit float subtract
         fmul16XYZ                          insV=7 4x 16-bit float multiply
         fdiv16XYZ                          insV=8 4x 16-bit float divide
         fneg16XYZ                          insV=9 4x 16-bit float negate
         fadd8XYZ                           insV=A 8x 8-bit float add
         fsub8XYZ                           insV=B 8x 8-bit float subtract
         fmul8XYZ                           insV=C 8x 8-bit float multiply
         fdiv8XYZ                           insV=D 8x 8-bit float divide
         fneg8XYZ                           insV=E 8x 8-bit float negate
         fmin16XYZ                          insV=F 4x 16-bit float min
D      | flpavec1XYZ | ALU FlpA Vector    | store advanced float op[insV] regY regZ to regX
         fsin32XYZ                          insV=0 2x 32-bit float sine
         ftan32XYZ                          insV=1 2x 32-bit float tangent
         fcos32XYZ                          insV=2 2x 32-bit float cosine
         flog32XYZ                          insV=3 2x 32-bit float logarithm
         fpow32XYZ                          insV=4 2x 32-bit float power
         fsin16XYZ                          insV=5 4x 16-bit float sine
         ftan16XYZ                          insV=6 4x 16-bit float tangent
         fcos16XYZ                          insV=7 4x 16-bit float cosine
         flog16XYZ                          insV=8 4x 16-bit float logarithm
         fpow16XYZ                          insV=9 4x 16-bit float power
         fsin8XYZ                           insV=A 8x 8-bit float sine
         ftan8XYZ                           insV=B 8x 8-bit float tangent
         fcos8XYZ                           insV=C 8x 8-bit float cosine
         flog8XYZ                           insV=D 8x 8-bit float logarithm
         fpow8XYZ                           insV=E 8x 8-bit float power
         fmax16XYZ                          insV=F 4x 16-bit float max
E      | flpavec2XYZ | ALU FlpA Vector 2  | store advanced float op[insV] regY regZ to regX
         fasin32XYZ                         insV=0 2x 32-bit float arcsine
         fatan32XYZ                         insV=1 2x 32-bit float arctangent
         facos32XYZ                         insV=2 2x 32-bit float arccosine
         fsqrt32XYZ                         insV=3 2x 32-bit float square root
         fabs32XYZ                          insV=4 2x 32-bit float abs
         fasin16XYZ                         insV=5 4x 16-bit float arcsine
         fatan16XYZ                         insV=6 4x 16-bit float arctangent
         facos16XYZ                         insV=7 4x 16-bit float arccosine
         fsqrt16XYZ                         insV=8 4x 16-bit float square root
         fabs16XYZ                          insV=9 4x 16-bit float abs
         fasin8XYZ                          insV=A 8x 8-bit float arcsine
         fatan8XYZ                          insV=B 8x 8-bit float arctangent
         facos8XYZ                          insV=C 8x 8-bit float arccosine
         fsqrt8XYZ                          insV=D 8x 8-bit float square root
         fabs8XYZ                           insV=E 8x 8-bit float abs
         fmin8XYZ                           insV=F 8x 8-bit float min
F      | flpvec2XYZ  | ALU Flp Vector 2   | store float op[insV] regY regZ to regX
         fitf32XYZ                          insV=0 2x 32-bit integer to float
         ftin32XYZ                          insV=1 2x 32-bit float to integer nearest
         ftid32XYZ                          insV=2 2x 32-bit float to integer round down
         ftiu32XYZ                          insV=3 2x 32-bit float to integer round up
         ftit32XYZ                          insV=4 2x 32-bit float to integer truncate
         fitf16XYZ                          insV=5 4x 16-bit integer to float
         ftin16XYZ                          insV=6 4x 16-bit float to integer nearest
         ftid16XYZ                          insV=7 4x 16-bit float to integer round down
         ftiu16XYZ                          insV=8 4x 16-bit float to integer round up
         ftit16XYZ                          insV=9 4x 16-bit float to integer truncate
         fitf8XYZ                           insV=A 8x 8-bit integer to float
         ftin8XYZ                           insV=B 8x 8-bit float to integer nearest
         ftid8XYZ                           insV=C 8x 8-bit float to integer round down
         ftiu8XYZ                           insV=D 8x 8-bit float to integer round up
         ftit8XYZ                           insV=E 8x 8-bit float to integer truncate
         fmax8XYZ                           insV=F 8x 8-bit float max
```

Example looping test assembly code source and binary:
```
source listing         | binary           | explanation
----------------------------------------------------------------------------------------------------
[]                     | 0000000000000000 | empty line
// empty line          | 0000000000000000 | comment line
nop  00000200          | 0000000002000000 | no operation sleep 512+1 cycles
ldi  0000 00000001 ff  | 000000000001ff02 | load registers 0-7 with 0x1, current fibonacci num
ldi  0008 00000001 ff  | 000800000001ff02 | load registers 8-15 with 0x1, previous fibonacci num
ldi  0010 00000000 ff  | 001000000000ff02 | load registers 16-23 with 0x0, previous+ fibonacci num
ldi  0018 00000000     | 0018000000000002 | load register 24 with value 0x0, for loop index from 0
ldi  0019 00000020     | 0019000000200002 | load register 25 with value 0x20, for loop less than 32
ldi  001a 00000018     | 001a000000180002 | load register 26 with value 0x18, ram store start index
ldi  001b 00000001     | 001b000000010002 | load register 27 with value 0x1, constant 0x1 add
ldi  001c 00000008     | 001c000000080002 | load register 28 with value 0x8, constant 0x8 add
ldi  001d 0000000C     | 001d0000000C0002 | load register 29 with value 0xC constant jump address
ldi  0020 00000000     | 0020000000000002 | load register 32 with value 0x0 constant jump address
copy 0010 0008 0000 ff | 001000080000ff56 | copy registers 8-15 to register 16-23
copy 0008 0000 0000 ff | 000800000000ff56 | copy registers 0-7 to register 8-15
add  0000 0008 0010 ff | 000000080010ff05 | store add of registers 8-15 and 16-23 to register 0-7
memw 0000 001a 0000 ff | 0000001a0000ff13 | store registers 0-7 to register 26 memory location 0-7
add  001a 001a 001c    | 001a001a001c0005 | store add of register 26 and register 28 to register 26
add  0018 0018 001b    | 00180018001b0005 | store add of register 24 and register 27 to register 24
sub  001e 0018 0019    | 001e001800190025 | store sub of register 24 and register 25 to register 30
cmpl 001f 001e         | 001f001e00000014 | clear register 31, set if register 30 int less than 0
jmpc 001d 001f         | 001d001f00000011 | jump to register 29 if register 31 is not zero
jmp  0020              | 0020000000000001 | unconditional jump to register 32
##   A123456789ABCDEF  | a123456789abcdef | custom data segment with any instruction or data
```

Example looping test assembly to c-code approximate:
```
while(true) {                      // infinite while loop
  register<0> long fib1{8} = 0x1;  // init fib1 with registers array 0-7 to long 1 vectorized 8x
  register<8> long fib2{8} = 0x1;  // init fib2 with registers array 8-15 to long 1 vectorized 8x
  register<16> long fib3{8} = 0x0; // init fib3 with registers array 16-23 to long 0 vectorized 8x
  register<24> long i = 0;         // init loop i with register 24 long integer value 0
  register<25> long imax = 32;     // init loop imax with register 25 long integer value 32
  register<26> long *mem = 0x18;   // init mem with register 26 long integer pointer at 0x18
  for (;i<imax;i++) {              // for loop long integer i index value from 0 to 31
    fib3{8} = fib2{8};             // copy array of old fib2 values to fib3 vectorized 8x
    fib2{8} = fib1{8};             // copy array of old fib1 values to fib2 vectorized 8x
    fib1{8} = fib2{8} + fib3{8};   // calculate array of new fib1 adding fib2 and fib3 vectorized 8x
    mem{8} = fib1{8};              // store array of fib1 values to mem location index vectorized 8x
    mem += 8;                      // move memory pointer 8 indexes forward
  }                                // for loop close
}                                  // infinite while loop close
```
