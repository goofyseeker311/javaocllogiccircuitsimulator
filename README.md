# Java OpenCL Logic Circuit Simulator

Logic Circuit Assembler (LCA) / Logic Gate Assembler (LGA)

Java OpenCL Logic Circuit Simulator for simulating and debugging fully pipelined binary gate logic.
Includes visual designer that also converts OpenCL C code to binary micro-fpga gate logic.

* Not designed to be sequential operation execution similar to assembly code, but rather be a continuous execution circuit definition language with core-width x pipeline-depth input work item dimensions.
* System architecture is based on 1-cycle latency FPGA gates and large SRAM block with three full-length sram block mimo OR-multiplexers to read/write int32 argument/indirect($) and store pointer values directly for each gate.
* Any external communication to the logic gate system is through SRAM direct read/write, from such as PCIe, USB, SD-card, HBM or DDR5 memory bridge controllers.
* Each separately programmable/assignable micro-FPGA gate runs internally at a multiplier speed of the main circuit clock speed to enable one clock cycle per gate operation.
* Programmer/IDE is responsible of assigning correct output pointer values for each gate considering multiple OR-multiplexed value store collision.

Logisim evolution 4.0.0 used for circuit illustrations and functional models: https://github.com/logisim-evolution/logisim-evolution

GNU Octave 10.3.0 used for generic math and generating circuit constants: https://octave.org

HxD - Hex Editor and Disk Editor: https://mh-nexus.de/en/hxd/

![logiccircuitgateassembler](https://github.com/user-attachments/assets/2a9904c7-4958-4e9e-9b49-f2199cf8d8c1)
![logicgatepipelinecompute35a](https://github.com/user-attachments/assets/b4f329cd-e06d-4db8-9960-a326d26a73a0)
<img width="3840" height="2160" alt="gatepipelinecomputearchitecture50a" src="https://github.com/user-attachments/assets/01ce93e4-1485-4f61-84ef-cb870990b77b" />
<img width="3840" height="2160" alt="computecorefpganetwork16a" src="https://github.com/user-attachments/assets/b6b8fab4-c29f-4b8b-a790-f336ad341ca0" />
<img width="3840" height="2112" alt="muxrisccore50" src="https://github.com/user-attachments/assets/f179682b-2e56-4c9d-abce-88d6c70294d5" />
<img width="3840" height="2112" alt="muxrisccore50a" src="https://github.com/user-attachments/assets/94aff471-252b-48b5-bf52-f545a078816c" />
<img width="3840" height="2112" alt="muxrisccore50b" src="https://github.com/user-attachments/assets/dffeb99a-ce11-43bf-bb42-bd4f5e5cc01b" />
<img width="3840" height="2112" alt="muxrisccore50c" src="https://github.com/user-attachments/assets/f1b58bc1-30cd-461c-a820-e72b09ae172e" />

---




RISC core-gate instruction set architecture (64-bit variation of RISC-V):
```
Each core contains 2x 32k core-rail and 1-to-1 routing lines, 512 io-lines, and 1024 registers.
Every instruction uses/operates on full 64-bit register values always.
Instruction high bits can contain specific simple variations of instructions.
Each 64-bit instruction is formed from 16-bit [regX regY regZ insT] parameters.
insT parameter is formed from 8-4-4-bit [bitI insV insO] parameters.
Estimated logic transistors per core is 200k making 32k cores about 6.4 billion.
Estimated ram transistors per core is 4million 512KB and 128billion total 16GB.
Estimated compute 64-bit teraops at 5GHz per core is 5gops and 160tops total.

Opcode | Cycles | Instruction | Name              | Description
----------------------------------------------------------------------------------------------------
any    | any    | ##          | Any Raw Data      | direct data line 64-bit value
0      | 1      | nopYZ       | No Operation      | no operation sleep constant regYZ cycles
                  []                                empty line or white space line
                  //                                comment line
1      | 1      | jmpXY       | Jump Destination  | jump to regX if regYb[bitI] is set
                  jmpcXY                            insV=0 jump to regX if regYb[bitI] is set
                  jmpuXY                            insV=1 unconditional jump to regX
2      | 1      | ldiXYZ      | Load 32-bit Uint  | load regX with constant regYZ
3      | 2      | memXY       | Memory Double     | store/load[insV] regX at memory[regY]
                  memrXY                            insV=0 load
                  memwXY                            insV=1 store
4      | 1      | cmpXY       | Compare to Zero   | clear regXb[bitI], set to 1 if regY comp[insV]
                  cmpeXY                            insV=0 integer equal to
                  cmplXY                            insV=1 integer less than
                  cmpefXY                           insV=2 float equal to
                  cmplfXY                           insV=3 float less than
5      | 1      | intXYZ      | ALU Int Operation | store integer op[insV] regY regZ to regX
                  addXYZ                            insV=0 integer add
                  addoXYZ                           insV=1 integer add overflow bit regXb[bitI]
                  subXYZ                            insV=2 integer subtract
                  subbXYZ                           insV=3 integer subtract borrow bit regXb[bitI]
                  mulXYZ                            insV=4 integer multiply
                  muloXYZ                           insV=5 integer multiply overflow
                  divXYZ                            insV=6 integer divide
                  divrXYZ                           insV=7 integer divide remainder
                  negXYZ                            insV=8 integer negate
6      | 1      | bitXYZ      | ALU Bit Operation | store bitwise op[insV] regY regZ to regX
                  shlXYZ                            insV=0 bitwise shift left regZ bits
                  shrXYZ                            insV=1 bitwise shift right regZ bits
                  sharXYZ                           insV=2 bitwise shift arithmetic right regZ bits
                  rotlXYZ                           insV=3 bitwise rotate left regZ bits
                  rotrXYZ                           insV=4 bitwise rotate right regZ bits
                  copyXYZ                           insV=5 bitwise copy
                  notXYZ                            insV=6 bitwise not
                  orXYZ                             insV=7 bitwise or
                  andXYZ                            insV=8 bitwise and
                  nandXYZ                           insV=9 bitwise nand
                  norXYZ                            insV=A bitwise nor
                  xorXYZ                            insV=B bitwise xor
                  xnorXYZ                           insV=C bitwise xnor
7      | 1      | flpXYZ      | ALU Flp Operation | store float op[insV] regY regZ to regX
                  addfXYZ                           insV=0 float add
                  subfXYZ                           insV=1 float subtract
                  mulfXYZ                           insV=2 float multiply
                  divfXYZ                           insV=3 float divide
                  negfXYZ                           insV=4 float negate
                  itfXYZ                            insV=5 integer to float
                  ftinXYZ                           insV=6 float to integer nearest
                  ftidXYZ                           insV=7 float to integer round down
                  ftiuXYZ                           insV=8 float to integer round up
                  ftitXYZ                           insV=9 float to integer truncate
```

Example boot loader assembly code source and binary:
```
source listing      | binary           | explanation
----------------------------------------------------------------------------------------------------
ldi  0000 00000000  | 0000000000000002 | load register 0 with value 0x00000 rom read addr counter
ldi  0001 00020000  | 0001000200000002 | load register 0 with value 0x20000 ram write addr counter
ldi  0002 00000001  | 0002000000010002 | load register 0 with value 0x00001 constant value 0x1
ldi  0003 00020000  | 0003000200000002 | load register 0 with value 0x20000 constant jump address
ldi  0004 000000ff  | 0004000000ff0002 | load register 0 with value 0x000ff rom to ram index limit
ldi  0005 00000005  | 0005000000050002 | load register 0 with value 0x00005 contant jump address
copy 00df 0000      | 00df000000000056 | copy register 0 rom read addr to output reg223 rom addr
copy 0006 00df      | 000600df00000056 | copy core input register 223 rom data to register 6
memw 0006 0001      | 0006000100000013 | store register 6 to register 1 memory location
add  0000 0000 0002 | 0000000000020005 | store addition of register 0 and register 2 to register 0
add  0001 0001 0002 | 0001000100020005 | store addition of register 1 and register 2 to register 1
sub  0007 0000 0004 | 0007000000040025 | store subtract of register 0 and register 4 to register 7
cmpl 0008 0007      | 0008000700000014 | clear register 8 bit 0, set if register 7 int less than 0
jmpc 0005 0008      | 0005000800000001 | jump to register 5 if register 8 bit 0 is set
jmpu 0003           | 0003000000000011 | unconditional jump to register 3
```

Example looping test assembly code source and binary:
```
source listing      | binary           | explanation
----------------------------------------------------------------------------------------------------
[]                  | 0000000000000000 | empty line
// empty line       | 0000000000000000 | comment line
nop  00000200       | 0000000002000000 | no operation sleep 512+1 cycles
ldi  0000 00000001  | 0000000000010002 | load register 0 with value 0x1, current fibonacci number
ldi  0001 00000001  | 0001000000010002 | load register 1 with value 0x1, previous fibonacci number
ldi  0002 00000000  | 0002000000000002 | load register 2 with value 0x0, previous+ fibonacci number
ldi  0003 00000000  | 0003000000000002 | load register 3 with value 0x0, for loop index from 0
ldi  0004 00000020  | 0004000000200002 | load register 4 with value 0x20, for loop less than 32
ldi  0005 00020018  | 0005000200180002 | load register 5 with value 0x20018, ram store start index
ldi  0006 00000001  | 0006000000010002 | load register 6 with value 0x1, constant 0x1 add and jump
ldi  0007 0002000C  | 00070002000C0002 | load register 7 with value 0x2000C constant jump address
ldi  000b 00020000  | 000b000200000002 | load register 11 with value 0x20000 constant jump address
copy 0002 0001      | 0002000100000056 | copy register 1 to register 2
copy 0001 0000      | 0001000000000056 | copy register 0 to register 1
add  0000 0001 0002 | 0000000100020005 | store addition of register 1 and register 2 to register 0
add  000a 0005 0003 | 000a000500030005 | store addition of register 5 and register 3 to register 10
memw 0000 000a      | 0000000a00000013 | store register 0 to register 10 memory location
add  0003 0003 0006 | 0003000300060005 | store addition of register 3 and register 6 to register 3
sub  0008 0003 0004 | 0008000300040025 | store subtract of register 3 and register 4 to register 8
cmpl 0009 0008      | 0009000800000014 | clear register 9 bit 0, set if register 8 int less than 0
jmpc 0007 0009      | 0007000900000001 | jump to register 7 if register 9 bit 0 is set
jmpu 000b           | 000b000000000011 | unconditional jump to register 11
## A123456789ABCDEF | a123456789abcdef | custom data segment with any instruction or data
```

Example looping test assembly to c-code approximate:
```
while(true) {                   // infinite while loop
  long fib1 = 0x1;              // init fib1 with 64-bit long integer value 1
  long fib2 = 0x1;              // init fib2 with 64-bit long integer value 1
  long fib3 = 0x0;              // init fib3 with 64-bit long integer value 0
  long *mem = 0x18;             // init mem as 64-bit long integer pointer at address 0x18
  for (long i=0;i<32;i++) {     // for loop 64-bit long integer i index value from 0 to 31
    fib3 = fib2;                // copy old fib2 value to fib3
    fib2 = fib1;                // copy old fib1 value to fib2
    fib1 = fib2 + fib3;         // calculate new fib1 value by adding fib2 and fib3
    mem[i] = fib1;              // store fib1 value to mem location +i index
  }                             // for loop close
}                               // infinite while loop close
```
