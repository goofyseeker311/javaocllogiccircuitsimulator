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
				case 0x30: if (true) {
					int jumpflag = (int)oldregisters[regY];
					if (jumpflag!=0) {
						programcounter = oldregisters[regX];
					} else {
						programcounter++;
					}
				} break;
				case 0x40: if (true) {
					short jumpflag = (short)oldregisters[regY];
					if (jumpflag!=0) {
						programcounter = oldregisters[regX];
					} else {
						programcounter++;
					}
				} break;
				case 0x50: if (true) {
					byte jumpflag = (byte)oldregisters[regY];
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
								case 0x60: if (true) {
									newregisters[regX+i] = regYZ;
								} break;
								case 0x70: if (true) {
									int intvalue = instbytes.getInt(2);
									instbytes2.clear();
									instbytes2.putInt(intvalue).putInt(intvalue).rewind();
									long regYZ32 = instbytes2.getLong();
									newregisters[regX+i] = regYZ32;
								} break;
								case 0x80: if (true) {
									short shortvalue = instbytes.getShort(4);
									instbytes3.clear();
									instbytes3.putShort(shortvalue).putShort(shortvalue).putShort(shortvalue).putShort(shortvalue).rewind();
									long regZ16 = instbytes3.getLong();
									newregisters[regX+i] = regZ16;
								} break;
								case 0x90: if (true) {
									byte bytevalue = instbytes.get(5);
									instbytes4.clear();
									instbytes4.put(bytevalue).put(bytevalue).put(bytevalue).put(bytevalue).put(bytevalue).put(bytevalue).put(bytevalue).put(bytevalue).rewind();
									long regZ8 = instbytes4.getLong();
									newregisters[regX+i] = regZ8;
								} break;
								case 0xa0: if (true) {
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
								case 0xb0: if (true) {
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
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									if (Double.isInfinite(longdouble)) {
										newregisters[regX+i] = 1;
									}
								} break;
								case 0x51: if (true) {
									newregisters[regX+i] = 0;
									if (oldregisters[regY+i]==oldregisters[regZ+i]) {
										newregisters[regX+i] = 1;
									}
								} break;
								case 0x61: if (true) {
									newregisters[regX+i] = 0;
									if (oldregisters[regY+i]<oldregisters[regZ+i]) {
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
									if (longdouble==longdouble2) {
										newregisters[regX+i] = 1;
									}
								} break;
								case 0x81: if (true) {
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
								case 0x91: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									if (Double.isNaN(longdouble)) {
										newregisters[regX+i] = 1;
									}
								} break;
								case 0xa1: if (true) {
									newregisters[regX+i] = oldregisters[regY+i] << oldregisters[regZ+i];
								} break;
								case 0xb1: if (true) {
									newregisters[regX+i] = oldregisters[regY+i] >>> oldregisters[regZ+i];
								} break;
								case 0xc1: if (true) {
									newregisters[regX+i] = oldregisters[regY+i] >> oldregisters[regZ+i];
								} break;
								case 0xd1: if (true) {
									newregisters[regX+i] = Long.rotateLeft(oldregisters[regY+i], (int)oldregisters[regZ+i]);
								} break;
								case 0xe1: if (true) {
									newregisters[regX+i] = Long.rotateRight(oldregisters[regY+i], (int)oldregisters[regZ+i]);
								} break;
								
								case 0x02: if (true) {
									newregisters[regX+i] = oldregisters[regY+i];
								} break;
								case 0x12: if (true) {
									newregisters[regX+i] = ~oldregisters[regY+i];
								} break;
								case 0x22: if (true) {
									newregisters[regX+i] = oldregisters[regY+i] | oldregisters[regZ+i];
								} break;
								case 0x32: if (true) {
									newregisters[regX+i] = oldregisters[regY+i] & oldregisters[regZ+i];
								} break;
								case 0x42: if (true) {
									newregisters[regX+i] = ~(oldregisters[regY+i] & oldregisters[regZ+i]);
								} break;
								case 0x52: if (true) {
									newregisters[regX+i] = ~(oldregisters[regY+i] | oldregisters[regZ+i]);
								} break;
								case 0x62: if (true) {
									newregisters[regX+i] = oldregisters[regY+i] ^ oldregisters[regZ+i];
								} break;
								case 0x72: if (true) {
									newregisters[regX+i] = ~(oldregisters[regY+i] ^ oldregisters[regZ+i]);
								} break;
								case 0x82: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									int shortvalue1 = longbytes.getShort(0);
									int shortvalue2 = longbytes.getShort(2);
									int shortvalue3 = longbytes.getShort(4);
									int shortvalue4 = longbytes.getShort(6);
									longbytes.putLong(0L).rewind();
									longbytes.put(0, (byte)shortvalue1).rewind();
									longbytes.put(1, (byte)shortvalue1).rewind();
									longbytes.put(2, (byte)shortvalue2).rewind();
									longbytes.put(3, (byte)shortvalue2).rewind();
									longbytes.put(4, (byte)shortvalue3).rewind();
									longbytes.put(5, (byte)shortvalue3).rewind();
									longbytes.put(6, (byte)shortvalue4).rewind();
									longbytes.put(7, (byte)shortvalue4).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x92: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									int bytevalue1 = longbytes.get(0);
									int bytevalue2 = longbytes.get(2);
									int bytevalue3 = longbytes.get(4);
									int bytevalue4 = longbytes.get(6);
									longbytes.putLong(0L).rewind();
									longbytes.putShort(0, (short)bytevalue1).rewind();
									longbytes.putShort(2, (short)bytevalue2).rewind();
									longbytes.putShort(4, (short)bytevalue3).rewind();
									longbytes.putShort(6, (short)bytevalue4).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xa2: if (true) {
									newregisters[regX+i] = this.counters[i];
									if (oldregisters[regZ+i]!=0) {
										this.counters[i] = oldregisters[regY+i];
									}
								} break;
								case 0xb2: if (true) {
									if (oldregisters[regY+i]!=0) {
										this.randoms[i].setSeed(oldregisters[regZ+i]);
									}
									newregisters[regX+i] = this.randoms[i].nextLong();
								} break;
								case 0xc2: if (true) {
									if (oldregisters[regZ+i]!=0) {
										riscchip.clockfrequency = oldregisters[regY+i];
									}
									if (riscchip.clockfrequency==0) {
										riscchip.clockfrequency = 100000000L;
									}
									newregisters[regX+i] = riscchip.clockfrequency;
								} break;
								case 0xd2: if (true) {
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
								case 0xe2: if (true) {
									if (oldregisters[regZ+i]!=0) {
										this.timers[i] = oldregisters[regY+i];
									}
									newregisters[regX+i] = this.timers[i];
								} break;
								
								case 0x03: if (true) {
									newregisters[regX+i] = oldregisters[regY+i] + oldregisters[regZ+i];
								} break;
								case 0x13: if (true) {
									newregisters[regX+i] = oldregisters[regY+i] - oldregisters[regZ+i];
								} break;
								case 0x23: if (true) {
									newregisters[regX+i] = oldregisters[regY+i] * oldregisters[regZ+i];
								} break;
								case 0x33: if (true) {
									newregisters[regX+i] = oldregisters[regY+i] / oldregisters[regZ+i];
								} break;
								case 0x43: if (true) {
									newregisters[regX+i] = -oldregisters[regY+i];
								} break;
								case 0x53: if (true) {
									newregisters[regX+i] = 0;
									BigInteger oldregY = BigInteger.valueOf(oldregisters[regY+i]);
									BigInteger oldregZ = BigInteger.valueOf(oldregisters[regZ+i]);
									BigInteger newregX = oldregY.add(oldregZ);
									BigInteger newregXover = newregX.shiftRight(64);
									if (newregXover.compareTo(BigInteger.ZERO)!=0) {
										newregisters[regX+i] = 1;
									}
								} break;
								case 0x63: if (true) {
									newregisters[regX+i] = 0;
									BigInteger oldregY = BigInteger.valueOf(oldregisters[regY+i]);
									BigInteger oldregZ = BigInteger.valueOf(oldregisters[regZ+i]);
									BigInteger newregX = oldregY.subtract(oldregZ);
									BigInteger newregXover = newregX.shiftRight(64);
									if (newregXover.compareTo(BigInteger.ZERO)!=0) {
										newregisters[regX+i] = 1;
									}
								} break;
								case 0x73: if (true) {
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
								case 0x83: if (true) {
									newregisters[regX+i] = oldregisters[regY+i] % oldregisters[regZ+i];
								} break;
								case 0x93: if (true) {
									newregisters[regX+i] = oldregisters[regX+i];
									if (oldregisters[regZ+i]!=0) {
										newregisters[regX+i] = oldregisters[regY+i];
									}
								} break;
								case 0xa3: if (true) {
									long[] regyarray = {oldregisters[regY+i]};
									BitSet regybits = BitSet.valueOf(regyarray);
									long regylowone = regybits.nextSetBit(0);
									newregisters[regX+i] = regylowone;
								} break;
								case 0xb3: if (true) {
									long[] regyarray = {oldregisters[regY+i]};
									BitSet regybits = BitSet.valueOf(regyarray);
									long regyhighone = regybits.previousSetBit(63);
									newregisters[regX+i] = regyhighone;
								} break;
								case 0xc3: if (true) {
									long[] regyarray = {oldregisters[regY+i]};
									BitSet regybits = BitSet.valueOf(regyarray);
									long regylowzero = regybits.nextClearBit(0);
									newregisters[regX+i] = regylowzero;
								} break;
								case 0xd3: if (true) {
									long[] regyarray = {oldregisters[regY+i]};
									BitSet regybits = BitSet.valueOf(regyarray);
									long regyhighzero = regybits.previousClearBit(63);
									newregisters[regX+i] = regyhighzero;
								} break;
								case 0xe3: if (true) {
									long[] regyarray = {oldregisters[regY+i]};
									BitSet regybits = BitSet.valueOf(regyarray);
									newregisters[regX+i] = regybits.cardinality();
								} break;
								
								case 0x04: if (true) {
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
								case 0x14: if (true) {
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
								case 0x24: if (true) {
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
								case 0x34: if (true) {
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
								case 0x44: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									double longdouble3 = -longdouble;
									longbytes3.clear();
									longbytes3.putDouble(longdouble3).rewind();
									newregisters[regX+i] = longbytes3.getLong();
								} break;
								case 0x54: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									double longdouble3 = Math.sin(longdouble);
									longbytes3.clear();
									longbytes3.putDouble(longdouble3).rewind();
									newregisters[regX+i] = longbytes3.getLong();
								} break;
								case 0x64: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									double longdouble3 = Math.tan(longdouble);
									longbytes3.clear();
									longbytes3.putDouble(longdouble3).rewind();
									newregisters[regX+i] = longbytes3.getLong();
								} break;
								case 0x74: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									double longdouble3 = Math.cos(longdouble);
									longbytes3.clear();
									longbytes3.putDouble(longdouble3).rewind();
									newregisters[regX+i] = longbytes3.getLong();
								} break;
								case 0x84: if (true) {
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
								case 0x94: if (true) {
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
								case 0xa4: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									double longdouble3 = Math.asin(longdouble);
									longbytes3.clear();
									longbytes3.putDouble(longdouble3).rewind();
									newregisters[regX+i] = longbytes3.getLong();
								} break;
								case 0xb4: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									double longdouble3 = Math.atan(longdouble);
									longbytes3.clear();
									longbytes3.putDouble(longdouble3).rewind();
									newregisters[regX+i] = longbytes3.getLong();
								} break;
								case 0xc4: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									double longdouble3 = Math.acos(longdouble);
									longbytes3.clear();
									longbytes3.putDouble(longdouble3).rewind();
									newregisters[regX+i] = longbytes3.getLong();
								} break;
								case 0xd4: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									double longdouble3 = Math.sqrt(longdouble);
									longbytes3.clear();
									longbytes3.putDouble(longdouble3).rewind();
									newregisters[regX+i] = longbytes3.getLong();
								} break;
								
								case 0x05: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double doublevalue1 = longbytes.getDouble(0);
									longbytes.putLong(0L).rewind();
									longbytes.putFloat(0, (float)doublevalue1).rewind();
									longbytes.putFloat(4, (float)doublevalue1).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x15: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1 = longbytes.getFloat(0);
									float floatvalue2 = longbytes.getFloat(4);
									longbytes.putLong(0L).rewind();
									longbytes.putShort(0, floattohalf(floatvalue1)).rewind();
									longbytes.putShort(2, floattohalf(floatvalue1)).rewind();
									longbytes.putShort(4, floattohalf(floatvalue2)).rewind();
									longbytes.putShort(6, floattohalf(floatvalue2)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x25: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float shortvalue1 = halftofloat(longbytes.getShort(0));
									float shortvalue2 = halftofloat(longbytes.getShort(2));
									float shortvalue3 = halftofloat(longbytes.getShort(4));
									float shortvalue4 = halftofloat(longbytes.getShort(6));
									longbytes.putLong(0L).rewind();
									longbytes.put(0, floattomini(shortvalue1)).rewind();
									longbytes.put(1, floattomini(shortvalue1)).rewind();
									longbytes.put(2, floattomini(shortvalue2)).rewind();
									longbytes.put(3, floattomini(shortvalue2)).rewind();
									longbytes.put(4, floattomini(shortvalue3)).rewind();
									longbytes.put(5, floattomini(shortvalue3)).rewind();
									longbytes.put(6, floattomini(shortvalue4)).rewind();
									longbytes.put(7, floattomini(shortvalue4)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x35: if (true) {
									long longvalue1 = oldregisters[regY+i];
									longbytes.putInt(0, (int)longvalue1).rewind();
									longbytes.putInt(4, (int)longvalue1).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x45: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									int intvalue1 = longbytes.getInt(0);
									int intvalue2 = longbytes.getInt(4);
									longbytes.putLong(0L).rewind();
									longbytes.putShort(0, (short)intvalue1).rewind();
									longbytes.putShort(2, (short)intvalue1).rewind();
									longbytes.putShort(4, (short)intvalue2).rewind();
									longbytes.putShort(6, (short)intvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x55: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float bytevalue1 = minitofloat(longbytes.get(0));
									float bytevalue2 = minitofloat(longbytes.get(2));
									float bytevalue3 = minitofloat(longbytes.get(4));
									float bytevalue4 = minitofloat(longbytes.get(6));
									longbytes.putLong(0L).rewind();
									longbytes.putShort(0, floattohalf(bytevalue1)).rewind();
									longbytes.putShort(2, floattohalf(bytevalue2)).rewind();
									longbytes.putShort(4, floattohalf(bytevalue3)).rewind();
									longbytes.putShort(6, floattohalf(bytevalue4)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x65: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float shortvalue1 = halftofloat(longbytes.getShort(0));
									float shortvalue2 = halftofloat(longbytes.getShort(4));
									longbytes.putLong(0L).rewind();
									longbytes.putFloat(0, shortvalue1).rewind();
									longbytes.putFloat(4, shortvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x75: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1 = longbytes.getFloat(0);
									longbytes.putLong(0L).rewind();
									longbytes.putDouble(0, (double)floatvalue1).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x85: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									int intvalue1 = longbytes.getInt(0);
									longbytes.putLong(0L).rewind();
									longbytes.putLong(0, (long)intvalue1).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x95: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									int bytevalue1 = longbytes.getShort(0);
									int bytevalue2 = longbytes.getShort(4);
									longbytes.putLong(0L).rewind();
									longbytes.putInt(0, (int)bytevalue1).rewind();
									longbytes.putInt(4, (int)bytevalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xa5: if (true) {
									longbytes.clear();
									longbytes.putDouble((double)oldregisters[regY+i]).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xb5: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									newregisters[regX+i] = Math.round(longdouble);
								} break;
								case 0xc5: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									newregisters[regX+i] = (long)Math.floor(longdouble);
								} break;
								case 0xd5: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									newregisters[regX+i] = (long)Math.ceil(longdouble);
								} break;
								case 0xe5: if (true) {
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									double longdouble = longbytes.asDoubleBuffer().get();
									newregisters[regX+i] = (long)longdouble;
								} break;
								
								case 0x06: if (true) {
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
								case 0x16: if (true) {
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
								case 0x26: if (true) {
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
								case 0x36: if (true) {
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
								case 0x46: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = longbytes.getFloat(0);
									float floatvalue1b = longbytes.getFloat(4);
									longbytes.putLong(0L).rewind();
									int intvalue1 = Float.isInfinite(floatvalue1a)?1:0;
									int intvalue2 = Float.isInfinite(floatvalue1b)?1:0;
									longbytes.putInt(0, intvalue1).rewind();
									longbytes.putInt(4, intvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x56: if (true) {
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
								case 0x66: if (true) {
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
								case 0x76: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1 = halftofloat(longbytes.getShort(0));
									float floatvalue2 = halftofloat(longbytes.getShort(2));
									float floatvalue3 = halftofloat(longbytes.getShort(4));
									float floatvalue4 = halftofloat(longbytes.getShort(6));
									longbytes.putLong(0L).rewind();
									if (floatvalue1==0.0f) {
										longbytes.putShort(0, (short)1).rewind();
									}
									if (floatvalue2==0.0f) {
										longbytes.putShort(2, (short)1).rewind();
									}
									if (floatvalue3==0.0f) {
										longbytes.putShort(4, (short)1).rewind();
									}
									if (floatvalue4==0.0f) {
										longbytes.putShort(6, (short)1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x86: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1 = halftofloat(longbytes.getShort(0));
									float floatvalue2 = halftofloat(longbytes.getShort(2));
									float floatvalue3 = halftofloat(longbytes.getShort(4));
									float floatvalue4 = halftofloat(longbytes.getShort(6));
									longbytes.putLong(0L).rewind();
									if (floatvalue1<0.0f) {
										longbytes.putShort(0, (short)1).rewind();
									}
									if (floatvalue2<0.0f) {
										longbytes.putShort(2, (short)1).rewind();
									}
									if (floatvalue3<0.0f) {
										longbytes.putShort(4, (short)1).rewind();
									}
									if (floatvalue4<0.0f) {
										longbytes.putShort(6, (short)1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x96: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = halftofloat(longbytes.getShort(0));
									float floatvalue1b = halftofloat(longbytes.getShort(2));
									float floatvalue1c = halftofloat(longbytes.getShort(4));
									float floatvalue1d = halftofloat(longbytes.getShort(6));
									longbytes.putLong(0L).rewind();
									int intvalue1 = Float.isInfinite(floatvalue1a)?1:0;
									int intvalue2 = Float.isInfinite(floatvalue1b)?1:0;
									int intvalue3 = Float.isInfinite(floatvalue1c)?1:0;
									int intvalue4 = Float.isInfinite(floatvalue1d)?1:0;
									longbytes.putShort(0, (short)intvalue1).rewind();
									longbytes.putShort(2, (short)intvalue2).rewind();
									longbytes.putShort(4, (short)intvalue3).rewind();
									longbytes.putShort(6, (short)intvalue4).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xa6: if (true) {
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
								case 0xb6: if (true) {
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
								case 0xc6: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1 = minitofloat(longbytes.get(0));
									float floatvalue2 = minitofloat(longbytes.get(1));
									float floatvalue3 = minitofloat(longbytes.get(2));
									float floatvalue4 = minitofloat(longbytes.get(3));
									float floatvalue5 = minitofloat(longbytes.get(4));
									float floatvalue6 = minitofloat(longbytes.get(5));
									float floatvalue7 = minitofloat(longbytes.get(6));
									float floatvalue8 = minitofloat(longbytes.get(7));
									longbytes.putLong(0L).rewind();
									if (floatvalue1==0.0f) {
										longbytes.put(0, (byte)1).rewind();
									}
									if (floatvalue2==0.0f) {
										longbytes.put(1, (byte)1).rewind();
									}
									if (floatvalue3==0.0f) {
										longbytes.put(2, (byte)1).rewind();
									}
									if (floatvalue4==0.0f) {
										longbytes.put(3, (byte)1).rewind();
									}
									if (floatvalue5==0.0f) {
										longbytes.put(4, (byte)1).rewind();
									}
									if (floatvalue6==0.0f) {
										longbytes.put(5, (byte)1).rewind();
									}
									if (floatvalue7==0.0f) {
										longbytes.put(6, (byte)1).rewind();
									}
									if (floatvalue8==0.0f) {
										longbytes.put(7, (byte)1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xd6: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1 = minitofloat(longbytes.get(0));
									float floatvalue2 = minitofloat(longbytes.get(1));
									float floatvalue3 = minitofloat(longbytes.get(2));
									float floatvalue4 = minitofloat(longbytes.get(3));
									float floatvalue5 = minitofloat(longbytes.get(4));
									float floatvalue6 = minitofloat(longbytes.get(5));
									float floatvalue7 = minitofloat(longbytes.get(6));
									float floatvalue8 = minitofloat(longbytes.get(7));
									longbytes.putLong(0L).rewind();
									if (floatvalue1<0.0f) {
										longbytes.put(0, (byte)1).rewind();
									}
									if (floatvalue2<0.0f) {
										longbytes.put(1, (byte)1).rewind();
									}
									if (floatvalue3<0.0f) {
										longbytes.put(2, (byte)1).rewind();
									}
									if (floatvalue4<0.0f) {
										longbytes.put(3, (byte)1).rewind();
									}
									if (floatvalue5<0.0f) {
										longbytes.put(4, (byte)1).rewind();
									}
									if (floatvalue6<0.0f) {
										longbytes.put(5, (byte)1).rewind();
									}
									if (floatvalue7<0.0f) {
										longbytes.put(6, (byte)1).rewind();
									}
									if (floatvalue8<0.0f) {
										longbytes.put(7, (byte)1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xe6: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = minitofloat(longbytes.get(0));
									float floatvalue1b = minitofloat(longbytes.get(1));
									float floatvalue1c = minitofloat(longbytes.get(2));
									float floatvalue1d = minitofloat(longbytes.get(3));
									float floatvalue1e = minitofloat(longbytes.get(4));
									float floatvalue1f = minitofloat(longbytes.get(5));
									float floatvalue1g = minitofloat(longbytes.get(6));
									float floatvalue1h = minitofloat(longbytes.get(7));
									longbytes.putLong(0L).rewind();
									int intvalue1 = Float.isInfinite(floatvalue1a)?1:0;
									int intvalue2 = Float.isInfinite(floatvalue1b)?1:0;
									int intvalue3 = Float.isInfinite(floatvalue1c)?1:0;
									int intvalue4 = Float.isInfinite(floatvalue1d)?1:0;
									int intvalue5 = Float.isInfinite(floatvalue1e)?1:0;
									int intvalue6 = Float.isInfinite(floatvalue1f)?1:0;
									int intvalue7 = Float.isInfinite(floatvalue1g)?1:0;
									int intvalue8 = Float.isInfinite(floatvalue1h)?1:0;
									longbytes.put(0, (byte)intvalue1).rewind();
									longbytes.put(1, (byte)intvalue2).rewind();
									longbytes.put(2, (byte)intvalue3).rewind();
									longbytes.put(3, (byte)intvalue4).rewind();
									longbytes.put(4, (byte)intvalue5).rewind();
									longbytes.put(5, (byte)intvalue6).rewind();
									longbytes.put(6, (byte)intvalue7).rewind();
									longbytes.put(7, (byte)intvalue8).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;

								case 0x07: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int intvalue1a = longbytes.getInt(0);
									int intvalue1b = longbytes.getInt(4);
									int intvalue2a = longbytes2.getInt(0);
									int intvalue2b = longbytes2.getInt(4);
									longbytes.putLong(0L).rewind();
									if (intvalue1a==intvalue2a) {
										longbytes.putInt(0, 1).rewind();
									}
									if (intvalue1b==intvalue2b) {
										longbytes.putInt(4, 1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x17: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int intvalue1a = longbytes.getInt(0);
									int intvalue1b = longbytes.getInt(4);
									int intvalue2a = longbytes2.getInt(0);
									int intvalue2b = longbytes2.getInt(4);
									longbytes.putLong(0L).rewind();
									if (intvalue1a<intvalue2a) {
										longbytes.putInt(0, 1).rewind();
									}
									if (intvalue1b<intvalue2b) {
										longbytes.putInt(4, 1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x27: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = longbytes.getFloat(0);
									float floatvalue1b = longbytes.getFloat(4);
									float floatvalue2a = longbytes2.getFloat(0);
									float floatvalue2b = longbytes2.getFloat(4);
									longbytes.putLong(0L).rewind();
									if (floatvalue1a==floatvalue2a) {
										longbytes.putInt(0, 1).rewind();
									}
									if (floatvalue1b==floatvalue2b) {
										longbytes.putInt(4, 1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x37: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = longbytes.getFloat(0);
									float floatvalue1b = longbytes.getFloat(4);
									float floatvalue2a = longbytes2.getFloat(0);
									float floatvalue2b = longbytes2.getFloat(4);
									longbytes.putLong(0L).rewind();
									if (floatvalue1a<floatvalue2a) {
										longbytes.putInt(0, 1).rewind();
									}
									if (floatvalue1b<floatvalue2b) {
										longbytes.putInt(4, 1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x47: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = longbytes.getFloat(0);
									float floatvalue1b = longbytes.getFloat(4);
									longbytes.putLong(0L).rewind();
									int intvalue1 = Float.isNaN(floatvalue1a)?1:0;
									int intvalue2 = Float.isNaN(floatvalue1b)?1:0;
									longbytes.putInt(0, intvalue1).rewind();
									longbytes.putInt(4, intvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x57: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									short shortvalue1a = longbytes.getShort(0);
									short shortvalue1b = longbytes.getShort(2);
									short shortvalue1c = longbytes.getShort(4);
									short shortvalue1d = longbytes.getShort(6);
									short shortvalue2a = longbytes2.getShort(0);
									short shortvalue2b = longbytes2.getShort(2);
									short shortvalue2c = longbytes2.getShort(4);
									short shortvalue2d = longbytes2.getShort(6);
									longbytes.putLong(0L).rewind();
									if (shortvalue1a==shortvalue2a) {
										longbytes.putShort(0, (short)1).rewind();
									}
									if (shortvalue1b==shortvalue2b) {
										longbytes.putShort(2, (short)1).rewind();
									}
									if (shortvalue1c==shortvalue2c) {
										longbytes.putShort(4, (short)1).rewind();
									}
									if (shortvalue1d==shortvalue2d) {
										longbytes.putShort(6, (short)1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x67: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									short shortvalue1a = longbytes.getShort(0);
									short shortvalue1b = longbytes.getShort(2);
									short shortvalue1c = longbytes.getShort(4);
									short shortvalue1d = longbytes.getShort(6);
									short shortvalue2a = longbytes2.getShort(0);
									short shortvalue2b = longbytes2.getShort(2);
									short shortvalue2c = longbytes2.getShort(4);
									short shortvalue2d = longbytes2.getShort(6);
									longbytes.putLong(0L).rewind();
									if (shortvalue1a<shortvalue2a) {
										longbytes.putShort(0, (short)1).rewind();
									}
									if (shortvalue1b<shortvalue2b) {
										longbytes.putShort(2, (short)1).rewind();
									}
									if (shortvalue1c<shortvalue2c) {
										longbytes.putShort(4, (short)1).rewind();
									}
									if (shortvalue1d<shortvalue2d) {
										longbytes.putShort(6, (short)1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x77: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = halftofloat(longbytes.getShort(0));
									float floatvalue1b = halftofloat(longbytes.getShort(2));
									float floatvalue1c = halftofloat(longbytes.getShort(4));
									float floatvalue1d = halftofloat(longbytes.getShort(6));
									float floatvalue2a = halftofloat(longbytes2.getShort(0));
									float floatvalue2b = halftofloat(longbytes2.getShort(2));
									float floatvalue2c = halftofloat(longbytes2.getShort(4));
									float floatvalue2d = halftofloat(longbytes2.getShort(6));
									longbytes.putLong(0L).rewind();
									if (floatvalue1a==floatvalue2a) {
										longbytes.putShort(0, (short)1).rewind();
									}
									if (floatvalue1b==floatvalue2b) {
										longbytes.putShort(2, (short)1).rewind();
									}
									if (floatvalue1c==floatvalue2c) {
										longbytes.putShort(4, (short)1).rewind();
									}
									if (floatvalue1d==floatvalue2d) {
										longbytes.putShort(6, (short)1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x87: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = halftofloat(longbytes.getShort(0));
									float floatvalue1b = halftofloat(longbytes.getShort(2));
									float floatvalue1c = halftofloat(longbytes.getShort(4));
									float floatvalue1d = halftofloat(longbytes.getShort(6));
									float floatvalue2a = halftofloat(longbytes2.getShort(0));
									float floatvalue2b = halftofloat(longbytes2.getShort(2));
									float floatvalue2c = halftofloat(longbytes2.getShort(4));
									float floatvalue2d = halftofloat(longbytes2.getShort(6));
									longbytes.putLong(0L).rewind();
									if (floatvalue1a<floatvalue2a) {
										longbytes.putShort(0, (short)1).rewind();
									}
									if (floatvalue1b<floatvalue2b) {
										longbytes.putShort(2, (short)1).rewind();
									}
									if (floatvalue1c<floatvalue2c) {
										longbytes.putShort(4, (short)1).rewind();
									}
									if (floatvalue1d<floatvalue2d) {
										longbytes.putShort(6, (short)1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x97: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = halftofloat(longbytes.getShort(0));
									float floatvalue1b = halftofloat(longbytes.getShort(2));
									float floatvalue1c = halftofloat(longbytes.getShort(4));
									float floatvalue1d = halftofloat(longbytes.getShort(6));
									longbytes.putLong(0L).rewind();
									int intvalue1 = Float.isNaN(floatvalue1a)?1:0;
									int intvalue2 = Float.isNaN(floatvalue1b)?1:0;
									int intvalue3 = Float.isNaN(floatvalue1c)?1:0;
									int intvalue4 = Float.isNaN(floatvalue1d)?1:0;
									longbytes.putShort(0, (short)intvalue1).rewind();
									longbytes.putShort(2, (short)intvalue2).rewind();
									longbytes.putShort(4, (short)intvalue3).rewind();
									longbytes.putShort(6, (short)intvalue4).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xa7: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									byte bytevalue1a = longbytes.get(0);
									byte bytevalue1b = longbytes.get(1);
									byte bytevalue1c = longbytes.get(2);
									byte bytevalue1d = longbytes.get(3);
									byte bytevalue1e = longbytes.get(4);
									byte bytevalue1f = longbytes.get(5);
									byte bytevalue1g = longbytes.get(6);
									byte bytevalue1h = longbytes.get(7);
									byte bytevalue2a = longbytes2.get(0);
									byte bytevalue2b = longbytes2.get(1);
									byte bytevalue2c = longbytes2.get(2);
									byte bytevalue2d = longbytes2.get(3);
									byte bytevalue2e = longbytes2.get(4);
									byte bytevalue2f = longbytes2.get(5);
									byte bytevalue2g = longbytes2.get(6);
									byte bytevalue2h = longbytes2.get(7);
									longbytes.putLong(0L).rewind();
									if (bytevalue1a==bytevalue2a) {
										longbytes.put(0, (byte)1).rewind();
									}
									if (bytevalue1b==bytevalue2b) {
										longbytes.put(1, (byte)1).rewind();
									}
									if (bytevalue1c==bytevalue2c) {
										longbytes.put(2, (byte)1).rewind();
									}
									if (bytevalue1d==bytevalue2d) {
										longbytes.put(3, (byte)1).rewind();
									}
									if (bytevalue1e==bytevalue2e) {
										longbytes.put(4, (byte)1).rewind();
									}
									if (bytevalue1f==bytevalue2f) {
										longbytes.put(5, (byte)1).rewind();
									}
									if (bytevalue1g==bytevalue2g) {
										longbytes.put(6, (byte)1).rewind();
									}
									if (bytevalue1h==bytevalue2h) {
										longbytes.put(7, (byte)1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xb7: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									byte bytevalue1a = longbytes.get(0);
									byte bytevalue1b = longbytes.get(1);
									byte bytevalue1c = longbytes.get(2);
									byte bytevalue1d = longbytes.get(3);
									byte bytevalue1e = longbytes.get(4);
									byte bytevalue1f = longbytes.get(5);
									byte bytevalue1g = longbytes.get(6);
									byte bytevalue1h = longbytes.get(7);
									byte bytevalue2a = longbytes2.get(0);
									byte bytevalue2b = longbytes2.get(1);
									byte bytevalue2c = longbytes2.get(2);
									byte bytevalue2d = longbytes2.get(3);
									byte bytevalue2e = longbytes2.get(4);
									byte bytevalue2f = longbytes2.get(5);
									byte bytevalue2g = longbytes2.get(6);
									byte bytevalue2h = longbytes2.get(7);
									longbytes.putLong(0L).rewind();
									if (bytevalue1a<bytevalue2a) {
										longbytes.put(0, (byte)1).rewind();
									}
									if (bytevalue1b<bytevalue2b) {
										longbytes.put(1, (byte)1).rewind();
									}
									if (bytevalue1c<bytevalue2c) {
										longbytes.put(2, (byte)1).rewind();
									}
									if (bytevalue1d<bytevalue2d) {
										longbytes.put(3, (byte)1).rewind();
									}
									if (bytevalue1e<bytevalue2e) {
										longbytes.put(4, (byte)1).rewind();
									}
									if (bytevalue1f<bytevalue2f) {
										longbytes.put(5, (byte)1).rewind();
									}
									if (bytevalue1g<bytevalue2g) {
										longbytes.put(6, (byte)1).rewind();
									}
									if (bytevalue1h<bytevalue2h) {
										longbytes.put(7, (byte)1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xc7: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = minitofloat(longbytes.get(0));
									float floatvalue1b = minitofloat(longbytes.get(1));
									float floatvalue1c = minitofloat(longbytes.get(2));
									float floatvalue1d = minitofloat(longbytes.get(3));
									float floatvalue1e = minitofloat(longbytes.get(4));
									float floatvalue1f = minitofloat(longbytes.get(5));
									float floatvalue1g = minitofloat(longbytes.get(6));
									float floatvalue1h = minitofloat(longbytes.get(7));
									float floatvalue2a = minitofloat(longbytes2.get(0));
									float floatvalue2b = minitofloat(longbytes2.get(1));
									float floatvalue2c = minitofloat(longbytes2.get(2));
									float floatvalue2d = minitofloat(longbytes2.get(3));
									float floatvalue2e = minitofloat(longbytes2.get(4));
									float floatvalue2f = minitofloat(longbytes2.get(5));
									float floatvalue2g = minitofloat(longbytes2.get(6));
									float floatvalue2h = minitofloat(longbytes2.get(7));
									longbytes.putLong(0L).rewind();
									if (floatvalue1a==floatvalue2a) {
										longbytes.put(0, (byte)1).rewind();
									}
									if (floatvalue1b==floatvalue2b) {
										longbytes.put(1, (byte)1).rewind();
									}
									if (floatvalue1c==floatvalue2c) {
										longbytes.put(2, (byte)1).rewind();
									}
									if (floatvalue1d==floatvalue2d) {
										longbytes.put(3, (byte)1).rewind();
									}
									if (floatvalue1e==floatvalue2e) {
										longbytes.put(4, (byte)1).rewind();
									}
									if (floatvalue1f==floatvalue2f) {
										longbytes.put(5, (byte)1).rewind();
									}
									if (floatvalue1g==floatvalue2g) {
										longbytes.put(6, (byte)1).rewind();
									}
									if (floatvalue1h==floatvalue2h) {
										longbytes.put(7, (byte)1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xd7: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = minitofloat(longbytes.get(0));
									float floatvalue1b = minitofloat(longbytes.get(1));
									float floatvalue1c = minitofloat(longbytes.get(2));
									float floatvalue1d = minitofloat(longbytes.get(3));
									float floatvalue1e = minitofloat(longbytes.get(4));
									float floatvalue1f = minitofloat(longbytes.get(5));
									float floatvalue1g = minitofloat(longbytes.get(6));
									float floatvalue1h = minitofloat(longbytes.get(7));
									float floatvalue2a = minitofloat(longbytes2.get(0));
									float floatvalue2b = minitofloat(longbytes2.get(1));
									float floatvalue2c = minitofloat(longbytes2.get(2));
									float floatvalue2d = minitofloat(longbytes2.get(3));
									float floatvalue2e = minitofloat(longbytes2.get(4));
									float floatvalue2f = minitofloat(longbytes2.get(5));
									float floatvalue2g = minitofloat(longbytes2.get(6));
									float floatvalue2h = minitofloat(longbytes2.get(7));
									longbytes.putLong(0L).rewind();
									if (floatvalue1a<floatvalue2a) {
										longbytes.put(0, (byte)1).rewind();
									}
									if (floatvalue1b<floatvalue2b) {
										longbytes.put(1, (byte)1).rewind();
									}
									if (floatvalue1c<floatvalue2c) {
										longbytes.put(2, (byte)1).rewind();
									}
									if (floatvalue1d<floatvalue2d) {
										longbytes.put(3, (byte)1).rewind();
									}
									if (floatvalue1e<floatvalue2e) {
										longbytes.put(4, (byte)1).rewind();
									}
									if (floatvalue1f<floatvalue2f) {
										longbytes.put(5, (byte)1).rewind();
									}
									if (floatvalue1g<floatvalue2g) {
										longbytes.put(6, (byte)1).rewind();
									}
									if (floatvalue1h<floatvalue2h) {
										longbytes.put(7, (byte)1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xe7: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = minitofloat(longbytes.get(0));
									float floatvalue1b = minitofloat(longbytes.get(1));
									float floatvalue1c = minitofloat(longbytes.get(2));
									float floatvalue1d = minitofloat(longbytes.get(3));
									float floatvalue1e = minitofloat(longbytes.get(4));
									float floatvalue1f = minitofloat(longbytes.get(5));
									float floatvalue1g = minitofloat(longbytes.get(6));
									float floatvalue1h = minitofloat(longbytes.get(7));
									longbytes.putLong(0L).rewind();
									int intvalue1 = Float.isNaN(floatvalue1a)?1:0;
									int intvalue2 = Float.isNaN(floatvalue1b)?1:0;
									int intvalue3 = Float.isNaN(floatvalue1c)?1:0;
									int intvalue4 = Float.isNaN(floatvalue1d)?1:0;
									int intvalue5 = Float.isNaN(floatvalue1e)?1:0;
									int intvalue6 = Float.isNaN(floatvalue1f)?1:0;
									int intvalue7 = Float.isNaN(floatvalue1g)?1:0;
									int intvalue8 = Float.isNaN(floatvalue1h)?1:0;
									longbytes.put(0, (byte)intvalue1).rewind();
									longbytes.put(1, (byte)intvalue2).rewind();
									longbytes.put(2, (byte)intvalue3).rewind();
									longbytes.put(3, (byte)intvalue4).rewind();
									longbytes.put(4, (byte)intvalue5).rewind();
									longbytes.put(5, (byte)intvalue6).rewind();
									longbytes.put(6, (byte)intvalue7).rewind();
									longbytes.put(7, (byte)intvalue8).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								
								case 0x08: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int intvalue1 = longbytes.getInt(0);
									int intvalue2 = longbytes.getInt(4);
									int shiftvalue1 = longbytes2.getInt(0);
									int shiftvalue2 = longbytes2.getInt(4);
									longbytes.putLong(0L).rewind();
									intvalue1 = intvalue1 << shiftvalue1;
									intvalue2 = intvalue2 << shiftvalue2;
									longbytes.putInt(0, intvalue1).rewind();
									longbytes.putInt(4, intvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x18: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int intvalue1 = longbytes.getInt(0);
									int intvalue2 = longbytes.getInt(4);
									int shiftvalue1 = longbytes2.getInt(0);
									int shiftvalue2 = longbytes2.getInt(4);
									longbytes.putLong(0L).rewind();
									intvalue1 = intvalue1 >>> shiftvalue1;
									intvalue2 = intvalue2 >>> shiftvalue2;
									longbytes.putInt(0, intvalue1).rewind();
									longbytes.putInt(4, intvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x28: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int intvalue1 = longbytes.getInt(0);
									int intvalue2 = longbytes.getInt(4);
									int shiftvalue1 = longbytes2.getInt(0);
									int shiftvalue2 = longbytes2.getInt(4);
									longbytes.putLong(0L).rewind();
									intvalue1 = intvalue1 >> shiftvalue1;
									intvalue2 = intvalue2 >> shiftvalue2;
									longbytes.putInt(0, intvalue1).rewind();
									longbytes.putInt(4, intvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x38: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int intvalue1 = longbytes.getInt(0);
									int intvalue2 = longbytes.getInt(4);
									int shiftvalue1 = longbytes2.getInt(0);
									int shiftvalue2 = longbytes2.getInt(4);
									longbytes.putLong(0L).rewind();
									intvalue1 = Integer.rotateLeft(intvalue1, shiftvalue1);
									intvalue2 = Integer.rotateLeft(intvalue2, shiftvalue2);
									longbytes.putInt(0, intvalue1).rewind();
									longbytes.putInt(4, intvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x48: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int intvalue1 = longbytes.getInt(0);
									int intvalue2 = longbytes.getInt(4);
									int shiftvalue1 = longbytes2.getInt(0);
									int shiftvalue2 = longbytes2.getInt(4);
									longbytes.putLong(0L).rewind();
									intvalue1 = Integer.rotateRight(intvalue1, shiftvalue1);
									intvalue2 = Integer.rotateRight(intvalue2, shiftvalue2);
									longbytes.putInt(0, intvalue1).rewind();
									longbytes.putInt(4, intvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x58: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int shortvalue1 = longbytes.getShort(0);
									int shortvalue2 = longbytes.getShort(2);
									int shortvalue3 = longbytes.getShort(4);
									int shortvalue4 = longbytes.getShort(6);
									int shiftvalue1 = longbytes2.getShort(0);
									int shiftvalue2 = longbytes2.getShort(2);
									int shiftvalue3 = longbytes2.getShort(4);
									int shiftvalue4 = longbytes2.getShort(6);
									longbytes.putLong(0L).rewind();
									shortvalue1 = shortvalue1 << shiftvalue1;
									shortvalue2 = shortvalue2 << shiftvalue2;
									shortvalue3 = shortvalue3 << shiftvalue3;
									shortvalue4 = shortvalue4 << shiftvalue4;
									longbytes.putShort(0, (short)shortvalue1).rewind();
									longbytes.putShort(2, (short)shortvalue2).rewind();
									longbytes.putShort(4, (short)shortvalue3).rewind();
									longbytes.putShort(6, (short)shortvalue4).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x68: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int shortvalue1 = longbytes.getShort(0);
									int shortvalue2 = longbytes.getShort(2);
									int shortvalue3 = longbytes.getShort(4);
									int shortvalue4 = longbytes.getShort(6);
									int shiftvalue1 = longbytes2.getShort(0);
									int shiftvalue2 = longbytes2.getShort(2);
									int shiftvalue3 = longbytes2.getShort(4);
									int shiftvalue4 = longbytes2.getShort(6);
									longbytes.putLong(0L).rewind();
									shortvalue1 = shortvalue1 >>> shiftvalue1;
									shortvalue2 = shortvalue2 >>> shiftvalue2;
									shortvalue3 = shortvalue3 >>> shiftvalue3;
									shortvalue4 = shortvalue4 >>> shiftvalue4;
									longbytes.putShort(0, (short)shortvalue1).rewind();
									longbytes.putShort(2, (short)shortvalue2).rewind();
									longbytes.putShort(4, (short)shortvalue3).rewind();
									longbytes.putShort(6, (short)shortvalue4).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x78: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int shortvalue1 = longbytes.getShort(0);
									int shortvalue2 = longbytes.getShort(2);
									int shortvalue3 = longbytes.getShort(4);
									int shortvalue4 = longbytes.getShort(6);
									int shiftvalue1 = longbytes2.getShort(0);
									int shiftvalue2 = longbytes2.getShort(2);
									int shiftvalue3 = longbytes2.getShort(4);
									int shiftvalue4 = longbytes2.getShort(6);
									longbytes.putLong(0L).rewind();
									shortvalue1 = shortvalue1 >> shiftvalue1;
									shortvalue2 = shortvalue2 >> shiftvalue2;
									shortvalue3 = shortvalue3 >> shiftvalue3;
									shortvalue4 = shortvalue4 >> shiftvalue4;
									longbytes.putShort(0, (short)shortvalue1).rewind();
									longbytes.putShort(2, (short)shortvalue2).rewind();
									longbytes.putShort(4, (short)shortvalue3).rewind();
									longbytes.putShort(6, (short)shortvalue4).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x88: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int shortvalue1 = longbytes.getShort(0);
									int shortvalue2 = longbytes.getShort(2);
									int shortvalue3 = longbytes.getShort(4);
									int shortvalue4 = longbytes.getShort(6);
									int shiftvalue1 = longbytes2.getShort(0);
									int shiftvalue2 = longbytes2.getShort(2);
									int shiftvalue3 = longbytes2.getShort(4);
									int shiftvalue4 = longbytes2.getShort(6);
									longbytes.putLong(0L).rewind();
									shortvalue1 = Integer.rotateLeft(shortvalue1, shiftvalue1);
									shortvalue2 = Integer.rotateLeft(shortvalue2, shiftvalue2);
									shortvalue3 = Integer.rotateLeft(shortvalue3, shiftvalue3);
									shortvalue4 = Integer.rotateLeft(shortvalue4, shiftvalue4);
									longbytes.putShort(0, (short)shortvalue1).rewind();
									longbytes.putShort(2, (short)shortvalue2).rewind();
									longbytes.putShort(4, (short)shortvalue3).rewind();
									longbytes.putShort(6, (short)shortvalue4).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x98: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int shortvalue1 = longbytes.getShort(0);
									int shortvalue2 = longbytes.getShort(2);
									int shortvalue3 = longbytes.getShort(4);
									int shortvalue4 = longbytes.getShort(6);
									int shiftvalue1 = longbytes2.getShort(0);
									int shiftvalue2 = longbytes2.getShort(2);
									int shiftvalue3 = longbytes2.getShort(4);
									int shiftvalue4 = longbytes2.getShort(6);
									longbytes.putLong(0L).rewind();
									shortvalue1 = Integer.rotateRight(shortvalue1, shiftvalue1);
									shortvalue2 = Integer.rotateRight(shortvalue2, shiftvalue2);
									shortvalue3 = Integer.rotateRight(shortvalue3, shiftvalue3);
									shortvalue4 = Integer.rotateRight(shortvalue4, shiftvalue4);
									longbytes.putShort(0, (short)shortvalue1).rewind();
									longbytes.putShort(2, (short)shortvalue2).rewind();
									longbytes.putShort(4, (short)shortvalue3).rewind();
									longbytes.putShort(6, (short)shortvalue4).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xa8: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int bytevalue1 = longbytes.get(0);
									int bytevalue2 = longbytes.get(1);
									int bytevalue3 = longbytes.get(2);
									int bytevalue4 = longbytes.get(3);
									int bytevalue5 = longbytes.get(4);
									int bytevalue6 = longbytes.get(5);
									int bytevalue7 = longbytes.get(6);
									int bytevalue8 = longbytes.get(7);
									int shiftvalue1 = longbytes2.get(0);
									int shiftvalue2 = longbytes2.get(1);
									int shiftvalue3 = longbytes2.get(2);
									int shiftvalue4 = longbytes2.get(3);
									int shiftvalue5 = longbytes2.get(4);
									int shiftvalue6 = longbytes2.get(5);
									int shiftvalue7 = longbytes2.get(6);
									int shiftvalue8 = longbytes2.get(7);
									longbytes.putLong(0L).rewind();
									bytevalue1 = bytevalue1 << shiftvalue1;
									bytevalue2 = bytevalue2 << shiftvalue2;
									bytevalue3 = bytevalue3 << shiftvalue3;
									bytevalue4 = bytevalue4 << shiftvalue4;
									bytevalue5 = bytevalue5 << shiftvalue5;
									bytevalue6 = bytevalue6 << shiftvalue6;
									bytevalue7 = bytevalue7 << shiftvalue7;
									bytevalue8 = bytevalue8 << shiftvalue8;
									longbytes.put(0, (byte)bytevalue1).rewind();
									longbytes.put(1, (byte)bytevalue2).rewind();
									longbytes.put(2, (byte)bytevalue3).rewind();
									longbytes.put(3, (byte)bytevalue4).rewind();
									longbytes.put(4, (byte)bytevalue5).rewind();
									longbytes.put(5, (byte)bytevalue6).rewind();
									longbytes.put(6, (byte)bytevalue7).rewind();
									longbytes.put(7, (byte)bytevalue8).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xb8: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int bytevalue1 = longbytes.get(0);
									int bytevalue2 = longbytes.get(1);
									int bytevalue3 = longbytes.get(2);
									int bytevalue4 = longbytes.get(3);
									int bytevalue5 = longbytes.get(4);
									int bytevalue6 = longbytes.get(5);
									int bytevalue7 = longbytes.get(6);
									int bytevalue8 = longbytes.get(7);
									int shiftvalue1 = longbytes2.get(0);
									int shiftvalue2 = longbytes2.get(1);
									int shiftvalue3 = longbytes2.get(2);
									int shiftvalue4 = longbytes2.get(3);
									int shiftvalue5 = longbytes2.get(4);
									int shiftvalue6 = longbytes2.get(5);
									int shiftvalue7 = longbytes2.get(6);
									int shiftvalue8 = longbytes2.get(7);
									longbytes.putLong(0L).rewind();
									bytevalue1 = bytevalue1 >>> shiftvalue1;
									bytevalue2 = bytevalue2 >>> shiftvalue2;
									bytevalue3 = bytevalue3 >>> shiftvalue3;
									bytevalue4 = bytevalue4 >>> shiftvalue4;
									bytevalue5 = bytevalue5 >>> shiftvalue5;
									bytevalue6 = bytevalue6 >>> shiftvalue6;
									bytevalue7 = bytevalue7 >>> shiftvalue7;
									bytevalue8 = bytevalue8 >>> shiftvalue8;
									longbytes.put(0, (byte)bytevalue1).rewind();
									longbytes.put(1, (byte)bytevalue2).rewind();
									longbytes.put(2, (byte)bytevalue3).rewind();
									longbytes.put(3, (byte)bytevalue4).rewind();
									longbytes.put(4, (byte)bytevalue5).rewind();
									longbytes.put(5, (byte)bytevalue6).rewind();
									longbytes.put(6, (byte)bytevalue7).rewind();
									longbytes.put(7, (byte)bytevalue8).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xc8: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int bytevalue1 = longbytes.get(0);
									int bytevalue2 = longbytes.get(1);
									int bytevalue3 = longbytes.get(2);
									int bytevalue4 = longbytes.get(3);
									int bytevalue5 = longbytes.get(4);
									int bytevalue6 = longbytes.get(5);
									int bytevalue7 = longbytes.get(6);
									int bytevalue8 = longbytes.get(7);
									int shiftvalue1 = longbytes2.get(0);
									int shiftvalue2 = longbytes2.get(1);
									int shiftvalue3 = longbytes2.get(2);
									int shiftvalue4 = longbytes2.get(3);
									int shiftvalue5 = longbytes2.get(4);
									int shiftvalue6 = longbytes2.get(5);
									int shiftvalue7 = longbytes2.get(6);
									int shiftvalue8 = longbytes2.get(7);
									longbytes.putLong(0L).rewind();
									bytevalue1 = bytevalue1 >> shiftvalue1;
									bytevalue2 = bytevalue2 >> shiftvalue2;
									bytevalue3 = bytevalue3 >> shiftvalue3;
									bytevalue4 = bytevalue4 >> shiftvalue4;
									bytevalue5 = bytevalue5 >> shiftvalue5;
									bytevalue6 = bytevalue6 >> shiftvalue6;
									bytevalue7 = bytevalue7 >> shiftvalue7;
									bytevalue8 = bytevalue8 >> shiftvalue8;
									longbytes.put(0, (byte)bytevalue1).rewind();
									longbytes.put(1, (byte)bytevalue2).rewind();
									longbytes.put(2, (byte)bytevalue3).rewind();
									longbytes.put(3, (byte)bytevalue4).rewind();
									longbytes.put(4, (byte)bytevalue5).rewind();
									longbytes.put(5, (byte)bytevalue6).rewind();
									longbytes.put(6, (byte)bytevalue7).rewind();
									longbytes.put(7, (byte)bytevalue8).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xd8: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int bytevalue1 = longbytes.get(0);
									int bytevalue2 = longbytes.get(1);
									int bytevalue3 = longbytes.get(2);
									int bytevalue4 = longbytes.get(3);
									int bytevalue5 = longbytes.get(4);
									int bytevalue6 = longbytes.get(5);
									int bytevalue7 = longbytes.get(6);
									int bytevalue8 = longbytes.get(7);
									int shiftvalue1 = longbytes2.get(0);
									int shiftvalue2 = longbytes2.get(1);
									int shiftvalue3 = longbytes2.get(2);
									int shiftvalue4 = longbytes2.get(3);
									int shiftvalue5 = longbytes2.get(4);
									int shiftvalue6 = longbytes2.get(5);
									int shiftvalue7 = longbytes2.get(6);
									int shiftvalue8 = longbytes2.get(7);
									longbytes.putLong(0L).rewind();
									bytevalue1 = Integer.rotateLeft(bytevalue1, shiftvalue1);
									bytevalue2 = Integer.rotateLeft(bytevalue2, shiftvalue2);
									bytevalue3 = Integer.rotateLeft(bytevalue3, shiftvalue3);
									bytevalue4 = Integer.rotateLeft(bytevalue4, shiftvalue4);
									bytevalue5 = Integer.rotateLeft(bytevalue5, shiftvalue5);
									bytevalue6 = Integer.rotateLeft(bytevalue6, shiftvalue6);
									bytevalue7 = Integer.rotateLeft(bytevalue7, shiftvalue7);
									bytevalue8 = Integer.rotateLeft(bytevalue8, shiftvalue8);
									longbytes.put(0, (byte)bytevalue1).rewind();
									longbytes.put(1, (byte)bytevalue2).rewind();
									longbytes.put(2, (byte)bytevalue3).rewind();
									longbytes.put(3, (byte)bytevalue4).rewind();
									longbytes.put(4, (byte)bytevalue5).rewind();
									longbytes.put(5, (byte)bytevalue6).rewind();
									longbytes.put(6, (byte)bytevalue7).rewind();
									longbytes.put(7, (byte)bytevalue8).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xe8: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int bytevalue1 = longbytes.get(0);
									int bytevalue2 = longbytes.get(1);
									int bytevalue3 = longbytes.get(2);
									int bytevalue4 = longbytes.get(3);
									int bytevalue5 = longbytes.get(4);
									int bytevalue6 = longbytes.get(5);
									int bytevalue7 = longbytes.get(6);
									int bytevalue8 = longbytes.get(7);
									int shiftvalue1 = longbytes2.get(0);
									int shiftvalue2 = longbytes2.get(1);
									int shiftvalue3 = longbytes2.get(2);
									int shiftvalue4 = longbytes2.get(3);
									int shiftvalue5 = longbytes2.get(4);
									int shiftvalue6 = longbytes2.get(5);
									int shiftvalue7 = longbytes2.get(6);
									int shiftvalue8 = longbytes2.get(7);
									longbytes.putLong(0L).rewind();
									bytevalue1 = Integer.rotateRight(bytevalue1, shiftvalue1);
									bytevalue2 = Integer.rotateRight(bytevalue2, shiftvalue2);
									bytevalue3 = Integer.rotateRight(bytevalue3, shiftvalue3);
									bytevalue4 = Integer.rotateRight(bytevalue4, shiftvalue4);
									bytevalue5 = Integer.rotateRight(bytevalue5, shiftvalue5);
									bytevalue6 = Integer.rotateRight(bytevalue6, shiftvalue6);
									bytevalue7 = Integer.rotateRight(bytevalue7, shiftvalue7);
									bytevalue8 = Integer.rotateRight(bytevalue8, shiftvalue8);
									longbytes.put(0, (byte)bytevalue1).rewind();
									longbytes.put(1, (byte)bytevalue2).rewind();
									longbytes.put(2, (byte)bytevalue3).rewind();
									longbytes.put(3, (byte)bytevalue4).rewind();
									longbytes.put(4, (byte)bytevalue5).rewind();
									longbytes.put(5, (byte)bytevalue6).rewind();
									longbytes.put(6, (byte)bytevalue7).rewind();
									longbytes.put(7, (byte)bytevalue8).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								
								case 0x09: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int intvalue1a = longbytes.getInt(0);
									int intvalue1b = longbytes.getInt(4);
									int intvalue2a = longbytes2.getInt(0);
									int intvalue2b = longbytes2.getInt(4);
									longbytes.putLong(0L).rewind();
									int intvaluea = intvalue1a + intvalue2a;
									int intvalueb = intvalue1b + intvalue2b;
									longbytes.putInt(0, intvaluea).rewind();
									longbytes.putInt(4, intvalueb).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x19: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int intvalue1a = longbytes.getInt(0);
									int intvalue1b = longbytes.getInt(4);
									int intvalue2a = longbytes2.getInt(0);
									int intvalue2b = longbytes2.getInt(4);
									longbytes.putLong(0L).rewind();
									int intvaluea = intvalue1a - intvalue2a;
									int intvalueb = intvalue1b - intvalue2b;
									longbytes.putInt(0, intvaluea).rewind();
									longbytes.putInt(4, intvalueb).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x29: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int intvalue1a = longbytes.getInt(0);
									int intvalue1b = longbytes.getInt(4);
									int intvalue2a = longbytes2.getInt(0);
									int intvalue2b = longbytes2.getInt(4);
									longbytes.putLong(0L).rewind();
									int intvaluea = intvalue1a * intvalue2a;
									int intvalueb = intvalue1b * intvalue2b;
									longbytes.putInt(0, intvaluea).rewind();
									longbytes.putInt(4, intvalueb).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x39: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int intvalue1a = longbytes.getInt(0);
									int intvalue1b = longbytes.getInt(4);
									int intvalue2a = longbytes2.getInt(0);
									int intvalue2b = longbytes2.getInt(4);
									longbytes.putLong(0L).rewind();
									int intvaluea = intvalue1a / intvalue2a;
									int intvalueb = intvalue1b / intvalue2b;
									longbytes.putInt(0, intvaluea).rewind();
									longbytes.putInt(4, intvalueb).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x49: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int intvalue1 = -longbytes.getInt(0);
									int intvalue2 = -longbytes.getInt(4);
									longbytes.putLong(0L).rewind();
									longbytes.putInt(0, intvalue1).rewind();
									longbytes.putInt(4, intvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x59: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int shortvalue1a = longbytes.getShort(0);
									int shortvalue1b = longbytes.getShort(2);
									int shortvalue1c = longbytes.getShort(4);
									int shortvalue1d = longbytes.getShort(6);
									int shortvalue2a = longbytes2.getShort(0);
									int shortvalue2b = longbytes2.getShort(2);
									int shortvalue2c = longbytes2.getShort(4);
									int shortvalue2d = longbytes2.getShort(6);
									longbytes.putLong(0L).rewind();
									int shortvaluea = shortvalue1a + shortvalue2a;
									int shortvalueb = shortvalue1b + shortvalue2b;
									int shortvaluec = shortvalue1c + shortvalue2c;
									int shortvalued = shortvalue1d + shortvalue2d;
									longbytes.putShort(0, (short)shortvaluea).rewind();
									longbytes.putShort(2, (short)shortvalueb).rewind();
									longbytes.putShort(4, (short)shortvaluec).rewind();
									longbytes.putShort(6, (short)shortvalued).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x69: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int shortvalue1a = longbytes.getShort(0);
									int shortvalue1b = longbytes.getShort(2);
									int shortvalue1c = longbytes.getShort(4);
									int shortvalue1d = longbytes.getShort(6);
									int shortvalue2a = longbytes2.getShort(0);
									int shortvalue2b = longbytes2.getShort(2);
									int shortvalue2c = longbytes2.getShort(4);
									int shortvalue2d = longbytes2.getShort(6);
									longbytes.putLong(0L).rewind();
									int shortvaluea = shortvalue1a - shortvalue2a;
									int shortvalueb = shortvalue1b - shortvalue2b;
									int shortvaluec = shortvalue1c - shortvalue2c;
									int shortvalued = shortvalue1d - shortvalue2d;
									longbytes.putShort(0, (short)shortvaluea).rewind();
									longbytes.putShort(2, (short)shortvalueb).rewind();
									longbytes.putShort(4, (short)shortvaluec).rewind();
									longbytes.putShort(6, (short)shortvalued).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x79: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int shortvalue1a = longbytes.getShort(0);
									int shortvalue1b = longbytes.getShort(2);
									int shortvalue1c = longbytes.getShort(4);
									int shortvalue1d = longbytes.getShort(6);
									int shortvalue2a = longbytes2.getShort(0);
									int shortvalue2b = longbytes2.getShort(2);
									int shortvalue2c = longbytes2.getShort(4);
									int shortvalue2d = longbytes2.getShort(6);
									longbytes.putLong(0L).rewind();
									int shortvaluea = shortvalue1a * shortvalue2a;
									int shortvalueb = shortvalue1b * shortvalue2b;
									int shortvaluec = shortvalue1c * shortvalue2c;
									int shortvalued = shortvalue1d * shortvalue2d;
									longbytes.putShort(0, (short)shortvaluea).rewind();
									longbytes.putShort(2, (short)shortvalueb).rewind();
									longbytes.putShort(4, (short)shortvaluec).rewind();
									longbytes.putShort(6, (short)shortvalued).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x89: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int shortvalue1a = longbytes.getShort(0);
									int shortvalue1b = longbytes.getShort(2);
									int shortvalue1c = longbytes.getShort(4);
									int shortvalue1d = longbytes.getShort(6);
									int shortvalue2a = longbytes2.getShort(0);
									int shortvalue2b = longbytes2.getShort(2);
									int shortvalue2c = longbytes2.getShort(4);
									int shortvalue2d = longbytes2.getShort(6);
									longbytes.putLong(0L).rewind();
									int shortvaluea = shortvalue1a / shortvalue2a;
									int shortvalueb = shortvalue1b / shortvalue2b;
									int shortvaluec = shortvalue1c / shortvalue2c;
									int shortvalued = shortvalue1d / shortvalue2d;
									longbytes.putShort(0, (short)shortvaluea).rewind();
									longbytes.putShort(2, (short)shortvalueb).rewind();
									longbytes.putShort(4, (short)shortvaluec).rewind();
									longbytes.putShort(6, (short)shortvalued).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x99: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int shortvalue1 = -longbytes.getShort(0);
									int shortvalue2 = -longbytes.getShort(2);
									int shortvalue3 = -longbytes.getShort(4);
									int shortvalue4 = -longbytes.getShort(6);
									longbytes.putLong(0L).rewind();
									longbytes.putShort(0, (short)shortvalue1).rewind();
									longbytes.putShort(2, (short)shortvalue2).rewind();
									longbytes.putShort(4, (short)shortvalue3).rewind();
									longbytes.putShort(6, (short)shortvalue4).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xa9: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int bytevalue1a = longbytes.get(0);
									int bytevalue1b = longbytes.get(1);
									int bytevalue1c = longbytes.get(2);
									int bytevalue1d = longbytes.get(3);
									int bytevalue1e = longbytes.get(4);
									int bytevalue1f = longbytes.get(5);
									int bytevalue1g = longbytes.get(6);
									int bytevalue1h = longbytes.get(7);
									int bytevalue2a = longbytes2.get(0);
									int bytevalue2b = longbytes2.get(1);
									int bytevalue2c = longbytes2.get(2);
									int bytevalue2d = longbytes2.get(3);
									int bytevalue2e = longbytes2.get(4);
									int bytevalue2f = longbytes2.get(5);
									int bytevalue2g = longbytes2.get(6);
									int bytevalue2h = longbytes2.get(7);
									longbytes.putLong(0L).rewind();
									int bytevaluea = bytevalue1a + bytevalue2a;
									int bytevalueb = bytevalue1b + bytevalue2b;
									int bytevaluec = bytevalue1c + bytevalue2c;
									int bytevalued = bytevalue1d + bytevalue2d;
									int bytevaluee = bytevalue1e + bytevalue2e;
									int bytevaluef = bytevalue1f + bytevalue2f;
									int bytevalueg = bytevalue1g + bytevalue2g;
									int bytevalueh = bytevalue1h + bytevalue2h;
									longbytes.put(0, (byte)bytevaluea).rewind();
									longbytes.put(1, (byte)bytevalueb).rewind();
									longbytes.put(2, (byte)bytevaluec).rewind();
									longbytes.put(3, (byte)bytevalued).rewind();
									longbytes.put(4, (byte)bytevaluee).rewind();
									longbytes.put(5, (byte)bytevaluef).rewind();
									longbytes.put(6, (byte)bytevalueg).rewind();
									longbytes.put(7, (byte)bytevalueh).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xb9: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int bytevalue1a = longbytes.get(0);
									int bytevalue1b = longbytes.get(1);
									int bytevalue1c = longbytes.get(2);
									int bytevalue1d = longbytes.get(3);
									int bytevalue1e = longbytes.get(4);
									int bytevalue1f = longbytes.get(5);
									int bytevalue1g = longbytes.get(6);
									int bytevalue1h = longbytes.get(7);
									int bytevalue2a = longbytes2.get(0);
									int bytevalue2b = longbytes2.get(1);
									int bytevalue2c = longbytes2.get(2);
									int bytevalue2d = longbytes2.get(3);
									int bytevalue2e = longbytes2.get(4);
									int bytevalue2f = longbytes2.get(5);
									int bytevalue2g = longbytes2.get(6);
									int bytevalue2h = longbytes2.get(7);
									longbytes.putLong(0L).rewind();
									int bytevaluea = bytevalue1a - bytevalue2a;
									int bytevalueb = bytevalue1b - bytevalue2b;
									int bytevaluec = bytevalue1c - bytevalue2c;
									int bytevalued = bytevalue1d - bytevalue2d;
									int bytevaluee = bytevalue1e - bytevalue2e;
									int bytevaluef = bytevalue1f - bytevalue2f;
									int bytevalueg = bytevalue1g - bytevalue2g;
									int bytevalueh = bytevalue1h - bytevalue2h;
									longbytes.put(0, (byte)bytevaluea).rewind();
									longbytes.put(1, (byte)bytevalueb).rewind();
									longbytes.put(2, (byte)bytevaluec).rewind();
									longbytes.put(3, (byte)bytevalued).rewind();
									longbytes.put(4, (byte)bytevaluee).rewind();
									longbytes.put(5, (byte)bytevaluef).rewind();
									longbytes.put(6, (byte)bytevalueg).rewind();
									longbytes.put(7, (byte)bytevalueh).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xc9: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int bytevalue1a = longbytes.get(0);
									int bytevalue1b = longbytes.get(1);
									int bytevalue1c = longbytes.get(2);
									int bytevalue1d = longbytes.get(3);
									int bytevalue1e = longbytes.get(4);
									int bytevalue1f = longbytes.get(5);
									int bytevalue1g = longbytes.get(6);
									int bytevalue1h = longbytes.get(7);
									int bytevalue2a = longbytes2.get(0);
									int bytevalue2b = longbytes2.get(1);
									int bytevalue2c = longbytes2.get(2);
									int bytevalue2d = longbytes2.get(3);
									int bytevalue2e = longbytes2.get(4);
									int bytevalue2f = longbytes2.get(5);
									int bytevalue2g = longbytes2.get(6);
									int bytevalue2h = longbytes2.get(7);
									longbytes.putLong(0L).rewind();
									int bytevaluea = bytevalue1a * bytevalue2a;
									int bytevalueb = bytevalue1b * bytevalue2b;
									int bytevaluec = bytevalue1c * bytevalue2c;
									int bytevalued = bytevalue1d * bytevalue2d;
									int bytevaluee = bytevalue1e * bytevalue2e;
									int bytevaluef = bytevalue1f * bytevalue2f;
									int bytevalueg = bytevalue1g * bytevalue2g;
									int bytevalueh = bytevalue1h * bytevalue2h;
									longbytes.put(0, (byte)bytevaluea).rewind();
									longbytes.put(1, (byte)bytevalueb).rewind();
									longbytes.put(2, (byte)bytevaluec).rewind();
									longbytes.put(3, (byte)bytevalued).rewind();
									longbytes.put(4, (byte)bytevaluee).rewind();
									longbytes.put(5, (byte)bytevaluef).rewind();
									longbytes.put(6, (byte)bytevalueg).rewind();
									longbytes.put(7, (byte)bytevalueh).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xd9: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int bytevalue1a = longbytes.get(0);
									int bytevalue1b = longbytes.get(1);
									int bytevalue1c = longbytes.get(2);
									int bytevalue1d = longbytes.get(3);
									int bytevalue1e = longbytes.get(4);
									int bytevalue1f = longbytes.get(5);
									int bytevalue1g = longbytes.get(6);
									int bytevalue1h = longbytes.get(7);
									int bytevalue2a = longbytes2.get(0);
									int bytevalue2b = longbytes2.get(1);
									int bytevalue2c = longbytes2.get(2);
									int bytevalue2d = longbytes2.get(3);
									int bytevalue2e = longbytes2.get(4);
									int bytevalue2f = longbytes2.get(5);
									int bytevalue2g = longbytes2.get(6);
									int bytevalue2h = longbytes2.get(7);
									longbytes.putLong(0L).rewind();
									int bytevaluea = bytevalue1a / bytevalue2a;
									int bytevalueb = bytevalue1b / bytevalue2b;
									int bytevaluec = bytevalue1c / bytevalue2c;
									int bytevalued = bytevalue1d / bytevalue2d;
									int bytevaluee = bytevalue1e / bytevalue2e;
									int bytevaluef = bytevalue1f / bytevalue2f;
									int bytevalueg = bytevalue1g / bytevalue2g;
									int bytevalueh = bytevalue1h / bytevalue2h;
									longbytes.put(0, (byte)bytevaluea).rewind();
									longbytes.put(1, (byte)bytevalueb).rewind();
									longbytes.put(2, (byte)bytevaluec).rewind();
									longbytes.put(3, (byte)bytevalued).rewind();
									longbytes.put(4, (byte)bytevaluee).rewind();
									longbytes.put(5, (byte)bytevaluef).rewind();
									longbytes.put(6, (byte)bytevalueg).rewind();
									longbytes.put(7, (byte)bytevalueh).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xe9: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int bytevalue1 = -longbytes.get(0);
									int bytevalue2 = -longbytes.get(1);
									int bytevalue3 = -longbytes.get(2);
									int bytevalue4 = -longbytes.get(3);
									int bytevalue5 = -longbytes.get(4);
									int bytevalue6 = -longbytes.get(5);
									int bytevalue7 = -longbytes.get(6);
									int bytevalue8 = -longbytes.get(7);
									longbytes.putLong(0L).rewind();
									longbytes.put(0, (byte)bytevalue1).rewind();
									longbytes.put(1, (byte)bytevalue2).rewind();
									longbytes.put(2, (byte)bytevalue3).rewind();
									longbytes.put(3, (byte)bytevalue4).rewind();
									longbytes.put(4, (byte)bytevalue5).rewind();
									longbytes.put(5, (byte)bytevalue6).rewind();
									longbytes.put(6, (byte)bytevalue7).rewind();
									longbytes.put(7, (byte)bytevalue8).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;

								case 0x0a: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									long longvalue1a = longbytes.getInt(0);
									long longvalue1b = longbytes.getInt(4);
									long longvalue2a = longbytes2.getInt(0);
									long longvalue2b = longbytes2.getInt(4);
									longbytes.putLong(0L).rewind();
									long longvaluea = (longvalue1a + longvalue2a) >> 32;
									long longvalueb = (longvalue1b + longvalue2b) >> 32;
									if (longvaluea!=0) {
										longbytes.putInt(0, (int)1).rewind();
									}
									if (longvalueb!=0) {
										longbytes.putInt(4, (int)1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x1a: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									long longvalue1a = longbytes.getInt(0);
									long longvalue1b = longbytes.getInt(4);
									long longvalue2a = longbytes2.getInt(0);
									long longvalue2b = longbytes2.getInt(4);
									longbytes.putLong(0L).rewind();
									long longvaluea = (longvalue1a - longvalue2a) >> 32;
									long longvalueb = (longvalue1b - longvalue2b) >> 32;
									if (longvaluea!=0) {
										longbytes.putInt(0, (int)1).rewind();
									}
									if (longvalueb!=0) {
										longbytes.putInt(4, (int)1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x2a: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									long longvalue1a = longbytes.getInt(0);
									long longvalue1b = longbytes.getInt(4);
									long longvalue2a = longbytes2.getInt(0);
									long longvalue2b = longbytes2.getInt(4);
									longbytes.putLong(0L).rewind();
									long longvaluea = (longvalue1a * longvalue2a) >> 32;
									long longvalueb = (longvalue1b * longvalue2b) >> 32;
									if (longvaluea!=0) {
										longbytes.putInt(0, (int)longvaluea).rewind();
									}
									if (longvalueb!=0) {
										longbytes.putInt(4, (int)longvalueb).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x3a: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									long longvalue1a = longbytes.getInt(0);
									long longvalue1b = longbytes.getInt(4);
									long longvalue2a = longbytes2.getInt(0);
									long longvalue2b = longbytes2.getInt(4);
									longbytes.putLong(0L).rewind();
									long longvaluea = longvalue1a % longvalue2a;
									long longvalueb = longvalue1b % longvalue2b;
									if (longvaluea!=0) {
										longbytes.putInt(0, (int)longvaluea).rewind();
									}
									if (longvalueb!=0) {
										longbytes.putInt(4, (int)longvalueb).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x4a: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									int intvalue1 = longbytes.getInt(0);
									int intvalue2 = longbytes.getInt(4);
									int changevalue1 = longbytes2.getInt(0);
									int changevalue2 = longbytes2.getInt(4);
									longbytes.putLong(oldregisters[regX+i]).rewind();
									if (changevalue1!=0) {
										longbytes.putInt(0, intvalue1).rewind();
									}
									if (changevalue2!=0) {
										longbytes.putInt(4, intvalue2).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x5a: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									long longvalue1a = longbytes.getShort(0);
									long longvalue1b = longbytes.getShort(2);
									long longvalue1c = longbytes.getShort(4);
									long longvalue1d = longbytes.getShort(6);
									long longvalue2a = longbytes2.getShort(0);
									long longvalue2b = longbytes2.getShort(2);
									long longvalue2c = longbytes2.getShort(4);
									long longvalue2d = longbytes2.getShort(6);
									longbytes.putLong(0L).rewind();
									long longvaluea = (longvalue1a + longvalue2a) >> 16;
									long longvalueb = (longvalue1b + longvalue2b) >> 16;
									long longvaluec = (longvalue1c + longvalue2c) >> 16;
									long longvalued = (longvalue1d + longvalue2d) >> 16;
									if (longvaluea!=0) {
										longbytes.putShort(0, (short)1).rewind();
									}
									if (longvalueb!=0) {
										longbytes.putShort(2, (short)1).rewind();
									}
									if (longvaluec!=0) {
										longbytes.putShort(4, (short)1).rewind();
									}
									if (longvalued!=0) {
										longbytes.putShort(6, (short)1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x6a: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									long longvalue1a = longbytes.getShort(0);
									long longvalue1b = longbytes.getShort(2);
									long longvalue1c = longbytes.getShort(4);
									long longvalue1d = longbytes.getShort(6);
									long longvalue2a = longbytes2.getShort(0);
									long longvalue2b = longbytes2.getShort(2);
									long longvalue2c = longbytes2.getShort(4);
									long longvalue2d = longbytes2.getShort(6);
									longbytes.putLong(0L).rewind();
									long longvaluea = (longvalue1a - longvalue2a) >> 16;
									long longvalueb = (longvalue1b - longvalue2b) >> 16;
									long longvaluec = (longvalue1c - longvalue2c) >> 16;
									long longvalued = (longvalue1d - longvalue2d) >> 16;
									if (longvaluea!=0) {
										longbytes.putShort(0, (short)1).rewind();
									}
									if (longvalueb!=0) {
										longbytes.putShort(2, (short)1).rewind();
									}
									if (longvaluec!=0) {
										longbytes.putShort(4, (short)1).rewind();
									}
									if (longvalued!=0) {
										longbytes.putShort(6, (short)1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x7a: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									long longvalue1a = longbytes.getShort(0);
									long longvalue1b = longbytes.getShort(2);
									long longvalue1c = longbytes.getShort(4);
									long longvalue1d = longbytes.getShort(6);
									long longvalue2a = longbytes2.getShort(0);
									long longvalue2b = longbytes2.getShort(2);
									long longvalue2c = longbytes2.getShort(4);
									long longvalue2d = longbytes2.getShort(6);
									longbytes.putLong(0L).rewind();
									long longvaluea = (longvalue1a * longvalue2a) >> 16;
									long longvalueb = (longvalue1b * longvalue2b) >> 16;
									long longvaluec = (longvalue1c * longvalue2c) >> 16;
									long longvalued = (longvalue1d * longvalue2d) >> 16;
									if (longvaluea!=0) {
										longbytes.putShort(0, (short)longvaluea).rewind();
									}
									if (longvalueb!=0) {
										longbytes.putShort(2, (short)longvalueb).rewind();
									}
									if (longvaluec!=0) {
										longbytes.putShort(4, (short)longvaluec).rewind();
									}
									if (longvalued!=0) {
										longbytes.putShort(6, (short)longvalued).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x8a: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									long longvalue1a = longbytes.getShort(0);
									long longvalue1b = longbytes.getShort(2);
									long longvalue1c = longbytes.getShort(4);
									long longvalue1d = longbytes.getShort(6);
									long longvalue2a = longbytes2.getShort(0);
									long longvalue2b = longbytes2.getShort(2);
									long longvalue2c = longbytes2.getShort(4);
									long longvalue2d = longbytes2.getShort(6);
									longbytes.putLong(0L).rewind();
									long longvaluea = longvalue1a % longvalue2a;
									long longvalueb = longvalue1b % longvalue2b;
									long longvaluec = longvalue1c % longvalue2c;
									long longvalued = longvalue1d % longvalue2d;
									if (longvaluea!=0) {
										longbytes.putShort(0, (short)longvaluea).rewind();
									}
									if (longvalueb!=0) {
										longbytes.putShort(2, (short)longvalueb).rewind();
									}
									if (longvaluec!=0) {
										longbytes.putShort(4, (short)longvaluec).rewind();
									}
									if (longvalued!=0) {
										longbytes.putShort(6, (short)longvalued).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x9a: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									short shortvalue1 = longbytes.getShort(0);
									short shortvalue2 = longbytes.getShort(2);
									short shortvalue3 = longbytes.getShort(4);
									short shortvalue4 = longbytes.getShort(6);
									int changevalue1 = longbytes2.getShort(0);
									int changevalue2 = longbytes2.getShort(2);
									int changevalue3 = longbytes2.getShort(4);
									int changevalue4 = longbytes2.getShort(6);
									longbytes.putLong(oldregisters[regX+i]).rewind();
									if (changevalue1!=0) {
										longbytes.putShort(0, shortvalue1).rewind();
									}
									if (changevalue2!=0) {
										longbytes.putShort(2, shortvalue2).rewind();
									}
									if (changevalue3!=0) {
										longbytes.putShort(4, shortvalue3).rewind();
									}
									if (changevalue4!=0) {
										longbytes.putShort(6, shortvalue4).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xaa: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									long longvalue1a = longbytes.get(0);
									long longvalue1b = longbytes.get(1);
									long longvalue1c = longbytes.get(2);
									long longvalue1d = longbytes.get(3);
									long longvalue1e = longbytes.get(4);
									long longvalue1f = longbytes.get(5);
									long longvalue1g = longbytes.get(6);
									long longvalue1h = longbytes.get(7);
									long longvalue2a = longbytes2.get(0);
									long longvalue2b = longbytes2.get(1);
									long longvalue2c = longbytes2.get(2);
									long longvalue2d = longbytes2.get(3);
									long longvalue2e = longbytes2.get(4);
									long longvalue2f = longbytes2.get(5);
									long longvalue2g = longbytes2.get(6);
									long longvalue2h = longbytes2.get(7);
									longbytes.putLong(0L).rewind();
									long longvaluea = (longvalue1a + longvalue2a) >> 8;
									long longvalueb = (longvalue1b + longvalue2b) >> 8;
									long longvaluec = (longvalue1c + longvalue2c) >> 8;
									long longvalued = (longvalue1d + longvalue2d) >> 8;
									long longvaluee = (longvalue1e + longvalue2e) >> 8;
									long longvaluef = (longvalue1f + longvalue2f) >> 8;
									long longvalueg = (longvalue1g + longvalue2g) >> 8;
									long longvalueh = (longvalue1h + longvalue2h) >> 8;
									if (longvaluea!=0) {
										longbytes.put(0, (byte)1).rewind();
									}
									if (longvalueb!=0) {
										longbytes.put(1, (byte)1).rewind();
									}
									if (longvaluec!=0) {
										longbytes.put(2, (byte)1).rewind();
									}
									if (longvalued!=0) {
										longbytes.put(3, (byte)1).rewind();
									}
									if (longvaluee!=0) {
										longbytes.put(4, (byte)1).rewind();
									}
									if (longvaluef!=0) {
										longbytes.put(5, (byte)1).rewind();
									}
									if (longvalueg!=0) {
										longbytes.put(6, (byte)1).rewind();
									}
									if (longvalueh!=0) {
										longbytes.put(7, (byte)1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xba: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									long longvalue1a = longbytes.get(0);
									long longvalue1b = longbytes.get(1);
									long longvalue1c = longbytes.get(2);
									long longvalue1d = longbytes.get(3);
									long longvalue1e = longbytes.get(4);
									long longvalue1f = longbytes.get(5);
									long longvalue1g = longbytes.get(6);
									long longvalue1h = longbytes.get(7);
									long longvalue2a = longbytes2.get(0);
									long longvalue2b = longbytes2.get(1);
									long longvalue2c = longbytes2.get(2);
									long longvalue2d = longbytes2.get(3);
									long longvalue2e = longbytes2.get(4);
									long longvalue2f = longbytes2.get(5);
									long longvalue2g = longbytes2.get(6);
									long longvalue2h = longbytes2.get(7);
									longbytes.putLong(0L).rewind();
									long longvaluea = (longvalue1a - longvalue2a) >> 8;
									long longvalueb = (longvalue1b - longvalue2b) >> 8;
									long longvaluec = (longvalue1c - longvalue2c) >> 8;
									long longvalued = (longvalue1d - longvalue2d) >> 8;
									long longvaluee = (longvalue1e - longvalue2e) >> 8;
									long longvaluef = (longvalue1f - longvalue2f) >> 8;
									long longvalueg = (longvalue1g - longvalue2g) >> 8;
									long longvalueh = (longvalue1h - longvalue2h) >> 8;
									if (longvaluea!=0) {
										longbytes.put(0, (byte)1).rewind();
									}
									if (longvalueb!=0) {
										longbytes.put(1, (byte)1).rewind();
									}
									if (longvaluec!=0) {
										longbytes.put(2, (byte)1).rewind();
									}
									if (longvalued!=0) {
										longbytes.put(3, (byte)1).rewind();
									}
									if (longvaluee!=0) {
										longbytes.put(4, (byte)1).rewind();
									}
									if (longvaluef!=0) {
										longbytes.put(5, (byte)1).rewind();
									}
									if (longvalueg!=0) {
										longbytes.put(6, (byte)1).rewind();
									}
									if (longvalueh!=0) {
										longbytes.put(7, (byte)1).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xca: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									long longvalue1a = longbytes.get(0);
									long longvalue1b = longbytes.get(1);
									long longvalue1c = longbytes.get(2);
									long longvalue1d = longbytes.get(3);
									long longvalue1e = longbytes.get(4);
									long longvalue1f = longbytes.get(5);
									long longvalue1g = longbytes.get(6);
									long longvalue1h = longbytes.get(7);
									long longvalue2a = longbytes2.get(0);
									long longvalue2b = longbytes2.get(1);
									long longvalue2c = longbytes2.get(2);
									long longvalue2d = longbytes2.get(3);
									long longvalue2e = longbytes2.get(4);
									long longvalue2f = longbytes2.get(5);
									long longvalue2g = longbytes2.get(6);
									long longvalue2h = longbytes2.get(7);
									longbytes.putLong(0L).rewind();
									long longvaluea = (longvalue1a * longvalue2a) >> 8;
									long longvalueb = (longvalue1b * longvalue2b) >> 8;
									long longvaluec = (longvalue1c * longvalue2c) >> 8;
									long longvalued = (longvalue1d * longvalue2d) >> 8;
									long longvaluee = (longvalue1e * longvalue2e) >> 8;
									long longvaluef = (longvalue1f * longvalue2f) >> 8;
									long longvalueg = (longvalue1g * longvalue2g) >> 8;
									long longvalueh = (longvalue1h * longvalue2h) >> 8;
									if (longvaluea!=0) {
										longbytes.put(0, (byte)longvaluea).rewind();
									}
									if (longvalueb!=0) {
										longbytes.put(1, (byte)longvalueb).rewind();
									}
									if (longvaluec!=0) {
										longbytes.put(2, (byte)longvaluec).rewind();
									}
									if (longvalued!=0) {
										longbytes.put(3, (byte)longvalued).rewind();
									}
									if (longvaluee!=0) {
										longbytes.put(4, (byte)longvaluee).rewind();
									}
									if (longvaluef!=0) {
										longbytes.put(5, (byte)longvaluef).rewind();
									}
									if (longvalueg!=0) {
										longbytes.put(6, (byte)longvalueg).rewind();
									}
									if (longvalueh!=0) {
										longbytes.put(7, (byte)longvalueh).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xda: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									long longvalue1a = longbytes.get(0);
									long longvalue1b = longbytes.get(1);
									long longvalue1c = longbytes.get(2);
									long longvalue1d = longbytes.get(3);
									long longvalue1e = longbytes.get(4);
									long longvalue1f = longbytes.get(5);
									long longvalue1g = longbytes.get(6);
									long longvalue1h = longbytes.get(7);
									long longvalue2a = longbytes2.get(0);
									long longvalue2b = longbytes2.get(1);
									long longvalue2c = longbytes2.get(2);
									long longvalue2d = longbytes2.get(3);
									long longvalue2e = longbytes2.get(4);
									long longvalue2f = longbytes2.get(5);
									long longvalue2g = longbytes2.get(6);
									long longvalue2h = longbytes2.get(7);
									longbytes.putLong(0L).rewind();
									long longvaluea = longvalue1a % longvalue2a;
									long longvalueb = longvalue1b % longvalue2b;
									long longvaluec = longvalue1c % longvalue2c;
									long longvalued = longvalue1d % longvalue2d;
									long longvaluee = longvalue1e % longvalue2e;
									long longvaluef = longvalue1f % longvalue2f;
									long longvalueg = longvalue1g % longvalue2g;
									long longvalueh = longvalue1h % longvalue2h;
									if (longvaluea!=0) {
										longbytes.put(0, (byte)longvaluea).rewind();
									}
									if (longvalueb!=0) {
										longbytes.put(1, (byte)longvalueb).rewind();
									}
									if (longvaluec!=0) {
										longbytes.put(2, (byte)longvaluec).rewind();
									}
									if (longvalued!=0) {
										longbytes.put(3, (byte)longvalued).rewind();
									}
									if (longvaluee!=0) {
										longbytes.put(4, (byte)longvaluee).rewind();
									}
									if (longvaluef!=0) {
										longbytes.put(5, (byte)longvaluef).rewind();
									}
									if (longvalueg!=0) {
										longbytes.put(6, (byte)longvalueg).rewind();
									}
									if (longvalueh!=0) {
										longbytes.put(7, (byte)longvalueh).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xea: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									byte bytevalue1 = longbytes.get(0);
									byte bytevalue2 = longbytes.get(1);
									byte bytevalue3 = longbytes.get(2);
									byte bytevalue4 = longbytes.get(3);
									byte bytevalue5 = longbytes.get(4);
									byte bytevalue6 = longbytes.get(5);
									byte bytevalue7 = longbytes.get(6);
									byte bytevalue8 = longbytes.get(7);
									int changevalue1 = longbytes2.get(0);
									int changevalue2 = longbytes2.get(1);
									int changevalue3 = longbytes2.get(2);
									int changevalue4 = longbytes2.get(3);
									int changevalue5 = longbytes2.get(4);
									int changevalue6 = longbytes2.get(5);
									int changevalue7 = longbytes2.get(6);
									int changevalue8 = longbytes2.get(7);
									longbytes.putLong(oldregisters[regX+i]).rewind();
									if (changevalue1!=0) {
										longbytes.put(0, bytevalue1).rewind();
									}
									if (changevalue2!=0) {
										longbytes.put(1, bytevalue2).rewind();
									}
									if (changevalue3!=0) {
										longbytes.put(2, bytevalue3).rewind();
									}
									if (changevalue4!=0) {
										longbytes.put(3, bytevalue4).rewind();
									}
									if (changevalue5!=0) {
										longbytes.put(4, bytevalue5).rewind();
									}
									if (changevalue6!=0) {
										longbytes.put(5, bytevalue6).rewind();
									}
									if (changevalue7!=0) {
										longbytes.put(6, bytevalue7).rewind();
									}
									if (changevalue8!=0) {
										longbytes.put(7, bytevalue8).rewind();
									}
									newregisters[regX+i] = longbytes.getLong();
								} break;

								case 0x0b: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									long longvalue1 = longbytes.getInt(0);
									long longvalue2 = longbytes.getInt(4);
									longbytes.putLong(0L).rewind();
									long[] regyarray1 = {longvalue1};
									long[] regyarray2 = {longvalue2};
									BitSet regybits1 = BitSet.valueOf(regyarray1);
									BitSet regybits2 = BitSet.valueOf(regyarray2);
									int intvalue1 = regybits1.nextSetBit(0);
									int intvalue2 = regybits2.nextSetBit(0);
									longbytes.putInt(0, intvalue1).rewind();
									longbytes.putInt(4, intvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x1b: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									long longvalue1 = longbytes.getInt(0);
									long longvalue2 = longbytes.getInt(4);
									longbytes.putLong(0L).rewind();
									long[] regyarray1 = {longvalue1};
									long[] regyarray2 = {longvalue2};
									BitSet regybits1 = BitSet.valueOf(regyarray1);
									BitSet regybits2 = BitSet.valueOf(regyarray2);
									int intvalue1 = regybits1.previousSetBit(31);
									int intvalue2 = regybits2.previousSetBit(31);
									longbytes.putInt(0, intvalue1).rewind();
									longbytes.putInt(4, intvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x2b: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									long longvalue1 = longbytes.getInt(0);
									long longvalue2 = longbytes.getInt(4);
									longbytes.putLong(0L).rewind();
									long[] regyarray1 = {longvalue1};
									long[] regyarray2 = {longvalue2};
									BitSet regybits1 = BitSet.valueOf(regyarray1);
									BitSet regybits2 = BitSet.valueOf(regyarray2);
									int intvalue1 = regybits1.nextClearBit(0);
									int intvalue2 = regybits2.nextClearBit(0);
									longbytes.putInt(0, intvalue1).rewind();
									longbytes.putInt(4, intvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x3b: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									long longvalue1 = longbytes.getInt(0);
									long longvalue2 = longbytes.getInt(4);
									longbytes.putLong(0L).rewind();
									long[] regyarray1 = {longvalue1};
									long[] regyarray2 = {longvalue2};
									BitSet regybits1 = BitSet.valueOf(regyarray1);
									BitSet regybits2 = BitSet.valueOf(regyarray2);
									int intvalue1 = regybits1.previousClearBit(31);
									int intvalue2 = regybits2.previousClearBit(31);
									longbytes.putInt(0, intvalue1).rewind();
									longbytes.putInt(4, intvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x4b: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									long longvalue1 = longbytes.getInt(0);
									long longvalue2 = longbytes.getInt(4);
									longbytes.putLong(0L).rewind();
									long[] regyarray1 = {longvalue1};
									long[] regyarray2 = {longvalue2};
									BitSet regybits1 = BitSet.valueOf(regyarray1);
									BitSet regybits2 = BitSet.valueOf(regyarray2);
									int intvalue1 = regybits1.cardinality();
									int intvalue2 = regybits2.cardinality();
									longbytes.putInt(0, intvalue1).rewind();
									longbytes.putInt(4, intvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x5b: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									long longvalue1 = longbytes.getShort(0);
									long longvalue2 = longbytes.getShort(2);
									long longvalue3 = longbytes.getShort(4);
									long longvalue4 = longbytes.getShort(6);
									longbytes.putLong(0L).rewind();
									long[] regyarray1 = {longvalue1};
									long[] regyarray2 = {longvalue2};
									long[] regyarray3 = {longvalue3};
									long[] regyarray4 = {longvalue4};
									BitSet regybits1 = BitSet.valueOf(regyarray1);
									BitSet regybits2 = BitSet.valueOf(regyarray2);
									BitSet regybits3 = BitSet.valueOf(regyarray3);
									BitSet regybits4 = BitSet.valueOf(regyarray4);
									int intvalue1 = regybits1.nextSetBit(0);
									int intvalue2 = regybits2.nextSetBit(0);
									int intvalue3 = regybits3.nextSetBit(0);
									int intvalue4 = regybits4.nextSetBit(0);
									longbytes.putShort(0, (short)intvalue1).rewind();
									longbytes.putShort(2, (short)intvalue2).rewind();
									longbytes.putShort(4, (short)intvalue3).rewind();
									longbytes.putShort(6, (short)intvalue4).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x6b: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									long longvalue1 = longbytes.getShort(0);
									long longvalue2 = longbytes.getShort(2);
									long longvalue3 = longbytes.getShort(4);
									long longvalue4 = longbytes.getShort(6);
									longbytes.putLong(0L).rewind();
									long[] regyarray1 = {longvalue1};
									long[] regyarray2 = {longvalue2};
									long[] regyarray3 = {longvalue3};
									long[] regyarray4 = {longvalue4};
									BitSet regybits1 = BitSet.valueOf(regyarray1);
									BitSet regybits2 = BitSet.valueOf(regyarray2);
									BitSet regybits3 = BitSet.valueOf(regyarray3);
									BitSet regybits4 = BitSet.valueOf(regyarray4);
									int intvalue1 = regybits1.previousSetBit(15);
									int intvalue2 = regybits2.previousSetBit(15);
									int intvalue3 = regybits3.previousSetBit(15);
									int intvalue4 = regybits4.previousSetBit(15);
									longbytes.putShort(0, (short)intvalue1).rewind();
									longbytes.putShort(2, (short)intvalue2).rewind();
									longbytes.putShort(4, (short)intvalue3).rewind();
									longbytes.putShort(6, (short)intvalue4).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x7b: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									long longvalue1 = longbytes.getShort(0);
									long longvalue2 = longbytes.getShort(2);
									long longvalue3 = longbytes.getShort(4);
									long longvalue4 = longbytes.getShort(6);
									longbytes.putLong(0L).rewind();
									long[] regyarray1 = {longvalue1};
									long[] regyarray2 = {longvalue2};
									long[] regyarray3 = {longvalue3};
									long[] regyarray4 = {longvalue4};
									BitSet regybits1 = BitSet.valueOf(regyarray1);
									BitSet regybits2 = BitSet.valueOf(regyarray2);
									BitSet regybits3 = BitSet.valueOf(regyarray3);
									BitSet regybits4 = BitSet.valueOf(regyarray4);
									int intvalue1 = regybits1.nextClearBit(0);
									int intvalue2 = regybits2.nextClearBit(0);
									int intvalue3 = regybits3.nextClearBit(0);
									int intvalue4 = regybits4.nextClearBit(0);
									longbytes.putShort(0, (short)intvalue1).rewind();
									longbytes.putShort(2, (short)intvalue2).rewind();
									longbytes.putShort(4, (short)intvalue3).rewind();
									longbytes.putShort(6, (short)intvalue4).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x8b: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									long longvalue1 = longbytes.getShort(0);
									long longvalue2 = longbytes.getShort(2);
									long longvalue3 = longbytes.getShort(4);
									long longvalue4 = longbytes.getShort(6);
									longbytes.putLong(0L).rewind();
									long[] regyarray1 = {longvalue1};
									long[] regyarray2 = {longvalue2};
									long[] regyarray3 = {longvalue3};
									long[] regyarray4 = {longvalue4};
									BitSet regybits1 = BitSet.valueOf(regyarray1);
									BitSet regybits2 = BitSet.valueOf(regyarray2);
									BitSet regybits3 = BitSet.valueOf(regyarray3);
									BitSet regybits4 = BitSet.valueOf(regyarray4);
									int intvalue1 = regybits1.previousClearBit(15);
									int intvalue2 = regybits2.previousClearBit(15);
									int intvalue3 = regybits3.previousClearBit(15);
									int intvalue4 = regybits4.previousClearBit(15);
									longbytes.putShort(0, (short)intvalue1).rewind();
									longbytes.putShort(2, (short)intvalue2).rewind();
									longbytes.putShort(4, (short)intvalue3).rewind();
									longbytes.putShort(6, (short)intvalue4).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x9b: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									long longvalue1 = longbytes.getShort(0);
									long longvalue2 = longbytes.getShort(2);
									long longvalue3 = longbytes.getShort(4);
									long longvalue4 = longbytes.getShort(6);
									longbytes.putLong(0L).rewind();
									long[] regyarray1 = {longvalue1};
									long[] regyarray2 = {longvalue2};
									long[] regyarray3 = {longvalue3};
									long[] regyarray4 = {longvalue4};
									BitSet regybits1 = BitSet.valueOf(regyarray1);
									BitSet regybits2 = BitSet.valueOf(regyarray2);
									BitSet regybits3 = BitSet.valueOf(regyarray3);
									BitSet regybits4 = BitSet.valueOf(regyarray4);
									int intvalue1 = regybits1.cardinality();
									int intvalue2 = regybits2.cardinality();
									int intvalue3 = regybits3.cardinality();
									int intvalue4 = regybits4.cardinality();
									longbytes.putShort(0, (short)intvalue1).rewind();
									longbytes.putShort(2, (short)intvalue2).rewind();
									longbytes.putShort(4, (short)intvalue3).rewind();
									longbytes.putShort(6, (short)intvalue4).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xab: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									long longvalue1 = longbytes.get(0);
									long longvalue2 = longbytes.get(1);
									long longvalue3 = longbytes.get(2);
									long longvalue4 = longbytes.get(3);
									long longvalue5 = longbytes.get(4);
									long longvalue6 = longbytes.get(5);
									long longvalue7 = longbytes.get(6);
									long longvalue8 = longbytes.get(7);
									longbytes.putLong(0L).rewind();
									long[] regyarray1 = {longvalue1};
									long[] regyarray2 = {longvalue2};
									long[] regyarray3 = {longvalue3};
									long[] regyarray4 = {longvalue4};
									long[] regyarray5 = {longvalue5};
									long[] regyarray6 = {longvalue6};
									long[] regyarray7 = {longvalue7};
									long[] regyarray8 = {longvalue8};
									BitSet regybits1 = BitSet.valueOf(regyarray1);
									BitSet regybits2 = BitSet.valueOf(regyarray2);
									BitSet regybits3 = BitSet.valueOf(regyarray3);
									BitSet regybits4 = BitSet.valueOf(regyarray4);
									BitSet regybits5 = BitSet.valueOf(regyarray5);
									BitSet regybits6 = BitSet.valueOf(regyarray6);
									BitSet regybits7 = BitSet.valueOf(regyarray7);
									BitSet regybits8 = BitSet.valueOf(regyarray8);
									int intvalue1 = regybits1.nextSetBit(0);
									int intvalue2 = regybits2.nextSetBit(0);
									int intvalue3 = regybits3.nextSetBit(0);
									int intvalue4 = regybits4.nextSetBit(0);
									int intvalue5 = regybits5.nextSetBit(0);
									int intvalue6 = regybits6.nextSetBit(0);
									int intvalue7 = regybits7.nextSetBit(0);
									int intvalue8 = regybits8.nextSetBit(0);
									longbytes.put(0, (byte)intvalue1).rewind();
									longbytes.put(1, (byte)intvalue2).rewind();
									longbytes.put(2, (byte)intvalue3).rewind();
									longbytes.put(3, (byte)intvalue4).rewind();
									longbytes.put(4, (byte)intvalue5).rewind();
									longbytes.put(5, (byte)intvalue6).rewind();
									longbytes.put(6, (byte)intvalue7).rewind();
									longbytes.put(7, (byte)intvalue8).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xbb: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									long longvalue1 = longbytes.get(0);
									long longvalue2 = longbytes.get(1);
									long longvalue3 = longbytes.get(2);
									long longvalue4 = longbytes.get(3);
									long longvalue5 = longbytes.get(4);
									long longvalue6 = longbytes.get(5);
									long longvalue7 = longbytes.get(6);
									long longvalue8 = longbytes.get(7);
									longbytes.putLong(0L).rewind();
									long[] regyarray1 = {longvalue1};
									long[] regyarray2 = {longvalue2};
									long[] regyarray3 = {longvalue3};
									long[] regyarray4 = {longvalue4};
									long[] regyarray5 = {longvalue5};
									long[] regyarray6 = {longvalue6};
									long[] regyarray7 = {longvalue7};
									long[] regyarray8 = {longvalue8};
									BitSet regybits1 = BitSet.valueOf(regyarray1);
									BitSet regybits2 = BitSet.valueOf(regyarray2);
									BitSet regybits3 = BitSet.valueOf(regyarray3);
									BitSet regybits4 = BitSet.valueOf(regyarray4);
									BitSet regybits5 = BitSet.valueOf(regyarray5);
									BitSet regybits6 = BitSet.valueOf(regyarray6);
									BitSet regybits7 = BitSet.valueOf(regyarray7);
									BitSet regybits8 = BitSet.valueOf(regyarray8);
									int intvalue1 = regybits1.previousSetBit(7);
									int intvalue2 = regybits2.previousSetBit(7);
									int intvalue3 = regybits3.previousSetBit(7);
									int intvalue4 = regybits4.previousSetBit(7);
									int intvalue5 = regybits5.previousSetBit(7);
									int intvalue6 = regybits6.previousSetBit(7);
									int intvalue7 = regybits7.previousSetBit(7);
									int intvalue8 = regybits8.previousSetBit(7);
									longbytes.put(0, (byte)intvalue1).rewind();
									longbytes.put(1, (byte)intvalue2).rewind();
									longbytes.put(2, (byte)intvalue3).rewind();
									longbytes.put(3, (byte)intvalue4).rewind();
									longbytes.put(4, (byte)intvalue5).rewind();
									longbytes.put(5, (byte)intvalue6).rewind();
									longbytes.put(6, (byte)intvalue7).rewind();
									longbytes.put(7, (byte)intvalue8).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xcb: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									long longvalue1 = longbytes.get(0);
									long longvalue2 = longbytes.get(1);
									long longvalue3 = longbytes.get(2);
									long longvalue4 = longbytes.get(3);
									long longvalue5 = longbytes.get(4);
									long longvalue6 = longbytes.get(5);
									long longvalue7 = longbytes.get(6);
									long longvalue8 = longbytes.get(7);
									longbytes.putLong(0L).rewind();
									long[] regyarray1 = {longvalue1};
									long[] regyarray2 = {longvalue2};
									long[] regyarray3 = {longvalue3};
									long[] regyarray4 = {longvalue4};
									long[] regyarray5 = {longvalue5};
									long[] regyarray6 = {longvalue6};
									long[] regyarray7 = {longvalue7};
									long[] regyarray8 = {longvalue8};
									BitSet regybits1 = BitSet.valueOf(regyarray1);
									BitSet regybits2 = BitSet.valueOf(regyarray2);
									BitSet regybits3 = BitSet.valueOf(regyarray3);
									BitSet regybits4 = BitSet.valueOf(regyarray4);
									BitSet regybits5 = BitSet.valueOf(regyarray5);
									BitSet regybits6 = BitSet.valueOf(regyarray6);
									BitSet regybits7 = BitSet.valueOf(regyarray7);
									BitSet regybits8 = BitSet.valueOf(regyarray8);
									int intvalue1 = regybits1.nextClearBit(0);
									int intvalue2 = regybits2.nextClearBit(0);
									int intvalue3 = regybits3.nextClearBit(0);
									int intvalue4 = regybits4.nextClearBit(0);
									int intvalue5 = regybits5.nextClearBit(0);
									int intvalue6 = regybits6.nextClearBit(0);
									int intvalue7 = regybits7.nextClearBit(0);
									int intvalue8 = regybits8.nextClearBit(0);
									longbytes.put(0, (byte)intvalue1).rewind();
									longbytes.put(1, (byte)intvalue2).rewind();
									longbytes.put(2, (byte)intvalue3).rewind();
									longbytes.put(3, (byte)intvalue4).rewind();
									longbytes.put(4, (byte)intvalue5).rewind();
									longbytes.put(5, (byte)intvalue6).rewind();
									longbytes.put(6, (byte)intvalue7).rewind();
									longbytes.put(7, (byte)intvalue8).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xdb: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									long longvalue1 = longbytes.get(0);
									long longvalue2 = longbytes.get(1);
									long longvalue3 = longbytes.get(2);
									long longvalue4 = longbytes.get(3);
									long longvalue5 = longbytes.get(4);
									long longvalue6 = longbytes.get(5);
									long longvalue7 = longbytes.get(6);
									long longvalue8 = longbytes.get(7);
									longbytes.putLong(0L).rewind();
									long[] regyarray1 = {longvalue1};
									long[] regyarray2 = {longvalue2};
									long[] regyarray3 = {longvalue3};
									long[] regyarray4 = {longvalue4};
									long[] regyarray5 = {longvalue5};
									long[] regyarray6 = {longvalue6};
									long[] regyarray7 = {longvalue7};
									long[] regyarray8 = {longvalue8};
									BitSet regybits1 = BitSet.valueOf(regyarray1);
									BitSet regybits2 = BitSet.valueOf(regyarray2);
									BitSet regybits3 = BitSet.valueOf(regyarray3);
									BitSet regybits4 = BitSet.valueOf(regyarray4);
									BitSet regybits5 = BitSet.valueOf(regyarray5);
									BitSet regybits6 = BitSet.valueOf(regyarray6);
									BitSet regybits7 = BitSet.valueOf(regyarray7);
									BitSet regybits8 = BitSet.valueOf(regyarray8);
									int intvalue1 = regybits1.previousClearBit(7);
									int intvalue2 = regybits2.previousClearBit(7);
									int intvalue3 = regybits3.previousClearBit(7);
									int intvalue4 = regybits4.previousClearBit(7);
									int intvalue5 = regybits5.previousClearBit(7);
									int intvalue6 = regybits6.previousClearBit(7);
									int intvalue7 = regybits7.previousClearBit(7);
									int intvalue8 = regybits8.previousClearBit(7);
									longbytes.put(0, (byte)intvalue1).rewind();
									longbytes.put(1, (byte)intvalue2).rewind();
									longbytes.put(2, (byte)intvalue3).rewind();
									longbytes.put(3, (byte)intvalue4).rewind();
									longbytes.put(4, (byte)intvalue5).rewind();
									longbytes.put(5, (byte)intvalue6).rewind();
									longbytes.put(6, (byte)intvalue7).rewind();
									longbytes.put(7, (byte)intvalue8).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xeb: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									long longvalue1 = longbytes.get(0);
									long longvalue2 = longbytes.get(1);
									long longvalue3 = longbytes.get(2);
									long longvalue4 = longbytes.get(3);
									long longvalue5 = longbytes.get(4);
									long longvalue6 = longbytes.get(5);
									long longvalue7 = longbytes.get(6);
									long longvalue8 = longbytes.get(7);
									longbytes.putLong(0L).rewind();
									long[] regyarray1 = {longvalue1};
									long[] regyarray2 = {longvalue2};
									long[] regyarray3 = {longvalue3};
									long[] regyarray4 = {longvalue4};
									long[] regyarray5 = {longvalue5};
									long[] regyarray6 = {longvalue6};
									long[] regyarray7 = {longvalue7};
									long[] regyarray8 = {longvalue8};
									BitSet regybits1 = BitSet.valueOf(regyarray1);
									BitSet regybits2 = BitSet.valueOf(regyarray2);
									BitSet regybits3 = BitSet.valueOf(regyarray3);
									BitSet regybits4 = BitSet.valueOf(regyarray4);
									BitSet regybits5 = BitSet.valueOf(regyarray5);
									BitSet regybits6 = BitSet.valueOf(regyarray6);
									BitSet regybits7 = BitSet.valueOf(regyarray7);
									BitSet regybits8 = BitSet.valueOf(regyarray8);
									int intvalue1 = regybits1.cardinality();
									int intvalue2 = regybits2.cardinality();
									int intvalue3 = regybits3.cardinality();
									int intvalue4 = regybits4.cardinality();
									int intvalue5 = regybits5.cardinality();
									int intvalue6 = regybits6.cardinality();
									int intvalue7 = regybits7.cardinality();
									int intvalue8 = regybits8.cardinality();
									longbytes.put(0, (byte)intvalue1).rewind();
									longbytes.put(1, (byte)intvalue2).rewind();
									longbytes.put(2, (byte)intvalue3).rewind();
									longbytes.put(3, (byte)intvalue4).rewind();
									longbytes.put(4, (byte)intvalue5).rewind();
									longbytes.put(5, (byte)intvalue6).rewind();
									longbytes.put(6, (byte)intvalue7).rewind();
									longbytes.put(7, (byte)intvalue8).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								
								case 0x0c: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = longbytes.getFloat(0);
									float floatvalue1b = longbytes.getFloat(4);
									float floatvalue2a = longbytes2.getFloat(0);
									float floatvalue2b = longbytes2.getFloat(4);
									longbytes.putLong(0L).rewind();
									float floatvaluea = floatvalue1a + floatvalue2a;
									float floatvalueb = floatvalue1b + floatvalue2b;
									longbytes.putFloat(0, floatvaluea).rewind();
									longbytes.putFloat(4, floatvalueb).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x1c: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = longbytes.getFloat(0);
									float floatvalue1b = longbytes.getFloat(4);
									float floatvalue2a = longbytes2.getFloat(0);
									float floatvalue2b = longbytes2.getFloat(4);
									longbytes.putLong(0L).rewind();
									float floatvaluea = floatvalue1a - floatvalue2a;
									float floatvalueb = floatvalue1b - floatvalue2b;
									longbytes.putFloat(0, floatvaluea).rewind();
									longbytes.putFloat(4, floatvalueb).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x2c: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = longbytes.getFloat(0);
									float floatvalue1b = longbytes.getFloat(4);
									float floatvalue2a = longbytes2.getFloat(0);
									float floatvalue2b = longbytes2.getFloat(4);
									longbytes.putLong(0L).rewind();
									float floatvaluea = floatvalue1a * floatvalue2a;
									float floatvalueb = floatvalue1b * floatvalue2b;
									longbytes.putFloat(0, floatvaluea).rewind();
									longbytes.putFloat(4, floatvalueb).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x3c: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = longbytes.getFloat(0);
									float floatvalue1b = longbytes.getFloat(4);
									float floatvalue2a = longbytes2.getFloat(0);
									float floatvalue2b = longbytes2.getFloat(4);
									longbytes.putLong(0L).rewind();
									float floatvaluea = floatvalue1a / floatvalue2a;
									float floatvalueb = floatvalue1b / floatvalue2b;
									longbytes.putFloat(0, floatvaluea).rewind();
									longbytes.putFloat(4, floatvalueb).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x4c: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1 = -longbytes.getFloat(0);
									float floatvalue2 = -longbytes.getFloat(4);
									longbytes.putLong(0L).rewind();
									longbytes.putFloat(0, floatvalue1).rewind();
									longbytes.putFloat(4, floatvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x5c: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = halftofloat(longbytes.getShort(0));
									float floatvalue1b = halftofloat(longbytes.getShort(2));
									float floatvalue1c = halftofloat(longbytes.getShort(4));
									float floatvalue1d = halftofloat(longbytes.getShort(6));
									float floatvalue2a = halftofloat(longbytes2.getShort(0));
									float floatvalue2b = halftofloat(longbytes2.getShort(2));
									float floatvalue2c = halftofloat(longbytes2.getShort(4));
									float floatvalue2d = halftofloat(longbytes2.getShort(6));
									longbytes.putLong(0L).rewind();
									float floatvaluea = floatvalue1a + floatvalue2a;
									float floatvalueb = floatvalue1b + floatvalue2b;
									float floatvaluec = floatvalue1c + floatvalue2c;
									float floatvalued = floatvalue1d + floatvalue2d;
									longbytes.putShort(0, floattohalf(floatvaluea)).rewind();
									longbytes.putShort(2, floattohalf(floatvalueb)).rewind();
									longbytes.putShort(4, floattohalf(floatvaluec)).rewind();
									longbytes.putShort(6, floattohalf(floatvalued)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x6c: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = halftofloat(longbytes.getShort(0));
									float floatvalue1b = halftofloat(longbytes.getShort(2));
									float floatvalue1c = halftofloat(longbytes.getShort(4));
									float floatvalue1d = halftofloat(longbytes.getShort(6));
									float floatvalue2a = halftofloat(longbytes2.getShort(0));
									float floatvalue2b = halftofloat(longbytes2.getShort(2));
									float floatvalue2c = halftofloat(longbytes2.getShort(4));
									float floatvalue2d = halftofloat(longbytes2.getShort(6));
									longbytes.putLong(0L).rewind();
									float floatvaluea = floatvalue1a - floatvalue2a;
									float floatvalueb = floatvalue1b - floatvalue2b;
									float floatvaluec = floatvalue1c - floatvalue2c;
									float floatvalued = floatvalue1d - floatvalue2d;
									longbytes.putShort(0, floattohalf(floatvaluea)).rewind();
									longbytes.putShort(2, floattohalf(floatvalueb)).rewind();
									longbytes.putShort(4, floattohalf(floatvaluec)).rewind();
									longbytes.putShort(6, floattohalf(floatvalued)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x7c: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = halftofloat(longbytes.getShort(0));
									float floatvalue1b = halftofloat(longbytes.getShort(2));
									float floatvalue1c = halftofloat(longbytes.getShort(4));
									float floatvalue1d = halftofloat(longbytes.getShort(6));
									float floatvalue2a = halftofloat(longbytes2.getShort(0));
									float floatvalue2b = halftofloat(longbytes2.getShort(2));
									float floatvalue2c = halftofloat(longbytes2.getShort(4));
									float floatvalue2d = halftofloat(longbytes2.getShort(6));
									longbytes.putLong(0L).rewind();
									float floatvaluea = floatvalue1a * floatvalue2a;
									float floatvalueb = floatvalue1b * floatvalue2b;
									float floatvaluec = floatvalue1c * floatvalue2c;
									float floatvalued = floatvalue1d * floatvalue2d;
									longbytes.putShort(0, floattohalf(floatvaluea)).rewind();
									longbytes.putShort(2, floattohalf(floatvalueb)).rewind();
									longbytes.putShort(4, floattohalf(floatvaluec)).rewind();
									longbytes.putShort(6, floattohalf(floatvalued)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x8c: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = halftofloat(longbytes.getShort(0));
									float floatvalue1b = halftofloat(longbytes.getShort(2));
									float floatvalue1c = halftofloat(longbytes.getShort(4));
									float floatvalue1d = halftofloat(longbytes.getShort(6));
									float floatvalue2a = halftofloat(longbytes2.getShort(0));
									float floatvalue2b = halftofloat(longbytes2.getShort(2));
									float floatvalue2c = halftofloat(longbytes2.getShort(4));
									float floatvalue2d = halftofloat(longbytes2.getShort(6));
									longbytes.putLong(0L).rewind();
									float floatvaluea = floatvalue1a / floatvalue2a;
									float floatvalueb = floatvalue1b / floatvalue2b;
									float floatvaluec = floatvalue1c / floatvalue2c;
									float floatvalued = floatvalue1d / floatvalue2d;
									longbytes.putShort(0, floattohalf(floatvaluea)).rewind();
									longbytes.putShort(2, floattohalf(floatvalueb)).rewind();
									longbytes.putShort(4, floattohalf(floatvaluec)).rewind();
									longbytes.putShort(6, floattohalf(floatvalued)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x9c: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1 = -halftofloat(longbytes.getShort(0));
									float floatvalue2 = -halftofloat(longbytes.getShort(2));
									float floatvalue3 = -halftofloat(longbytes.getShort(4));
									float floatvalue4 = -halftofloat(longbytes.getShort(6));
									longbytes.putLong(0L).rewind();
									longbytes.putShort(0, floattohalf(floatvalue1)).rewind();
									longbytes.putShort(2, floattohalf(floatvalue2)).rewind();
									longbytes.putShort(4, floattohalf(floatvalue3)).rewind();
									longbytes.putShort(6, floattohalf(floatvalue4)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xac: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = minitofloat(longbytes.get(0));
									float floatvalue1b = minitofloat(longbytes.get(1));
									float floatvalue1c = minitofloat(longbytes.get(2));
									float floatvalue1d = minitofloat(longbytes.get(3));
									float floatvalue1e = minitofloat(longbytes.get(4));
									float floatvalue1f = minitofloat(longbytes.get(5));
									float floatvalue1g = minitofloat(longbytes.get(6));
									float floatvalue1h = minitofloat(longbytes.get(7));
									float floatvalue2a = minitofloat(longbytes2.get(0));
									float floatvalue2b = minitofloat(longbytes2.get(1));
									float floatvalue2c = minitofloat(longbytes2.get(2));
									float floatvalue2d = minitofloat(longbytes2.get(3));
									float floatvalue2e = minitofloat(longbytes2.get(4));
									float floatvalue2f = minitofloat(longbytes2.get(5));
									float floatvalue2g = minitofloat(longbytes2.get(6));
									float floatvalue2h = minitofloat(longbytes2.get(7));
									longbytes.putLong(0L).rewind();
									float floatvaluea = floatvalue1a + floatvalue2a;
									float floatvalueb = floatvalue1b + floatvalue2b;
									float floatvaluec = floatvalue1c + floatvalue2c;
									float floatvalued = floatvalue1d + floatvalue2d;
									float floatvaluee = floatvalue1e + floatvalue2e;
									float floatvaluef = floatvalue1f + floatvalue2f;
									float floatvalueg = floatvalue1g + floatvalue2g;
									float floatvalueh = floatvalue1h + floatvalue2h;
									longbytes.put(0, floattomini(floatvaluea)).rewind();
									longbytes.put(1, floattomini(floatvalueb)).rewind();
									longbytes.put(2, floattomini(floatvaluec)).rewind();
									longbytes.put(3, floattomini(floatvalued)).rewind();
									longbytes.put(4, floattomini(floatvaluee)).rewind();
									longbytes.put(5, floattomini(floatvaluef)).rewind();
									longbytes.put(6, floattomini(floatvalueg)).rewind();
									longbytes.put(7, floattomini(floatvalueh)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xbc: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = minitofloat(longbytes.get(0));
									float floatvalue1b = minitofloat(longbytes.get(1));
									float floatvalue1c = minitofloat(longbytes.get(2));
									float floatvalue1d = minitofloat(longbytes.get(3));
									float floatvalue1e = minitofloat(longbytes.get(4));
									float floatvalue1f = minitofloat(longbytes.get(5));
									float floatvalue1g = minitofloat(longbytes.get(6));
									float floatvalue1h = minitofloat(longbytes.get(7));
									float floatvalue2a = minitofloat(longbytes2.get(0));
									float floatvalue2b = minitofloat(longbytes2.get(1));
									float floatvalue2c = minitofloat(longbytes2.get(2));
									float floatvalue2d = minitofloat(longbytes2.get(3));
									float floatvalue2e = minitofloat(longbytes2.get(4));
									float floatvalue2f = minitofloat(longbytes2.get(5));
									float floatvalue2g = minitofloat(longbytes2.get(6));
									float floatvalue2h = minitofloat(longbytes2.get(7));
									longbytes.putLong(0L).rewind();
									float floatvaluea = floatvalue1a - floatvalue2a;
									float floatvalueb = floatvalue1b - floatvalue2b;
									float floatvaluec = floatvalue1c - floatvalue2c;
									float floatvalued = floatvalue1d - floatvalue2d;
									float floatvaluee = floatvalue1e - floatvalue2e;
									float floatvaluef = floatvalue1f - floatvalue2f;
									float floatvalueg = floatvalue1g - floatvalue2g;
									float floatvalueh = floatvalue1h - floatvalue2h;
									longbytes.put(0, floattomini(floatvaluea)).rewind();
									longbytes.put(1, floattomini(floatvalueb)).rewind();
									longbytes.put(2, floattomini(floatvaluec)).rewind();
									longbytes.put(3, floattomini(floatvalued)).rewind();
									longbytes.put(4, floattomini(floatvaluee)).rewind();
									longbytes.put(5, floattomini(floatvaluef)).rewind();
									longbytes.put(6, floattomini(floatvalueg)).rewind();
									longbytes.put(7, floattomini(floatvalueh)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xcc: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = minitofloat(longbytes.get(0));
									float floatvalue1b = minitofloat(longbytes.get(1));
									float floatvalue1c = minitofloat(longbytes.get(2));
									float floatvalue1d = minitofloat(longbytes.get(3));
									float floatvalue1e = minitofloat(longbytes.get(4));
									float floatvalue1f = minitofloat(longbytes.get(5));
									float floatvalue1g = minitofloat(longbytes.get(6));
									float floatvalue1h = minitofloat(longbytes.get(7));
									float floatvalue2a = minitofloat(longbytes2.get(0));
									float floatvalue2b = minitofloat(longbytes2.get(1));
									float floatvalue2c = minitofloat(longbytes2.get(2));
									float floatvalue2d = minitofloat(longbytes2.get(3));
									float floatvalue2e = minitofloat(longbytes2.get(4));
									float floatvalue2f = minitofloat(longbytes2.get(5));
									float floatvalue2g = minitofloat(longbytes2.get(6));
									float floatvalue2h = minitofloat(longbytes2.get(7));
									longbytes.putLong(0L).rewind();
									float floatvaluea = floatvalue1a * floatvalue2a;
									float floatvalueb = floatvalue1b * floatvalue2b;
									float floatvaluec = floatvalue1c * floatvalue2c;
									float floatvalued = floatvalue1d * floatvalue2d;
									float floatvaluee = floatvalue1e * floatvalue2e;
									float floatvaluef = floatvalue1f * floatvalue2f;
									float floatvalueg = floatvalue1g * floatvalue2g;
									float floatvalueh = floatvalue1h * floatvalue2h;
									longbytes.put(0, floattomini(floatvaluea)).rewind();
									longbytes.put(1, floattomini(floatvalueb)).rewind();
									longbytes.put(2, floattomini(floatvaluec)).rewind();
									longbytes.put(3, floattomini(floatvalued)).rewind();
									longbytes.put(4, floattomini(floatvaluee)).rewind();
									longbytes.put(5, floattomini(floatvaluef)).rewind();
									longbytes.put(6, floattomini(floatvalueg)).rewind();
									longbytes.put(7, floattomini(floatvalueh)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xdc: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = minitofloat(longbytes.get(0));
									float floatvalue1b = minitofloat(longbytes.get(1));
									float floatvalue1c = minitofloat(longbytes.get(2));
									float floatvalue1d = minitofloat(longbytes.get(3));
									float floatvalue1e = minitofloat(longbytes.get(4));
									float floatvalue1f = minitofloat(longbytes.get(5));
									float floatvalue1g = minitofloat(longbytes.get(6));
									float floatvalue1h = minitofloat(longbytes.get(7));
									float floatvalue2a = minitofloat(longbytes2.get(0));
									float floatvalue2b = minitofloat(longbytes2.get(1));
									float floatvalue2c = minitofloat(longbytes2.get(2));
									float floatvalue2d = minitofloat(longbytes2.get(3));
									float floatvalue2e = minitofloat(longbytes2.get(4));
									float floatvalue2f = minitofloat(longbytes2.get(5));
									float floatvalue2g = minitofloat(longbytes2.get(6));
									float floatvalue2h = minitofloat(longbytes2.get(7));
									longbytes.putLong(0L).rewind();
									float floatvaluea = floatvalue1a / floatvalue2a;
									float floatvalueb = floatvalue1b / floatvalue2b;
									float floatvaluec = floatvalue1c / floatvalue2c;
									float floatvalued = floatvalue1d / floatvalue2d;
									float floatvaluee = floatvalue1e / floatvalue2e;
									float floatvaluef = floatvalue1f / floatvalue2f;
									float floatvalueg = floatvalue1g / floatvalue2g;
									float floatvalueh = floatvalue1h / floatvalue2h;
									longbytes.put(0, floattomini(floatvaluea)).rewind();
									longbytes.put(1, floattomini(floatvalueb)).rewind();
									longbytes.put(2, floattomini(floatvaluec)).rewind();
									longbytes.put(3, floattomini(floatvalued)).rewind();
									longbytes.put(4, floattomini(floatvaluee)).rewind();
									longbytes.put(5, floattomini(floatvaluef)).rewind();
									longbytes.put(6, floattomini(floatvalueg)).rewind();
									longbytes.put(7, floattomini(floatvalueh)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xec: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1 = -minitofloat(longbytes.get(0));
									float floatvalue2 = -minitofloat(longbytes.get(1));
									float floatvalue3 = -minitofloat(longbytes.get(2));
									float floatvalue4 = -minitofloat(longbytes.get(3));
									float floatvalue5 = -minitofloat(longbytes.get(4));
									float floatvalue6 = -minitofloat(longbytes.get(5));
									float floatvalue7 = -minitofloat(longbytes.get(6));
									float floatvalue8 = -minitofloat(longbytes.get(7));
									longbytes.putLong(0L).rewind();
									longbytes.put(0, floattomini(floatvalue1)).rewind();
									longbytes.put(1, floattomini(floatvalue2)).rewind();
									longbytes.put(2, floattomini(floatvalue3)).rewind();
									longbytes.put(3, floattomini(floatvalue4)).rewind();
									longbytes.put(4, floattomini(floatvalue5)).rewind();
									longbytes.put(5, floattomini(floatvalue6)).rewind();
									longbytes.put(6, floattomini(floatvalue7)).rewind();
									longbytes.put(7, floattomini(floatvalue8)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								
								case 0x0d: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = longbytes.getFloat(0);
									float floatvalue1b = longbytes.getFloat(4);
									longbytes.putLong(0L).rewind();
									float floatvalue1 = (float)Math.sin(floatvalue1a);
									float floatvalue2 = (float)Math.sin(floatvalue1b);
									longbytes.putFloat(0, floatvalue1).rewind();
									longbytes.putFloat(4, floatvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x1d: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = longbytes.getFloat(0);
									float floatvalue1b = longbytes.getFloat(4);
									longbytes.putLong(0L).rewind();
									float floatvalue1 = (float)Math.tan(floatvalue1a);
									float floatvalue2 = (float)Math.tan(floatvalue1b);
									longbytes.putFloat(0, floatvalue1).rewind();
									longbytes.putFloat(4, floatvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x2d: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = longbytes.getFloat(0);
									float floatvalue1b = longbytes.getFloat(4);
									longbytes.putLong(0L).rewind();
									float floatvalue1 = (float)Math.cos(floatvalue1a);
									float floatvalue2 = (float)Math.cos(floatvalue1b);
									longbytes.putFloat(0, floatvalue1).rewind();
									longbytes.putFloat(4, floatvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x3d: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = longbytes.getFloat(0);
									float floatvalue1b = longbytes.getFloat(4);
									float floatvalue2a = longbytes2.getFloat(0);
									float floatvalue2b = longbytes2.getFloat(4);
									longbytes.putLong(0L).rewind();
									float floatvaluea = (float)(Math.log(floatvalue1a)/Math.log(floatvalue2a));
									float floatvalueb = (float)(Math.log(floatvalue1b)/Math.log(floatvalue2b));
									longbytes.putFloat(0, floatvaluea).rewind();
									longbytes.putFloat(4, floatvalueb).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x4d: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = longbytes.getFloat(0);
									float floatvalue1b = longbytes.getFloat(4);
									float floatvalue2a = longbytes2.getFloat(0);
									float floatvalue2b = longbytes2.getFloat(4);
									longbytes.putLong(0L).rewind();
									float floatvaluea = (float)(Math.pow(floatvalue1a, floatvalue2a));
									float floatvalueb = (float)(Math.pow(floatvalue1b, floatvalue2b));
									longbytes.putFloat(0, floatvaluea).rewind();
									longbytes.putFloat(4, floatvalueb).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x5d: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = halftofloat(longbytes.getShort(0));
									float floatvalue1b = halftofloat(longbytes.getShort(2));
									float floatvalue1c = halftofloat(longbytes.getShort(4));
									float floatvalue1d = halftofloat(longbytes.getShort(6));
									longbytes.putLong(0L).rewind();
									float floatvalue1 = (float)Math.sin(floatvalue1a);
									float floatvalue2 = (float)Math.sin(floatvalue1b);
									float floatvalue3 = (float)Math.sin(floatvalue1c);
									float floatvalue4 = (float)Math.sin(floatvalue1d);
									longbytes.putShort(0, floattohalf(floatvalue1)).rewind();
									longbytes.putShort(2, floattohalf(floatvalue2)).rewind();
									longbytes.putShort(4, floattohalf(floatvalue3)).rewind();
									longbytes.putShort(6, floattohalf(floatvalue4)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x6d: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = halftofloat(longbytes.getShort(0));
									float floatvalue1b = halftofloat(longbytes.getShort(2));
									float floatvalue1c = halftofloat(longbytes.getShort(4));
									float floatvalue1d = halftofloat(longbytes.getShort(6));
									longbytes.putLong(0L).rewind();
									float floatvalue1 = (float)Math.tan(floatvalue1a);
									float floatvalue2 = (float)Math.tan(floatvalue1b);
									float floatvalue3 = (float)Math.tan(floatvalue1c);
									float floatvalue4 = (float)Math.tan(floatvalue1d);
									longbytes.putShort(0, floattohalf(floatvalue1)).rewind();
									longbytes.putShort(2, floattohalf(floatvalue2)).rewind();
									longbytes.putShort(4, floattohalf(floatvalue3)).rewind();
									longbytes.putShort(6, floattohalf(floatvalue4)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x7d: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = halftofloat(longbytes.getShort(0));
									float floatvalue1b = halftofloat(longbytes.getShort(2));
									float floatvalue1c = halftofloat(longbytes.getShort(4));
									float floatvalue1d = halftofloat(longbytes.getShort(6));
									longbytes.putLong(0L).rewind();
									float floatvalue1 = (float)Math.cos(floatvalue1a);
									float floatvalue2 = (float)Math.cos(floatvalue1b);
									float floatvalue3 = (float)Math.cos(floatvalue1c);
									float floatvalue4 = (float)Math.cos(floatvalue1d);
									longbytes.putShort(0, floattohalf(floatvalue1)).rewind();
									longbytes.putShort(2, floattohalf(floatvalue2)).rewind();
									longbytes.putShort(4, floattohalf(floatvalue3)).rewind();
									longbytes.putShort(6, floattohalf(floatvalue4)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x8d: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = halftofloat(longbytes.getShort(0));
									float floatvalue1b = halftofloat(longbytes.getShort(2));
									float floatvalue1c = halftofloat(longbytes.getShort(4));
									float floatvalue1d = halftofloat(longbytes.getShort(6));
									float floatvalue2a = halftofloat(longbytes2.getShort(0));
									float floatvalue2b = halftofloat(longbytes2.getShort(2));
									float floatvalue2c = halftofloat(longbytes2.getShort(4));
									float floatvalue2d = halftofloat(longbytes2.getShort(6));
									longbytes.putLong(0L).rewind();
									float floatvaluea = (float)(Math.log(floatvalue1a)/Math.log(floatvalue2a));
									float floatvalueb = (float)(Math.log(floatvalue1b)/Math.log(floatvalue2b));
									float floatvaluec = (float)(Math.log(floatvalue1c)/Math.log(floatvalue2c));
									float floatvalued = (float)(Math.log(floatvalue1d)/Math.log(floatvalue2d));
									longbytes.putShort(0, floattohalf(floatvaluea)).rewind();
									longbytes.putShort(2, floattohalf(floatvalueb)).rewind();
									longbytes.putShort(4, floattohalf(floatvaluec)).rewind();
									longbytes.putShort(6, floattohalf(floatvalued)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x9d: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = halftofloat(longbytes.getShort(0));
									float floatvalue1b = halftofloat(longbytes.getShort(2));
									float floatvalue1c = halftofloat(longbytes.getShort(4));
									float floatvalue1d = halftofloat(longbytes.getShort(6));
									float floatvalue2a = halftofloat(longbytes2.getShort(0));
									float floatvalue2b = halftofloat(longbytes2.getShort(2));
									float floatvalue2c = halftofloat(longbytes2.getShort(4));
									float floatvalue2d = halftofloat(longbytes2.getShort(6));
									longbytes.putLong(0L).rewind();
									float floatvaluea = (float)(Math.pow(floatvalue1a, floatvalue2a));
									float floatvalueb = (float)(Math.pow(floatvalue1b, floatvalue2b));
									float floatvaluec = (float)(Math.pow(floatvalue1c, floatvalue2c));
									float floatvalued = (float)(Math.pow(floatvalue1d, floatvalue2d));
									longbytes.putShort(0, floattohalf(floatvaluea)).rewind();
									longbytes.putShort(2, floattohalf(floatvalueb)).rewind();
									longbytes.putShort(4, floattohalf(floatvaluec)).rewind();
									longbytes.putShort(6, floattohalf(floatvalued)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xad: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = minitofloat(longbytes.get(0));
									float floatvalue1b = minitofloat(longbytes.get(1));
									float floatvalue1c = minitofloat(longbytes.get(2));
									float floatvalue1d = minitofloat(longbytes.get(3));
									float floatvalue1e = minitofloat(longbytes.get(4));
									float floatvalue1f = minitofloat(longbytes.get(5));
									float floatvalue1g = minitofloat(longbytes.get(6));
									float floatvalue1h = minitofloat(longbytes.get(7));
									longbytes.putLong(0L).rewind();
									float floatvalue1 = (float)Math.sin(floatvalue1a);
									float floatvalue2 = (float)Math.sin(floatvalue1b);
									float floatvalue3 = (float)Math.sin(floatvalue1c);
									float floatvalue4 = (float)Math.sin(floatvalue1d);
									float floatvalue5 = (float)Math.sin(floatvalue1e);
									float floatvalue6 = (float)Math.sin(floatvalue1f);
									float floatvalue7 = (float)Math.sin(floatvalue1g);
									float floatvalue8 = (float)Math.sin(floatvalue1h);
									longbytes.put(0, floattomini(floatvalue1)).rewind();
									longbytes.put(1, floattomini(floatvalue2)).rewind();
									longbytes.put(2, floattomini(floatvalue3)).rewind();
									longbytes.put(3, floattomini(floatvalue4)).rewind();
									longbytes.put(4, floattomini(floatvalue5)).rewind();
									longbytes.put(5, floattomini(floatvalue6)).rewind();
									longbytes.put(6, floattomini(floatvalue7)).rewind();
									longbytes.put(7, floattomini(floatvalue8)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xbd: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = minitofloat(longbytes.get(0));
									float floatvalue1b = minitofloat(longbytes.get(1));
									float floatvalue1c = minitofloat(longbytes.get(2));
									float floatvalue1d = minitofloat(longbytes.get(3));
									float floatvalue1e = minitofloat(longbytes.get(4));
									float floatvalue1f = minitofloat(longbytes.get(5));
									float floatvalue1g = minitofloat(longbytes.get(6));
									float floatvalue1h = minitofloat(longbytes.get(7));
									longbytes.putLong(0L).rewind();
									float floatvalue1 = (float)Math.tan(floatvalue1a);
									float floatvalue2 = (float)Math.tan(floatvalue1b);
									float floatvalue3 = (float)Math.tan(floatvalue1c);
									float floatvalue4 = (float)Math.tan(floatvalue1d);
									float floatvalue5 = (float)Math.tan(floatvalue1e);
									float floatvalue6 = (float)Math.tan(floatvalue1f);
									float floatvalue7 = (float)Math.tan(floatvalue1g);
									float floatvalue8 = (float)Math.tan(floatvalue1h);
									longbytes.put(0, floattomini(floatvalue1)).rewind();
									longbytes.put(1, floattomini(floatvalue2)).rewind();
									longbytes.put(2, floattomini(floatvalue3)).rewind();
									longbytes.put(3, floattomini(floatvalue4)).rewind();
									longbytes.put(4, floattomini(floatvalue5)).rewind();
									longbytes.put(5, floattomini(floatvalue6)).rewind();
									longbytes.put(6, floattomini(floatvalue7)).rewind();
									longbytes.put(7, floattomini(floatvalue8)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xcd: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = minitofloat(longbytes.get(0));
									float floatvalue1b = minitofloat(longbytes.get(1));
									float floatvalue1c = minitofloat(longbytes.get(2));
									float floatvalue1d = minitofloat(longbytes.get(3));
									float floatvalue1e = minitofloat(longbytes.get(4));
									float floatvalue1f = minitofloat(longbytes.get(5));
									float floatvalue1g = minitofloat(longbytes.get(6));
									float floatvalue1h = minitofloat(longbytes.get(7));
									longbytes.putLong(0L).rewind();
									float floatvalue1 = (float)Math.cos(floatvalue1a);
									float floatvalue2 = (float)Math.cos(floatvalue1b);
									float floatvalue3 = (float)Math.cos(floatvalue1c);
									float floatvalue4 = (float)Math.cos(floatvalue1d);
									float floatvalue5 = (float)Math.cos(floatvalue1e);
									float floatvalue6 = (float)Math.cos(floatvalue1f);
									float floatvalue7 = (float)Math.cos(floatvalue1g);
									float floatvalue8 = (float)Math.cos(floatvalue1h);
									longbytes.put(0, floattomini(floatvalue1)).rewind();
									longbytes.put(1, floattomini(floatvalue2)).rewind();
									longbytes.put(2, floattomini(floatvalue3)).rewind();
									longbytes.put(3, floattomini(floatvalue4)).rewind();
									longbytes.put(4, floattomini(floatvalue5)).rewind();
									longbytes.put(5, floattomini(floatvalue6)).rewind();
									longbytes.put(6, floattomini(floatvalue7)).rewind();
									longbytes.put(7, floattomini(floatvalue8)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xdd: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = minitofloat(longbytes.get(0));
									float floatvalue1b = minitofloat(longbytes.get(1));
									float floatvalue1c = minitofloat(longbytes.get(2));
									float floatvalue1d = minitofloat(longbytes.get(3));
									float floatvalue1e = minitofloat(longbytes.get(4));
									float floatvalue1f = minitofloat(longbytes.get(5));
									float floatvalue1g = minitofloat(longbytes.get(6));
									float floatvalue1h = minitofloat(longbytes.get(7));
									float floatvalue2a = minitofloat(longbytes2.get(0));
									float floatvalue2b = minitofloat(longbytes2.get(1));
									float floatvalue2c = minitofloat(longbytes2.get(2));
									float floatvalue2d = minitofloat(longbytes2.get(3));
									float floatvalue2e = minitofloat(longbytes2.get(4));
									float floatvalue2f = minitofloat(longbytes2.get(5));
									float floatvalue2g = minitofloat(longbytes2.get(6));
									float floatvalue2h = minitofloat(longbytes2.get(7));
									longbytes.putLong(0L).rewind();
									float floatvaluea = (float)(Math.log(floatvalue1a)/Math.log(floatvalue2a));
									float floatvalueb = (float)(Math.log(floatvalue1b)/Math.log(floatvalue2b));
									float floatvaluec = (float)(Math.log(floatvalue1c)/Math.log(floatvalue2c));
									float floatvalued = (float)(Math.log(floatvalue1d)/Math.log(floatvalue2d));
									float floatvaluee = (float)(Math.log(floatvalue1e)/Math.log(floatvalue2e));
									float floatvaluef = (float)(Math.log(floatvalue1f)/Math.log(floatvalue2f));
									float floatvalueg = (float)(Math.log(floatvalue1g)/Math.log(floatvalue2g));
									float floatvalueh = (float)(Math.log(floatvalue1h)/Math.log(floatvalue2h));
									longbytes.put(0, floattomini(floatvaluea)).rewind();
									longbytes.put(1, floattomini(floatvalueb)).rewind();
									longbytes.put(2, floattomini(floatvaluec)).rewind();
									longbytes.put(3, floattomini(floatvalued)).rewind();
									longbytes.put(4, floattomini(floatvaluee)).rewind();
									longbytes.put(5, floattomini(floatvaluef)).rewind();
									longbytes.put(6, floattomini(floatvalueg)).rewind();
									longbytes.put(7, floattomini(floatvalueh)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xed: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									longbytes2.clear();
									longbytes2.putLong(oldregisters[regZ+i]).rewind();
									float floatvalue1a = minitofloat(longbytes.get(0));
									float floatvalue1b = minitofloat(longbytes.get(1));
									float floatvalue1c = minitofloat(longbytes.get(2));
									float floatvalue1d = minitofloat(longbytes.get(3));
									float floatvalue1e = minitofloat(longbytes.get(4));
									float floatvalue1f = minitofloat(longbytes.get(5));
									float floatvalue1g = minitofloat(longbytes.get(6));
									float floatvalue1h = minitofloat(longbytes.get(7));
									float floatvalue2a = minitofloat(longbytes2.get(0));
									float floatvalue2b = minitofloat(longbytes2.get(1));
									float floatvalue2c = minitofloat(longbytes2.get(2));
									float floatvalue2d = minitofloat(longbytes2.get(3));
									float floatvalue2e = minitofloat(longbytes2.get(4));
									float floatvalue2f = minitofloat(longbytes2.get(5));
									float floatvalue2g = minitofloat(longbytes2.get(6));
									float floatvalue2h = minitofloat(longbytes2.get(7));
									longbytes.putLong(0L).rewind();
									float floatvaluea = (float)(Math.pow(floatvalue1a, floatvalue2a));
									float floatvalueb = (float)(Math.pow(floatvalue1b, floatvalue2b));
									float floatvaluec = (float)(Math.pow(floatvalue1c, floatvalue2c));
									float floatvalued = (float)(Math.pow(floatvalue1d, floatvalue2d));
									float floatvaluee = (float)(Math.pow(floatvalue1e, floatvalue2e));
									float floatvaluef = (float)(Math.pow(floatvalue1f, floatvalue2f));
									float floatvalueg = (float)(Math.pow(floatvalue1g, floatvalue2g));
									float floatvalueh = (float)(Math.pow(floatvalue1h, floatvalue2h));
									longbytes.put(0, floattomini(floatvaluea)).rewind();
									longbytes.put(1, floattomini(floatvalueb)).rewind();
									longbytes.put(2, floattomini(floatvaluec)).rewind();
									longbytes.put(3, floattomini(floatvalued)).rewind();
									longbytes.put(4, floattomini(floatvaluee)).rewind();
									longbytes.put(5, floattomini(floatvaluef)).rewind();
									longbytes.put(6, floattomini(floatvalueg)).rewind();
									longbytes.put(7, floattomini(floatvalueh)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								
								case 0x0e: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = longbytes.getFloat(0);
									float floatvalue1b = longbytes.getFloat(4);
									longbytes.putLong(0L).rewind();
									float floatvalue1 = (float)Math.asin(floatvalue1a);
									float floatvalue2 = (float)Math.asin(floatvalue1b);
									longbytes.putFloat(0, floatvalue1).rewind();
									longbytes.putFloat(4, floatvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x1e: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = longbytes.getFloat(0);
									float floatvalue1b = longbytes.getFloat(4);
									longbytes.putLong(0L).rewind();
									float floatvalue1 = (float)Math.atan(floatvalue1a);
									float floatvalue2 = (float)Math.atan(floatvalue1b);
									longbytes.putFloat(0, floatvalue1).rewind();
									longbytes.putFloat(4, floatvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x2e: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = longbytes.getFloat(0);
									float floatvalue1b = longbytes.getFloat(4);
									longbytes.putLong(0L).rewind();
									float floatvalue1 = (float)Math.acos(floatvalue1a);
									float floatvalue2 = (float)Math.acos(floatvalue1b);
									longbytes.putFloat(0, floatvalue1).rewind();
									longbytes.putFloat(4, floatvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x3e: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = longbytes.getFloat(0);
									float floatvalue1b = longbytes.getFloat(4);
									longbytes.putLong(0L).rewind();
									float floatvalue1 = (float)Math.sqrt(floatvalue1a);
									float floatvalue2 = (float)Math.sqrt(floatvalue1b);
									longbytes.putFloat(0, floatvalue1).rewind();
									longbytes.putFloat(4, floatvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x5e: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = halftofloat(longbytes.getShort(0));
									float floatvalue1b = halftofloat(longbytes.getShort(2));
									float floatvalue1c = halftofloat(longbytes.getShort(4));
									float floatvalue1d = halftofloat(longbytes.getShort(6));
									longbytes.putLong(0L).rewind();
									float floatvalue1 = (float)Math.asin(floatvalue1a);
									float floatvalue2 = (float)Math.asin(floatvalue1b);
									float floatvalue3 = (float)Math.asin(floatvalue1c);
									float floatvalue4 = (float)Math.asin(floatvalue1d);
									longbytes.putShort(0, floattohalf(floatvalue1)).rewind();
									longbytes.putShort(2, floattohalf(floatvalue2)).rewind();
									longbytes.putShort(4, floattohalf(floatvalue3)).rewind();
									longbytes.putShort(6, floattohalf(floatvalue4)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x6e: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = halftofloat(longbytes.getShort(0));
									float floatvalue1b = halftofloat(longbytes.getShort(2));
									float floatvalue1c = halftofloat(longbytes.getShort(4));
									float floatvalue1d = halftofloat(longbytes.getShort(6));
									longbytes.putLong(0L).rewind();
									float floatvalue1 = (float)Math.atan(floatvalue1a);
									float floatvalue2 = (float)Math.atan(floatvalue1b);
									float floatvalue3 = (float)Math.atan(floatvalue1c);
									float floatvalue4 = (float)Math.atan(floatvalue1d);
									longbytes.putShort(0, floattohalf(floatvalue1)).rewind();
									longbytes.putShort(2, floattohalf(floatvalue2)).rewind();
									longbytes.putShort(4, floattohalf(floatvalue3)).rewind();
									longbytes.putShort(6, floattohalf(floatvalue4)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x7e: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = halftofloat(longbytes.getShort(0));
									float floatvalue1b = halftofloat(longbytes.getShort(2));
									float floatvalue1c = halftofloat(longbytes.getShort(4));
									float floatvalue1d = halftofloat(longbytes.getShort(6));
									longbytes.putLong(0L).rewind();
									float floatvalue1 = (float)Math.acos(floatvalue1a);
									float floatvalue2 = (float)Math.acos(floatvalue1b);
									float floatvalue3 = (float)Math.acos(floatvalue1c);
									float floatvalue4 = (float)Math.acos(floatvalue1d);
									longbytes.putShort(0, floattohalf(floatvalue1)).rewind();
									longbytes.putShort(2, floattohalf(floatvalue2)).rewind();
									longbytes.putShort(4, floattohalf(floatvalue3)).rewind();
									longbytes.putShort(6, floattohalf(floatvalue4)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x8e: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = halftofloat(longbytes.getShort(0));
									float floatvalue1b = halftofloat(longbytes.getShort(2));
									float floatvalue1c = halftofloat(longbytes.getShort(4));
									float floatvalue1d = halftofloat(longbytes.getShort(6));
									longbytes.putLong(0L).rewind();
									float floatvalue1 = (float)Math.sqrt(floatvalue1a);
									float floatvalue2 = (float)Math.sqrt(floatvalue1b);
									float floatvalue3 = (float)Math.sqrt(floatvalue1c);
									float floatvalue4 = (float)Math.sqrt(floatvalue1d);
									longbytes.putShort(0, floattohalf(floatvalue1)).rewind();
									longbytes.putShort(2, floattohalf(floatvalue2)).rewind();
									longbytes.putShort(4, floattohalf(floatvalue3)).rewind();
									longbytes.putShort(6, floattohalf(floatvalue4)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xae: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = minitofloat(longbytes.get(0));
									float floatvalue1b = minitofloat(longbytes.get(1));
									float floatvalue1c = minitofloat(longbytes.get(2));
									float floatvalue1d = minitofloat(longbytes.get(3));
									float floatvalue1e = minitofloat(longbytes.get(4));
									float floatvalue1f = minitofloat(longbytes.get(5));
									float floatvalue1g = minitofloat(longbytes.get(6));
									float floatvalue1h = minitofloat(longbytes.get(7));
									longbytes.putLong(0L).rewind();
									float floatvaluea = (float)Math.asin(floatvalue1a);
									float floatvalueb = (float)Math.asin(floatvalue1b);
									float floatvaluec = (float)Math.asin(floatvalue1c);
									float floatvalued = (float)Math.asin(floatvalue1d);
									float floatvaluee = (float)Math.asin(floatvalue1e);
									float floatvaluef = (float)Math.asin(floatvalue1f);
									float floatvalueg = (float)Math.asin(floatvalue1g);
									float floatvalueh = (float)Math.asin(floatvalue1h);
									longbytes.put(0, floattomini(floatvaluea)).rewind();
									longbytes.put(1, floattomini(floatvalueb)).rewind();
									longbytes.put(2, floattomini(floatvaluec)).rewind();
									longbytes.put(3, floattomini(floatvalued)).rewind();
									longbytes.put(4, floattomini(floatvaluee)).rewind();
									longbytes.put(5, floattomini(floatvaluef)).rewind();
									longbytes.put(6, floattomini(floatvalueg)).rewind();
									longbytes.put(7, floattomini(floatvalueh)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xbe: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = minitofloat(longbytes.get(0));
									float floatvalue1b = minitofloat(longbytes.get(1));
									float floatvalue1c = minitofloat(longbytes.get(2));
									float floatvalue1d = minitofloat(longbytes.get(3));
									float floatvalue1e = minitofloat(longbytes.get(4));
									float floatvalue1f = minitofloat(longbytes.get(5));
									float floatvalue1g = minitofloat(longbytes.get(6));
									float floatvalue1h = minitofloat(longbytes.get(7));
									longbytes.putLong(0L).rewind();
									float floatvaluea = (float)Math.atan(floatvalue1a);
									float floatvalueb = (float)Math.atan(floatvalue1b);
									float floatvaluec = (float)Math.atan(floatvalue1c);
									float floatvalued = (float)Math.atan(floatvalue1d);
									float floatvaluee = (float)Math.atan(floatvalue1e);
									float floatvaluef = (float)Math.atan(floatvalue1f);
									float floatvalueg = (float)Math.atan(floatvalue1g);
									float floatvalueh = (float)Math.atan(floatvalue1h);
									longbytes.put(0, floattomini(floatvaluea)).rewind();
									longbytes.put(1, floattomini(floatvalueb)).rewind();
									longbytes.put(2, floattomini(floatvaluec)).rewind();
									longbytes.put(3, floattomini(floatvalued)).rewind();
									longbytes.put(4, floattomini(floatvaluee)).rewind();
									longbytes.put(5, floattomini(floatvaluef)).rewind();
									longbytes.put(6, floattomini(floatvalueg)).rewind();
									longbytes.put(7, floattomini(floatvalueh)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xce: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = minitofloat(longbytes.get(0));
									float floatvalue1b = minitofloat(longbytes.get(1));
									float floatvalue1c = minitofloat(longbytes.get(2));
									float floatvalue1d = minitofloat(longbytes.get(3));
									float floatvalue1e = minitofloat(longbytes.get(4));
									float floatvalue1f = minitofloat(longbytes.get(5));
									float floatvalue1g = minitofloat(longbytes.get(6));
									float floatvalue1h = minitofloat(longbytes.get(7));
									longbytes.putLong(0L).rewind();
									float floatvaluea = (float)Math.acos(floatvalue1a);
									float floatvalueb = (float)Math.acos(floatvalue1b);
									float floatvaluec = (float)Math.acos(floatvalue1c);
									float floatvalued = (float)Math.acos(floatvalue1d);
									float floatvaluee = (float)Math.acos(floatvalue1e);
									float floatvaluef = (float)Math.acos(floatvalue1f);
									float floatvalueg = (float)Math.acos(floatvalue1g);
									float floatvalueh = (float)Math.acos(floatvalue1h);
									longbytes.put(0, floattomini(floatvaluea)).rewind();
									longbytes.put(1, floattomini(floatvalueb)).rewind();
									longbytes.put(2, floattomini(floatvaluec)).rewind();
									longbytes.put(3, floattomini(floatvalued)).rewind();
									longbytes.put(4, floattomini(floatvaluee)).rewind();
									longbytes.put(5, floattomini(floatvaluef)).rewind();
									longbytes.put(6, floattomini(floatvalueg)).rewind();
									longbytes.put(7, floattomini(floatvalueh)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xde: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float floatvalue1a = minitofloat(longbytes.get(0));
									float floatvalue1b = minitofloat(longbytes.get(1));
									float floatvalue1c = minitofloat(longbytes.get(2));
									float floatvalue1d = minitofloat(longbytes.get(3));
									float floatvalue1e = minitofloat(longbytes.get(4));
									float floatvalue1f = minitofloat(longbytes.get(5));
									float floatvalue1g = minitofloat(longbytes.get(6));
									float floatvalue1h = minitofloat(longbytes.get(7));
									longbytes.putLong(0L).rewind();
									float floatvaluea = (float)Math.sqrt(floatvalue1a);
									float floatvalueb = (float)Math.sqrt(floatvalue1b);
									float floatvaluec = (float)Math.sqrt(floatvalue1c);
									float floatvalued = (float)Math.sqrt(floatvalue1d);
									float floatvaluee = (float)Math.sqrt(floatvalue1e);
									float floatvaluef = (float)Math.sqrt(floatvalue1f);
									float floatvalueg = (float)Math.sqrt(floatvalue1g);
									float floatvalueh = (float)Math.sqrt(floatvalue1h);
									longbytes.put(0, floattomini(floatvaluea)).rewind();
									longbytes.put(1, floattomini(floatvalueb)).rewind();
									longbytes.put(2, floattomini(floatvaluec)).rewind();
									longbytes.put(3, floattomini(floatvalued)).rewind();
									longbytes.put(4, floattomini(floatvaluee)).rewind();
									longbytes.put(5, floattomini(floatvaluef)).rewind();
									longbytes.put(6, floattomini(floatvalueg)).rewind();
									longbytes.put(7, floattomini(floatvalueh)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								
								case 0x0f: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									int intvalue1a = longbytes.getInt(0);
									int intvalue1b = longbytes.getInt(4);
									longbytes.putLong(0L).rewind();
									float floatvalue1 = (float)intvalue1a;
									float floatvalue2 = (float)intvalue1b;
									longbytes.putFloat(0, floatvalue1).rewind();
									longbytes.putFloat(4, floatvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x1f: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float intvalue1a = longbytes.getFloat(0);
									float intvalue1b = longbytes.getFloat(4);
									longbytes.putLong(0L).rewind();
									int intvalue1 = (int)Math.round(intvalue1a);
									int intvalue2 = (int)Math.round(intvalue1b);
									longbytes.putInt(0, intvalue1).rewind();
									longbytes.putInt(4, intvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x2f: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float intvalue1a = longbytes.getFloat(0);
									float intvalue1b = longbytes.getFloat(4);
									longbytes.putLong(0L).rewind();
									int intvalue1 = (int)Math.floor(intvalue1a);
									int intvalue2 = (int)Math.floor(intvalue1b);
									longbytes.putInt(0, intvalue1).rewind();
									longbytes.putInt(4, intvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x3f: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float intvalue1a = longbytes.getFloat(0);
									float intvalue1b = longbytes.getFloat(4);
									longbytes.putLong(0L).rewind();
									int intvalue1 = (int)Math.ceil(intvalue1a);
									int intvalue2 = (int)Math.ceil(intvalue1b);
									longbytes.putInt(0, intvalue1).rewind();
									longbytes.putInt(4, intvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x4f: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float intvalue1a = longbytes.getFloat(0);
									float intvalue1b = longbytes.getFloat(4);
									longbytes.putLong(0L).rewind();
									int intvalue1 = (int)intvalue1a;
									int intvalue2 = (int)intvalue1b;
									longbytes.putInt(0, intvalue1).rewind();
									longbytes.putInt(4, intvalue2).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x5f: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									int intvalue1a = longbytes.getShort(0);
									int intvalue1b = longbytes.getShort(2);
									int intvalue1c = longbytes.getShort(4);
									int intvalue1d = longbytes.getShort(6);
									longbytes.putLong(0L).rewind();
									float floatvalue1 = (float)intvalue1a;
									float floatvalue2 = (float)intvalue1b;
									float floatvalue3 = (float)intvalue1c;
									float floatvalue4 = (float)intvalue1d;
									longbytes.putShort(0, floattohalf(floatvalue1)).rewind();
									longbytes.putShort(2, floattohalf(floatvalue2)).rewind();
									longbytes.putShort(4, floattohalf(floatvalue3)).rewind();
									longbytes.putShort(6, floattohalf(floatvalue4)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x6f: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float intvalue1a = halftofloat(longbytes.getShort(0));
									float intvalue1b = halftofloat(longbytes.getShort(2));
									float intvalue1c = halftofloat(longbytes.getShort(4));
									float intvalue1d = halftofloat(longbytes.getShort(6));
									longbytes.putLong(0L).rewind();
									int intvalue1 = (int)Math.round(intvalue1a);
									int intvalue2 = (int)Math.round(intvalue1b);
									int intvalue3 = (int)Math.round(intvalue1c);
									int intvalue4 = (int)Math.round(intvalue1d);
									longbytes.putShort(0, (short)intvalue1).rewind();
									longbytes.putShort(2, (short)intvalue2).rewind();
									longbytes.putShort(4, (short)intvalue3).rewind();
									longbytes.putShort(6, (short)intvalue4).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x7f: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float intvalue1a = halftofloat(longbytes.getShort(0));
									float intvalue1b = halftofloat(longbytes.getShort(2));
									float intvalue1c = halftofloat(longbytes.getShort(4));
									float intvalue1d = halftofloat(longbytes.getShort(6));
									longbytes.putLong(0L).rewind();
									int intvalue1 = (int)Math.floor(intvalue1a);
									int intvalue2 = (int)Math.floor(intvalue1b);
									int intvalue3 = (int)Math.floor(intvalue1c);
									int intvalue4 = (int)Math.floor(intvalue1d);
									longbytes.putShort(0, (short)intvalue1).rewind();
									longbytes.putShort(2, (short)intvalue2).rewind();
									longbytes.putShort(4, (short)intvalue3).rewind();
									longbytes.putShort(6, (short)intvalue4).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x8f: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float intvalue1a = halftofloat(longbytes.getShort(0));
									float intvalue1b = halftofloat(longbytes.getShort(2));
									float intvalue1c = halftofloat(longbytes.getShort(4));
									float intvalue1d = halftofloat(longbytes.getShort(6));
									longbytes.putLong(0L).rewind();
									int intvalue1 = (int)Math.ceil(intvalue1a);
									int intvalue2 = (int)Math.ceil(intvalue1b);
									int intvalue3 = (int)Math.ceil(intvalue1c);
									int intvalue4 = (int)Math.ceil(intvalue1d);
									longbytes.putShort(0, (short)intvalue1).rewind();
									longbytes.putShort(2, (short)intvalue2).rewind();
									longbytes.putShort(4, (short)intvalue3).rewind();
									longbytes.putShort(6, (short)intvalue4).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0x9f: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float intvalue1a = halftofloat(longbytes.getShort(0));
									float intvalue1b = halftofloat(longbytes.getShort(2));
									float intvalue1c = halftofloat(longbytes.getShort(4));
									float intvalue1d = halftofloat(longbytes.getShort(6));
									longbytes.putLong(0L).rewind();
									int intvalue1 = (int)intvalue1a;
									int intvalue2 = (int)intvalue1b;
									int intvalue3 = (int)intvalue1c;
									int intvalue4 = (int)intvalue1d;
									longbytes.putShort(0, (short)intvalue1).rewind();
									longbytes.putShort(2, (short)intvalue2).rewind();
									longbytes.putShort(4, (short)intvalue3).rewind();
									longbytes.putShort(6, (short)intvalue4).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xaf: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									int intvalue1a = longbytes.get(0);
									int intvalue1b = longbytes.get(1);
									int intvalue1c = longbytes.get(2);
									int intvalue1d = longbytes.get(3);
									int intvalue1e = longbytes.get(4);
									int intvalue1f = longbytes.get(5);
									int intvalue1g = longbytes.get(6);
									int intvalue1h = longbytes.get(7);
									longbytes.putLong(0L).rewind();
									float floatvalue1 = (float)intvalue1a;
									float floatvalue2 = (float)intvalue1b;
									float floatvalue3 = (float)intvalue1c;
									float floatvalue4 = (float)intvalue1d;
									float floatvalue5 = (float)intvalue1e;
									float floatvalue6 = (float)intvalue1f;
									float floatvalue7 = (float)intvalue1g;
									float floatvalue8 = (float)intvalue1h;
									longbytes.put(0, floattomini(floatvalue1)).rewind();
									longbytes.put(1, floattomini(floatvalue2)).rewind();
									longbytes.put(2, floattomini(floatvalue3)).rewind();
									longbytes.put(3, floattomini(floatvalue4)).rewind();
									longbytes.put(4, floattomini(floatvalue5)).rewind();
									longbytes.put(5, floattomini(floatvalue6)).rewind();
									longbytes.put(6, floattomini(floatvalue7)).rewind();
									longbytes.put(7, floattomini(floatvalue8)).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xbf: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float intvalue1a = minitofloat(longbytes.get(0));
									float intvalue1b = minitofloat(longbytes.get(1));
									float intvalue1c = minitofloat(longbytes.get(2));
									float intvalue1d = minitofloat(longbytes.get(3));
									float intvalue1e = minitofloat(longbytes.get(4));
									float intvalue1f = minitofloat(longbytes.get(5));
									float intvalue1g = minitofloat(longbytes.get(6));
									float intvalue1h = minitofloat(longbytes.get(7));
									longbytes.putLong(0L).rewind();
									int intvalue1 = (int)Math.round(intvalue1a);
									int intvalue2 = (int)Math.round(intvalue1b);
									int intvalue3 = (int)Math.round(intvalue1c);
									int intvalue4 = (int)Math.round(intvalue1d);
									int intvalue5 = (int)Math.round(intvalue1e);
									int intvalue6 = (int)Math.round(intvalue1f);
									int intvalue7 = (int)Math.round(intvalue1g);
									int intvalue8 = (int)Math.round(intvalue1h);
									longbytes.put(0, (byte)intvalue1).rewind();
									longbytes.put(1, (byte)intvalue2).rewind();
									longbytes.put(2, (byte)intvalue3).rewind();
									longbytes.put(3, (byte)intvalue4).rewind();
									longbytes.put(4, (byte)intvalue5).rewind();
									longbytes.put(5, (byte)intvalue6).rewind();
									longbytes.put(6, (byte)intvalue7).rewind();
									longbytes.put(7, (byte)intvalue8).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xcf: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float intvalue1a = minitofloat(longbytes.get(0));
									float intvalue1b = minitofloat(longbytes.get(1));
									float intvalue1c = minitofloat(longbytes.get(2));
									float intvalue1d = minitofloat(longbytes.get(3));
									float intvalue1e = minitofloat(longbytes.get(4));
									float intvalue1f = minitofloat(longbytes.get(5));
									float intvalue1g = minitofloat(longbytes.get(6));
									float intvalue1h = minitofloat(longbytes.get(7));
									longbytes.putLong(0L).rewind();
									int intvalue1 = (int)Math.floor(intvalue1a);
									int intvalue2 = (int)Math.floor(intvalue1b);
									int intvalue3 = (int)Math.floor(intvalue1c);
									int intvalue4 = (int)Math.floor(intvalue1d);
									int intvalue5 = (int)Math.floor(intvalue1e);
									int intvalue6 = (int)Math.floor(intvalue1f);
									int intvalue7 = (int)Math.floor(intvalue1g);
									int intvalue8 = (int)Math.floor(intvalue1h);
									longbytes.put(0, (byte)intvalue1).rewind();
									longbytes.put(1, (byte)intvalue2).rewind();
									longbytes.put(2, (byte)intvalue3).rewind();
									longbytes.put(3, (byte)intvalue4).rewind();
									longbytes.put(4, (byte)intvalue5).rewind();
									longbytes.put(5, (byte)intvalue6).rewind();
									longbytes.put(6, (byte)intvalue7).rewind();
									longbytes.put(7, (byte)intvalue8).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xdf: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float intvalue1a = minitofloat(longbytes.get(0));
									float intvalue1b = minitofloat(longbytes.get(1));
									float intvalue1c = minitofloat(longbytes.get(2));
									float intvalue1d = minitofloat(longbytes.get(3));
									float intvalue1e = minitofloat(longbytes.get(4));
									float intvalue1f = minitofloat(longbytes.get(5));
									float intvalue1g = minitofloat(longbytes.get(6));
									float intvalue1h = minitofloat(longbytes.get(7));
									longbytes.putLong(0L).rewind();
									int intvalue1 = (int)Math.ceil(intvalue1a);
									int intvalue2 = (int)Math.ceil(intvalue1b);
									int intvalue3 = (int)Math.ceil(intvalue1c);
									int intvalue4 = (int)Math.ceil(intvalue1d);
									int intvalue5 = (int)Math.ceil(intvalue1e);
									int intvalue6 = (int)Math.ceil(intvalue1f);
									int intvalue7 = (int)Math.ceil(intvalue1g);
									int intvalue8 = (int)Math.ceil(intvalue1h);
									longbytes.put(0, (byte)intvalue1).rewind();
									longbytes.put(1, (byte)intvalue2).rewind();
									longbytes.put(2, (byte)intvalue3).rewind();
									longbytes.put(3, (byte)intvalue4).rewind();
									longbytes.put(4, (byte)intvalue5).rewind();
									longbytes.put(5, (byte)intvalue6).rewind();
									longbytes.put(6, (byte)intvalue7).rewind();
									longbytes.put(7, (byte)intvalue8).rewind();
									newregisters[regX+i] = longbytes.getLong();
								} break;
								case 0xef: if (true) {
									newregisters[regX+i] = 0;
									longbytes.clear();
									longbytes.putLong(oldregisters[regY+i]).rewind();
									float intvalue1a = minitofloat(longbytes.get(0));
									float intvalue1b = minitofloat(longbytes.get(1));
									float intvalue1c = minitofloat(longbytes.get(2));
									float intvalue1d = minitofloat(longbytes.get(3));
									float intvalue1e = minitofloat(longbytes.get(4));
									float intvalue1f = minitofloat(longbytes.get(5));
									float intvalue1g = minitofloat(longbytes.get(6));
									float intvalue1h = minitofloat(longbytes.get(7));
									longbytes.putLong(0L).rewind();
									int intvalue1 = (int)intvalue1a;
									int intvalue2 = (int)intvalue1b;
									int intvalue3 = (int)intvalue1c;
									int intvalue4 = (int)intvalue1d;
									int intvalue5 = (int)intvalue1e;
									int intvalue6 = (int)intvalue1f;
									int intvalue7 = (int)intvalue1g;
									int intvalue8 = (int)intvalue1h;
									longbytes.put(0, (byte)intvalue1).rewind();
									longbytes.put(1, (byte)intvalue2).rewind();
									longbytes.put(2, (byte)intvalue3).rewind();
									longbytes.put(3, (byte)intvalue4).rewind();
									longbytes.put(4, (byte)intvalue5).rewind();
									longbytes.put(5, (byte)intvalue6).rewind();
									longbytes.put(6, (byte)intvalue7).rewind();
									longbytes.put(7, (byte)intvalue8).rewind();
									newregisters[regX+i] = longbytes.getLong();
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
		}
		
		public void updateregisters() {
			for (int i=0;i<RiscChip.registeramount;i++) {
				oldregisters[i] = newregisters[i];
			}
		}
	}
	
	public static float halftofloat(short half) {
		long longvalue = Short.toUnsignedLong(half);
		long longsign = (longvalue & 0x8000)>>>15;
		long longexp = ((longvalue & 0x7C00)>>>10) - 15 + 127;
		long longfrac = (longvalue & 0x3FF);
		if (longexp==112) { longexp = 0x0; }
		else if (longexp==143) { longexp = 0xFF; }
		long longfloat = (longsign<<31) | (longexp<<23) | (longfrac<<13);
		ByteBuffer longbytes = ByteBuffer.allocate(8);
		longbytes.clear(); longbytes.putLong(longfloat).rewind();
		float floatvalue = longbytes.asFloatBuffer().get(1);
		return floatvalue;
	}
	public static short floattohalf(float half) {
		long longvalue = Integer.toUnsignedLong(Float.floatToRawIntBits(half));
		long longsign = (longvalue & 0x80000000)>>>31;
		long longexp = ((longvalue & 0x7F800000)>>>23) - 127 + 15;
		long longfrac = (longvalue & 0x7FFFFF)>>>13;
		if (longexp == -112) { longexp = 0; }
		else if (longexp == 143) { longexp = 0x1F; }
		else if (longexp <= 0) { longexp = 0; longfrac = 0; }
		else if (longexp >= 31) { longexp = 0x1F; longfrac = 0; }
		else {
			long roundup = (longvalue & 0x1000);
			if (roundup!=0) {
				if (longfrac == 0x3FF) {
					longexp += 1;
				}
				longfrac += 1;
				longfrac &= 0x3FF;
			}
		}
		long floatvalue = (longsign<<15) | (longexp<<10) | longfrac;
		short shortvalue = (short)floatvalue;
		return shortvalue;
	}
	public static float minitofloat(byte mini) {
		long longvalue = Short.toUnsignedLong(mini);
		long longsign = (longvalue & 0x80)>>>7;
		long longexp = ((longvalue & 0x78)>>>3) - 7 + 127;
		long longfrac = (longvalue & 0x7);
		if (longexp==120) { longexp = 0x0; }
		else if (longexp==135) { longexp = 0xFF; }
		long longfloat = (longsign<<31) | (longexp<<23) | (longfrac<<20);
		ByteBuffer longbytes = ByteBuffer.allocate(8);
		longbytes.clear(); longbytes.putLong(longfloat).rewind();
		float floatvalue = longbytes.asFloatBuffer().get(1);
		return floatvalue;
	}
	public static byte floattomini(float mini) {
		long longvalue = Integer.toUnsignedLong(Float.floatToRawIntBits(mini));
		long longsign = (longvalue & 0x80000000)>>>31;
		long longexp = ((longvalue & 0x7F800000)>>>23) - 127 + 7;
		long longfrac = (longvalue & 0x7FFFFF)>>>20;
		if (longexp == -120) { longexp = 0; }
		else if (longexp == 135) { longexp = 0xF; }
		else if (longexp <= 0) { longexp = 0; longfrac = 0; }
		else if (longexp >= 15) { longexp = 0xF; longfrac = 0; }
		else {
			long roundup = (longvalue & 0x80000);
			if (roundup!=0) {
				if (longfrac == 0x7) {
					longexp += 1;
				}
				longfrac += 1;
				longfrac &= 0x7;
			}
		}
		long floatvalue = (longsign<<7) | (longexp<<3) | longfrac;
		byte minivalue = (byte)floatvalue;
		return minivalue;
	}
	
}
