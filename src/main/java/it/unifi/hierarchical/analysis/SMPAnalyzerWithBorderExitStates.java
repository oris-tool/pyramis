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

//FIXME: This class may be removed and its methods integrated with those of class SMPAnalyzer.
/**
 * Extend the SMPAnalyzer so as to handle the case of composite states with exits on the border
 */
public class SMPAnalyzerWithBorderExitStates implements TransientAnalyzer{

	private final Map<Region, NumericalValues> regionSojournTimeDistributions;
	private final List<LogicalLocation> states;
	private Map<LogicalLocation, List<RegionState>> compositeStateToRegionStates;
	private SMPAnalyzer analyzer;
	private final double timeLimit;
	private final double timeStep;

	private final int CYCLE;
	private SMPAnalyzerForCycle analyzerForCycle;

	private final boolean variableTime;

	private final LogicalLocation absorbingState;

	public SMPAnalyzerWithBorderExitStates(LogicalLocation initialState, Map<LogicalLocation, NumericalValues> sojournTimeDistributions, Map<Region, NumericalValues> regionSojournTimeDistributions, double timeLimit, double timeStep, boolean variable) {
		this(initialState, sojournTimeDistributions, regionSojournTimeDistributions, timeLimit, timeStep, null, variable);
	}

	public SMPAnalyzerWithBorderExitStates(LogicalLocation initialState, Map<LogicalLocation, NumericalValues> sojournTimeDistributions, Map<Region, NumericalValues> regionSojournTimeDistributions, double timeLimit, double timeStep, LogicalLocation absorbingState, boolean variable) {
		this(initialState, sojournTimeDistributions, regionSojournTimeDistributions, timeLimit, timeStep, absorbingState, variable, 0);
	}
	/**
	 *
	 * @param initialState
	 * @param sojournTimeDistributions
	 * @param regionSojournTimeDistributions
	 * @param timeLimit
	 * @param timeStep
	 * @param absorbingState
	 * @param variable true when timeStep refers only to the current region,
	 * false when the same step is applied to all the model
	 */

	public SMPAnalyzerWithBorderExitStates(LogicalLocation initialState, Map<LogicalLocation, NumericalValues> sojournTimeDistributions, Map<Region, NumericalValues> regionSojournTimeDistributions, double timeLimit, double timeStep, LogicalLocation absorbingState, boolean variable, int CYCLE) {
		this.regionSojournTimeDistributions = regionSojournTimeDistributions;
		this.timeLimit = timeLimit;
		this.timeStep = timeStep;
		this.absorbingState = absorbingState;

		this.CYCLE =CYCLE;

		this.variableTime = variable;

		//1- Find "normal" (i.e. not region) reachable states
		this.states = StateUtils.getReachableStates(initialState);

		//2- Create the state list with regionstates instead of CompositesWithExitOnBorder
		List<LogicalLocation> stateWithExitConditioning = getStateWithExitConditioning(states);

		//3-Add regionState to the sojournTimeDistributions map
		Map<LogicalLocation, NumericalValues> augmentedSojournTimeDistributions = new HashMap<>();
		for (LogicalLocation state : stateWithExitConditioning) {

			NumericalValues stateDistrib;
			if(state instanceof RegionState) {

				//conditions distribution to fire first
				stateDistrib = computeRegionStateDistribution((RegionState) state);
			}else {
				stateDistrib = variableTime ? NumericalUtils.rescaleCDF(sojournTimeDistributions.get(state), timeStep) : sojournTimeDistributions.get(state) ;
			}

			augmentedSojournTimeDistributions.put(state, stateDistrib);
		}

		//        System.out.println("Solve SMP");
		//4- solve SMP
		//we pass the new region, with the rescaled CDFs, states to this SMPAnalyzer, NOT the original compositeWithBorder
		//so it does not know the originals (and so the initial, and ending, state cannot be with border since it contains only regionStates)
		if(CYCLE == 0) {
			this.analyzer = new SMPAnalyzer(stateWithExitConditioning, augmentedSojournTimeDistributions, timeLimit, timeStep, this.absorbingState);
		}else {
			//TODO FIXME qui considero solo caso del journal dove il ciclo inizia al secondo stato e non vi sono uscite
			this.analyzerForCycle = new SMPAnalyzerForCycle(stateWithExitConditioning, augmentedSojournTimeDistributions, timeLimit, timeStep, CYCLE);
		}
	}

	private NumericalValues computeRegionStateDistribution(RegionState state) {

		List<NumericalValues> vl= new LinkedList<>();
		NumericalValues fired = regionSojournTimeDistributions.get(state.getRegion());

		for(Region r: state.getCompetingRegions()) {
			vl.add(regionSojournTimeDistributions.get(r));
		}

		fired = variableTime ? NumericalUtils.rescaleCDF(fired, timeStep) : fired;

		fired = NumericalUtils.conditionDistributionToFire(fired,vl,timeLimit,timeStep,variableTime);

		return fired;
	}

	//the first state From is always the initial state of the region (so it is not ExitOnBorder)
	@Override
	public NumericalValues getTransientProbability(LogicalLocation from, LogicalLocation to) {

		//Aggregation not required
		if(!isCompositeWithBorderExit(to) || to.equals(absorbingState)) {
			return analyzer.getTransientProbability(from, to);
		}

		//Aggregation required
		int steps = NumericalUtils.computeTickNumber(new OmegaBigDecimal(""+timeLimit), new BigDecimal(""+timeStep));
		List<RegionState> mappedState = compositeStateToRegionStates.get(to);
		double[] result = new double[steps];
		for (RegionState rState : mappedState) {
			NumericalValues probs = analyzer.getTransientProbability(from, rState);
			for(int t=0; t<result.length; t++) {
				result[t]+= probs.getValues()[t];
			}
		}
		return new NumericalValues(result, timeStep);
	}

	public List<NumericalValues> getProbsFromToList(LogicalLocation from, LogicalLocation to) {

		List<NumericalValues> list = new LinkedList<>();

		for(int i=0;i<CYCLE;i++) {

			//Aggregation not required
			if(!isCompositeWithBorderExit(to) || to.equals(absorbingState)) {
				list.add(analyzerForCycle.getProbsFromTo(from, to, i));
			}else {

				//Aggregation required
				int steps = NumericalUtils.computeTickNumber(new OmegaBigDecimal(""+timeLimit), new BigDecimal(""+timeStep));
				List<RegionState> mappedState = compositeStateToRegionStates.get(to);
				double[] result = new double[steps];
				for (RegionState rState : mappedState) {
					NumericalValues probs = analyzerForCycle.getProbsFromTo(from, rState, i);
					for(int t=0; t<result.length; t++) {
						result[t]+= probs.getValues()[t];
					}
				}
				list.add(new NumericalValues(result, timeStep));
			}
		}
		return list;
	}

	@Override
	public double getTimeLimit() {
		return timeLimit;
	}

	@Override
	public double getTimeStep() {
		return timeStep;
	}

	@Override
	public List<LogicalLocation> getStates() {
		return states;
	}

	//FIXME manage composite steps of type FIRST with NEVERENDING regions
	/**
	 * For each composite state that has exits on border, create a dummy state for each region.
	 * For each state that has a composite state having exits on border, change the branching probs considering probability that a region is faster
	 * Note: if the composite state having exits on border, is the absorbing one, don't convert it
	 */
	private List<LogicalLocation> getStateWithExitConditioning(List<LogicalLocation> statesBeforeConditioning) {
		this.compositeStateToRegionStates = new HashMap<>();
		//1- Convert State to a set of RegionState where required
		List<LogicalLocation> convertedStates = new ArrayList<>();
		for (LogicalLocation state : statesBeforeConditioning) {
			if(!state.equals(absorbingState) && isCompositeWithBorderExit(state)) {
				List<RegionState> regionStates = new ArrayList<>();
				for(Region region: ((CompositeStep)state).getRegions()){

					//each RegionState is identifiable and equivalent based on name parent and index of the region
					RegionState rState = new RegionState(region, ((CompositeStep) state));
					convertedStates.add(rState);
					regionStates.add(rState);
				}
				compositeStateToRegionStates.put(state, regionStates);
			}else {
				convertedStates.add(state);
			}
		}
		//2- Change branching probabilities according to the preselection probability
		List<LogicalLocation> stateWithExitConditioningInner = new ArrayList<>();
		for (LogicalLocation state : convertedStates) {
			stateWithExitConditioningInner.add(handleExitConditioning(state));
		}
		return stateWithExitConditioningInner;
	}

	private LogicalLocation handleExitConditioning(LogicalLocation oldstate) {
		LogicalLocation newState = oldstate.makeCopy();
		List<Double> branchingProbs = new ArrayList<>();
		List<LogicalLocation> nextStates = new ArrayList<>();


		if(oldstate.equals(absorbingState)) {
			branchingProbs.add(1.0);
			nextStates.add(newState);
		}else {

			for (int b = 0; b< oldstate.getNextLocations().size(); b++) {

				//if the current state is a RegionState getNextStates
				//and getBranchingProbs are correctly overridden

				LogicalLocation toState = oldstate.getNextLocations().get(b);

				if(!toState.equals(absorbingState) && isCompositeWithBorderExit(toState)) {
					List<Region> regions = ((CompositeStep)toState).getRegions();
					List<Double> probFaster = evaluateProbabilityFasterRegions(regions, (CompositeStep)toState);

					List<RegionState> regionStates = compositeStateToRegionStates.get(toState);

					for(int r=0; r < regions.size(); r++) {
						Double p = oldstate.getBranchingProbabilities().get(b) * probFaster.get(r);
						nextStates.add(regionStates.get(r));
						branchingProbs.add(p);
					}
				}else {
					nextStates.add(oldstate.getNextLocations().get(b));
					branchingProbs.add(oldstate.getBranchingProbabilities().get(b));
				}
			}
		}
		if(!(oldstate instanceof FinalLocation)) {
			((Step) newState).setNextLocations(nextStates, branchingProbs);
		}
		return newState;
	}

	//Evaluate a probability that one region finish first
	private List<Double> evaluateProbabilityFasterRegions(List<Region> regions, CompositeStep parentState){
		List<NumericalValues> distributions = new ArrayList<>();
		for (Region region : regions) {
			distributions.add(regionSojournTimeDistributions.get(region));
		}

		double time;
		if(variableTime) {
			time = parentState.getTimeStep();
		}else {
			time=-1.0;
		}
		return NumericalUtils.evaluateFireFirstProbabilities(distributions, time);
	}

	private static boolean isCompositeWithBorderExit(LogicalLocation state) {
		return StateUtils.isCompositeWithBorderExit(state);
	}

	private static class RegionState extends Step {

		private final Region region;
		private final CompositeStep state;
		private final List<Region> competingRegions;

		protected RegionState(Region region, CompositeStep state) {
			super(state.getName() + "-REGION-" + state.getRegions().indexOf(region), state.getTimeStep());
			this.depth = state.getDepth();
			this.region = region;
			this.state = state;
			this.competingRegions= new LinkedList<>();
			for(Region r: state.getRegions()) {
				if(!r.equals(region)) {
					competingRegions.add(r);
				}
			}
		}

		@Override
		public void accept(LogicalLocationVisitor visitor) {
			throw new UnsupportedOperationException("Region state can't accept visitors");
		}

		public List<LogicalLocation> getNextStates() {
			List<LogicalLocation> list= state.getNextLocations(StateUtils.findEndState(region));
			return Collections.unmodifiableList(list);
		}

		public List<Double> getBranchingProbs() {

			LogicalLocation s = StateUtils.findEndState(region);

			List<Double> list= state.getBranchingProbabilities(s);

			return Collections.unmodifiableList(list);
		}

		//not needed
		@Override
		public int getDepth() {
			return state.getDepth();
		}

		//to not modify
		protected Region getRegion() {
			return region;
		}

		protected List<Region> getCompetingRegions(){
			return competingRegions;
		}

		@Override
		public LogicalLocation makeCopy() {
			return new RegionState(region, state);
		}

		@Override
		public List<LogicalLocation> getNextLocations() {
			List<LogicalLocation> list= state.getNextLocations(StateUtils.findEndState(region));
			return Collections.unmodifiableList(list);
		}

		@Override
		public List<Double> getBranchingProbabilities() {
			LogicalLocation s = StateUtils.findEndState(region);

			List<Double> list= state.getBranchingProbabilities(s);

			return Collections.unmodifiableList(list);
		}
	}
}

