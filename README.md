# MISC K64 Chip

MISC compute chip contains 64k cores, total of 32GB register nvsram and 8TB memory nvsram.
Each core contains 64k local 64-bit ram registers, and mapped rom, ram, touch-display ram.
Every instruction uses/operates on full 64-bit register values always, and runs in 1 cycle.
Every integer instruction uses two's complement signed long integer operations.
Instruction high bits can contain specific simple variations of instructions, and vector duplicates.
Each 64-bit instruction is formed from 16-bit [regX regY regZ insT] parameters.
insT parameter is formed from 8-4-4-bit [vecN insV insO] parameters.
Estimated logic transistors per core is 1million making 64k cores about 64 billion.
Estimated ram transistors per core is 8million 512KB and 512billion total 32GB.
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


MISC instruction set architecture:
```
Op  | Instruction (-, 32, 16, 8)          | Description (1x64b, 2x32b, 4x16b, 8x8b)
----------------------------------------------------------------------------------------------------
any | Raw Data                            | any raw data
      ##                                    direct data line 64-bit value
0   | Flow Control                        | generic flow control
      nop [] //                             insV=0 no operation sleep constant regYZ cycles, comment
      jmp                                   insV=1 unconditional jump to regX
      jmpc, jmpc32, jmpc16, jmpc8           insV=2-5 jump to regX if regY 64/32/16/8-bit is not zero
      ldi, ldi32, ldi16, ldi8               insV=6-9 load regX 1x32/2x32/4x16/8x8-bit constant regYZ
      clk, rnd, core, time                  insV=A-D integer counter, random, core info, global time
      memr, memw                            insV=E-F load/store regX from/to shared memory[regY]
1   | ALU Compare Zero                    | set 1 if comp regY to zero
      cmpez, cmpez32, cmpez16, cmpez8       insV=0-3 integer equal to zero
      cmplz, cmplz32, cmplz16, cmplz8       insV=4-7 integer less than zero
      fcmpez, fcmpez32, fcmpez16, fcmpez8   insV=8-B float equal to zero
      fcmplz, fcmplz32, fcmplz16, fcmplz8   insV=C-F float less than zero
2   | ALU Compare Value                   | set 1 if comp regY to regZ
      cmpe, cmpe32, cmpe16, cmpe8           insV=0-3 integer equal to regZ
      cmpl, cmpl32, cmpl16, cmpl8           insV=4-7 integer less than regZ
      fcmpe, fcmpe32, fcmpe16, fcmpe8       insV=8-B float equal to regZ
      fcmpl, fcmpl32, fcmpl16, fcmpl8       insV=C-F float less than regZ
3   | ALU Compare Special                 | set 1 if comp regY regZ
      neg, neg32, neg16, neg8               insV=0-3 integer negate
      copyc, copyc32, copyc16, copyc8       insV=4-7 conditional copy
      finf, finf32, finf16, finf8           insV=8-B float is infinity
      fnan, fnan32, fnan16, fnan8           insV=C-F float is not-a-number
3   | ALU Bitwise                         | bitwise regY regZ to regX
      copy, not, or, and                    insV=0-3 bitwise copy/not/or/and
      nand, nor, xor, xnor                  insV=4-7 bitwise nand/nor/xor/xnor
      shl, shl32, shl16, shl8               insV=8-B bitwise shift left
      shr, shr32, shr16, shr8               insV=C-F bitwise shift right
4   | ALU Bitwise                         | bitwise regY regZ to regX
      shar, shar32, shar16, shar8           insV=0-3 bitwise arithmetic shift right
      rotl, rotl32, rotl16, rotl8           insV=4-7 bitwise rotate left
      rotr, rotr32, rotr16, rotr8           insV=8-B bitwise rotate right
      ones, ones32, ones16, ones8           insV=C-F bitwise count of one bits
5   | ALU Bitwise                         | bitwise regY to regX
      lone, lone32, lone16, lone8           insV=0-3 bitwise lowest one bit or -1 
      hone, hone32, hone16, hone8           insV=4-7 bitwise highest one bit or -1
      lzero, lzero32, lzero16, lzero8       insV=8-B bitwise lowest zero bit or -1
      hzero, hzero32, hzero16, hzero8       insV=C-F bitwise highest zero bit or -1
9   | ALU Integer                         | integer regY regZ to regX
      add, add32, add16, add8               insV=0-3 integer add
      sub, sub32, sub16, sub8               insV=4-7 integer subtract
      mul, mul32, mul16, mul8               insV=8-B integer multiply
      div, div32, div16, div8               insV=C-F integer divide
A   | ALU Integer                         | integer regY regZ to regX
      addo, addo32, addo16, addo8           insV=0-3 integer add overflow
      subb, subb32, subb16, subb8           insV=4-7 integer subtract borrow
      mulo, mulo32, mulo16, mulo8           insV=8-B integer multiply overflow
      divr, divr32, divr16, divr8           insV=C-F integer divide remainder
9   | ALU Float                           | float regY regZ to regX
      fadd, fadd32, fadd16, fadd8           insV=0-3 float add
      fsub, fsub32, fsub16, fsub8           insV=4-7 float subtract
      fmul, fmul32, fmul16, fmul8           insV=8-B float multiply
      fdiv, fdiv32, fdiv16, fdiv8           insV=C-F float divide
A   | ALU Float                           | float regY regZ to regX
      fneg, fneg32, fneg16, fneg8           insV=0-3 float negate
      flog, flog32, flog16, flog8           insV=4-7 float logarithm
      fpow, fpow32, fpow16, fpow8           insV=8-B float power
      fsqrt, fsqrt32, fsqrt16, fsqrt8       insV=C-F float square root
B   | ALU Float                           | float regY regZ to regX
      fsin, fsin32, fsin16, fsin8           insV=0-3 float sine
      ftan, ftan32, ftan16, ftan8           insV=4-7 float tangent
      fcos, fcos32, fcos16, fcos8           insV=8-B float cosine
      fmin, fmin32, fmin16, fmin8           insV=C-F float min
C   | ALU Float                           | float regY regZ to regX
      fasin, fasin32, fasin16, fasin8       insV=0-3 float arcsine
      fatan, fatan32, fatan16, fatan8       insV=4-7 float arctangent
      facos, facos32, facos16, facos8       insV=8-B float arccosine
      fmax, fmax32, fmax16, fmax8           insV=C-F float max
D   | ALU Float                           | float regY to regX
      fln, fln32, fln16, fln8               insV=0-3 float natural log
      fexp, fexp32, fexp16, fexp8           insV=4-7 float exponential
      fabs, fabs32, fabs16, fabs8           insV=8-B float abs
E   | ALU Conversion                      | conversion regY to regX
      ftin, ftin32, ftin16, ftin8           insV=0-3 float to int nearest
      ftid, ftid32, ftid16, ftid8           insV=4-7 float to int down
      ftiu, ftiu32, ftiu16, ftiu8           insV=8-B float to int up
      ftit, ftit32, ftit16, ftit8           insV=C-F float to int truncate
F   | ALU Conversion                      | conversion regY to regX
      ii32, i32i16, i16i8                   insV=0-2 integer 1x64b->2x32b, 2x32b->4x16b, 4x16b->8x8b
      i32i, i16i32, i8i16                   insV=3-5 integer 1x32b->1x64b, 2x16b->2x32b, 4x8b->4x16b
      ff32, f32f16, f16f8                   insV=6-8 float 1x64b->2x32b, 2x32b->4x16b, 4x16b->8x8b
      f32f, f16f32, f8f16                   insV=9-B float 1x32b->1x64b, 2x16b->2x32b, 4x8b->4x16b
      fitf, fitf32, fitf16, fitf8           insV=C-F integer to float
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
