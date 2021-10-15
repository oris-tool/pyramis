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

public class Region {
    
    public enum RegionType{
        FINAL,
        EXIT,
        NEVER
    }
    
    private State initialState;
    private RegionType type;
    private double timeStep;
    
    private boolean compositeCycle;

    public Region(State initialState, RegionType type, double timeStep, boolean cycle) {
    	this.timeStep = timeStep;
        this.initialState = initialState;
        this.type = type;
        this.compositeCycle=cycle;
    }
    
    public Region(State initialState, RegionType type) {
        this(initialState, type, -1.0, false);
    }
    
    public Region(State initialState, RegionType type, boolean cycle) {
        this(initialState, type, -1.0, cycle);
    }
    
    public Region(State initialState, RegionType type, double timeStep) {
        this(initialState, type, timeStep, false);
    }

    public State getInitialState() {
        return initialState;
    }
    
    public void setInitialState(State initial) {
        this.initialState = initial;
    }

    public RegionType getType() {
        return type;
    }
    
    public double getTimeStep() {
        return timeStep;
    }
    
    public boolean containsCompositeCycle() {
        return compositeCycle;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((initialState == null) ? 0 : initialState.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        Region other = (Region) obj;
        if (initialState == null) {
            if (other.initialState != null)
                return false;
        } else if (!initialState.equals(other.initialState))
            return false;
        if (type != other.type)
            return false;
        if(timeStep!=other.timeStep){
        	return false;
        }
        return true;
    }
    
    public Region makeCopy() {
    	return new Region(initialState, type, timeStep, compositeCycle);
    }
    
    @Override
    public String toString() {
        return "Region_init_"  + initialState.getName();
    }
   
}
