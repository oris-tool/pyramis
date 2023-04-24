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
import org.apache.commons.math3.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a composite step, which has a certain number of concurrent regions of the same {@link CompositeStepType}
 * (FIRST, LAST). A composite step of type FIRST terminates as soon any of its regions terminates, while composite steps
 * of type LAST terminates when all its regions have terminated.
 * If there are regions of type FIRST, a composite step supports different exit steps based on which region has
 * terminated first.
 */
public class CompositeStep extends Step {

    /**
     * Type of composite step, meaning that all regions will be of type FIRST or LAST
     */
    private final CompositeStepType type;

    /**
     * {@link List} of regions that can be found inside the composite step
     */
    private final List<Region> regions;

    /**
     * Map used when all regions are of type FIRST and have a different next step based on which region has terminated
     * first; here, final locations (references to generic {@link LogicalLocation} instances instead of
     * {@link FinalLocation}) are mapped with a list of their possible next steps, associated with a branching
     * probability.
     */
    private final Map<LogicalLocation, List<Pair<LogicalLocation, Double>>> exitSteps;

    /**
     * Constructor of a composite step, complete with all possible arguments
     * @param name name of the composite step
     * @param type type of all the regions found in composite step (FIRST or LAST)
     * @param timeStep
     */
    public CompositeStep(String name, CompositeStepType type, int depth, double timeStep) {
        super(name, depth, timeStep);
        this.type = type;
        regions = new ArrayList<>();
        exitSteps = new LinkedHashMap<>();
    }

    public CompositeStep(String name, CompositeStepType type, double timeStep) {
        super(name, 0, timeStep);
        this.type = type;
        regions = new ArrayList<>();
        exitSteps = new LinkedHashMap<>();
    }

    /**
     * Constructor of a composite step, leaving a default time step
     * @param name name of the composite step
     * @param type type of all the regions found in composite step (FIRST or LAST)
     */
    public CompositeStep(String name, CompositeStepType type, int depth) {
        this(name, type, depth, -1.0);
    }

    public CompositeStep(String name, CompositeStepType type) {
        this(name, type, 0, -1.0);
    }

    /**
     * Copy constructor of a composite step
     * @param compositeStep composite step to be copied
     */
    public CompositeStep(CompositeStep compositeStep) {
        super(compositeStep.name, compositeStep.depth, compositeStep.timeStep);
        type = compositeStep.type;
        regions = new ArrayList<>(compositeStep.regions);
        exitSteps = new HashMap<>(compositeStep.exitSteps);
    }

    /**
     * Copy constructor of a composite step, assigning a different name to the new composite step
     * @param compositeStep composite step to be copied
     * @param id id to postpone to the composite step name
     */
    public CompositeStep(CompositeStep compositeStep, int id) {
        super(compositeStep.name + "_" + id, compositeStep.depth, compositeStep.timeStep);
        type = compositeStep.type;
        regions = new ArrayList<>(compositeStep.regions);
        exitSteps = new HashMap<>(compositeStep.exitSteps);
    }

    /**
     * Copy constructor of a composite step, assigning a different name to the new composite step
     * @param compositeStep composite step to be copied
     * @param id id to postpone to the composite step name
     * @param regions new {@link List} of regions to assign to the newly created composite step
     */
    public CompositeStep(CompositeStep compositeStep, int id, List<Region> regions) {
        super(compositeStep.name + "_" + id, compositeStep.depth, compositeStep.timeStep);
        type = compositeStep.type;
        // FIXME: check whether a deep copy of regions is needed, including final locations within the composite step
        this.regions = new ArrayList<>(regions);
        exitSteps = new HashMap<>();
        //exitSteps = new HashMap<>(compositeStep.exitSteps);
    }

    /**
     * Adds a single region to the composite step
     * @param region region to be added to the composite step
     */
    public void addRegion(Region region) {
        regions.add(region);

        //DepthVisitor depthVisitor = new DepthVisitor(depth + 1);
        //depthVisitor.visit(region.getInitialStep());
    }

    /**
     * Adds a list of regions to the composite step
     * @param regions {@link List} of regions to be added to the composite step
     */
    public void addRegions(List<Region> regions) {
        this.regions.addAll(regions);
    }

    /**
     * Adds a final location, along with its reachable next steps and their branching probability; can be used only on
     * composite steps that contain regions of type FIRST
     * @param finalLocation final location of one of the regions of the composite step
     * @param nextLocations {@link List} of next steps reachable from the final location
     * @param branchingProbabilities {@link List} of branching probabilities of next steps from the final location (must
     *                                           be in the same order as the next steps)
     */
    public void addExitStep(LogicalLocation finalLocation, List<LogicalLocation> nextLocations, List<Double> branchingProbabilities) {
        if (type.equals(CompositeStepType.LAST))
            throw new RuntimeException("Final location with different exit steps can only be specified in composite" +
                    " steps with regions of type FIRST");

        if (nextLocations.size() != branchingProbabilities.size())
            throw new IllegalArgumentException("Regions, next locations and branching probabilities must have the " +
                    "same size");

        List<Pair<LogicalLocation, Double>> nextLocationProbabilities = new ArrayList<>();

        for (int index = 0; index < nextLocations.size(); index++)
            nextLocationProbabilities.add(new Pair<>(nextLocations.get(index), branchingProbabilities.get(index)));

        exitSteps.put(finalLocation, nextLocationProbabilities);

        nextLocations.forEach(nextLocation -> nextLocation.setDepth(depth));
    }

    /**
     * Adds all the possible next locations that can be reached once the composite step has terminated its execution;
     * this steps are common to all possible terminations, both FIRST and LAST regions type
     * @param nextLocations {@link List} of next steps reachable from the composite step
     * @param branchingProbabilities {@link List} of branching probabilities of next steps from the composite step (must
     *      *                                           be in the same order as the next steps)
     */
    public void addNextLocations(List<LogicalLocation> nextLocations, List<Double> branchingProbabilities) {
        if (nextLocations.size() != branchingProbabilities.size())
            throw new IllegalArgumentException("Regions, next locations and branching probabilities must have the " +
                    "same size");

        for (int index = 0; index < nextLocations.size(); index++)
            addNextLocation(nextLocations.get(index), branchingProbabilities.get(index));
    }

    /**
     * Getter of type attribute
     * @return the type of composite step
     */
    public CompositeStepType getType() {
        return type;
    }

    /**
     * Checks whether the composite step has exit steps on border
     * @return the truth value indicating if there are exit steps on border or not
     */
    public boolean hasExitStepsOnBorder() {
        return !exitSteps.isEmpty();
    }

    /**
     * Getter of regions attribute
     * @return a {@link List} of all the regions inside the composite step
     */
    public List<Region> getRegions() {
        return regions;
    }

    /**
     * Getter of exit steps
     * @return a {@link Map} indicating next locations reachable from the composite step and their branching probability for
     * each final location referring to a region of the composite step
     */
    public Map<LogicalLocation, List<Pair<LogicalLocation, Double>>> getExitSteps() {
        return exitSteps;
    }

    /**
     * Getter of the next locations reachable from the composite step, automatically considering whether the composite
     * step has exit steps on border or not
     * @return a {@link List} containing all the reachable next locations
     */
    @Override
    public List<LogicalLocation> getNextLocations() {
        Set<LogicalLocation> nextLocations = new HashSet<>();

        if (hasExitStepsOnBorder()) {
            for (List<Pair<LogicalLocation, Double>> locations : exitSteps.values()) {
                for (Pair<LogicalLocation, Double> location : locations)
                    nextLocations.add(location.getKey());
            }
        } else {
            nextLocations.addAll(this.nextLocations.keySet());
        }

        return new ArrayList<>(nextLocations);
    }

    /**
     * Getter of the branching probabilities of next locations reachable from the composite step, automatically
     * considering whether the composite step has exit steps on border or not
     * @return a {@link List} containing the branching probabilities of all the reachable next locations
     */
    @Override
    public List<Double> getBranchingProbabilities() {
        List<Double> branchingProbabilities = new ArrayList<>();

        if (hasExitStepsOnBorder()) {
            for (List<Pair<LogicalLocation, Double>> locations : exitSteps.values()) {
                for (Pair<LogicalLocation, Double> location : locations)
                    branchingProbabilities.add(location.getValue());
            }
        } else {
            branchingProbabilities.addAll(this.nextLocations.values());
        }

        return branchingProbabilities;
    }

    /**
     * Getter that associates to each final location all its reachable next locations; if the composite step does not
     * have exit steps on border, each final location will have the same list of reachable next locations
     * @return a {@link Map} associating to each final location a {@link List} of next locations reachable from it
     */
    public Map<LogicalLocation, List<LogicalLocation>> getExitStepsOnBorder() {
        Map<LogicalLocation, List<LogicalLocation>> exitStatesOnBorder = new HashMap<>();

        if (this.hasExitStepsOnBorder()) {
            for (LogicalLocation finalLocation : exitSteps.keySet())
                exitStatesOnBorder.put(finalLocation,
                        exitSteps.get(finalLocation).stream().map(Pair::getKey).collect(Collectors.toList()));
        } else {
            exitSteps.keySet().forEach(finalLocation -> exitStatesOnBorder.put(finalLocation, getNextLocations()));
        }

        return exitStatesOnBorder;
    }

    /**
     * Getter that associates to each final location the branching probabilities of its reachable next locations;
     * if the composite step does not have exit steps on border, each final location will have the same list of
     * branching probabilities of reachable next locations
     * @return a {@link Map} associating to each final location a {@link List} of branching probabilities of next
     * locations reachable from it
     */
    public Map<LogicalLocation, List<Double>> getExitStatesProbabilities() {
        Map<LogicalLocation, List<Double>> exitStatesProbability = new HashMap<>();

        for (LogicalLocation finalLocation : exitSteps.keySet())
            exitStatesProbability.put(finalLocation,
                    exitSteps.get(finalLocation).stream().map(Pair::getValue).collect(Collectors.toList()));

        return exitStatesProbability;
    }

    /**
     * Getter of the next locations reachable from a given final location of the composite step; if the composite step
     * does not have exit steps on border, it will return all next locations
     * @param finalLocation final location of which next locations must be returned
     * @return a {@link List} of all the reachable next steps from the given final location
     */
    public List<LogicalLocation> getNextLocations(LogicalLocation finalLocation) {
        if (!exitSteps.isEmpty())
            return exitSteps.get(finalLocation).stream().map(Pair::getKey).collect(Collectors.toList());

        return super.getNextLocations();
    }

    /**
     * Getter of the branching probabilities of next locations reachable from a given final location of the composite
     * step; if the composite step does not have exit steps on border, it will return the branching probabilities of all
     * next locations
     * @param finalLocation final location of which branching probabilities of next locations must be returned
     * @return a {@link List} of all the reachable next steps from the given final location
     */
    public List<Double> getBranchingProbabilities(LogicalLocation finalLocation) {
        if (!exitSteps.isEmpty())
            return exitSteps.get(finalLocation).stream().map(Pair::getValue).collect(Collectors.toList());

        return super.getBranchingProbabilities();
    }

    /**
     * Default method to return a stringified version of the composite step, along with its relevant information
     * @return a {@link String} containing the composite step and its attributes
     */
    @Override
    public String toString() {
        if (hasExitStepsOnBorder()) {
            Set<LogicalLocation> uniqueNextLocations = new HashSet<>();

            for (List<Pair<LogicalLocation, Double>> locations : exitSteps.values())
                for (Pair<LogicalLocation, Double> location : locations)
                    uniqueNextLocations.add(location.getKey());

            return "CompositeStep [numberOfRegions=" + regions.size() + ", numberOfNextSteps=" + uniqueNextLocations.size() +
                    ", depth=" + depth + ", name=" + name + ", hasExitStepsOnBorder=" + true + "]";
        }

        return "CompositeStep [numberOfRegions=" + regions.size() + ", numberOfNextSteps=" +
                (nextLocations != null ? nextLocations.size() : 0) + ", depth=" + depth + ", name=" + name +
                ", hasExitSteps=" + false + "]";
    }

    /**
     * Default method used by {@link LogicalLocationVisitor} visitors to visit a composite step
     * @param visitor visitor that is currently visit the composite step
     */
    @Override
    public void accept(LogicalLocationVisitor visitor) {
        visitor.visit(this);
    }

    // This method makes a copy of the step using the same regions (it is used to manage
    // composite steps with different next step PDF depending on the region that has terminated first).

    /**
     * Makes a copy of the step using the same regions (it is used to manage composite steps with different next step
     * PDF depending on the region that has terminated first).
     * @return a copy of the composite step, cast to logical location since it overrides a previously defined method
     */
    @Override
    public LogicalLocation makeCopy() {
        return new CompositeStep(this);
    }

    /**
     * Makes a copy of the step, postponing an id to its name and using a list of new regions for the copy
     * @return a copy of the composite step, cast to logical location since it overrides a previously defined method
     */
    public LogicalLocation makeCopy(int id, List<Region> newRegions) {
        return new CompositeStep(this, id, newRegions);
    }
}

