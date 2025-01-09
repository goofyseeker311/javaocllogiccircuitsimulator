package fi.jkauppa.javaocllogiccircuitsimulator;

import java.util.ArrayList;
import java.util.Arrays;

import fi.jkauppa.javaocllogiccircuitsimulator.ComputeLib.Device;

public class JavaOCLLogicCircuitSimulator {
	private ComputeLib computelib = new ComputeLib();
	private int de;
	private int re;
	private final int cs = 4;

	public JavaOCLLogicCircuitSimulator(int vde, int vre) {
		this.de = vde;
		this.re = vre;
	}

	public static void main(String[] args) {
		System.out.println("JavaOCLLogicCircuitSimulator v0.1.4");
		int de = 0;
		int re = 1000000;
		try {de = Integer.parseInt(args[0]);} catch(Exception ex) {}
		try {re = Integer.parseInt(args[1]);} catch(Exception ex) {}
		JavaOCLLogicCircuitSimulator app = new JavaOCLLogicCircuitSimulator(de,re);
		app.run();
		System.out.println("exit.");
	}
	
	public void run() {
		System.out.println("init.");
		System.out.println("Repeat count: "+this.re);

		long device = computelib.devicelist[this.de];
		Device devicedata = computelib.devicemap.get(device);
		long queue = devicedata.queue;
		String devicename = devicedata.devicename;
		System.out.println("Using device["+de+"]: "+devicename);
		
		String clSource = ComputeLib.loadProgram("res/clprograms/simulator.cl", true);
		long program = computelib.compileProgram(device, clSource);

		String circuitcode = ComputeLib.loadProgram("res/circuits/circuit.lc", true);
		CodeBlocks codeblocks = parseCode(circuitcode);
		int[] circuitints = codeblocks.maincode.circuit;

		int gc = circuitints.length;
		long circuitptr = computelib.createBuffer(device, gc);
		computelib.writeBufferi(device, queue, circuitptr, circuitints);
		
		int vc = 77;
		int[] newvalues = {-1,5,0,~255,0,255,128,0,1,2,0,2,4,0,-2,-2,0,-4,4,0,2,-1,0,4,1,0,8,1,0,-9,0,7,3,0,5,4,0,2,3,0,8,4,0,
				Float.floatToIntBits((float)(Math.PI)),0,Float.floatToIntBits((float)(Math.PI/2.0f)),0,Float.floatToIntBits(1.0f),0,Float.floatToIntBits(-1.0f),0,Float.floatToIntBits(1.0f),0,Float.floatToIntBits(1.557408f),0,
				Float.floatToIntBits(100.0f),0,Float.floatToIntBits(4.6051702f),0,3,Float.floatToIntBits(-2.0f),0,Float.floatToIntBits(4.5f),Float.floatToIntBits(7.2f),0,Float.floatToIntBits(3.1f),Float.floatToIntBits(1.2f),0,
				Float.floatToIntBits(1.8f),Float.floatToIntBits(2.5f),0,Float.floatToIntBits(-3.7f),Float.floatToIntBits(-0.85f),0,50,1,0};
		int[] oldvalues = new int[vc];
		Arrays.fill(oldvalues, 0);
		long newvaluesptr = computelib.createBuffer(device, vc);
		long oldvaluesptr = computelib.createBuffer(device, vc);
		computelib.writeBufferi(device, queue, newvaluesptr, newvalues);
		computelib.writeBufferi(device, queue, oldvaluesptr, oldvalues);
		
		float ctimedif = 0.0f;
		computelib.insertBarrier(queue);
		ctimedif = computelib.runProgram(device, queue, program, "updatevalues", new long[]{oldvaluesptr,newvaluesptr}, new int[]{0}, new int[]{vc}, 0, false);
		computelib.insertBarrier(queue);
		ctimedif = computelib.runProgram(device, queue, program, "processgates", new long[]{circuitptr,oldvaluesptr,newvaluesptr}, new int[]{0}, new int[]{gc/cs}, re, true)/(float)re;
		System.out.println(String.format("%.4f",ctimedif).replace(",", ".")+"ms\t device: "+devicename);

		computelib.readBufferi(device, queue, newvaluesptr, newvalues);
		computelib.readBufferi(device, queue, oldvaluesptr, oldvalues);
		
		for (int i=0;i<vc;i++) {
			System.out.println("values["+i+"]: "+oldvalues[i]+"("+Float.intBitsToFloat(oldvalues[i])+"f) => "+newvalues[i]+"("+Float.intBitsToFloat(newvalues[i])+"f)");
		}
		
		System.out.println("done.");
	}
	
	private class CodeBlock {
		String code = null;
		int[] circuit = null;
		String name = null;
		int arg1 = -1;
		int arg2 = -1;
		int sto3 = -1;
		int height = -1;
		int depth = -1;
	}

	private class CodeBlocks {
		CodeBlock maincode = null;
		CodeBlock[] userblocks = null;
	}
	
	private CodeBlocks parseCode(String code) {
		CodeBlocks codeblocks = new CodeBlocks();
		codeblocks.maincode = new CodeBlock();
		codeblocks.maincode.code = "";
		codeblocks.maincode.name = "MAIN";
		ArrayList<CodeBlock> codearray = new ArrayList<CodeBlock>();
		boolean maincodeblock = true;
		
		String[] codelines = code.split("\n");
		for (int i=0;i<codelines.length;i++) {
			String codeline = codelines[i].trim();
			if (codeline.length()>0) {
				if (codeline.startsWith("//")) {
				} else if (codeline.startsWith("#")) {
					int arg1 = -1;
					int arg2 = -1;
					int sto3 = -1;
					String[] codelineparts = codeline.split(":");
					String[] codenameparts = codelineparts[0].split(" ");
					String codename = codenameparts[0].substring(1).trim();
					if (codenameparts.length>2) {
						arg2 = Integer.parseInt(codenameparts[2].trim());
					}
					if (codenameparts.length>1) {
						arg1 = Integer.parseInt(codenameparts[1].trim());
					}
					if (codelineparts.length>1) {
						sto3 = Integer.parseInt(codelineparts[1].trim());
					}
					
					if (codename.equals("MAIN")) {
						maincodeblock = true;
						codeblocks.maincode.arg1 = arg1;
						codeblocks.maincode.arg2 = arg2;
						codeblocks.maincode.sto3 = sto3;
					} else if(codename.startsWith("##"))  {
						maincodeblock = true;
					} else {
						maincodeblock = false;
						CodeBlock newblock = new CodeBlock();
						newblock.code = "";
						newblock.name = codename;
						newblock.arg1 = arg1;
						newblock.arg2 = arg2;
						newblock.sto3 = sto3;
						codearray.add(newblock);
					}
				} else {
					if (maincodeblock) {
						codeblocks.maincode.code += codeline+"\n";
					} else {
						codearray.get(codearray.size()-1).code += codeline+"\n";
					}
				}
			}
		}
		codeblocks.userblocks = codearray.toArray(new CodeBlock[codearray.size()]);
		codeblocks.maincode.circuit = parseCircuit(codeblocks.maincode.code);
		int me = codeblocks.maincode.circuit.length/cs;
		for (int e=0;e<me;e++) {
			if (codeblocks.maincode.circuit[e*cs+1]>codeblocks.maincode.height) {codeblocks.maincode.height=codeblocks.maincode.circuit[e*cs+1];}
			if (codeblocks.maincode.circuit[e*cs+2]>codeblocks.maincode.height) {codeblocks.maincode.height=codeblocks.maincode.circuit[e*cs+2];}
			if (codeblocks.maincode.circuit[e*cs+3]>codeblocks.maincode.height) {codeblocks.maincode.height=codeblocks.maincode.circuit[e*cs+3];}
		}
		for (int i=0;i<codeblocks.userblocks.length;i++) {
			codeblocks.userblocks[i].circuit = parseCircuit(codeblocks.userblocks[i].code);
			int ce = codeblocks.userblocks[i].circuit.length/cs;
			for (int e=0;e<ce;e++) {
				if (codeblocks.userblocks[i].circuit[e*cs+1]>codeblocks.userblocks[i].height) {codeblocks.userblocks[i].height=codeblocks.userblocks[i].circuit[e*cs+1];}
				if (codeblocks.userblocks[i].circuit[e*cs+2]>codeblocks.userblocks[i].height) {codeblocks.userblocks[i].height=codeblocks.userblocks[i].circuit[e*cs+2];}
				if (codeblocks.userblocks[i].circuit[e*cs+3]>codeblocks.userblocks[i].height) {codeblocks.userblocks[i].height=codeblocks.userblocks[i].circuit[e*cs+3];}
			}
		}
		return codeblocks;
	}
	
	private int[] parseCircuit(String circuit) {
		ArrayList<Integer> circuitarray = new ArrayList<Integer>();
		String[] circuitlines = circuit.split("\n");
		for (int i=0;i<circuitlines.length;i++) {
			int arg1 = -1;
			int oper = -1;
			int arg2 = -1;
			int sto3 = -1;

			String circuitline = circuitlines[i].trim();
			
			if (circuitline.length()>0) {
				String[] circuitlineparts = circuitline.split(":");
				String circuitlineop = circuitlineparts[0].trim();
				String circuitlinestore = circuitlineparts[1].trim();
				String[] circuitlineopparts = circuitlineop.split(" ");
				
				String operString = circuitlineopparts[0].trim();
				if (circuitlineopparts.length>2) {
					arg2 = Integer.parseInt(circuitlineopparts[2].trim());
				}
				if (circuitlineopparts.length>1) {
					arg1 = Integer.parseInt(circuitlineopparts[1].trim());
				}
				sto3 = Integer.parseInt(circuitlinestore);
				
				if (operString.equals("BUF")) {
					oper = 0;
				} if (operString.equals("NOT")) {
					oper = 1;
				} if (operString.equals("AND")) {
					oper = 2;
				} if (operString.equals("OR")) {
					oper = 3;
				} if (operString.equals("XOR")) {
					oper = 4;
				} if (operString.equals("NAND")) {
					oper = 5;
				} if (operString.equals("NOR")) {
					oper = 6;
				} if (operString.equals("XNOR")) {
					oper = 7;
				} if (operString.equals("SHL")) {
					oper = 8;
				} if (operString.equals("SHR")) {
					oper = 9;
				} if (operString.equals("NEGi")) {
					oper = 10;
				} if (operString.equals("SUMi")) {
					oper = 11;
				} if (operString.equals("SUBi")) {
					oper = 12;
				} if (operString.equals("MULi")) {
					oper = 13;
				} if (operString.equals("DIVi")) {
					oper = 14;
				} if (operString.equals("COS")) {
					oper = 15;
				} if (operString.equals("SIN")) {
					oper = 16;
				} if (operString.equals("TAN")) {
					oper = 17;
				} if (operString.equals("ACOS")) {
					oper = 18;
				} if (operString.equals("ASIN")) {
					oper = 19;
				} if (operString.equals("ATAN")) {
					oper = 20;
				} if (operString.equals("LOG")) {
					oper = 21;
				} if (operString.equals("EXP")) {
					oper = 22;
				} if (operString.equals("POW")) {
					oper = 23;
				} if (operString.equals("SQRT")) {
					oper = 24;
				} if (operString.equals("NROOT")) {
					oper = 25;
				} if (operString.equals("NULL")) {
					oper = 26;
				} if (operString.equals("ITOF")) {
					oper = 27;
				} if (operString.equals("FTOI")) {
					oper = 28;
				} if (operString.equals("MGET")) {
					oper = 29;
				} if (operString.equals("MSTO")) {
					oper = 30;
				} if (operString.equals("IFBUF")) {
					oper = 31;
				} if (operString.equals("NEG")) {
					oper = 32;
				} if (operString.equals("SUM")) {
					oper = 33;
				} if (operString.equals("SUB")) {
					oper = 34;
				} if (operString.equals("MUL")) {
					oper = 35;
				} if (operString.equals("DIV")) {
					oper = 36;
				}
				
				circuitarray.add(oper); circuitarray.add(arg1); circuitarray.add(arg2); circuitarray.add(sto3);
			}
		}
		
		int[] circuitints = new int[circuitarray.size()];
		for (int i=0;i<circuitints.length;i++) {
			circuitints[i] = circuitarray.get(i);
		}
		return circuitints;
	}
}
