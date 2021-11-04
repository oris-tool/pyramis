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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unifi.hierarchical.model.visitor.StateVisitor;

/**
 * A logical location of an HSMP model.
 */
public abstract class State {
	// FIXME: Rename State to LogicalLocation.
	// FIXME: Add a class Step that extends LogicalLocation.
	
    private final static double EPSILON = 0.000001;
    
	// FIXME: Rename nextStates to nextLocations and add it to Step.
    protected List<State> nextStates;

	// FIXME: Rename branchingProbs to nextLocationDistribution. 
	//        Should it be a map? Could the attribute nextLocations be removed in this case?
    protected List<Double> branchingProbs;

	// FIXME: Maybe the attribute depth is not needed 
    //		  (note that depth is 0 for the top level, 1 for the subsequent lower level and so on).
    protected int depth;
    
    protected String name;
    
    // FIXME: The concept of time step should be part of the analysis configuration (not of the model).
    protected double timeStep;
    
    private boolean looping=false;
    private boolean cycle=false;
    
    protected State(String name, int depth) {
    	this(name, depth, -1.0);
    }
    
    /**
     * FIXME: This condition should be automatically checked (note that 
     * in some cases the last step if a cycle may not be defined).
     * 
     * @return true if the step is part of a cycle and it is the last step of the cycle.
     */
    public boolean isLooping() {
		return looping;
	}
    
    /**
     * FIXME: This condition should be automatically checked.
     * 
     * @return true if the step is part of a cycle
     */
    public boolean isCycle() {
		return cycle;
	}

	public void setCyleLooping(boolean cycle, boolean looping) {
		this.looping = looping;
		this.cycle=cycle;
	}

	protected State(String name, int depth, double timeStep) {
        if(name == null || name.trim().equals(""))
            throw new IllegalArgumentException("Name can't be empty");
        
        this.name = name;
        this.depth = depth;
        this.nextStates = new ArrayList<>();
        this.branchingProbs = new ArrayList<>();
        this.timeStep=timeStep;
    }
    
    public abstract State makeCopy();

    public void setNextStates(List<State> nextStates, List<Double> branchingProbs) {
        if(nextStates== null && branchingProbs == null) {
            this.nextStates = new ArrayList<>();
            this.branchingProbs = new ArrayList<>();
            return;
        }
        if(nextStates == null || branchingProbs == null)
            throw new IllegalArgumentException("nextStates and branching probs must have the same size");
        if(nextStates.size() != branchingProbs.size())
            throw new IllegalArgumentException("nextStates and branching probs must have the same size");
        
        double totalProb = branchingProbs.stream().reduce(0.0, (x,y) -> x+y);
        if(totalProb -1.0 > EPSILON)
            throw new IllegalArgumentException("Total branching probability must be 1");
        this.nextStates = nextStates;
        this.branchingProbs = branchingProbs;
        
    }
    
    public List<State> getNextStates() {
        return Collections.unmodifiableList(nextStates);
    }

    public List<Double> getBranchingProbs() {
        return Collections.unmodifiableList(branchingProbs);
    }

    public int getDepth() {
        return depth;
    }
    
    public double getTimeStep() {
        return timeStep;
    }
    
    public String getName() {
        return name;
    }

    //TODO introduce the varying value of timeStep in HashCode()
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + depth;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        State other = (State) obj;
        if (depth != other.depth)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if(timeStep!=other.timeStep)
        	return false;
        return true;
    }
    
    public abstract void accept(StateVisitor visitor);
 
}
