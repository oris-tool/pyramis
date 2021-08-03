package it.unifi.hierarchical.analysis;


import java.util.Date;

import org.junit.Test;
import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.SteadyStateSolution;
import org.oristool.models.stpn.steady.RegSteadyState;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;

import it.unifi.hierarchical.model.example.pn_journal.PN_ARejuvProbability;


/**
 * Test to check correctness of analysis when there is a composite state inside another composite state.
 * This was the first test created. The idea was to check the correctness of the shift and project approach
 */
public class UtilCalculateRegionProbabilities {
    
    
    @Test
    public void test1() {     
    	int weeks=4;
        //PN
        PetriNet net = new PetriNet();
        Marking m = new Marking();
        PN_ARejuvProbability.build(net, m,weeks);
        
        Date start = new Date();
        RegSteadyState analysisPN = RegSteadyState.builder().build();
        SteadyStateSolution<Marking> ssPN = analysisPN.compute(net, m);
        RewardRate reward1 = RewardRate.fromString("A1failed");
        RewardRate reward2 = RewardRate.fromString("A1rejuv");
        RewardRate reward3 = RewardRate.fromString("A1");
        SteadyStateSolution<RewardRate> rewardPN = SteadyStateSolution.computeRewards(ssPN, reward1, reward2,reward3);
        double resultPN1 = rewardPN.getSteadyState().get(reward1).doubleValue();
        double resultPN2 = rewardPN.getSteadyState().get(reward2).doubleValue();
        double resultPN3 = rewardPN.getSteadyState().get(reward3).doubleValue();
        Date end = new Date();
        System.out.println("Time Regenerative SS  analysis:" + (end.getTime() - start.getTime()) + "ms");
        
        //Compare results
        System.out.println("Regenerative SS  analysis result for state 1: " + resultPN1);
        System.out.println("Regenerative SS  analysis result for state 2: " + resultPN2);
        System.out.println("Regenerative SS  analysis result for state 3: " + resultPN3);
           
       System.out.println("Failed= "+ (resultPN1/(resultPN1+resultPN2)));
       System.out.println("Rejuv= "+ (resultPN2/(resultPN1+resultPN2)));
        
    }
}
