# Java OpenCL Logic Circuit Simulator

Logic Circuit Assembler (LCA) / Logic Gate Assembler (LGA)

Java OpenCL Logic Circuit Simulator for simulating and debugging fully pipelined binary gate logic.

* Not designed to be sequential operation execution similar to assembly code, but rather be a continuous execution circuit definition language with core-width x pipeline-depth input work item dimensions.
* System architecture is based on 1-cycle latency FPGA gates and large SRAM block with three full-length sram block mimo multiplexers to read/write int32 argument and store pointer values directly for each gate.
* Any external communication to the logic gate system is through SRAM direct read/write, from such as PCIe, USB, SD-card, HBM or DDR5 memory bridge controllers.
* Each FPGA gate runs internally at a multiplier speed of the main circuit clock speed to enable one clock cycle per gate operation.

---

Language text/binary syntax:
```
Code format: 4-int32 blocks of [(#)OPERATION ARGUMENT-PTR1 ARGUMENT-PTR2: STORE-PTR3].
Value format: 32-bit shared integer and floating point values (int32 and fp32).
Run format: new-old value store-update each gate once per clock cycle.
```

Operation list:
```
0=BUF: delay buffer arg1
1=NOT: invert arg1
2=AND: bitwise and
3=OR: bitwise or
4=XOR: bitwise xor
5=NAND: bitwise nand
6=NOR: bitwise nor
7=XNOR: bitwise xnor
8=SHL: shift left
9=SHR: shift right

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
25=NROOT: nth-root arg1 (float)

26=ZERO: zero value
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

User defined operations block:
```
#<NAME> <ARG1-ptr-in> <ARG2-ptr-in>: <STO3-ptr-out>
<code blocks>
###
```

Comments:
```
//comment
```
