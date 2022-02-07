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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.visitor.StateVisitor;
import it.unifi.hierarchical.utils.StateUtils;

import java.util.Set;
import java.util.Stack;

//FIXME: This class can be removed and its methods integrated with those of class SubstatesSteadyStateEvaluatorVisitor.

public class SubstatesSteadyStateEvaluatorVisitorForced implements StateVisitor{

	//allowed difference between steady state of parents and sum of steady state of childrens
	private static final double SS_ALLOWED_EPSILON = 0.00000001;

	private double parentLevelSSProb;
	private double parentLevelMeanSojourn;
	private Map<State, Double> meanSojournTimes;
	private Map<String, Double> subStateSSProbs;

	private List<Double> meanSojournVmAvailableAtCycle;
	private Map<State, List<Double>> meanSojournInnerAtCycle;

	private State currentParent;

	/**
	 * Visitor called on a Parent to calculate the steady states of the childrens: 
	 * as such it is always called only on Composites
	 * 
	 * after the calculation of steady state of the childrens, their remaining time is associated with the ending state
	 *  
	 */
	public SubstatesSteadyStateEvaluatorVisitorForced(State currentParent, double parentLevelSSProb, double parentLevelMeanSojourn, Map<State, Double> meanSojournTimes, List<Double> meanSojournVmAvailableAtCycle,
			Map<State, List<Double>> meanSojournInnerAtCycle){
		this.currentParent = currentParent;
		this.parentLevelSSProb = parentLevelSSProb;
		this.parentLevelMeanSojourn = parentLevelMeanSojourn;
		this.subStateSSProbs = new HashMap<>();
		this.meanSojournTimes = meanSojournTimes;

		this.meanSojournVmAvailableAtCycle = meanSojournVmAvailableAtCycle;
		this.meanSojournInnerAtCycle =meanSojournInnerAtCycle;
	}

	@Override
	public void visit(SimpleState state) { }

	@Override
	public void visit(CompositeState state) {
		System.out.println("ss "+ state.getName());

		double parentPrecision = state.getTimeStep();

		//Recursively use the visitor
		List<Region> regions = state.getRegions();
		for (Region region : regions) {


			double regionPrecision = region.getTimeStep();

			State regionEnd=null;
			Double childrenSS=0.0;

			// if the region is of type exit, theres no time spent on the exit state, 
			//so we can smooth the steady state based on the one calculated on the parent
			//if the region is of type final, no normalization can be applied
			if(regionPrecision != parentPrecision && region.getType()==RegionType.EXIT) {

				Map<String, Double> subStateSSProbsTemporary = new HashMap<>();


				Set<State> visited = new HashSet<>();
				Stack<State> toBeVisited = new Stack<>();
				toBeVisited.push(region.getInitialState());
				while(!toBeVisited.isEmpty()) {
					State current = toBeVisited.pop();
					visited.add(current);
					Double currentSS = parentLevelSSProb * meanSojournTimes.get(current)/parentLevelMeanSojourn;
					subStateSSProbsTemporary.put(current.getName(), currentSS);

					childrenSS+=currentSS;


					//add successors on the same level of the hierarchy
					if(StateUtils.isCompositeWithBorderExit(current)) {
						CompositeState cState = (CompositeState) current;
						for(State exitState : cState.getNextStatesConditional().keySet()) {
							List<State> successors = cState.getNextStatesConditional().get(exitState);

							for (State successor : successors) {
								if(!visited.contains(successor) && !toBeVisited.contains(successor)) {
									//Check if its an exit state
									if(!(successor instanceof ExitState || successor instanceof FinalState)) {
										toBeVisited.add(successor);
									}else
										regionEnd=successor;
								}
							}

						}
					}else {
						for (State successor : current.getNextStates()) {
							if(!visited.contains(successor) && !toBeVisited.contains(successor)) {
								//Check if its an exit state
								if(!(successor instanceof ExitState || successor instanceof FinalState)) {
									toBeVisited.add(successor);
								}else
									regionEnd=successor;
							}
						}
					}
				}

				//normalization of Region type exit with different precision
				for(Entry<String,Double> entry : subStateSSProbsTemporary.entrySet()) {

					double ss = entry.getValue()/childrenSS * parentLevelSSProb;
					subStateSSProbs.put(entry.getKey(), ss);

				}

				childrenSS = parentLevelSSProb;

				for(State s : visited) {

					SubstatesSteadyStateEvaluatorVisitorForced newVisitor = new SubstatesSteadyStateEvaluatorVisitorForced(
							s,
							subStateSSProbs.get(s.getName()), 
							meanSojournTimes.get(s), 
							meanSojournTimes,
							meanSojournVmAvailableAtCycle,
							meanSojournInnerAtCycle);
					s.accept(newVisitor);
					subStateSSProbs.putAll(newVisitor.getSubStateSSProbs());
				}



			}else {


				Set<State> visited = new HashSet<>();
				Stack<State> toBeVisited = new Stack<>();
				toBeVisited.push(region.getInitialState());
				while(!toBeVisited.isEmpty()) {
					State current = toBeVisited.pop();
					visited.add(current);


					Double currentSS=0.0;

					if(current.getName().equals("Vm")) {

						for(int i=0;i<meanSojournVmAvailableAtCycle.size();i++) {
							currentSS+= parentLevelSSProb * meanSojournVmAvailableAtCycle.get(i)/parentLevelMeanSojourn;
						}

						subStateSSProbs.put(current.getName(), currentSS);
					}else if(current.getDepth()>1) {

						Double sum=0.0;
						
						for(int i=0;i<meanSojournInnerAtCycle.size();i++) {
							sum+=meanSojournInnerAtCycle.get(current).get(i);
							
							double superparentLevelSSProb = parentLevelSSProb / parentLevelMeanSojourn;

							currentSS+= superparentLevelSSProb * meanSojournInnerAtCycle.get(current).get(i);
						}
						subStateSSProbs.put(current.getName(), currentSS);
						meanSojournTimes.put(current, sum);
						
					}else {


						currentSS= parentLevelSSProb * meanSojournTimes.get(current)/parentLevelMeanSojourn;
						subStateSSProbs.put(current.getName(), currentSS);
					}
					childrenSS+=currentSS;


					//add successors on the same level of the hierarchy
					if(StateUtils.isCompositeWithBorderExit(current)) {
						CompositeState cState = (CompositeState) current;
						for(State exitState : cState.getNextStatesConditional().keySet()) {
							List<State> successors = cState.getNextStatesConditional().get(exitState);

							for (State successor : successors) {
								if(!visited.contains(successor) && !toBeVisited.contains(successor)) {
									//Check if its an exit state
									if(!(successor instanceof ExitState || successor instanceof FinalState)) {
										toBeVisited.add(successor);
									}else
										regionEnd=successor;
								}
							}

						}
					}else {
						for (State successor : current.getNextStates()) {
							if(!visited.contains(successor) && !toBeVisited.contains(successor)) {
								//Check if its an exit state
								if(!(successor instanceof ExitState || successor instanceof FinalState)) {
									toBeVisited.add(successor);
								}else
									regionEnd=successor;
							}
						}
					}




					SubstatesSteadyStateEvaluatorVisitorForced newVisitor = new SubstatesSteadyStateEvaluatorVisitorForced(
							current,
							subStateSSProbs.get(current.getName()), 
							meanSojournTimes.get(current), 
							meanSojournTimes,meanSojournVmAvailableAtCycle,
							meanSojournInnerAtCycle);
					current.accept(newVisitor);
					subStateSSProbs.putAll(newVisitor.getSubStateSSProbs());
				}

			}

			if(region.getType()!=RegionType.NEVER) {
				double endRegionSS = parentLevelSSProb - childrenSS;
				if(endRegionSS<0. && endRegionSS>=- SS_ALLOWED_EPSILON) {
					endRegionSS =0.;
				}
				if(endRegionSS< -SS_ALLOWED_EPSILON) {
					System.out.println("Error: steady state of childrens is higher than steady state of parent "+ currentParent.getName());
				}
				subStateSSProbs.put(regionEnd.getName(), endRegionSS);
			}

		}
	}

	@Override
	public void visit(FinalState state) { }

	@Override
	public void visit(ExitState state) { }

	public Map<String, Double> getSubStateSSProbs() {
		return subStateSSProbs;
	}

}
