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
				LongBuffer programbuffer = programbytes.asLongBuffer();
				programbuffer.get(program, 0, programbuffer.remaining());
				emulator.riscchip.risccores[0].loadprogram(program);
				emulator.process(1024);
				long[] memoryout = new long[65536];
				emulator.riscchip.risccores[0].saveprogram(memoryout);
				byte[] memoryarray = new byte[65536*8];
				ByteBuffer memorybytes = ByteBuffer.wrap(memoryarray);
				LongBuffer memorylongs = memorybytes.asLongBuffer();
				memorylongs.put(memoryout, 0, memoryout.length);
				fileoutput.write(memoryarray);
				fileoutput.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("exit.");
	}
	
	public void process(int cycles) {
		for (int i=0;i<cycles;i++) {
			riscchip.processchip();
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
		private ByteBuffer longbytes2 = ByteBuffer.allocate(8);
		private ByteBuffer longbytes3 = ByteBuffer.allocate(8);
		
		public void loadprogram(long[] program) {
			if (program!=null) {
				for (int i=0;(i<program.length)&&(i<registeramount);i++) {
					memoryram[i] = program[i];
				}
			}
		}
		public void saveprogram(long[] program) {
			if (program!=null) {
				for (int i=0;(i<program.length)&&(i<registeramount);i++) {
					program[i] = memoryram[i];
				}
			}
		}
		
		public void processinstruction() {
			instructionstate = memoryram[programcounter];
			System.out.println("instructionstate: "+String.format("%016x", instructionstate)+", instructionstep: "+instructionstep+
					", r0:"+String.format("%016x", oldregisters[0x0])+", r1:"+String.format("%016x", oldregisters[0x1])+", r2:"+String.format("%016x", oldregisters[0x2])+", r3:"+String.format("%016x", oldregisters[0x3])+
					", r4:"+String.format("%016x", oldregisters[0x4])+", r5:"+String.format("%016x", oldregisters[0x5])+", r6:"+String.format("%016x", oldregisters[0x6])+", r7:"+String.format("%016x", oldregisters[0x7])+
					", r8:"+String.format("%016x", oldregisters[0x8])+", r9:"+String.format("%016x", oldregisters[0x9])+", rA:"+String.format("%016x", oldregisters[0xA])+", rB:"+String.format("%016x", oldregisters[0xB])+
					", rC:"+String.format("%016x", oldregisters[0xC])+", rD:"+String.format("%016x", oldregisters[0xD])+", rE:"+String.format("%016x", oldregisters[0xE])+", rF:"+String.format("%016x", oldregisters[0xF])
					);
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
				try {
					Math.addExact(oldregisters[regY], oldregisters[regZ]);
				} catch (ArithmeticException e) {
					newregisters[regX] = oldregisters[regX]&(~(0x1<<bitI));
					newregisters[regX] = newregisters[regX]|(0x1<<bitI);
				}
				programcounter++;
			} else if (insT==0x25) {
				newregisters[regX] = oldregisters[regY] - oldregisters[regZ];
				programcounter++;
			} else if (insT==0x35) {
				try {
					Math.subtractExact(oldregisters[regY], oldregisters[regZ]);
				} catch (ArithmeticException e) {
					newregisters[regX] = oldregisters[regX]&(~(0x1<<bitI));
					newregisters[regX] = newregisters[regX]|(0x1<<bitI);
				}
				programcounter++;
			} else if (insT==0x45) {
				newregisters[regX] = oldregisters[regY] * oldregisters[regZ];
				programcounter++;
			} else if (insT==0x55) {
				try {
					Math.multiplyExact(oldregisters[regY], oldregisters[regZ]);
				} catch (ArithmeticException e) {
					// todo
				}
				programcounter++;
			} else if (insT==0x65) {
				newregisters[regX] = oldregisters[regY] / oldregisters[regZ];
				programcounter++;
			} else if (insT==0x75) {
				newregisters[regX] = oldregisters[regY] % oldregisters[regZ];
				programcounter++;
			} else if (insT==0x85) {
				newregisters[regX] = -oldregisters[regY];
				programcounter++;
			} else if (insT==0x06) {
				newregisters[regX] = oldregisters[regY] << oldregisters[regZ];
				programcounter++;
			} else if (insT==0x16) {
				newregisters[regX] = oldregisters[regY] >>> oldregisters[regZ];
				programcounter++;
			} else if (insT==0x26) {
				newregisters[regX] = oldregisters[regY] >> oldregisters[regZ];
				programcounter++;
			} else if (insT==0x36) {
				newregisters[regX] = Long.rotateLeft(oldregisters[regY], (int)oldregisters[regZ]);
				programcounter++;
			} else if (insT==0x46) {
				newregisters[regX] = Long.rotateRight(oldregisters[regY], (int)oldregisters[regZ]);
				programcounter++;
			} else if (insT==0x56) {
				newregisters[regX] = oldregisters[regY];
				programcounter++;
			} else if (insT==0x66) {
				newregisters[regX] = ~oldregisters[regY];
				programcounter++;
			} else if (insT==0x76) {
				newregisters[regX] = oldregisters[regY] | oldregisters[regZ];
				programcounter++;
			} else if (insT==0x86) {
				newregisters[regX] = oldregisters[regY] & oldregisters[regZ];
				programcounter++;
			} else if (insT==0x96) {
				newregisters[regX] = ~(oldregisters[regY] & oldregisters[regZ]);
				programcounter++;
			} else if (insT==0xA6) {
				newregisters[regX] = ~(oldregisters[regY] | oldregisters[regZ]);
				programcounter++;
			} else if (insT==0xB6) {
				newregisters[regX] = oldregisters[regY] ^ oldregisters[regZ];
				programcounter++;
			} else if (insT==0xC6) {
				newregisters[regX] = ~(oldregisters[regY] ^ oldregisters[regZ]);
				programcounter++;
			} else if (insT==0x07) {
				longbytes.clear();
				longbytes.putLong(oldregisters[regY]).rewind();
				double longdouble = longbytes.asDoubleBuffer().get();
				longbytes2.clear();
				longbytes2.putLong(oldregisters[regZ]).rewind();
				double longdouble2 = longbytes2.asDoubleBuffer().get();
				double longdouble3 = longdouble + longdouble2;
				longbytes3.clear();
				longbytes3.putDouble(longdouble3).rewind();
				newregisters[regX] = longbytes3.getLong();
				programcounter++;
			} else if (insT==0x17) {
				longbytes.clear();
				longbytes.putLong(oldregisters[regY]).rewind();
				double longdouble = longbytes.asDoubleBuffer().get();
				longbytes2.clear();
				longbytes2.putLong(oldregisters[regZ]).rewind();
				double longdouble2 = longbytes2.asDoubleBuffer().get();
				double longdouble3 = longdouble - longdouble2;
				longbytes3.clear();
				longbytes3.putDouble(longdouble3).rewind();
				newregisters[regX] = longbytes3.getLong();
				programcounter++;
			} else if (insT==0x27) {
				longbytes.clear();
				longbytes.putLong(oldregisters[regY]).rewind();
				double longdouble = longbytes.asDoubleBuffer().get();
				longbytes2.clear();
				longbytes2.putLong(oldregisters[regZ]).rewind();
				double longdouble2 = longbytes2.asDoubleBuffer().get();
				double longdouble3 = longdouble * longdouble2;
				longbytes3.clear();
				longbytes3.putDouble(longdouble3).rewind();
				newregisters[regX] = longbytes3.getLong();
				programcounter++;
			} else if (insT==0x37) {
				longbytes.clear();
				longbytes.putLong(oldregisters[regY]).rewind();
				double longdouble = longbytes.asDoubleBuffer().get();
				longbytes2.clear();
				longbytes2.putLong(oldregisters[regZ]).rewind();
				double longdouble2 = longbytes2.asDoubleBuffer().get();
				double longdouble3 = longdouble / longdouble2;
				longbytes3.clear();
				longbytes3.putDouble(longdouble3).rewind();
				newregisters[regX] = longbytes3.getLong();
				programcounter++;
			} else if (insT==0x47) {
				longbytes.clear();
				longbytes.putLong(oldregisters[regY]).rewind();
				double longdouble = longbytes.asDoubleBuffer().get();
				double longdouble3 = -longdouble;
				longbytes3.clear();
				longbytes3.putDouble(longdouble3).rewind();
				newregisters[regX] = longbytes3.getLong();
				programcounter++;
			} else if (insT==0x57) {
				longbytes.clear();
				longbytes.putDouble((double)oldregisters[regY]).rewind();
				newregisters[regX] = longbytes.getLong();
				programcounter++;
			} else if (insT==0x67) {
				longbytes.clear();
				longbytes.putLong(oldregisters[regY]).rewind();
				double longdouble = longbytes.asDoubleBuffer().get();
				newregisters[regX] = Math.round(longdouble);
				programcounter++;
			} else if (insT==0x77) {
				longbytes.clear();
				longbytes.putLong(oldregisters[regY]).rewind();
				double longdouble = longbytes.asDoubleBuffer().get();
				newregisters[regX] = (long)Math.floor(longdouble);
				programcounter++;
			} else if (insT==0x87) {
				longbytes.clear();
				longbytes.putLong(oldregisters[regY]).rewind();
				double longdouble = longbytes.asDoubleBuffer().get();
				newregisters[regX] = (long)Math.ceil(longdouble);
				programcounter++;
			} else if (insT==0x97) {
				longbytes.clear();
				longbytes.putLong(oldregisters[regY]).rewind();
				double longdouble = longbytes.asDoubleBuffer().get();
				newregisters[regX] = (long)longdouble;
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
