package fi.jkauppa.javaocllogiccircuitsimulator;

import java.util.ArrayList;
import java.util.Arrays;

import fi.jkauppa.javaocllogiccircuitsimulator.ComputeLib.Device;

public class JavaOCLLogicCircuitSImulator {
	private ComputeLib computelib = new ComputeLib();
	private int de;

	public JavaOCLLogicCircuitSImulator(int vde) {
		this.de = vde;
	}

	public static void main(String[] args) {
		System.out.println("JavaOCLLogicCircuitSImulator v0.0.4");
		int de = 0;
		try {de = Integer.parseInt(args[0]);} catch(Exception ex) {}
		JavaOCLLogicCircuitSImulator app = new JavaOCLLogicCircuitSImulator(de);
		app.run();
		System.out.println("exit.");
	}
	
	public void run() {
		System.out.println("init.");

		long device = computelib.devicelist[this.de];
		Device devicedata = computelib.devicemap.get(device);
		long queue = devicedata.queue;
		String devicename = devicedata.devicename;
		System.out.println("Using device["+de+"]: "+devicename);
		
		String clSource = ComputeLib.loadProgram("res/clprograms/simulator.cl", true);
		long program = computelib.compileProgram(device, clSource);

		String circuit = ComputeLib.loadProgram("res/circuits/circuit.lc", true);
		int[] circuitints = parseCircuit(circuit);

		int gc = circuitints.length;
		long circuitptr = computelib.createBuffer(device, gc);
		computelib.writeBufferi(device, queue, circuitptr, circuitints);
		
		int vc = 57;
		int[] newvalues = {5,0,~255,0,255,128,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		int[] oldvalues = new int[vc];
		Arrays.fill(oldvalues, 0);
		long newvaluesptr = computelib.createBuffer(device, vc);
		long oldvaluesptr = computelib.createBuffer(device, vc);
		computelib.writeBufferi(device, queue, newvaluesptr, newvalues);
		computelib.writeBufferi(device, queue, oldvaluesptr, oldvalues);
		
		float ctimedif = 0.0f;
		ctimedif = computelib.runProgram(device, queue, program, "updatevalues", new long[]{oldvaluesptr,newvaluesptr}, new int[]{0}, new int[]{vc}, 0, true);
		ctimedif = computelib.runProgram(device, queue, program, "processgates", new long[]{circuitptr,oldvaluesptr,newvaluesptr}, new int[]{0}, new int[]{gc/4}, 0, true);
		System.out.println(String.format("%.4f",ctimedif).replace(",", ".")+"ms\t device: "+devicename);

		computelib.readBufferi(device, queue, newvaluesptr, newvalues);
		computelib.readBufferi(device, queue, oldvaluesptr, oldvalues);
		
		System.out.println("done.");
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
				
				arg1 = Integer.parseInt(circuitlineopparts[0].trim());
				String operString = circuitlineopparts[1].trim();
				if (circuitlineopparts.length>2) {
					arg2 = Integer.parseInt(circuitlineopparts[2].trim());
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
				} if (operString.equals("NEG")) {
					oper = 10;
				} if (operString.equals("SUM")) {
					oper = 11;
				} if (operString.equals("MUL")) {
					oper = 12;
				} if (operString.equals("DIV")) {
					oper = 13;
				} if (operString.equals("COS")) {
					oper = 14;
				} if (operString.equals("SIN")) {
					oper = 15;
				} if (operString.equals("TAN")) {
					oper = 16;
				} if (operString.equals("ACOS")) {
					oper = 17;
				} if (operString.equals("ASIN")) {
					oper = 18;
				} if (operString.equals("ATAN")) {
					oper = 19;
				} if (operString.equals("LOG")) {
					oper = 20;
				} if (operString.equals("EXP")) {
					oper = 21;
				} if (operString.equals("POW")) {
					oper = 22;
				} if (operString.equals("SQRT")) {
					oper = 23;
				} if (operString.equals("NROOT")) {
					oper = 24;
				} if (operString.equals("ZERO")) {
					oper = 25;
				} if (operString.equals("ITOF")) {
					oper = 26;
				} if (operString.equals("FTOI")) {
					oper = 27;
				} if (operString.equals("MGET")) {
					oper = 28;
				} if (operString.equals("MSTO")) {
					oper = 29;
				} if (operString.equals("IFBUF")) {
					oper = 30;
				}
				
				circuitarray.add(arg1); circuitarray.add(oper); circuitarray.add(arg2); circuitarray.add(sto3);
			}
		}
		
		int[] circuitints = new int[circuitarray.size()];
		for (int i=0;i<circuitints.length;i++) {
			circuitints[i] = circuitarray.get(i);
		}
		return circuitints;
	}
}
