package it.unifi.hierarchical.model.example.hsmp;

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

public class HSMP_OnlyVariable {

    public static HierarchicalSMP build() {
    	
    	double state0t  = 0.1;
    	double state1t  = 0.1;
    	double state11t = 0.1;
    	                    
    	double state0rt = 0.1;
    	double state1rt = 0.1;
    	                  
    	double stateTopt= 0.2;
    	
    	
        //Level depth 1
        int depth = 1;
        
        State E0 = new ExitState("E0",1);
        State E1 = new ExitState("E1",1);
        
         
        State state0 = new SimpleState(
                "State0",
                GEN.newDeterministic(new BigDecimal(10)),
                Arrays.asList(E0), 
                Arrays.asList(1.0), 
                depth,state0t);
        
        State state11 = new SimpleState(
                "State11",
                GEN.newUniform(new OmegaBigDecimal(2), new OmegaBigDecimal(10)),
                Arrays.asList(E1), 
                Arrays.asList(1.0), 
                depth,state11t);
        State state1 = new SimpleState(
                "State1",
                GEN.newDeterministic(new BigDecimal(5)),
                Arrays.asList(state11), 
                Arrays.asList(1.0), 
                depth,state1t);
        
          
        Region region0 = new Region(state0, RegionType.EXIT,state0rt);
        Region region1 = new Region(state1, RegionType.EXIT,state1rt);

        
        //Level depth 0, toplevel only state with a selfloop
        depth = 0;
        List<State> nextStates = null;//Required to avoid ambiguity
        State stateTop = new CompositeState(
                "StateTop",  
                Arrays.asList(region0,region1), 
                nextStates, 
                null, 
                depth,stateTopt);
              
        stateTop.setNextStates(Arrays.asList(stateTop), Arrays.asList(1.0));
        
        return new HierarchicalSMP(stateTop);
    }
}
