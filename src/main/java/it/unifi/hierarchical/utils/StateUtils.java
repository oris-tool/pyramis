/* This program is part of the PYRAMIS library for compositional analysis of hierarchical UML statecharts.
 * Copyright (C) 2019-2023 The PYRAMIS Authors.
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

package it.unifi.hierarchical.utils;

import it.unifi.hierarchical.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

// FIXME: Could some of these methods be methods of the classes Region etc.?

public class StateUtils {

	public static LogicalLocation findEndState(Region r) {
		if(r.getType()== RegionType.NEVERENDING) {
			throw new IllegalStateException("Searching End in NeverEnding");
		}
		return findFinalLocation(getReachableStates(r.getInitialStep()));
	}

	/**
	 * This method assumes that each region contains at most one final location.
	 */
	public static LogicalLocation findFinalLocation(List<LogicalLocation> states) {
		for (LogicalLocation state : states) {
			if (state instanceof FinalLocation)
				return state;
		}
		throw new IllegalStateException("End state not found");
	}

	/**
	 * Return a list of reachable state. If a state is composite, it is considered a single state and its internal structure is not visited
	 * If two or more Exit or Final states are found, reachable in a single region print an error
	 *
	 */
	public static List<LogicalLocation> getReachableStates(LogicalLocation initialState) {
		boolean endStateVisited= false;

		List<LogicalLocation> states = new ArrayList<>();
		Stack<LogicalLocation> toBeVisited = new Stack<>();
		if(initialState instanceof FinalLocation) {
			System.out.println("Warning! InitialState is an Exit or Final, depth "+initialState.getDepth());
		}
		toBeVisited.add(initialState);
		while (!toBeVisited.isEmpty()) {
			LogicalLocation current = toBeVisited.pop();
			if(current instanceof FinalLocation) {
				if (!endStateVisited) {
					endStateVisited=true;
				}else {
					System.out.println("Error! Multiple Exit or Final States present in Region depth "+current.getDepth());
				}
			}

			states.add(current);

			if(StateUtils.isCompositeWithBorderExit(current)) {
				assert current instanceof CompositeStep;
				CompositeStep cState = (CompositeStep) current;

				for (LogicalLocation exitState : cState.getExitSteps().keySet()) {
					List<LogicalLocation> successors = cState.getNextLocations(exitState);

					for (LogicalLocation successor : successors) {
						if (!states.contains(successor) && !toBeVisited.contains(successor))
							toBeVisited.add(successor);
					}
				}
			}else {
				for (LogicalLocation successor : current.getNextLocations()) {
					if (!states.contains(successor) && !toBeVisited.contains(successor))
						toBeVisited.add(successor);
				}
			}
		}

		return states;
	}

	public static LogicalLocation searchStateByName(List<LogicalLocation> states, String name) {
		for (LogicalLocation state : states) {
			if(state.getName().equals(name))
				return state;
		}
		throw new IllegalArgumentException("State not found!");
	}

	/**
	 * @param state a logical location
	 * @return true if it is a composite step of type first
	 */
	public static boolean isCompositeWithBorderExit(LogicalLocation state) {
		return state instanceof CompositeStep &&
				((CompositeStep) state).hasExitStepsOnBorder();
	}
}
