kernel void processgates(global int *circuit, global int *oldvalues, global int *newvalues) {
	unsigned int gid = get_global_id(0);
	printf("gid: %i\n",gid);
}
