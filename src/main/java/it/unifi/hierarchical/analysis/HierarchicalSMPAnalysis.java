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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;

import org.oristool.models.gspn.chains.DTMC;
import org.oristool.models.gspn.chains.DTMCStationary;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.utils.NumericalUtils;
import it.unifi.hierarchical.utils.StateUtils;


/**
 * Notes: 
 * - we assume the embedded DTMC to be irreducible 
 * @author marco e francesco
 *
 */
public class HierarchicalSMPAnalysis {

	private static final double DTMC_STRUCTURE_ALLOWED_EPSILON = 0.00000001;
	
	public static NumericalValues cdf;

	private int CYCLE_UNROLLING;

	private HierarchicalSMP model;
	private Map<State, NumericalValues> sojournTimeDistributions;
	private Map<Region, NumericalValues> regionSojournTimeDistributions;
	private Map<Region, TransientAnalyzer> regionTransientProbabilities;
	private Map<State, Double> meanSojournTimes;
	private Map<State, Double> emcSolution;

	//for cycle unrolling
	private Map<State, List<State>> aliasStates;
	private Map<Region, List<Region>> aliasRegion;
	private boolean compositeCycles=false;	

	public HierarchicalSMPAnalysis(HierarchicalSMP model) {
		this(model,0);
	}

	public HierarchicalSMPAnalysis(HierarchicalSMP model, int CYCLE) {
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


		//0.1 - unrolling all cycles forcibly: use a fixed number of unrollings, 
		//TODO add treatment to specify a confidence and identify correct number of unrolls
		//TODO add correct treatment if cycles are present in multiple levels
		//TODO add correct treatment if multiple cycles connect the same nodes 
		//TODO add correct treatment for cycles in regions containing nodes not in the cycle
		//qui tratto solo il caso di cicli che occupano completamente una neverending reg
		// e composite che contengono solo stati simple
		if(CYCLE_UNROLLING!=0)
			identifyAndUnrollCycles();
		
		long time;
		Date d1 = new Date();
	
		//		System.out.println("Evaluate Sojourn Time Distributions");
		//1- Sojourn times distribution
		evaluateSojournTimeDistributions(timeStep, timeLimit);

		Date d2 = new Date();
		
		time = d2.getTime() - d1.getTime();
		System.out.println(time+ "  sojournXXX");
		
		//		System.out.println("Evaluate Mean Sojourn Times");
		//2- Mean sojourn times 
		evaluateMeanSojournTimes(timeStep, timeLimit);

		Date d3 = new Date();
		
		time = d3.getTime() - d2.getTime();
		System.out.println(time+ "  meanXXX");
		
		//		System.out.println("Solve Embedded");
		//3- Solve EMC
		solveEmbeddedDTMC(timeStep);

		Date d4 = new Date();
		
		time = d4.getTime() - d3.getTime();
		System.out.println(time+ "  solveDTMCXXX");
		
		
		//		System.out.println("Evaluate Steady State");
		//4- steady state (Use solution of 2 and 3 to evaluate steady)
		Map<String, Double> result = evalueteSS();    

		if(compositeCycles) {
			for(State s : aliasStates.keySet()) {
				Double res =0.;
				for(State t: aliasStates.get(s)) {
					res+=result.get(t.getName());
				}
				result.put(s.getName(), res);
			}
		}

		return result;
	}


	private void identifyAndUnrollCycles() {
		Map<Region, Set<State>> toDuplicateMap = new HashMap<>();

		CycleVisitor visitor = new CycleVisitor();

		model.getInitialState().accept(visitor);
		if(visitor.containsCompositeCycles()) {
			compositeCycles=true;
			toDuplicateMap.putAll(visitor.getMap());

			aliasStates = new HashMap<>();
			aliasRegion = new HashMap<>();

			for(Region r: toDuplicateMap.keySet()) {
				for(State s: toDuplicateMap.get(r)) {
					if(s.isCycle()) {
						createStateCopy(s);
					}

				}
				for(State s: toDuplicateMap.get(r)) {
					linkStates(s, false);
				}


			}
		}
	}


	//FIXME !isCycle considers only simples at the start
	private void linkStates(State s, boolean inner) {

		System.out.println("linko "+s.getName()+" "+inner);

		if(!inner && !s.isCycle()) {
			if(s instanceof SimpleState) {
				List<State> nextStates = s.getNextStates();
				List<Double> branchP = s.getBranchingProbs();

				List<State> newNext = new LinkedList<>();
				for(State st : nextStates) {
					if(st.isCycle()) {
						newNext.add(aliasStates.get(st).get(0));
					}else {
						newNext.add(st);
					}
				}
				s.setNextStates(newNext, branchP);

			}//else if composite / compositeOnBorder
			return;
		}


		boolean loop = s.isLooping();

		if(s instanceof SimpleState) {
			for(int i=0; i<CYCLE_UNROLLING;i++) {
				int pp;

				if(!inner)
					pp= loop?i+1:i;
				else
					pp= i;

				SimpleState curr= (SimpleState) aliasStates.get(s).get(i);

				List<State> nextStates = curr.getNextStates();
				List<State> newStates = new LinkedList<>();
				List<Double> branchP = curr.getBranchingProbs();

				if(pp==CYCLE_UNROLLING) {
					nextStates= new LinkedList<>();
					branchP = new LinkedList<>();
				}else {

					for(int j=0; j<nextStates.size();j++) {

						List<State> rr = aliasStates.get(nextStates.get(j));
						State nn = rr.get(pp);

						newStates.add(nn);

					}
				}
				curr.setNextStates(newStates, branchP);
			}
		}else if(s instanceof CompositeState) {

			for (Region region : ((CompositeState) s).getRegions()) {
				State init=region.getInitialState();
				List<State> reach = StateUtils.getReachableStates(init);
				for(State li: reach) {
					linkStates(li, true);
				}
			}


			for(int i=0; i<CYCLE_UNROLLING;i++) {
				CompositeState curr= (CompositeState) aliasStates.get(s).get(i);

				State init;

				//reset (correct region's) initial state
				for (Region region : curr.getRegions()) {
					init=region.getInitialState();
					region.setInitialState(aliasStates.get(init).get(i));					
				}

				//linka il composite e poi linka gli stati interni
				int pp;
				if(!inner)
					pp= loop?i+1:i;
				else
					pp= i;

				if(!StateUtils.isCompositeWithBorderExit(s)) {
					List<State> nextStates = curr.getNextStates();
					List<State> newStates = new LinkedList<>();
					List<Double> branchP = curr.getBranchingProbs();

					if(pp==CYCLE_UNROLLING) {
						nextStates= new LinkedList<>();
						branchP = new LinkedList<>();
					}else {

						for(int j=0; j<nextStates.size();j++) {

							newStates.add(aliasStates.get(nextStates.get(j)).get(pp));

						}
					}
					curr.setNextStates(newStates, branchP);

				}else {

					Map<State, List<State>> nextStatesMap = curr.getNextStatesConditional();
					Map<State, List<State>> nextStatesLinked = new HashMap<>();
					
					Map<State, List<Double>> nextBranchMap = curr.getBranchingProbsConditional();
					Map<State, List<Double>> nextBranchLinked = new HashMap<>();

					for(State in: nextStatesMap.keySet()) {
						State news= aliasStates.get(in).get(i);
						nextStatesLinked.put(news, new LinkedList<State>());
						for(State out: nextStatesMap.get(in)) {
							nextStatesLinked.get(news).add(aliasStates.get(out).get(pp));
						}
					}
					
					for(State in: nextBranchMap.keySet()) {
						State news= aliasStates.get(in).get(i);
						nextBranchLinked.put(news, new LinkedList<Double>());
						for(Double out: nextBranchMap.get(in)) {
							nextBranchLinked.get(news).add(out);
						}
					}
					
					curr.setNextStatesConditional(nextStatesLinked);
					curr.setBranchingProbsConditional(nextBranchLinked);
					
				}
			}

		}

	}



	private void createCopyRegion(State s) {

		Set<State> visited = new HashSet<>();
		Stack<State> toBeVisited = new Stack<>();
		toBeVisited.add(s);
		while(!toBeVisited.isEmpty()) {
			State current = toBeVisited.pop();
			visited.add(current);

			if(StateUtils.isCompositeWithBorderExit(current)) {
				CompositeState cState = (CompositeState)current;

				for(Entry<State, List<State>> e:cState.getNextStatesConditional().entrySet()) {
					for (State successor : e.getValue()) {
						if(visited.contains(successor) || toBeVisited.contains(successor))
							continue;
						toBeVisited.push(successor);    
					}
				}
				//STANDARD CASE
			}else {
				for(State successor:current.getNextStates()) {
					if(visited.contains(successor) || toBeVisited.contains(successor))
						continue;
					toBeVisited.push(successor);
				}
			}
		}

		for(State v: visited) {
			createStateCopy(v);
		}
	}


	private void createStateCopy(State s) {

		aliasStates.put(s, new LinkedList<State>());

		if(s instanceof SimpleState) {
			for(int i=0; i<CYCLE_UNROLLING;i++) {
				aliasStates.get(s).add(((SimpleState) s).makeCopy(i));
			}
		}else if(s instanceof CompositeState) {

			CompositeState sC = (CompositeState) s;

			for(Region r: sC.getRegions()) {
				aliasRegion.put(r, new LinkedList<>());
				createCopyRegion(r.getInitialState());
			}

			for(int i=0; i<CYCLE_UNROLLING;i++) {
				List<Region> listR = new LinkedList<>();
				for(Region r: sC.getRegions()) {
					Region copy=r.makeCopy();
					listR.add(copy);
					aliasRegion.get(r).add(copy);
				}

				aliasStates.get(s).add(((CompositeState) s).makeCopy(i, listR));

			}


		}else if(s instanceof ExitState) {
			for(int i=0; i<CYCLE_UNROLLING;i++) {
				aliasStates.get(s).add(((ExitState) s).makeCopy(i));
			}	
		}else if(s instanceof FinalState) {
			for(int i=0; i<CYCLE_UNROLLING;i++) {
				aliasStates.get(s).add(((FinalState) s).makeCopy(i));
			}	
		}

	}


	private boolean checkInitialsNoBorder(Set<State> offenderSet) {

		BorderExitInitialVisitor visitor = new BorderExitInitialVisitor();


		model.getInitialState().accept(visitor);
		offenderSet.addAll(visitor.getOffenderSet());

		//the only border state accepted in the initial position is the initialState of the toplevel 
		return visitor.isModelCorrect();
	}


	private void evaluateSojournTimeDistributions(double timeStep, double timeLimit) {
		SojournTimeEvaluatorVisitor visitor = new SojournTimeEvaluatorVisitor(timeStep, timeLimit);
		model.getInitialState().accept(visitor);
		this.sojournTimeDistributions = visitor.getSojournTimeDistributions();
		this.regionSojournTimeDistributions = visitor.getRegionSojournTimeDistributions();
		this.regionTransientProbabilities = visitor.getRegionTransientProbabilities();
		
		HierarchicalSMPAnalysis.cdf=sojournTimeDistributions.get(model.getInitialState());
	}


	private void evaluateMeanSojournTimes(double timeStep, double timeLimit) {

		boolean variableTimeStep= (timeStep<0.0);


		MeanSojournTimeEvaluatorVisitor visitor = new MeanSojournTimeEvaluatorVisitor(model.getInitialState(), sojournTimeDistributions, regionSojournTimeDistributions, regionTransientProbabilities, variableTimeStep, timeLimit);
		model.getInitialState().accept(visitor);
		this.meanSojournTimes = visitor.getMeanSojournTimes();




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



			SubstatesSteadyStateEvaluatorVisitor visitor = 
					new SubstatesSteadyStateEvaluatorVisitor(
							higherLevelState,
							ss.get(higherLevelState.getName()),
							meanSojournTimes.get(higherLevelState)
							, meanSojournTimes);
			higherLevelState.accept(visitor);
			ss.putAll(visitor.getSubStateSSProbs());
		}

		return ss;
	}

}