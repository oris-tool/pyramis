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
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.utils.NumericalUtils;
import org.oristool.math.OmegaBigDecimal;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Numerical transient analysis of the semi-Markov process underlying a {@link Region}.
 */
public class SMPAnalyzer implements TransientAnalyzer{

	private final List<LogicalLocation> states;
	private final double[][][] probs;
	private final double timeLimit;
	private final double timeStep;

	// FIXME: This attribute may be eliminated
	private final LogicalLocation absorbingState;

	// FIXME: This attribute may be replaced by a local variable
	private static final long timeElapsed = 0;

	// FIXME: This constructor may be eliminated
	//FIXME: absorbingState may be eliminated
	public SMPAnalyzer(List<LogicalLocation> states, Map<LogicalLocation, NumericalValues> sojournTimeDistributions, double timeLimit, double timeStep) {
		this(states, sojournTimeDistributions, timeLimit, timeStep, null);
	}

	// FIXME: It may be better to run the analysis in a static method rather than in the constructor
	public SMPAnalyzer(List<LogicalLocation> states, Map<LogicalLocation, NumericalValues> sojournTimeDistributions, double timeLimit, double timeStep, LogicalLocation absorbingState) {
		this.states = states;
		this.timeLimit = timeLimit;
		this.timeStep = timeStep;
		this.absorbingState = absorbingState;
		int ticks = NumericalUtils.computeTickNumber(new OmegaBigDecimal(""+timeLimit), new BigDecimal(""+timeStep));

		//System.out.println("RandomStateInRegion "+states.get(0).getName());
		//System.out.println(states.size()+" "+ ticks);

		//Date start = new Date();
		//Date d2 = new Date();
		//long time;

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

					for(int t = 0; t < ticks; t++) {
						kernel[i][j][t] = p_ij * sojournDistrib[t];
					}
				}
			}
		}

		//Date d3 = new Date();
		//time = d3.getTime() - d2.getTime();
		//System.out.println(time+ "  kernelQQQ");

		//Init transient probs array
		probs = new double[states.size()][states.size()][ticks];
		for (int i = 0; i < probs.length; i++) {
			probs[i][i][0] = 1;
		}

		// Evaluate transient probabilities of the SMP
		// REMARK: only a single step is allowed to be executed during a time tick,
		// i.e., a sequence of immediate executed steps may cause errors
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
							probs[i][j][t]+= (kernel[i][k][u] -lastKernelProb)*probs[k][j][t - u];
						}
					}
				}
			}
		}

		//Date d4 = new Date();
		//time = d4.getTime() - d3.getTime();
		//System.out.println(time+ "  transQQQ");

		//Date end = new Date();
		//timeElapsed += end.getTime() - start.getTime();
		//System.out.println("(ms): " + timeElapsed);
	}

	@Override
	public List<LogicalLocation> getStates() {
		return states;
	}

	public double[][][] getTransientProbabilities() {
		return probs;
	}

	@Override
	public double getTimeLimit() {
		return timeLimit;
	}

	@Override
	public double getTimeStep() {
		return timeStep;
	}

	public LogicalLocation getAbsorbingState() {
		return absorbingState;
	}

	@Override
	public NumericalValues getTransientProbability(LogicalLocation from, LogicalLocation to) {
		int fromStateIndex = states.indexOf(from);
		int toStateIndex = states.indexOf(to);
		double[] result = probs[fromStateIndex][toStateIndex];
		return new NumericalValues(result, timeStep);
	}
}
