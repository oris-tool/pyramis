package it.unifi.hierarchical.analysis_journal;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;


import it.unifi.hierarchical.analysis.HierarchicalSMPAnalysis;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.example.hsmp_journal.HSMP_AB_DifferentCombinations;
import it.unifi.hierarchical.model.example.hsmp_journal.HSMP_C_DifferentCombinations;


/**
 * Test to check correctness of analysis when there is a composite state inside another composite state.
 * This was the first test created. The idea was to check the correctness of the shift and project approach
 */
public class TestJournal_DifferentCombinations {

	private static final double TIME_STEP = 0.8;

	@Test
	public void test1() {     

		List<Integer> weekABL = Arrays.asList(8);

		List<Integer> RgtRejL = Arrays.asList(16,20,24);
		List<Integer> RgtRepL = Arrays.asList(48,72,96);

		//"fixed" due to the way the unrolled BorderExit has been calculated
		List<Integer> weekAL = Arrays.asList(1,2,4);

		for(Integer weekAB : weekABL) {
			double TIME_LIMIT = weekAB*168+1;

			for(Integer RgtRej : RgtRejL) {
				for(Integer RgtRep : RgtRepL) {

					//same values for the AB and C blocks
					HierarchicalSMP modelC = HSMP_C_DifferentCombinations.build(
							RgtRej, RgtRep, weekAB);

					//Analyze
					Date startC = new Date();
					HierarchicalSMPAnalysis analysisC = new HierarchicalSMPAnalysis(modelC);
					Map<String, Double> ssHSMPC = analysisC.evaluateSteadyState(TIME_STEP, TIME_LIMIT);
					Double rsCH1 = ssHSMPC.get("State1");
					Double rsCH2 = ssHSMPC.get("State2");
					Double rsCH3 = ssHSMPC.get("State3");

					Date endC = new Date();
					long timeC = endC.getTime() - startC.getTime();

					File fileC = new File("C_"+RgtRej+"_"+RgtRep+".txt");
					try (PrintWriter writerC = new PrintWriter(fileC)) {

						writerC.write("time,"+timeC+"\n");
						writerC.write("1,"+rsCH1+"\n");
						writerC.write("2,"+rsCH2+"\n");
						writerC.write("3,"+rsCH3+"\n");

					} catch (FileNotFoundException e) {
						System.out.println("errore");
						System.out.println(e.getMessage());
					}

					for(Integer weekA : weekAL) {

						//HSMP
						//Build the model
						HierarchicalSMP model = HSMP_AB_DifferentCombinations.build(
								RgtRej, RgtRep, weekA, weekAB);

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

						File file = new File("D_"+weekA+"_"+RgtRej+"_"+RgtRep+".txt");
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


						double blockC12work= (1.-(1.-rsCH1)*(1.-rsCH1));
						double blockAB1work=rsH31+rsH32;
						double blockAB1AB2work= (1.-(1.-blockAB1work)*(1.-blockAB1work));

						double working = blockC12work*blockAB1AB2work;

						//at least a component is rejuvenating/repairing
						//! does not mean that the system is down !

						double rejuvenation = 1. - (1-rej)*(1-rej)*(1-rsCH3)*(1-rsCH3);
						double repair = 1. - (1-rep)*(1-rep)*(1-rsCH2)*(1-rsCH2);

						double meanRej =(Arj+Brj)*2+(rsCH3)*2;
						double meanRep =(Arp+Brp)*2+(rsCH2)*2;		

						File fileJournal = new File("Journal_"+weekA+"_"+RgtRej+"_"+RgtRep+".txt");
						try (PrintWriter writer = new PrintWriter(fileJournal)) {

							writer.write("timeTotal,"+(time+timeC)+"\n");
							writer.write("work,"+working+"\n");
							writer.write("notwork,"+(1.-working)+"\n");
							writer.write("inrej,"+rejuvenation+"\n");
							writer.write("inrep,"+repair+"\n");
							writer.write("meanrej,"+meanRej+"\n");
							writer.write("meanrep,"+meanRep+"\n");


						} catch (FileNotFoundException e) {
							System.out.println("errore");
							System.out.println(e.getMessage());
						}

					}
				}
			}	
		}


	}
}
