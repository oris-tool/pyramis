package it.unifi.hierarchical.analysis_journal;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.SteadyStateSolution;
import org.oristool.models.stpn.steady.RegSteadyState;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;

import it.unifi.hierarchical.analysis.HierarchicalSMPAnalysis;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.example.hsmp_journal.HSMP_AB_DifferentCombinations;
import it.unifi.hierarchical.model.example.pn_journal.PN_ABDifferentCombinations;


/**
 * Test to check correctness of analysis when there is a composite state inside another composite state.
 * This was the first test created. The idea was to check the correctness of the shift and project approach
 */
public class TestJournalAB_DifferentCombinations {

	private static final double TIME_STEP = 0.1;

	@Test
	public void test1() {     

		List<Integer> weekABL = Arrays.asList(8);

		List<Integer> RgtRejL = Arrays.asList(16,20,24);
		List<Integer> RgtRepL = Arrays.asList(48,72,96);

		//"fixed" due to the way the unrolled BorderExit has been calculated
		List<Integer> weekAL = Arrays.asList(1,2,4);

		for(Integer weekAB : weekABL) {
			for(Integer weekA : weekAL) {

				double TIME_LIMIT = Math.max(weekA, weekAB)*168+1;

				for(Integer RgtRej : RgtRejL) {
					for(Integer RgtRep : RgtRepL) {

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
						System.out.println("Time Hierarchical SMP analysis:" + (end.getTime() - start.getTime()) + "ms");


						File file = new File(weekA+"_"+RgtRej+"_"+RgtRep+".txt");
						try (PrintWriter writer = new PrintWriter(file)) {

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

							double Arj = (rsH22+rsH312+rsH411);
							double Arp=rsH211;
							double Awork = 1 -Arj-Arp;
							double Brj = rsH311+rsH412;
							double Brp=rsH212;
							double Bwork = 1 -Brj-Brp;


							writer.write("Arj,"+Arj+"\n");
							writer.write("Arp,"+Arp+"\n");
							writer.write("Awork,"+Awork+"\n");
							writer.write("Brj,"+Brj+"\n");
							writer.write("Brp,"+Brp+"\n");
							writer.write("Bwork,"+Bwork+"\n");
							writer.write("work,"+(rsH31+rsH32)+"\n");
							writer.write("rprj,"+(1.0-(rsH31+rsH32))+"\n");

						} catch (FileNotFoundException e) {
							System.out.println("errore");
							System.out.println(e.getMessage());
						}

						PetriNet net = new PetriNet();
						Marking m = new Marking();
						PN_ABDifferentCombinations.build(net, m, RgtRej,RgtRep,weekA,weekAB);

						start = new Date();
						RegSteadyState analysisPN = RegSteadyState.builder().build();
						SteadyStateSolution<Marking> ssPN = analysisPN.compute(net, m);
						RewardRate rwA1                  = RewardRate.fromString("A1");
						RewardRate rwB1                  = RewardRate.fromString("B1");
						RewardRate rwA1detectedRepair    = RewardRate.fromString("A1detectedRepair");
						RewardRate rwA1detectedRejuv     = RewardRate.fromString("A1detectedRejuv");
						RewardRate rwWait                = RewardRate.fromString("Wait");
						RewardRate rwWaitRejuvA          = RewardRate.fromString("WaitRejuvA");
						RewardRate rwWaitRejuvB          = RewardRate.fromString("WaitRejuvB");
						RewardRate rwB1detectedRepair    = RewardRate.fromString("B1detectedRepair");
						RewardRate rwB1detectedRejuv     = RewardRate.fromString("B1detectedRejuv");                                                                                                
						RewardRate rwA1watchdogEnd       = RewardRate.fromString("A1watchdogEnd");                                                                                                

						RewardRate rwWaitRejuv           = RewardRate.fromString("WaitRejuvA||WaitRejuvB");                                                                 


						SteadyStateSolution<RewardRate> rewardPN = SteadyStateSolution.computeRewards(ssPN, rwA1, rwB1, rwA1detectedRepair, rwA1detectedRejuv, 
								rwWait, rwWaitRejuvA, rwWaitRejuvB, rwB1detectedRepair, rwB1detectedRejuv, rwA1watchdogEnd, rwWaitRejuv);

						double rA1               = rewardPN.getSteadyState().get(rwA1).doubleValue();
						//double rB1               = rewardPN.getSteadyState().get(rwB1).doubleValue();
						double rA1detectedRepair = rewardPN.getSteadyState().get(rwA1detectedRepair).doubleValue();
						double rA1detectedRejuv  = rewardPN.getSteadyState().get(rwA1detectedRejuv).doubleValue();
						//double rWait             = rewardPN.getSteadyState().get(rwWait).doubleValue();
						double rWaitRejuvA       = rewardPN.getSteadyState().get(rwWaitRejuvA).doubleValue();
						double rWaitRejuvB       = rewardPN.getSteadyState().get(rwWaitRejuvB).doubleValue();
						double rB1detectedRepair = rewardPN.getSteadyState().get(rwB1detectedRepair).doubleValue();
						double rB1detectedRejuv  = rewardPN.getSteadyState().get(rwB1detectedRejuv).doubleValue();
						double rA1watchdogEnd    = rewardPN.getSteadyState().get(rwA1watchdogEnd).doubleValue();


						double rWaitRejuv = rewardPN.getSteadyState().get(rwWaitRejuv).doubleValue();


						end = new Date();

						double time = end.getTime() - start.getTime();
						System.out.println("Time Regenerative SS  analysis:" + (time) + "ms");

						double Arj = rB1detectedRejuv+rWaitRejuvA+rA1watchdogEnd;
						double Arp = rA1detectedRepair;
						//not only A1 but also when there is a token in B1detectedRepair and resetB1
						double Awork = 1 -Arj-Arp;

						double Brj = rA1detectedRejuv+rWaitRejuvB;
						double Brp=  rB1detectedRepair;
						double Bwork = 1 -Brj-Brp;
						double rep= rB1detectedRepair+rA1detectedRepair;
						double rej= rA1detectedRejuv+rB1detectedRejuv+rWaitRejuv+rA1watchdogEnd;

						//B1 is active even when A1 is undergoing rejuvenation
						double work = rA1;

						file = new File("Regen_"+weekA+"_"+RgtRej+"_"+RgtRep+".txt");
						try (PrintWriter writer = new PrintWriter(file)) {

							writer.write("time,"+time+"\n");

							writer.write("Arj,"+Arj+"\n");
							writer.write("Arp,"+Arp+"\n");
							writer.write("Awork,"+Awork+"\n");
							writer.write("Brj,"+Brj+"\n");
							writer.write("Brp,"+Brp+"\n");
							writer.write("Bwork,"+Bwork+"\n");
							writer.write("work,"+work+"\n");
							writer.write("rep,"+ rep+"\n");
							writer.write("rej,"+ rej+"\n");
							writer.write("notwork,"+(1.0-work)+"\n");

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
