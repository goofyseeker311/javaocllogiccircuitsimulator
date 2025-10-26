package fi.jkauppa.javaocllogiccircuitsimulator;

public class JavaOCLLogicCircuitEmulator {

	public static void main(String[] arg) {
		System.out.println("init.");
		if (arg.length>1) {
			String filenamein = arg[0];
			String filenameout = arg[1];
			JavaOCLLogicCircuitEmulator emulator = new JavaOCLLogicCircuitEmulator();
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("exit.");
	}
	
	public JavaOCLLogicCircuitEmulator() {
		RiscChip riscchip = new RiscChip();
	}
	
	public class RiscChip {
		private int risccoreamount = 32000;
		private RiscCore[] risccores = new RiscCore[risccoreamount];
		
		public RiscChip() {
			for (int i=0;i<risccoreamount;i++) {
				risccores[i] = new RiscCore();
			}
		}
	}


	public class RiscCore {
		private int registeramount = 65536;
		private long[] newregisters = new long[registeramount];
		private long[] oldregisters = new long[registeramount];
		private long[] memoryram = new long[registeramount];
		private long instructionstate = 0L;
		private int instructionstep = 0;
		private int programcounter = 0;
		
		public RiscCore() {this(null);}
		public RiscCore(long[] program) {
			if (program!=null) {
				for (int i=0;(i<program.length)&&(i<registeramount);i++) {
					memoryram[i] = program[i];
				}
			}
		}
		
		public void processinstruction() {
			long instruction = memoryram[programcounter];
		}
	}
}
