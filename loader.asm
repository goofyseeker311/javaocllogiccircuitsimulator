ldi  0000 00000000   // rom read index 0x00000 [ init variables ]
ldi  0001 00020000   // ram write index 0x20000
ldi  0002 00000001   // constant 0x1
ldi  0003 00020000   // jump address 0x20000
ldi  0004 00000100   // rom to ram copy size
ldi  0005 0000001A   // zero branch jump address
ldi  0006 00000011   // non-zero branch jump address
ldi  0007 0000FFFF   // 16-bit core num and filter
ldi  0008 0000FFFF   // 16-bit core rail and filter
ldi  0009 00000020   // 16-bit core rail and filter shift bits
copy 0010 00dc       // get current core id [ core id zero check ]
and  0011 0010 0007  // get core id core index
shl  0008 0008 0009  // shift rail mask left 32 bits
and  0012 0010 0008  // get core id rail index
shr  0012 0012 0009  // shift core id rail index right 32 bits
cmpe 0013 0011       // set 1 if core id is zero
jmpc 0005 0013       // jump to core zero code
nop  00000002        // exact sync wait 3 cycles with zero branch [ core id non-zero branch ]
copy 0030 00E0       // get external rom data from core rail zero
memw 0030 0001       // store external rom data to ram
add  0000 0000 0002  // rom index++
add  0001 0001 0002  // ram index++
sub  0031 0000 0004  // rom index minus copy size
cmpl 0032 0031       // set 1 if rom index < copy size
jmpc 0006 0032       // if rom index < copy size loop back
jmpu 0003            // jump to ram start if done
copy 00df 0000       // put rom index to output1 [ core id zero branch ]
copy 0030 00df       // get rom data from input1
copy 00E0 0030       // store external rom data to core rail zero
memw 0030 0001       // store external rom data to ram
nop  0000            // exact sync wait 1 cycles with zero branch
add  0000 0000 0002  // rom index++
add  0001 0001 0002  // ram index++
sub  0031 0000 0004  // rom index minus copy size
cmpl 0032 0031       // set 1 if rom index < copy size
jmpc 0005 0032       // if rom index < copy size loop back
jmpu 0003            // jump to ram start if done