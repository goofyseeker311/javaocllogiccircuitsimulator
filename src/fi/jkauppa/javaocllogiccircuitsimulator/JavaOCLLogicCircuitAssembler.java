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
			long insval = 0L;
			int linenumber = 0;
			String readline = null;
			while((readline=filereader.readLine())!=null) {
				String codeline = readline.trim();
				commentline = "";
				if (codeline.length()==0) {
					System.out.println("readline("+linenumber+"): '"+readline+"', output: nothing");
				} else if (codeline.startsWith("//")) {
					System.out.println("readline("+linenumber+"): '"+readline+"', output: comment");
				} else {
					int commentind = codeline.indexOf("//");
					if (commentind>-1) {
						commentline = codeline.substring(commentind).trim();
						codeline = codeline.substring(0,commentind).trim();
					}
					
					
					insval = 0L;
					insvalbytes.clear();
					insvalbytes.putLong(insval).rewind();
					insvalbytes.get(outputbytes, 0, 8);
					fileoutput.write(outputbytes);
					System.out.println("readline("+linenumber+"): '"+codeline+"', output: "+Long.toHexString(insval)+", comment: '"+commentline+"'");
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
