package fi.jkauppa.javaocllogiccircuitsimulator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.file.Files;

public class JavaOCLLogicCircuitEmulator {
	public RiscChip riscchip = new RiscChip();
	
	public static void main(String[] arg) {
		System.out.println("init.");
		if (arg.length>1) {
			String filein = arg[0];
			String fileout = arg[1];
			JavaOCLLogicCircuitEmulator emulator = new JavaOCLLogicCircuitEmulator();
			File inputfile = new File(filein);
			File outputfile = new File(fileout);
			try {
				byte[] inputfilebytes = Files.readAllBytes(inputfile.toPath());
				BufferedOutputStream fileoutput = new BufferedOutputStream(new FileOutputStream(outputfile));
				ByteBuffer programbytes = ByteBuffer.wrap(inputfilebytes);
				long[] program = new long[65536];
				LongBuffer porgrambuffer = programbytes.asLongBuffer();
				porgrambuffer.get(program, 0, porgrambuffer.remaining());
				emulator.riscchip.risccores[0].loadprogram(program);
				emulator.process();
				fileoutput.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("exit.");
	}
	
	public void process() {
		while(true) {
			riscchip.processchip();
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public class RiscChip {
		private int risccoreamount = 1; //32000
		public RiscCore[] risccores = new RiscCore[risccoreamount];
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
		private ByteBuffer instbytes = ByteBuffer.allocate(8);
		private ByteBuffer longbytes = ByteBuffer.allocate(8);
		
		public void loadprogram(long[] program) {
			if (program!=null) {
				for (int i=0;(i<program.length)&&(i<registeramount);i++) {
					memoryram[i] = program[i];
				}
			}
		}
		
		public void processinstruction() {
			instructionstate = memoryram[programcounter];
			System.out.println("instructionstate: "+String.format("%016x", instructionstate)+", instructionstep: "+instructionstep);
			instbytes.clear();
			instbytes.putLong(instructionstate).rewind();
			int regX = instbytes.getShort();
			int regY = instbytes.getShort();
			int regZ = instbytes.getShort();
			int bitI = instbytes.get();
			int insT = instbytes.get();
			if (insT==0x00) {
				int sleepsteps = (regY<<16) + regZ;
				if (instructionstep<sleepsteps) {
					instructionstep++;
				} else {
					instructionstep = 0;
					programcounter++;
				}
			} else if (insT==0x01) {
				long jumpflag = oldregisters[regY]&(0x1<<bitI);
				if (jumpflag>0) {
					programcounter = (int)oldregisters[regX];
				} else {
					programcounter++;
				}
			} else if (insT==0x11) {
				programcounter = (int)oldregisters[regX];
			} else if (insT==0x02) {
				newregisters[regX] = (regY<<16) + regZ;
				programcounter++;
			} else if (insT==0x03) {
				if (instructionstep==1) {
					newregisters[regX] = memoryram[(int)oldregisters[regY]];
					instructionstep = 0;
					programcounter++;
				} else {
					instructionstep = 1;
				}
			} else if (insT==0x13) {
				if (instructionstep==1) {
					memoryram[(int)oldregisters[regY]] = oldregisters[regX];
					instructionstep = 0;
					programcounter++;
				} else {
					instructionstep = 1;
				}
			} else if (insT==0x04) {
				newregisters[regX] = oldregisters[regX]&(~(0x1<<bitI));
				if (oldregisters[regY]==0) {
					newregisters[regX] = newregisters[regX]|(0x1<<bitI);
				}
				programcounter++;
			} else if (insT==0x14) {
				newregisters[regX] = oldregisters[regX]&(~(0x1<<bitI));
				if (oldregisters[regY]<0) {
					newregisters[regX] = newregisters[regX]|(0x1<<bitI);
				}
				programcounter++;
			} else if (insT==0x24) {
				newregisters[regX] = oldregisters[regX]&(~(0x1<<bitI));
				longbytes.clear();
				longbytes.putLong(oldregisters[regY]).rewind();
				double longdouble = longbytes.asDoubleBuffer().get();
				if (longdouble==0.0f) {
					newregisters[regX] = newregisters[regX]|(0x1<<bitI);
				}
				programcounter++;
			} else if (insT==0x34) {
				newregisters[regX] = oldregisters[regX]&(~(0x1<<bitI));
				longbytes.clear();
				longbytes.putLong(oldregisters[regY]).rewind();
				double longdouble = longbytes.asDoubleBuffer().get();
				if (longdouble<0.0f) {
					newregisters[regX] = newregisters[regX]|(0x1<<bitI);
				}
				programcounter++;
			} else if (insT==0x05) {
				newregisters[regX] = oldregisters[regY] + oldregisters[regZ];
				programcounter++;
			} else if (insT==0x15) {
				programcounter++;
			} else if (insT==0x25) {
				newregisters[regX] = oldregisters[regY] - oldregisters[regZ];
				programcounter++;
			} else if (insT==0x35) {
				programcounter++;
			} else if (insT==0x45) {
				programcounter++;
			} else if (insT==0x55) {
				programcounter++;
			} else if (insT==0x65) {
				programcounter++;
			} else if (insT==0x75) {
				programcounter++;
			} else if (insT==0x85) {
				programcounter++;
			} else if (insT==0x06) {
				programcounter++;
			} else if (insT==0x16) {
				programcounter++;
			} else if (insT==0x26) {
				programcounter++;
			} else if (insT==0x36) {
				programcounter++;
			} else if (insT==0x46) {
				programcounter++;
			} else if (insT==0x56) {
				newregisters[regX] = oldregisters[regY];
				programcounter++;
			} else if (insT==0x66) {
				programcounter++;
			} else if (insT==0x76) {
				programcounter++;
			} else if (insT==0x86) {
				programcounter++;
			} else if (insT==0x96) {
				programcounter++;
			} else if (insT==0xA6) {
				programcounter++;
			} else if (insT==0xB6) {
				programcounter++;
			} else if (insT==0xC6) {
				programcounter++;
			} else if (insT==0x07) {
				programcounter++;
			} else if (insT==0x17) {
				programcounter++;
			} else if (insT==0x27) {
				programcounter++;
			} else if (insT==0x37) {
				programcounter++;
			} else if (insT==0x47) {
				programcounter++;
			} else if (insT==0x57) {
				programcounter++;
			} else if (insT==0x67) {
				programcounter++;
			} else if (insT==0x77) {
				programcounter++;
			} else if (insT==0x87) {
				programcounter++;
			} else if (insT==0x97) {
				programcounter++;
			} else {
				programcounter++;
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
