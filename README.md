# Java OpenCL Logic Circuit Simulator
Java OpenCL Logic Circuit Simulator for simulating and debugging fully pipelined binary gate logic.

```
Code format: 4-int32 blocks of [ARGUMENT-PTR1 OPERATION ARGUMENT-PTR2: STORE-PTR3].
Value format: 32-bit shared integer and floating point values (int32 and fp32).
Run format: new-old value store-update each gate once per clock cycle.
```

Operation list:
```
0=BUF: delay buffer
1=NOT: invert arg1
2=AND: bitwise and
3=OR: bitwise or
4=XOR: bitwise xor
5=NAND: bitwise nand
6=NOR: bitwise nor
7=XNOR: bitwise xnor
8=SHL: shift left
9=SHR: shift right

10=NEG: negate arg1
11=SUM: arg1 sum arg2
12=MUL: arg1 multiply arg2
13=DIV: arg1 division by arg2

14=COS: cos arg1
15=SIN: sin arg1
16=TAN: tan arg1
17=ACOS: acos arg1
18=ASIN: asin arg1
19=ATAN: atan arg1
20=LOG: log arg1
21=EXP: exp arg1
22=POW: arg1 power to arg2
23=SQRT: sqrt arg1
24=NROOT: nth-root arg1

25=ZERO: zero value
26=ITOF: convert int32 to fp32 value
27=FTOI: convert fp32 to int32 value
```
