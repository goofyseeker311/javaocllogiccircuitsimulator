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
#define cNEGi 10
#define cSUMi 11
#define cSUBi 12
#define cMULi 13
#define cDIVi 14
#define cCOS 15
#define cSIN 16
#define cTAN 17
#define cACOS 18
#define cASIN 19
#define cATAN 20
#define cLOG 21
#define cEXP 22
#define cPOW 23
#define cSQRT 24
#define cNROOT 25
#define cZERO 26
#define cITOF 27
#define cFTOI 28
#define cMGET 29
#define cMSTO 30
#define cIFBUF 31
#define cNEG 32
#define cSUM 33
#define cSUB 34
#define cMUL 35
#define cDIV 36

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
	} else if (oper==cNEGi) {
		newvalues[sto3] = -oldvalues[arg1];
	} else if (oper==cSUMi) {
		newvalues[sto3] = oldvalues[arg1] + oldvalues[arg2];
	} else if (oper==cSUBi) {
		newvalues[sto3] = oldvalues[arg1] - oldvalues[arg2];
	} else if (oper==cMULi) {
		newvalues[sto3] = oldvalues[arg1] * oldvalues[arg2];
	} else if (oper==cDIVi) {
		newvalues[sto3] = oldvalues[arg1] / oldvalues[arg2];
	} else if (oper==cCOS) {
		newvalues[sto3] = as_int(cos(as_float(oldvalues[arg1])));
	} else if (oper==cSIN) {
		newvalues[sto3] = as_int(sin(as_float(oldvalues[arg1])));
	} else if (oper==cTAN) {
		newvalues[sto3] = as_int(tan(as_float(oldvalues[arg1])));
	} else if (oper==cACOS) {
		newvalues[sto3] = as_int(acos(as_float(oldvalues[arg1])));
	} else if (oper==cASIN) {
		newvalues[sto3] = as_int(asin(as_float(oldvalues[arg1])));
	} else if (oper==cATAN) {
		newvalues[sto3] = as_int(atan(as_float(oldvalues[arg1])));
	} else if (oper==cLOG) {
		newvalues[sto3] = as_int(log(as_float(oldvalues[arg1])));
	} else if (oper==cEXP) {
		newvalues[sto3] = as_int(exp(as_float(oldvalues[arg1])));
	} else if (oper==cPOW) {
		newvalues[sto3] = as_int(pow(as_float(oldvalues[arg1]), as_float(oldvalues[arg2])));
	} else if (oper==cSQRT) {
		newvalues[sto3] = as_int(sqrt(as_float(oldvalues[arg1])));
	} else if (oper==cNROOT) {
		newvalues[sto3] = as_int(rootn(as_float(oldvalues[arg1]), as_float(oldvalues[arg2])));
	} else if (oper==cZERO) {
		newvalues[sto3] = 0;
	} else if (oper==cITOF) {
		newvalues[sto3] = as_int((float)oldvalues[arg1]);
	} else if (oper==cFTOI) {
		newvalues[sto3] = (int)as_float(oldvalues[arg1]);
	} else if (oper==cMGET) {
		newvalues[sto3] = oldvalues[oldvalues[arg1]];
	} else if (oper==cMSTO) {
		newvalues[sto3] = oldvalues[arg1];
		oldvalues[oldvalues[arg2]] = oldvalues[arg1];
	} else if (oper==cIFBUF) {
		newvalues[sto3] = oldvalues[arg1];
	} else if (oper==cNEG) {
		newvalues[sto3] = as_int(-as_float(oldvalues[arg1]));
	} else if (oper==cSUM) {
		newvalues[sto3] = as_int(as_float(oldvalues[arg1]) + as_float(oldvalues[arg2]));
	} else if (oper==cSUB) {
		newvalues[sto3] = as_int(as_float(oldvalues[arg1]) - as_float(oldvalues[arg2]));
	} else if (oper==cMUL) {
		newvalues[sto3] = as_int(as_float(oldvalues[arg1]) * as_float(oldvalues[arg2]));
	} else if (oper==cDIV) {
		newvalues[sto3] = as_int(as_float(oldvalues[arg1]) / as_float(oldvalues[arg2]));
	}
}
