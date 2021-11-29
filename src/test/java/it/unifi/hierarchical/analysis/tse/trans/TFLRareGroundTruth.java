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

package it.unifi.hierarchical.analysis.tse.trans;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.function.Function;
import org.oristool.math.function.GEN;
import org.oristool.simulator.samplers.MetropolisHastings;
import org.oristool.simulator.samplers.Sampler;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.tse.trans.TFL;
import it.unifi.hierarchical.model.State;

/**
 * This class supports the derivation of a ground truth (through stochastic simulation)
 * for the HSMP models with rare events
 * used in the case study on transient timed failure logic analysis of component based systems 
 * of the paper titled "Compositional Analysis of Hierarchical UML Statecharts".
 */
public class TFLRareGroundTruth {

	public static double[]	arrSave;
	public static int indexSave;
	public static final boolean sameExp = true;

	public static void sampleFromTime(long time, int parallel, int depth, int seq,boolean expolSame, RegionType lastB, int x, int y) {

		int q=x;
		for(int i=q*y;i<(q+1)*y;i++) {
			sample(time, parallel,depth,seq,expolSame, lastB, i);
		}
	}

	public static void main(String[] args){

		for(int last=0; last>-1;last--) {
			RegionType lastB = (last==1)? RegionType.FINAL : RegionType.EXIT;

			for(int parallel=2;parallel<5; parallel++) {
				for(int depth=1;depth<4;depth++) {
					for(int seq=2;seq<5;seq++) {
						sample(0,parallel,depth,seq,sameExp, lastB, 0);

					}}}}

	}

	public static void sample(long timeSS, int parallel,int depth,int seq,boolean expolSame, RegionType lastB, int iteration){

		arrSave= new double[1000000];
		indexSave=0;

		long EndTime = 720000000;
		if(timeSS>0) {
			EndTime= timeSS;
		}

		long toEndTime = EndTime;

		// result
		HashMap<String,Double> probabs = new HashMap<>();

		HashMap<String,Double> probabsOld = new HashMap<>();

		Map<String,Sampler> samplers = new HashMap<>();
		initialize(samplers);

		HierarchicalSMP model = TFL.build(parallel,depth,seq,true, lastB);
		State initial = model.getInitialState();

		// all the steps (i.e., not the final locations)
		Set<String> sList = TFL.getStates();

		for(String s: sList) {
			probabs.put(s, 0.0);
		}
		probabs.put("S0", 1.0);

		int i=0;
		boolean changed = true;
		int RUNS_AT_A_TIME=1000;

		while(toEndTime>0 && changed) {

			deepCopy(probabs,probabsOld);

			i++;
			//Analyze
			Date start = new Date();

			samplerOut(probabs, samplers, RUNS_AT_A_TIME,initial, lastB);

			Date end = new Date();
			long time = end.getTime() - start.getTime();
			System.out.println("Sample:" + time + "ms");

			toEndTime-=time;

			changed = isChanged(probabs,probabsOld,i);

		}
		String g="";
		String it="";
		if(iteration>0) {
			it="_"+iteration;
		}
		if(timeSS>0) {
			g="src//main//resources//pyramisAnalyticSameTimeRare//"+"sameTime_";
		}else {

			g= "src//main//resources//pyramisSimulationRare//";
		}
		String print= g+"p-"+parallel+"_d-"+(depth+1)+"_s-"+seq+"_Final-"+lastB+"_"+(i*RUNS_AT_A_TIME)+it+".txt";

		File file = new File(print);
		file.getParentFile().mkdirs();
		
		try (PrintWriter writer = new PrintWriter(file)) {

			writer.write("TIME=  "		+((EndTime-toEndTime))	+"ms \n\n");

			for(String s: sList) {

				int n = s.length();
				String x = s;
				for(int r=0;r<10-n;r++) {
					x=x+" ";
				}

				writer.write(x+ (probabs.get(s)/probabs.get("S0") )+"\n");
			}
			writer.write("\n");


		} catch (FileNotFoundException e) {
			System.out.println("errore");
			System.out.println(e.getMessage());
		}
		if(timeSS==0) {
			print=g="src//main//resources//groundTruthDistributions//distributionRare_"+g+"p-"+parallel+"_d-"+(depth+1)+"_s-"+seq+"_Final-"+lastB+"_"+(i*RUNS_AT_A_TIME)+".txt";

			file = new File(print);
			try (PrintWriter writer = new PrintWriter(file)) {

				for(int ii=0;ii<arrSave.length;ii++) {
					if(arrSave[ii]>0.)
						writer.write(arrSave[ii]+" ");
				}

			} catch (FileNotFoundException e) {
				System.out.println("errore");
				System.out.println(e.getMessage());
			}

		}
	}


	@SuppressWarnings("deprecation")
	private static void deepCopy(Map<String,Double> probabs,Map<String,Double> probOld) {
		for(String s: probabs.keySet()) {
			probOld.put(s, new Double(probabs.get(s)));
		}

	}


	private static void add(String s,Double v, Map<String,Double> m) {
		Double d = v+m.get(s);
		m.put(s, d);
	}

//	private static class DetSampler implements Sampler {
//
//		private double det;
//		public DetSampler(double f) {
//			this.det = f;
//		}
//
//		@Override
//		public BigDecimal getSample() {
//
//			return new BigDecimal(det);
//		}
//	}

	private static class ExpolSampler implements Sampler {

		private MetropolisHastings mp;
		public ExpolSampler(Function f) {
			this.mp = new MetropolisHastings(f);
		}

		@Override
		public BigDecimal getSample() {
			return mp.getSample();
		}
	}

//	private static class ErlExpSampler implements Sampler {
//		private Sampler erl;
//		private Sampler exp;
//
//		public ErlExpSampler(int shape, BigDecimal rateErl, BigDecimal rateExp) {
//			this.erl = new ErlangSampler(new Erlang(Variable.X,shape, rateErl));
//			this.exp = new ExponentialSampler(rateExp);
//		}
//
//		@Override
//		public BigDecimal getSample() {
//
//			BigDecimal sample = erl.getSample();
//			sample = sample.add(exp.getSample());
//
//			return sample;
//		}
//	}


//	private static class ExponErlangSampler implements Sampler {
//
//		private Sampler samp1;
//		private Sampler samp2;
//
//		public ExponErlangSampler(Sampler s1, Sampler s2) {
//			this.samp1 = s1;
//			this.samp2=s2;
//		}
//
//		public ExponErlangSampler(double s1, double s2) {
//			this.samp1 = new ExponentialSampler(new BigDecimal(s1));
//			this.samp2= new ExponentialSampler(new BigDecimal(s2));
//
//		}
//
//		@Override
//		public BigDecimal getSample() {
//			return samp1.getSample().add(samp2.getSample());
//		}
//	}


	private static void initialize(Map<String,Sampler> samplers) {

		GEN exp = GEN.newExpolynomial("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2", OmegaBigDecimal.ZERO, OmegaBigDecimal.ONE);

		GEN expDiff;

		if(sameExp)
			expDiff= exp;
		else {
			expDiff = GEN.newExpolynomial("0.22517 * Exp[-0.311427 x] * x + -0.022517 * Exp[-0.311427 x] * x^2", OmegaBigDecimal.ZERO, OmegaBigDecimal.TEN); 
		}


		samplers.put("exp", new ExpolSampler(exp));
		samplers.put("expDiff", new ExpolSampler(expDiff));


	}

	private static boolean isChanged(Map<String,Double> probabs,Map<String,Double> probOld, int i) {
		boolean changed =false;

		for(String s: probabs.keySet()) {

			if( Math.abs(   (((probabs.get(s)/probabs.get("S0") ))-(probOld.get(s)/probOld.get("S0")))  /(probabs.get(s)/probabs.get("S0") )) >0.0001) {

				if(i%10==0)
					System.out.println(Math.abs(   ((probabs.get(s))-(probOld.get(s)))/probabs.get(s)));
				changed=true;
				break;
			}

		}
		if(!changed) {
			for(String s: probabs.keySet()) {

				System.out.println( Math.abs((((probabs.get(s)/probabs.get("S0") ))-(probOld.get(s)/probOld.get("S0")))  /(probabs.get(s)/probabs.get("S0") )));
				System.out.println(probabs.get(s)+" "+probabs.get(s)/probabs.get("S0")+" -- "+probabs.get(s)+" "+probOld.get(s)/probOld.get("S0"));
				System.out.println(probabs.get("S0"));
			}
		}
		return changed;
	}

	public static int samplerOut(Map<String,Double> probabs, Map<String,Sampler> samplers, int RUNS_AT_A_TIME,State initial, RegionType lastB) {

		int countDown = RUNS_AT_A_TIME;

		int c=0;
		while(countDown>0) {
			System.out.println(countDown);

			Map<State,Double> samp = new HashMap<State,Double>();
			Map<Region,Double> regSamp = new HashMap<Region,Double>();

			Set<State> compS = TFL.compS;

			Set<State> expS = TFL.expS;
//			Set<State> expDiffS = HSMP_Scalability.expDiffS;
			Map<State,Region> regMap = TFL.regMap;

			double d = Math.random();
			boolean expDiffBool = d<0.005;
			// initialize samp with the composite steps (it includes S0 which must be ignored, anyway it is associated with zero)
			for(State s: compS) {
				samp.put(s, 0.0);
			}

			// accumulate values sampled for the leaves at any level
			for(State s: expS) {
				Region rparent= regMap.get(s);
				if(!regSamp.containsKey(rparent)) {
					regSamp.put(rparent, 0.0);
				}

				State doub = TFL.doublesMap.get(s);
				State win;
				double v;
				if(!expDiffBool) {
					v = samplers.get("exp").getSample().doubleValue();
					samp.put(s, v);
					samp.put(doub, 0.);
					win=s;
				}else {
					v= samplers.get("expDiff").getSample().doubleValue();
					samp.put(s, 0.);
					samp.put(doub, v);
					win=doub;
				}

				regSamp.put(rparent, regSamp.get(rparent)+v);

				for(int q=1;q<TFL.sequenceS;q++) {
					win= win.getNextStates().get(0);
					if(!expDiffBool) {
						v = samplers.get("exp").getSample().doubleValue();
						samp.put(win, v);
						samp.put(TFL.doublesMap.get(win), 0.);
					}else {
						v= samplers.get("expDiff").getSample().doubleValue();
						samp.put(win, v);
						samp.put(TFL.doublesMapFrom2.get(win), 0.);

					}
					regSamp.put(rparent, regSamp.get(rparent)+v);
				}


			}

			// accumulate values in bottom-up order
			for(int i=TFL.depthS;i>=0;i--) {
				for(State s: compS) {
					if(s.getDepth()==i-1) {
						Double min=100000.;
						Double max=0.;
						for(int r=0; r< TFL.parallelS;r++) {
							Double val= regSamp.get(((CompositeState)s).getRegions().get(r));
							if(val<min) min=val;
							if(val>max) max=val;
						}						

						Region rparent= regMap.get(s);

						if(lastB == RegionType.FINAL) {
							samp.put(s,max);	
							if(s.getDepth()>0)
								regSamp.put(rparent, regSamp.get(rparent)+max);
						}else if(lastB== RegionType.EXIT) {
							samp.put(s, min);
							if(s.getDepth()>0)
								regSamp.put(rparent, regSamp.get(rparent)+min);
						}


					}
				}

			}

			// Here the evaluaton of S0 is correct
			Double timeToEnd = samp.get(initial);
			if(indexSave<arrSave.length) {
				arrSave[indexSave]=timeToEnd;
				indexSave++;
			}
			add(initial.getName(), timeToEnd, probabs);

			for(Region r: ((CompositeState)initial).getRegions()) {
				topDown(r, timeToEnd, samp, probabs);
			}

			countDown--;
		}
		return c;
	}

	private static void topDown(Region r, Double timeToEnd, Map<State,Double> samp,Map<String,Double> probabs ) {

		State init = r.getInitialState();

		// If the depth is larger than zero, the first step is a composite step
		if(init.getDepth()!=TFL.depthS) {
			if(samp.get(init)>timeToEnd) {
				add(init.getName(), timeToEnd, probabs);
				for(Region r1: ((CompositeState)init).getRegions()) {
					topDown(r1, timeToEnd, samp, probabs);
				}

			}else {
				add(init.getName(), samp.get(init), probabs);
				for(Region r1: ((CompositeState)init).getRegions()) {
					topDown(r1, samp.get(init), samp, probabs);
				}

				double remaining= timeToEnd-samp.get(init);
				List<State> list= init.getNextStates();

				State win = samp.get(list.get(0)) >0. ? list.get(0) : list.get(1);

				while(remaining>0) {

					if(samp.get(win)>remaining) {
						add(win.getName(), remaining, probabs);
						break;
					}
					add(win.getName(), samp.get(win), probabs);
					remaining=remaining-samp.get(win);
					if(win.getNextStates().get(0) instanceof FinalState ||  win.getNextStates().get(0) instanceof ExitState) {
						break;
					}
					win=win.getNextStates().get(0);
				}
			}
			// Init is at maximum depth
		}else {

			List<State> list= init.getNextStates();

			State win = samp.get(list.get(0)) >0. ? list.get(0) : list.get(1);
			Double remaining = timeToEnd;

			while(remaining>0) {

				if(samp.get(win)>remaining) {
					add(win.getName(), remaining, probabs);
					break;
				}
				add(win.getName(), samp.get(win), probabs);
				remaining=remaining-samp.get(win);
				if(win.getNextStates().get(0) instanceof FinalState ||  win.getNextStates().get(0) instanceof ExitState) {
					break;
				}
				win=win.getNextStates().get(0);
			}
		}
	}
}