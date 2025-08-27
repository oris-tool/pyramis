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
import it.unifi.hierarchical.utils.TwoParameterHypoEXP;
import org.oristool.eulero.modeling.stochastictime.TruncatedExponentialTime;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.domain.DBMZone;
import org.oristool.math.expression.Expolynomial;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

// Here cycles are unrolled (and so the cycle is not present anymore)

public class SojournTimeEvaluatorVisitor implements LogicalLocationVisitor {

	private final Map<LogicalLocation, NumericalValues> sojournTimeDistributions;
	private final Map<Region, NumericalValues> regionSojournTimeDistributions;
	private final Map<Region, TransientAnalyzer> regionTransientProbabilities;
	private final Set<LogicalLocation> evaluated;
	private final double timeStep;
	private final double timeLimit;

	public SojournTimeEvaluatorVisitor(double timeLimit) {
		this(-1.0, timeLimit);
	}

	/**
	 *
	 * @param timeStep  if <0 then calculations must account for variable steps
	 * @param timeLimit limit of the interval of interest
	 */
	public SojournTimeEvaluatorVisitor(double timeStep, double timeLimit) {
		this.sojournTimeDistributions = new HashMap<>();
		this.regionSojournTimeDistributions = new HashMap<>();
		this.regionTransientProbabilities = new HashMap<>();
		this.evaluated = new HashSet<>();
		this.timeStep = timeStep;
		this.timeLimit = timeLimit;
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

		Expolynomial density = simpleStep.getDensityFunction().getDensities().get(0);
		OmegaBigDecimal domainsEFT = simpleStep.getDensityFunction().getDomainsEFT();
		OmegaBigDecimal domainsLFT = simpleStep.getDensityFunction().getDomainsLFT();
		boolean isExp = ((GEN) simpleStep.getDensityFunction()).getDensity().isExponential();

		PartitionedFunction densityFunction = simpleStep.getDensityFunction();
		List<GEN> gens = new ArrayList<>();
		for(Function f : densityFunction.getFunctions()){

			Expolynomial expol = new Expolynomial(f.getDensity().integrate(Variable.X));
    		Map<Variable, OmegaBigDecimal> timePoint = new HashMap<>();
    		timePoint.put(Variable.X, new OmegaBigDecimal("" + f.getDomainsEFT()));
    		expol.sub(Expolynomial.newConstantInstance(expol.evaluate(timePoint)));

			DBMZone zone = f.getDomain();
			GEN gen = new GEN(zone, expol);
			gens.add(gen);
			
		}
		PartitionedGEN partitionedGEN = new PartitionedGEN(gens);
		values = NumericalUtils.evaluateFunction(partitionedGEN, new OmegaBigDecimal(""+timeLimit), new BigDecimal(""+step));

		sojournTimeDistributions.put(simpleStep, new NumericalValues(values, step));

		//Visit successors not yet visited
		List<LogicalLocation> successors = simpleStep.getNextLocations();
		for (LogicalLocation successor : successors) {
			if(evaluated.contains(successor))
				continue;
			successor.accept(this);
		}
	}

	public double CDF(double t, double rate, double a, double b) {
		double c = rate > 0 ? a : b;
		if (t < a) {
			return 0;
		}

		if (t > b) {
			return 1;
		}
		return (Math.abs(rate) * Math.exp(rate * c)) / (rate * (1 - Math.exp(-Math.abs(rate) * (b - a))))
				* (Math.exp(-rate * a) - Math.exp(-rate * t));
	}

	@Override
	public void visit(CompositeStep compositeStep) {

		evaluated.add(compositeStep);
		// Evaluate children sojourn time distribution
		// In case of Never simply study ignoring parent's peculiarity
		List<Region> regions = compositeStep.getRegions();
		for (Region region : regions) {
			region.getInitialStep().accept(this);
		}

		// Evaluate regions distribution
		Map<Region, NumericalValues> mapSojournTimeDistributions = new HashMap<>();

		// if Region is Never put a null in mapSojournTimeDistribution
		for (Region region : regions) {
			NumericalValues regionSojournTimeDistribution = evaluateRegionSojournTime(region);
			if (regionSojournTimeDistribution != null)
				mapSojournTimeDistributions.put(region, regionSojournTimeDistribution);
		}

		CompositeStepType regionsType = compositeStep.getType();

		NumericalValues sojournTimeDistribution = null;
		// FIXME Manage NEVERENDING regions
		switch (regionsType) {
			case FIRST:
				if (timeStep < 0.0) {// FIXME: if timeStep<0 then we have different timeStep for different steps
					sojournTimeDistribution = NumericalUtils.minCDFvar(mapSojournTimeDistributions.values(),
							compositeStep.getTimeStep());
				} else {
					sojournTimeDistribution = NumericalUtils.minCDF(mapSojournTimeDistributions.values());
				}
				break;
			case LAST:
				if (timeStep < 0.0) {
					sojournTimeDistribution = NumericalUtils.maxCDFvar(mapSojournTimeDistributions.values(),
							compositeStep.getTimeStep());
				} else {
					sojournTimeDistribution = NumericalUtils.maxCDF(mapSojournTimeDistributions.values());
				}
				break;
		}

		// System.out.println("composite");
		// System.out.println(Arrays.toString(sojournTimeDistribution.getValues()));
		//

		sojournTimeDistributions.put(compositeStep, sojournTimeDistribution);
		// Visit successors not yet visited
		if (StateUtils.isCompositeWithBorderExit(compositeStep)) {

			for (LogicalLocation exitState : compositeStep.getExitSteps().keySet()) {
				List<LogicalLocation> successors = compositeStep.getNextLocations(exitState);

				for (LogicalLocation successor : successors) {
					if (evaluated.contains(successor))
						continue;
					successor.accept(this);
				}
			}
		} else {
			List<LogicalLocation> successors = compositeStep.getNextLocations();
			for (LogicalLocation successor : successors) {
				if (evaluated.contains(successor))
					continue;
				successor.accept(this);
			}
		}
	}

	// Calculate CDF
	private NumericalValues evaluateRegionSojournTime(Region region) {

		RegionType type = region.getType();

		LogicalLocation initialState = region.getInitialStep();
		List<LogicalLocation> smpStates = StateUtils.getReachableStates(region.getInitialStep());

		boolean variableSteps;
		double time;
		if (timeStep < 0) {
			time = region.getTimeStep();
			variableSteps = true;
		} else {
			time = timeStep;
			variableSteps = false;
		}

		if (type == RegionType.NEVERENDING) {
			regionSojournTimeDistributions.put(region, null);
			TransientAnalyzer analyzer = new SMPAnalyzerWithBorderExitStates(region.getInitialStep(),
					sojournTimeDistributions, regionSojournTimeDistributions, timeLimit, time, variableSteps);
			regionTransientProbabilities.put(region, analyzer);
			return null;
		}

		LogicalLocation endState = StateUtils.findFinalLocation(smpStates);

		// System.out.println("SojournTimeDistributions -> Analyzer");

		// Date d1 = new Date();

		// long timeX;

		TransientAnalyzer analyzer = new SMPAnalyzerWithBorderExitStates(region.getInitialStep(),
				sojournTimeDistributions, regionSojournTimeDistributions, timeLimit, time, variableSteps);
		// Date d2 = new Date();

		// timeX = d2.getTime() - d1.getTime();
		// System.out.println(timeX+ " sojournPPP");

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

	public Map<Region, TransientAnalyzer> getRegionTransientProbabilities() {
		return regionTransientProbabilities;
	}

}
