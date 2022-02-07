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
import java.util.Map.Entry;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.visitor.StateVisitor;
import it.unifi.hierarchical.utils.StateUtils;

/**
 * Visitor of logical locations, supporting the identification of cycles visiting non-top-level composite steps.
 */
public class CycleVisitor implements StateVisitor{

	private Set<State> evaluated;
	private boolean compositeCycles;
	
	// Set of regions containing a cycle visiting a non-top-level composite step
	private Set<Region> regionSet;
	
	// For each region that contains a cycle visiting a non-top-level composute step,
	// the map yields the set of logical locations of the region
	private Map<Region,Set<State>> map;

	public CycleVisitor(){
		System.out.println("Cycle visitor started");
		this.regionSet = new HashSet<>();
		this.map= new HashMap<>();
		this.compositeCycles=false;
		this.evaluated=new HashSet<>();
	}

	@Override
	public void visit(SimpleState state) {

		evaluated.add(state);
		//Visit successors not yet visited
		List<State> successors = state.getNextStates();
		for (State successor : successors) {
			if(evaluated.contains(successor))
				continue;
			successor.accept(this);
		}
	}

	@Override
	public void visit(CompositeState state) {

		evaluated.add(state);

		List<Region> regions = state.getRegions();
		for (Region region : regions) {

			if(region.containsCompositeCycle()) {
				compositeCycles=true;
				regionSet.add(region);
			
				Set<State> visited = new HashSet<>();
				Stack<State> toBeVisited = new Stack<>();
				toBeVisited.add(region.getInitialState());
				while(!toBeVisited.isEmpty()) {
					State current = toBeVisited.pop();
					visited.add(current);

					if(StateUtils.isCompositeWithBorderExit(current)) {
						CompositeState cState = (CompositeState)current;
						
						for(Entry<State, List<State>> e:cState.getNextStatesConditional().entrySet()) {
							for (State successor : e.getValue()) {
								if(visited.contains(successor) || toBeVisited.contains(successor))
									continue;
								toBeVisited.push(successor);    
							}
						}
						//STANDARD CASE
					}else {
						//Add missing children to the DTMC
						for(State successor:current.getNextStates()) {
							if(visited.contains(successor) || toBeVisited.contains(successor))
								continue;
							toBeVisited.push(successor);
						}
					}
				}
				
			map.put(region, visited);
			}
			region.getInitialState().accept(this);
		}

		// Visit successors not yet visited
		if(StateUtils.isCompositeWithBorderExit(state)) {

			CompositeState cState = state;
			for(State exitState : cState.getNextStatesConditional().keySet()) {
				List<State> successors = cState.getNextStatesConditional().get(exitState);
				for (State successor : successors) {
					if(evaluated.contains(successor))
						continue;
					successor.accept(this);
				}    
			}
		} else {
			List<State> successors = state.getNextStates();
			for (State successor : successors) {
				if(evaluated.contains(successor))
					continue;
				successor.accept(this);
			}    
		}
	}

	@Override
	public void visit(FinalState state) {
		evaluated.add(state);
	}

	@Override
	public void visit(ExitState state) {
		evaluated.add(state);
	}

	public boolean containsCompositeCycles() {
		return compositeCycles;
	}

	public Set<Region> getRegionSet() {
		return regionSet;
	}

	public Map<Region, Set<State>> getMap() {
		return map;
	}
}
