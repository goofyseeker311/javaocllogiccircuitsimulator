# Java OpenCL Logic Circuit Simulator
Java OpenCL Logic Circuit Simulator for simulating and debugging fully pipelined binary gate logic.

```
Code format: 4-int32 blocks of [ARGUMENT-PTR1 OPERATION ARGUMENT-PTR2: STORE-PTR3].
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

10=NEG: negate arg1
11=SUM: arg1 sum arg2
12=SUB: arg1 minus arg2
13=MUL: arg1 multiply arg2
14=DIV: arg1 division by arg2

15=COS: cos arg1
16=SIN: sin arg1
17=TAN: tan arg1
18=ACOS: acos arg1
19=ASIN: asin arg1
20=ATAN: atan arg1
21=LOG: log arg1
22=EXP: exp arg1
23=POW: arg1 power to arg2
24=SQRT: sqrt arg1
25=NROOT: nth-root arg1

26=ZERO: zero value
27=ITOF: convert arg1 to fp32 value
28=FTOI: convert arg1 to int32 value
29=MGET: get arg1 pointer value
30=MSTO: store arg1 value to arg2 pointer
31=IFBUF: delay buffer arg1 if arg2 is 1
```
