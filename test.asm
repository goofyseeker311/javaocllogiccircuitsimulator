nop      1ff rom header boot load size
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
ldi32    00c1 00000001
ldi      00c2 000000e3
jmpc32   00c2 00c1
ldi      00c3 ffaaffa1
ldi      00c4 bbaaccd1
ldi16    00c5 0001
ldi      00c6 000000e8
jmpc16   00c6 00c1
ldi      00c7 ffaaffa2
ldi      00c8 bbaaccd2
ldi8     00c9 01
ldi      00ca 000000ed
jmpc8    00ca 00c1
ldi      00cb ffaaffa3
ldi      00cc bbaaccd3
ldi8     00cd 4C
ldi8     00ce 40
fsin32   00cf 00cd
ftan32   00d0 00cd
fcos32   00d1 00cd
flog32   00d2 00cd 00ce
fpow32   00d3 00cd 00ce
fsin16   00d4 00cd
ftan16   00d5 00cd
fcos16   00d6 00cd
flog16   00d7 00cd 00ce
fpow16   00d8 00cd 00ce
fsin8    00d9 00cd
ftan8    00da 00cd
fcos8    00db 00cd
flog8    00dc 00cd 00ce
fpow8    00dd 00cd 00ce
fasin32  00de 00cf
fatan32  00df 00d0
facos32  00e0 00d1
fsqrt32  00e1 00cd
fasin16  00e2 00d4
fatan16  00e3 00d5
facos16  00e4 00d6
fsqrt16  00e5 00cd
fasin8   00e6 00d9
fatan8   00e7 00da
facos8   00e8 00db
fsqrt8   00e9 00cd
ldi8     00ea 78
ldi8     00eb FF
finf32   00ec 00ea
fnan32   00ed 00eb
finf16   00ee 00ea
fnan16   00ef 00eb
finf8    00f0 00ea
fnan8    00f1 00eb
fitf32   00f2 00cd
ftin32   00f3 00cd
ftid32   00f4 00cd
ftiu32   00f5 00cd
ftit32   00f6 00cd
fitf16   00f7 00cd
ftin16   00f8 00cd
ftid16   00f9 00cd
ftiu16   00fa 00cd
ftit16   00fb 00cd
fitf8    00fc 00cd
ftin8    00fd 00cd
ftid8    00fe 00cd
ftiu8    00ff 00cd
ftit8    0100 00cd
ldi8     0101 23
ldi8     0102 78
cmpe32   0103 0101 0101
cmpl32   0104 0101 0102
fcmpe32  0105 0101 0101
fcmpl32  0106 0101 0102
cmpe16   0107 0101 0101
cmpl16   0108 0101 0102
fcmpe16  0109 0101 0101
fcmpl16  010a 0101 0102
cmpe8    010b 0101 0101
cmpl8    010c 0101 0102
fcmpe8   010d 0101 0101
fcmpl8   010e 0101 0102
ldi8     010f 44
ldi8     0110 FF
ff32     0111 010f
f32f16   0112 010f
f16f8    0113 010f
ii32     0114 0110
i32i16   0115 0110
i16i8    0116 0110
f32f     0117 0111
f16f32   0118 0112
f8f16    0119 0113
i32i     011a 0114
i16i32   011b 0115
i8i16    011c 0116
ldi8     011d 56
ldi8     011e 78
ldi8     011f 00
ldi8     0120 01
lone32   0121 011f
lone32   0122 0120
hone32   0123 011f
hone32   0124 0120
lzero32  0125 011f
lzero32  0126 0120
hzero32  0127 011f
hzero32  0128 0120
ones32   0129 011f
ones32   012a 0120
lone16   012b 011f
lone16   012c 0120
hone16   012d 011f
hone16   012e 0120
lzero16  012f 011f
lzero16  0130 0120
hzero16  0131 011f
hzero16  0132 0120
ones16   0133 011f
ones16   0134 0120
lone8    0135 011f
lone8    0136 0120
hone8    0137 011f
hone8    0138 0120
lzero8   0139 011f
lzero8   013a 0120
hzero8   013b 011f
hzero8   013c 0120
ones8    013d 011f
ones8    013e 0120
ldi8     013f 9E
ldi8     0140 83
ldi8     0141 48
ldi8     0142 37
addo32   0143 013f 0140
subb32   0144 0140 013f
mulo32   0145 0141 0142
divr32   0146 0141 0142
addo16   0147 013f 0140
subb16   0148 0140 013f
mulo16   0149 0141 0142
divr16   014a 0141 0142
addo8    014b 013f 0140
subb8    014c 0140 013f
mulo8    014d 0141 0142
divr8    014e 0141 0142
ldi8     014f 29
ldi8     0150 F2
fmin     0151 014f 0150
fmax     0152 014f 0150
fabs     0153 0150
fmin32   0154 014f 0150
fmax32   0155 014f 0150
fabs32   0156 0150
fmin16   0157 014f 0150
fmax16   0158 014f 0150
fabs16   0159 0150
fmin8    015a 014f 0150
fmax8    015b 014f 0150
fabs8    015c 0150
ldi      01ff 00000000
clk      0200 01ff 01ff
nop      ffff
jmp      0000
##       A123456789ABCDEF some data