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
import it.unifi.hierarchical.utils.NumericalUtils;
import it.unifi.hierarchical.utils.StateUtils;
import org.oristool.math.OmegaBigDecimal;

import java.math.BigDecimal;
import java.util.*;

//FIXME: This class may be removed and its methods integrated with those of class SojournTimeEvaluatorVisitor

public class SojournTimeEvaluatorVisitorForced implements LogicalLocationVisitor {

	private final Map<LogicalLocation, NumericalValues> sojournTimeDistributions;
	private final Map<Region, NumericalValues> regionSojournTimeDistributions;
	private final Map<Region, TransientAnalyzer> regionTransientProbabilities;
	private final Set<LogicalLocation> evaluated;
	private final double timeStep;
	private final double timeLimit;

	private final int CYCLE;

	// FIXME: Rename this attribute
	private SMPAnalyzerWithBorderExitStates cycleTransientList;


	public SMPAnalyzerWithBorderExitStates getCycleTransientList() {
		return cycleTransientList;
	}

	/**
	 *
	 * @param timeStep if <0 then calculations must account for variable steps
	 * @param timeLimit limit of the interval of interest
	 */
	public SojournTimeEvaluatorVisitorForced(double timeStep, double timeLimit, int CYCLE){
		this.sojournTimeDistributions = new HashMap<>();
		this.regionSojournTimeDistributions = new HashMap<>();
		this.regionTransientProbabilities = new HashMap<>();
		this.evaluated = new HashSet<>();
		this.timeStep = timeStep;
		this.timeLimit = timeLimit;
		this.CYCLE = CYCLE;
	}

	@Override
	public void visit(SimpleStep simpleStep) {

		evaluated.add(simpleStep);
		//Evaluate its sojourn time distribution

		double step;

		if(timeStep<0.0) {
			step= simpleStep.getTimeStep();
		}else {
			step= timeStep;
		}


		double[] values;

		double up = simpleStep.getUpperBound();
		if(up>0.) {

			if(simpleStep.getDepth()!=0) {
				System.out.println("ERROR ARBITRARY PRECISION NOT IN TOPLEVEL!! - SojournTimeEvaluatorForced");
				return;
			}

			step = up/50000;
			values = NumericalUtils.evaluateFunction(simpleStep.getDensityFunction(), new OmegaBigDecimal(""+up), new BigDecimal(""+step));
			values = NumericalUtils.computeCDFFromPDF(values,  new BigDecimal(""+step));

		}else {
			values = NumericalUtils.evaluateFunction(simpleStep.getDensityFunction(), new OmegaBigDecimal(""+timeLimit), new BigDecimal(""+step));
			values = NumericalUtils.computeCDFFromPDF(values,  new BigDecimal(""+step));
		}

		sojournTimeDistributions.put(simpleStep, new NumericalValues(values, step));

		//Visit successors not yet visited
		List<LogicalLocation> successors = simpleStep.getNextLocations();
		for (LogicalLocation successor : successors) {
			if(evaluated.contains(successor))
				continue;
			successor.accept(this);
		}
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void visit(CompositeStep compositeStep) {

		evaluated.add(compositeStep);
		//Evaluate children sojourn time distribution
		//In case of Never simply study ignoring parent's peculiarity
		List<Region> regions = compositeStep.getRegions();
		for (Region region : regions) {
			region.getInitialStep().accept(this);
		}

		//Evaluate regions distribution
		Map<Region, NumericalValues> mapSojournTimeDistributions = new HashMap<>();

		//if Region is Never put a null in mapSojournTimeDistribution
		for (Region region : regions) {
			//System.out.println(region.getInitialStep().getName());
			NumericalValues regionSojournTimeDistribution = evaluateRegionSojournTime(region);
			if(regionSojournTimeDistribution!= null)
				mapSojournTimeDistributions.put(region, regionSojournTimeDistribution);
		}

		CompositeStepType regionsType = compositeStep.getType();

		NumericalValues sojournTimeDistribution = null;
		//FIXME manage NEVERENDING regions
		switch (regionsType) {
			case FIRST:
				if(timeStep<0.0) {
					sojournTimeDistribution = NumericalUtils.minCDFvar(mapSojournTimeDistributions.values(), compositeStep.getTimeStep());
				}else {
					sojournTimeDistribution = NumericalUtils.minCDF(mapSojournTimeDistributions.values());
				}
				break;
			case LAST:
				if(timeStep<0.0) {
					sojournTimeDistribution = NumericalUtils.maxCDFvar(mapSojournTimeDistributions.values(), compositeStep.getTimeStep());
				}else {
					sojournTimeDistribution = NumericalUtils.maxCDF(mapSojournTimeDistributions.values());
				}
				break;
		}

		//		System.out.println("composite");
		//		System.out.println(Arrays.toString(sojournTimeDistribution.getValues()));
		//

		sojournTimeDistributions.put(compositeStep, sojournTimeDistribution);
		//Visit successors not yet visited
		if(StateUtils.isCompositeWithBorderExit(compositeStep)) {

			for (LogicalLocation exitState : compositeStep.getExitSteps().keySet()) {
				List<LogicalLocation> successors = compositeStep.getNextLocations(exitState);
				for (LogicalLocation successor : successors) {
					if(evaluated.contains(successor))
						continue;
					successor.accept(this);
				}
			}
		}else {
			List<LogicalLocation> successors = compositeStep.getNextLocations();
			for (LogicalLocation successor : successors) {
				if(evaluated.contains(successor))
					continue;
				successor.accept(this);
			}
		}
	}

	private NumericalValues evaluateRegionSojournTime(Region region) {
		System.out.println("Evaluate Region Sojourn Time");
		RegionType type= region.getType();

		LogicalLocation initialState = region.getInitialStep();
		List<LogicalLocation> smpStates = StateUtils.getReachableStates(region.getInitialStep());

		boolean variableSteps;
		double time;
		if(timeStep<0) {
			time=region.getTimeStep();
			variableSteps=true;
		}else {
			time=timeStep;
			variableSteps=false;
		}

		if(type==RegionType.NEVERENDING) {
			regionSojournTimeDistributions.put(region, null);
			cycleTransientList= new SMPAnalyzerWithBorderExitStates(region.getInitialStep(), sojournTimeDistributions, regionSojournTimeDistributions, timeLimit, time, null, variableSteps, CYCLE);
			regionTransientProbabilities.put(region, null);
			return null;
		}

		LogicalLocation endState = StateUtils.findFinalLocation(smpStates);

		System.out.println("SojournTimeDistributions -> Analyzer");
		TransientAnalyzer analyzer = new SMPAnalyzerWithBorderExitStates(region.getInitialStep(), sojournTimeDistributions, regionSojournTimeDistributions, timeLimit, time, variableSteps);

		NumericalValues sojournTimeDistribution = analyzer.getTransientProbability(initialState, endState);

		regionSojournTimeDistributions.put(region, sojournTimeDistribution);
		regionTransientProbabilities.put(region, analyzer);
		return sojournTimeDistribution;
	}

	@Override
	public void visit(FinalLocation finalLocation) {
		evaluated.add(finalLocation);
		sojournTimeDistributions.put(finalLocation, null);
	}

	public Map<LogicalLocation, NumericalValues> getSojournTimeDistributions() {
		return sojournTimeDistributions;
	}

	public Map<Region, NumericalValues> getRegionSojournTimeDistributions() {
		return regionSojournTimeDistributions;
	}

	public Map<Region, TransientAnalyzer> getRegionTransientProbabilities(){
		return regionTransientProbabilities;
	}

}
