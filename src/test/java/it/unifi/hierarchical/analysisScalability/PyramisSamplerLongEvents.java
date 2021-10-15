package it.unifi.hierarchical.analysisScalability;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.util.StringUtils;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.domain.DBMZone;
import org.oristool.math.expression.Expolynomial;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.Erlang;
import org.oristool.math.function.Function;
import org.oristool.math.function.GEN;
import org.oristool.simulator.samplers.ErlangSampler;
import org.oristool.simulator.samplers.ExponentialSampler;
import org.oristool.simulator.samplers.MetropolisHastings;
import org.oristool.simulator.samplers.Sampler;
import org.oristool.simulator.samplers.UniformSampler;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.example.hsmp_scalability.HSMP_JournalLongEvents;
import it.unifi.hierarchical.utils.StateUtils;
import it.unifi.hierarchical.model.State;


/**
 * Execution of calculations
 *  
 */
public class PyramisSamplerLongEvents {

	public static double[]	arrSave;
	public static int indexSave;


	public static final boolean sameExp = true;


	public static void sampleFromTime(int numberLeaves, long time, int parallel, int depth, int seq,boolean expolSame, RegionType lastB, int y) {


		for(int i=y*0;i<=10*(y+1);i++) {
			sample(numberLeaves,time, parallel,depth,seq,expolSame, lastB, i);
		}

	}



	public static void main(String[] args){
		for(int l=1;l<4;l++) {
			for(int last=0; last>-1;last--) {
				RegionType lastB = (last==1)? RegionType.FINAL : RegionType.EXIT;

				for(int parallel=3;parallel<4; parallel++) {
					for(int depth=2;depth<3;depth++) {
						for(int seq=3;seq<4;seq++) {
							//FIXME
							//if( !(parallel==2 && depth==1 && seq==2) )
							sample(l, 0,parallel,depth,seq,sameExp, lastB,0 );

						}}}}
		}
	}

	public static void sample(int numberLongLeaves,long timeSS, int parallel,int depth,int seq,boolean expolSame, RegionType lastB, int iteration){


		arrSave= new double[1000000];
		indexSave=0;

		long EndTime = 720000000;
		if(timeSS>0) {
			EndTime= timeSS;
		}

		long toEndTime = EndTime;

		//dove raccolgo
		HashMap<String,Double> probabs = new HashMap<>();

		HashMap<String,Double> probabsOld = new HashMap<>();


		Map<String,Sampler> samplers = new HashMap<>();

		initialize(samplers);

		HierarchicalSMP model = HSMP_JournalLongEvents.build(numberLongLeaves,parallel,depth,seq,true, lastB);
		State initial = model.getInitialState();

		//tutti gli stati NON exit/final (attenzione che gli stati final, dovendo aspettare possono non avere 0)
		Set<String> sList = HSMP_JournalLongEvents.getStates();



		for(String s: sList) {
			probabs.put(s, 0.0);
		}
		probabs.put("S0", 1.0);


		int i=0;
		boolean changed = true;
		int RUNS_AT_A_TIME=1000;

		while(toEndTime>0 && changed) {

			deepCopy(probabs,probabsOld);



			//System.out.println("in "+i);
			i++;
			//Analyze
			Date start = new Date();





			samplerOut(numberLongLeaves,probabs, samplers, RUNS_AT_A_TIME,initial, lastB);



			Date end = new Date();
			long time = end.getTime() - start.getTime();
			System.out.println("Sample:" + time + "ms");

			toEndTime-=time;

			changed = isChanged(probabs,probabsOld,i);

		}

		String it="";
		if(iteration>0) {
			it="_"+iteration;
		}
		String g="";
		if(timeSS>0) {
			g="src//main//resources//pyramisAnalyticSameTimeLong//"+"sameTime_";
		}else {

			g= "src//main//resources//pyramisSimulationLong//";
		}
		String print= g+"p-"+parallel+"_d-"+(depth+1)+"_s-"+seq+"_Final-"+lastB+"_l-"+numberLongLeaves+"_"+(i*RUNS_AT_A_TIME)+it+".txt";

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
			print=g="src//main//resources//groundTruthDistributions//distributionLong_"+g+"p-"+parallel+"_d-"+(depth+1)+"_s-"+seq+"_Final-"+lastB+"_l-"+numberLongLeaves+"_"+(i*RUNS_AT_A_TIME)+".txt";

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



	private static void initialize(Map<String,Sampler> samplers) {

		GEN exp = GEN.newExpolynomial("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2", OmegaBigDecimal.ZERO, OmegaBigDecimal.ONE);

		GEN exp10= GEN.newExpolynomial("0.22517 * Exp[-0.311427 x] * x + -0.022517 * Exp[-0.311427 x] * x^2", OmegaBigDecimal.ZERO, OmegaBigDecimal.TEN); 



		samplers.put("exp", new ExpolSampler(exp));
		samplers.put("exp10", new ExpolSampler(exp10));


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

		return changed;
	}

	public static int samplerOut(int leaves, Map<String,Double> probabs, Map<String,Sampler> samplers, int RUNS_AT_A_TIME,State initial, RegionType lastB) {

		int countDown = RUNS_AT_A_TIME;

		int c=0;
		while(countDown>0) {
			System.out.println(countDown);

			Map<State,Double> samp = new HashMap<State,Double>();
			Map<Region,Double> regSamp = new HashMap<Region,Double>();



			Set<State> compS = HSMP_JournalLongEvents.compS;

			Set<State> expS = HSMP_JournalLongEvents.expS;
			Set<State> fL = HSMP_JournalLongEvents.firstLeaf;
			Set<State> sL = HSMP_JournalLongEvents.secondLeaf;
			Set<State> tL= HSMP_JournalLongEvents.thirdLeaf;
			Map<State,Region> regMap = HSMP_JournalLongEvents.regMap;


			double d = Math.random();
			boolean expDiffBool = d<0.5;
			//inizializzo samp con i comp (contiene anche S0 ma va ignorato, in ogni caso è 0)
			for(State s: compS) {
				samp.put(s, 0.0);
			}

			//campiono valori per le foglie a tutti i livelli e le sommo alla reg
			for(State s: expS) {


				Sampler sampler;
				if(fL.contains(s) || (leaves>=2 && sL.contains(s)) || (leaves==3 && tL.contains(s))) {
					sampler=samplers.get("exp10");
				}else {
					sampler=samplers.get("exp");
				}



				Region rparent= regMap.get(s);
				if(!regSamp.containsKey(rparent)) {
					regSamp.put(rparent, 0.0);
				}

				State doub = HSMP_JournalLongEvents.doublesMap.get(s);
				State win;
				double v;
				if(!expDiffBool) {
					v = sampler.getSample().doubleValue();
					samp.put(s, v);
					samp.put(doub, 0.);
					win=s;
				}else {
					v= sampler.getSample().doubleValue();
					samp.put(s, 0.);
					samp.put(doub, v);
					win=doub;
				}

				regSamp.put(rparent, regSamp.get(rparent)+v);

				for(int q=1;q<HSMP_JournalLongEvents.sequenceS;q++) {
					win= win.getNextStates().get(0);
					if(!expDiffBool) {
						v = sampler.getSample().doubleValue();
						samp.put(win, v);
						samp.put(HSMP_JournalLongEvents.doublesMap.get(win), 0.);
					}else {
						v= sampler.getSample().doubleValue();
						samp.put(win, v);
						samp.put(HSMP_JournalLongEvents.doublesMapFrom2.get(win), 0.);

					}
					regSamp.put(rparent, regSamp.get(rparent)+v);
				}


			}


			// sommo i valori di comp in ordine dal basso ai genitoriRegione!!
			for(int i=HSMP_JournalLongEvents.depthS;i>=0;i--) {
				for(State s: compS) {
					if(s.getDepth()==i-1) {
						Double min=100000.;
						Double max=0.;
						for(int r=0; r< HSMP_JournalLongEvents.parallelS;r++) {
							Double val= regSamp.get(((CompositeState)s).getRegions().get(r));
							if(val<min) min=val;
							if(val>max) max=val;
						}						

						Region rparent= regMap.get(s);


						//final tutte assieme, exit la prima
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
			//qui sono giunto a S0 corretto
			Double timeToEnd = samp.get(initial);
			if(indexSave<arrSave.length) {
				arrSave[indexSave]=timeToEnd;
				indexSave++;
			}
			add(initial.getName(), timeToEnd, probabs);

			for(Region r: ((CompositeState)initial).getRegions()) {
				topDown(r.getInitialState(),r, timeToEnd, samp, probabs);
			}


			countDown--;

		}


		return c;
	}





	private static void topDown(State initQ, Region r, Double timeToEnd, Map<State,Double> samp,Map<String,Double> probabs ) {

		State init;
		Set<String> setZero =HSMP_JournalLongEvents.zeros;
		if(setZero.contains(initQ.getName())) {
			List<State> kl = initQ.getNextStates();

			init=samp.get(kl.get(0)) >0. ? kl.get(0) : kl.get(1);
		}else {
			init=initQ;
		}

		//attenzione allo zero, se depth diversa il primo è un composite
		if(init instanceof CompositeState  ) {
			if(samp.get(init)>timeToEnd) {
				add(init.getName(), timeToEnd, probabs);
				for(Region r1: ((CompositeState)init).getRegions()) {
					topDown(r1.getInitialState(), r1, timeToEnd, samp, probabs);
				}

			}else {
				add(init.getName(), samp.get(init), probabs);
				for(Region r1: ((CompositeState)init).getRegions()) {
					topDown(r1.getInitialState(),r1, samp.get(init), samp, probabs);
				}
				State win = null;
				double remaining= timeToEnd-samp.get(init);
				if(!(remaining>0.0)) {
					return;
				}

				List<State> list= init.getNextStates();

				win = samp.get(list.get(0)) >0. ? list.get(0) : list.get(1);


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

			//init è a depth max
		}else {


			State win =init;
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
