nop   ff rom header boot load size
nop   01 rom header data
nop   02 rom header data
nop   03 rom header data
nop   04 rom header data
nop   05 rom header data
nop   06 rom header data
nop   07 rom header data
nop   A0 rom header data
nop   A1 rom header data
nop   A2 rom header data
nop   A3 rom header data
nop   A4 rom header data
nop   A5 rom header data
nop   A6 rom header data
nop   A7 rom header data

// empty line
nop   00000200
ldi   0000 00000001 ff
ldi   0008 00000001 ff
ldi   0010 00000000 ff
ldi   0018 00000000
ldi   0019 00000020
ldi   001a 00000028
ldi   001b 00000001
ldi   001c 00000008
ldi   001d 0000001D
ldi   0020 00000010
copy  0010 0008 0000 ff
copy  0008 0000 0000 ff
add   0000 0008 0010 ff
memw  0000 001a 0000 ff
add   001a 001a 001c
add   0018 0018 001b
sub   001e 0018 0019
cmplz 001f 001e
jmpc  001d 001f
jmp   0020
##    A123456789ABCDEF some data