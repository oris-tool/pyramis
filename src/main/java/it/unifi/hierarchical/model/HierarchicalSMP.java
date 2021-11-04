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

/**
 * An HSMP model. 
 */
package it.unifi.hierarchical.model;

// FIXME: Rename HierarchicalSMP to HSMP.
public class HierarchicalSMP {
    
    private State initialState;

    public HierarchicalSMP(State initialState) {
        this.initialState = initialState;
    }

    public State getInitialState() {
        return initialState;
    }

    @Override
    public String toString() {
        return "HierarchicalSMP [\n\tinitialState=" + initialState + "\n]";
    }
   
}
