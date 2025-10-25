package fi.jkauppa.javaocllogiccircuitsimulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class JavaOCLLogicCircuitAssembler {

	public static void main(String[] arg) {
		System.out.println("init.");
		if (arg.length>0) {
			String filename = arg[0];
			File inputfile = new File(filename);
			long filesize = inputfile.length();
			System.out.println("file: "+filename+", size: "+filesize+" bytes");
			try {
				BufferedReader filereader = new BufferedReader(new FileReader(inputfile));
				String readline = null;
				while((readline=filereader.readLine())!=null) {
					System.out.println("readline: "+readline);
				}
				filereader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("exit.");
	}
}
