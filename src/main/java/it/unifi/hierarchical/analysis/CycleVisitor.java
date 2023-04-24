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

package it.unifi.hierarchical.analysis;

import it.unifi.hierarchical.model.*;
import it.unifi.hierarchical.model.visitor.LogicalLocationVisitor;
import it.unifi.hierarchical.utils.StateUtils;

import java.util.*;

/**
 * Visitor of logical locations, supporting the identification of cycles visiting non-top-level composite steps.
 */
// Serve in quanto si memorizza solo lo step iniziale di una regione: serve per capire se si ha un ciclo che comprende
// stati compositi che non siano di top-level
public class CycleVisitor implements LogicalLocationVisitor {

	private final Set<LogicalLocation> evaluated;
	private boolean compositeCycles;

	// Set of regions containing a cycle visiting a non-top-level composite step
	private final Set<Region> regionSet;

	// For each region that contains a cycle visiting a non-top-level composite step,
	// the map yields the set of logical locations of the region
	private final Map<Region,Set<LogicalLocation>> map;

	public CycleVisitor() {
		this.regionSet = new HashSet<>();
		this.map = new HashMap<>();
		this.compositeCycles = false;
		this.evaluated = new HashSet<>();
	}

	@Override
	public void visit(SimpleStep simpleStep) {
		evaluated.add(simpleStep);

		//Visit successors not yet visited
		List<LogicalLocation> successors = simpleStep.getNextLocations();

		for (LogicalLocation successor : successors) {
			if (evaluated.contains(successor))
				continue;

			successor.accept(this);
		}
	}
	@Override
	public void visit(CompositeStep compositeStep) {
		evaluated.add(compositeStep);

		List<Region> regions = compositeStep.getRegions();
		for (Region region : regions) {
			if (region.containsCompositeCycle()) {
				compositeCycles = true;
				regionSet.add(region);

				Set<LogicalLocation> visited = new HashSet<>();
				Stack<LogicalLocation> toBeVisited = new Stack<>();
				toBeVisited.add(region.getInitialStep());

				while (!toBeVisited.isEmpty()) {
					LogicalLocation current = toBeVisited.pop();
					visited.add(current);

					if (StateUtils.isCompositeWithBorderExit(current)) {
						CompositeStep cState = (CompositeStep)current;

						for (LogicalLocation successor : cState.getNextLocations()) {
							if (visited.contains(successor) || toBeVisited.contains(successor))
								continue;

							toBeVisited.push(successor);
						}
						//STANDARD CASE
					} else {
						//Add missing children to the DTMC
						for (LogicalLocation successor : current.getNextLocations()) {
							if (visited.contains(successor) || toBeVisited.contains(successor))
								continue;

							toBeVisited.push(successor);
						}
					}
				}

				map.put(region, visited);
			}

			region.getInitialStep().accept(this);
		}

		// Visit successors not yet visited
		for (LogicalLocation successor : compositeStep.getNextLocations()) {
			if (evaluated.contains(successor))
				continue;

			successor.accept(this);
		}
	}

	@Override
	public void visit(FinalLocation finalLocation) {
		evaluated.add(finalLocation);
	}

	public boolean containsCompositeCycles() {
		return compositeCycles;
	}

	public Set<Region> getRegionSet() {
		return regionSet;
	}

	public Map<Region, Set<LogicalLocation>> getMap() {
		return map;
	}
}
