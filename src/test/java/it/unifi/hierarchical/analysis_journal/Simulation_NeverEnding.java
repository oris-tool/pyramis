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
import org.oristool.simulator.stpn.TransientMarkingConditionProbability;

import it.unifi.hierarchical.model.example.pn_journal.PN_Cycles;
import it.unifi.hierarchical.model.example.pn_journal.PN_NeverEnding;

/**
 * @author fsantoni
 *
 */
public class Simulation_NeverEnding {	

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// Parameters
		int samplesNumber = 10000; 



		// Model specification
		PetriNet pn = new PetriNet();
		Marking m = new Marking();        
		PN_NeverEnding.build(pn, m);

		Set<String> rewardString = new HashSet<>();

		rewardString.addAll(Arrays.asList("Vm"));

		Map<String,Double> steadyStates;

		Date start = new Date();
		
		// Model simulation
		steadyStates=simulate(pn,m, samplesNumber,rewardString);
		System.out.println("completed simulation");
		
		double rVm               =steadyStates.get("Vm");                     
	
		
	
		
		Date end = new Date();
		
		double time = end.getTime() - start.getTime();
	
		File file = new File("Simulation.txt");
		try (PrintWriter writer = new PrintWriter(file)) {

			writer.write("time,"+time+"\n");

			writer.write("Vm"+rVm+"\n");

		} catch (FileNotFoundException e) {
			System.out.println("errore");
			System.out.println(e.getMessage());
		}

	}



	public static Map<String,Double> simulate(PetriNet net, Marking initialMarking, int samplesNumber, Set<String> rewardStringSet) {

		Map<String,Double> steadyStates = new HashMap<>();
		Map<String, TransientMarkingConditionProbability> rewardMap = new HashMap<>();

		Sequencer s = new Sequencer(net, initialMarking, new STPNSimulatorComponentsFactory(), new AnalysisLogger() {
			@Override
			public void log(String message) { }
			@Override
			public void debug(String string) { }
		});

		for(String rewardString : rewardStringSet) {
			// Create a reward (which is a sequencer observer)
			// sums up for every firing the time spent in a given condition
			TransientMarkingConditionProbability reward = new TransientMarkingConditionProbability(s, 
					new ContinuousRewardTime(new BigDecimal("0.01")), samplesNumber, MarkingCondition.fromString(rewardString));

			rewardMap.put(rewardString, reward);
			
			RewardEvaluator rewardEvaluator = new RewardEvaluator(reward, 1);
	    	

		}

		// Run simulation
		s.simulate();

		for(String rewardString : rewardStringSet) {

			TransientMarkingConditionProbability reward = rewardMap.get(rewardString);
			// Get simulation results
			NumericRewardResult result = (NumericRewardResult) reward.evaluate();
			steadyStates.put(rewardString, result.getResult().doubleValue());
		}
		// Plot results
		return steadyStates;
	}


}
