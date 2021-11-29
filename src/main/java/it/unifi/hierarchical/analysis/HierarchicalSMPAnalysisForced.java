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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;

import org.oristool.models.gspn.chains.DTMC;
import org.oristool.models.gspn.chains.DTMCStationary;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.utils.NumericalUtils;
import it.unifi.hierarchical.utils.StateUtils;

//FIXME: Can this class be removed and its methods integrated with those of class HierarchicalSMPAnalysis?


/**
 * Notes: 
 * - we assume the embedded DTMC to be irreducible 
 * @author marco e francesco
 *
 */
public class HierarchicalSMPAnalysisForced {

	private static final double DTMC_STRUCTURE_ALLOWED_EPSILON = 0.00000001;

	private int CYCLE_UNROLLING;

	private HierarchicalSMP model;
	private Map<State, NumericalValues> sojournTimeDistributions;
	private Map<Region, NumericalValues> regionSojournTimeDistributions;
	private Map<Region, TransientAnalyzer> regionTransientProbabilities;
	private Map<State, Double> meanSojournTimes;
	private Map<State, Double> emcSolution;

	private SMPAnalyzerWithBorderExitStates cycleTransientList;
	
	private List<Double> meanSojournVmAvailableAtCycle;  
	private Map<State, List<Double>> meanSojournInnerAtCycle;

	public HierarchicalSMPAnalysisForced(HierarchicalSMP model) {
		this(model,0);
	}

	public HierarchicalSMPAnalysisForced(HierarchicalSMP model, int CYCLE) {
		this.model = model;
		this.CYCLE_UNROLLING=CYCLE;
	}

	/**
	 * @param timeStep  discretization step for the numerical passes, -1.0 is used to operate on different timeStep per state and regions 
	 * @param timeLimit longest meaningful interval of time, in a state or region, 
	 * over which the calculation must be executed, in case of unbounded distributions a truncation point must be 
	 * identified in an educated way
	 */
	public Map<String, Double> evaluateSteadyState(double timeStep, double timeLimit) {

		//0 - checking model does not contains exits on borders of initial states
		boolean noExitInitials = true;
		Set<State> offendingStateSet=new HashSet<>();
		noExitInitials=checkInitialsNoBorder(offendingStateSet);
		if(!noExitInitials) {
			System.out.println("Initial state/s composite with border exit found, for the moment not implemented, "
					+ "shortcut through introducing a fake initial state with duration 0");

			for(State s: offendingStateSet) {
				System.out.println(s.getName());
			}

			return null;
		}


		//		System.out.println("Evaluate Sojourn Time Distributions");
		//1- Sojourn times distribution
		evaluateSojournTimeDistributions(timeStep, timeLimit);


		//		System.out.println("Evaluate Mean Sojourn Times");
		//2- Mean sojourn times 
		
		System.out.println("in mean");
		evaluateMeanSojournTimes(timeStep, timeLimit);


		//		System.out.println("Solve Embedded");
		//3- Solve EMC
		solveEmbeddedDTMC(timeStep);

		//		System.out.println("Evaluate Steady State");
		//4- steady state (Use solution of 2 and 3 to evaluate steady)
		Map<String, Double> result = evalueteSS();    

		return result;
	}

	private boolean checkInitialsNoBorder(Set<State> offenderSet) {

		BorderExitInitialVisitor visitor = new BorderExitInitialVisitor();


		model.getInitialState().accept(visitor);
		offenderSet.addAll(visitor.getOffenderSet());

		//the only border state accepted in the initial position is the initialState of the toplevel 
		return visitor.isModelCorrect();
	}
	
	private void evaluateSojournTimeDistributions(double timeStep, double timeLimit) {
		SojournTimeEvaluatorVisitorForced visitor = new SojournTimeEvaluatorVisitorForced(timeStep, timeLimit, CYCLE_UNROLLING);
		model.getInitialState().accept(visitor);
		this.sojournTimeDistributions = visitor.getSojournTimeDistributions();
		this.regionSojournTimeDistributions = visitor.getRegionSojournTimeDistributions();
		this.regionTransientProbabilities = visitor.getRegionTransientProbabilities();
		this.cycleTransientList = visitor.getCycleTransientList();
		
	}

	private void evaluateMeanSojournTimes(double timeStep, double timeLimit) {

		boolean variableTimeStep= (timeStep<0.0);


		MeanSojournTimeEvaluatorVisitorForced visitor = new MeanSojournTimeEvaluatorVisitorForced(model.getInitialState(), sojournTimeDistributions, regionSojournTimeDistributions, regionTransientProbabilities, variableTimeStep, timeLimit, cycleTransientList, CYCLE_UNROLLING);
		model.getInitialState().accept(visitor);
		this.meanSojournTimes = visitor.getMeanSojournTimes();
		this.meanSojournVmAvailableAtCycle = visitor.getMeanSojournVmAvailableAtCycle();
		this.meanSojournInnerAtCycle = visitor.getMeanSojournInnerAtCycle();
	}

	private void solveEmbeddedDTMC(double timeStep){
		//3.1- Build DTMC
		DTMC<State> dtmc = buildEDTMC(timeStep);
		//3.2- Evaluate steady state considering only branching probabilities and neglecting sojourn times
		this.emcSolution = evaluateDTMCSteadyState(dtmc);
	}

	private DTMC<State> buildEDTMC(double timeStep) {
		DTMC<State> dtmc = DTMC.create();

		//Initial state
		dtmc.initialStates().add(model.getInitialState());
		dtmc.initialProbs().add(1.0);

		//transition probabilities
		Set<State> visited = new HashSet<>();
		Stack<State> toBeVisited = new Stack<>();
		toBeVisited.add(model.getInitialState());
		dtmc.probsGraph().addNode(model.getInitialState());
		while(!toBeVisited.isEmpty()) {
			State current = toBeVisited.pop();
			visited.add(current);

			//CASE OF COMPOSITE STATE WITH EXIT STATES ON THE BORDER
			if(StateUtils.isCompositeWithBorderExit(current)) {
				CompositeState cState = (CompositeState)current;
				//Add missing children to the dtmc
				for(Entry<State, List<State>> e:cState.getNextStatesConditional().entrySet()) {
					for (State successor : e.getValue()) {
						if(visited.contains(successor) || toBeVisited.contains(successor))
							continue;
						dtmc.probsGraph().addNode(successor);
						toBeVisited.push(successor);    
					}
				}
				//Evaluate successors probabilities
				List<NumericalValues> distributions = new ArrayList<>();
				for (Region region : cState.getRegions()) {
					if(region.getType()!=RegionType.NEVER)
						distributions.add(regionSojournTimeDistributions.get(region));
				}

				double time;
				if(timeStep<0) {
					time = cState.getTimeStep();
				}else {
					time=-1.0;
				}
				List<Double> fireFirstProb = NumericalUtils.evaluateFireFirstProbabilities(distributions, time);

				Map<State, Double> fireFirstProbMap = new HashMap<>();

				int count=0;
				for (Region region : cState.getRegions()) {
					if(region.getType()!=RegionType.NEVER) {
						State endState = StateUtils.findEndState(region);
						fireFirstProbMap.put(endState, fireFirstProb.get(count));
						count++;
					}
				}
				//Add edges
				for(State exitState: cState.getNextStatesConditional().keySet()) {
					for(int b = 0; b < cState.getBranchingProbsConditional().get(exitState).size(); b++) {
						double prob = cState.getBranchingProbsConditional().get(exitState).get(b) * fireFirstProbMap.get(exitState);
						addEdgeValue(dtmc, current, cState.getNextStatesConditional().get(exitState).get(b), prob);
					}
				}
				//STANDARD CASE
			}else {
				//Add missing children to the dtmc
				for(State successor:current.getNextStates()) {
					if(visited.contains(successor) || toBeVisited.contains(successor))
						continue;
					dtmc.probsGraph().addNode(successor);
					toBeVisited.push(successor);
				}
				//Add edges
				for(int i = 0; i <current.getBranchingProbs().size(); i++) {
					addEdgeValue(dtmc, current, current.getNextStates().get(i), current.getBranchingProbs().get(i));
				}                
			}
		}
		return dtmc;
	}

	/**
	 * If it not exists, create an edge in the DTMC between from and to with specified value.
	 * If the edge already exists sums values
	 */
	private static void addEdgeValue(DTMC<State> dtmc, State from, State to, double prob) {
		Optional<Double> old = dtmc.probsGraph().edgeValue(from, to);
		double newProb = prob;
		if(old.isPresent()) {
			newProb+=old.get().doubleValue();
		}
		dtmc.probsGraph().putEdgeValue(from, to, newProb);
	}

	private static Map<State, Double> evaluateDTMCSteadyState(DTMC<State> dtmc) {
		DTMCStationary<State> DTMCss = DTMCStationary.<State>builder().epsilon(DTMC_STRUCTURE_ALLOWED_EPSILON).build();
		return DTMCss.apply(dtmc.probsGraph());
	}


	private Map<String, Double> evalueteSS() {        
		//4.1- At higher level use the standard solution method for SS of an SMP
		Map<String, Double> ss = new HashMap<>();
		double denominator = 0.0;
		for (State higherLevelState : emcSolution.keySet()) {
			denominator += meanSojournTimes.get(higherLevelState) * emcSolution.get(higherLevelState);
		}

		for (State higherLevelState : emcSolution.keySet()) {


			double numerator = meanSojournTimes.get(higherLevelState)* emcSolution.get(higherLevelState);
			ss.put(higherLevelState.getName(), numerator / denominator);
		}


		//4.2- At lower level recursively go down:
		//The SS of a sub-state in a region can be obtained by multiplying the steady-state probability of the surrounding composite state with the
		//fraction of mean sojourn time in the sub-state and the surrounding composite state
		for (State higherLevelState : emcSolution.keySet()) {
			//			System.out.println("higher "+higherLevelState.getName());

			SubstatesSteadyStateEvaluatorVisitorForced visitor = 
					new SubstatesSteadyStateEvaluatorVisitorForced(
							higherLevelState,
							ss.get(higherLevelState.getName()),
							meanSojournTimes.get(higherLevelState)
							, meanSojournTimes,meanSojournVmAvailableAtCycle, meanSojournInnerAtCycle);
			higherLevelState.accept(visitor);
			ss.putAll(visitor.getSubStateSSProbs());
		}

		return ss;
	}
}
