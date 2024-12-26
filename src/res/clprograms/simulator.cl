kernel void processgates(global float *c) {
	unsigned int xid = get_global_id(0);
	int bit = xid*3; int bitint = bit/32; int bitind = bit-bitint*32;
	int bit2 = xid*3+1; int bitint2 = bit2/32; int bitind2 = bit2-bitint2*32;
	int bit3 = xid*3+2; int bitint3 = bit3/32; int bitind3 = bit3-bitint3*32;
}
