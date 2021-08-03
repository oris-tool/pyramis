package it.unifi.hierarchical.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.Region.RegionType;

public class StateUtils {

	public static State findEndState(Region r) {
		if(r.getType()==RegionType.NEVER) {
			throw new IllegalStateException("Searching End in NeverEnding");
		}
		return findEndState(getReachableStates(r.getInitialState()));
	}

	/**
	 * Assume to have a single end state
	 */
	public static State findEndState(List<State> states) {
		for (int i = 0; i < states.size(); i++) {
			State state = states.get(i);
			if (state instanceof ExitState || state instanceof FinalState)
				return state;
		}
		throw new IllegalStateException("End state not found");
	}

	/**
	 * Return a list of reachable state. If a state is composite, it is considered a single state and its internal structure is not visited
	 * If two or more Exit or Final states are found, reachable in a single region print an error
	 *	
	 */
	public static List<State> getReachableStates(State initialState) {
		boolean endStateVisited= false;
		
		List<State> states = new ArrayList<>();
		Stack<State> toBeVisited = new Stack<>();
		if(initialState instanceof ExitState || initialState instanceof FinalState) {
			System.out.println("Warning! InitialState is an Exit or Final, depth "+initialState.getDepth());
		}
		toBeVisited.add(initialState);
		while (!toBeVisited.isEmpty()) {
			State current = toBeVisited.pop();
			if(current instanceof ExitState || current instanceof FinalState) {
				if (!endStateVisited) {
					endStateVisited=true;
				}else {
					System.out.println("Error! Multiple Exit or Final States present in Region depth "+current.getDepth());
				}
			}
				
			
				
			states.add(current);

			if(StateUtils.isCompositeWithBorderExit(current)) {
				CompositeState cState = (CompositeState) current;
				for(State exitState : cState.getNextStatesConditional().keySet()) {
					List<State> successors = cState.getNextStatesConditional().get(exitState);

					for (State successor : successors) {
						if (!states.contains(successor) && !toBeVisited.contains(successor))
							toBeVisited.add(successor);
					}

				}
			}else {
				for (State successor : current.getNextStates()) {
					if (!states.contains(successor) && !toBeVisited.contains(successor))
						toBeVisited.add(successor);
				}
			}

		}

		return states;
	}


	public static State searchStateByName(List<State> states, String name) {
		for (State state : states) {
			if(state.getName().equals(name))
				return state; 
		}
		throw new IllegalArgumentException("State not found!");
	}

	public static boolean isCompositeWithBorderExit(State state) {
		if(     state instanceof CompositeState && 
				((CompositeState)state).hasExitStatesOnBorder()) {
			return true;
		}
		return false;
	}

}
