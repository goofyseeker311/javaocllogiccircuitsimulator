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
					int insT = 0x30;
					if (codelinestr.startsWith("ldi32")) {
						insT = 0x40;
					} else if (codelinestr.startsWith("ldi16")) {
						insT = 0x50;
					} else if (codelinestr.startsWith("ldi8")) {
						insT = 0x60;
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
					} else if (codelineparts[0].equals("memr")) {
						insT = 0x70;
					} else if (codelineparts[0].equals("memw")) {
						insT = 0x80;
					} else if (codelineparts[0].equals("cmpez")) {
						insT = 0x01;
					} else if (codelineparts[0].equals("cmplz")) {
						insT = 0x11;
					} else if (codelineparts[0].equals("fcmpez")) {
						insT = 0x21;
					} else if (codelineparts[0].equals("fcmplz")) {
						insT = 0x31;
					} else if (codelineparts[0].equals("cmpe")) {
						insT = 0x41;
					} else if (codelineparts[0].equals("cmpl")) {
						insT = 0x51;
					} else if (codelineparts[0].equals("fcmpe")) {
						insT = 0x61;
					} else if (codelineparts[0].equals("fcmpl")) {
						insT = 0x71;
					} else if (codelineparts[0].equals("shl")) {
						insT = 0x02;
					} else if (codelineparts[0].equals("shr")) {
						insT = 0x12;
					} else if (codelineparts[0].equals("shar")) {
						insT = 0x22;
					} else if (codelineparts[0].equals("rotl")) {
						insT = 0x32;
					} else if (codelineparts[0].equals("rotr")) {
						insT = 0x42;
					} else if (codelineparts[0].equals("copy")) {
						insT = 0x52;
					} else if (codelineparts[0].equals("not")) {
						insT = 0x62;
					} else if (codelineparts[0].equals("or")) {
						insT = 0x72;
					} else if (codelineparts[0].equals("and")) {
						insT = 0x82;
					} else if (codelineparts[0].equals("nand")) {
						insT = 0x92;
					} else if (codelineparts[0].equals("nor")) {
						insT = 0xA2;
					} else if (codelineparts[0].equals("xor")) {
						insT = 0xB2;
					} else if (codelineparts[0].equals("xnor")) {
						insT = 0xC2;
					} else if (codelineparts[0].equals("copyc")) {
						insT = 0xD2;
					} else if (codelineparts[0].equals("lone")) {
						insT = 0x03;
					} else if (codelineparts[0].equals("hone")) {
						insT = 0x13;
					} else if (codelineparts[0].equals("lzero")) {
						insT = 0x23;
					} else if (codelineparts[0].equals("hzero")) {
						insT = 0x33;
					} else if (codelineparts[0].equals("ones")) {
						insT = 0x43;
					} else if (codelineparts[0].equals("add")) {
						insT = 0x04;
					} else if (codelineparts[0].equals("addo")) {
						insT = 0x14;
					} else if (codelineparts[0].equals("sub")) {
						insT = 0x24;
					} else if (codelineparts[0].equals("subb")) {
						insT = 0x34;
					} else if (codelineparts[0].equals("mul")) {
						insT = 0x44;
					} else if (codelineparts[0].equals("mulo")) {
						insT = 0x54;
					} else if (codelineparts[0].equals("div")) {
						insT = 0x64;
					} else if (codelineparts[0].equals("divr")) {
						insT = 0x74;
					} else if (codelineparts[0].equals("neg")) {
						insT = 0x84;
					} else if (codelineparts[0].equals("fadd")) {
						insT = 0x05;
					} else if (codelineparts[0].equals("fsub")) {
						insT = 0x15;
					} else if (codelineparts[0].equals("fmul")) {
						insT = 0x25;
					} else if (codelineparts[0].equals("fdiv")) {
						insT = 0x35;
					} else if (codelineparts[0].equals("fneg")) {
						insT = 0x45;
					} else if (codelineparts[0].equals("fitf")) {
						insT = 0x55;
					} else if (codelineparts[0].equals("ftin")) {
						insT = 0x65;
					} else if (codelineparts[0].equals("ftid")) {
						insT = 0x75;
					} else if (codelineparts[0].equals("ftiu")) {
						insT = 0x85;
					} else if (codelineparts[0].equals("ftit")) {
						insT = 0x95;
					} else if (codelineparts[0].equals("finf")) {
						insT = 0xA5;
					} else if (codelineparts[0].equals("fnan")) {
						insT = 0xB5;
					} else if (codelineparts[0].equals("fsin")) {
						insT = 0x06;
					} else if (codelineparts[0].equals("ftan")) {
						insT = 0x16;
					} else if (codelineparts[0].equals("fcos")) {
						insT = 0x26;
					} else if (codelineparts[0].equals("fasin")) {
						insT = 0x36;
					} else if (codelineparts[0].equals("fatan")) {
						insT = 0x46;
					} else if (codelineparts[0].equals("facos")) {
						insT = 0x56;
					} else if (codelineparts[0].equals("flog")) {
						insT = 0x66;
					} else if (codelineparts[0].equals("fpow")) {
						insT = 0x76;
					} else if (codelineparts[0].equals("fsqrt")) {
						insT = 0x86;
					} else if (codelineparts[0].equals("clk")) {
						insT = 0x07;
					} else if (codelineparts[0].equals("rnd")) {
						insT = 0x17;
					} else if (codelineparts[0].equals("freq")) {
						insT = 0x27;
					} else if (codelineparts[0].equals("core")) {
						insT = 0x37;
					} else if (codelineparts[0].equals("time")) {
						insT = 0x47;
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
