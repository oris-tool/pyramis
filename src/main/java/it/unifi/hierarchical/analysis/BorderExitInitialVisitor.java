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

// LAURA: è i lvisitor che controlla se il primo stato è un border exit
// per ogni regione, guarda se ogni stato è un border exit e ti produce una lista di errori
// se il primo stato della regione è un border exit
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
