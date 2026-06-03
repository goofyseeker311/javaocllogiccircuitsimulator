package fi.jkauppa.javaocllogiccircuitsimulator;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class JavaOCLLogicCircuitCompiler {

	public static void main(String[] arg) {
		System.out.println("init.");
		if (arg.length>1) {
			String filenamein = arg[0];
			String filenameout = arg[1];
			JavaOCLLogicCircuitCompiler compiler = new JavaOCLLogicCircuitCompiler();
			compiler.parse(filenamein, filenameout);
		} else {
			System.out.println("arguments expected: program.c program.asm");
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
			String readline = null;
			while((readline=filereader.readLine())!=null) {
				String codeline = readline.trim();
				System.out.println("codeline: '"+codeline+"'");
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
