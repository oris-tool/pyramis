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

/**
 * Represents a Hierarchical Semi-Markov Process, characterized by its initial step
 */

public class HSMP {
    // TODO: Servono distribuzioni perché ci potrebbero essere più stati iniziali

    /**
     * Initial step from which the HSMP unfolds
     */
    private final LogicalLocation initialStep;

    /**
     * Default constructor, which initializes the HSMP with its initial step
     * @param initialStep initial step of the HSMP
     */
    public HSMP(LogicalLocation initialStep) {
        this.initialStep = initialStep;
    }

    /**
     * Getter of the initial step
     * @return a logical location representing the initial step of the HSMP
     */
    public LogicalLocation getInitialStep() {
        return initialStep;
    }

    /**
     * Default method to return a stringified version of the HSMP, along with its relevant information
     * @return a {@link String} containing the HSMP and its attribute
     */
    @Override
    public String toString() {
        return "HSMP [\n\tinitialStep=" + initialStep + "\n]";
    }

}
