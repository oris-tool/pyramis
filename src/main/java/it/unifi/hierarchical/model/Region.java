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

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Represents a region, part of a {@link CompositeStep}, which is then composed of logical locations and can be ending
 * (no cycles) or never-ending (with cycles)
 */
public class Region {

    /**
     * Initial step located inside of the region
     */
    private LogicalLocation initialStep;

    /**
     * Type of region between ending and never-ending
     */
    private final RegionType type;

    // FIXME: timeStep should be part of the analysis configuration.
    /**
     * Time step at which the region is analyzed
     */
    private final double timeStep;

    /**
     * Indicates whether the region contains a cycle or not (the parent composite containsa a cycle)
     */
    private final boolean compositeCycle;

    /**
     * Constructor of a region, complete with all possible arguments
     * @param initialStep is the initial state located inside the region
     * @param type is the type of region that indicates whether it has a final location or is a neverending region
     * @param timeStep is the time step at which the region will be analyzed
     * @param cycle indicates whether the region contains a cycle or not
     */
    public Region(LogicalLocation initialStep, RegionType type, double timeStep, boolean cycle) {
        this.timeStep = timeStep;
        this.initialStep = initialStep;
        this.type = type;
        this.compositeCycle = cycle;
    }

    /**
     * Constructor of a region, omitting the time step parameter
     * @param initialStep is the initial state located inside the region
     * @param type is the type of region that indicates whether it has a final location or is a neverending region
     * @param cycle indicates whether the region contains a cycle or not
     */
    public Region(LogicalLocation initialStep, RegionType type, boolean cycle) {
        this(initialStep, type, -1.0, cycle);
    }

    /**
     * Constructor of a region, omitting the cycle parameter
     * @param initialStep is the initial state located inside the region
     * @param type is the type of region that indicates whether it has a final location or is a neverending region
     * @param timeStep is the time step at which the region will be analyzed
     */
    public Region(LogicalLocation initialStep, RegionType type, double timeStep) {
        this(initialStep, type, timeStep, false);
    }

    /**
     * Constructor of a region, using a default time step and without any cycle inside
     * @param initialStep is the initial state located inside the region
     * @param type is the type of region that indicates whether it has a final location or is a neverending region
     */
    public Region(LogicalLocation initialStep, RegionType type) {
        this(initialStep, type, -1.0, false);
    }

    /**
     * Method that, starting from the given initial location of the region, returns the final location found in the
     * region; if the region is never ending or does not contain any initial location, it returns null
     * @return the final location situated inside the region
     */
    public LogicalLocation getFinalLocation() {
        if (type.equals(RegionType.NEVERENDING))
            return null;

        Queue<LogicalLocation> steps = new ArrayDeque<>();
        steps.add(initialStep);

        while (!steps.isEmpty()) {
            LogicalLocation currentLocation = steps.poll();

            if (currentLocation instanceof FinalLocation)
                return currentLocation;

            steps.addAll(currentLocation.getNextLocations());
        }

        return null;
    }

    /**
     * Getter of the initial step found inside the region
     * @return the initial step of the region
     */
    public LogicalLocation getInitialStep() {
        return initialStep;
    }

    /**
     * Setter of the initial step of the region
     * @param initial is the initial step that can be found in the region
     */
    public void setInitialStep(LogicalLocation initial) {
        this.initialStep = initial;
    }

    /**
     * Getter of the region type
     * @return the type of region, between ending and neverending
     */
    public RegionType getType() {
        return type;
    }

    /**
     * Getter of the time step at which the region will be analyzed
     * @return the time step of the region
     */
    public double getTimeStep() {
        return timeStep;
    }

    // FIXME: This condition should be automatically checked.
    /**
     * Getter of the composite cycle attribute
     * @return true if the region contains a cycle that visits a non-top-level composite step.
     */
    public boolean containsCompositeCycle() {
        return compositeCycle;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((initialStep == null) ? 0 : initialStep.hashCode());
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

        if (initialStep == null) {
            if (other.initialStep != null)
                return false;
        } else if (!initialStep.equals(other.initialStep))
            return false;

        if (type != other.type)
            return false;

        return timeStep == other.timeStep;
    }

    /**
     * Method used to make a copy of the region, used in the analysis process
     * @return a new region with the same initial step, type, time step and composite cycle value
     */
    public Region makeCopy() {
        return new Region(initialStep, type, timeStep, compositeCycle);
    }

    @Override
    public String toString() {
        return "Region_init_"  + initialStep.getName();
    }

}

