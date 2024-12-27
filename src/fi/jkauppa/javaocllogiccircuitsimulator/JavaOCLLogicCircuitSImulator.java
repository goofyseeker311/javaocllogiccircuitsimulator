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
		System.out.println("JavaOCLLogicCircuitSImulator v0.0.2");
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
		int[] newvalues = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		int[] oldvalues = new int[vc];
		Arrays.fill(oldvalues, 0);
		long newvaluesptr = computelib.createBuffer(device, vc);
		long oldvaluesptr = computelib.createBuffer(device, vc);
		computelib.writeBufferi(device, queue, newvaluesptr, newvalues);
		computelib.writeBufferi(device, queue, oldvaluesptr, oldvalues);
		
		long[] buffers = {circuitptr,oldvaluesptr,newvaluesptr};
		float ctimedif = computelib.runProgram(device, queue, program, "processgates", buffers, new int[]{0}, new int[]{gc/4}, 0, true);
		System.out.println(String.format("%.4f",ctimedif).replace(",", ".")+"ms\t device: "+devicename);
		
		System.out.println("done.");
	}
	
	private int[] parseCircuit(String circuit) {
		ArrayList<Integer> circuitarray = new ArrayList<Integer>();
		circuitarray.add(0); circuitarray.add(0); circuitarray.add(-1); circuitarray.add(1);
		circuitarray.add(2); circuitarray.add(1); circuitarray.add(-1); circuitarray.add(3);
		
		int[] circuitints = new int[circuitarray.size()];
		for (int i=0;i<circuitints.length;i++) {
			circuitints[i] = circuitarray.get(i);
		}
		return circuitints;
	}
}
