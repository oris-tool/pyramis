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
import it.unifi.hierarchical.utils.StateUtils;

import java.util.*;
import java.util.Map.Entry;

// Calculates the steady state of steps that are not at top-level
public class SubstatesSteadyStateEvaluatorVisitor implements LogicalLocationVisitor {

	//allowed difference between steady state of parents and sum of steady state of childrens
	private static final double SS_ALLOWED_EPSILON = 0.00000001;

	private final double parentLevelSSProb;
	private final double parentLevelMeanSojourn;
	private final Map<LogicalLocation, Double> meanSojournTimes;
	private final Map<String, Double> subStateSSProbs;

	private final LogicalLocation currentParent;

	/**
	 * Visitor called on a Parent to calculate the steady states of the childrens:
	 * as such it is always called only on Composites
	 *
	 * after the calculation of steady state of the childrens, their remaining time is associated with the ending state
	 *
	 */
	public SubstatesSteadyStateEvaluatorVisitor(LogicalLocation currentParent, double parentLevelSSProb, double parentLevelMeanSojourn, Map<LogicalLocation, Double> meanSojournTimes){
		this.currentParent = currentParent;
		this.parentLevelSSProb = parentLevelSSProb;
		this.parentLevelMeanSojourn = parentLevelMeanSojourn;
		this.subStateSSProbs = new HashMap<>();
		this.meanSojournTimes = meanSojournTimes;
	}

	@Override
	public void visit(SimpleStep simpleStep) { }

	@Override
	public void visit(CompositeStep compositeStep) {
		//Recursively use the visitor
		List<Region> regions = compositeStep.getRegions();
		for (Region region : regions) {


			LogicalLocation regionEnd=null;
			Double childrenSS=0.0;


			// if the region is of type exit, theres no time spent on the exit state,
			//so we can smooth the steady state based on the one calculated on the parent
			//if the region is of type final, no normalization can be applied
			if(compositeStep.getType().equals(CompositeStepType.FIRST)) {

				Map<String, Double> subStateSSProbsTemporary = new HashMap<>();

				double sumSojourn =0.0;

				Set<LogicalLocation> visited = new HashSet<>();
				Stack<LogicalLocation> toBeVisited = new Stack<>();
				toBeVisited.push(region.getInitialStep());
				while(!toBeVisited.isEmpty()) {
					LogicalLocation current = toBeVisited.pop();
					visited.add(current);
					double currentSS = parentLevelSSProb * meanSojournTimes.get(current)/parentLevelMeanSojourn;
					subStateSSProbsTemporary.put(current.getName(), currentSS);
					sumSojourn += meanSojournTimes.get(current);

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
				for(Entry<String,Double> entry : subStateSSProbsTemporary.entrySet()) {

					double ss = entry.getValue()/childrenSS * parentLevelSSProb;
					subStateSSProbs.put(entry.getKey(), ss);

				}

				childrenSS = parentLevelSSProb;

				for(LogicalLocation s : visited) {

					SubstatesSteadyStateEvaluatorVisitor newVisitor = new SubstatesSteadyStateEvaluatorVisitor(
							s,
							subStateSSProbs.get(s.getName()),
							meanSojournTimes.get(s),
							meanSojournTimes);
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
					double currentSS = parentLevelSSProb * meanSojournTimes.get(current)/parentLevelMeanSojourn;
					subStateSSProbs.put(current.getName(), currentSS);

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




					SubstatesSteadyStateEvaluatorVisitor newVisitor = new SubstatesSteadyStateEvaluatorVisitor(
							current,
							subStateSSProbs.get(current.getName()),
							meanSojournTimes.get(current),
							meanSojournTimes);
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
