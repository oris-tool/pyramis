package it.unifi.hierarchical.analysis_journal;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;


import it.unifi.hierarchical.analysis.HierarchicalSMPAnalysis;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.example.hsmp_journal.HSMP_JournalSensitivity;


/**
 * Execution of calculations
 *  
 */
public class PyramisSensitivity24 {


	

	public static void main(String[] args){
		test1();
	}
	

	public static void test1() {    
		
		double TIME_STEP= -1;
		double[] timeVMA=    new double[]{0.005};
		double[] timeR1= 	 new double[]{0.005};
		double[] timeVMMA=	 new double[]{0.00125};
		double[] timeEX= 	 new double[]{0.00125};
		
		int[] i24a = new int[] {90};
		int[] i96a = new int[] {48,60,72,84,108,120,132};
		int[] i96b = new int[] {54,66,78,102,114,126,144};
		
		
		double probDef = 0.97;
		
		int i=0;
		
		for(int j=0;j<1;j++) {
			double prob = probDef; //probA[j];
			int i96 =  96;//i96b[j];
			
			int i24 = i24a[j];
			
			double TIME_LIMIT = i96 +1;
			int LOOPS = (int) 3;
		
			try {


				String print= "v3_"+i96+"_"+i24+".txt";
				//in 168 si hanno sempre 6 cicli massimo





				//HSMP
				//Build the model
				HierarchicalSMP model = HSMP_JournalSensitivity.build(timeVMA[i],timeR1[i],timeVMMA[i],timeEX[i], timeVMMA[i], i24,i96, prob);

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
					writer.write("TIME=  "		+time/1000 		+"secs \n\n");

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
