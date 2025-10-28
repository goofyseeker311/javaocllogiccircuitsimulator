// init variables
ldi  0000 00000000   // rom read index 0x00000
ldi  0001 00020000   // ram write index 0x20000
ldi  0002 00000001   // constant 0x1
ldi  0003 00020000   // jump address 0x20000
ldi  0004 00000100   // rom to ram copy size
ldi  0005 00000017   // zero branch jump address
ldi  0006 00000011   // non-zero branch jump address
ldi  0007 0000FFFF   // 16-bit and filter

// core id zero check
copy 0010 00dc       // get current core id
and  0011 0010 0007  // get core id last 16-bit
cmpe 0012 0011       // set 1 if core id is zero
jmpc 0005 0012       // jump to core zero code

// core id non-zero branch



jmpu 0003

// core id zero branch
copy 00df 0000       // put rom index to output1
copy 0020 00df       // get rom data from input1
memw 0020 0001       // store ext rom data to ram 
add  0000 0000 0002  // rom index++
add  0001 0001 0002  // ram index++
sub  0021 0000 0004  // rom index minus copy size
cmpl 0022 0021       // set 1 if rom index < copy size
jmpc 0005 0022       // if rom index < copy size loop back
jmpu 0003            // jump to ram start if done