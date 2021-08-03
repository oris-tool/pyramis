package it.unifi.hierarchical.analysis;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.visitor.StateVisitor;
import it.unifi.hierarchical.utils.StateUtils;

public class BorderExitInitialVisitor implements StateVisitor{

	private Set<State> evaluated;
	private boolean correctModel;
	private Set<State> offenderSet;

	public BorderExitInitialVisitor(){
		this.offenderSet = new HashSet<>();
		this.correctModel=true;
		this.evaluated=new HashSet<>();
	}

	@Override
	public void visit(SimpleState state) {

//		System.out.println(state.getDepth()+" "+state.getName());
		
		evaluated.add(state);
		//Visit successors not yet visited
		List<State> successors = state.getNextStates();
		for (State successor : successors) {
			if(evaluated.contains(successor))
				continue;
			successor.accept(this);
			
			if(!correctModel)
				return;
		}
	}

	@Override
	public void visit(CompositeState state) {

		System.out.println(state.getDepth()+" "+state.getName());
		
		
		evaluated.add(state);

		List<Region> regions = state.getRegions();
		for (Region region : regions) {

			System.out.println("region ");
			
			
			if(StateUtils.isCompositeWithBorderExit( region.getInitialState())) {
				offenderSet.add(region.getInitialState());
				correctModel=false;
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
		System.out.println(state.getDepth()+" "+state.getName());
		
		evaluated.add(state);
	}

	@Override
	public void visit(ExitState state) {
		System.out.println(state.getDepth()+" "+state.getName());
		
		evaluated.add(state);
	}

	public boolean isModelCorrect() {
		return correctModel;
	}

	public Set<State> getOffenderSet() {
		return offenderSet;
	}

}
