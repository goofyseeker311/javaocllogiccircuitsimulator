# Java OpenCL Logic Circuit Simulator

Logic Circuit Assembler (LCA) / Logic Gate Assembler (LGA)

Java OpenCL Logic Circuit Simulator for simulating and debugging fully pipelined binary gate logic.
Includes visual designer that also converts OpenCL C code to binary micro-fpga gate logic.

* Not designed to be sequential operation execution similar to assembly code, but rather be a continuous execution circuit definition language with core-width x pipeline-depth input work item dimensions.
* System architecture is based on 1-cycle latency FPGA gates and large SRAM block with three full-length sram block mimo OR-multiplexers to read/write int32 argument/indirect($) and store pointer values directly for each gate.
* Any external communication to the logic gate system is through SRAM direct read/write, from such as PCIe, USB, SD-card, HBM or DDR5 memory bridge controllers.
* Each separately programmable/assignable micro-FPGA gate runs internally at a multiplier speed of the main circuit clock speed to enable one clock cycle per gate operation.
* Programmer/IDE is responsible of assigning correct output pointer values for each gate considering multiple OR-multiplexed value store collision.

Logisim evolution v4.1.0 used for circuit illustrations and functional models: https://github.com/logisim-evolution/logisim-evolution

GNU Octave 11.1.0 used for generic math and generating circuit constants: https://octave.org

HxD - Hex Editor and Disk Editor: https://mh-nexus.de/en/hxd/

![logiccircuitgateassembler](https://github.com/user-attachments/assets/2a9904c7-4958-4e9e-9b49-f2199cf8d8c1)
![logicgatepipelinecompute35a](https://github.com/user-attachments/assets/b4f329cd-e06d-4db8-9960-a326d26a73a0)
<img width="3840" height="2160" alt="gatepipelinecomputearchitecture50a" src="https://github.com/user-attachments/assets/01ce93e4-1485-4f61-84ef-cb870990b77b" />
<img width="3840" height="2160" alt="computecorefpganetwork16a" src="https://github.com/user-attachments/assets/b6b8fab4-c29f-4b8b-a790-f336ad341ca0" />
<img width="3840" height="2160" alt="misccomputechip16a" src="https://github.com/user-attachments/assets/4a07f1a0-883b-4efd-89a6-f1136022905a" />
<img width="3840" height="2112" alt="muxrisccore73" src="https://github.com/user-attachments/assets/bde29009-ce36-4190-9923-9c82a71c1bfb" />
<img width="3840" height="2112" alt="muxrisccore73a" src="https://github.com/user-attachments/assets/7c65f111-99c3-4cac-aaec-883bcdb63c9a" />

---




RISC core-gate instruction set architecture (64-bit variation of RISC-V):
```
MISC compute chip contains 64k cores, total of 32GB register nvsram and 8TB memory nvsram.
Each core contains 64k local 64-bit ram registers. load/store instructions can address global memory.
Each core contains 24-bit addressed 128MB ram, including rom, ram, touch-display ram, and nand nvram.
Every instruction uses/operates on full 64-bit register values always, and runs in 1 cycle.
Instruction high bits can contain specific simple variations of instructions, and vector duplicates.
Each 64-bit instruction is formed from 16-bit [regX regY regZ insT] parameters.
insT parameter is formed from 8-4-4-bit [vecN insV insO] parameters.
Estimated logic transistors per core is 200k making 64k cores about 12.8 billion.
Estimated ram transistors per core is 8million 512KB and 256billion total 32GB.
Estimated compute 64-bit teraops at 5GHz x 8-vector per core is 40gops and 2560tops total.

Opcode | Instruction | Name               | Description
----------------------------------------------------------------------------------------------------
any    | ##          | Any Raw Data       | direct data line 64-bit value
0      | nopYZ       | No Operation       | no operation sleep constant regYZ cycles
         []                                 empty line or white space line
         //                                 comment line
1      | jmpXY       | Jump Destination   | jump to regX if regY is not zero
         jmpcXY                             insV=0 jump to regX if regY is not zero
         jmpXY                              insV=1 unconditional jump to regX
2      | ldiXYZ      | Load 32-bit Uint   | load regX with constant regYZ
3      | memXY       | Memory Double      | store/load[insV] regX at memory[regY]
         memrXY                             insV=0 load local
         memwXY                             insV=1 store local
         smemrXY                            insV=2 load shared
         smemwXY                            insV=3 store shared
4      | cmpXY       | Compare to Zero    | clear regX to 0, set to 1 if regY comp[insV]
         cmpeXY                             insV=0 integer equal to
         cmplXY                             insV=1 integer less than
         fcmpeXY                            insV=2 float equal to
         fcmplXY                            insV=3 float less than
5      | intXYZ      | ALU Int Operation  | store integer op[insV] regY regZ to regX
         addXYZ                             insV=0 integer add
         addoXYZ                            insV=1 integer add overflow bit
         subXYZ                             insV=2 integer subtract
         subbXYZ                            insV=3 integer subtract borrow bit
         mulXYZ                             insV=4 integer multiply
         muloXYZ                            insV=5 integer multiply overflow
         divXYZ                             insV=6 integer divide
         divrXYZ                            insV=7 integer divide remainder
         negXYZ                             insV=8 integer negate
         clkXYZ                             insV=9 integer clock counter
         rndXYZ                             insV=A integer clock random
6      | bitXYZ      | ALU Bit Operation  | store bitwise op[insV] regY regZ to regX
         shlXYZ                             insV=0 bitwise shift left regZ bits
         shrXYZ                             insV=1 bitwise shift right regZ bits
         sharXYZ                            insV=2 bitwise shift arithmetic right regZ bits
         rotlXYZ                            insV=3 bitwise rotate left regZ bits
         rotrXYZ                            insV=4 bitwise rotate right regZ bits
         copyXYZ                            insV=5 bitwise copy
         notXYZ                             insV=6 bitwise not
         orXYZ                              insV=7 bitwise or
         andXYZ                             insV=8 bitwise and
         nandXYZ                            insV=9 bitwise nand
         norXYZ                             insV=A bitwise nor
         xorXYZ                             insV=B bitwise xor
         xnorXYZ                            insV=C bitwise xnor
         copycXYZ                           insV=D bitwise conditional copy if regZ is not zero
7      | flpXYZ      | ALU Flp Operation  | store float op[insV] regY regZ to regX
         faddXYZ                            insV=0 float add
         fsubXYZ                            insV=1 float subtract
         fmulXYZ                            insV=2 float multiply
         fdivXYZ                            insV=3 float divide
         fnegXYZ                            insV=4 float negate
         fitfXYZ                            insV=5 integer to float
         ftinXYZ                            insV=6 float to integer nearest
         ftidXYZ                            insV=7 float to integer round down
         ftiuXYZ                            insV=8 float to integer round up
         ftitXYZ                            insV=9 float to integer truncate
         finfXYZ                            insV=A float is infinity
         fnanXYZ                            insV=B float is not-a-number
8      | flpaXYZ     | ALU FlpA Operation | store advanced float op[insV] regY regZ to regX
         fsinXYZ                            insV=0 float sine
         ftanXYZ                            insV=1 float tangent
         fcosXYZ                            insV=2 float cosine
         fasinXYZ                           insV=3 float arcsine
         fatanXYZ                           insV=4 float arctangent
         facosXYZ                           insV=5 float arccosine
         flogXYZ                            insV=6 float logarithm
         fpowXYZ                            insV=7 float power
         fsqrtXYZ                           insV=8 float square root
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
jmpc 001d 001f         | 001d001f00000001 | jump to register 29 if register 31 is not zero
jmpu 0020              | 0020000000000011 | unconditional jump to register 32
##   A123456789ABCDEF  | a123456789abcdef | custom data segment with any instruction or data
```

Example looping test assembly to c-code approximate:
```
while(true) {                        // infinite while loop
  register<0> long fib1[8] = 0x1;    // init fib1 with registers array 0-7 to long integer value 1
  register<8> long fib2[8] = 0x1;    // init fib2 with registers array 8-15 to long integer value 1
  register<16> long fib3[8] = 0x0;   // init fib3 with registers array 16-23 to long integer value 0
  register<24> long i = 0;           // init loop i with register 24 long integer value 0
  register<25> long imax = 32;       // init loop imax with register 25 long integer value 32
  register<26> long *mem = 0x18;     // init mem with register 26 long integer pointer at 0x18
  for (;i<imax;i++) {                // for loop long integer i index value from 0 to 31
    fib3{} = fib2{};                 // copy array of old fib2 values to fib3 vectorized
    fib2{} = fib1{};                 // copy array of old fib1 values to fib2 vectorized
    fib1{} = fib2{} + fib3{};        // calculate array of new fib1 adding fib2 and fib3 vectorized
    mem{} = fib1{};                  // store array of fib1 values to mem location index vectorized
    mem += 8;                        // move memory pointer 8 indexes forward
  }                                  // for loop close
}                                    // infinite while loop close
```
