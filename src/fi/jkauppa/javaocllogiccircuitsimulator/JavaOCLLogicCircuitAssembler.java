package fi.jkauppa.javaocllogiccircuitsimulator;

import java.io.File;

public class JavaOCLLogicCircuitAssembler {

	public static void main(String[] arg) {
		System.out.println("init.");
		if (arg.length>0) {
			String filename = arg[0];
			File inputfile = new File(filename);
			long filesize = inputfile.length();
			System.out.println("file: "+filename+", size: "+filesize+" bytes");
		}
		System.out.println("exit.");
	}
}
