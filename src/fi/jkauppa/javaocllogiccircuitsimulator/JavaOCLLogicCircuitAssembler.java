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
			int linenumber = 0;
			String readline = null;
			while((readline=filereader.readLine())!=null) {
				long insval = compileline(readline);
				ByteBuffer insvalbytes = ByteBuffer.allocate(8);
				insvalbytes.putLong(insval).rewind();
				insvalbytes.get(outputbytes, 0, 8);
				fileoutput.write(outputbytes);
				System.out.println("readline("+linenumber+"): "+readline+", insval: "+Long.toHexString(insval));
				linenumber++;
			}
			filereader.close();
			fileoutput.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public long compileline(String codeline) {
		long insvalue = Long.parseUnsignedLong("A123456789ABCDEF", 16);
		return insvalue;
	}
}
