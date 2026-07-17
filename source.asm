       ##    00ff000000000000   // rom header boot load size
       ##    0001000000000000   // rom header data
       ##    0002000000000000   // rom header data
       ##    0003000000000000   // rom header data
       ##    0004000000000000   // rom header data
       ##    0005000000000000   // rom header data
       ##    0006000000000000   // rom header data
       ##    0007000000000000   // rom header data
       ##    00a0000000000000   // rom header data
       ##    00a1000000000000   // rom header data
       ##    00a2000000000000   // rom header data
       ##    00a3000000000000   // rom header data
       ##    00a4000000000000   // rom header data
       ##    00a5000000000000   // rom header data
       ##    00a6000000000000   // rom header data
       ##    00a7000000000000   // rom header data

       // empty line
START: nop   00000200
       ldi   0000 00000001 ff
       ldi   0008 00000001 ff
       ldi   0010 00000000 ff
       ldi   0018 00000000
       ldi   0019 00000020
       ldi   001a 00000028
       ldi   001b 00000001
       ldi   001c 00000008
COPY:  copy  0010 0008 0000 ff
       copy  0008 0000 0000 ff
       add   0000 0008 0010 ff
       memw  0000 001a 0000 ff
       add   001a 001a 001c
       add   0018 0018 001b
       sub   001e 0018 0019
       cmplz 001f 001e
       ldi   001d COPY
       jmpc  001d 001f
       jmpi  START
       ##    A123456789ABCDEF    // some data