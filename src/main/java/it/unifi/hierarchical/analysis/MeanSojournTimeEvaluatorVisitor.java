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

//2.1- Navigate on the higher level searching for all possible state
//2.2- For each one, evaluate the mean based on sojourn time distribution,
//     if composite,
public class MeanSojournTimeEvaluatorVisitor implements LogicalLocationVisitor {

	private final Map<LogicalLocation, NumericalValues> sojournTimeDistributions;
	private final Map<Region, NumericalValues> regionSojournTimeDistributions;
	private final Map<Region, TransientAnalyzer> regionTransientProbabilities;
	private final Map<LogicalLocation, Region> parentRegions;
	private final Map<Region, CompositeStep> parentStates;
	private final Map<LogicalLocation, Double> meanSojournTimes;
	private final double timeLimit;
	private final boolean variableTimeStep;

	private final Map<Region, Map<LogicalLocation, NumericalValues>> absorbingProbabilities;//Given a region that contain a state, give the distribution of time to be absorbed in such state
	private final Map<Region, NumericalValues> shiftedExitDistributions;//Given a target region, given the minimum exit distribution of parallel regions

	/**
	 *
	 * @param initialState
	 * @param sojournTimeDistributions
	 * @param regionSojournTimeDistributions
	 * @param regionTransientProbabilities
	 * @param variableTimeStep true if different timeSteps for regions and states are present
	 * @param timeLimit
	 */
	public MeanSojournTimeEvaluatorVisitor(LogicalLocation initialState, Map<LogicalLocation, NumericalValues> sojournTimeDistributions, Map<Region, NumericalValues> regionSojournTimeDistributions, Map<Region, TransientAnalyzer> regionTransientProbabilities, boolean variableTimeStep, double timeLimit) {
		this.sojournTimeDistributions = sojournTimeDistributions;
		this.regionSojournTimeDistributions = regionSojournTimeDistributions;
		this.regionTransientProbabilities = regionTransientProbabilities;
		ModelStructureAnalyzer modelStructure = new ModelStructureAnalyzer(initialState);
		this.parentRegions = modelStructure.getParentRegions();
		this.parentStates = modelStructure.getParentStates();
		this.meanSojournTimes = new HashMap<>();
		this.absorbingProbabilities = new HashMap<>();
		this.shiftedExitDistributions = new HashMap<>();
		this.timeLimit = timeLimit;

		this.variableTimeStep=variableTimeStep;
	}

	@Override
	public void visit(SimpleStep simpleStep) {
		evaluateStateMeanSojournTime(simpleStep);
	}

	@Override
	public void visit(CompositeStep compositeStep) {
		evaluateStateMeanSojournTime(compositeStep);

		for (Region region : compositeStep.getRegions()) {
			if (meanSojournTimes.containsKey(region.getInitialStep()))
				continue;

			region.getInitialStep().accept(this);
		}
	}

	@Override
	public void visit(FinalLocation finalLocation) {
		//Do nothing!
	}

	private void evaluateStateMeanSojournTime(LogicalLocation state) {
		if (state.getDepth() == 0) {//Top level sojourn time --> not affected by exit of any other region
			evaluateTopLevelStateSojournTime(state);
		} else { //Not top level sojourn time --> affected by exit in parallel regions
			evaluateLowerLevelStateSojournTime(state);
		}

		//Evaluate mean sojourn time also for successor states if required
		if(StateUtils.isCompositeWithBorderExit(state)) {
			CompositeStep cState = (CompositeStep) state;

			for (LogicalLocation exitState : cState.getExitSteps().keySet()) {
				List<LogicalLocation> successors = cState.getNextLocations(exitState);

				for (LogicalLocation successor : successors) {
					if(meanSojournTimes.containsKey(successor))
						continue;
					successor.accept(this);
				}
			}
		}else {
			for(LogicalLocation successor: state.getNextLocations()) {
				if(meanSojournTimes.containsKey(successor))
					continue;
				successor.accept(this);
			}
		}
	}

	public Map<LogicalLocation, Double> getMeanSojournTimes() {
		return meanSojournTimes;
	}

	private void evaluateTopLevelStateSojournTime(LogicalLocation state) {
		NumericalValues sojournDistrubution = sojournTimeDistributions.get(state);

		double timeStep = sojournDistrubution.getStep();
		double mean = 0;
		for (int t = 0; t < sojournDistrubution.getValues().length; t++) {
			mean+= (1-sojournDistrubution.getValues()[t]) * timeStep;
		}
		meanSojournTimes.put(state, mean);
	}

	private void evaluateLowerLevelStateSojournTime(LogicalLocation state) {

		//1- Get region transient probabilities. Since it is not a top level state, it must belong to a region
		Region parentRegion = parentRegions.get(state);
		TransientAnalyzer parentRegionAnalysis = regionTransientProbabilities.get(parentRegion);

		double greatestTimeStep = parentRegionAnalysis.getTimeStep();
		NumericalValues transientNumerical = parentRegionAnalysis.getTransientProbability(parentRegion.getInitialStep(), state);

		//2- Get exit distributions of parallel regions at same level of the direct parent composite state
		List<NumericalValues> exitDistributions = new ArrayList<>();
		CompositeStep parentState = parentStates.get(parentRegion);
		if(!(parentState instanceof CompositeStep))
			throw new IllegalStateException("A non composite state can't contains regions!");
		for (Region r : parentState.getRegions()) {
			if(!parentState.getType().equals(CompositeStepType.FIRST))//If final (or Never), neglect parallel regions since they don't affect sojourn time
				continue;
			if(r.equals(parentRegion))//Consider only other regions, not the current one
				continue;
			if(r.getType()==RegionType.NEVERENDING)//Consider only other regions, not the current one
				continue;
			NumericalValues regDistro = regionSojournTimeDistributions.get(r);

			//REMARK all timeSteps must be multiple of each other
			if(variableTimeStep && regDistro.getStep()>greatestTimeStep) {
				greatestTimeStep = regDistro.getStep();
			}
			exitDistributions.add(regDistro);
		}

		boolean noExitDistrib;

		//3- Get exit distribution of parallel regions at higher level. Note that distribution must have
		//the same time origin and thus need to be shifted
		if(state.getDepth() > 1) {

			//3.1 Get parent exit distribution of previous level region
			Region previousParentRegion = parentRegions.get(parentStates.get(parentRegion));
			NumericalValues parentExitDistribution = shiftedExitDistributions.get(previousParentRegion);

			noExitDistrib = true;
			for(int i =0; i<parentExitDistribution.getValues().length;i++) {
				if(parentExitDistribution.getValues()[i]>0.0) {
					noExitDistrib=false;
					break;
				}
			}

			if(variableTimeStep && previousParentRegion.getTimeStep()>greatestTimeStep) {
				greatestTimeStep = previousParentRegion.getTimeStep();
			}
			if(variableTimeStep && parentExitDistribution.getStep()>greatestTimeStep) {
				greatestTimeStep = parentExitDistribution.getStep();
			}
			//here we have determined the least common multiplier step (if -all- steps are multiples of each other)

			if(!noExitDistrib) {

				//3.2 evaluate time to be absorbed in current state in the current region
				absorbingProbabilities.computeIfAbsent(previousParentRegion, k -> new HashMap<>());

				//REMARK here we call analyzer

				if(absorbingProbabilities.get(previousParentRegion).get(parentState) == null) {

					//CREATE ABSORBING PROBABILITIES: CHANGE THE MODEL FOR ABSORPTION!!
					TransientAnalyzer analyzer = new SMPAnalyzerWithBorderExitStates(previousParentRegion.getInitialStep(), sojournTimeDistributions, regionSojournTimeDistributions, timeLimit, greatestTimeStep, parentState, variableTimeStep);
					double[] absorbingResult = analyzer.getTransientProbability(previousParentRegion.getInitialStep(), parentState).getValues();
					absorbingProbabilities.get(previousParentRegion).put(parentState, new NumericalValues(absorbingResult, greatestTimeStep));

				}
				//not necessarily scaled
				NumericalValues absorbingProbs = absorbingProbabilities.get(previousParentRegion).get(parentState);

				NumericalValues absorbingProbsRescaled = NumericalUtils.rescaleCDF(absorbingProbs, greatestTimeStep);

				NumericalValues parentExitDistributionRescaled = NumericalUtils.rescaleCDF(parentExitDistribution, greatestTimeStep);

				//3.3 evaluate sojourn time conditioned to be absorbed (shift and project)

				NumericalValues parallelExitDistribution = NumericalUtils.shiftAndProjectAndMinimum(
						absorbingProbsRescaled,
						List.of(parentExitDistributionRescaled));

				//3.5 add to the "exitDistributions" array
				exitDistributions.add(parallelExitDistribution);
			}
		}

		//4- Evaluate reaching probability of target sub-state, given that we are in the parent state
		double reachingProbability = 1;

		if(state.getDepth() > 1) {

			Region previousParentRegion = parentRegions.get(parentStates.get(parentRegion));
			NumericalValues absorbingProbs = absorbingProbabilities.get(previousParentRegion).get(parentState);
			NumericalValues parentExitDistribution = shiftedExitDistributions.get(previousParentRegion);

			NumericalValues absorbingProbsRescaled = NumericalUtils.rescaleCDF(absorbingProbs, greatestTimeStep);
			NumericalValues parentExitDistributionRescaled = NumericalUtils.rescaleCDF(parentExitDistribution, greatestTimeStep);

			double[] absPDF = NumericalUtils.computePDFFromCDF(absorbingProbsRescaled.getValues(), new BigDecimal("" + greatestTimeStep));
			double[] exitPDF = NumericalUtils.computePDFFromCDF(parentExitDistributionRescaled.getValues(), new BigDecimal("" + greatestTimeStep));
			//Evaluate probability that absorbingProbs is faster
			reachingProbability = 0;
			for(int t1=0; t1<absPDF.length; t1++) {
				//was t2=0 e if(t1 < t2)
				for(int t2=t1; t2<exitPDF.length; t2++) {
					reachingProbability+= absPDF[t1] * exitPDF[t2] * greatestTimeStep * greatestTimeStep;
				}
			}
		}

		//5- Evaluate the exit distribution as the minimum and save it for lower regions
		double[] finalExitDistribution = new double[NumericalUtils.computeTickNumber(new OmegaBigDecimal("" + timeLimit), new BigDecimal("" + greatestTimeStep))];
		if(exitDistributions.size() == 0) {
			Arrays.fill(finalExitDistribution, 0.0);
			finalExitDistribution[finalExitDistribution.length-1]=1.0;
		}else if(exitDistributions.size() == 1) {
			NumericalValues singleD = variableTimeStep ? NumericalUtils.rescaleCDF(exitDistributions.get(0), greatestTimeStep) : exitDistributions.get(0);

			finalExitDistribution = singleD.getValues();
		}else {

			List<NumericalValues> exitD;
			if(variableTimeStep) {
				exitD = new LinkedList<>();
				for(NumericalValues exitDistro: exitDistributions) {
					exitD.add(NumericalUtils.rescaleCDF(exitDistro, greatestTimeStep));
				}

			}else {
				exitD = exitDistributions;
			}

			for (int t = 0; t < finalExitDistribution.length; t++) {
				double product = 1;
				for(NumericalValues exitDistro: exitD) {
					product*= (1 - exitDistro.getValues()[t]);
				}
				finalExitDistribution[t] = 1 - product;
			}
		}
		shiftedExitDistributions.put(parentRegion, new NumericalValues(finalExitDistribution, greatestTimeStep));

		double[] transientProbs = NumericalUtils.rescaleCDF(transientNumerical, greatestTimeStep).getValues();

		//6- Evaluate the mean sojourn time
		double mean = 0;
		for (int t = 0; t < transientProbs.length; t++) {
			mean+= transientProbs[t] *  (1 - finalExitDistribution[t]) * greatestTimeStep ;
		} //
		mean = mean * reachingProbability;

		meanSojournTimes.put(state, mean);
	}
}
