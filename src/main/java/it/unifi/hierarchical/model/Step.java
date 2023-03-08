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

package it.unifi.hierarchical.model;

import it.unifi.hierarchical.model.visitor.StateVisitor;

import java.util.*;

/**
 * A logical location of an HSMP model.
 */

/**
 * Represents a step of an HSMP model
 */

public abstract class Step extends LogicalLocation {
    // Note that nextStates is not used to select the next step if the step is a composite step of type first
    // with different next step PDF depending on the region that has terminated first.
    protected Map<LogicalLocation, Double> nextLocations;

    protected Step(String name, int depth, double timeStep) {
        super(name, depth, timeStep);

        nextLocations = new LinkedHashMap<>();
    }

    protected Step(String name, double timeStep) {
        super(name, 0, timeStep);

        nextLocations = new LinkedHashMap<>();
    }

    protected Step(String name, int depth) {
        super(name, depth);

        nextLocations = new LinkedHashMap<>();
    }

    protected Step(String name) {
        super(name, 0);

        nextLocations = new LinkedHashMap<>();
    }

    protected Step(Step step) {
        super(step);

        nextLocations = Map.copyOf(step.nextLocations);
    }

    public void addNextLocation(LogicalLocation logicalLocation, Double branchingProbability) {
        nextLocations.put(logicalLocation, branchingProbability);
        logicalLocation.setDepth(depth);
    }

    public List<LogicalLocation> getNextLocations() {
        return List.copyOf(nextLocations.keySet());
    }

    public List<Double> getBranchingProbabilities() {
        return List.copyOf(nextLocations.values());
    }

    public List<Double> getBranchingProbs() {
        return List.copyOf(nextLocations.values());
    }

    public Map<LogicalLocation, Double> getNextLocationsMap() {
        return nextLocations;
    }

    public void setNextLocations(List<LogicalLocation> logicalLocations, List<Double> branchingProbs) {
        if (logicalLocations == null || branchingProbs == null)
            throw new IllegalArgumentException("nextStates and branching probs must not be null");

        if (logicalLocations.size() != branchingProbs.size())
            throw new IllegalArgumentException("nextStates and branching probs must have the same size");

        nextLocations = new HashMap<>();

        for (int index = 0; index < logicalLocations.size(); index++)
            addNextLocation(logicalLocations.get(index), branchingProbs.get(index));
    }
}
