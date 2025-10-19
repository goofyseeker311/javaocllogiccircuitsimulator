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
<img width="3840" height="2112" alt="muxrisccore40" src="https://github.com/user-attachments/assets/8a057d78-a776-4e1e-9a5a-221dd2944913" />
<img width="3840" height="2112" alt="muxrisccore40a" src="https://github.com/user-attachments/assets/d82b4d79-d85c-4488-ab7a-ad4d7f0f92bf" />
<img width="3840" height="2112" alt="muxrisccore40b" src="https://github.com/user-attachments/assets/8209299e-88e7-4976-ba9c-e52a5e7732f5" />

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
source listing           | binary           | explanation
----------------------------------------------------------------------------------------------------
ldi0000 0x1              | 0000000000010002 | load register 0 with value 0x1
nop 0x2                  | 0000000000020000 | nop operation 3 cycles
jmp00010000              | 0001000000000001 | jump to register 1 if register 0 bit 0 is set
```
