package fi.jkauppa.javaocllogiccircuitsimulator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.file.Files;
import java.util.Random;

import static org.lwjgl.system.MemoryUtil.NULL;

import fi.jkauppa.javaocllogiccircuitsimulator.ComputeLib.Device;

public class JavaOCLLogicCircuitSimulator {
	private RiscChip riscchip = null;
	private ComputeLib computelib = new ComputeLib();
	private long device = NULL;
	private Device devicedata = null;
	private long queue = NULL;
	private String devicename = null;
	private int devicenum;

	public JavaOCLLogicCircuitSimulator(int vdevicenum) {
		this.devicenum = vdevicenum;
		
		System.out.println("init.");

		device = computelib.devicelist[devicenum];
		devicedata = computelib.devicemap.get(device);
		queue = devicedata.queue;
		devicename = devicedata.devicename;
		System.out.println("Using device["+devicenum+"]: "+devicename);
	}

	/*
	vc = 77;
	int[] newvalues = {-1,5,0,~255,0,255,128,0,1,2,0,2,4,0,-2,-2,0,-4,4,0,2,-1,0,4,1,0,8,1,0,-9,0,7,3,0,5,4,0,2,3,0,8,4,0,
			Float.floatToIntBits((float)(Math.PI)),0,Float.floatToIntBits((float)(Math.PI/2.0f)),0,Float.floatToIntBits(1.0f),0,Float.floatToIntBits(-1.0f),0,Float.floatToIntBits(1.0f),0,Float.floatToIntBits(1.557408f),0,
			Float.floatToIntBits(100.0f),0,Float.floatToIntBits(4.6051702f),0,3,Float.floatToIntBits(-2.0f),0,Float.floatToIntBits(4.5f),Float.floatToIntBits(7.2f),0,Float.floatToIntBits(3.1f),Float.floatToIntBits(1.2f),0,
			Float.floatToIntBits(1.8f),Float.floatToIntBits(2.5f),0,Float.floatToIntBits(-3.7f),Float.floatToIntBits(-0.85f),0,50,1,0};
	int[] oldvalues = new int[vc];
	Arrays.fill(oldvalues, 0);
	newvaluesptr = computelib.createBuffer(device, vc);
	oldvaluesptr = computelib.createBuffer(device, vc);
	computelib.writeBufferi(device, queue, newvaluesptr, newvalues);
	computelib.writeBufferi(device, queue, oldvaluesptr, oldvalues);
	*/
	/*
	private class RunThread extends Thread {
		public void run() {
			System.out.println("running.");
			float ctimedif = 0.0f;
			computelib.insertBarrier(queue);
			ctimedif = computelib.runProgram(device, queue, program, "updatevalues", new long[]{oldvaluesptr,newvaluesptr}, new int[]{0}, new int[]{vc}, 0, false);
			computelib.insertBarrier(queue);
			ctimedif = computelib.runProgram(device, queue, program, "processgates", new long[]{circuitptr,oldvaluesptr,newvaluesptr}, new int[]{0}, new int[]{gatecount/cs}, re, true)/(float)re;
			float tflops = (gatecount/cs) * (1000.0f/ctimedif) / 1000000000000.0f;
			System.out.println(String.format("%.4f",ctimedif).replace(",", ".")+"ms\t "+String.format("%.4f",tflops).replace(",", ".")+"tflops\t device: "+devicename);
			computelib.readBufferi(device, queue, newvaluesptr, newvalues);
			computelib.readBufferi(device, queue, oldvaluesptr, oldvalues);
			for (int i=0;i<vc;i++) {
				System.out.println("values["+i+"]: "+oldvalues[i]+"("+Float.intBitsToFloat(oldvalues[i])+"f) => "+newvalues[i]+"("+Float.intBitsToFloat(newvalues[i])+"f)");
			}
			System.out.println("done.");
		}
	}
	*/

	public static void main(String[] arg) {
		System.out.println("init.");
		if (arg.length>1) {
			String fileboot = arg[1];
			String filecart = arg[2];
			String filememout = arg[3];
			String filecartout = arg[4];
			String filedispout = arg[5];
			String fileregout = arg[6];
			int cores = 1;
			long cycles = 8192;
			int devnum = Integer.parseUnsignedInt(arg[0]);
			if (arg.length>7) {
				cores = Integer.parseUnsignedInt(arg[7]);
			}
			if (arg.length>8) {
				cycles = Long.parseUnsignedLong(arg[8]);
			}
			JavaOCLLogicCircuitSimulator emulator = new JavaOCLLogicCircuitSimulator(devnum);
			emulator.run(fileboot, filecart, filememout, filecartout, filedispout, fileregout, cycles, cores);
		} else {
			System.out.println("arguments expected: devnum boot.bin cart.bin mem.out cart.out disp.out reg.out [cores] [cycles]");
		}
		System.out.println("exit.");
	}
	
	public void run(String fileboot, String filecart, String filememout, String filecartout, String filedispout, String fileregout, long cycles, int cores) {
		riscchip = new RiscChip(cores);
		File bootfile = new File(fileboot);
		File cartfile = new File(filecart);
		File outputmemfile = new File(filememout);
		File outputcartfile = new File(filecartout);
		File outputdispfile = new File(filedispout);
		File outputregfile = new File(fileregout);
		try {
			byte[] bootfilebytes = Files.readAllBytes(bootfile.toPath());
			byte[] cartfilebytes = Files.readAllBytes(cartfile.toPath());
			BufferedOutputStream filememoutput = new BufferedOutputStream(new FileOutputStream(outputmemfile));
			BufferedOutputStream filecartoutput = new BufferedOutputStream(new FileOutputStream(outputcartfile));
			BufferedOutputStream filedispoutput = new BufferedOutputStream(new FileOutputStream(outputdispfile));
			BufferedOutputStream fileregoutput = new BufferedOutputStream(new FileOutputStream(outputregfile));
			ByteBuffer bootbytes = ByteBuffer.wrap(bootfilebytes);
			ByteBuffer cartbytes = ByteBuffer.wrap(cartfilebytes);
			long[] boot = new long[RiscChip.memoryamount];
			long[] cart = new long[RiscChip.cartamount];
			LongBuffer bootbuffer = bootbytes.asLongBuffer();
			LongBuffer cartbuffer = cartbytes.asLongBuffer();
			bootbuffer.get(boot, 0, bootbuffer.remaining());
			cartbuffer.get(cart, 0, cartbuffer.remaining());
			riscchip.loadmemory(boot);
			riscchip.loadcart(cart);
			riscchip.processchip(cycles);
			String clSource = ComputeLib.loadProgram("simulator.cl", true);
			riscchip.program = computelib.compileProgram(device, clSource);
			riscchip.memoryptr = computelib.createBufferL(device, RiscChip.memoryamount);
			computelib.writeBufferL(device, queue, riscchip.memoryptr, boot);
			riscchip.cartptr = computelib.createBufferL(device, RiscChip.cartamount);
			computelib.writeBufferL(device, queue, riscchip.cartptr, cart);
			long[] memoryout = new long[RiscChip.memoryamount];
			long[] cartout = new long[RiscChip.cartamount];
			long[] dispout = new long[RiscChip.displayamount];
			long[] regout = new long[RiscChip.registeramount*riscchip.risccores.length];
			riscchip.savememory(memoryout);
			riscchip.savecart(cartout);
			riscchip.savedisplay(dispout);
			riscchip.saveregistry(regout);
			byte[] memoryarray = new byte[RiscChip.memoryamount*8];
			byte[] cartoutarray = new byte[RiscChip.cartamount*8];
			byte[] dispoutarray = new byte[RiscChip.displayamount*8];
			byte[] regoutarray = new byte[RiscChip.registeramount*riscchip.risccores.length*8];
			ByteBuffer memorybytes = ByteBuffer.wrap(memoryarray);
			ByteBuffer cartoutbytes = ByteBuffer.wrap(cartoutarray);
			ByteBuffer dispoutbytes = ByteBuffer.wrap(dispoutarray);
			ByteBuffer regoutbytes = ByteBuffer.wrap(regoutarray);
			LongBuffer memorylongs = memorybytes.asLongBuffer();
			LongBuffer cartoutlongs = cartoutbytes.asLongBuffer();
			LongBuffer dispoutlongs = dispoutbytes.asLongBuffer();
			LongBuffer regoutlongs = regoutbytes.asLongBuffer();
			memorylongs.put(memoryout, 0, memoryout.length);
			cartoutlongs.put(cartout, 0, cartout.length);
			dispoutlongs.put(dispout, 0, dispout.length);
			regoutlongs.put(regout, 0, regout.length);
			filememoutput.write(memoryarray);
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
		private long program = NULL;
		private long memoryptr = NULL;
		private long cartptr = NULL;
		public RiscCore[] risccores;
		public static final int memoryamount = 65536*256;
		public static final int cartamount = 65536*256;
		public static final int displayamount = 512*512;
		public static final int registeramount = 65536;
		public long[] memoryram = new long[memoryamount];
		public long[] cartram = new long[cartamount];
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
		private long newregsptr = NULL;
		private long oldregsptr = NULL;
		public int corenum = 0;
		public int cyclenum = 0;
		private long[] newregisters = new long[RiscChip.registeramount];
		private long[] oldregisters = new long[RiscChip.registeramount];
		private long[] counters = {0, 0, 0, 0, 0, 0, 0 ,0};
		private long[] timers = {0, 0, 0, 0, 0, 0, 0 ,0};
		private Random[] randoms = {new Random(), new Random(), new Random(), new Random(), new Random(), new Random(), new Random(), new Random()};
		private int timerstep = 0;
		private long instructionstate = 0L;
		private long instructionstep = 0;
		private int programcounter = 0;
		private ByteBuffer instbytes = ByteBuffer.allocate(8);
		private ByteBuffer longbytes = ByteBuffer.allocate(8);
		private ByteBuffer longbytes2 = ByteBuffer.allocate(8);
		private ByteBuffer longbytes3 = ByteBuffer.allocate(8);
		
		public RiscCore(int corenumi) {
			corenum = corenumi;
		}
		
		public void processinstruction() {
		}
		public void updateregisters() {
		}
	}
}
