package it.unifi.hierarchical.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.utils.StateUtils;

public class ModelStructureAnalyzer{

    private Map<State, Region> parentRegions; // Region parent of a state
    private Map<Region, CompositeState> parentStates; //Composite state parent of the region
    
    public ModelStructureAnalyzer(State initialState) {
        parentRegions =  new HashMap<>();
        parentStates = new HashMap<>();
        Set<State> visited = new HashSet<>();
        Stack<State> toBeVisited = new Stack<>();
        toBeVisited.add(initialState);
        
        while(!toBeVisited.isEmpty()) {
            State c = toBeVisited.pop();
            visited.add(c);
            if(c instanceof CompositeState) {
                for (Region region : ((CompositeState) c).getRegions()) {
                    parentStates.put(region, (CompositeState) c);
                    parentRegions.put(region.getInitialState(), region);
                    toBeVisited.add(region.getInitialState());
                }
            }
            
            if(StateUtils.isCompositeWithBorderExit(c)) {
            	CompositeState cState = (CompositeState) c;
    			for(State exitState : cState.getNextStatesConditional().keySet()) {
    				List<State> successors = cState.getNextStatesConditional().get(exitState);
    				for (State s : successors) {
        				if(visited.contains(s) || toBeVisited.contains(s))
                            continue;
        				parentRegions.put(s, parentRegions.get(c));//Same of predecessor
                        toBeVisited.add(s);
        			} 
    			}
    		}else {
    			List<State> successors = c.getNextStates();
    			for (State s : successors) {
    				if(visited.contains(s) || toBeVisited.contains(s))
                        continue;
    				parentRegions.put(s, parentRegions.get(c));//Same of predecessor
                    toBeVisited.add(s);
    			}    
    		}
            
        }
    }

    public Map<State, Region> getParentRegions() {
        return parentRegions;
    }

    public Map<Region, CompositeState> getParentStates() {
        return parentStates;
    }
    
}
