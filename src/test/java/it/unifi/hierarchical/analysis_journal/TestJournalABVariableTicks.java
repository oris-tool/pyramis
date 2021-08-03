package it.unifi.hierarchical.analysis_journal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

import org.junit.Test;
import it.unifi.hierarchical.analysis.HierarchicalSMPAnalysis;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.example.hsmp_journal.HSMP_BlockABVariableTicks;
import it.unifi.hierarchical.model.example.hsmp_journal.HSMP_BlockABVariableTicks1;
import it.unifi.hierarchical.model.example.hsmp_journal.HSMP_BlockABVariableTicks2;
import it.unifi.hierarchical.model.example.hsmp_journal.HSMP_BlockABVariableTicks3;


/**
 * Test to check correctness of analysis when there is a composite state inside another composite state.
 * This was the first test created. The idea was to check the correctness of the shift and project approach
 */
public class TestJournalABVariableTicks {

	//constant to force the different timeSteps of the states
	
	//Need to be chosen according to the model, based on which is the maximum time elapsed in a region or a state
	private static final double TIME_LIMIT = 1345;

	@Test
	public void test1() {    
		
		int j=3;

		for(int i=j; i>j-1;i--) {

			//HSMP
			//Build the model
			HierarchicalSMP model = null;
			double TIME_STEP = 0.;
			String filename = "";
			
			
			if(i==3) {
				TIME_STEP = -1.0;
				model = HSMP_BlockABVariableTicks.build();
				filename = "ABVariableTics01-24Fuori.txt";
			}else if(i==2) {
				TIME_STEP = -1.0;
				model = HSMP_BlockABVariableTicks1.build();
				filename = "ABVariableTics12-24Fuori.txt";
			}else if(i==1) {
				TIME_STEP = -1.0;
				model = HSMP_BlockABVariableTicks2.build();
				filename = "ABVariableTics08-32Fuori.txt";
			}else if(i==0) {
				TIME_STEP = -1.0;
				model = HSMP_BlockABVariableTicks3.build();
				filename = "ABVariableTics12-36Fuori.txt";
			}

			if(TIME_STEP == 0.0) {
				System.out.println("time step 0!!");
			}
			
			//Analyze
			Date start = new Date();
			HierarchicalSMPAnalysis analysis = new HierarchicalSMPAnalysis(model);
			Map<String, Double> ssHSMP = analysis.evaluateSteadyState(TIME_STEP, TIME_LIMIT);
			Double rsH12 = ssHSMP.get("State12");
			Double rsH13 = ssHSMP.get("State13");
			Double rsH14 = ssHSMP.get("State14");
			Double rsH31 = ssHSMP.get("State31");
			Double rsH32 = ssHSMP.get("State32");
			Double rsH22 = ssHSMP.get("State22");
			Double rsH23 = ssHSMP.get("State23");

			Double rsH211 = ssHSMP.get("State211");
			Double rsH212 = ssHSMP.get("State212");
			Double rsH311 = ssHSMP.get("State311");
			Double rsH312 = ssHSMP.get("State312");
			Double rsH411 = ssHSMP.get("State411");
			Double rsH412 = ssHSMP.get("State412");


			Double rsH11 = ssHSMP.get("State11");

			Date end = new Date();
			long time = end.getTime() - start.getTime();
			System.out.println("Time Hierarchical SMP analysis:" + time + "ms");

			double Arj = (rsH22+rsH312+rsH411);
			double Arp=rsH211;
			double Awork = 1 -Arj-Arp;
			double Brj = rsH212+rsH412;
			double Brp=rsH311;
			double Bwork = 1 -Brj-Brp;
			double rep= rsH212+rsH311;
			double rej= rsH22+rsH312+rsH212+rsH14;

			File file = new File(filename);
			try (PrintWriter writer = new PrintWriter(file)) {

				writer.write("time,"+time+"\n");
				writer.write("11,"+rsH11+"\n");
				writer.write("12,"+rsH12+"\n");
				writer.write("13,"+rsH13+"\n");
				writer.write("14,"+rsH14+"\n");
				writer.write("31,"+rsH31+"\n");
				writer.write("32,"+rsH32+"\n");
				writer.write("22,"+rsH22+"\n");
				writer.write("23,"+rsH23+"\n");
				writer.write("211,"+rsH211+"\n");
				writer.write("212,"+rsH212+"\n");
				writer.write("311,"+rsH311+"\n");
				writer.write("312,"+rsH312+"\n");
				writer.write("411,"+rsH411+"\n");
				writer.write("412,"+rsH412+"\n");

				writer.write("Arj,"+Arj+"\n");
				writer.write("Arp,"+Arp+"\n");
				writer.write("Awork,"+Awork+"\n");
				writer.write("Brj,"+Brj+"\n");
				writer.write("Brp,"+Brp+"\n");
				writer.write("Bwork,"+Bwork+"\n");
				writer.write("work,"+(rsH31+rsH32)+"\n");
				writer.write("rep,"+ rep+"\n");
				writer.write("rej,"+ rej+"\n");
				writer.write("notwork,"+(1.0-(rsH31+rsH32))+"\n");

			} catch (FileNotFoundException e) {
				System.out.println("errore");
				System.out.println(e.getMessage());
			}
		}

	}
}
