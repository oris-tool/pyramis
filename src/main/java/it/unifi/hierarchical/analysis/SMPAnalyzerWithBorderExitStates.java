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

package it.unifi.hierarchical.analysis;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.oristool.math.OmegaBigDecimal;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.visitor.StateVisitor;
import it.unifi.hierarchical.utils.NumericalUtils;
import it.unifi.hierarchical.utils.StateUtils;

//FIXME: This class be removed and its methods integrated with those of class SMPAnalyzer.
/**
 * Extend the SMPAnalyzer so as to handle the case of composite states with exits on the border
 */
public class SMPAnalyzerWithBorderExitStates implements TransientAnalyzer{

	private Map<Region, NumericalValues> regionSojournTimeDistributions;
	private List<State> states;
	private List<State> stateWithExitConditioning;
	private Map<State, List<RegionState>> compositeStateToRegionStates;
	private SMPAnalyzer analyzer;
	private double timeLimit;
	private double timeStep;

	private int CYCLE;
	private SMPAnalyzerForCycle analyzerForCycle;

	private boolean variableTime;

	private State absorbingState;

	public SMPAnalyzerWithBorderExitStates(State initialState, Map<State, NumericalValues> sojournTimeDistributions, Map<Region, NumericalValues> regionSojournTimeDistributions, double timeLimit, double timeStep, boolean variable) {
		this(initialState, sojournTimeDistributions, regionSojournTimeDistributions, timeLimit, timeStep, null, variable);
	}

	public SMPAnalyzerWithBorderExitStates(State initialState, Map<State, NumericalValues> sojournTimeDistributions, Map<Region, NumericalValues> regionSojournTimeDistributions, double timeLimit, double timeStep, State absorbingState, boolean variable) {
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

	public SMPAnalyzerWithBorderExitStates(State initialState, Map<State, NumericalValues> sojournTimeDistributions, Map<Region, NumericalValues> regionSojournTimeDistributions, double timeLimit, double timeStep, State absorbingState, boolean variable, int CYCLE) {
		this.regionSojournTimeDistributions = regionSojournTimeDistributions;
		this.timeLimit = timeLimit;
		this.timeStep = timeStep;
		this.absorbingState = absorbingState;

		this.CYCLE =CYCLE;

		this.variableTime = variable;

		//1- Find "normal" (i.e. not region) reachable states
		this.states = StateUtils.getReachableStates(initialState);

		//2- Create the state list with regionstates instead of CompositesWithExitOnBorder
		this.stateWithExitConditioning = getStateWithExitConditioning(states);

		//3-Add regionState to the sojournTimeDistributions map
		Map<State, NumericalValues> augmentedSojournTimeDistributions = new HashMap<>();
		for (State state : stateWithExitConditioning) {

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
	public NumericalValues getProbsFromTo(State from, State to) {

		//Aggregation not required
		if(!isCompositeWithBorderExit(to) || to.equals(absorbingState)) {
			return analyzer.getProbsFromTo(from, to);
		}

		//Aggregation required
		int steps = NumericalUtils.computeTickNumber(new OmegaBigDecimal(""+timeLimit), new BigDecimal(""+timeStep));
		List<RegionState> mappedState = compositeStateToRegionStates.get(to);
		double[] result = new double[steps];
		for (RegionState rState : mappedState) {
			NumericalValues probs = analyzer.getProbsFromTo(from, rState);
			for(int t=0; t<result.length; t++) {
				result[t]+= probs.getValues()[t];
			}
		}
		return new NumericalValues(result, timeStep);
	}

	public List<NumericalValues> getProbsFromToList(State from, State to) {

		List<NumericalValues> list = new LinkedList<NumericalValues>();

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
	public List<State> getStates() {
		return states;
	}

	/**
	 * For each composite state that has exits on border, create a dummy state for each region.
	 * For each state that has a composite state having exits on border, change the branching probs considering probability that a region is faster
	 * Note: if the composite state having exits on border, is the absorbing one, don't convert it
	 */
	private List<State> getStateWithExitConditioning(List<State> statesBeforeConditioning) {
		this.compositeStateToRegionStates = new HashMap<>();
		//1- Convert State to a set of RegionState where required 
		List<State> convertedStates = new ArrayList<>();
		for (State state : statesBeforeConditioning) {
			if(!state.equals(absorbingState) && isCompositeWithBorderExit(state)) { 
				List<RegionState> regionStates = new ArrayList<>();
				for(Region region: ((CompositeState)state).getRegions()){

					//each RegionState is identifiable and equivalent based on name parent and index of the region
					RegionState rState = new RegionState(region, ((CompositeState)state));
					convertedStates.add(rState);
					regionStates.add(rState);
				}
				compositeStateToRegionStates.put(state, regionStates);
			}else {
				convertedStates.add(state);    
			}            
		}
		//2- Change branching probabilities according to the preselection probability
		List<State> stateWithExitConditioningInner = new ArrayList<>();
		for (State state : convertedStates) {
			stateWithExitConditioningInner.add(handleExitConditioning(state));
		}
		return stateWithExitConditioningInner;
	}

	private State handleExitConditioning(State oldstate) {
		State newState = oldstate.makeCopy();
		List<Double> branchingProbs = new ArrayList<>();
		List<State> nextStates = new ArrayList<>();

		if(oldstate.equals(absorbingState)) {
			branchingProbs.add(1.0);
			nextStates.add(newState);
		}else {       

			for (int b = 0; b< oldstate.getNextStates().size(); b++) {

				//if the current state is a RegionState getNextStates 
				//and getBranchingProbs are correctly overridden

				State toState = oldstate.getNextStates().get(b);

				if(!toState.equals(absorbingState) && isCompositeWithBorderExit(toState)) {
					List<Region> regions = ((CompositeState)toState).getRegions();
					List<Double> probFaster = evaluateProbabilityFasterRegions(regions, (CompositeState)toState); 

					List<RegionState> regionStates = compositeStateToRegionStates.get(toState);

					for(int r=0; r < regions.size(); r++) {
						Double p = oldstate.getBranchingProbs().get(b) * probFaster.get(r);
						nextStates.add(regionStates.get(r));
						branchingProbs.add(p);
					}
				}else {
					nextStates.add(oldstate.getNextStates().get(b));
					branchingProbs.add(oldstate.getBranchingProbs().get(b));
				}
			}
		}
		newState.setNextStates(nextStates, branchingProbs);
		return newState;
	}

	//Evaluate a probability that one region finish first
	private List<Double> evaluateProbabilityFasterRegions(List<Region> regions, CompositeState parentState){
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

	private static boolean isCompositeWithBorderExit(State state) {
		return StateUtils.isCompositeWithBorderExit(state);
	}

	private class RegionState extends State{

		private Region region;
		private CompositeState state;
		private List<Region> competingRegions;

		protected RegionState(Region region, CompositeState state) {
			super(state.getName() + "-REGION-" + state.getRegions().indexOf(region), state.getDepth(), state.getTimeStep());
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
		public void accept(StateVisitor visitor) {
			throw new UnsupportedOperationException("Region state can't accept visitors");
		}

		@Override
		public List<State> getNextStates() {
			List<State> list= state.getNextStatesConditional().get(StateUtils.findEndState(region));
			return Collections.unmodifiableList(list);
		}

		@Override
		public List<Double> getBranchingProbs() {

			State s = StateUtils.findEndState(region);

			List<Double> list= state.getBranchingProbsConditional().get(s);

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
		public State makeCopy() {
			return new RegionState(region, state);
		} 
	}
}
