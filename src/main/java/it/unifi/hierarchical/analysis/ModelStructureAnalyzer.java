/* This program is part of the PYRAMIS library for compositional analysis of hierarchical UML statecharts.
 * Copyright (C) 2019-2021 The PYRAMIS Authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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

/**
 * Analysis of the hierarchical structure of an HSMP in order to 
 * derive the parent step of each region and the parent regions of each step.
 */
public class ModelStructureAnalyzer{
	
	// Parent region of each step
    private Map<State, Region> parentRegions; 
    
    // Parent composite step of each region
    private Map<Region, CompositeState> parentStates; 
    
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
        				parentRegions.put(s, parentRegions.get(c)); //Same of predecessor
                        toBeVisited.add(s);
        			} 
    			}
    		} else {
    			List<State> successors = c.getNextStates();
    			for (State s : successors) {
    				if(visited.contains(s) || toBeVisited.contains(s))
                        continue;
    				parentRegions.put(s, parentRegions.get(c)); //Same of predecessor
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
