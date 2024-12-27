#define cs 4
#define cBUF 0
#define cNOT 1
#define cAND 2
#define cOR 3
#define cXOR 4
#define cNAND 5
#define cNOR 6
#define cXNOR 7
#define cSHL 8
#define cSHR 9
#define cNEG 10
#define cSUM 11
#define cMUL 12
#define cDIV 13
#define cCOS 14
#define cSIN 15
#define cTAN 16
#define cACOS 17
#define cASIN 18
#define cATAN 19
#define cLOG 20
#define cEXP 21
#define cPOW 22
#define cSQRT 23
#define cNROOT 24
#define cZERO 25
#define cITOF 26
#define cFTOI 27
#define cMGET 28
#define cMSTO 29
#define cIFBUF 30

kernel void updatevalues(global int *oldvalues, global int *newvalues) {
	unsigned int vid = get_global_id(0);
	oldvalues[vid] = newvalues[vid];
}

kernel void processgates(global int *circuit, global int *oldvalues, global int *newvalues) {
	unsigned int gid = get_global_id(0);
	int arg1 = circuit[gid*cs+0];
	int oper = circuit[gid*cs+1];
	int arg2 = circuit[gid*cs+2];
	int sto3 = circuit[gid*cs+3];

	if (oper==cBUF) {
		newvalues[sto3] = oldvalues[arg1];
	} else if (oper==cNOT) {
		newvalues[sto3] = ~oldvalues[arg1];
	} else if (oper==cAND) {
		newvalues[sto3] = oldvalues[arg1] & oldvalues[arg2];
	} else if (oper==cOR) {
		newvalues[sto3] = oldvalues[arg1] | oldvalues[arg2];
	} else if (oper==cXOR) {
		newvalues[sto3] = oldvalues[arg1] ^ oldvalues[arg2];
	} else if (oper==cNAND) {
		newvalues[sto3] = ~(oldvalues[arg1] & oldvalues[arg2]);
	} else if (oper==cNOR) {
		newvalues[sto3] = ~(oldvalues[arg1] | oldvalues[arg2]);
	} else if (oper==cXNOR) {
		newvalues[sto3] = ~(oldvalues[arg1] ^ oldvalues[arg2]);
	} else if (oper==cSHL) {
		newvalues[sto3] = oldvalues[arg1] << oldvalues[arg2];
	} else if (oper==cSHR) {
		newvalues[sto3] = oldvalues[arg1] >> oldvalues[arg2];
	}
	printf("gid[%i]: %i %i %i %i, %i %i => %i\n",gid,arg1,oper,arg2,sto3,oldvalues[arg1],oldvalues[arg2],newvalues[sto3]);
}
