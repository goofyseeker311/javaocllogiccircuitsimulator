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
#define cNULL 26
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
	int oper = circuit[gid*cs+0];
	int arg1 = circuit[gid*cs+1];
	int arg2 = circuit[gid*cs+2];
	int sto3 = circuit[gid*cs+3];

	switch(oper) {
		case cBUF:
			newvalues[sto3] = oldvalues[arg1];
		break;
		case cNOT:
			newvalues[sto3] = ~oldvalues[arg1];
		break;
		case cAND:
			newvalues[sto3] = oldvalues[arg1] & oldvalues[arg2];
		break;
		case cOR:
			newvalues[sto3] = oldvalues[arg1] | oldvalues[arg2];
		break;
		case cXOR:
			newvalues[sto3] = oldvalues[arg1] ^ oldvalues[arg2];
		break;
		case cNAND:
			newvalues[sto3] = ~(oldvalues[arg1] & oldvalues[arg2]);
		break;
		case cNOR:
			newvalues[sto3] = ~(oldvalues[arg1] | oldvalues[arg2]);
		break;
		case cXNOR:
			newvalues[sto3] = ~(oldvalues[arg1] ^ oldvalues[arg2]);
		break;
		case cSHL:
			newvalues[sto3] = oldvalues[arg1] << oldvalues[arg2];
		break;
		case cSHR:
			newvalues[sto3] = oldvalues[arg1] >> oldvalues[arg2];
		break;
		case cNEGi:
			newvalues[sto3] = -oldvalues[arg1];
		break;
		case cSUMi:
			newvalues[sto3] = oldvalues[arg1] + oldvalues[arg2];
		break;
		case cSUBi:
			newvalues[sto3] = oldvalues[arg1] - oldvalues[arg2];
		break;
		case cMULi:
			newvalues[sto3] = oldvalues[arg1] * oldvalues[arg2];
		break;
		case cDIVi:
			newvalues[sto3] = oldvalues[arg1] / oldvalues[arg2];
		break;
		case cCOS:
			newvalues[sto3] = as_int(cos(as_float(oldvalues[arg1])));
		break;
		case cSIN:
			newvalues[sto3] = as_int(sin(as_float(oldvalues[arg1])));
		break;
		case cTAN:
			newvalues[sto3] = as_int(tan(as_float(oldvalues[arg1])));
		break;
		case cACOS:
			newvalues[sto3] = as_int(acos(as_float(oldvalues[arg1])));
		break;
		case cASIN:
			newvalues[sto3] = as_int(asin(as_float(oldvalues[arg1])));
		break;
		case cATAN:
			newvalues[sto3] = as_int(atan(as_float(oldvalues[arg1])));
		break;
		case cLOG:
			newvalues[sto3] = as_int(log(as_float(oldvalues[arg1])));
		break;
		case cEXP:
			newvalues[sto3] = as_int(exp(as_float(oldvalues[arg1])));
		break;
		case cPOW:
			newvalues[sto3] = as_int(pow(as_float(oldvalues[arg1]), as_float(oldvalues[arg2])));
		break;
		case cSQRT:
			newvalues[sto3] = as_int(sqrt(as_float(oldvalues[arg1])));
		break;
		case cNROOT:
			newvalues[sto3] = as_int(rootn(as_float(oldvalues[arg1]), as_float(oldvalues[arg2])));
		break;
		case cNULL:
			newvalues[sto3] = NULL;
		break;
		case cITOF:
			newvalues[sto3] = as_int((float)oldvalues[arg1]);
		break;
		case cFTOI:
			newvalues[sto3] = (int)as_float(oldvalues[arg1]);
		break;
		case cMGET:
			newvalues[sto3] = oldvalues[oldvalues[arg1]];
		break;
		case cMSTO:
			newvalues[sto3] = oldvalues[arg1];
			oldvalues[oldvalues[arg2]] = oldvalues[arg1];
		break;
		case cIFBUF:
			if (oldvalues[arg2]==1) {
				newvalues[sto3] = oldvalues[arg1];
			} else {
				newvalues[sto3] = NULL;
			}
		break;
		case cNEG:
			newvalues[sto3] = as_int(-as_float(oldvalues[arg1]));
		break;
		case cSUM:
			newvalues[sto3] = as_int(as_float(oldvalues[arg1]) + as_float(oldvalues[arg2]));
		break;
		case cSUB:
			newvalues[sto3] = as_int(as_float(oldvalues[arg1]) - as_float(oldvalues[arg2]));
		break;
		case cMUL:
			newvalues[sto3] = as_int(as_float(oldvalues[arg1]) * as_float(oldvalues[arg2]));
		break;
		case cDIV:
			newvalues[sto3] = as_int(as_float(oldvalues[arg1]) / as_float(oldvalues[arg2]));
		break;
		default:
		break;
	}
}
