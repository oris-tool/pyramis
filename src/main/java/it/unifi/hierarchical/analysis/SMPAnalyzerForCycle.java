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

import it.unifi.hierarchical.model.LogicalLocation;
import it.unifi.hierarchical.utils.NumericalUtils;
import org.oristool.math.OmegaBigDecimal;

import java.math.BigDecimal;
import java.util.*;

// FIXME: This class should be removed and its methods integrated with those of class SMPAnalyzer.
public class SMPAnalyzerForCycle implements TransientAnalyzer{

	private final List<LogicalLocation> states;
	private final List<double[][][]> probsL;
	private final double timeLimit;
	private final double timeStep;

	private int indexChange;
	private int endIndex;

	private LogicalLocation initialCycleState;

	//private static long timeElapsed=0;

	public SMPAnalyzerForCycle(List<LogicalLocation> states, Map<LogicalLocation, NumericalValues> sojournTimeDistributions, double timeLimit, double timeStep, int CYCLE) {
		this.states = states;
		this.timeLimit = timeLimit;
		this.timeStep = timeStep;

		Map<Integer, Double> nonZeroIndex = new HashMap<>();
		probsL = new LinkedList<>();

		for(LogicalLocation s: states) {
			if(s.getName().contains("VmRes")) {
				initialCycleState=s;
				indexChange = states.indexOf(s);
			}
			if(s.getName().contains("Enever")) {
				endIndex = states.indexOf(s);
			}
		}
		if(initialCycleState==null) {
			System.out.println("null initial Cycle c'� un problema");
		}

		int ticks = NumericalUtils.computeTickNumber(new OmegaBigDecimal(""+timeLimit), new BigDecimal(""+timeStep));

		//System.out.println("Region Never ");
		Date start = new Date();

		//Evaluate kernel
		double[][][] kernel = new double[states.size()][states.size()][ticks];

		for(int i = 0; i < states.size(); i++) {//From

			LogicalLocation from = states.get(i);

			NumericalValues sojourn = sojournTimeDistributions.get(from);
			double[] sojournDistrib;
			if(sojourn == null) { //absorbing state
				sojournDistrib = new double[ticks];
				//returns all ticks as 0 for current state, i in the kernel, not adding anything
			}else {

				sojournDistrib = sojourn.getValues();

				for(int q=0; q < from.getNextLocations().size(); q++) {//To
					LogicalLocation to = from.getNextLocations().get(q);
					int j = states.indexOf(to);


					double p_ij = from.getBranchingProbabilities().get(q);
					if(indexChange==i)
						nonZeroIndex.put(j, p_ij);


					for(int t = 0; t < ticks; t++) {
						kernel[i][j][t] = p_ij * sojournDistrib[t];
					}
				}
			}
		}

		for(int rr=0; rr<CYCLE;rr++) {

			//System.out.println("ciclo "+rr);

			//Init transient probs array
			double[][][] probs = new double[states.size()][states.size()][ticks];
			for (int i = 0; i < probs.length; i++) {
				probs[i][i][0] = 1;
			}

			//Evaluate transient probabilities of the SMP
			//REMARK only a single step is allowed in a tick, Multiple Det(0) are likely cause of errors
			for(int t = 1; t < ticks; t++) {
				for(int i = 0; i < states.size(); i++) {//From
					LogicalLocation from = states.get(i);

					NumericalValues sojourn = sojournTimeDistributions.get(from);

					boolean immediateTransition=false;

					double[] sojournDistrib;
					if(sojourn == null) //absorbing state
						sojournDistrib = new double[ticks];
					else {
						sojournDistrib = sojourn.getValues();
						immediateTransition = (sojourn.getValues()[0] == 1.0);
					}
					for(int j = 0; j < states.size(); j++) {//To

						//holding time
						if(j==i)
							probs[i][i][t]+= (1 - sojournDistrib[t]);

						//global kernel derivative (kernel - kernel) * prob(t-u)
						for(int k = 0; k < states.size(); k++) {
							for(int u = 1; u <= t; u++) {//Integral

								//FIXME we create a special case only for an Immediate,
								//perhaps the problem is for everything and a shift is needed
								double lastKernelProb;
								if(immediateTransition && u==1) {
									lastKernelProb=0.0;
								}else {
									lastKernelProb=kernel[i][k][u - 1];
								}

								probs[i][j][t] += (kernel[i][k][u] -lastKernelProb)*probs[k][j][t - u];
							}
						}
					}
				}
			}

			double[] sojournDistrib = probs[indexChange][endIndex];

			for(Integer kk: nonZeroIndex.keySet()) {//To
				for(int t = 0; t < ticks; t++) {
					kernel[indexChange][kk][t] = nonZeroIndex.get(kk) * sojournDistrib[t];
				}
			}
			probsL.add(probs);
		}

		//Date end = new Date();
		//timeElapsed += end.getTime() - start.getTime();
		//System.out.println("(ms): " + timeElapsed );
	}

	@Override
	public List<LogicalLocation> getStates() {
		return states;
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
	public NumericalValues getTransientProbability(LogicalLocation from, LogicalLocation to) {
		System.out.println("error should not be here SMPAnalyzerForCycle called with getProbsFromTo");
		return null;
	}

	public NumericalValues getProbsFromTo(LogicalLocation from, LogicalLocation to, int i) {

		int fromStateIndex = states.indexOf(from);
		int toStateIndex = states.indexOf(to);

		double[] result = probsL.get(i)[fromStateIndex][toStateIndex];
		return new NumericalValues(result, timeStep);
	}
}
