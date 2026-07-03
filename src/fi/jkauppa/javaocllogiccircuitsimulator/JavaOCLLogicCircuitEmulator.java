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
	private RiscChip riscchip = null;
	public static void main(String[] arg) {
		System.out.println("init.");
		if (arg.length>1) {
			String filememory = arg[0];
			String fileboot = arg[1];
			String filecart = arg[2];
			String filememout = arg[3];
			String filecartout = arg[4];
			String filedispout = arg[5];
			String fileregout = arg[6];
			int cores = 1;
			long cycles = 8192;
			if (arg.length>7) {
				cores = Integer.parseUnsignedInt(arg[7]);
			}
			if (arg.length>8) {
				cycles = Long.parseUnsignedLong(arg[8]);
			}
			JavaOCLLogicCircuitEmulator emulator = new JavaOCLLogicCircuitEmulator();
			emulator.run(filememory, fileboot, filecart, filememout, filecartout, filedispout, fileregout, cycles, cores);
		} else {
			System.out.println("arguments expected: mem.bin boot.bin cart.bin mem.out cart.out disp.out reg.out [cores] [cycles]");
		}
		System.out.println("exit.");
	}
	
	public void run(String filememory, String fileboot, String filecart, String filememout, String filecartout, String filedispout, String fileregout, long cycles, int cores) {
		riscchip = new RiscChip(cores);
		File memoryfile = new File(filememory);
		File bootfile = new File(fileboot);
		File cartfile = new File(filecart);
		File outputmemfile = new File(filememout);
		File outputcartfile = new File(filecartout);
		File outputdispfile = new File(filedispout);
		File outputregfile = new File(fileregout);
		try {
			byte[] memoryfilebytes = Files.readAllBytes(memoryfile.toPath());
			byte[] bootfilebytes = Files.readAllBytes(bootfile.toPath());
			byte[] cartfilebytes = Files.readAllBytes(cartfile.toPath());
			BufferedOutputStream filememoutput = new BufferedOutputStream(new FileOutputStream(outputmemfile));
			BufferedOutputStream filecartoutput = new BufferedOutputStream(new FileOutputStream(outputcartfile));
			BufferedOutputStream filedispoutput = new BufferedOutputStream(new FileOutputStream(outputdispfile));
			BufferedOutputStream fileregoutput = new BufferedOutputStream(new FileOutputStream(outputregfile));
			ByteBuffer memorybytes = ByteBuffer.wrap(memoryfilebytes);
			ByteBuffer bootbytes = ByteBuffer.wrap(bootfilebytes);
			ByteBuffer cartbytes = ByteBuffer.wrap(cartfilebytes);
			long[] memory = new long[RiscChip.memoryamount];
			long[] boot = new long[RiscChip.bootamount];
			long[] cart = new long[RiscChip.cartamount];
			LongBuffer memorybuffer = memorybytes.asLongBuffer();
			LongBuffer bootbuffer = bootbytes.asLongBuffer();
			LongBuffer cartbuffer = cartbytes.asLongBuffer();
			memorybuffer.get(memory, 0, memorybuffer.remaining());
			bootbuffer.get(boot, 0, bootbuffer.remaining());
			cartbuffer.get(cart, 0, cartbuffer.remaining());
			riscchip.loadmemory(memory);
			riscchip.loadboot(boot);
			riscchip.loadcart(cart);
			riscchip.processchip(cycles);
			long[] memoryout = new long[RiscChip.memoryamount];
			long[] cartout = new long[RiscChip.cartamount];
			long[] dispout = new long[RiscChip.displayamount];
			long[] regout = new long[RiscChip.registeramount*riscchip.risccores.length];
			riscchip.savememory(memoryout);
			riscchip.savecart(cartout);
			riscchip.savedisplay(dispout);
			riscchip.saveregistry(regout);
			byte[] memoryoutarray = new byte[RiscChip.memoryamount*8];
			byte[] cartoutarray = new byte[RiscChip.cartamount*8];
			byte[] dispoutarray = new byte[RiscChip.displayamount*8];
			byte[] regoutarray = new byte[RiscChip.registeramount*riscchip.risccores.length*8];
			ByteBuffer memoryoutbytes = ByteBuffer.wrap(memoryoutarray);
			ByteBuffer cartoutbytes = ByteBuffer.wrap(cartoutarray);
			ByteBuffer dispoutbytes = ByteBuffer.wrap(dispoutarray);
			ByteBuffer regoutbytes = ByteBuffer.wrap(regoutarray);
			LongBuffer memoryoutlongs = memoryoutbytes.asLongBuffer();
			LongBuffer cartoutlongs = cartoutbytes.asLongBuffer();
			LongBuffer dispoutlongs = dispoutbytes.asLongBuffer();
			LongBuffer regoutlongs = regoutbytes.asLongBuffer();
			memoryoutlongs.put(memoryout, 0, memoryout.length);
			cartoutlongs.put(cartout, 0, cartout.length);
			dispoutlongs.put(dispout, 0, dispout.length);
			regoutlongs.put(regout, 0, regout.length);
			filememoutput.write(memoryoutarray);
			filecartoutput.write(cartoutarray);
			filedispoutput.write(dispoutarray);
			fileregoutput.write(regoutarray);
			filememoutput.close();
			filecartoutput.close();
			filedispoutput.close();
			fileregoutput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public class RiscChip {
		public RiscCore[] risccores;
		public static final int memoryamount = 65536*256;
		public static final int cartamount = 65536*256;
		public static final int bootamount = 65536*256;
		public static final int displayamount = 512*512;
		public static final int registeramount = 65536;
		public long[] memoryram = new long[memoryamount];
		public long[] cartram = new long[cartamount];
		public long[] bootrom = new long[bootamount];
		public long[] displayram = new long[displayamount];
		public long clockfrequency = 5000000000L;
		public RiscChip(int risccoreamount) {
			risccores = new RiscCore[risccoreamount];
			for (int i=0;i<risccoreamount;i++) {
				risccores[i] = new RiscCore(i);
			}
		}
		public void processchip(long cycles) {
			for (long j=0;j<cycles;j++) {
				for (int i=risccores.length-1;i>=0;i--) {
					risccores[i].updateregisters();
					risccores[i].processinstruction();
				}
			}
		}
		public void loadmemory(long[] memory) {
			if (memory!=null) {
				for (int i=0;(i<memory.length)&&(i<memoryamount);i++) {
					memoryram[i] = memory[i];
				}
			}
		}
		public void savememory(long[] memory) {
			if (memory!=null) {
				for (int i=0;(i<memory.length)&&(i<memoryamount);i++) {
					memory[i] = memoryram[i];
				}
			}
		}
		public void loadcart(long[] cart) {
			if (cart!=null) {
				for (int i=0;(i<cart.length)&&(i<cartamount);i++) {
					cartram[i] = cart[i];
				}
			}
		}
		public void savecart(long[] cart) {
			if (cart!=null) {
				for (int i=0;(i<cart.length)&&(i<cartamount);i++) {
					cart[i] = cartram[i];
				}
			}
		}
		public void loadboot(long[] boot) {
			if (boot!=null) {
				for (int i=0;(i<boot.length)&&(i<bootamount);i++) {
					bootrom[i] = boot[i];
				}
			}
		}
		public void saveboot(long[] boot) {
			if (boot!=null) {
				for (int i=0;(i<boot.length)&&(i<bootamount);i++) {
					boot[i] = bootrom[i];
				}
			}
		}
		public void savedisplay(long[] display) {
			if (display!=null) {
				for (int i=0;(i<display.length)&&(i<displayamount);i++) {
					display[i] = displayram[i];
				}
			}
		}
		public void saveregistry(long[] registry) {
			if (registry!=null) {
				for (int j=0;j<risccores.length;j++) {
					for (int i=0;i<registeramount;i++) {
						registry[j*registeramount+i] = risccores[j].oldregisters[i];
					}
				}
			}
		}
	}

	public class RiscCore {
		public int corenum = 0;
		public int cyclenum = 0;
		private long[] newregisters = new long[RiscChip.registeramount];
		private long[] oldregisters = new long[RiscChip.registeramount];
		private long[] counters = {0, 0, 0, 0, 0, 0, 0 ,0};
		private long[] timers = {0, 0, 0, 0, 0, 0, 0 ,0};
		private Random[] randoms = {new Random(), new Random(), new Random(), new Random(), new Random(), new Random(), new Random(), new Random()};
		private int timerstep = 0;
		private long instructionstate = 0L;
		private long instructionstep = 0L;
		private long programcounter = 0xC000000000000000L;
		private ByteBuffer instbytes = ByteBuffer.allocate(8);
		private ByteBuffer instbytes2 = ByteBuffer.allocate(8);
		private ByteBuffer instbytes3 = ByteBuffer.allocate(8);
		private ByteBuffer instbytes4 = ByteBuffer.allocate(8);
		private ByteBuffer longbytes = ByteBuffer.allocate(8);
		private ByteBuffer longbytes2 = ByteBuffer.allocate(8);
		private ByteBuffer longbytes3 = ByteBuffer.allocate(8);
		
		public RiscCore(int corenumi) {
			corenum = corenumi;
		}
		
		public void processinstruction() {
			long[] progarray = {programcounter};
			BitSet progbits = BitSet.valueOf(progarray);
			long progcounter = programcounter;
			if (progbits.get(63) && progbits.get(62)) {
				progbits.clear(63);
				progbits.clear(62);
				long[] bootarray = progbits.toLongArray();
				progcounter = 0;
				if (bootarray.length>0) {
					progcounter = bootarray[0];
				}
				instructionstate = riscchip.bootrom[(int)progcounter];
			} else {
				instructionstate = riscchip.memoryram[(int)progcounter];
			}
			System.out.println("core: "+String.format("%04x", corenum)+", cycle: "+String.format("%016x", cyclenum)+", programcounter: "+String.format("%016x", programcounter)+", instructionstate: "+String.format("%016x", instructionstate)+", instructionstep: "+instructionstep+
					", r0:"+String.format("%016x", oldregisters[0x0])+", r1:"+String.format("%016x", oldregisters[0x1])+", r2:"+String.format("%016x", oldregisters[0x2])+", r3:"+String.format("%016x", oldregisters[0x3])+
					", r4:"+String.format("%016x", oldregisters[0x4])+", r5:"+String.format("%016x", oldregisters[0x5])+", r6:"+String.format("%016x", oldregisters[0x6])+", r7:"+String.format("%016x", oldregisters[0x7])+
					", r8:"+String.format("%016x", oldregisters[0x8])+", r9:"+String.format("%016x", oldregisters[0x9])+", rA:"+String.format("%016x", oldregisters[0xA])+", rB:"+String.format("%016x", oldregisters[0xB])+
					", rC:"+String.format("%016x", oldregisters[0xC])+", rD:"+String.format("%016x", oldregisters[0xD])+", rE:"+String.format("%016x", oldregisters[0xE])+", rF:"+String.format("%016x", oldregisters[0xF])
					);
			instbytes.clear();
			instbytes.putLong(instructionstate).rewind();
			int regX = Short.toUnsignedInt(instbytes.getShort());
			int regY = Short.toUnsignedInt(instbytes.getShort());
			int regZ = Short.toUnsignedInt(instbytes.getShort());
			byte vecN = instbytes.get();
			int insT = Byte.toUnsignedInt(instbytes.get());
			long regYZ = Integer.toUnsignedLong(instbytes.getInt(2));
			byte[] vecnarray = {vecN};
			BitSet vecnbits = BitSet.valueOf(vecnarray);
			vecnbits.set(0);
			switch(insT) {
				case 0x00: if (true) {
					long sleepsteps = regYZ;
					if (instructionstep<sleepsteps) {
						instructionstep++;
					} else {
						instructionstep = 0;
						programcounter++;
					}
				} break;
				case 0x10: if (true) {
					programcounter = oldregisters[regX];
				} break;
				case 0x20: if (true) {
					long jumpflag = oldregisters[regY];
					if (jumpflag!=0) {
						programcounter = oldregisters[regX];
					} else {
						programcounter++;
					}
				} break;
				default: if (true) {
					for (int i=0;i<8;i++) {
						if (vecnbits.get(i)) {
	
							switch(insT) {
								case 0x30: if (true) {
									newregisters[regX+i] = regYZ;
								} break;
								case 0x40: if (true) {
									int intvalue = instbytes.getInt(2);
									instbytes2.clear();
									instbytes2.putInt(intvalue).putInt(intvalue).rewind();
									long regYZ32 = instbytes2.getLong();
									newregisters[regX+i] = regYZ32;
								} break;
								case 0x50: if (true) {
									short shortvalue = instbytes.getShort(4);
									instbytes3.clear();
									instbytes3.putShort(shortvalue).putShort(shortvalue).putShort(shortvalue).putShort(shortvalue).rewind();
									long regZ16 = instbytes3.getLong();
									newregisters[regX+i] = regZ16;
								} break;
								case 0x60: if (true) {
									byte bytevalue = instbytes.get(5);
									instbytes4.clear();
									instbytes4.put(bytevalue).put(bytevalue).put(bytevalue).put(bytevalue).put(bytevalue).put(bytevalue).put(bytevalue).put(bytevalue).rewind();
									long regZ8 = instbytes4.getLong();
									newregisters[regX+i] = regZ8;
								} break;
								case 0x70: if (true) {
									long[] regyaddr = {oldregisters[regY]};
									BitSet regybits = BitSet.valueOf(regyaddr);
									boolean regybit63 = regybits.get(63);
									boolean regybit62 = regybits.get(62);
									if (regybit63 || regybit62) {
										regybits.clear(62, 64);
										long[] memaddr = regybits.toLongArray();
										long memaddress = 0;
										if (memaddr.length>0) {
											memaddress = memaddr[0];
										}
										if (regybit63) {
											newregisters[regX+i] = riscchip.displayram[((int)memaddress)+i];
										} else {
											newregisters[regX+i] = riscchip.cartram[((int)memaddress)+i];
										}
									} else {
										newregisters[regX+i] = riscchip.memoryram[((int)oldregisters[regY])+i];
									}
								} break;
								case 0x80: if (true) {
									long[] regyaddr = {oldregisters[regY]};
									BitSet regybits = BitSet.valueOf(regyaddr);
									boolean regybit63 = regybits.get(63);
									boolean regybit62 = regybits.get(62);
									if (regybit63 || regybit62) {
										regybits.clear(62, 64);
										long[] memaddr = regybits.toLongArray();
										long memaddress = 0;
										if (memaddr.length>0) {
											memaddress = memaddr[0];
										}
										if (regybit63) {
											riscchip.displayram[((int)memaddress)+i] = oldregisters[regX+i];
										} else {
											riscchip.cartram[((int)memaddress)+i] = oldregisters[regX+i];
										}
									} else {
										riscchip.memoryram[((int)oldregisters[regY])+i] = oldregisters[regX+i];
									}
								} break;
								case 0x01: if (true) {
									newregisters[regX+i] = 0;
									if (oldregisters[regY+i]==0) {
										newregisters[regX+i] = 1;
									}
								} break;
								case 0x11: if (true) {
									newregisters[regX+i] = 0;
									if (oldregisters[regY+i]<0) {
										newregisters[regX+i] = 1;
									}
								} break;
								case 0x21: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									if (longdouble==0.0f) {
										newregisters[regX+i] = 1;
									}
								} break;
								case 0x31: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									if (longdouble<0.0f) {
										newregisters[regX+i] = 1;
									}
								} break;
								case 0x41: if (true) {
									newregisters[regX+i] = 0;
									if (oldregisters[regY+i]==oldregisters[regZ+i]) {
										newregisters[regX+i] = 1;
									}
								} break;
								case 0x51: if (true) {
									newregisters[regX+i] = 0;
									if (oldregisters[regY+i]<oldregisters[regZ+i]) {
										newregisters[regX+i] = 1;
									}
								} break;
								case 0x61: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									double longdouble2 = longbytes2.asDoubleBuffer().get();
									if (longdouble==longdouble2) {
										newregisters[regX+i] = 1;
									}
								} break;
								case 0x71: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									double longdouble2 = longbytes2.asDoubleBuffer().get();
									if (longdouble<longdouble2) {
										newregisters[regX+i] = 1;
									}
								} break;
								case 0x02: if (true) {
									newregisters[regX+i] = oldregisters[regY+i] << oldregisters[regZ+i];
								} break;
								case 0x12: if (true) {
									newregisters[regX+i] = oldregisters[regY+i] >>> oldregisters[regZ+i];
								} break;
								case 0x22: if (true) {
									newregisters[regX+i] = oldregisters[regY+i] >> oldregisters[regZ+i];
								} break;
								case 0x32: if (true) {
									newregisters[regX+i] = Long.rotateLeft(oldregisters[regY+i], (int)oldregisters[regZ+i]);
								} break;
								case 0x42: if (true) {
									newregisters[regX+i] = Long.rotateRight(oldregisters[regY+i], (int)oldregisters[regZ+i]);
								} break;
								case 0x52: if (true) {
									newregisters[regX+i] = oldregisters[regY+i];
								} break;
								case 0x62: if (true) {
									newregisters[regX+i] = ~oldregisters[regY+i];
								} break;
								case 0x72: if (true) {
									newregisters[regX+i] = oldregisters[regY+i] | oldregisters[regZ+i];
								} break;
								case 0x82: if (true) {
									newregisters[regX+i] = oldregisters[regY+i] & oldregisters[regZ+i];
								} break;
								case 0x92: if (true) {
									newregisters[regX+i] = ~(oldregisters[regY+i] & oldregisters[regZ+i]);
								} break;
								case 0xA2: if (true) {
									newregisters[regX+i] = ~(oldregisters[regY+i] | oldregisters[regZ+i]);
								} break;
								case 0xB2: if (true) {
									newregisters[regX+i] = oldregisters[regY+i] ^ oldregisters[regZ+i];
								} break;
								case 0xC2: if (true) {
									newregisters[regX+i] = ~(oldregisters[regY+i] ^ oldregisters[regZ+i]);
								} break;
								case 0xD2: if (true) {
									newregisters[regX+i] = oldregisters[regX+i];
									if (oldregisters[regZ+i]!=0) {
										newregisters[regX+i] = oldregisters[regY+i];
									}
								} break;
								case 0x03: if (true) {
									long[] regyarray = {oldregisters[regY+i]};
									BitSet regybits = BitSet.valueOf(regyarray);
									long regylowone = regybits.previousSetBit(63);
									newregisters[regX+i] = regylowone;
								} break;
								case 0x13: if (true) {
									long[] regyarray = {oldregisters[regY+i]};
									BitSet regybits = BitSet.valueOf(regyarray);
									long regyhighone = regybits.nextSetBit(0);
									newregisters[regX+i] = regyhighone;
								} break;
								case 0x23: if (true) {
									long[] regyarray = {oldregisters[regY+i]};
									BitSet regybits = BitSet.valueOf(regyarray);
									long regylowzero = regybits.nextClearBit(0);
									newregisters[regX+i] = regylowzero;
								} break;
								case 0x33: if (true) {
									long[] regyarray = {oldregisters[regY+i]};
									BitSet regybits = BitSet.valueOf(regyarray);
									long regyhighzero = regybits.previousClearBit(63);
									newregisters[regX+i] = regyhighzero;
								} break;
								case 0x43: if (true) {
									long[] regyarray = {oldregisters[regY+i]};
									BitSet regybits = BitSet.valueOf(regyarray);
									newregisters[regX+i] = regybits.cardinality();
								} break;
								case 0x04: if (true) {
									newregisters[regX+i] = oldregisters[regY+i] + oldregisters[regZ+i];
								} break;
								case 0x14: if (true) {
									newregisters[regX+i] = 0;
									BigInteger oldregY = BigInteger.valueOf(oldregisters[regY+i]);
									BigInteger oldregZ = BigInteger.valueOf(oldregisters[regZ+i]);
									BigInteger newregX = oldregY.add(oldregZ);
									BigInteger newregXover = newregX.shiftRight(64);
									if (newregXover.compareTo(BigInteger.ZERO)!=0) {
										newregisters[regX+i] = 1;
									}
								} break;
								case 0x24: if (true) {
									newregisters[regX+i] = oldregisters[regY+i] - oldregisters[regZ+i];
								} break;
								case 0x34: if (true) {
									newregisters[regX+i] = 0;
									BigInteger oldregY = BigInteger.valueOf(oldregisters[regY+i]);
									BigInteger oldregZ = BigInteger.valueOf(oldregisters[regZ+i]);
									BigInteger newregX = oldregY.subtract(oldregZ);
									BigInteger newregXover = newregX.shiftRight(64);
									if (newregXover.compareTo(BigInteger.ZERO)!=0) {
										newregisters[regX+i] = 1;
									}
								} break;
								case 0x44: if (true) {
									newregisters[regX+i] = oldregisters[regY+i] * oldregisters[regZ+i];
								} break;
								case 0x54: if (true) {
									newregisters[regX+i] = 0;
									BigInteger oldregY = BigInteger.valueOf(oldregisters[regY+i]);
									BigInteger oldregZ = BigInteger.valueOf(oldregisters[regZ+i]);
									BigInteger newregX = oldregY.multiply(oldregZ);
									BigInteger newregXover = newregX.shiftRight(64);
									if (newregXover.compareTo(BigInteger.ZERO)!=0) {
										long newregXlong = newregXover.longValueExact();
										newregisters[regX+i] = newregXlong;
									}
								} break;
								case 0x64: if (true) {
									newregisters[regX+i] = oldregisters[regY+i] / oldregisters[regZ+i];
								} break;
								case 0x74: if (true) {
									newregisters[regX+i] = oldregisters[regY+i] % oldregisters[regZ+i];
								} break;
								case 0x84: if (true) {
									newregisters[regX+i] = -oldregisters[regY+i];
								} break;
								case 0x05: if (true) {
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
								} break;
								case 0x15: if (true) {
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
								} break;
								case 0x25: if (true) {
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
								} break;
								case 0x35: if (true) {
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
								} break;
								case 0x45: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									double longdouble3 = -longdouble;
									longbytes3.clear();
									longbytes3.putDouble(longdouble3).rewind();
									newregisters[regX+i] = longbytes3.getLong();
								} break;
								case 0x55: if (true) {
									longbytes.clear();
									longbytes.putDouble((double)oldregisters[regY+i]).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x65: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									newregisters[regX+i] = Math.round(longdouble);
								} break;
								case 0x75: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									newregisters[regX+i] = (long)Math.floor(longdouble);
								} break;
								case 0x85: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									newregisters[regX+i] = (long)Math.ceil(longdouble);
								} break;
								case 0x95: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									newregisters[regX+i] = (long)longdouble;
								} break;
								case 0xA5: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									if (Double.isInfinite(longdouble)) {
										newregisters[regX+i] = 1;
									}
								} break;
								case 0xB5: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									if (Double.isNaN(longdouble)) {
										newregisters[regX+i] = 1;
									}
								} break;
								case 0x06: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									double longdouble3 = Math.sin(longdouble);
									longbytes3.clear();
									longbytes3.putDouble(longdouble3).rewind();
									newregisters[regX+i] = longbytes3.getLong();
								} break;
								case 0x16: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									double longdouble3 = Math.tan(longdouble);
									longbytes3.clear();
									longbytes3.putDouble(longdouble3).rewind();
									newregisters[regX+i] = longbytes3.getLong();
								} break;
								case 0x26: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									double longdouble3 = Math.cos(longdouble);
									longbytes3.clear();
									longbytes3.putDouble(longdouble3).rewind();
									newregisters[regX+i] = longbytes3.getLong();
								} break;
								case 0x36: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									double longdouble3 = Math.asin(longdouble);
									longbytes3.clear();
									longbytes3.putDouble(longdouble3).rewind();
									newregisters[regX+i] = longbytes3.getLong();
								} break;
								case 0x46: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									double longdouble3 = Math.atan(longdouble);
									longbytes3.clear();
									longbytes3.putDouble(longdouble3).rewind();
									newregisters[regX+i] = longbytes3.getLong();
								} break;
								case 0x56: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									double longdouble3 = Math.acos(longdouble);
									longbytes3.clear();
									longbytes3.putDouble(longdouble3).rewind();
									newregisters[regX+i] = longbytes3.getLong();
								} break;
								case 0x66: if (true) {
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
								} break;
								case 0x76: if (true) {
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
								} break;
								case 0x86: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									double longdouble3 = Math.sqrt(longdouble);
									longbytes3.clear();
									longbytes3.putDouble(longdouble3).rewind();
									newregisters[regX+i] = longbytes3.getLong();
								} break;
								case 0x07: if (true) {
									if (oldregisters[regZ+i]!=0) {
										this.counters[i] = oldregisters[regY+i];
									}
									newregisters[regX+i] = this.counters[i];
								} break;
								case 0x17: if (true) {
									if (oldregisters[regY+i]!=0) {
										this.randoms[i].setSeed(oldregisters[regZ+i]);
									}
									newregisters[regX+i] = this.randoms[i].nextLong();
								} break;
								case 0x27: if (true) {
									if (oldregisters[regZ+i]!=0) {
										riscchip.clockfrequency = oldregisters[regY+i];
									}
									if (riscchip.clockfrequency==0) {
										riscchip.clockfrequency = 100000000L;
									}
									newregisters[regX+i] = riscchip.clockfrequency;
								} break;
								case 0x37: if (true) {
									if (oldregisters[regY+i]==1) {
										newregisters[regX+i] = riscchip.risccores.length;
									} else if (oldregisters[regY+i]==2) {
										newregisters[regX+i] = RiscChip.registeramount;
									} else if (oldregisters[regY+i]==3) {
										newregisters[regX+i] = RiscChip.memoryamount;
									} else if (oldregisters[regY+i]==4) {
										newregisters[regX+i] = 0;
									} else if (oldregisters[regY+i]==5) {
										newregisters[regX+i] = 0;
									} else if (oldregisters[regY+i]==6) {
										newregisters[regX+i] = 0;
									} else if (oldregisters[regY+i]==7) {
										newregisters[regX+i] = 0;
									} else {
										newregisters[regX+i] = corenum;
									}
								} break;
								case 0x47: if (true) {
									if (oldregisters[regZ+i]!=0) {
										this.timers[i] = oldregisters[regY+i];
									}
									newregisters[regX+i] = this.timers[i];
								} break;
								case 0x08: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									int intvalue1 = longbytes.getInt(0);
									int intvalue2 = longbytes.getInt(4);
									longbytes.putLong(0L).rewind();
									if (intvalue1==0) {
										longbytes.putInt(0, 1).rewind();
									}
									if (intvalue2==0) {
										longbytes.putInt(4, 1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x18: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									int intvalue1 = longbytes.getInt(0);
									int intvalue2 = longbytes.getInt(4);
									longbytes.putLong(0L).rewind();
									if (intvalue1<0) {
										longbytes.putInt(0, 1).rewind();
									}
									if (intvalue2<0) {
										longbytes.putInt(4, 1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x28: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1 = longbytes.getFloat(0);
									float floatvalue2 = longbytes.getFloat(4);
									longbytes.putLong(0L).rewind();
									if (floatvalue1==0.0f) {
										longbytes.putInt(0, 1).rewind();
									}
									if (floatvalue2==0.0f) {
										longbytes.putInt(4, 1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x38: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1 = longbytes.getFloat(0);
									float floatvalue2 = longbytes.getFloat(4);
									longbytes.putLong(0L).rewind();
									if (floatvalue1<0.0f) {
										longbytes.putInt(0, 1).rewind();
									}
									if (floatvalue2<0.0f) {
										longbytes.putInt(4, 1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x48: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									short shortvalue1 = longbytes.getShort(0);
									short shortvalue2 = longbytes.getShort(2);
									short shortvalue3 = longbytes.getShort(4);
									short shortvalue4 = longbytes.getShort(6);
									longbytes.putLong(0L).rewind();
									if (shortvalue1==0) {
										longbytes.putShort(0, (short)1).rewind();
									}
									if (shortvalue2==0) {
										longbytes.putShort(2, (short)1).rewind();
									}
									if (shortvalue3==0) {
										longbytes.putShort(4, (short)1).rewind();
									}
									if (shortvalue4==0) {
										longbytes.putShort(6, (short)1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x58: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									short shortvalue1 = longbytes.getShort(0);
									short shortvalue2 = longbytes.getShort(2);
									short shortvalue3 = longbytes.getShort(4);
									short shortvalue4 = longbytes.getShort(6);
									longbytes.putLong(0L).rewind();
									if (shortvalue1<0) {
										longbytes.putShort(0, (short)1).rewind();
									}
									if (shortvalue2<0) {
										longbytes.putShort(2, (short)1).rewind();
									}
									if (shortvalue3<0) {
										longbytes.putShort(4, (short)1).rewind();
									}
									if (shortvalue4<0) {
										longbytes.putShort(6, (short)1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x68: if (true) {
									//TODO fp16 cmpez
								} break;
								case 0x78: if (true) {
									//TODO fp16 cmplz
								} break;
								case 0x88: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									byte bytevalue1 = longbytes.get(0);
									byte bytevalue2 = longbytes.get(1);
									byte bytevalue3 = longbytes.get(2);
									byte bytevalue4 = longbytes.get(3);
									byte bytevalue5 = longbytes.get(4);
									byte bytevalue6 = longbytes.get(5);
									byte bytevalue7 = longbytes.get(6);
									byte bytevalue8 = longbytes.get(7);
									longbytes.putLong(0L).rewind();
									if (bytevalue1==0) {
										longbytes.put(0, (byte)1).rewind();
									}
									if (bytevalue2==0) {
										longbytes.put(1, (byte)1).rewind();
									}
									if (bytevalue3==0) {
										longbytes.put(2, (byte)1).rewind();
									}
									if (bytevalue4==0) {
										longbytes.put(3, (byte)1).rewind();
									}
									if (bytevalue5==0) {
										longbytes.put(4, (byte)1).rewind();
									}
									if (bytevalue6==0) {
										longbytes.put(5, (byte)1).rewind();
									}
									if (bytevalue7==0) {
										longbytes.put(6, (byte)1).rewind();
									}
									if (bytevalue8==0) {
										longbytes.put(7, (byte)1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x98: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									byte bytevalue1 = longbytes.get(0);
									byte bytevalue2 = longbytes.get(1);
									byte bytevalue3 = longbytes.get(2);
									byte bytevalue4 = longbytes.get(3);
									byte bytevalue5 = longbytes.get(4);
									byte bytevalue6 = longbytes.get(5);
									byte bytevalue7 = longbytes.get(6);
									byte bytevalue8 = longbytes.get(7);
									longbytes.putLong(0L).rewind();
									if (bytevalue1<0) {
										longbytes.put(0, (byte)1).rewind();
									}
									if (bytevalue2<0) {
										longbytes.put(1, (byte)1).rewind();
									}
									if (bytevalue3<0) {
										longbytes.put(2, (byte)1).rewind();
									}
									if (bytevalue4<0) {
										longbytes.put(3, (byte)1).rewind();
									}
									if (bytevalue5<0) {
										longbytes.put(4, (byte)1).rewind();
									}
									if (bytevalue6<0) {
										longbytes.put(5, (byte)1).rewind();
									}
									if (bytevalue7<0) {
										longbytes.put(6, (byte)1).rewind();
									}
									if (bytevalue8<0) {
										longbytes.put(7, (byte)1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xa8: if (true) {
									//TODO fp8 cmpez
								} break;
								case 0xb8: if (true) {
									//TODO fp8 cmplz
								} break;
							}
						}
					}
				}
				programcounter++;
			}
			
			if (progcounter>=RiscChip.memoryamount) {
				programcounter = 0;
			}
			cyclenum++;

			for (int i=0;i<counters.length;i++) {
				counters[i]++;
			}
			if (timerstep==0) {
				for (int i=0;i<timers.length;i++) {
					timers[i]++;
				}
			}
			if (++timerstep>=5) {
				timerstep = 0;
			}
			for (int i=0;i<randoms.length;i++) {
				randoms[i].nextLong();
			}
		}
		
		public void updateregisters() {
			for (int i=0;i<RiscChip.registeramount;i++) {
				oldregisters[i] = newregisters[i];
			}
		}
	}
}
