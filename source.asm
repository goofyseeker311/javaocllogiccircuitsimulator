       ##    0000000000000100   // rom header boot load size
       ##    0000000000000100   // rom header data
       ##    0000000000000200   // rom header data
       ##    0000000000000300   // rom header data
       ##    0000000000000400   // rom header data
       ##    0000000000000500   // rom header data
       ##    0000000000000600   // rom header data
       ##    0000000000000700   // rom header data
       ##    000000000000a000   // rom header data
       ##    000000000000a100   // rom header data
       ##    000000000000a200   // rom header data
       ##    000000000000a300   // rom header data
       ##    000000000000a400   // rom header data
       ##    000000000000a500   // rom header data
       ##    000000000000a600   // rom header data
       ##    000000000000a700   // rom header data

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