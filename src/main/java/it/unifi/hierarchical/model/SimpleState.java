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

package it.unifi.hierarchical.model;

import java.util.List;

import org.oristool.math.function.PartitionedFunction;

import it.unifi.hierarchical.model.visitor.StateVisitor;

public class SimpleState extends State{
	// FIXME: Rename SimpleState to SimpleStep.
	// FIXME: SimpleStep should extend Step.

    private PartitionedFunction densityFunction;
    
    // FIXME: Remove upperBound: the analysis parameters should be part of the analysis configuration, not the model
    //        (note that if upperBound==-1., then the step is analyzed not using the analysis time limit,
    //		  but a predefined number of time points; this is used in software rejuvenation case study of the TSE paper).
    private double upperBound=-1.;
    
    public double getUpperBound() {
		return upperBound;
	}

	public SimpleState(String name, PartitionedFunction densityFunction, List<State> nextStates, List<Double> branchingProbs, int depth, double timeStep, double bound) {
    	this(name, densityFunction, nextStates, branchingProbs, depth, timeStep);
    	this.upperBound= bound;
    }
    
    
    public SimpleState(String name, PartitionedFunction densityFunction, List<State> nextStates, List<Double> branchingProbs, int depth, double timeStep) {
        super(name, depth, timeStep);
        this.setNextStates(nextStates, branchingProbs);
        this.densityFunction = densityFunction;
    }
    
    public SimpleState(String name, PartitionedFunction densityFunction, List<State> nextStates, List<Double> branchingProbs, int depth) {
        this(name, densityFunction, nextStates, branchingProbs, depth, -1.0);
    }

    public PartitionedFunction getDensityFunction() {
        return densityFunction;
    }

    @Override
    public String toString() {
        return "SimpleState [densityFunction=" + densityFunction + ", numberOfNextStates=" + (nextStates != null? nextStates.size():0) + ", depth=" + depth
                + ", name=" + name + "]";
    }
    
    @Override
    public void accept(StateVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public State makeCopy() {
        return new SimpleState(name, densityFunction, nextStates, branchingProbs, depth, timeStep);
    }
    
    public State makeCopy(int id) {
        return new SimpleState(name+"_"+id, densityFunction, nextStates, branchingProbs, depth, timeStep);
    }
}
