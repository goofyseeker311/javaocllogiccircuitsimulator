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
<img width="3840" height="2112" alt="microfpgacircuittest13" src="https://github.com/user-attachments/assets/b764072b-af20-471d-b2e9-a72cb4801107" />
<img width="3840" height="2112" alt="microfpgamuxcircuit27" src="https://github.com/user-attachments/assets/d9461cb6-6a66-4dd5-a225-2ad9f54599e5" />
<img width="3840" height="2112" alt="microfpgamuxselector26" src="https://github.com/user-attachments/assets/792274b5-8b44-48a9-91f0-bb0a68ba73c8" />
<img width="3840" height="2112" alt="microfpgamuxalu30" src="https://github.com/user-attachments/assets/e11bdb26-2844-4b51-98ce-e234508c33d3" />
<img width="3840" height="2112" alt="microfpgamuxfmod4" src="https://github.com/user-attachments/assets/439bdd78-4dba-414b-b33b-4a801484a269" />
<img width="3840" height="2112" alt="microfpgamuxsine6" src="https://github.com/user-attachments/assets/af358c01-4f4c-45f5-9ada-c43883df2b81" />
<img width="3840" height="2112" alt="microfpgamuxcosine3" src="https://github.com/user-attachments/assets/7b49584f-7204-4c99-b8fa-95be049996e4" />
<img width="3840" height="2112" alt="microfpgamuxtangent3" src="https://github.com/user-attachments/assets/281b3365-fb50-45d2-8e80-1209b7097b57" />
<img width="3840" height="2112" alt="microfpgamuxexp2" src="https://github.com/user-attachments/assets/63f314a2-ed8a-4684-a78d-970bcd3ddb84" />
<img width="3840" height="2112" alt="microfpgamuxlogln4" src="https://github.com/user-attachments/assets/3fc92388-5a1a-4fa4-a8d9-821f9501982e" />
<img width="3840" height="2112" alt="microfpgamuxpower5" src="https://github.com/user-attachments/assets/4ffc1ae9-d377-4033-bd39-cfd42d9f1460" />

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
