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

import java.util.List;

/**
 * Represents a logical location of an HSMP model
 */
public abstract class LogicalLocation {
    // FIXME: Maybe the attribute depth is not needed
    //		  (note that depth is 0 for the top level, 1 for the subsequent lower level and so on).

    /**
     * Depth at which the logical location is situated, starting from 0 for top-level locations
     */
    protected int depth;

    /**
     * Name that identifies the logical location
     */
    protected String name;

    // FIXME: The concept of time step should be part of the analysis configuration (not of the model).
    /**
     * Time step at which the logical location is analyzed
     */
    protected double timeStep;

    /**
     * Indicates whether the logical location is the last location before the cycle (or loop) restarts
     */
    private boolean looping = false;

    /**
     * Indicates whether is part of a cycle (or loop)
     */
    private boolean cycle = false;

    /**
     * Constructor of a logical location, identified by name, depth and time step
     * @param name name of the logical location
     * @param timeStep time step at which the logical location is analyzed
     */
    protected LogicalLocation(String name, int depth, double timeStep) {
        if(name == null || name.trim().equals(""))
            throw new IllegalArgumentException("Name can't be empty");

        this.name = name;
        this.depth = depth;
        this.timeStep = timeStep;
    }

    protected LogicalLocation(String name, double timeStep) {
        if(name == null || name.trim().equals(""))
            throw new IllegalArgumentException("Name can't be empty");

        this.name = name;
        this.depth = 0;
        this.timeStep = timeStep;
    }

    /**
     * Constructor of a logical location, identified by name and depth
     * @param name name of the logical location
     */
    protected LogicalLocation(String name, int depth) {
        this(name, depth,-1.0);
    }

    protected LogicalLocation(String name) {
        this(name, 0,-1.0);
    }

    /**
     * Copy constructor of a logical location
     * @param logicalLocation logical location to be copied
     */
    protected LogicalLocation(LogicalLocation logicalLocation) {
        name = logicalLocation.name;
        depth = logicalLocation.depth;
        timeStep = logicalLocation.timeStep;
        cycle = logicalLocation.cycle;
        looping = logicalLocation.looping;
    }

    /**
     * Abstract method that forces sub-classes to implement a method to create a copy
     * @return a copy of the logical location
     */
    public abstract LogicalLocation makeCopy();

    /**
     * Abstract method that forces sub-classes to implement a method that returns a list of logical locations reachable
     * from the current one
     * @return a {@link List} of logical locations reachable from the current one
     */
    public abstract List<LogicalLocation> getNextLocations();

    /**
     * Abstract method that forces sub-classes to implement a method that returns a list of branching probabilities of
     * logical locations reachable from the current one
     * @return a {@link List} of branching probabilities of logical locations reachable from the current one
     */
    public abstract List<Double> getBranchingProbabilities();

    // FIXME: This condition should be automatically checked (note that in some cases the last step of a cycle may not
    //  be defined).
    /**
     * Getter of looping attribute
     * @return a boolean indicating whether the logical location is part of a cycle and is the last step of the cycle
     */
    public boolean isLooping() {
        return looping;
    }

    // FIXME: This condition should be automatically checked.
    /**
     * Getter of cycle attribute
     * @return a boolean indicating whether the logical location is part of a cycle
     */
    public boolean isCycle() {
        return cycle;
    }

    /**
     * Setter of both cycle and looping attributes
     * @param cycle true if the logical location is part of a cycle, false otherwise
     * @param looping true if the logical location is part of a cycle and is the last before the restart, false
     *                otherwise
     */
    public void setCyleLooping(boolean cycle, boolean looping) {
        this.looping = looping;
        this.cycle = cycle;
    }

    /**
     * Getter of the depth attribute
     * @return the depth at which the logical location is situated
     */
    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * Getter of the time step attribute
     * @return the time step at which the logical location is analyzed
     */
    public double getTimeStep() {
        return timeStep;
    }

    /**
     * Getter of the name of the logical location
     * @return the name of the logical location
     */
    public String getName() {
        return name;
    }

    /**
     * Default hashCode() method
     * @return the integer number representing the hashcode of the logical location
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + depth;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    /**
     * Default equals method
     * @param obj the object to be compared
     * @return true if the two objects (in this case, logical locations) are equals, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        LogicalLocation other = (LogicalLocation) obj;

        if (depth != other.depth)
            return false;

        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;

        return timeStep == other.timeStep;
    }

    /**
     * Abstract method used by {@link LogicalLocationVisitor} visitors to visit a logical location: sub-classes will
     * have to implement this method to accept visitors
     * @param visitor visitor that is currently visit the logical location
     */
    public abstract void accept(LogicalLocationVisitor visitor);
}
