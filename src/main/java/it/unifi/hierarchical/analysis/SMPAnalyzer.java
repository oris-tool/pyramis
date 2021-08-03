package it.unifi.hierarchical.analysis;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.oristool.math.OmegaBigDecimal;

import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.utils.NumericalUtils;

public class SMPAnalyzer implements TransientAnalyzer{

	private List<State> states;
	private double[][][] probs;
	private double timeLimit;
	private double timeStep;
	private State absorbingState;
	
	private static long timeElapsed=0;

//absorbing state NON serve a niente, in quanto quando viene passato si è avuta una chiamata a SMPAWithBorderExit che ha reso lo stato assorbente un selflooping
	public SMPAnalyzer(List<State> states, Map<State, NumericalValues> sojournTimeDistributions, double timeLimit, double timeStep) {
		this(states, sojournTimeDistributions, timeLimit, timeStep, null);
	}

	public SMPAnalyzer(List<State> states, Map<State, NumericalValues> sojournTimeDistributions, double timeLimit, double timeStep, State absorbingState) {
		
		
		this.states = states;
		this.timeLimit = timeLimit;
		this.timeStep = timeStep;
		this.absorbingState = absorbingState;
		int ticks = NumericalUtils.computeStepNumber(new OmegaBigDecimal(""+timeLimit), new BigDecimal(""+timeStep));

		
		System.out.println("RandomStateInRegion "+states.get(0).getName());
		
		System.out.println(states.size()+" "+ ticks);
		
		Date start = new Date();
			
			
		
		Date d2 = new Date();
		
		long time;
		//Evaluate kernel
		double[][][] kernel = new double[states.size()][states.size()][ticks];

		for(int i = 0; i < states.size(); i++) {//From

			State from = states.get(i);

			NumericalValues sojourn = sojournTimeDistributions.get(from);
			double[] sojournDistrib;
			if(sojourn == null) { //absorbing state
				sojournDistrib = new double[ticks];
				//returns all ticks as 0 for current state, i in the kernel, not adding anything
			}else {

				sojournDistrib = sojourn.getValues();

				for(int q=0; q < from.getNextStates().size(); q++) {//To
					State to = from.getNextStates().get(q);
					int j = states.indexOf(to);
					double p_ij = from.getBranchingProbs().get(q);

					for(int t = 0; t < ticks; t++) {
						kernel[i][j][t] = p_ij * sojournDistrib[t];
					}
				}
			}
		}
			
		Date d3 = new Date();
		
		time = d3.getTime() - d2.getTime();
		System.out.println(time+ "  kernelQQQ");
	
		
		//FIXME qui mette direttamente a 1 la prob di essere al tempo zero in se stesso, mentre per immediate forse
		//vuole 0? e 1 sui finali?
		
		//Init transient probs array
		probs = new double[states.size()][states.size()][ticks];
		for (int i = 0; i < probs.length; i++) {
			probs[i][i][0] = 1;
		}

	
		//Evaluate transient probabilities of the SMP
		//REMARK only a single step is allowed in a tick, Multiple Det(0) are likely cause of errors
		for(int t = 1; t < ticks; t++) {
			for(int i = 0; i < states.size(); i++) {//From
				State from = states.get(i);

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
							
								
							probs[i][j][t]+=  
									(kernel[i][k][u] -lastKernelProb)* 
									probs[k][j][t - u];    
							
														
						}
					}
				}
			}
		}
		
		Date d4 = new Date();
		
		time = d4.getTime() - d3.getTime();
		System.out.println(time+ "  transQQQ");
	
		

		Date end = new Date();
		timeElapsed += end.getTime() - start.getTime();
		System.out.println("(ms): " + timeElapsed );
		
	}

	@Override
	public List<State> getStates() {
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

	public State getAbsorbingState() {
		return absorbingState;
	}

	@Override
	public NumericalValues getProbsFromTo(State from, State to) {
		int fromStateIndex = states.indexOf(from);
		int toStateIndex = states.indexOf(to);

		double[] result = probs[fromStateIndex][toStateIndex];
		return new NumericalValues(result, timeStep);
	}

}
