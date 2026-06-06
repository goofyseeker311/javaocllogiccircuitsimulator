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
			String fileboot = arg[0];
			String filecart = arg[1];
			String filememout = arg[2];
			String filecartout = arg[3];
			int cores = 1;
			long cycles = 8192;
			if (arg.length>4) {
				cores = Integer.parseUnsignedInt(arg[4]);
			}
			if (arg.length>5) {
				cycles = Long.parseUnsignedLong(arg[5]);
			}
			JavaOCLLogicCircuitEmulator emulator = new JavaOCLLogicCircuitEmulator();
			emulator.run(fileboot, filecart, filememout, filecartout, cycles, cores);
		} else {
			System.out.println("arguments expected: boot.bin program.bin memory.out cart.out [cores] [cycles]");
		}
		System.out.println("exit.");
	}
	
	public void run(String fileboot, String filecart, String filememout, String filecartout, long cycles, int cores) {
		riscchip = new RiscChip(cores);
		File bootfile = new File(fileboot);
		File cartfile = new File(filecart);
		File outputmemfile = new File(filememout);
		File outputcartfile = new File(filecartout);
		try {
			byte[] bootfilebytes = Files.readAllBytes(bootfile.toPath());
			byte[] cartfilebytes = Files.readAllBytes(cartfile.toPath());
			BufferedOutputStream filememoutput = new BufferedOutputStream(new FileOutputStream(outputmemfile));
			BufferedOutputStream filecartoutput = new BufferedOutputStream(new FileOutputStream(outputcartfile));
			ByteBuffer bootbytes = ByteBuffer.wrap(bootfilebytes);
			ByteBuffer cartbytes = ByteBuffer.wrap(cartfilebytes);
			long[] boot = new long[riscchip.memoryamount];
			long[] cart = new long[riscchip.memoryamount];
			LongBuffer bootbuffer = bootbytes.asLongBuffer();
			LongBuffer cartbuffer = cartbytes.asLongBuffer();
			bootbuffer.get(boot, 0, bootbuffer.remaining());
			cartbuffer.get(cart, 0, cartbuffer.remaining());
			riscchip.loadmemory(boot);
			riscchip.loadcart(cart);
			riscchip.processchip(cycles);
			long[] memoryout = new long[riscchip.memoryamount];
			long[] cartout = new long[riscchip.memoryamount];
			riscchip.savememory(memoryout);
			riscchip.savecart(cartout);
			byte[] memoryarray = new byte[riscchip.memoryamount*8];
			byte[] cartoutarray = new byte[riscchip.memoryamount*8];
			ByteBuffer memorybytes = ByteBuffer.wrap(memoryarray);
			ByteBuffer cartoutbytes = ByteBuffer.wrap(cartoutarray);
			LongBuffer memorylongs = memorybytes.asLongBuffer();
			LongBuffer cartoutlongs = cartoutbytes.asLongBuffer();
			memorylongs.put(memoryout, 0, memoryout.length);
			cartoutlongs.put(cartout, 0, cartout.length);
			filememoutput.write(memoryarray);
			filecartoutput.write(cartoutarray);
			filememoutput.close();
			filecartoutput.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public class RiscChip {
		public RiscCore[] risccores;
		public int memoryamount = 65536*256;
		public long[] memoryram = new long[memoryamount];
		public long[] cartram = new long[memoryamount];
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
		public void loadmemory(long[] program) {
			if (program!=null) {
				for (int i=0;(i<program.length)&&(i<riscchip.memoryram.length);i++) {
					riscchip.memoryram[i] = program[i];
				}
			}
		}
		public void savememory(long[] program) {
			if (program!=null) {
				for (int i=0;(i<program.length)&&(i<riscchip.memoryram.length);i++) {
					program[i] = riscchip.memoryram[i];
				}
			}
		}
		public void loadcart(long[] cart) {
			if (cart!=null) {
				for (int i=0;(i<cart.length)&&(i<riscchip.cartram.length);i++) {
					riscchip.cartram[i] = cart[i];
				}
			}
		}
		public void savecart(long[] cart) {
			if (cart!=null) {
				for (int i=0;(i<cart.length)&&(i<riscchip.cartram.length);i++) {
					cart[i] = riscchip.cartram[i];
				}
			}
		}
	}

	public class RiscCore {
		private int corenum = 0;
		private int cyclenum = 0;
		private int registeramount = 65536;
		private long[] newregisters = new long[registeramount];
		private long[] oldregisters = new long[registeramount];
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
		
		public void processinstruction() {
			instructionstate = riscchip.memoryram[programcounter];
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
							long[] regyaddr = {oldregisters[regY]};
							BitSet regybits = BitSet.valueOf(regyaddr);
							if (regybits.get(63)) {
								regybits.clear(60, 64);
								long[] cartaddr = regybits.toLongArray();
								long cartaddress = 0;
								if (cartaddr.length>0) {
									cartaddress = cartaddr[0];
								}
								newregisters[regX+i] = riscchip.cartram[((int)cartaddress)+i];
							} else {
								newregisters[regX+i] = riscchip.memoryram[((int)oldregisters[regY])+i];
							}
						} else if (insT==0x13) {
							long[] regyaddr = {oldregisters[regY]};
							BitSet regybits = BitSet.valueOf(regyaddr);
							if (regybits.get(63)) {
								regybits.clear(60, 64);
								long[] cartaddr = regybits.toLongArray();
								long cartaddress = 0;
								if (cartaddr.length>0) {
									cartaddress = cartaddr[0];
								}
								riscchip.cartram[((int)cartaddress)+i] = oldregisters[regX+i];
							} else {
								riscchip.memoryram[((int)oldregisters[regY])+i] = oldregisters[regX+i];
							}
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
						} else if (insT==0xB5) {
							newregisters[regX+i] = corenum;
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
						} else if (insT==0x09) {
							long[] regtarray = {oldregisters[regY+i]};
							BitSet regybits = BitSet.valueOf(regtarray);
							newregisters[regX+i] = regybits.previousSetBit(63);
						} else if (insT==0x19) {
							long[] regtarray = {oldregisters[regY+i]};
							BitSet regybits = BitSet.valueOf(regtarray);
							newregisters[regX+i] = regybits.nextSetBit(0);
						} else if (insT==0x29) {
							long[] regtarray = {oldregisters[regY+i]};
							BitSet regybits = BitSet.valueOf(regtarray);
							newregisters[regX+i] = regybits.previousClearBit(63);
						} else if (insT==0x39) {
							long[] regtarray = {oldregisters[regY+i]};
							BitSet regybits = BitSet.valueOf(regtarray);
							newregisters[regX+i] = regybits.nextClearBit(0);
						} else if (insT==0x49) {
							long[] regtarray = {oldregisters[regY+i]};
							BitSet regybits = BitSet.valueOf(regtarray);
							newregisters[regX+i] = regybits.cardinality();
						}
					}
				}
				programcounter++;
			}
			
			if (programcounter>=riscchip.memoryamount) {
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
