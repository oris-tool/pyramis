package it.unifi.hierarchical.analysis_journal;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;


import it.unifi.hierarchical.analysis.HierarchicalSMPAnalysis;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.example.hsmp_journal.HSMP_JournalNoCycles;
import it.unifi.hierarchical.model.example.hsmp_journal.HSMP_JournalNoCyclesNoExp;
import it.unifi.hierarchical.model.example.hsmp_journal.HSMP_JournalOnlyFix;


/**
 * Execution of calculations
 *  
 */
public class PyramisNoCycles {


	private static int LOOPS= 0; 

	private static boolean expol = false;
	
	
	//forza a uscire con 168 non con VmmA
	private static boolean onlyFix = false;
	

	//Need to be chosen according to the model, based on which is the maximum time elapsed in a region or a state
	private static final double TIME_LIMIT = 169;

	public static void main(String[] args){
		test1();
	}


	public static void test1() {    
		double[] tics= new double[]{0.02};

		double TIME_STEP;

		for(int i=0;i<tics.length;i++) {
			TIME_STEP = tics[i];

			try {
				String print;
				HierarchicalSMP model;
				if(expol) {

					print= "noCycles_"+TIME_STEP+".txt";

					//HSMP
					//Build the model
					model = HSMP_JournalNoCycles.build();
				}else if(!onlyFix){
					print= "nocyclesNoExp_"+TIME_STEP+".txt";

					//HSMP
					//Build the model
					model = HSMP_JournalNoCyclesNoExp.build();
				}else {
					print= "onlyFix_"+TIME_STEP+".txt";
					System.out.println("im here");
					//HSMP
					//Build the model
					model = HSMP_JournalOnlyFix.build();
				}
				//Analyze
				Date start = new Date();
				HierarchicalSMPAnalysis analysis = new HierarchicalSMPAnalysis(model, LOOPS);
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
