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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a final location that can be found in an HSMP model, used as the final location of a region inside a
 * composite step
 */
public class FinalLocation extends LogicalLocation {

    /**
     * Constructor of a final location, specifying a name and a depth
     * @param name name of the final location
     */
    public FinalLocation(String name, int depth) {
        super(name, depth);
    }

    public FinalLocation(String name) {
        super(name, 0);
    }

    /**
     * Constructor of a final location, specifying only a depth and assigning a random name
     */
    public FinalLocation(int depth) {
        super("final" + UUID.randomUUID(), depth);
    }

    public FinalLocation() {
        super("final" + UUID.randomUUID(), 0);
    }

    /**
     * Default method to return a stringified version of the final location, along with its relevant information
     * @return a {@link String} containing the final location and its attributes
     */
    @Override
    public String toString() {
        return "FinalLocation [depth=" + depth + ", name=" + name + "]";
    }

    /**
     * Default method used by {@link LogicalLocationVisitor} visitors to visit a final location
     * @param visitor visitor that is currently visit the final location
     */
    @Override
    public void accept(LogicalLocationVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Fake implementation of the next locations' getter, defined by {@link LogicalLocation}, since a final location
     * does not have (at least not directly) any next location
     * @return an empty {@link List} representing no next locations
     */
    @Override
    public List<LogicalLocation> getNextLocations() {
        return new ArrayList<>();
    }

    /**
     * Fake implementation of the branching probabilities' getter, defined by {@link LogicalLocation}, since a final
     * location does not have (at least not directly) any next locations' branching probability
     * @return an empty {@link List} representing no next locations' probabilities
     */
    @Override
    public List<Double> getBranchingProbabilities() {
        return new ArrayList<>();
    }

    /**
     * Makes an identical copy of the final location
     * @return a copy of the final location, cast to logical location since it overrides a previously defined method
     */
    @Override
    public LogicalLocation makeCopy() {
        return new FinalLocation(name, depth);
    }

    /**
     * Makes a copy of the final location by appending the given id to the name
     * @param id id to append to the final location's name
     * @return a copy of the final location, cast to logical location since it overrides a previously defined method
     */
    public LogicalLocation makeCopy(int id) {
        return new FinalLocation(name + "_" + id, depth);
    }
}
