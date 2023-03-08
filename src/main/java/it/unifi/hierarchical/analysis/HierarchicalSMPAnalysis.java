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
import it.unifi.hierarchical.utils.NumericalUtils;
import it.unifi.hierarchical.utils.StateUtils;
import org.oristool.models.gspn.chains.DTMC;
import org.oristool.models.gspn.chains.DTMCStationary;

import java.util.*;
import java.util.Map.Entry;

/**
 * Notes:
 * - we assume the embedded DTMC to be irreducible
 *
 */

public class HierarchicalSMPAnalysis {

	private static final double DTMC_STRUCTURE_ALLOWED_EPSILON = 0.00000001;

	public static NumericalValues cdf;

	private int CYCLE_UNROLLING;

	private HSMP model;
	private Map<Step, NumericalValues> sojournTimeDistributions;
	private Map<Region, NumericalValues> regionSojournTimeDistributions;
	private Map<Region, TransientAnalyzer> regionTransientProbabilities;
	private Map<Step, Double> meanSojournTimes;
	private Map<Step, Double> emcSolution;

	//for cycle unrolling
	private Map<Step, List<Step>> aliasStates;
	private Map<Region, List<Region>> aliasRegion;
	private boolean compositeCycles=false;

	public HierarchicalSMPAnalysis(HSMP model) {
		this(model,0);
	}

	public HierarchicalSMPAnalysis(HSMP model, int CYCLE) {
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

		boolean noExitInitials = true;
		Set<Step> offendingStateSet=new HashSet<>();
		noExitInitials=checkInitialsNoBorder(offendingStateSet);
		if(!noExitInitials) {
			System.out.println("Initial state/s composite with border exit found, for the moment not implemented, "
					+ "shortcut through introducing a fake initial state with duration 0");

			for(Step s: offendingStateSet) {
				System.out.println(s.getName());
			}

			return null;
		}

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
			for(Step s : aliasStates.keySet()) {
				Double res =0.;
				for(Step t: aliasStates.get(s)) {
					res+=result.get(t.getName());
				}
				result.put(s.getName(), res);
			}
		}

		return result;
	}


	private void identifyAndUnrollCycles() {
		Map<Region, Set<Step>> toDuplicateMap = new HashMap<>();

		CycleVisitor visitor = new CycleVisitor();

		model.getInitialState().accept(visitor);
		if(visitor.containsCompositeCycles()) {
			compositeCycles=true;
			toDuplicateMap.putAll(visitor.getMap());

			aliasStates = new HashMap<>();
			aliasRegion = new HashMap<>();

			for(Region r: toDuplicateMap.keySet()) {
				for(Step s: toDuplicateMap.get(r)) {
					if(s.isCycle()) {
						createStateCopy(s);
					}

				}
				for(Step s: toDuplicateMap.get(r)) {
					linkStates(s, false);
				}
			}
		}
	}

	//FIXME !isCycle considers only simples at the start
	private void linkStates(Step s, boolean inner) {

		System.out.println("linko "+s.getName()+" "+inner);

		if(!inner && !s.isCycle()) {
			if(s instanceof SimpleStep) {
				List<Step> nextStates = s.getNextStates();
				List<Double> branchP = s.getBranchingProbs();

				List<Step> newNext = new LinkedList<>();
				for(Step st : nextStates) {
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

		if(s instanceof SimpleStep) {
			for(int i=0; i<CYCLE_UNROLLING;i++) {
				int pp;

				if(!inner)
					pp= loop?i+1:i;
				else
					pp= i;

				SimpleStep curr= (SimpleStep) aliasStates.get(s).get(i);

				List<Step> nextStates = curr.getNextStates();
				List<Step> newStates = new LinkedList<>();
				List<Double> branchP = curr.getBranchingProbs();

				if(pp==CYCLE_UNROLLING) {
					nextStates= new LinkedList<>();
					branchP = new LinkedList<>();
				}else {

					for(int j=0; j<nextStates.size();j++) {

						List<Step> rr = aliasStates.get(nextStates.get(j));
						Step nn = rr.get(pp);

						newStates.add(nn);

					}
				}
				curr.setNextStates(newStates, branchP);
			}
		}else if(s instanceof CompositeStep) {

			for (Region region : ((CompositeStep) s).getRegions()) {
				Step init=region.getInitialState();
				List<Step> reach = StateUtils.getReachableStates(init);
				for(Step li: reach) {
					linkStates(li, true);
				}
			}

			for(int i=0; i<CYCLE_UNROLLING;i++) {
				CompositeStep curr= (CompositeStep) aliasStates.get(s).get(i);

				Step init;

				//reset (correct region's) initial state
				for (Region region : curr.getRegions()) {
					init=region.getInitialState();
					region.setInitialState(aliasStates.get(init).get(i));
				}

				int pp;
				if(!inner)
					pp= loop?i+1:i;
				else
					pp= i;

				if(!StateUtils.isCompositeWithBorderExit(s)) {
					List<Step> nextStates = curr.getNextStates();
					List<Step> newStates = new LinkedList<>();
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

					Map<Step, List<Step>> nextStatesMap = curr.getNextStatesConditional();
					Map<Step, List<Step>> nextStatesLinked = new HashMap<>();

					Map<Step, List<Double>> nextBranchMap = curr.getBranchingProbsConditional();
					Map<Step, List<Double>> nextBranchLinked = new HashMap<>();

					for(Step in: nextStatesMap.keySet()) {
						Step news= aliasStates.get(in).get(i);
						nextStatesLinked.put(news, new LinkedList<Step>());
						for(Step out: nextStatesMap.get(in)) {
							nextStatesLinked.get(news).add(aliasStates.get(out).get(pp));
						}
					}

					for(Step in: nextBranchMap.keySet()) {
						Step news= aliasStates.get(in).get(i);
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

	private void createCopyRegion(Step s) {

		Set<Step> visited = new HashSet<>();
		Stack<Step> toBeVisited = new Stack<>();
		toBeVisited.add(s);
		while(!toBeVisited.isEmpty()) {
			Step current = toBeVisited.pop();
			visited.add(current);

			if(StateUtils.isCompositeWithBorderExit(current)) {
				CompositeStep cState = (CompositeStep)current;

				for(Entry<Step, List<Step>> e:cState.getNextStatesConditional().entrySet()) {
					for (Step successor : e.getValue()) {
						if(visited.contains(successor) || toBeVisited.contains(successor))
							continue;
						toBeVisited.push(successor);
					}
				}
				//STANDARD CASE
			}else {
				for(Step successor:current.getNextStates()) {
					if(visited.contains(successor) || toBeVisited.contains(successor))
						continue;
					toBeVisited.push(successor);
				}
			}
		}

		for(Step v: visited) {
			createStateCopy(v);
		}
	}

	private void createStateCopy(Step s) {

		aliasStates.put(s, new LinkedList<Step>());

		if(s instanceof SimpleStep) {
			for(int i=0; i<CYCLE_UNROLLING;i++) {
				aliasStates.get(s).add(((SimpleStep) s).makeCopy(i));
			}
		}else if(s instanceof CompositeStep) {

			CompositeStep sC = (CompositeStep) s;

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

				aliasStates.get(s).add(((CompositeStep) s).makeCopy(i, listR));

			}


		}else if(s instanceof ExitState) {
			for(int i=0; i<CYCLE_UNROLLING;i++) {
				aliasStates.get(s).add(((ExitState) s).makeCopy(i));
			}
		}else if(s instanceof FinalLocation) {
			for(int i=0; i<CYCLE_UNROLLING;i++) {
				aliasStates.get(s).add(((FinalLocation) s).makeCopy(i));
			}
		}

	}


	private boolean checkInitialsNoBorder(Set<Step> offenderSet) {

		RegionVisitor visitor = new RegionVisitor();

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
		DTMC<Step> dtmc = buildEDTMC(timeStep);
		//3.2- Evaluate steady state considering only branching probabilities and neglecting sojourn times
		this.emcSolution = evaluateDTMCSteadyState(dtmc);
	}

	private DTMC<Step> buildEDTMC(double timeStep) {
		DTMC<Step> dtmc = DTMC.create();

		//Initial state
		dtmc.initialStates().add(model.getInitialState());
		dtmc.initialProbs().add(1.0);

		//transition probabilities
		Set<Step> visited = new HashSet<>();
		Stack<Step> toBeVisited = new Stack<>();
		toBeVisited.add(model.getInitialState());
		dtmc.probsGraph().addNode(model.getInitialState());
		while(!toBeVisited.isEmpty()) {
			Step current = toBeVisited.pop();
			visited.add(current);

			//CASE OF COMPOSITE STATE WITH EXIT STATES ON THE BORDER
			if(StateUtils.isCompositeWithBorderExit(current)) {
				CompositeStep cState = (CompositeStep)current;
				//Add missing children to the dtmc
				for(Entry<Step, List<Step>> e:cState.getNextStatesConditional().entrySet()) {
					for (Step successor : e.getValue()) {
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

				Map<Step, Double> fireFirstProbMap = new HashMap<>();

				int count=0;
				for (Region region : cState.getRegions()) {
					if(region.getType()!=RegionType.NEVER) {
						Step endState = StateUtils.findEndState(region);
						fireFirstProbMap.put(endState, fireFirstProb.get(count));
						count++;
					}
				}
				//Add edges
				for(Step exitState: cState.getNextStatesConditional().keySet()) {
					for(int b = 0; b < cState.getBranchingProbsConditional().get(exitState).size(); b++) {
						double prob = cState.getBranchingProbsConditional().get(exitState).get(b) * fireFirstProbMap.get(exitState);
						addEdgeValue(dtmc, current, cState.getNextStatesConditional().get(exitState).get(b), prob);
					}
				}
				//STANDARD CASE
			}else {
				//Add missing children to the dtmc
				for(Step successor:current.getNextStates()) {
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
	private static void addEdgeValue(DTMC<Step> dtmc, Step from, Step to, double prob) {
		Optional<Double> old = dtmc.probsGraph().edgeValue(from, to);
		double newProb = prob;
		if(old.isPresent()) {
			newProb+=old.get().doubleValue();
		}
		dtmc.probsGraph().putEdgeValue(from, to, newProb);
	}

	private static Map<Step, Double> evaluateDTMCSteadyState(DTMC<Step> dtmc) {
		DTMCStationary<Step> DTMCss = DTMCStationary.<Step>builder().epsilon(DTMC_STRUCTURE_ALLOWED_EPSILON).build();
		return DTMCss.apply(dtmc.probsGraph());
	}


	// FIXME: Rename this method to evaluateSteadyState
	private Map<String, Double> evalueteSS() {
		//4.1- At higher level use the standard solution method for SS of an SMP
		Map<String, Double> ss = new HashMap<>();
		double denominator = 0.0;
		for (Step higherLevelState : emcSolution.keySet()) {
			denominator += meanSojournTimes.get(higherLevelState) * emcSolution.get(higherLevelState);
		}

		for (Step higherLevelState : emcSolution.keySet()) {


			double numerator = meanSojournTimes.get(higherLevelState)* emcSolution.get(higherLevelState);
			ss.put(higherLevelState.getName(), numerator / denominator);
		}


		//4.2- At lower level recursively go down:
		//The SS of a sub-state in a region can be obtained by multiplying the steady-state probability of the surrounding composite state with the
		//fraction of mean sojourn time in the sub-state and the surrounding composite state
		for (Step higherLevelState : emcSolution.keySet()) {
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
