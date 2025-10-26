package fi.jkauppa.javaocllogiccircuitsimulator;

public class JavaOCLLogicCircuitEmulator {

	public static void main(String[] arg) {
		System.out.println("init.");
		if (arg.length>1) {
			String filenamein = arg[0];
			String filenameout = arg[1];
		}
		System.out.println("exit.");
	}
	
	public class RiscChip {
		
	}


	public class RiscCore {
		private long[] registers = new long[65536];
		private long[] memoryram = new long[65536];
		private long state = 0L;
	}
}
