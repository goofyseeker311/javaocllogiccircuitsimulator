package fi.jkauppa.javaocllogiccircuitsimulator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.file.Files;
import java.util.BitSet;
import java.util.Random;

public class JavaOCLLogicCircuitEmulator {
	public static void main(String[] arg) {
		System.out.println("init.");
		if (arg.length>1) {
			String filein = arg[0];
			String fileout = arg[1];
			int cores = 1;
			long cycles = 1024;
			if (arg.length>2) {
				cores = Integer.parseUnsignedInt(arg[2]);
			}
			if (arg.length>3) {
				cycles = Long.parseUnsignedLong(arg[3]);
			}
			JavaOCLLogicCircuitEmulator emulator = new JavaOCLLogicCircuitEmulator();
			emulator.run(filein, fileout, cycles, cores);
		} else {
			System.out.println("arguments expected: program.bin memory.out [cores] [cycles]");
		}
		System.out.println("exit.");
	}
	
	public void run(String filein, String fileout, long cycles, int cores) {
		File inputfile = new File(filein);
		File outputfile = new File(fileout);
		try {
			byte[] inputfilebytes = Files.readAllBytes(inputfile.toPath());
			BufferedOutputStream fileoutput = new BufferedOutputStream(new FileOutputStream(outputfile));
			ByteBuffer programbytes = ByteBuffer.wrap(inputfilebytes);
			long[] program = new long[65536];
			LongBuffer programbuffer = programbytes.asLongBuffer();
			programbuffer.get(program, 0, programbuffer.remaining());
			RiscChip riscchip = new RiscChip(cores);
			riscchip.risccores[0].loadprogram(program);
			riscchip.processchip(cycles);
			long[] memoryout = new long[65536];
			riscchip.risccores[0].saveprogram(memoryout);
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
	
	public class RiscChip {
		public RiscCore[] risccores;
		public RiscChip(int risccoreamount) {
			risccores = new RiscCore[risccoreamount];
			for (int i=0;i<risccoreamount;i++) {
				risccores[i] = new RiscCore(i);
			}
		}
		public void processchip(long cycles) {
			for (long j=0;j<cycles;j++) {
				for (int i=0;i<risccores.length;i++) {
					risccores[i].updateregisters();
					risccores[i].processinstruction();
				}
			}
		}
	}

	public class RiscCore {
		private int corenum = 0;
		private int cyclenum = 0;
		private int registeramount = 65536;
		private int memoryamount = 65536*256;
		private long[] newregisters = new long[registeramount];
		private long[] oldregisters = new long[registeramount];
		private long[] memoryram = new long[memoryamount];
		private long[] counters = {0, 0, 0, 0, 0, 0, 0 ,0};
		private Random[] randoms = {new Random(), new Random(), new Random(), new Random(), new Random(), new Random(), new Random(), new Random()};
		private long instructionstate = 0L;
		private int instructionstep = 0;
		private int programcounter = 0;
		private ByteBuffer instbytes = ByteBuffer.allocate(8);
		private ByteBuffer longbytes = ByteBuffer.allocate(8);
		private ByteBuffer longbytes2 = ByteBuffer.allocate(8);
		private ByteBuffer longbytes3 = ByteBuffer.allocate(8);
		
		public RiscCore(int corenumi) {
			corenum = corenumi;
		}
		
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
			System.out.println("core: "+String.format("%04x", corenum)+", cycle: "+String.format("%016x", cyclenum)+", instructionstate: "+String.format("%016x", instructionstate)+", instructionstep: "+instructionstep+
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
			byte vecN = instbytes.get();
			int insT = instbytes.get();
			byte[] vecnarray = {vecN};
			BitSet vecnbits = BitSet.valueOf(vecnarray);
			vecnbits.set(0);
			if (insT==0x00) {
				int sleepsteps = (regY<<16) + regZ;
				if (instructionstep<sleepsteps) {
					instructionstep++;
				} else {
					instructionstep = 0;
					programcounter++;
				}
			} else if (insT==0x01) {
				long jumpflag = oldregisters[regY];
				if (jumpflag!=0) {
					programcounter = (int)oldregisters[regX];
				} else {
					programcounter++;
				}
			} else if (insT==0x11) {
				programcounter = (int)oldregisters[regX];
			} else {
				for (int i=0;i<8;i++) {
					if (vecnbits.get(i)) {

						if (insT==0x02) {
							newregisters[regX+i] = (regY<<16) + regZ;
						} else if (insT==0x03) {
							newregisters[regX+i] = memoryram[((int)oldregisters[regY])+i];
						} else if (insT==0x13) {
							memoryram[((int)oldregisters[regY])+i] = oldregisters[regX+i];
						} else if (insT==0x04) {
							newregisters[regX+i] = 0;
							if (oldregisters[regY+i]==0) {
								newregisters[regX+i] = 1;
							}
						} else if (insT==0x14) {
							newregisters[regX+i] = 0;
							if (oldregisters[regY+i]<0) {
								newregisters[regX+i] = 1;
							}
						} else if (insT==0x24) {
							newregisters[regX+i] = 0;
							longbytes.clear();
							longbytes.putLong(oldregisters[regY+i]).rewind();
							double longdouble = longbytes.asDoubleBuffer().get();
							if (longdouble==0.0f) {
								newregisters[regX+i] = 1;
							}
						} else if (insT==0x34) {
							newregisters[regX+i] = 0;
							longbytes.clear();
							longbytes.putLong(oldregisters[regY+i]).rewind();
							double longdouble = longbytes.asDoubleBuffer().get();
							if (longdouble<0.0f) {
								newregisters[regX+i] = 1;
							}
						} else if (insT==0x05) {
							newregisters[regX+i] = oldregisters[regY+i] + oldregisters[regZ+i];
						} else if (insT==0x15) {
							try {
								newregisters[regX+i] = 0;
								Math.addExact(oldregisters[regY+i], oldregisters[regZ+i]);
							} catch (ArithmeticException e) {
								newregisters[regX+i] = 1;
							}
						} else if (insT==0x25) {
							newregisters[regX+i] = oldregisters[regY+i] - oldregisters[regZ+i];
						} else if (insT==0x35) {
							try {
								newregisters[regX+i] = 0;
								Math.subtractExact(oldregisters[regY+i], oldregisters[regZ+i]);
							} catch (ArithmeticException e) {
								newregisters[regX+i] = 1;
							}
						} else if (insT==0x45) {
							newregisters[regX+i] = oldregisters[regY+i] * oldregisters[regZ+i];
						} else if (insT==0x55) {
							try {
								newregisters[regX+i] = 0;
								Math.multiplyExact(oldregisters[regY+i], oldregisters[regZ+i]);
							} catch (ArithmeticException e) {
								BigInteger oldregY = BigInteger.valueOf(oldregisters[regY+i]);
								BigInteger oldregZ = BigInteger.valueOf(oldregisters[regZ+i]);
								BigInteger newregX = oldregY.add(oldregZ);
								ByteBuffer newregXbytes = ByteBuffer.allocate(16);
								newregXbytes.put(newregX.toByteArray());
								newregXbytes.position(8);
								newregisters[regX+i] = newregXbytes.getLong();
							}
						} else if (insT==0x65) {
							newregisters[regX+i] = oldregisters[regY+i] / oldregisters[regZ+i];
						} else if (insT==0x75) {
							newregisters[regX+i] = oldregisters[regY+i] % oldregisters[regZ+i];
						} else if (insT==0x85) {
							newregisters[regX+i] = -oldregisters[regY+i];
						} else if (insT==0x95) {
							if (oldregisters[regY+i]!=0) {
								this.counters[i] = oldregisters[regZ+i];
							}
							newregisters[regX+i] = this.counters[i];
						} else if (insT==0xA5) {
							if (oldregisters[regY+i]!=0) {
								this.randoms[i].setSeed(oldregisters[regZ+i]);
							}
							newregisters[regX+i] = this.randoms[i].nextLong();
						} else if (insT==0x06) {
							newregisters[regX+i] = oldregisters[regY+i] << oldregisters[regZ+i];
						} else if (insT==0x16) {
							newregisters[regX+i] = oldregisters[regY+i] >>> oldregisters[regZ+i];
						} else if (insT==0x26) {
							newregisters[regX+i] = oldregisters[regY+i] >> oldregisters[regZ+i];
						} else if (insT==0x36) {
							newregisters[regX+i] = Long.rotateLeft(oldregisters[regY+i], (int)oldregisters[regZ+i]);
						} else if (insT==0x46) {
							newregisters[regX+i] = Long.rotateRight(oldregisters[regY+i], (int)oldregisters[regZ+i]);
						} else if (insT==0x56) {
							newregisters[regX+i] = oldregisters[regY+i];
						} else if (insT==0x66) {
							newregisters[regX+i] = ~oldregisters[regY+i];
						} else if (insT==0x76) {
							newregisters[regX+i] = oldregisters[regY+i] | oldregisters[regZ+i];
						} else if (insT==0x86) {
							newregisters[regX+i] = oldregisters[regY+i] & oldregisters[regZ+i];
						} else if (insT==0x96) {
							newregisters[regX+i] = ~(oldregisters[regY+i] & oldregisters[regZ+i]);
						} else if (insT==0xA6) {
							newregisters[regX+i] = ~(oldregisters[regY+i] | oldregisters[regZ+i]);
						} else if (insT==0xB6) {
							newregisters[regX+i] = oldregisters[regY+i] ^ oldregisters[regZ+i];
						} else if (insT==0xC6) {
							newregisters[regX+i] = ~(oldregisters[regY+i] ^ oldregisters[regZ+i]);
						} else if (insT==0xD6) {
							newregisters[regX+i] = oldregisters[regX+i];
							if (oldregisters[regZ+i]!=0) {
								newregisters[regX+i] = oldregisters[regY+i];
							}
						} else if (insT==0x07) {
							longbytes.clear();
							longbytes.putLong(oldregisters[regY+i]).rewind();
							double longdouble = longbytes.asDoubleBuffer().get();
							longbytes2.clear();
							longbytes2.putLong(oldregisters[regZ+i]).rewind();
							double longdouble2 = longbytes2.asDoubleBuffer().get();
							double longdouble3 = longdouble + longdouble2;
							longbytes3.clear();
							longbytes3.putDouble(longdouble3).rewind();
							newregisters[regX+i] = longbytes3.getLong();
						} else if (insT==0x17) {
							longbytes.clear();
							longbytes.putLong(oldregisters[regY+i]).rewind();
							double longdouble = longbytes.asDoubleBuffer().get();
							longbytes2.clear();
							longbytes2.putLong(oldregisters[regZ+i]).rewind();
							double longdouble2 = longbytes2.asDoubleBuffer().get();
							double longdouble3 = longdouble - longdouble2;
							longbytes3.clear();
							longbytes3.putDouble(longdouble3).rewind();
							newregisters[regX+i] = longbytes3.getLong();
						} else if (insT==0x27) {
							longbytes.clear();
							longbytes.putLong(oldregisters[regY+i]).rewind();
							double longdouble = longbytes.asDoubleBuffer().get();
							longbytes2.clear();
							longbytes2.putLong(oldregisters[regZ+i]).rewind();
							double longdouble2 = longbytes2.asDoubleBuffer().get();
							double longdouble3 = longdouble * longdouble2;
							longbytes3.clear();
							longbytes3.putDouble(longdouble3).rewind();
							newregisters[regX+i] = longbytes3.getLong();
						} else if (insT==0x37) {
							longbytes.clear();
							longbytes.putLong(oldregisters[regY+i]).rewind();
							double longdouble = longbytes.asDoubleBuffer().get();
							longbytes2.clear();
							longbytes2.putLong(oldregisters[regZ+i]).rewind();
							double longdouble2 = longbytes2.asDoubleBuffer().get();
							double longdouble3 = longdouble / longdouble2;
							longbytes3.clear();
							longbytes3.putDouble(longdouble3).rewind();
							newregisters[regX+i] = longbytes3.getLong();
						} else if (insT==0x47) {
							longbytes.clear();
							longbytes.putLong(oldregisters[regY+i]).rewind();
							double longdouble = longbytes.asDoubleBuffer().get();
							double longdouble3 = -longdouble;
							longbytes3.clear();
							longbytes3.putDouble(longdouble3).rewind();
							newregisters[regX+i] = longbytes3.getLong();
						} else if (insT==0x57) {
							longbytes.clear();
							longbytes.putDouble((double)oldregisters[regY+i]).rewind();
							newregisters[regX+i] = longbytes.getLong();
						} else if (insT==0x67) {
							longbytes.clear();
							longbytes.putLong(oldregisters[regY+i]).rewind();
							double longdouble = longbytes.asDoubleBuffer().get();
							newregisters[regX+i] = Math.round(longdouble);
						} else if (insT==0x77) {
							longbytes.clear();
							longbytes.putLong(oldregisters[regY+i]).rewind();
							double longdouble = longbytes.asDoubleBuffer().get();
							newregisters[regX+i] = (long)Math.floor(longdouble);
						} else if (insT==0x87) {
							longbytes.clear();
							longbytes.putLong(oldregisters[regY+i]).rewind();
							double longdouble = longbytes.asDoubleBuffer().get();
							newregisters[regX+i] = (long)Math.ceil(longdouble);
						} else if (insT==0x97) {
							longbytes.clear();
							longbytes.putLong(oldregisters[regY+i]).rewind();
							double longdouble = longbytes.asDoubleBuffer().get();
							newregisters[regX+i] = (long)longdouble;
						} else if (insT==0xA7) {
							newregisters[regX+i] = 0;
							longbytes.clear();
							longbytes.putLong(oldregisters[regY+i]).rewind();
							double longdouble = longbytes.asDoubleBuffer().get();
							if (Double.isInfinite(longdouble)) {
								newregisters[regX+i] = 1;
							}
						} else if (insT==0xB7) {
							newregisters[regX+i] = 0;
							longbytes.clear();
							longbytes.putLong(oldregisters[regY+i]).rewind();
							double longdouble = longbytes.asDoubleBuffer().get();
							if (Double.isNaN(longdouble)) {
								newregisters[regX+i] = 1;
							}
						} else if (insT==0x08) {
							longbytes.clear();
							longbytes.putLong(oldregisters[regY+i]).rewind();
							double longdouble = longbytes.asDoubleBuffer().get();
							double longdouble3 = Math.sin(longdouble);
							longbytes3.clear();
							longbytes3.putDouble(longdouble3).rewind();
							newregisters[regX+i] = longbytes3.getLong();
						} else if (insT==0x18) {
							longbytes.clear();
							longbytes.putLong(oldregisters[regY+i]).rewind();
							double longdouble = longbytes.asDoubleBuffer().get();
							double longdouble3 = Math.tan(longdouble);
							longbytes3.clear();
							longbytes3.putDouble(longdouble3).rewind();
							newregisters[regX+i] = longbytes3.getLong();
						} else if (insT==0x28) {
							longbytes.clear();
							longbytes.putLong(oldregisters[regY+i]).rewind();
							double longdouble = longbytes.asDoubleBuffer().get();
							double longdouble3 = Math.cos(longdouble);
							longbytes3.clear();
							longbytes3.putDouble(longdouble3).rewind();
							newregisters[regX+i] = longbytes3.getLong();
						} else if (insT==0x38) {
							longbytes.clear();
							longbytes.putLong(oldregisters[regY+i]).rewind();
							double longdouble = longbytes.asDoubleBuffer().get();
							double longdouble3 = Math.asin(longdouble);
							longbytes3.clear();
							longbytes3.putDouble(longdouble3).rewind();
							newregisters[regX+i] = longbytes3.getLong();
						} else if (insT==0x48) {
							longbytes.clear();
							longbytes.putLong(oldregisters[regY+i]).rewind();
							double longdouble = longbytes.asDoubleBuffer().get();
							double longdouble3 = Math.atan(longdouble);
							longbytes3.clear();
							longbytes3.putDouble(longdouble3).rewind();
							newregisters[regX+i] = longbytes3.getLong();
						} else if (insT==0x58) {
							longbytes.clear();
							longbytes.putLong(oldregisters[regY+i]).rewind();
							double longdouble = longbytes.asDoubleBuffer().get();
							double longdouble3 = Math.acos(longdouble);
							longbytes3.clear();
							longbytes3.putDouble(longdouble3).rewind();
							newregisters[regX+i] = longbytes3.getLong();
						} else if (insT==0x68) {
							longbytes.clear();
							longbytes.putLong(oldregisters[regY+i]).rewind();
							double longdouble = longbytes.asDoubleBuffer().get();
							longbytes2.clear();
							longbytes2.putLong(oldregisters[regZ+i]).rewind();
							double longdouble2 = longbytes2.asDoubleBuffer().get();
							double longdouble3 = Math.log(longdouble)/Math.log(longdouble2);
							longbytes3.clear();
							longbytes3.putDouble(longdouble3).rewind();
							newregisters[regX+i] = longbytes3.getLong();
						} else if (insT==0x78) {
							longbytes.clear();
							longbytes.putLong(oldregisters[regY+i]).rewind();
							double longdouble = longbytes.asDoubleBuffer().get();
							longbytes2.clear();
							longbytes2.putLong(oldregisters[regZ+i]).rewind();
							double longdouble2 = longbytes2.asDoubleBuffer().get();
							double longdouble3 = Math.pow(longdouble, longdouble2);
							longbytes3.clear();
							longbytes3.putDouble(longdouble3).rewind();
							newregisters[regX+i] = longbytes3.getLong();
						} else if (insT==0x88) {
							longbytes.clear();
							longbytes.putLong(oldregisters[regY+i]).rewind();
							double longdouble = longbytes.asDoubleBuffer().get();
							double longdouble3 = Math.sqrt(longdouble);
							longbytes3.clear();
							longbytes3.putDouble(longdouble3).rewind();
							newregisters[regX+i] = longbytes3.getLong();
						}
					}
				}
				programcounter++;
			}
			
			if (programcounter>=memoryamount) {
				programcounter = 0;
			}
			cyclenum++;

			for (int i=0;i<counters.length;i++) {
				counters[i]++;
			}
			for (int i=0;i<randoms.length;i++) {
				randoms[i].nextLong();
			}
		}
		
		public void updateregisters() {
			for (int i=0;i<registeramount;i++) {
				oldregisters[i] = newregisters[i];
			}
		}
	}
}
