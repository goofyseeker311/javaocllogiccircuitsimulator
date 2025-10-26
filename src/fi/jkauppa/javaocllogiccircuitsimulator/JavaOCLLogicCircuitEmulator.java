package fi.jkauppa.javaocllogiccircuitsimulator;

import java.nio.ByteBuffer;

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
			risccores[0].processinstruction();
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
			instruction = Long.parseUnsignedLong("0001000200030514", 16);
			ByteBuffer instbytes = ByteBuffer.allocate(8);
			instbytes.putLong(instruction).rewind();
			short regX = instbytes.getShort();
			short regY = instbytes.getShort();
			short regZ = instbytes.getShort();
			byte bitI = instbytes.get();
			byte insT = instbytes.get();
			System.out.println("regX: "+regX+", regY: "+regY+", regZ: "+regZ+", bitI: "+bitI+", insT: "+insT);
			if (insT==0x14) {
				System.out.println("insT==0x14");
			}
		}
		
		public void updateregisters() {
			
		}
	}
}
