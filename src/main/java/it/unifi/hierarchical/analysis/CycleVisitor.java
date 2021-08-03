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

public class CycleVisitor implements StateVisitor{

	private Set<State> evaluated;
	private boolean compositeCycles;
	private Set<Region> regionSet;
	private Map<Region,Set<State>> map;

	public CycleVisitor(){
		
		System.out.println("cycle visitor initiated");
		
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

	
	//FIXME aggiungo tutti gli stati direttamente, 
	//ma in realtà il ciclo non coinvolge generalmente tutto
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
						//Add missing children to the dtmc
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

		//Visit successors not yet visited
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
		}else {
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
