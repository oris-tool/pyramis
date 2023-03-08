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

import it.unifi.hierarchical.model.visitor.LogicalLocationVisitor;
import org.oristool.math.function.PartitionedFunction;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a simple step, modeling an atomic action with independent execution time distribution.
 */
public class SimpleStep extends Step {

    /**
     * Probability density function of the simple step
     */
    private final PartitionedFunction densityFunction;

    // FIXME: Remove upperBound: the analysis parameters should be part of the analysis configuration, not the model
    //        (note that if upperBound==-1., then the step is analyzed not using the analysis time limit,
    //		  but a predefined number of time points; this is used in software rejuvenation case study of the TSE paper).
    private double upperBound=-1.;

    /**
     * Complete constructor of a single step, specyfing its name, density function, list of reachable next locations,
     * list of associated branching probabilities, depth, time step and upper bound
     * @param name name of the simple step
     * @param densityFunction probability density function associated to the simple step
     * @param nextSteps list of next locations reachable from the simple step
     * @param branchingProbs list of branching probabilities (must sum to 1) of next locations
     * @param timeStep
     * @param bound
     */
    public SimpleStep(String name, PartitionedFunction densityFunction, List<LogicalLocation> nextSteps, List<Double> branchingProbs, int depth, double timeStep, double bound) {
        this(name, densityFunction, nextSteps, branchingProbs, depth, timeStep);
        this.upperBound= bound;
    }

    public SimpleStep(String name, PartitionedFunction densityFunction, List<LogicalLocation> nextSteps, List<Double> branchingProbs, double timeStep, double bound) {
        this(name, densityFunction, nextSteps, branchingProbs, 0, timeStep);
        this.upperBound= bound;
    }

    /**
     * Constructor of a single step, omitting its upper bound
     * @param name name of the simple step
     * @param densityFunction probability density function associated to the simple step
     * @param nextSteps list of next locations reachable from the simple step
     * @param branchingProbs list of branching probabilities (must sum to 1) of next locations
     * @param timeStep
     */
    public SimpleStep(String name, PartitionedFunction densityFunction, List<LogicalLocation> nextSteps, List<Double> branchingProbs, int depth, double timeStep) {
        super(name, depth, timeStep);
        this.setNextLocations(nextSteps, branchingProbs);
        this.densityFunction = densityFunction;
    }

    public SimpleStep(String name, PartitionedFunction densityFunction, List<LogicalLocation> nextSteps, List<Double> branchingProbs, double timeStep) {
        super(name, 0, timeStep);
        this.setNextLocations(nextSteps, branchingProbs);
        this.densityFunction = densityFunction;
    }

    /**
     * Constructor of a single step, omitting its upper bound and time step
     * @param name name of the simple step
     * @param densityFunction probability density function associated to the simple step
     * @param nextSteps list of next locations reachable from the simple step
     * @param branchingProbs list of branching probabilities (must sum to 1) of next locations
     */
    public SimpleStep(String name, PartitionedFunction densityFunction, List<LogicalLocation> nextSteps, List<Double> branchingProbs, int depth) {
        this(name, densityFunction, nextSteps, branchingProbs, depth, -1.0);
    }

    public SimpleStep(String name, PartitionedFunction densityFunction, List<LogicalLocation> nextSteps, List<Double> branchingProbs) {
        this(name, densityFunction, nextSteps, branchingProbs, 0, -1.0);
    }

    /**
     * Complete constructor of a single step, specyfing its name, density function, map associating its reachable next
     * locations and their associated branching probabilities, depth, time step and upper bound
     * @param name name of the simple step
     * @param densityFunction probability density function associated to the simple step
     * @param nextLocations map of next locations and branching probabilities
     * @param timeStep
     * @param bound
     */
    public SimpleStep(String name, PartitionedFunction densityFunction, Map<LogicalLocation, Double> nextLocations, int depth, double timeStep, double bound) {
        this(name, densityFunction, nextLocations, depth, timeStep);
        this.upperBound= bound;
    }

    public SimpleStep(String name, PartitionedFunction densityFunction, Map<LogicalLocation, Double> nextLocations, double timeStep, double bound) {
        this(name, densityFunction, nextLocations, 0, timeStep);
        this.upperBound= bound;
    }

    /**
     * Constructor of a single step, omitting its upper bound
     * @param name name of the simple step
     * @param densityFunction probability density function associated to the simple step
     * @param nextLocations map of next locations and branching probabilities
     * @param timeStep
     */
    public SimpleStep(String name, PartitionedFunction densityFunction, Map<LogicalLocation, Double> nextLocations, int depth, double timeStep) {
        super(name, depth, timeStep);
        this.nextLocations = new LinkedHashMap<>(nextLocations);
        this.densityFunction = densityFunction;
    }

    public SimpleStep(String name, PartitionedFunction densityFunction, Map<LogicalLocation, Double> nextLocations, double timeStep) {
        super(name, 0, timeStep);
        this.nextLocations = new LinkedHashMap<>(nextLocations);
        this.densityFunction = densityFunction;
    }

    /**
     * Constructor of a single step, omitting its upper bound and time step
     * @param name name of the simple step
     * @param densityFunction probability density function associated to the simple step
     * @param nextLocations map of next locations and branching probabilities
     */
    public SimpleStep(String name, PartitionedFunction densityFunction, Map<LogicalLocation, Double> nextLocations, int depth) {
        this(name, densityFunction, nextLocations, depth,-1.0);
    }

    public SimpleStep(String name, PartitionedFunction densityFunction, Map<LogicalLocation, Double> nextLocations) {
        this(name, densityFunction, nextLocations, 0,-1.0);
    }

    /**
     * Getter of the upper bound
     * @return the upper bound of the time step
     */
    public double getUpperBound() {
        return upperBound;
    }

    /**
     * Getter of the probability density function
     * @return the probability density function of the time step
     */
    public PartitionedFunction getDensityFunction() {
        return densityFunction;
    }

    @Override
    public String toString() {
        return "SimpleStep [densityFunction=" + densityFunction + ", numberOfnextSteps=" + (nextLocations != null ? nextLocations.size() : 0) + ", depth=" + depth
                + ", name=" + name + "]";
    }

    /**
     * Default method used by {@link LogicalLocationVisitor} visitors to visit a simple step
     * @param visitor visitor that is currently visit the simple step
     */
    @Override
    public void accept(LogicalLocationVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Method used to make a copy of the simple step, used in the analysis process
     * @return a copy of the simple step, cast to logical location since it overrides a previously defined method
     */
    @Override
    public LogicalLocation makeCopy() {
        return new SimpleStep(name, densityFunction, nextLocations, depth, timeStep);
    }

    /**
     * Makes a copy of the simple step, postponing an id to its name
     * @return a copy of the simple step, cast to logical location since it overrides a previously defined method
     */
    public LogicalLocation makeCopy(int id) {
        return new SimpleStep(name + "_" + id, densityFunction, nextLocations, depth, timeStep);
    }
}
