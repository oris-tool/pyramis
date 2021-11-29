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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

//FIXME: Can this class be removed and its methods integrated with those of class MeanSojournTimeEvaluatorVisitor?

//2.1- Navigate on the higher level searching for all possible state
//2.2- For each one, evaluate the mean based on sojourn time distribution,
//     if composite,
public class MeanSojournTimeEvaluatorVisitorForced implements StateVisitor{

	private Map<State, NumericalValues> sojournTimeDistributions;
	private Map<Region, NumericalValues> regionSojournTimeDistributions;
	private Map<Region, TransientAnalyzer> regionTransientProbabilities;
	private Map<State, Region> parentRegions;
	private Map<Region, CompositeState> parentStates;
	private Map<State, Double> meanSojournTimes;
	private double timeLimit;
	private boolean variableTimeStep;

	private int CYCLE;

	private SMPAnalyzerWithBorderExitStates cycleTransientList;

	private List<Double> meanSojournVmAvailableAtCycle;
	private List<NumericalValues> arrivalsInVm;
	private Map<State, List<Double>> meanSojournInnerAtCycle;



	public List<Double> getMeanSojournVmAvailableAtCycle() {
		return meanSojournVmAvailableAtCycle;
	}

	public Map<State, List<Double>> getMeanSojournInnerAtCycle() {
		return meanSojournInnerAtCycle;
	}


	private Map<Region, NumericalValues> shiftedExitDistributions;//Given a target region, given the minimum exit distribution of parallel regions 

	/**
	 * 
	 * @param initialState
	 * @param sojournTimeDistributions
	 * @param regionSojournTimeDistributions
	 * @param regionTransientProbabilities
	 * @param variableTimeStep true if different timeSteps for regions and states are present
	 * @param timeLimit
	 */
	public MeanSojournTimeEvaluatorVisitorForced(State initialState, Map<State, NumericalValues> sojournTimeDistributions, Map<Region, NumericalValues> regionSojournTimeDistributions, 
			Map<Region, TransientAnalyzer> regionTransientProbabilities,boolean variableTimeStep, double timeLimit, SMPAnalyzerWithBorderExitStates cycleTransientList, int CYCLE) {
		this.sojournTimeDistributions = sojournTimeDistributions;
		this.regionSojournTimeDistributions = regionSojournTimeDistributions;
		this.regionTransientProbabilities = regionTransientProbabilities;
		ModelStructureAnalyzer modelStructure = new ModelStructureAnalyzer(initialState);
		this.parentRegions = modelStructure.getParentRegions();
		this.parentStates = modelStructure.getParentStates();
		this.meanSojournTimes = new HashMap<>();
		this.shiftedExitDistributions = new HashMap<>();
		this.timeLimit = timeLimit;

		this.CYCLE = CYCLE;
		this.cycleTransientList= cycleTransientList;
		
		this.meanSojournInnerAtCycle = new HashMap<>();
		this.meanSojournVmAvailableAtCycle = new LinkedList<>();

		this.variableTimeStep=variableTimeStep;
	}

	@Override
	public void visit(SimpleState state) {

		evaluateStateMeanSojournTime(state);
	}

	@Override
	public void visit(CompositeState state) {
		evaluateStateMeanSojournTime(state);
		for (Region region : state.getRegions()) {
			if(meanSojournTimes.containsKey(region.getInitialState()))
				continue;
			region.getInitialState().accept(this);
		}
	}

	@Override
	public void visit(FinalState state) {
		//Do nothing!
	}

	@Override
	public void visit(ExitState state) {
		//Do nothing!
	}

	private void evaluateStateMeanSojournTime(State state) {
	
		
		System.out.println("state " + state.getName() );
		if(state.getDepth() == 0) {//Top level sojourn time --> not affected by exit of any other region
			evaluateTopLevelStateSojournTime(state);
		}else { //Not top level sojourn time --> affected by exit in parallel regions
			evaluateLowerLevelStateSojournTime(state);    
		}

		//Evaluate mean sojourn time also for successor states if required
		if(StateUtils.isCompositeWithBorderExit(state)) {
			CompositeState cState = (CompositeState) state;
			for(State exitState : cState.getNextStatesConditional().keySet()) {
				List<State> successors = cState.getNextStatesConditional().get(exitState);
				for (State successor : successors) {
					if(meanSojournTimes.containsKey(successor))
						continue;
					successor.accept(this);
				}    
			}
		}else {
			for(State successor: state.getNextStates()) {
				System.out.println("current "+state.getName() +" succ "+ successor.getName() );
				if(meanSojournTimes.containsKey(successor))
					continue;
				successor.accept(this);
			}
		}
	}

	public Map<State, Double> getMeanSojournTimes() {
		return meanSojournTimes;
	}

	private void evaluateTopLevelStateSojournTime(State state) {
		NumericalValues sojournDistrubution = sojournTimeDistributions.get(state);

		double timeStep = sojournDistrubution.getStep();
		double mean = 0;
		for (int t = 0; t < sojournDistrubution.getValues().length; t++) {
			mean+= (1-sojournDistrubution.getValues()[t]) * timeStep;
		}
		meanSojournTimes.put(state, mean);
	}


	//INFO: I calcoli si basano sul fatto che il ciclo � tale da non permettere mai l'uscita!!
	// Reaching prob nella never � sempre 1!!
	private void evaluateLowerLevelStateSojournTime(State state) {


		//1- Get region transient probabilities. Since it is not a top level state, it must belong to a region
		Region parentRegion = parentRegions.get(state);


		boolean directNever = (parentRegion.getType()==RegionType.NEVER);
		boolean insideVm = false;

		if(state.getDepth() >1) {
			insideVm = (parentRegions.get(parentStates.get(parentRegion)).getType()==RegionType.NEVER);
		}

		//FIXME al momento per costruzione se depth > 1 insideVm == true
		if(insideVm){
			meanSojournInnerAtCycle.put(state, new LinkedList<Double>());
		}




		//			prima non mi preoccupavo di nulla potevo direttamente 
		//			usare lo studio transiente della regione ai differenti tempi (sommavano a 1 con sufficienti cicli extra)
		//			
		//			adesso devo "ricostruire" la prob della regione!!
		//			
		//			e devo mantenere info dei CYCLE differenti per poter calcolare sugli stati lower!!
		//			
		//			
		//			trattamento diverso anche per gli stati interni, magari usare direttamente il nome?
		//					check state.getName() ??



		double greatestTimeStep=state.getTimeStep();
		TransientAnalyzer parentRegionAnalysis=null;
		NumericalValues transientNumerical=null;

		boolean vm = false;


		if(directNever && !state.getName().equals("Vm")) {
			greatestTimeStep= cycleTransientList.getTimeStep();

			if(state.getName().equals("VmRes")) {

				transientNumerical = cycleTransientList.getProbsFromToList(parentRegion.getInitialState(), state).get(0);


			}else {

				List<NumericalValues> list = cycleTransientList.getProbsFromToList(parentRegion.getInitialState(), state);

				double[] values = new double[list.get(0).getValues().length];

				for(int i=0;i<list.size();i++) {
					double curr[] = list.get(i).getValues();
					for(int j = 0;j<values.length;j++) {
						values[j]+= curr[j];
					}

				}

				transientNumerical = new NumericalValues(values, greatestTimeStep);


			}
		}else if(state.getName().equals("Vm")) {	
			greatestTimeStep= cycleTransientList.getTimeStep();

			State endState=null;
			State res=null;
			for(State s: cycleTransientList.getStates()) {
				if(s.getName().equals("Enever"))
					endState = s;
				if(s.getName().equals("VmRes"))
					res = s;

			}
			if(endState == null || res== null) {
				System.out.println("endstate o vmres null in MeanSojourn");
				return;
			}

			// i tempi di arrivo in Vm sono i tempi di conclusione di ogni ciclo + il tempo di soggiorno di VmRes
			arrivalsInVm = cycleTransientList.getProbsFromToList(parentRegion.getInitialState(), endState);
			arrivalsInVm.add(0, sojournTimeDistributions.get(res));
			arrivalsInVm.remove(arrivalsInVm.size()-1);

			vm = true;

		}else {

			parentRegionAnalysis = regionTransientProbabilities.get(parentRegion);

			greatestTimeStep = parentRegionAnalysis.getTimeStep();
			transientNumerical = parentRegionAnalysis.getProbsFromTo(parentRegion.getInitialState(), state);

		}

		//FIXME SE CASO NEVER + FINAL QUI IL FINAL NON VIENE CONSIDERATO (PER ORA CONSIDERA SOLO NEVER + EXIT* )
		//2- Get exit distributions of parallel regions at same level of the direct parent composite state
		List<NumericalValues> exitDistributions = new ArrayList<>();
		State parentState = parentStates.get(parentRegion);
		if(!(parentState instanceof CompositeState))
			throw new IllegalStateException("A non composite state can't contains regions!");
		CompositeState parentCState = (CompositeState) parentState;
		for (Region r : parentCState.getRegions()) {
			if(!r.getType().equals(Region.RegionType.EXIT))//If final (or Never), neglect parallel regions since they don't affect sojourn time
				continue;
			if(r.equals(parentRegion))//Consider only other regions, not the current one
				continue;
			NumericalValues regDistro = regionSojournTimeDistributions.get(r);

			//REMARK all timeSteps must be multiple of each other
			if(variableTimeStep && regDistro.getStep()>greatestTimeStep) {
				greatestTimeStep = regDistro.getStep();
			}
			exitDistributions.add(regDistro);
		}

		double reachingProbability = 1;

		// PER I FIGLI TROVA LA DISTRIBUZIONE PER ENTRARE NEL PADRE E SHIFTA LE PARALLELE DEL PADRE ALL INGRESSO
		//3- Get exit distribution of parallel regions at higher level. Note that distribution must have
		//the same time origin and thus need to be shifted
		if(state.getDepth() > 1) {
			
			//3.1 Get parent exit distribution of previous level region
			Region previousParentRegion = parentRegions.get(parentStates.get(parentRegion)); 
			NumericalValues parentExitDistribution = shiftedExitDistributions.get(previousParentRegion);

			if(variableTimeStep && previousParentRegion.getTimeStep()>greatestTimeStep) {
				greatestTimeStep = previousParentRegion.getTimeStep();
			}
			if(variableTimeStep && parentExitDistribution.getStep()>greatestTimeStep) {
				greatestTimeStep = parentExitDistribution.getStep();
			}
			//here we have determined the least common multiplier step (if -all- steps are multiples of each other)
			NumericalValues parentExitDistributionRescaled = NumericalUtils.rescaleCDF(parentExitDistribution, greatestTimeStep);

			
			for(int i=0; i<CYCLE;i++) {

				List<NumericalValues> exitDistribCycle = new LinkedList<>();
				for(NumericalValues n: exitDistributions) {
					exitDistribCycle.add(n);
				}
			
				//not necessarily scaled
				NumericalValues absorbingProbs = arrivalsInVm.get(i);


				NumericalValues absorbingProbsRescaled = NumericalUtils.rescaleCDF(absorbingProbs, greatestTimeStep);

				
				//3.3 evaluate sojourn time conditioned to be absorbed (shift and project)
				NumericalValues parallelExitDistribution = NumericalUtils.shiftAndProjectAndMinimum(
						absorbingProbsRescaled, 
						Arrays.asList(parentExitDistributionRescaled));

				//3.5 add to the "exitDistribCycle" array
				exitDistribCycle.add(parallelExitDistribution);



				// calcola separatamente alle exit modificate, la probabilit� che non falliscano prima
				//4- Evaluate reaching probability of target sub-state, given that we are in the parent state


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


				//TODO raggruppa questi per regione...
				//5- Evaluate the exit distribution as the minimum and save it for lower regions
				double[] finalExitDistribution = new double[NumericalUtils.computeStepNumber(new OmegaBigDecimal("" + timeLimit), new BigDecimal("" + greatestTimeStep))];
				if(exitDistribCycle.size() == 0) {
					Arrays.fill(finalExitDistribution, 0.0);    
				}else if(exitDistribCycle.size() == 1) {
					NumericalValues singleD = variableTimeStep ? NumericalUtils.rescaleCDF(exitDistribCycle.get(0), greatestTimeStep) : exitDistribCycle.get(0);

					finalExitDistribution = singleD.getValues();
				}else {

					List<NumericalValues> exitD;
					if(variableTimeStep) {
						exitD = new LinkedList<>();
						for(NumericalValues exitDistro: exitDistribCycle) {
							exitD.add(NumericalUtils.rescaleCDF(exitDistro, greatestTimeStep));
						}

					}else {
						exitD = exitDistribCycle;
					}

					for (int t = 0; t < finalExitDistribution.length; t++) {
						double product = 1;
						for(NumericalValues exitDistro: exitD) {
							product*= (1 - exitDistro.getValues()[t]);
						}
						finalExitDistribution[t] = 1 - product;
					}
				}




				double[] transientProbs = NumericalUtils.rescaleCDF(transientNumerical, greatestTimeStep).getValues();

				//6- Evaluate the mean sojourn time
				double mean = 0;
				for (int t = 0; t < transientProbs.length; t++) {
					mean+= transientProbs[t] *  (1 - finalExitDistribution[t]) * greatestTimeStep ;
				} //
				mean = mean * reachingProbability;

				meanSojournInnerAtCycle.get(state).add(mean);
			}

			return;

		}


		//TODO raggruppa questi per regione...
		//5- Evaluate the exit distribution as the minimum and save it for lower regions
		double[] finalExitDistribution = new double[NumericalUtils.computeStepNumber(new OmegaBigDecimal("" + timeLimit), new BigDecimal("" + greatestTimeStep))];
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

		//FIXME viene fatto pi� volte...
		shiftedExitDistributions.put(parentRegion, new NumericalValues(finalExitDistribution, greatestTimeStep));

		if(vm) {

			List<NumericalValues> list = cycleTransientList.getProbsFromToList(parentRegion.getInitialState(), state);

			
			double superMean=0.0;
			for(int i=0; i<arrivalsInVm.size();i++) {

				double[] transientProbs = NumericalUtils.rescaleCDF(list.get(i), greatestTimeStep).getValues();

				//6- Evaluate the mean sojourn time
				double mean = 0;
				for (int t = 0; t < transientProbs.length; t++) {
					mean+= transientProbs[t] *  (1 - finalExitDistribution[t]) * greatestTimeStep ;
				} //
				mean = mean * reachingProbability;

				meanSojournVmAvailableAtCycle.add(mean);
				superMean+=mean;

			}
			meanSojournTimes.put(state, superMean);


		}else {


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



}
