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

<img width="3840" height="2112" alt="muxrisccore142" src="https://github.com/user-attachments/assets/e3d2524f-a8db-4caf-92a0-67510b5e66e8" />
<img width="3840" height="2112" alt="muxrisccore142a" src="https://github.com/user-attachments/assets/74f3165d-6915-49c6-a450-23f0a57b9c9e" />
<img width="3840" height="2112" alt="muxrisccore142b" src="https://github.com/user-attachments/assets/2e4cbc58-17d5-47b3-b319-ab84bf4b49dd" />
<img width="3840" height="2112" alt="muxrisccore142c" src="https://github.com/user-attachments/assets/6c921e7a-2329-4833-b77b-65bb39c90be1" />
<img width="3840" height="2112" alt="muxrisccore142d" src="https://github.com/user-attachments/assets/7a47e453-f7cb-494b-9562-645c8779f57d" />
<img width="3840" height="2112" alt="muxrisccore142e" src="https://github.com/user-attachments/assets/66f27115-4c93-4b85-a222-e7eeb166b2de" />
<img width="3840" height="2112" alt="muxrisccore142f" src="https://github.com/user-attachments/assets/ebf74342-e374-4dc9-a08c-066e87b86829" />
<img width="3840" height="2112" alt="microfpgamux11" src="https://github.com/user-attachments/assets/94a30e13-19f2-4139-ace9-8e971c280713" />
<img width="3840" height="2112" alt="microfpgamux11a" src="https://github.com/user-attachments/assets/cd161100-2d86-44a3-a655-48f662db4a90" />
<img width="3840" height="2112" alt="microfpgamux11b" src="https://github.com/user-attachments/assets/196386ae-b823-4ae1-8455-d9856306a3cc" />

---


MISC instruction set architecture:
```
Op  | Instruction (-, 32, 16, 8)          | Description (1x64b, 2x32b, 4x16b, 8x8b)
----------------------------------------------------------------------------------------------------
any | Raw Data                            | any raw data
      ## LABEL                              direct data line 64-bit value, or LABEL: address
0   | Flow Control                        | generic flow control
      LABEL: nop [] //                      insV=0 no operation sleep regXYZN cycles, LABEL, comment
      jmpi, jmp, jmpc                       insV=1-3 jump to constant/LABEL regXYZN, regX, if regY
      ldi, ld, ld32, ld16, ld8              insV=4-8 load regX constant/LABEL regYZ, lane 1 regY
      clk, rnd, core, freq, time            insV=9-D counter, random, core info, frequency, nanotime
      memr, memw                            insV=E-F load/store regX from/to shared memory[regY]
1   | ALU Compare Zero                    | set 1 if comp regY to zero
      cmpez, cmpez32, cmpez16, cmpez8       insV=0-3 integer regY equal to zero
      cmplz, cmplz32, cmplz16, cmplz8       insV=4-7 integer regY less than zero
      fcmpez, fcmpez32, fcmpez16, fcmpez8   insV=8-B float regY equal to zero
      fcmplz, fcmplz32, fcmplz16, fcmplz8   insV=C-F float regY less than zero
2   | ALU Compare Value                   | set 1 if comp regY to regZ
      cmpe, cmpe32, cmpe16, cmpe8           insV=0-3 integer regY equal to regZ
      cmpl, cmpl32, cmpl16, cmpl8           insV=4-7 integer regY less than regZ
      fcmpe, fcmpe32, fcmpe16, fcmpe8       insV=8-B float regY equal to regZ
      fcmpl, fcmpl32, fcmpl16, fcmpl8       insV=C-F float regY less than regZ
3   | ALU Compare Special                 | set 1 if comp regY regZ
      neg, neg32, neg16, neg8               insV=0-3 integer negate
      copyc, copyc32, copyc16, copyc8       insV=4-7 conditional copy if regZ
      finf, finf32, finf16, finf8           insV=8-B float is infinity
      fnan, fnan32, fnan16, fnan8           insV=C-F float is not-a-number
4   | ALU Bitwise                         | bitwise regY regZ to regX
      copy, not, or, and                    insV=0-3 bitwise copy/not/or/and
      nand, nor, xor, xnor                  insV=4-7 bitwise nand/nor/xor/xnor
      shl, shl32, shl16, shl8               insV=8-B bitwise shift left
      shr, shr32, shr16, shr8               insV=C-F bitwise shift right
5   | ALU Bitwise                         | bitwise regY regZ to regX
      shar, shar32, shar16, shar8           insV=0-3 bitwise arithmetic shift right
      rotl, rotl32, rotl16, rotl8           insV=4-7 bitwise rotate left
      rotr, rotr32, rotr16, rotr8           insV=8-B bitwise rotate right
      ones, ones32, ones16, ones8           insV=C-F bitwise count of one bits
6   | ALU Bitwise                         | bitwise regY to regX
      lone, lone32, lone16, lone8           insV=0-3 bitwise lowest one bit or -1 
      hone, hone32, hone16, hone8           insV=4-7 bitwise highest one bit or -1
      lzero, lzero32, lzero16, lzero8       insV=8-B bitwise lowest zero bit or -1
      hzero, hzero32, hzero16, hzero8       insV=C-F bitwise highest zero bit or -1
7   | ALU Integer                         | integer regY regZ to regX
      add, add32, add16, add8               insV=0-3 integer add
      sub, sub32, sub16, sub8               insV=4-7 integer subtract
      mul, mul32, mul16, mul8               insV=8-B integer multiply
      div, div32, div16, div8               insV=C-F integer divide
8   | ALU Integer                         | integer regY regZ to regX
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
      fgam, fgam32, fgam16, fgam8           insV=C-F float gamma(x+1) = n!
E   | ALU Conversion                      | conversion regY to regX
      ftin, ftin32, ftin16, ftin8           insV=0-3 float to integer nearest
      ftid, ftid32, ftid16, ftid8           insV=4-7 float to integer down
      ftiu, ftiu32, ftiu16, ftiu8           insV=8-B float to integer up
      ftit, ftit32, ftit16, ftit8           insV=C-F float to integer truncate
F   | ALU Conversion                      | conversion regY to regX
      fitf, fitf32, fitf16, fitf8           insV=0-3 integer to float
      ii32, i32i16, i16i8                   insV=4-6 integer 1x64b->2x32b, 2x32b->4x16b, 4x16b->8x8b
      i32i, i16i32, i8i16                   insV=7-9 integer 1x32b->1x64b, 2x16b->2x32b, 4x8b->4x16b
      ff32, f32f16, f16f8                   insV=A-C float 1x64b->2x32b, 2x32b->4x16b, 4x16b->8x8b
      f32f, f16f32, f8f16                   insV=D-F float 1x32b->1x64b, 2x16b->2x32b, 4x8b->4x16b
```

Example looping test assembly code source and binary:
```
source listing                  | binary           | explanation
----------------------------------------------------------------------------------------------------
       ##    0000000000000000   | 0000000000000000 | data lines 0-f
       []                       | 0000000000000000 | empty line
       // empty line            | 0000000000000000 | comment line
START: nop   00000000000200     | 0000000000020000 | sleep for 513 cycles, label START
       ldi   0000 00000001 ff   | 000000000001FF40 | load registers 0-7 with value 0x1
       ldi   0008 00000001 ff   | 000800000001FF40 | load registers 8-f with value 0x1
       ldi   0010 00000000 ff   | 001000000000FF40 | load registers 10-17 with value 0x0
       ldi   0018 00000000      | 0018000000000040 | load register 18 with value 0x0
       ldi   0019 00000020      | 0019000000200040 | load register 19 with value 0x20
       ldi   001a 00000028      | 001A000000280040 | load register 1a with value 0x28
       ldi   001b 00000001      | 001B000000010040 | load register 1b with value 0x1
       ldi   001c 00000008      | 001C000000080040 | load register 1c with value 0x8
COPY:  copy  0010 0008 0000 ff  | 001000080000FF04 | copy registers 8-f to 10-17, label COPY
       copy  0008 0000 0000 ff  | 000800000000FF04 | copy registers 0-7 to 8-f
       add   0000 0008 0010 ff  | 000000080010FF07 | add registers 8-f and 10-17 to 0-7
       memw  0000 001a 0000 ff  | 0000001A0000FFF0 | write registers 0-7 to mem[28-2f]
       add   001a 001a 001c     | 001A001A001C0007 | add register 1a and 1c to 1a
       add   0018 0018 001b     | 00180018001B0007 | add register 18 and 1b to 18
       sub   001e 0018 0019     | 001E001800190047 | subtract register 18 and 19 to 1e
       cmplz 001f 001e          | 001F001E00000041 | compare register 1e less 0 to 1f
       ldi   001d COPY          | 001D0000001B0040 | load register 1d label COPY line number
       jmpc  001d 001f          | 001D001F00000030 | jump to register 1d if 1f not zero
       jmpi  START              | 0000000000001210 | jump to label START line number
       ##    A123456789ABCDEF   | A123456789ABCDEF | data line A123456789ABCDEF
```

Example looping test assembly to c-code approximate:
```
while(true) {                      // infinite while loop
  sleep(0x201);                    // sleep for 513 cycles
  register<0> long fib1{8} = 0x1;  // init fib1 with registers array 0-7 to 0x1 vectorized 8x
  register<8> long fib2{8} = 0x1;  // init fib2 with registers array 8-15 to 0x1 vectorized 8x
  register<16> long fib3{8} = 0x0; // init fib3 with registers array 16-23 to 0x0 vectorized 8x
  register<24> long i = 0;         // init loop i with register 24 long integer value 0
  register<25> long imax = 0x20;   // init loop imax with register 25 long integer value 0x20
  register<26> long *mem = 0x28;   // init mem with register 26 long integer pointer at 0x28
  for (;i<imax;i++) {              // for loop long integer i index value from 0 to 31
    fib3{8} = fib2{8};             // copy array of old fib2 values to fib3 vectorized 8x
    fib2{8} = fib1{8};             // copy array of old fib1 values to fib2 vectorized 8x
    fib1{8} = fib2{8} + fib3{8};   // calculate array of new fib1 adding fib2 and fib3 vectorized 8x
    mem{8} = fib1{8};              // store array of fib1 values to mem location index vectorized 8x
    mem += 8;                      // move memory pointer 8 indexes forward
  }                                // for loop close
}                                  // infinite while loop close
```
