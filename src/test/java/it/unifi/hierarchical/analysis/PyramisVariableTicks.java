package it.unifi.hierarchical.analysis;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;


import it.unifi.hierarchical.analysis.HierarchicalSMPAnalysisForced;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.example.hsmp.HSMP_JournalVariableTicks;


/**
 * Execution of calculations
 *  
 */
public class PyramisVariableTicks {


	private static int LOOPS= 5; 



	//Need to be chosen according to the model, based on which is the maximum time elapsed in a region or a state
	private static final double TIME_LIMIT = 97;

	public static void main(String[] args){
		test1();
	}
	

	
	public static void test1() {    
		
		//these arrays correspond to the time tick at varying levels of the hierarchy, it is possible to modify them
		double TIME_STEP= -1;
		double[] timeVMA=    new double[]{0.01,0.05,0.0025};	
		double[] timeR1= 	 new double[]{0.01,0.05,0.0025};	
		double[] timeVMMA=	 new double[]{0.01,0.05,0.0025}; 
		double[] timeEX= 	 new double[]{0.01,0.05,0.0025}; 

		for(int i=0;i<timeVMA.length;i++) {
		
			try {


				String print="src//main//resources//pyramis//"+i+"_varyingTimeticks_"+timeVMA[i]+"_"+timeR1[i]+"_"+timeVMMA[i]+"_"+timeEX[i]+".txt";
				


				//HSMP
				//Build the model
				HierarchicalSMP model = HSMP_JournalVariableTicks.build(timeVMA[i],timeR1[i],timeVMMA[i],timeEX[i], timeVMMA[i]);

				//Analyze
				Date start = new Date();
				HierarchicalSMPAnalysisForced analysis = new HierarchicalSMPAnalysisForced(model, LOOPS);
				Map<String, Double> ssHSMP = analysis.evaluateSteadyState(TIME_STEP, TIME_LIMIT);
				Double rsVm 	= ssHSMP.get("Vm"		);
				Double rsVmR 	= ssHSMP.get("VmR"		);
				Double rsVmFD 	= ssHSMP.get("VmFD"		);
				Double rsVmRej 	= ssHSMP.get("VmRej"	);
				Double rsVmF 	= ssHSMP.get("VmF"		);
				Double rsVmA 	= ssHSMP.get("VmA"		);
				Double rsVmRes 	= ssHSMP.get("VmRes"	);



				Double rsVmmA 	= ssHSMP.get("VmmA"		);
				Double rsVmmF 	= ssHSMP.get("VmmF"		);
				Double rsVmmRejW= ssHSMP.get("VmmRejW"	);
				Double rsVmmFD 	= ssHSMP.get("VmmFD"	);
				Double rsVmmR 	= ssHSMP.get("VmmR"		);
				Double rsVmmRes = ssHSMP.get("VmmRes"	);
				Double rsVmSW 	= ssHSMP.get("VmSW"		);
				Double rsVmmRej = ssHSMP.get("VmmRej"	);


				Date end = new Date();
				long time = end.getTime() - start.getTime();
				System.out.println("Time Hierarchical SMP analysis:" + time + "ms");



				File file = new File(print);
				file.getParentFile().mkdirs();
				
				try (PrintWriter writer = new PrintWriter(file)) {
					writer.write("TIME=  "		+time 		+"ms \n\n");

					writer.write("Vm      "		+rsVm 		+"\n");
					writer.write("VmR     "		+rsVmR 		+"\n");
					writer.write("VmFD    "		+rsVmFD 	+"\n");
					writer.write("VmRej   "	+rsVmRej 	+"\n");
					writer.write("VmF     "		+rsVmF 		+"\n");
					writer.write("VmA     "		+rsVmA 		+"\n");
					writer.write("VmRes   "	+rsVmRes   	+"\n");



					writer.write("VmmA    "		+rsVmmA 	+"\n");
					writer.write("VmmF    "		+rsVmmF 	+"\n");
					writer.write("VmmRejW "	+rsVmmRejW	+"\n");
					writer.write("VmmFD   "	+rsVmmFD 	+"\n");
					writer.write("VmmR    "		+rsVmmR 	+"\n");
					writer.write("VmmRes  "	+rsVmmRes   +"\n");
					writer.write("VmSW    "		+rsVmSW 	+"\n");
					writer.write("VmmRej  "	+rsVmmRej   +"\n");




				} catch (FileNotFoundException e) {
					System.out.println("errore");
					System.out.println(e.getMessage());
				}

			}
			catch(Exception e){

			}
		}


	}
}
