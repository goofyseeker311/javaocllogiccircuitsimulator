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
					System.out.println("output: "+String.format("%016x", insval)+", readline("+linenumber+"): '"+codeline+"', nothing");
				} else if (codeline.startsWith("//")) {
					insvalbytes.clear();
					insvalbytes.putLong(0L);
					insvalbytes.rewind();
					insvalbytes.get(outputbytes, 0, 8);
					fileoutput.write(outputbytes);
					insvalbytes.rewind();
					long insval = insvalbytes.getLong();
					System.out.println("output: "+String.format("%016x", insval)+", readline("+linenumber+"): '"+codeline+"', comment");
				} else if (codeline.startsWith("##")) {
					codeline = codeline.substring(2).trim();
					String[] codelineparts = codeline.split(" ");
					String dataline = codelineparts[0];
					long dataval = Long.parseUnsignedLong(dataline, 16);
					insvalbytes.clear();
					insvalbytes.putLong(dataval);
					insvalbytes.rewind();
					insvalbytes.get(outputbytes, 0, 8);
					fileoutput.write(outputbytes);
					insvalbytes.rewind();
					long insval = insvalbytes.getLong();
					System.out.println("output: "+String.format("%016x", insval)+", readline("+linenumber+"): '"+codeline+"', data");
				} else {
					int commentind = codeline.indexOf("//");
					if (commentind>-1) {
						commentline = codeline.substring(commentind).trim();
						codeline = codeline.substring(0,commentind).trim();
					}
					
					int regX = 0;
					int regY = 0;
					int regZ = 0;
					int bitI = 0;
					int insT = 0;
					String[] codelineparts = codeline.split(" |,");
					if (codelineparts[0].equals("nop")) {
						insT = 0x00;
					} else if (codelineparts[0].equals("jmpc")) {
						insT = 0x01;
					} else if (codelineparts[0].equals("jmpu")) {
						insT = 0x11;
					} else if (codelineparts[0].equals("ldi")) {
						insT = 0x02;
					} else if (codelineparts[0].equals("memr")) {
						insT = 0x03;
					} else if (codelineparts[0].equals("memw")) {
						insT = 0x13;
					} else if (codelineparts[0].equals("cmpe")) {
						insT = 0x04;
					} else if (codelineparts[0].equals("cmpl")) {
						insT = 0x14;
					} else if (codelineparts[0].equals("cmpef")) {
						insT = 0x24;
					} else if (codelineparts[0].equals("cmplf")) {
						insT = 0x34;
					} else if (codelineparts[0].equals("add")) {
						insT = 0x05;
					} else if (codelineparts[0].equals("addo")) {
						insT = 0x15;
					} else if (codelineparts[0].equals("sub")) {
						insT = 0x25;
					} else if (codelineparts[0].equals("subb")) {
						insT = 0x35;
					} else if (codelineparts[0].equals("mul")) {
						insT = 0x45;
					} else if (codelineparts[0].equals("mulo")) {
						insT = 0x55;
					} else if (codelineparts[0].equals("div")) {
						insT = 0x65;
					} else if (codelineparts[0].equals("divr")) {
						insT = 0x75;
					} else if (codelineparts[0].equals("neg")) {
						insT = 0x85;
					} else if (codelineparts[0].equals("shl")) {
						insT = 0x06;
					} else if (codelineparts[0].equals("shr")) {
						insT = 0x16;
					} else if (codelineparts[0].equals("shar")) {
						insT = 0x26;
					} else if (codelineparts[0].equals("rotl")) {
						insT = 0x36;
					} else if (codelineparts[0].equals("rotr")) {
						insT = 0x46;
					} else if (codelineparts[0].equals("copy")) {
						insT = 0x56;
					} else if (codelineparts[0].equals("not")) {
						insT = 0x66;
					} else if (codelineparts[0].equals("or")) {
						insT = 0x76;
					} else if (codelineparts[0].equals("and")) {
						insT = 0x86;
					} else if (codelineparts[0].equals("nand")) {
						insT = 0x96;
					} else if (codelineparts[0].equals("nor")) {
						insT = 0xA6;
					} else if (codelineparts[0].equals("xor")) {
						insT = 0xB6;
					} else if (codelineparts[0].equals("xnor")) {
						insT = 0xC6;
					} else if (codelineparts[0].equals("addf")) {
						insT = 0x07;
					} else if (codelineparts[0].equals("subf")) {
						insT = 0x17;
					} else if (codelineparts[0].equals("mulf")) {
						insT = 0x27;
					} else if (codelineparts[0].equals("divf")) {
						insT = 0x37;
					} else if (codelineparts[0].equals("negf")) {
						insT = 0x47;
					} else if (codelineparts[0].equals("itf")) {
						insT = 0x57;
					} else if (codelineparts[0].equals("ftin")) {
						insT = 0x67;
					} else if (codelineparts[0].equals("ftid")) {
						insT = 0x77;
					} else if (codelineparts[0].equals("ftiu")) {
						insT = 0x87;
					} else if (codelineparts[0].equals("ftit")) {
						insT = 0x97;
					}
					
					if (codelineparts.length>=2) {
						regX = Short.parseShort(codelineparts[1], 16);
					}
					if (codelineparts.length>=3) {
						regY = Short.parseShort(codelineparts[2], 16);
					}
					if (codelineparts.length>=4) {
						regZ = Short.parseShort(codelineparts[3], 16);
					}
					if (codelineparts.length>=5) {
						bitI = Byte.parseByte(codelineparts[4], 16);
					}
					
					insvalbytes.clear();
					insvalbytes.putShort((short)regX);
					insvalbytes.putShort((short)regY);
					insvalbytes.putShort((short)regZ);
					insvalbytes.put((byte)bitI);
					insvalbytes.put((byte)insT);
					insvalbytes.rewind();
					insvalbytes.get(outputbytes, 0, 8);
					fileoutput.write(outputbytes);
					insvalbytes.rewind();
					long insval = insvalbytes.getLong();
					System.out.println("output: "+String.format("%016x", insval)+", readline("+linenumber+"): '"+codeline+"', comment: '"+commentline+"'");
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
