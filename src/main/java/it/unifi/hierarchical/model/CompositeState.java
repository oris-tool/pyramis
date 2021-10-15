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
import java.util.Map;

import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.visitor.StateVisitor;

public class CompositeState extends State {

    private List<Region> regions;
    
    //Exit states on the border
    private boolean exitStatesOnBorder;
    Map<State, List<State>> nextStatesConditional; 
    Map<State,List<Double>> branchingProbsConditional;
    
    
    /**
     * Use this constructor if the composite state has final states or exit states not on border
     */
    public CompositeState(String name, List<Region> regions, List<State> nextStates, List<Double> branchingProbs, int depth, double timeStep) {
        super(name, depth, timeStep);
        if(regions == null || regions.size() == 0)
            throw new IllegalArgumentException("Each composite state require at least one region");
        this.setNextStates(nextStates, branchingProbs);
        this.regions = regions;
        
        boolean start=true;
        RegionType rt=null;
        for(Region r: regions) {
        	if(start && r.getType()!=RegionType.NEVER) {
        		rt=r.getType();
        		start=false;
        	}else if(r.getType()!=RegionType.NEVER && r.getType()!=rt){
        		throw new IllegalArgumentException("Regions of different types in state "+name);
        	}
        }
        
        
        this.exitStatesOnBorder = false;
    }

    /**
     * Use this constructor if the composite state has exit states on border
     */
    public CompositeState(String name, List<Region> regions, Map<State, List<State>> nextStatesConditional, Map<State,List<Double>> branchingProbsConditional, int depth, double timeStep) {
        super(name, depth, timeStep);
        if(regions == null || regions.size() < 2)
            throw new IllegalArgumentException("Composite state with exit on border requires at least 2 regions(hopefully not Neverending)");
        //Don't set next states
        this.regions = regions;
        
        for(Region r: regions) {
        	if(r.getType()==RegionType.FINAL){
        		throw new IllegalArgumentException("Regions of FINAL type in state with border on exit "+name);
        	}
        }
        
        this.exitStatesOnBorder = true;
        this.nextStatesConditional = nextStatesConditional;
        this.branchingProbsConditional = branchingProbsConditional;
    }
    
    
    
    
    /**
     * Use this constructor if the composite state has final states or exit states not on border
     */
    public CompositeState(String name, List<Region> regions, List<State> nextStates, List<Double> branchingProbs, int depth) {
    	this(name, regions, nextStates, branchingProbs, depth, -1.0);
    }

    /**
     * Use this constructor if the composite state has exit states on border
     */
    public CompositeState(String name, List<Region> regions, Map<State, List<State>> nextStatesConditional, Map<State,List<Double>> branchingProbsConditional, int depth) {
    	this(name, regions, nextStatesConditional, branchingProbsConditional, depth, -1.0);
    }
    
    public List<Region> getRegions() {
        return regions;
    }

    @Override
    public String toString() {
        return "CompositeState [numberOfRegions=" + regions.size() + ", numberOfNextStates=" + (nextStates != null? nextStates.size():0) + ", depth=" + depth
                + ", name=" + name + ", hasExitStatesOnBorder=" + exitStatesOnBorder + "]";
    }

    @Override
    public void accept(StateVisitor visitor) {
        visitor.visit(this);
    }
    
    public boolean hasExitStatesOnBorder() {
        return exitStatesOnBorder;
    }

    public Map<State, List<State>> getNextStatesConditional() {
        return nextStatesConditional;
    }

    public void setNextStatesConditional(Map<State, List<State>> nextStatesConditional) {
        this.nextStatesConditional = nextStatesConditional;
        this.exitStatesOnBorder=true;
    }

    public Map<State, List<Double>> getBranchingProbsConditional() {
        return branchingProbsConditional;
    }

    public void setBranchingProbsConditional(Map<State, List<Double>> branchingProbsConditional) {
        this.branchingProbsConditional = branchingProbsConditional;
    }
    
    
    //FIXME non fa copia del caso exit states on border, non � che c'� possibile errore, oppure � intenzionale e lo usa nella trasformazione
    @Override
    public State makeCopy() {
    	
    	if(exitStatesOnBorder) {
    	
    		return new CompositeState(name, regions,nextStatesConditional, branchingProbsConditional, depth, timeStep);
    		
    	}
    	
        return new CompositeState(name, regions, nextStates, branchingProbs, depth, timeStep);
    }
    
    //requires to reset nextStates
    public State makeCopy(int id, List<Region> newRegs) {
    	if(exitStatesOnBorder) {
        	
    		return new CompositeState(name+"_"+id, newRegs,nextStatesConditional, branchingProbsConditional, depth, timeStep);
    		
    	}
    	
        return new CompositeState(name+"_"+id, newRegs, nextStates, branchingProbs, depth, timeStep);
    }
    
}
