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

package it.unifi.hierarchical.analysis;

import it.unifi.hierarchical.model.LogicalLocation;

import java.util.List;

/**
 * Common interface of methods for transient analysis of the semi-Markov process underlying a {@link it.unifi.stlab.hierarchical.model.Region}.
 */
public interface TransientAnalyzer {

    public double getTimeLimit();

    public double getTimeStep();

    public List<LogicalLocation> getStates();

    public NumericalValues getTransientProbability(LogicalLocation from, LogicalLocation to);
}
