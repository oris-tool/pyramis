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

import it.unifi.hierarchical.model.CompositeStep;
import it.unifi.hierarchical.model.LogicalLocation;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.utils.StateUtils;

import java.util.*;

/**
 * Analysis of the hierarchical structure of an HSMP in order to
 * derive the parent step of each region and the parent regions of each step.
 */
public class ModelStructureAnalyzer{

    // Parent region of each step
    private final Map<LogicalLocation, Region> parentRegions;

    // Parent composite step of each region
    private final Map<Region, CompositeStep> parentStates;

    public ModelStructureAnalyzer(LogicalLocation initialState) {
        parentRegions =  new HashMap<>();
        parentStates = new HashMap<>();
        Set<LogicalLocation> visited = new HashSet<>();
        Stack<LogicalLocation> toBeVisited = new Stack<>();
        toBeVisited.add(initialState);

        while (!toBeVisited.isEmpty()) {
            LogicalLocation c = toBeVisited.pop();
            visited.add(c);

            if (c instanceof CompositeStep) {
                for (Region region : ((CompositeStep) c).getRegions()) {
                    parentStates.put(region, (CompositeStep) c);
                    parentRegions.put(region.getInitialStep(), region);
                    toBeVisited.add(region.getInitialStep());
                }
            }

            if (StateUtils.isCompositeWithBorderExit(c)) {
                CompositeStep cState = (CompositeStep) c;

                for (LogicalLocation exitState : cState.getExitSteps().keySet()) {
                    List<LogicalLocation> successors = cState.getNextLocations(exitState);

                    for (LogicalLocation s : successors) {
                        if (visited.contains(s) || toBeVisited.contains(s))
                            continue;

                        parentRegions.put(s, parentRegions.get(c)); //Same of predecessor
                        toBeVisited.add(s);
                    }
                }
            } else {
                List<LogicalLocation> successors = c.getNextLocations();

                for (LogicalLocation s : successors) {
                    if (visited.contains(s) || toBeVisited.contains(s))
                        continue;

                    parentRegions.put(s, parentRegions.get(c)); //Same of predecessor
                    toBeVisited.add(s);
                }
            }
        }
    }

    public Map<LogicalLocation, Region> getParentRegions() {
        return parentRegions;
    }

    public Map<Region, CompositeStep> getParentStates() {
        return parentStates;
    }
}
