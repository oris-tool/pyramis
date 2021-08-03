package it.unifi.hierarchical.analysis_journal;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

import it.unifi.hierarchical.analysis.HierarchicalSMPAnalysisForced;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.example.hsmp_journal.HSMP_JournalForNever;


/**
 * Execution of calculations
 *  
 */
public class TestNever {


	private static int LOOPS= 3; 



	//Need to be chosen according to the model, based on which is the maximum time elapsed in a region or a state
	private static final double TIME_LIMIT = 20;

	public static void main(String[] args){
		test1();
	}
	
	
	public static void test1() {    
		double[] tics= new double[]{0.05}; 

		double TIME_STEP;

		for(int i=0;i<tics.length;i++) {
			TIME_STEP = tics[i];

			try {


				String print= "testNever_"+TIME_STEP+".txt";
				
	
				//HSMP
				//Build the model
				HierarchicalSMP model = HSMP_JournalForNever.build();

				//Analyze
				Date start = new Date();
				HierarchicalSMPAnalysisForced analysis = new HierarchicalSMPAnalysisForced(model, LOOPS);
				Map<String, Double> ssHSMP = analysis.evaluateSteadyState(TIME_STEP, TIME_LIMIT);
				Double rsVm 	= ssHSMP.get("Vm"		);
				Double rsVmRes 	= ssHSMP.get("VmRes"	);
				Double rsVpost 	= ssHSMP.get("Vpost"	);

				Double rsVA 	= ssHSMP.get("VmA"	);
				Double rsVF 	= ssHSMP.get("VmF"	);


				Double rsV2 = ssHSMP.get("V2"	);


				Date end = new Date();
				long time = end.getTime() - start.getTime();
				System.out.println("Time Hierarchical SMP analysis:" + time + "ms");

				double s=0.0;
				s+=rsVm;
				s+=rsVmRes;
				s+=rsVpost;
				System.out.println("sum out= "+s);
				
				s=0.0;
				s+=rsVA;
				s+=rsVF;
				System.out.println("Vm = "+rsVm);
				System.out.println("in = "+s);
				

				File file = new File(print);
				try (PrintWriter writer = new PrintWriter(file)) {
					writer.write("TIME=  "		+time 		+"ms \n\n");

					writer.write("Vm      "		+rsVm 		+"\n");
					writer.write("VmA     "		+rsVA		+"\n");
					writer.write("VMF     "		+rsVF 		+"\n");
					
					
					writer.write("VmRes   "	+rsVmRes   	+"\n");

					writer.write("Vpost   "	+rsVpost   	+"\n");

					writer.write("Vpost   "	+rsVpost   	+"\n");

					writer.write("V2      "	+rsV2   +"\n");




				} catch (FileNotFoundException e) {
					System.out.println("errore");
					System.out.println(e.getMessage());
				}

			}
			catch(Exception e){
				System.out.println(e.getMessage());
			}
		}


	}
}
