# Java OpenCL Logic Circuit Simulator
Java OpenCL Logic Circuit Simulator for simulating and debugging fully pipelined binary gate logic.

```
Code format: 4-int32 blocks of [ARGUMENT-PTR1 OPERATION ARGUMENT-PTR2: STORE-PTR3].
Value format: 32-bit integer (int32), two values make 32+32 floating point value.
Run format: new-old value store-update each gate once per clock cycle.
```

Operation list:
```
BUF: delay buffer
NOT: invert arg1
AND: bitwise and
OR: bitwise or
XOR: bitwise xor
NAND: bitwise nand
NOR: bitwise nor
XNOR: bitwise xnor
SHL: bitwise shift left
SHR: bitwise shift right

NEG: negate arg1
SUM: arg1 sum arg2
MUL: arg1 multiply arg2
DIV: arg1 division by arg2

COS: cos arg1
SIN: sin arg1
TAN: tan arg1
ACOS: acos arg1
ASIN: asin arg1
ATAN: atan arg1
LOG: log arg1
EXP: exp arg1
POW: arg1 power to arg2

ZERO: zero value
```
