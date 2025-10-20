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

![logiccircuitgateassembler](https://github.com/user-attachments/assets/2a9904c7-4958-4e9e-9b49-f2199cf8d8c1)
![logicgatepipelinecompute35a](https://github.com/user-attachments/assets/b4f329cd-e06d-4db8-9960-a326d26a73a0)
<img width="3840" height="2160" alt="gatepipelinecomputearchitecture50a" src="https://github.com/user-attachments/assets/01ce93e4-1485-4f61-84ef-cb870990b77b" />
<img width="3840" height="2160" alt="computecorefpganetwork16a" src="https://github.com/user-attachments/assets/b6b8fab4-c29f-4b8b-a790-f336ad341ca0" />
<img width="3840" height="2112" alt="muxrisccore42" src="https://github.com/user-attachments/assets/f6bc5322-eeb8-4632-b7d0-7e2ab2a5ce73" />
<img width="3840" height="2112" alt="muxrisccore42a" src="https://github.com/user-attachments/assets/0fb596a1-13ae-4e87-ade2-7e3f155bd125" />
<img width="3840" height="2112" alt="muxrisccore42b" src="https://github.com/user-attachments/assets/17dbe6e9-e918-40f7-9383-d73f07839dd1" />

---




RISC core-gate instruction set architecture (64-bit variation of RISC-V):
```
64x 64-bit general purpose registers (used by all type instructions equally).
Nx 64-bit direct-io routing registers (one register writeable for current core).
Every instruction uses/operates on full 64-bit register values always.
Instruction high bits can contain specific simple variations of instructions.
Each 64-bit instruction is formed from 16-bit [regX regY regZ insT] parameters.
insT parameter is formed from 4-bit [bitI bitN insV insO] parameters.

Opcode | Cycles | Instruction | Name             | Description
----------------------------------------------------------------------------------------------------
0      | 1      | nop         | No Operation     | no operation sleep constant regYZ cycles
1      | 1      | jmpXY       | Jump Destination | jump to regX if regYb[bitIN]
2      | 1      | ldiX        | Load 32-bit Uint | load regX with constant regYZ
3      | 2      | memXY       | Memory Double    | store/load[insV] regX at [regY]
4      | 1      | cmpXY       | Compare to Zero  | clear regXb[bitIN], set to 1 if regY comp[insV]
5      | 1      | intXYZ      | ALU Operation    | store integer op[insV] regY regZ to regX
6      | 1      | binXYZ      | ALU Operation    | store binary op[insV] regY regZ to regX
7      | 1      | flpXYZ      | ALU Operation    | store float op[insV] regY regZ to regX
```

Example looping test assembly code source and binary:
```
source listing   | binary           | explanation
----------------------------------------------------------------------------------------------------
ldi0000 0x1      | 0000000000010002 | load register 0 with value 0x1, current fibonacci number
ldi0001 0x1      | 0001000000010002 | load register 1 with value 0x1, previous fibonacci number
ldi0002 0x0      | 0002000000000002 | load register 2 with value 0x0, previous+ fibonacci number
ldi0003 0x0      | 0003000000000002 | load register 3 with value 0x0, for loop index from 0
ldi0004 0x20     | 0004000000200002 | load register 4 with value 0x20, for loop less than 32
ldi0005 0x18     | 0005000000180002 | load register 5 with value 0x18, ram store start index
ldi0006 0x1      | 0006000000010002 | load register 6 with value 0x1, constant 0x1 add and jump
ldi0007 0x8      | 0007000000080002 | load register 7 with value 0x8, constant 0x8 jump address
binc00020001     | 0002000100000056 | copy register 1 to register 2
binc00010000     | 0001000000000056 | copy register 0 to register 1
int+000000010005 | 0000000100020005 | store sum of register 1 and register 2 to register 0
int+000a00050005 | 000a000500030005 | store sum of register 5 and register 3 to register 10
memw0000000a     | 0000000a00000013 | store register 1 to register 10 memory location
int+000300030006 | 0003000300060005 | store sum of register 3 and register 6 to register 3
int-000800030004 | 0008000300040025 | store difference of register 3 and register 4 to register 8
cmpl00090008     | 0009000800000014 | clear register 9 bit 0, set if register 8 integer less than 0
jmp00070009      | 0007000900000001 | jump to register 7 if register 9 bit 0 is set
jmp000b0006      | 000b000600000001 | jump to register 11 if register 6 bit 0 is set
```

Example looping test assembly to c-code approximate:
```
while(true) {
  long fib1 = 0x1;
  long fib2 = 0x1;
  long fib3 = 0x0;
  long *mem = 0x18;
  for (long i=0;i<32;i++) {
    fib3 = fib2;
    fib2 = fib1;
    fib1 = fib2 + fib3;
    mem[i] = fib1;
  }
}
```
