#define cs 4

kernel void processgates(global int *circuit, global int *oldvalues, global int *newvalues) {
	unsigned int gid = get_global_id(0);
	int arg1 = circuit[gid*cs+0];
	int oper = circuit[gid*cs+1];
	int arg2 = circuit[gid*cs+2];
	int sto3 = circuit[gid*cs+3];
	printf("gid[%i]: %i %i %i %i\n",gid,arg1,oper,arg2,sto3);
}
