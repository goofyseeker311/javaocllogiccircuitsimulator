nop      ff rom header boot load size
nop      01 rom header data
nop      02 rom header data
nop      03 rom header data
nop      04 rom header data
nop      05 rom header data
nop      06 rom header data
nop      07 rom header data
nop      A0 rom header data
nop      A1 rom header data
nop      A2 rom header data
nop      A3 rom header data
nop      A4 rom header data
nop      A5 rom header data
nop      A6 rom header data
nop      A7 rom header data

// empty line
nop      00000000
ldi      0000 00000013
ldi      0001 00000010
ldi      0002 000000ff 7
shl      0003 0003 0001
shl      0004 0004 0001
shl      0004 0004 0001
ldi      0005 00000000
ldi      0006 00000200
ldi      0007 00000400
ldi      0008 80000000 ff
ldi      0010 00000020 ff
shl      0008 0008 0010 ff
add      0005 0005 0008 7
ldi      0010 00000001
ldi      0011 00000201
ldi      0012 00000401
ldi      0013 00000601
ldi      0014 00000801
ldi      0015 00000a01
ldi      0016 00000c01
ldi      0017 00000e01
add      0018 0008 0010 ff
ldi16    0020 00ff
memw     0020 0018
memw     0020 0019
memw     0020 001a
memw     0020 001b
memw     0020 001c
memw     0020 001d
memw     0020 001e
memw     0020 001f
memw     0002 0005
memw     0003 0006
memw     0004 0007
ldi      0021 00000000
ldi      0022 00000001
sub      0023 0021 0022
cmpez    0024 0021
cmpez    0025 0022
cmpez    0026 0023
cmplz    0027 0021
cmplz    0028 0022
cmplz    0029 0023
ldi      002a 00000041
jmpc     002a 0024
ldi      002b 01234567
jmpc     002a 0028
memr     002c 0000
memw     0020 0010
ldi      002d ffffffff
ldi      002e 8fffffff
ldi      002f 00000020
shl      002d 002d 002f
shl      002e 002e 002f
or       002d 002d 0012
or       002e 002e 0013
add      0030 002d 002e
addo     0031 002d 002e
sub      0032 002e 002d
subb     0033 002e 002d
mul      0034 002d 002e
mulo     0035 002d 002e
div      0036 0017 0014
divr     0037 0017 0014
neg      0038 002d
clk      0039 0000 0021
rnd      003a 0000 0021
freq     003b 0000 0021
core     003c 0010
time     003d 0000 0021
shr      003e 002d 002f
shar     003f 002e 002f
rotl     0040 002e 002f
rotr     0041 002d 002f
copy     0042 002e
not      0043 002e
or       0044 002d 002e
and      0045 002d 002e
nand     0046 002d 002e
nor      0047 002d 002e
xor      0048 002d 002e
xnor     0049 002d 002e
copyc    004a 002d 0021
copyc    004b 002d 0022
lone     004c 0021
lone     004d 0022
hone     004e 0021
hone     004f 0022
lzero    0050 0021
lzero    0051 0022
hzero    0052 0021
hzero    0053 0022
ones     0054 0022
ones     0055 002e
ldi      0056 BFF00000
shl      0056 0056 002f
ldi      0057 407C8000
shl      0057 0057 002f
fadd     0058 0056 0057
fsub     0059 0056 0057
fmul     005a 0056 0057
fdiv     005b 0056 0057
fneg     005c 0056
fitf     005d 002d
fadd     005e 005b 0057
ftin     005f 005e  
ftid     0060 005e
ftiu     0061 005e
ftit     0062 005e
finf     0063 005e
fnan     0064 005e
fsin     0065 0057 
ftan     0066 0057
fcos     0067 0057
fasin    0068 0065 
fatan    0069 0066
facos    006a 0067
ldi      006b 40240000
shl      006b 006b 002f
flog     006c 0057 006b
fpow     006d 0057 006b
fsqrt    006e 0057
fcmpez   006f 0059
fcmplz   0070 0059
ldi      0071 00000123
ldi      0072 00000456
cmpe     0073 0071 0072
cmpl     0074 0071 0072
fcmpe    0075 0059 0059
fcmpl    0076 0059 005a
ldi      0077 12345678
ldi32    0078 12345678
ldi16    0079 1234
ldi8     007a 12
ldi      007b BF800000
ldi32    007c FFFFFFFF
ldi16    007d 0000
ldi8     007e 00
ldi      007f 00000001
ldi32    0080 00000001
ldi16    0081 0001
ldi8     0082 01
cmpez32  0083 007e
cmplz32  0084 007c
fcmpez32 0085 007e
fcmplz32 0086 007b
copyc32  0087 007c 0080
cmpez16  0088 007e
cmplz16  0089 007c
fcmpez16 008a 007e
fcmplz16 008b 007b
copyc16  008c 007c 0081
cmpez8   008d 007e
cmplz8   008e 007c
fcmpez8  008f 007e
fcmplz8  0090 007b
copyc8   0091 007c 0082
shl32    0092 0078 0080
shr32    0093 0078 0080
shar32   0094 007c 0080
rotl32   0095 007c 0080
rotr32   0096 007c 0080
shl16    0097 0079 0081
shr16    0098 0079 0081
shar16   0099 007c 0081
rotl16   009a 007c 0081
rotr16   009b 007c 0081
shl8     009c 007a 0082
shr8     009d 007a 0082
shar8    009e 007c 0082
rotl8    009f 007c 0082
rotr8    00a0 007c 0082
add32    00a1 0078 007c
sub32    00a2 0078 007c
mul32    00a3 0078 007c
div32    00a4 0078 007c
neg32    00a5 0078
add16    00a6 0078 007c
sub16    00a7 0078 007c
mul16    00a8 0078 007c
div16    00a9 0078 007c
neg16    00aa 0078
add8     00ab 0078 007c
sub8     00ac 0078 007c
mul8     00ad 0078 007c
div8     00ae 0078 007c
neg8     00af 0078
ldi8     00b0 CC
ldi8     00b1 CF
fadd32   00b2 00b0 00b1
fsub32   00b3 00b0 00b1
fmul32   00b4 00b0 00b1
fdiv32   00b5 00b0 00b1
fneg32   00b6 00b0
fadd16   00b7 00b0 00b1
fsub16   00b8 00b0 00b1
fmul16   00b9 00b0 00b1
fdiv16   00ba 00b0 00b1
fneg16   00bb 00b0
fadd8    00bc 00b0 00b1
fsub8    00bd 00b0 00b1
fmul8    00be 00b0 00b1
fdiv8    00bf 00b0 00b1
fneg8    00c0 00b0
jmp      0000
##       A123456789ABCDEF some data