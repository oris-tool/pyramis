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

import it.unifi.hierarchical.model.*;
import it.unifi.hierarchical.model.visitor.LogicalLocationVisitor;
import it.unifi.hierarchical.utils.StateUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Visitor of logical locations, supporting the identification of regions whose initial step
 * is a composite step of type first with ending regions having different next step PDF.
 */
public class RegionVisitor implements LogicalLocationVisitor {

	private final Set<LogicalLocation> evaluated;
	private boolean correctModel;
	private final Set<LogicalLocation> offenderSet;

	public RegionVisitor(){
		this.offenderSet = new HashSet<>();
		this.correctModel=true;
		this.evaluated=new HashSet<>();
	}

	@Override
	public void visit(SimpleStep simpleStep) {
		evaluated.add(simpleStep);

		//Visit successors not yet visited
		List<LogicalLocation> successors = simpleStep.getNextLocations();

		for (LogicalLocation successor : successors) {
			if (evaluated.contains(successor))
				continue;

			successor.accept(this);

			if (!correctModel)
				return;
		}
	}

	@Override
	public void visit(CompositeStep compositeStep) {
		evaluated.add(compositeStep);

		List<Region> regions = compositeStep.getRegions();

		for (Region region : regions) {
			if (StateUtils.isCompositeWithBorderExit(region.getInitialStep())) {
				offenderSet.add(region.getInitialStep());
				correctModel = false;
			}

			region.getInitialStep().accept(this);
		}

		// Visit successors not yet visited
		for (LogicalLocation successor : compositeStep.getNextLocations()) {
			if (evaluated.contains(successor))
				continue;

			successor.accept(this);
		}
	}

	@Override
	public void visit(FinalLocation finalLocation) {
		evaluated.add(finalLocation);
	}

	public boolean isModelCorrect() {
		return correctModel;
	}

	public Set<LogicalLocation> getOffenderSet() {
		return offenderSet;
	}
}
