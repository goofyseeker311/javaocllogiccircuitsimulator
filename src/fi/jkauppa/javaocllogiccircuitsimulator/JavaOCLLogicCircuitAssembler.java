package fi.jkauppa.javaocllogiccircuitsimulator;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;

public class JavaOCLLogicCircuitAssembler {

	public static void main(String[] arg) {
		System.out.println("init.");
		if (arg.length>1) {
			String filenamein = arg[0];
			String filenameout = arg[1];
			JavaOCLLogicCircuitAssembler compiler = new JavaOCLLogicCircuitAssembler();
			compiler.parse(filenamein, filenameout);
		} else {
			System.out.println("arguments expected: program.asm program.bin");
		}
		System.out.println("exit.");
	}
	
	public void parse(String filein, String fileout) {
		File inputfile = new File(filein);
		File outputfile = new File(fileout);
		long filesize = inputfile.length();
		System.out.println("filein: "+filein+", fileout: "+fileout+", size: "+filesize+" bytes");
		try {
			BufferedReader filereader = new BufferedReader(new FileReader(inputfile));
			BufferedOutputStream fileoutput = new BufferedOutputStream(new FileOutputStream(outputfile));
			byte[] outputbytes = new byte[8];
			ByteBuffer insvalbytes = ByteBuffer.allocate(8);
			String commentline = null;
			int linenumber = 0;
			String splitregex = "\\s+|,";
			String readline = null;
			while((readline=filereader.readLine())!=null) {
				String codeline = readline.trim();
				commentline = "";
				if (codeline.length()==0) {
					insvalbytes.clear();
					insvalbytes.putLong(0L);
					insvalbytes.rewind();
					insvalbytes.get(outputbytes, 0, 8);
					fileoutput.write(outputbytes);
					insvalbytes.rewind();
					long insval = insvalbytes.getLong();
					System.out.println("output: "+String.format("%016x", insval)+", readline("+linenumber+"): '"+readline+"', nothing");
				} else if (codeline.startsWith("//")) {
					insvalbytes.clear();
					insvalbytes.putLong(0L);
					insvalbytes.rewind();
					insvalbytes.get(outputbytes, 0, 8);
					fileoutput.write(outputbytes);
					insvalbytes.rewind();
					long insval = insvalbytes.getLong();
					System.out.println("output: "+String.format("%016x", insval)+", readline("+linenumber+"): '"+readline+"', comment");
				} else if (codeline.startsWith("##")) {
					codeline = codeline.substring(2).trim();
					String[] codelineparts = codeline.split(splitregex);
					String dataline = codelineparts[0];
					long dataval = Long.parseUnsignedLong(dataline, 16);
					insvalbytes.clear();
					insvalbytes.putLong(dataval);
					insvalbytes.rewind();
					insvalbytes.get(outputbytes, 0, 8);
					fileoutput.write(outputbytes);
					insvalbytes.rewind();
					long insval = insvalbytes.getLong();
					System.out.println("output: "+String.format("%016x", insval)+", readline("+linenumber+"): '"+readline+"', data");
				} else if (codeline.startsWith("nop")) {
					codeline = codeline.substring(3).trim();
					String[] codelineparts = codeline.split(splitregex);
					String dataline = codelineparts[0];
					int regX = 0;
					long dataval = Long.parseUnsignedLong(dataline, 16);
					int insT = 0x00;
					insvalbytes.clear();
					insvalbytes.putShort((short)regX);
					insvalbytes.putInt((int)dataval);
					insvalbytes.putShort((short)insT);
					insvalbytes.rewind();
					insvalbytes.get(outputbytes, 0, 8);
					fileoutput.write(outputbytes);
					insvalbytes.rewind();
					long insval = insvalbytes.getLong();
					System.out.println("output: "+String.format("%016x", insval)+", readline("+linenumber+"): '"+readline+"', sleep");
				} else if (codeline.startsWith("ldi")) {
					String codelinestr = codeline;
					String[] codelineparts = codeline.split(splitregex);
					codeline = codelineparts[0];
					String regXline = codelineparts[1];
					String dataline = codelineparts[2];
					String vecnline = "0";
					if (codelineparts.length>=4) {
						vecnline = codelineparts[3];
					}
					int regX = Integer.parseUnsignedInt(regXline, 16);
					long dataval = Long.parseUnsignedLong(dataline, 16);
					int vecN = Integer.parseUnsignedInt(vecnline, 16);
					int insT = 0x60;
					if (codelinestr.startsWith("ldi32")) {
						insT = 0x70;
					} else if (codelinestr.startsWith("ldi16")) {
						insT = 0x80;
					} else if (codelinestr.startsWith("ldi8")) {
						insT = 0x90;
					}
					insvalbytes.clear();
					insvalbytes.putShort((short)regX);
					insvalbytes.putInt((int)dataval);
					insvalbytes.put((byte)vecN);
					insvalbytes.put((byte)insT);
					insvalbytes.rewind();
					insvalbytes.get(outputbytes, 0, 8);
					fileoutput.write(outputbytes);
					insvalbytes.rewind();
					long insval = insvalbytes.getLong();
					System.out.println("output: "+String.format("%016x", insval)+", readline("+linenumber+"): '"+readline+"', immediate");
				} else {
					int commentind = codeline.indexOf("//");
					if (commentind>-1) {
						commentline = codeline.substring(commentind).trim();
						codeline = codeline.substring(0,commentind).trim();
					}
					
					int regX = 0;
					int regY = 0;
					int regZ = 0;
					int vecN = 0;
					int insT = 0;
					String[] codelineparts = codeline.split(splitregex);
					if (codelineparts[0].equals("jmp")) {
						insT = 0x10;
					} else if (codelineparts[0].equals("jmpc")) {
						insT = 0x20;
					} else if (codelineparts[0].equals("jmpc32")) {
						insT = 0x30;
					} else if (codelineparts[0].equals("jmpc16")) {
						insT = 0x40;
					} else if (codelineparts[0].equals("jmpc8")) {
						insT = 0x50;
					} else if (codelineparts[0].equals("memr")) {
						insT = 0xa0;
					} else if (codelineparts[0].equals("memw")) {
						insT = 0xb0;
						
					} else if (codelineparts[0].equals("cmpez")) {
						insT = 0x01;
					} else if (codelineparts[0].equals("cmplz")) {
						insT = 0x11;
					} else if (codelineparts[0].equals("fcmpez")) {
						insT = 0x21;
					} else if (codelineparts[0].equals("fcmplz")) {
						insT = 0x31;
					} else if (codelineparts[0].equals("finf")) {
						insT = 0x41;
					} else if (codelineparts[0].equals("cmpe")) {
						insT = 0x51;
					} else if (codelineparts[0].equals("cmpl")) {
						insT = 0x61;
					} else if (codelineparts[0].equals("fcmpe")) {
						insT = 0x71;
					} else if (codelineparts[0].equals("fcmpl")) {
						insT = 0x81;
					} else if (codelineparts[0].equals("fnan")) {
						insT = 0x91;
					} else if (codelineparts[0].equals("shl")) {
						insT = 0xa1;
					} else if (codelineparts[0].equals("shr")) {
						insT = 0xb1;
					} else if (codelineparts[0].equals("shar")) {
						insT = 0xc1;
					} else if (codelineparts[0].equals("rotl")) {
						insT = 0xd1;
					} else if (codelineparts[0].equals("rotr")) {
						insT = 0xe1;
						
					} else if (codelineparts[0].equals("copy")) {
						insT = 0x02;
					} else if (codelineparts[0].equals("not")) {
						insT = 0x12;
					} else if (codelineparts[0].equals("or")) {
						insT = 0x22;
					} else if (codelineparts[0].equals("and")) {
						insT = 0x32;
					} else if (codelineparts[0].equals("nand")) {
						insT = 0x42;
					} else if (codelineparts[0].equals("nor")) {
						insT = 0x52;
					} else if (codelineparts[0].equals("xor")) {
						insT = 0x62;
					} else if (codelineparts[0].equals("xnor")) {
						insT = 0x72;
					} else if (codelineparts[0].equals("i16i8")) {
						insT = 0x82;
					} else if (codelineparts[0].equals("i8i16")) {
						insT = 0x92;
					} else if (codelineparts[0].equals("clk")) {
						insT = 0xa2;
					} else if (codelineparts[0].equals("rnd")) {
						insT = 0xb2;
					} else if (codelineparts[0].equals("freq")) {
						insT = 0xc2;
					} else if (codelineparts[0].equals("core")) {
						insT = 0xd2;
					} else if (codelineparts[0].equals("time")) {
						insT = 0xe2;
						
					} else if (codelineparts[0].equals("add")) {
						insT = 0x03;
					} else if (codelineparts[0].equals("sub")) {
						insT = 0x13;
					} else if (codelineparts[0].equals("mul")) {
						insT = 0x23;
					} else if (codelineparts[0].equals("div")) {
						insT = 0x33;
					} else if (codelineparts[0].equals("neg")) {
						insT = 0x43;
					} else if (codelineparts[0].equals("addo")) {
						insT = 0x53;
					} else if (codelineparts[0].equals("subb")) {
						insT = 0x63;
					} else if (codelineparts[0].equals("mulo")) {
						insT = 0x73;
					} else if (codelineparts[0].equals("divr")) {
						insT = 0x83;
					} else if (codelineparts[0].equals("copyc")) {
						insT = 0x93;
					} else if (codelineparts[0].equals("lone")) {
						insT = 0xa3;
					} else if (codelineparts[0].equals("hone")) {
						insT = 0xb3;
					} else if (codelineparts[0].equals("lzero")) {
						insT = 0xc3;
					} else if (codelineparts[0].equals("hzero")) {
						insT = 0xd3;
					} else if (codelineparts[0].equals("ones")) {
						insT = 0xe3;
					} else if (codelineparts[0].equals("fexp")) {
						insT = 0xf3;
						
					} else if (codelineparts[0].equals("fadd")) {
						insT = 0x04;
					} else if (codelineparts[0].equals("fsub")) {
						insT = 0x14;
					} else if (codelineparts[0].equals("fmul")) {
						insT = 0x24;
					} else if (codelineparts[0].equals("fdiv")) {
						insT = 0x34;
					} else if (codelineparts[0].equals("fneg")) {
						insT = 0x44;
					} else if (codelineparts[0].equals("fsin")) {
						insT = 0x54;
					} else if (codelineparts[0].equals("ftan")) {
						insT = 0x64;
					} else if (codelineparts[0].equals("fcos")) {
						insT = 0x74;
					} else if (codelineparts[0].equals("flog")) {
						insT = 0x84;
					} else if (codelineparts[0].equals("fpow")) {
						insT = 0x94;
					} else if (codelineparts[0].equals("fasin")) {
						insT = 0xa4;
					} else if (codelineparts[0].equals("fatan")) {
						insT = 0xb4;
					} else if (codelineparts[0].equals("facos")) {
						insT = 0xc4;
					} else if (codelineparts[0].equals("fsqrt")) {
						insT = 0xd4;
					} else if (codelineparts[0].equals("fmin")) {
						insT = 0xe4;
					} else if (codelineparts[0].equals("fmax")) {
						insT = 0xf4;
						
					} else if (codelineparts[0].equals("ff32")) {
						insT = 0x05;
					} else if (codelineparts[0].equals("f32f16")) {
						insT = 0x15;
					} else if (codelineparts[0].equals("f16f8")) {
						insT = 0x25;
					} else if (codelineparts[0].equals("ii32")) {
						insT = 0x35;
					} else if (codelineparts[0].equals("i32i16")) {
						insT = 0x45;
					} else if (codelineparts[0].equals("f8f16")) {
						insT = 0x55;
					} else if (codelineparts[0].equals("f16f32")) {
						insT = 0x65;
					} else if (codelineparts[0].equals("f32f")) {
						insT = 0x75;
					} else if (codelineparts[0].equals("i32i")) {
						insT = 0x85;
					} else if (codelineparts[0].equals("i16i32")) {
						insT = 0x95;
					} else if (codelineparts[0].equals("fitf")) {
						insT = 0xa5;
					} else if (codelineparts[0].equals("ftin")) {
						insT = 0xb5;
					} else if (codelineparts[0].equals("ftid")) {
						insT = 0xc5;
					} else if (codelineparts[0].equals("ftiu")) {
						insT = 0xd5;
					} else if (codelineparts[0].equals("ftit")) {
						insT = 0xe5;
					} else if (codelineparts[0].equals("fabs")) {
						insT = 0xf5;
						
					} else if (codelineparts[0].equals("cmpez32")) {
						insT = 0x06;
					} else if (codelineparts[0].equals("cmplz32")) {
						insT = 0x16;
					} else if (codelineparts[0].equals("fcmpez32")) {
						insT = 0x26;
					} else if (codelineparts[0].equals("fcmplz32")) {
						insT = 0x36;
					} else if (codelineparts[0].equals("finf32")) {
						insT = 0x46;
					} else if (codelineparts[0].equals("cmpez16")) {
						insT = 0x56;
					} else if (codelineparts[0].equals("cmplz16")) {
						insT = 0x66;
					} else if (codelineparts[0].equals("fcmpez16")) {
						insT = 0x76;
					} else if (codelineparts[0].equals("fcmplz16")) {
						insT = 0x86;
					} else if (codelineparts[0].equals("finf16")) {
						insT = 0x96;
					} else if (codelineparts[0].equals("cmpez8")) {
						insT = 0xa6;
					} else if (codelineparts[0].equals("cmplz8")) {
						insT = 0xb6;
					} else if (codelineparts[0].equals("fcmpez8")) {
						insT = 0xc6;
					} else if (codelineparts[0].equals("fcmplz8")) {
						insT = 0xd6;
					} else if (codelineparts[0].equals("finf8")) {
						insT = 0xe6;
						
					} else if (codelineparts[0].equals("cmpe32")) {
						insT = 0x07;
					} else if (codelineparts[0].equals("cmpl32")) {
						insT = 0x17;
					} else if (codelineparts[0].equals("fcmpe32")) {
						insT = 0x27;
					} else if (codelineparts[0].equals("fcmpl32")) {
						insT = 0x37;
					} else if (codelineparts[0].equals("fnan32")) {
						insT = 0x47;
					} else if (codelineparts[0].equals("cmpe16")) {
						insT = 0x57;
					} else if (codelineparts[0].equals("cmpl16")) {
						insT = 0x67;
					} else if (codelineparts[0].equals("fcmpe16")) {
						insT = 0x77;
					} else if (codelineparts[0].equals("fcmpl16")) {
						insT = 0x87;
					} else if (codelineparts[0].equals("fnan16")) {
						insT = 0x97;
					} else if (codelineparts[0].equals("cmpe8")) {
						insT = 0xa7;
					} else if (codelineparts[0].equals("cmpl8")) {
						insT = 0xb7;
					} else if (codelineparts[0].equals("fcmpe8")) {
						insT = 0xc7;
					} else if (codelineparts[0].equals("fcmpl8")) {
						insT = 0xd7;
					} else if (codelineparts[0].equals("fnan8")) {
						insT = 0xe7;
					} else if (codelineparts[0].equals("fexp32")) {
						insT = 0xf7;

					} else if (codelineparts[0].equals("shl32")) {
						insT = 0x08;
					} else if (codelineparts[0].equals("shr32")) {
						insT = 0x18;
					} else if (codelineparts[0].equals("shar32")) {
						insT = 0x28;
					} else if (codelineparts[0].equals("rotl32")) {
						insT = 0x38;
					} else if (codelineparts[0].equals("rotr32")) {
						insT = 0x48;
					} else if (codelineparts[0].equals("shl16")) {
						insT = 0x58;
					} else if (codelineparts[0].equals("shr16")) {
						insT = 0x68;
					} else if (codelineparts[0].equals("shar16")) {
						insT = 0x78;
					} else if (codelineparts[0].equals("rotl16")) {
						insT = 0x88;
					} else if (codelineparts[0].equals("rotr16")) {
						insT = 0x98;
					} else if (codelineparts[0].equals("shl8")) {
						insT = 0xa8;
					} else if (codelineparts[0].equals("shr8")) {
						insT = 0xb8;
					} else if (codelineparts[0].equals("shar8")) {
						insT = 0xc8;
					} else if (codelineparts[0].equals("rotl8")) {
						insT = 0xd8;
					} else if (codelineparts[0].equals("rotr8")) {
						insT = 0xe8;
					} else if (codelineparts[0].equals("fexp16")) {
						insT = 0xf8;

					} else if (codelineparts[0].equals("add32")) {
						insT = 0x09;
					} else if (codelineparts[0].equals("sub32")) {
						insT = 0x19;
					} else if (codelineparts[0].equals("mul32")) {
						insT = 0x29;
					} else if (codelineparts[0].equals("div32")) {
						insT = 0x39;
					} else if (codelineparts[0].equals("neg32")) {
						insT = 0x49;
					} else if (codelineparts[0].equals("add16")) {
						insT = 0x59;
					} else if (codelineparts[0].equals("sub16")) {
						insT = 0x69;
					} else if (codelineparts[0].equals("mul16")) {
						insT = 0x79;
					} else if (codelineparts[0].equals("div16")) {
						insT = 0x89;
					} else if (codelineparts[0].equals("neg16")) {
						insT = 0x99;
					} else if (codelineparts[0].equals("add8")) {
						insT = 0xa9;
					} else if (codelineparts[0].equals("sub8")) {
						insT = 0xb9;
					} else if (codelineparts[0].equals("mul8")) {
						insT = 0xc9;
					} else if (codelineparts[0].equals("div8")) {
						insT = 0xd9;
					} else if (codelineparts[0].equals("neg8")) {
						insT = 0xe9;
					} else if (codelineparts[0].equals("fexp8")) {
						insT = 0xf9;
						
					} else if (codelineparts[0].equals("addo32")) {
						insT = 0x0a;
					} else if (codelineparts[0].equals("subb32")) {
						insT = 0x1a;
					} else if (codelineparts[0].equals("mulo32")) {
						insT = 0x2a;
					} else if (codelineparts[0].equals("divr32")) {
						insT = 0x3a;
					} else if (codelineparts[0].equals("copyc32")) {
						insT = 0x4a;
					} else if (codelineparts[0].equals("addo16")) {
						insT = 0x5a;
					} else if (codelineparts[0].equals("subb16")) {
						insT = 0x6a;
					} else if (codelineparts[0].equals("mulo16")) {
						insT = 0x7a;
					} else if (codelineparts[0].equals("divr16")) {
						insT = 0x8a;
					} else if (codelineparts[0].equals("copyc16")) {
						insT = 0x9a;
					} else if (codelineparts[0].equals("addo8")) {
						insT = 0xaa;
					} else if (codelineparts[0].equals("subb8")) {
						insT = 0xba;
					} else if (codelineparts[0].equals("mulo8")) {
						insT = 0xca;
					} else if (codelineparts[0].equals("divr8")) {
						insT = 0xda;
					} else if (codelineparts[0].equals("copyc8")) {
						insT = 0xea;
					} else if (codelineparts[0].equals("fmin32")) {
						insT = 0xfa;
						
					} else if (codelineparts[0].equals("lone32")) {
						insT = 0x0b;
					} else if (codelineparts[0].equals("hone32")) {
						insT = 0x1b;
					} else if (codelineparts[0].equals("lzero32")) {
						insT = 0x2b;
					} else if (codelineparts[0].equals("hzero32")) {
						insT = 0x3b;
					} else if (codelineparts[0].equals("ones32")) {
						insT = 0x4b;
					} else if (codelineparts[0].equals("lone16")) {
						insT = 0x5b;
					} else if (codelineparts[0].equals("hone16")) {
						insT = 0x6b;
					} else if (codelineparts[0].equals("lzero16")) {
						insT = 0x7b;
					} else if (codelineparts[0].equals("hzero16")) {
						insT = 0x8b;
					} else if (codelineparts[0].equals("ones16")) {
						insT = 0x9b;
					} else if (codelineparts[0].equals("lone8")) {
						insT = 0xab;
					} else if (codelineparts[0].equals("hone8")) {
						insT = 0xbb;
					} else if (codelineparts[0].equals("lzero8")) {
						insT = 0xcb;
					} else if (codelineparts[0].equals("hzero8")) {
						insT = 0xdb;
					} else if (codelineparts[0].equals("ones8")) {
						insT = 0xeb;
					} else if (codelineparts[0].equals("fmax32")) {
						insT = 0xfb;
						
					} else if (codelineparts[0].equals("fadd32")) {
						insT = 0x0c;
					} else if (codelineparts[0].equals("fsub32")) {
						insT = 0x1c;
					} else if (codelineparts[0].equals("fmul32")) {
						insT = 0x2c;
					} else if (codelineparts[0].equals("fdiv32")) {
						insT = 0x3c;
					} else if (codelineparts[0].equals("fneg32")) {
						insT = 0x4c;
					} else if (codelineparts[0].equals("fadd16")) {
						insT = 0x5c;
					} else if (codelineparts[0].equals("fsub16")) {
						insT = 0x6c;
					} else if (codelineparts[0].equals("fmul16")) {
						insT = 0x7c;
					} else if (codelineparts[0].equals("fdiv16")) {
						insT = 0x8c;
					} else if (codelineparts[0].equals("fneg16")) {
						insT = 0x9c;
					} else if (codelineparts[0].equals("fadd8")) {
						insT = 0xac;
					} else if (codelineparts[0].equals("fsub8")) {
						insT = 0xbc;
					} else if (codelineparts[0].equals("fmul8")) {
						insT = 0xcc;
					} else if (codelineparts[0].equals("fdiv8")) {
						insT = 0xdc;
					} else if (codelineparts[0].equals("fneg8")) {
						insT = 0xec;
					} else if (codelineparts[0].equals("fmin16")) {
						insT = 0xfc;
						
					} else if (codelineparts[0].equals("fsin32")) {
						insT = 0x0d;
					} else if (codelineparts[0].equals("ftan32")) {
						insT = 0x1d;
					} else if (codelineparts[0].equals("fcos32")) {
						insT = 0x2d;
					} else if (codelineparts[0].equals("flog32")) {
						insT = 0x3d;
					} else if (codelineparts[0].equals("fpow32")) {
						insT = 0x4d;
					} else if (codelineparts[0].equals("fsin16")) {
						insT = 0x5d;
					} else if (codelineparts[0].equals("ftan16")) {
						insT = 0x6d;
					} else if (codelineparts[0].equals("fcos16")) {
						insT = 0x7d;
					} else if (codelineparts[0].equals("flog16")) {
						insT = 0x8d;
					} else if (codelineparts[0].equals("fpow16")) {
						insT = 0x9d;
					} else if (codelineparts[0].equals("fsin8")) {
						insT = 0xad;
					} else if (codelineparts[0].equals("ftan8")) {
						insT = 0xbd;
					} else if (codelineparts[0].equals("fcos8")) {
						insT = 0xcd;
					} else if (codelineparts[0].equals("flog8")) {
						insT = 0xdd;
					} else if (codelineparts[0].equals("fpow8")) {
						insT = 0xed;
					} else if (codelineparts[0].equals("fmax16")) {
						insT = 0xfd;
						
					} else if (codelineparts[0].equals("fasin32")) {
						insT = 0x0e;
					} else if (codelineparts[0].equals("fatan32")) {
						insT = 0x1e;
					} else if (codelineparts[0].equals("facos32")) {
						insT = 0x2e;
					} else if (codelineparts[0].equals("fsqrt32")) {
						insT = 0x3e;
					} else if (codelineparts[0].equals("fabs32")) {
						insT = 0x4e;
					} else if (codelineparts[0].equals("fasin16")) {
						insT = 0x5e;
					} else if (codelineparts[0].equals("fatan16")) {
						insT = 0x6e;
					} else if (codelineparts[0].equals("facos16")) {
						insT = 0x7e;
					} else if (codelineparts[0].equals("fsqrt16")) {
						insT = 0x8e;
					} else if (codelineparts[0].equals("fabs16")) {
						insT = 0x9e;
					} else if (codelineparts[0].equals("fasin8")) {
						insT = 0xae;
					} else if (codelineparts[0].equals("fatan8")) {
						insT = 0xbe;
					} else if (codelineparts[0].equals("facos8")) {
						insT = 0xce;
					} else if (codelineparts[0].equals("fsqrt8")) {
						insT = 0xde;
					} else if (codelineparts[0].equals("fabs8")) {
						insT = 0xee;
					} else if (codelineparts[0].equals("fmin8")) {
						insT = 0xfe;
						
					} else if (codelineparts[0].equals("fitf32")) {
						insT = 0x0f;
					} else if (codelineparts[0].equals("ftin32")) {
						insT = 0x1f;
					} else if (codelineparts[0].equals("ftid32")) {
						insT = 0x2f;
					} else if (codelineparts[0].equals("ftiu32")) {
						insT = 0x3f;
					} else if (codelineparts[0].equals("ftit32")) {
						insT = 0x4f;
					} else if (codelineparts[0].equals("fitf16")) {
						insT = 0x5f;
					} else if (codelineparts[0].equals("ftin16")) {
						insT = 0x6f;
					} else if (codelineparts[0].equals("ftid16")) {
						insT = 0x7f;
					} else if (codelineparts[0].equals("ftiu16")) {
						insT = 0x8f;
					} else if (codelineparts[0].equals("ftit16")) {
						insT = 0x9f;
					} else if (codelineparts[0].equals("fitf8")) {
						insT = 0xaf;
					} else if (codelineparts[0].equals("ftin8")) {
						insT = 0xbf;
					} else if (codelineparts[0].equals("ftid8")) {
						insT = 0xcf;
					} else if (codelineparts[0].equals("ftiu8")) {
						insT = 0xdf;
					} else if (codelineparts[0].equals("ftit8")) {
						insT = 0xef;
					} else if (codelineparts[0].equals("fmax8")) {
						insT = 0xff;
					}
					
					if (codelineparts.length>=2) {
						regX = Integer.parseUnsignedInt(codelineparts[1], 16);
					}
					if (codelineparts.length>=3) {
						regY = Integer.parseUnsignedInt(codelineparts[2], 16);
					}
					if (codelineparts.length>=4) {
						regZ = Integer.parseUnsignedInt(codelineparts[3], 16);
					}
					if (codelineparts.length>=5) {
						vecN = Integer.parseUnsignedInt(codelineparts[4], 16);
					}
					
					insvalbytes.clear();
					insvalbytes.putShort((short)regX);
					insvalbytes.putShort((short)regY);
					insvalbytes.putShort((short)regZ);
					insvalbytes.put((byte)vecN);
					insvalbytes.put((byte)insT);
					insvalbytes.rewind();
					insvalbytes.get(outputbytes, 0, 8);
					fileoutput.write(outputbytes);
					insvalbytes.rewind();
					long insval = insvalbytes.getLong();
					System.out.println("output: "+String.format("%016x", insval)+", readline("+linenumber+"): '"+readline+"', comment: '"+commentline+"'");
				}
				linenumber++;
			}
			filereader.close();
			fileoutput.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found.");
		} catch (IOException e) {
			System.out.println("IO failed.");
		}
	}
}
