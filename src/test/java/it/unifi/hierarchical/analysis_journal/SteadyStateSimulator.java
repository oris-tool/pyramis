package it.unifi.hierarchical.analysis_journal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.oristool.analyzer.log.AnalysisLogger;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.MarkingCondition;
import org.oristool.petrinet.PetriNet;
import org.oristool.simulator.Sequencer;
import org.oristool.simulator.rewards.ContinuousRewardTime;
import org.oristool.simulator.rewards.NumericRewardResult;
import org.oristool.simulator.rewards.RewardEvaluator;
import org.oristool.simulator.stpn.STPNSimulatorComponentsFactory;

import it.unifi.hierarchical.model.example.pn_journal.PN_BlockAB;

public class SteadyStateSimulator {	

	public static void main(String[] args) {

		// Parameters
		int samplesNumber = 1344*10000; 



		// Model specification
		PetriNet pn = new PetriNet();
		Marking m = new Marking();        
		PN_BlockAB.build(pn, m);

		Set<String> rewardString = new HashSet<>();

		rewardString.addAll(Arrays.asList("A1","B1","A1detectedRepair",
				"A1detectedRejuv","Wait","WaitRejuvA","WaitRejuvB","B1detectedRepair","B1detectedRejuv","A1watchdogEnd","WaitRejuvA||WaitRejuvB"));

		Map<String,Double> steadyStates;

		Date start = new Date();
		
		// Model simulation
		steadyStates=simulate(pn,m, samplesNumber,rewardString);
		System.out.println("completed simulation");
		
		double rA1               =steadyStates.get("A1");                      
		double rB1               =steadyStates.get("B1");                      
		double rA1detectedRepair =steadyStates.get("A1detectedRepair");        
		double rA1detectedRejuv  =steadyStates.get("A1detectedRejuv");         
		double rWait             =steadyStates.get("Wait");                    
		double rWaitRejuvA       =steadyStates.get("WaitRejuvA");              
		double rWaitRejuvB       =steadyStates.get("WaitRejuvB");              
		double rB1detectedRepair =steadyStates.get("B1detectedRepair");        
        double rB1detectedRejuv  =steadyStates.get("B1detectedRejuv");         
		double rA1watchdogEnd    =steadyStates.get("A1watchdogEnd");           
 		double rWaitRejuv        =steadyStates.get("WaitRejuvA||WaitRejuvB");  
		
	
		
		Date end = new Date();
		
		double time = end.getTime() - start.getTime();
			
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

		File file = new File("Simulation.txt");
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



	public static Map<String,Double> simulate(PetriNet net, Marking initialMarking, int samplesNumber, Set<String> rewardStringSet) {

		Map<String,Double> steadyStates = new HashMap<>();
		Map<String,SteadyStateMarkingConditionProbability> rewardMap = new HashMap<>();

		Sequencer s = new Sequencer(net, initialMarking, new STPNSimulatorComponentsFactory(), new AnalysisLogger() {
			@Override
			public void log(String message) { }
			@Override
			public void debug(String string) { }
		});

		for(String rewardString : rewardStringSet) {
			// Create a reward (which is a sequencer observer)
			// sums up for every firing the time spent in a given condition
			SteadyStateMarkingConditionProbability reward = new SteadyStateMarkingConditionProbability(s, 
					new ContinuousRewardTime(new BigDecimal("0.1")), samplesNumber, MarkingCondition.fromString(rewardString));

			rewardMap.put(rewardString, reward);
			
			RewardEvaluator rewardEvaluator = new RewardEvaluator(reward, 1);
	    	

		}

		// Run simulation
		s.simulate();

		for(String rewardString : rewardStringSet) {

			SteadyStateMarkingConditionProbability reward = rewardMap.get(rewardString);
			// Get simulation results
			NumericRewardResult result = (NumericRewardResult) reward.evaluate();
			steadyStates.put(rewardString, result.getResult().doubleValue());
		}
		// Plot results
		return steadyStates;
	}


}
