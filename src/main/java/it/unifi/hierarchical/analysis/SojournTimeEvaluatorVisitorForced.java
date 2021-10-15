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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oristool.math.OmegaBigDecimal;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.visitor.StateVisitor;
import it.unifi.hierarchical.utils.NumericalUtils;
import it.unifi.hierarchical.utils.StateUtils;

public class SojournTimeEvaluatorVisitorForced implements StateVisitor{

	private Map<State, NumericalValues> sojournTimeDistributions;
	private Map<Region, NumericalValues> regionSojournTimeDistributions;
	private Map<Region, TransientAnalyzer> regionTransientProbabilities;
	private Set<State> evaluated;
	private double timeStep;
	private double timeLimit;

	private int CYCLE;

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
	public void visit(SimpleState state) {

		evaluated.add(state);
		//Evaluate its sojourn time distribution

		double step;

		if(timeStep<0.0) {
			step= state.getTimeStep();
		}else {
			step= timeStep;
		}


		double[] values;

		double up = state.getUpperBound();
		if(up>0.) {
		
			if(state.getDepth()!=0) {
				System.out.println("ERRORE ARBITRARY PRECISION NOT IN TOPLEVEL!! - SojournTimeEvaluatorForced");
				return;
			}
			
			step = up/50000;
			values = NumericalUtils.evaluateFunction(state.getDensityFunction(), new OmegaBigDecimal(""+up), new BigDecimal(""+step));
			values = NumericalUtils.computeCDFFromPDF(values,  new BigDecimal(""+step));
	
		}else {
			values = NumericalUtils.evaluateFunction(state.getDensityFunction(), new OmegaBigDecimal(""+timeLimit), new BigDecimal(""+step));
			values = NumericalUtils.computeCDFFromPDF(values,  new BigDecimal(""+step));
		}



		sojournTimeDistributions.put(state, new NumericalValues(values, step));

		//Visit successors not yet visited
		List<State> successors = state.getNextStates();
		for (State successor : successors) {
			if(evaluated.contains(successor))
				continue;
			successor.accept(this);
		}
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void visit(CompositeState state) {

		evaluated.add(state);
		//Evaluate children sojourn time distribution
		//In case of Never simply study ignoring parent's peculiarity
		List<Region> regions = state.getRegions();
		for (Region region : regions) {
			region.getInitialState().accept(this);
		}

		//Evaluate regions distribution
		Map<Region, NumericalValues> mapSojournTimeDistributions = new HashMap<>();

		//if Region is Never put a null in mapSojournTimeDistribution
		for (Region region : regions) {
			NumericalValues regionSojournTimeDistribution = evaluateRegionSojournTime(region);
			if(regionSojournTimeDistribution!= null)
				mapSojournTimeDistributions.put(region, regionSojournTimeDistribution);
		}

		//Evaluate composite state distribution
		RegionType type=null;
		for(Region r: regions) {
			if (r.getType()!=RegionType.NEVER) {
				type=r.getType();
				break;
			}
		}

		NumericalValues sojournTimeDistribution = null;
		//non c'� mai il caso never
		switch (type) {
		case EXIT:
			if(timeStep<0.0) {
				sojournTimeDistribution = NumericalUtils.minCDFvar(mapSojournTimeDistributions.values(), state.getTimeStep());
			}else {
				sojournTimeDistribution = NumericalUtils.minCDF(mapSojournTimeDistributions.values());
			}
			break;
		case FINAL:
			if(timeStep<0.0) {
				sojournTimeDistribution = NumericalUtils.maxCDFvar(mapSojournTimeDistributions.values(), state.getTimeStep());
			}else {
				sojournTimeDistribution = NumericalUtils.maxCDF(mapSojournTimeDistributions.values());
			}
			break;
		}

		//		System.out.println("composite");
		//		System.out.println(Arrays.toString(sojournTimeDistribution.getValues()));
		//		

		sojournTimeDistributions.put(state, sojournTimeDistribution);
		//Visit successors not yet visited
		if(StateUtils.isCompositeWithBorderExit(state)) {

			CompositeState cState = state;
			for(State exitState : cState.getNextStatesConditional().keySet()) {
				List<State> successors = cState.getNextStatesConditional().get(exitState);
				for (State successor : successors) {
					if(evaluated.contains(successor))
						continue;
					successor.accept(this);
				}    
			}
		}else {
			List<State> successors = state.getNextStates();
			for (State successor : successors) {
				if(evaluated.contains(successor))
					continue;
				successor.accept(this);
			}    
		}
	}

	private NumericalValues evaluateRegionSojournTime(Region region) {

		RegionType type= region.getType();

		State initialState = region.getInitialState();
		List<State> smpStates = StateUtils.getReachableStates(region.getInitialState());

		boolean variableSteps;
		double time;
		if(timeStep<0) {
			time=region.getTimeStep();
			variableSteps=true;
		}else {
			time=timeStep;
			variableSteps=false;
		}

		if(type==RegionType.NEVER) {

			regionSojournTimeDistributions.put(region, null);

			cycleTransientList= new SMPAnalyzerWithBorderExitStates(region.getInitialState(), sojournTimeDistributions, regionSojournTimeDistributions, timeLimit, time, null, variableSteps, CYCLE);


			regionTransientProbabilities.put(region, null);




			return null;
		}




		State endState = StateUtils.findEndState(smpStates);

		//		System.out.println("SojournTimeDistributions -> Analyzer");

		TransientAnalyzer analyzer = new SMPAnalyzerWithBorderExitStates(region.getInitialState(), sojournTimeDistributions, regionSojournTimeDistributions, timeLimit, time, variableSteps);

		//REMARK ottiene prob di passare da init a end in un certo tempo, richiede che i due siano stati presenti in analyzer, quindi non borderExit
		NumericalValues sojournTimeDistribution = analyzer.getProbsFromTo(initialState, endState);


		//FIXME TRANSIENTE RICHIEDE UNLOOPING, CONOSCENZA REGIONI VICINE / FORZA BRUTA UN CERTO NUM
		regionSojournTimeDistributions.put(region, sojournTimeDistribution);
		regionTransientProbabilities.put(region, analyzer);
		return sojournTimeDistribution;
	}

	@Override
	public void visit(FinalState state) {


		evaluated.add(state);
		sojournTimeDistributions.put(state, null);
	}

	@Override
	public void visit(ExitState state) {

		evaluated.add(state);
		sojournTimeDistributions.put(state, null);
	}

	public Map<State, NumericalValues> getSojournTimeDistributions() {
		return sojournTimeDistributions;
	}

	public Map<Region, NumericalValues> getRegionSojournTimeDistributions() {
		return regionSojournTimeDistributions;
	}

	public Map<Region, TransientAnalyzer> getRegionTransientProbabilities(){
		return regionTransientProbabilities;
	}

}
