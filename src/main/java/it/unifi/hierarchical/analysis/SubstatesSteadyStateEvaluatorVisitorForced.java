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

//FIXME: This class can be removed and its methods integrated with those of class SubstatesSteadyStateEvaluatorVisitor.

import it.unifi.hierarchical.model.*;
import it.unifi.hierarchical.model.visitor.LogicalLocationVisitor;
import it.unifi.hierarchical.utils.StateUtils;

import java.util.*;

public class SubstatesSteadyStateEvaluatorVisitorForced implements LogicalLocationVisitor {

	//allowed difference between steady state of parents and sum of steady state of childrens
	private static final double SS_ALLOWED_EPSILON = 0.00000001;

	private final double parentLevelSSProb;
	private final double parentLevelMeanSojourn;
	private final Map<LogicalLocation, Double> meanSojournTimes;
	private final Map<String, Double> subStateSSProbs;

	private final List<Double> meanSojournVmAvailableAtCycle;
	private final Map<LogicalLocation, List<Double>> meanSojournInnerAtCycle;

	private final LogicalLocation currentParent;

	/**
	 * Visitor called on a Parent to calculate the steady states of the childrens:
	 * as such it is always called only on Composites
	 *
	 * after the calculation of steady state of the childrens, their remaining time is associated with the ending state
	 *
	 */
	public SubstatesSteadyStateEvaluatorVisitorForced(LogicalLocation currentParent, double parentLevelSSProb, double parentLevelMeanSojourn, Map<LogicalLocation, Double> meanSojournTimes, List<Double> meanSojournVmAvailableAtCycle,
													  Map<LogicalLocation, List<Double>> meanSojournInnerAtCycle){
		this.currentParent = currentParent;
		this.parentLevelSSProb = parentLevelSSProb;
		this.parentLevelMeanSojourn = parentLevelMeanSojourn;
		this.subStateSSProbs = new HashMap<>();
		this.meanSojournTimes = meanSojournTimes;

		this.meanSojournVmAvailableAtCycle = meanSojournVmAvailableAtCycle;
		this.meanSojournInnerAtCycle =meanSojournInnerAtCycle;
	}

	@Override
	public void visit(SimpleStep simpleStep) { }

	@Override
	public void visit(CompositeStep compositeStep) {
		//System.out.println("ss "+ compositeStep.getName());

		double parentPrecision = compositeStep.getTimeStep();

		//Recursively use the visitor
		List<Region> regions = compositeStep.getRegions();
		for (Region region : regions) {


			double regionPrecision = region.getTimeStep();

			LogicalLocation regionEnd=null;
			Double childrenSS=0.0;


			//FIXME NON FATTO CON DIFFERENTI PRECISIONI PER NEVER / FIGLI NEVER
			// if the region is of type exit, theres no time spent on the exit state,
			//so we can smooth the steady state based on the one calculated on the parent
			//if the region is of type final, no normalization can be applied
			if(regionPrecision != parentPrecision && compositeStep.getType().equals(CompositeStepType.FIRST)) {

				Map<String, Double> subStateSSProbsTemporary = new HashMap<>();


				Set<LogicalLocation> visited = new HashSet<>();
				Stack<LogicalLocation> toBeVisited = new Stack<>();
				toBeVisited.push(region.getInitialStep());
				while(!toBeVisited.isEmpty()) {
					LogicalLocation current = toBeVisited.pop();
					visited.add(current);
					double currentSS = parentLevelSSProb * meanSojournTimes.get(current)/parentLevelMeanSojourn;
					subStateSSProbsTemporary.put(current.getName(), currentSS);

					childrenSS+=currentSS;


					//add successors on the same level of the hierarchy
					if(StateUtils.isCompositeWithBorderExit(current)) {
						CompositeStep cState = (CompositeStep) current;
						for(LogicalLocation exitState : cState.getExitSteps().keySet()) {
							List<LogicalLocation> successors = cState.getNextLocations(exitState);

							for (LogicalLocation successor : successors) {
								if(!visited.contains(successor) && !toBeVisited.contains(successor)) {
									//Check if its an exit state
									if(!(successor instanceof FinalLocation)) {
										toBeVisited.add(successor);
									}else
										regionEnd=successor;
								}
							}

						}
					}else {
						for (LogicalLocation successor : current.getNextLocations()) {
							if(!visited.contains(successor) && !toBeVisited.contains(successor)) {
								//Check if its an exit state
								if(!(successor instanceof FinalLocation)) {
									toBeVisited.add(successor);
								}else
									regionEnd=successor;
							}
						}
					}
				}

				//normalization of Region type exit with different precision
				for(Map.Entry<String,Double> entry : subStateSSProbsTemporary.entrySet()) {

					double ss = entry.getValue()/childrenSS * parentLevelSSProb;
					subStateSSProbs.put(entry.getKey(), ss);

				}

				childrenSS = parentLevelSSProb;

				for(LogicalLocation s : visited) {

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


				Set<LogicalLocation> visited = new HashSet<>();
				Stack<LogicalLocation> toBeVisited = new Stack<>();
				toBeVisited.push(region.getInitialStep());
				while(!toBeVisited.isEmpty()) {
					LogicalLocation current = toBeVisited.pop();
					visited.add(current);


					double currentSS=0.0;

					if(current.getName().equals("Vm")) {

						for (Double aDouble : meanSojournVmAvailableAtCycle) {
							currentSS += parentLevelSSProb * aDouble / parentLevelMeanSojourn;
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
						CompositeStep cState = (CompositeStep) current;
						for(LogicalLocation exitState : cState.getExitSteps().keySet()) {
							List<LogicalLocation> successors = cState.getNextLocations(exitState);

							for (LogicalLocation successor : successors) {
								if(!visited.contains(successor) && !toBeVisited.contains(successor)) {
									//Check if its an exit state
									if(!(successor instanceof FinalLocation)) {
										toBeVisited.add(successor);
									}else
										regionEnd=successor;
								}
							}

						}
					}else {
						for (LogicalLocation successor : current.getNextLocations()) {
							if(!visited.contains(successor) && !toBeVisited.contains(successor)) {
								//Check if its an exit state
								if(!(successor instanceof FinalLocation)) {
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

			if(region.getType()!=RegionType.NEVERENDING) {
				double endRegionSS = parentLevelSSProb - childrenSS;
				if(endRegionSS<0. && endRegionSS>=- SS_ALLOWED_EPSILON) {
					endRegionSS =0.;
				}
				if(endRegionSS< -SS_ALLOWED_EPSILON) {
					//System.out.println("Error: steady state of childrens is higher than steady state of parent "+ currentParent.getName());
				}
				subStateSSProbs.put(regionEnd.getName(), endRegionSS);
			}

		}
	}

	@Override
	public void visit(FinalLocation finalLocation) { }

	public Map<String, Double> getSubStateSSProbs() {
		return subStateSSProbs;
	}

}
