package fi.jkauppa.javaocllogiccircuitsimulator;

import java.nio.ByteBuffer;

public class JavaOCLLogicCircuitEmulator {
	private RiscChip riscchip = new RiscChip();
	
	public static void main(String[] arg) {
		System.out.println("init.");
		if (arg.length>1) {
			String filenamein = arg[0];
			String filenameout = arg[1];
			JavaOCLLogicCircuitEmulator emulator = new JavaOCLLogicCircuitEmulator();
			emulator.process();
		}
		System.out.println("exit.");
	}
	
	public void process() {
		while(true) {
			riscchip.processchip();
		}
	}
	
	public class RiscChip {
		private int risccoreamount = 1; //32000
		private RiscCore[] risccores = new RiscCore[risccoreamount];
		public RiscChip() {
			for (int i=0;i<risccoreamount;i++) {
				risccores[i] = new RiscCore();
			}
		}
		public void processchip() {
			for (int i=0;i<risccoreamount;i++) {
				risccores[i].updateregisters();
				risccores[i].processinstruction();
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
			instruction = Long.parseUnsignedLong("000000002000000", 16);
			ByteBuffer instbytes = ByteBuffer.allocate(8);
			instbytes.putLong(instruction).rewind();
			int regX = instbytes.getShort();
			int regY = instbytes.getShort();
			int regZ = instbytes.getShort();
			int bitI = instbytes.get();
			int insT = instbytes.get();
			if (insT==0x00) {
				int sleepsteps = (regY<<16) + regZ;
				System.out.println("sleepsteps: "+sleepsteps+", instructionstep: "+instructionstep);
				if (instructionstep<sleepsteps) {
					instructionstep++;
				} else {
					instructionstep = 0;
					programcounter++;
				}
			}
			
			if (programcounter>65535) {
				programcounter = 0;
			}
		}
		
		public void updateregisters() {
			for (int i=0;i<registeramount;i++) {
				oldregisters[i] = newregisters[i];
			}
		}
	}
}
