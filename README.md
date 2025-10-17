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
<img width="3840" height="2112" alt="microfpgacircuittest14" src="https://github.com/user-attachments/assets/7b611062-62d1-4d2c-9c83-90c439514bf0" />
<img width="3840" height="2112" alt="microfpgamuxcircuit28" src="https://github.com/user-attachments/assets/31f04935-9ac9-4a7f-8d38-807c3e7f146a" />
<img width="3840" height="2112" alt="microfpgamuxselector27" src="https://github.com/user-attachments/assets/bffd51a5-1548-46d6-8b62-1c6937615e2a" />
<img width="3840" height="2112" alt="microfpgamuxalu33" src="https://github.com/user-attachments/assets/a9b40178-3a3b-41aa-88fb-0a2f3034e2fd" />
<img width="3840" height="2112" alt="microfpgamuxfmod5" src="https://github.com/user-attachments/assets/72dc29df-4bd5-4417-afcd-7dfbe70e1a2e" />
<img width="3840" height="2112" alt="microfpgamuxsine7" src="https://github.com/user-attachments/assets/8e38e587-1619-490c-9b04-2502b639f691" />
<img width="3840" height="2112" alt="microfpgamuxcosine4" src="https://github.com/user-attachments/assets/26747d83-2178-4161-963c-d0aaaa21622f" />
<img width="3840" height="2112" alt="microfpgamuxtangent4" src="https://github.com/user-attachments/assets/c0dcd30d-eb45-48db-a675-cf87ad7c5466" />
<img width="3840" height="2112" alt="microfpgamuxarcsine2" src="https://github.com/user-attachments/assets/4bfe1b68-6258-44c3-bf63-d59145e70b7f" />
<img width="3840" height="2112" alt="microfpgamuxarccosine" src="https://github.com/user-attachments/assets/5ff2f643-e955-4739-9aa2-7961b98e6d98" />
<img width="3840" height="2112" alt="microfpgamuxarctangent3" src="https://github.com/user-attachments/assets/7bfe8248-4260-4233-a56f-a481757d7fad" />
<img width="3840" height="2112" alt="microfpgamuxexp3" src="https://github.com/user-attachments/assets/f84be8ac-d7ab-47e9-a06c-0eccbcfc71d1" />
<img width="3840" height="2112" alt="microfpgamuxlogln5" src="https://github.com/user-attachments/assets/d9e21c49-ada4-4b20-9a1a-156f5eb623e6" />
<img width="3840" height="2112" alt="microfpgamuxpower6" src="https://github.com/user-attachments/assets/2b9922d5-8752-430a-a6cb-08e3059201e4" />
<img width="3840" height="2112" alt="microfpgamuxfmin" src="https://github.com/user-attachments/assets/01c06b6d-b471-48f1-9b07-eb5222dc5960" />
<img width="3840" height="2112" alt="microfpgamuxfmax" src="https://github.com/user-attachments/assets/dc3a8583-f3af-4334-b161-dbb1d0236c8f" />
<img width="3840" height="2112" alt="microfpgamuxmin" src="https://github.com/user-attachments/assets/e2eeb814-978c-4837-8f86-114b70549d21" />
<img width="3840" height="2112" alt="microfpgamuxmax" src="https://github.com/user-attachments/assets/2589ba11-5901-4700-bc6c-1ced6154b375" />
<img width="3840" height="2112" alt="muxrisccore31" src="https://github.com/user-attachments/assets/75ff74cb-1783-4bfb-a089-71e7cb20efac" />
<img width="3840" height="2112" alt="muxrisccore31a" src="https://github.com/user-attachments/assets/8258e8a1-d4bc-4254-8f6e-5cba72727178" />

---




Language text/binary syntax:
```
Code format: 4-int32 blocks of [OPERATION ($)ARGUMENT-PTR1 ($)ARGUMENT-PTR2: STORE-PTR1].
Value format: 32-bit shared integer and floating point values (int32 and fp32).
Run format: new-old value store-update each gate once per clock cycle.
```

Operation list:
```
0=BUF: delay buffer arg1
1=NOT: bitwise invert arg1
2=AND: bitwise arg1 and arg2
3=OR: bitwise arg1 or arg2
4=XOR: bitwise arg1 xor arg2
5=NAND: bitwise arg1 nand arg2
6=NOR: bitwise arg1 nor arg2
7=XNOR: bitwise arg1 xnor arg2
8=SHL: bitwise arg1 shift left by arg2
9=SHR: bitwise arg1 shift right by arg2

10=NEGi: negate arg1
11=SUMi: arg1 sum arg2
12=SUBi: arg1 minus arg2
13=MULi: arg1 multiply arg2
14=DIVi: arg1 division by arg2

15=COS: cos arg1 (float)
16=SIN: sin arg1 (float)
17=TAN: tan arg1 (float)
18=ACOS: acos arg1 (float)
19=ASIN: asin arg1 (float)
20=ATAN: atan arg1 (float)
21=LOG: log arg1 (float)
22=EXP: exp arg1 (float)
23=POW: arg1 power to arg2 (float)
24=SQRT: sqrt arg1 (float)
25=NROOT: arg1 nth-root to arg2 (float)

26=NULL: NULL value (0)
27=ITOF: convert arg1 to fp32 value
28=FTOI: convert arg1 to int32 value
29=MGET: get arg1 pointer value
30=MSTO: store arg1 value to arg2 pointer
31=IFBUF: delay buffer arg1 if arg2 is 1, otherwise NULL (0)

32=NEG: negate arg1 (float)
33=SUM: arg1 sum arg2 (float)
34=SUB: arg1 minus arg2 (float)
35=MUL: arg1 multiply arg2 (float)
36=DIV: arg1 division by arg2 (float)
```

User defined function blocks (memory-height x pipeline-depth):
```
#<NAME> ($)<ARG1-ptr-in> ... ($)<ARGn-ptr-in>: <STO1-ptr-out> ... <STOn-ptr-out>
<code blocks>
###
```

Comments:
```
//comment
```

RISC core-gate instruction set architecture (64-bit variation of RISC-V):
```
64x 64-bit general purpose registers (used by all type instructions equally).
Nx 64-bit direct-io routing registers (one register writeable for current core).
Every instruction uses/operates on full 64-bit register values always.
Instruction high bits can contain specific simple variations of instructions.
Each 64-bit instruction is formed from 8-bit [regX regY regZ ins4 ins3 ins2 ins1 insT] parameters.

Opcode | Cycles | Instruction | Name             | Arguments  | Description
----------------------------------------------------------------------------------------------------
0      | 1      | nop         | No Operation     | -          | no operation
1      | 1      | jmpXY       | Jump Destination | -          | jump to regX if regYb[ins2]
2      | 1      | ldiX        | Load 32-bit Uint | -          | load regX with constant [ins4321]
3      | 2      | memXY       | Memory Double    | -          | store/load[ins1] regX at [regY]
4      | 1      | cmpXY       | Compare to Zero  | -          | set regXb[ins2] if regY comp[ins1]
5      | 1      | aluXYZ      | ALU Operation    | -          | store alu-op[ins1] regY regZ to regX
```

Example looping test assembly code source and binary:
```
source listing    | binary           | explanation
----------------------------------------------------------------------------------------------------
ldi00 0x12345678  | 0000001234567802 | load 32-bit value 0x12345678 to register 0
cmp20bef0102      | 0102000000200204 | if register 2 float value is zero, set register 1 bit 32
alu+030001        | 0300010000000005 | store integer addition of register 0 and 1 to register 3
mem0002           | 0002000000000003 | read memory at register 2 position to register 0
jmp20b0201        | 0201000000200001 | jump to register 2 if register 1 bit 32 is set
```
