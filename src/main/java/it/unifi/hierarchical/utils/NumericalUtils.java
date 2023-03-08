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

package it.unifi.hierarchical.utils;

import it.unifi.hierarchical.analysis.ErlangExp;
import it.unifi.hierarchical.analysis.NumericalValues;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.EXP;
import org.oristool.math.function.GEN;
import org.oristool.math.function.PartitionedFunction;
import org.oristool.math.function.PartitionedGEN;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

// FIXME: May commented methods turn out to be useful or can they be eliminated?
// FIXME: Check whether evaluateEXP, evaluateGEN, evaluatePartitioneGEN, and isDeterministic are methods of the SIRIO library.
public class NumericalUtils {

	//	public static double[] convolveCDFs(double[] first, double[] second, double step) {
	//		double[] firstPDF = computePDFFromCDF(first, new BigDecimal("" + step));
	//		double[] secondPDF = computePDFFromCDF(second, new BigDecimal("" + step));
	//		double[] resultPDF = convolvePDFs(firstPDF, secondPDF, step);
	//		return computeCDFFromPDF(resultPDF, new BigDecimal("" + step));
	//	}
	//
	//	public static double[] convolvePDFs(double[] first, double[] second, double step) {
	//
	//		if (first == null || second == null)
	//			throw new IllegalArgumentException("Convolution parameter can't be null");
	//
	//		final int firstLen = first.length;
	//		final int secondLen = second.length;
	//
	//		if (firstLen == 0 || secondLen == 0) {
	//			throw new IllegalArgumentException("Convolution parameter can't be empty");
	//		}
	//		if (firstLen != secondLen) {
	//			throw new IllegalArgumentException("Convolution parameter must have the same length");
	//		}
	//
	//		int resultLength = firstLen;
	//
	//		double[] result = new double[resultLength];
	//		// Evaluate every element of the final result
	//		for (int i = 0; i < resultLength; i++) {
	//			result[i] = 0;
	//			// For every element of the second array. If j>i is not evaluated because it will have a negative index
	//			for (int j = 0; j <= i; j++) {
	//				result[i] += first[j] * step * second[i - j];
	//			}
	//		}
	//		return result;
	//	}

	public static NumericalValues maxCDF(Collection<NumericalValues> distributions) {

		double[] resultCDF = new double[distributions.iterator().next().getValues().length];
		List<NumericalValues> distributionsList = new ArrayList<>(distributions);

		for (int i = 0; i < resultCDF.length; i++) {
			resultCDF[i] = distributionsList.get(0).getValues()[i];
			for(int d = 1; d < distributionsList.size(); d++) {
				resultCDF[i] *= distributionsList.get(d).getValues()[i];
			}
		}
		return new NumericalValues(resultCDF, distributions.iterator().next().getStep());
	}

	/**
	 * Used when different ticks are present
	 */
	public static NumericalValues maxCDFvar(Collection<NumericalValues> distributions, double regionTimeStep) {
		Set<NumericalValues> newDistributions = new HashSet<>();

		for(NumericalValues distribution : distributions) {
			NumericalValues newDistribution = rescaleCDF(distribution, regionTimeStep);
			newDistributions.add(newDistribution);
		}
		return maxCDF(newDistributions);
	}


	//	public static NumericalValues minPDF(Collection<NumericalValues> pdfs) {
	//
	//		List<NumericalValues> distributions = new ArrayList<>();
	//
	//		for (NumericalValues pdf : pdfs) {
	//			BigDecimal stepBD = new BigDecimal(""+pdf.getStep());
	//			distributions.add(new NumericalValues(computeCDFFromPDF(pdf.getValues(), stepBD), pdf.getStep()));
	//		}
	//
	//		NumericalValues minCDF = minCDF(distributions);
	//		BigDecimal stepBD = new BigDecimal(""+minCDF.getStep());
	//
	//		return new NumericalValues(computePDFFromCDF(minCDF.getValues(),stepBD), minCDF.getStep());
	//	}

	public static NumericalValues minCDF(Collection<NumericalValues> distributions) {

		double[] resultCDF = new double[distributions.iterator().next().getValues().length];
		List<NumericalValues> distributionsList = new ArrayList<>(distributions);

		for (int i = 0; i < resultCDF.length; i++) {
			resultCDF[i] = 1 - distributionsList.get(0).getValues()[i];
			for(int d = 1; d < distributionsList.size(); d++) {
				resultCDF[i] *= 1 - distributionsList.get(d).getValues()[i];
			}

			resultCDF[i] = 1 - resultCDF[i];
		}
		return new NumericalValues(resultCDF, distributions.iterator().next().getStep());
	}

	/**
	 * Used when different timeSteps are present
	 */
	public static NumericalValues minCDFvar(Collection<NumericalValues> distributions, double regionTimeStep) {

		Set<NumericalValues> newDistributions = new HashSet<>();

		for(NumericalValues distribution : distributions) {
			NumericalValues newDistribution = rescaleCDF(distribution, regionTimeStep);
			newDistributions.add(newDistribution);
		}
		return minCDF(newDistributions);
	}

	public static double[] computeCDFFromPDF(double[] pdf, BigDecimal step) {
		double[] cdf = new double[pdf.length];
		for (int i = 0; i < pdf.length; i++) {
			double previousValue = i == 0 ? 0.0 : cdf[i - 1];
			double newValue = previousValue + pdf[i] * step.doubleValue();
			cdf[i] = newValue > 1 ? 1 : newValue;
			//REMARK Reduces possible errors from overexstimation but
			//does nothing to assure that once no more values !=0
			//are present the CDF is =1, which is a consequence
			//of having a choosen truncation point
		}

		return cdf;
	}

	public static double[] computePDFFromCDF(double[] cdf, BigDecimal step) {
		double[] pdf = new double[cdf.length];
		for (int i = 0; i < cdf.length; i++) {
			double previousValue = i == 0 ? 0.0 : cdf[i - 1];
			pdf[i] = (cdf[i] - previousValue) / step.doubleValue();
		}

		return pdf;
	}

	/**
	 *Given a NumericalValues CDF with step A, returns a NumericalValues with step newStep, by skipping intermediate values
	 */
	public static NumericalValues rescaleCDF(NumericalValues cdf, double newStep) {

		if(cdf==null) {
			return null;
		}

		double[] origValues = cdf.getValues();
		double oldStep = cdf.getStep();

		if(oldStep > newStep + 1e-9) {
			throw new UnsupportedOperationException("Not yet supported higher level timeSteps finer than the lower levels ");
		}

		//REMARK Gives errors when for example 0.2 and 0.3 are compared, because
		//we don't allow interpolation, only exact multiples
		int multiplier = (int) Math.round(newStep/oldStep);
		if(multiplier==1) {
			return cdf;
		}

		int oldSize = origValues.length;
		int newSize = ((oldSize-1)/ multiplier) +1;

		double[] newValues = new double[newSize];

		//does not care about PDFs need to rescale immediate!!
		//old version newValues[i]=origValues[i*multiplier];
		//old version meant that each cdf[t] was the value of cdf in t-, current version,
		//in agreement with analysis and evaluateFunction has cdf[t] = value of cdf in (t+1)-

		for(int i=0; i<newSize-1;i++) {
			newValues[i]=origValues[(i+1)*multiplier-1];
		}
		newValues[newSize-1] = origValues[oldSize-1];

		//System.out.println(Arrays.toString(rescaled.getValues()));

		return new NumericalValues(newValues, newStep);
	}

	//		/**
	//		 *Given a NumericalValues PDF with step A, returns a NumericalValues with step newStep
	//		 */
	//		public static NumericalValues rescalePDF(NumericalValues pdf, double newStep) {
	//
	//			if(pdf==null) {
	//				return null;
	//			}
	//
	//			double oldStep = pdf.getStep();
	//
	//			if(oldStep > newStep + 1e-9) {
	//				throw new UnsupportedOperationException("Not yet supported higher level timeSteps finer than the lower levels ");
	//
	//			}
	//
	//			int multiplier = (int) Math.round(newStep/oldStep);
	//			if(multiplier==1) {
	//				return pdf;
	//			}
	//
	//
	//			double[] origValuesCDF = computeCDFFromPDF(pdf.getValues(), new BigDecimal(""+pdf.getStep()));
	//
	//			int oldSize = origValuesCDF.length;
	//			int newSize = ((oldSize-1)/ multiplier) +1;
	//
	//			double[] newValuesCDF = new double[newSize];
	//
	//			for(int i=0; i<newSize;i++) {
	//				newValuesCDF[i]=origValuesCDF[i*multiplier];
	//			}
	//
	//			double[] newValuesPDF = computePDFFromCDF(newValuesCDF, new BigDecimal(""+newStep));
	//
	//
	//			NumericalValues rescaled = new NumericalValues(newValuesPDF, newStep);
	//			return rescaled;
	//		}

	public static int computeTickNumber(OmegaBigDecimal timeLimit, BigDecimal timeStep) {
		return timeLimit.divide(timeStep, MathContext.DECIMAL128).intValue() + 1;
	}

	public static double[] evaluateFunction(PartitionedFunction density, OmegaBigDecimal timeLimit, BigDecimal timeStep) {
		if(density instanceof EXP)
			return evaluateEXP((EXP) density, timeLimit, timeStep);
		else if( density instanceof TwoParameterHypoEXP)
			return evaluateErlangExp((TwoParameterHypoEXP) density, timeLimit, timeStep);
		else if(density instanceof GEN)
			return evaluateGEN((GEN) density, timeLimit, timeStep);
		else if(density instanceof PartitionedGEN)
			return evaluatePartitionedGEN((PartitionedGEN)density, timeLimit, timeStep);
		else
			throw new IllegalArgumentException("Unsupported type!");
	}


	public static double[] evaluateErlangExp(TwoParameterHypoEXP density, OmegaBigDecimal timeLimit, BigDecimal timeStep) {
		double[] val1;
		double[] val2;

		val1 = evaluateEXP(density.getFirst(), timeLimit, timeStep);
		val2 = evaluateEXP(density.getSecond(), timeLimit, timeStep);

		double[] val = new double[val1.length];

		for(int t0=0; t0< val1.length; t0++) {
			for(int t1=0; t1< val2.length; t1++) {
				if(t0+t1<val.length) {
					val[t0+t1]+= val1[t0]*val2[t1] * timeStep.doubleValue();
				}
			}
		}
		return val;
	}

	private static double[] evaluateGEN(GEN density, OmegaBigDecimal timeLimit, BigDecimal timeStep) {
		int stepsNumber = computeTickNumber(timeLimit, timeStep);
		double[] values = new double[stepsNumber];

		for (int t = 0; t < stepsNumber; t++) {
			double time = t * timeStep.doubleValue();
			Map<Variable, OmegaBigDecimal> timePoint = new HashMap<>();
			timePoint.put(Variable.X, new OmegaBigDecimal("" + time));

			if (isDeterministic(density)) {
				// Special case: IMMEDIATE or DETERMINISTIC
				//old version: function.getDomain().contains(timePoint)
				double detImpulse = density.getDomain().getCoefficient(Variable.X, Variable.TSTAR).doubleValue();

				if (detImpulse>=time && detImpulse<time+timeStep.doubleValue())
					values[t] = BigDecimal.ONE.divide(timeStep, MathContext.DECIMAL128).doubleValue();
				else
					values[t] = 0;
				continue;
			}

			if (density.getDomain().contains(timePoint)) {
				values[t] = density.getDensity().evaluate(timePoint).doubleValue();
			}
		}

		return values;
	}

	private static double[] evaluateEXP(EXP density, OmegaBigDecimal timeLimit, BigDecimal timeStep) {
		int stepsNumber = computeTickNumber(timeLimit, timeStep);
		double[] values = new double[stepsNumber];

		for (int t = 0; t < stepsNumber; t++) {
			double time = t * timeStep.doubleValue();
			Map<Variable, OmegaBigDecimal> timePoint = new HashMap<>();
			timePoint.put(Variable.X, new OmegaBigDecimal("" + time));

			if (density.getDomain().contains(timePoint)) {
				values[t] = density.getDensity().evaluate(timePoint).doubleValue();
			}
		}

		return values;
	}

	public static double[] evaluatePartitionedGEN(PartitionedGEN density, OmegaBigDecimal timeLimit, BigDecimal timeStep) {
		List<GEN> functions = density.getFunctions();

		int stepsNumber = computeTickNumber(timeLimit, timeStep);
		double[] values = new double[stepsNumber];

		for (int t = 0; t < stepsNumber; t++) {
			double time = t * timeStep.doubleValue();
			Map<Variable, OmegaBigDecimal> timePoint = new HashMap<>();
			timePoint.put(Variable.X, new OmegaBigDecimal("" + time));

			if (isDeterministic(density)) {
				// Special case: IMMEDIATE or DETERMINISTIC
				GEN function = functions.get(0);
				//old version: function.getDomain().contains(timePoint)
				double detImpulse = function.getDomain().getCoefficient(Variable.X, Variable.TSTAR).doubleValue();

				if (detImpulse>=time && detImpulse<time+timeStep.doubleValue())
					values[t] = BigDecimal.ONE.divide(timeStep, MathContext.DECIMAL128).doubleValue();
				else
					values[t] = 0;
				continue;
			}

			for (GEN function : functions) {
				if (function.getDomain().contains(timePoint)) {
					values[t] = function.getDensity().evaluate(timePoint).doubleValue();
				}
			}
		}

		return values;
	}

	public static boolean isDeterministic(PartitionedGEN pGen) {
		if (pGen.getFunctions().size() != 1)
			return false;
		GEN gen = pGen.getFunctions().get(0);

		return gen.getDomain().getCoefficient(Variable.X, Variable.TSTAR)
				.compareTo(gen.getDomain().getCoefficient(Variable.TSTAR, Variable.X).negate()) == 0;
	}

	public static boolean isDeterministic(GEN gen) {
		return gen.getDomain().getCoefficient(Variable.X, Variable.TSTAR)
				.compareTo(gen.getDomain().getCoefficient(Variable.TSTAR, Variable.X).negate()) == 0;
	}

	public static NumericalValues conditionDistributionToFire(NumericalValues fired, List<NumericalValues> others, double timeLimit, double greatestTimeStep, boolean variableTimeStep) {

		double[] finalExitDistribution = new double[NumericalUtils.computeTickNumber(new OmegaBigDecimal("" + timeLimit), new BigDecimal("" + greatestTimeStep))];
		if(others.size() == 0) {
			//System.out.println("Composite with single Region!! Care!!");
			return fired;
		} else if(others.size() == 1) {
			NumericalValues singleD = variableTimeStep ? NumericalUtils.rescaleCDF(others.get(0), greatestTimeStep) : others.get(0);

			finalExitDistribution = singleD.getValues();
		} else {

			List<NumericalValues> exitD;
			if(variableTimeStep) {
				exitD = new LinkedList<>();
				for(NumericalValues exitDistro: others) {
					exitD.add(NumericalUtils.rescaleCDF(exitDistro, greatestTimeStep));
				}

			}else {
				exitD = others;
			}

			for (int t = 0; t < finalExitDistribution.length; t++) {
				double product = 1;
				for(NumericalValues exitDistro: exitD) {
					product*= (1 - exitDistro.getValues()[t]);
				}
				finalExitDistribution[t] = 1 - product;
			}
		}

		NumericalValues first = new NumericalValues(finalExitDistribution, greatestTimeStep);

		double[] firedPDF = computePDFFromCDF(fired.getValues(), new BigDecimal("" + fired.getStep()));
		double[] firstPDF = computePDFFromCDF(first.getValues(), new BigDecimal("" + first.getStep()));

		//Successor probability
		double prob = 0;
		for (int t1 = 0; t1 < firedPDF.length; t1++) {
			for (int t2 = t1; t2 < firstPDF.length; t2++) {
				prob+= firedPDF[t1] * firstPDF[t2] * fired.getStep() * first.getStep();
			}
		}

		//Shift and project
		double[] conditionedPdf = new double[firstPDF.length];

		for (int t0 = 0; t0 < firedPDF.length; t0++) {
			for (int t1 = t0; t1 < firstPDF.length; t1++) {
				conditionedPdf[t0] += firedPDF[t0] * firstPDF[t1] * first.getStep();
			}
		}

		for(int t=0; t < firstPDF.length; t++) {
			conditionedPdf[t] = conditionedPdf[t] / prob;
		}

		double[] resultCDF = computeCDFFromPDF(conditionedPdf, new BigDecimal("" + greatestTimeStep));

		return new NumericalValues(resultCDF, greatestTimeStep);
	}

	//Not generic version--> easier than the version with an unspecified version of dimension
	public static NumericalValues shiftAndProjectAndMinimum(NumericalValues fired, List<NumericalValues> others) {
		if(others.size() == 1) {
			return shiftAndProjectAndMinimumS1(fired, others.get(0));
		}
		throw new UnsupportedOperationException("Not yet supported joint distribution of dimension " + others.size());
	}

	private static NumericalValues shiftAndProjectAndMinimumS1(NumericalValues fired, NumericalValues first) {
		double[] firedPDF = computePDFFromCDF(fired.getValues(), new BigDecimal("" + fired.getStep()));
		double[] firstPDF = computePDFFromCDF(first.getValues(), new BigDecimal("" + first.getStep()));

		//Successor probability
		double p0 = 0;
		for (int t1 = 0; t1 < firstPDF.length; t1++) {
			for (int t2 = t1; t2 < firstPDF.length; t2++) {
				p0+= firedPDF[t1] * firstPDF[t2] * fired.getStep() * first.getStep();
			}
		}

		//Shift and project
		double[] shiftedPDF = new double[firstPDF.length];

		for (int t1 = 0; t1 < firstPDF.length; t1++) {
			for (int t0 = 0; t0 < firedPDF.length; t0++) {
				if(t1- t0 >= 0)//Guarantee to be in the domain where fired distribution fire first
					shiftedPDF[t1 - t0] += firedPDF[t0] * firstPDF[t1] * first.getStep();
			}
		}

		//Normalizing using p0
		for(int t=0; t < firstPDF.length; t++) {
			shiftedPDF[t] = shiftedPDF[t] / p0;
		}

		//Evaluate minimum
		//Do nothing here since its only a single r.v. distribution
		double[] resultCDF = computeCDFFromPDF(shiftedPDF, new BigDecimal("" + first.getStep()));

		return new NumericalValues(resultCDF, first.getStep());
	}

	/**
	 *Given N independent distributions, evaluate the probability that each of that firez first
	 *
	 * @param distributions
	 * @param regionTimeStep if >0.0 then all the distribution of sojourn time are rescaled to that step value
	 * @return
	 */
	public static List<Double> evaluateFireFirstProbabilities(List<NumericalValues> distributions, double regionTimeStep) {
		//FIXME Implement the cases where N>3 (e.g., repeatedly evaluate the probability that a variable is lower than another variable)
		if(distributions.size() == 0)
			return null;
		else if(distributions.size() == 1)
			return List.of(1.0);
		else if(distributions.size() == 2)
			return evaluateFireFirstProbabilitiesS2(distributions.get(0), distributions.get(1), regionTimeStep);
		else if(distributions.size() == 3)
			return evaluateFireFirstProbabilitiesS3(distributions.get(0), distributions.get(1), distributions.get(2), regionTimeStep);
		else
			throw new UnsupportedOperationException("Evaluation of successor probabilities for N>3 dimensions not yet implemented");
	}

	//can account for different timeSteps
	private static List<Double> evaluateFireFirstProbabilitiesS2(NumericalValues aCDF, NumericalValues bCDF, double regionTimeStep) {

		double[] aPDF;
		double[] bPDF;

		if(regionTimeStep>0) {
			NumericalValues aRescaledCDF = rescaleCDF(aCDF, regionTimeStep);
			NumericalValues bRescaledCDF = rescaleCDF(bCDF, regionTimeStep);
			aPDF = computePDFFromCDF(aRescaledCDF.getValues(), new BigDecimal(""+regionTimeStep));
			bPDF = computePDFFromCDF(bRescaledCDF.getValues(), new BigDecimal(""+regionTimeStep));
		} else {
			aPDF = computePDFFromCDF(aCDF.getValues(), new BigDecimal("" + aCDF.getStep()));
			bPDF = computePDFFromCDF(bCDF.getValues(), new BigDecimal("" + bCDF.getStep()));
		}

		double p0 = 0;
		for (int timeStepA = 0; timeStepA < bPDF.length; timeStepA++) {
			for (int timeStepB = 0; timeStepB < timeStepA; timeStepB++) {
				p0+= aPDF[timeStepB] * bPDF[timeStepA] * aCDF.getStep() * bCDF.getStep();
			}
		}
		return Arrays.asList(p0, 1-p0);
	}

	private static List<Double> evaluateFireFirstProbabilitiesS3(NumericalValues aCDF, NumericalValues bCDF, NumericalValues cCDF, double regionTimeStep) {
		//Evaluate A first
		NumericalValues minBC;

		if(regionTimeStep>0) {
			minBC = minCDFvar(Arrays.asList(bCDF, cCDF), regionTimeStep);
		}else {
			minBC = minCDF(Arrays.asList(bCDF, cCDF));
		}
		double pa = evaluateFireFirstProbabilitiesS2(aCDF, minBC, regionTimeStep).get(0);

		NumericalValues minAC;

		if(regionTimeStep>0) {
			minAC = minCDFvar(Arrays.asList(aCDF, cCDF), regionTimeStep);
		}else {
			minAC = minCDF(Arrays.asList(aCDF, cCDF));
		}

		double pb = evaluateFireFirstProbabilitiesS2(bCDF, minAC, regionTimeStep).get(0);

		return Arrays.asList(pa, pb, 1 - pa - pb);
	}
}
