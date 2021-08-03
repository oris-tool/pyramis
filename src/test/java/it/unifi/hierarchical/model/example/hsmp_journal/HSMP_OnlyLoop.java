package it.unifi.hierarchical.model.example.hsmp_journal;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.function.GEN;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;

public class HSMP_OnlyLoop {

    public static HierarchicalSMP build() {
    	
    	final double prob1 = 0.6;
    	final double prob2 = 1 - prob1;
    	
    	
    	       
        //Level depth 1
        int depth = 1;
        
        State E0 = new ExitState("E0",1);
        
        List<State> nextStates1 = null;
         
        State state1 = new SimpleState(
                "State1",
                GEN.newDeterministic(new BigDecimal(1)),
                nextStates1, 
                null, 
                depth);
        
        State state2 = new SimpleState(
                "State2",
                GEN.newUniform(new OmegaBigDecimal(2), new OmegaBigDecimal(3)),
                Arrays.asList(state1), 
                Arrays.asList(1.0), 
                depth);
        
        state1.setNextStates(Arrays.asList(E0,state2), Arrays.asList(prob1,prob2));
        
                
        
        Region region0 = new Region(state1, RegionType.EXIT);

        
        //Level depth 0, toplevel only state with a selfloop
        depth = 0;
        List<State> nextStates = null;//Required to avoid ambiguity
        State stateTop = new CompositeState(
                "StateTop",  
                Arrays.asList(region0), 
                nextStates, 
                null, 
                depth);
              
        stateTop.setNextStates(Arrays.asList(stateTop), Arrays.asList(1.0));
        
        return new HierarchicalSMP(stateTop);
    }
}
