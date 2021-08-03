package it.unifi.hierarchical.model.example.Tompecs;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.function.Function;
import org.oristool.math.function.GEN;
import org.oristool.simulator.samplers.MetropolisHastings;
import org.oristool.simulator.samplers.Sampler;

import it.unifi.hierarchical.analysis.HierarchicalSMPAnalysis;
import it.unifi.hierarchical.analysis.NumericalValues;
import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.utils.StateUtils;
import it.unifi.hierarchical.model.State;


/**
 * Execution of calculations
 *  
 */
public class PyramisSamplerCyclesFromFiles {

	public static double[]	arrSave;
	public static int indexSave;
	
	public static double[] cdf;
	public static double step=0.0025;

	public static final boolean sameExp = true;


	public static void sampleFromTime(int LOOPS, int numberLeaves, long time, int parallel, int depth, int seq,boolean expolSame, RegionType lastB, int y) {


		for(int i=y*0;i<=20*(y+1);i++) {
			sample(8, numberLeaves, time, parallel,depth,seq,expolSame, lastB, i);
		}

	}



	public static void main(String[] args){

		int x =Integer.valueOf(args[0]);
		
		for(int LOOPS=x;LOOPS<x+1;LOOPS++) {
			for(int l=3;l<4;l++) {
				for(int last=0; last>-1;last--) {
					RegionType lastB = (last==1)? RegionType.FINAL : RegionType.EXIT;

					for(int parallel=3;parallel<4; parallel++) {
						for(int depth=2;depth<3;depth++) {
							for(int seq=3;seq<4;seq++) {
								//if( !(parallel==2 && depth==1 && seq==2) )
								sample(LOOPS, l, 0,parallel,depth,seq,sameExp, lastB,0);

							}}}}
			}
		}}

	public static void sample(int LOOPS, int numberLoopsPerLeaves,long timeSS, int parallel,int depth,int seq,boolean expolSame, RegionType lastB, int iteration){

		cdf=new double[4000];

		arrSave= new double[1000000];
		indexSave=0;

		long toEndTime = 1;
		if(timeSS>0) {
			toEndTime= timeSS;
		}

	
		//dove raccolgo
		HashMap<String,Double> probabs = new HashMap<>();

		HashMap<String,Double> probabsOld = new HashMap<>();

		File folder = new File("..//tsamples");
		//new File("..//tsamples");
		
		List<File> fileList = Arrays.asList(folder.listFiles());


		Sampler sampler = new ExpolSampler(fileList);




		HierarchicalSMP model = HSMP_JournalCycles.build(numberLoopsPerLeaves,parallel,depth,seq,true, lastB, LOOPS);
		State initial = model.getInitialState();

		//tutti gli stati NON exit/final (attenzione che gli stati final, dovendo aspettare possono non avere 0)
		Set<String> sList = HSMP_JournalCycles.getStates();



		for(String s: sList) {
			probabs.put(s, 0.0);
		}
		probabs.put("S0", 1.0);


		int i=0;
		boolean changed = true;
		int RUNS_AT_A_TIME=1000000;

		while(toEndTime>0 && changed) {

			deepCopy(probabs,probabsOld);


			i++;
			//Analyze
			Date start = new Date();





			samplerOut(numberLoopsPerLeaves,probabs, sampler, RUNS_AT_A_TIME,initial, lastB);



			Date end = new Date();
			long time = end.getTime() - start.getTime();
			System.out.println("Sample:" + time + "ms");

			toEndTime-=time;

			changed = isChanged(probabs,probabsOld,i);


			
			String g="";
			if(timeSS>0)
				g="sameTime_";

			String print= g+ "cycle_L"+LOOPS+"p-"+parallel+"_d-"+(depth+1)+"_s-"+seq+"_Final-"+lastB+"_l-"+numberLoopsPerLeaves+"_"+(i*RUNS_AT_A_TIME)+".txt";

			File file = new File(print);
			try (PrintWriter writer = new PrintWriter(file)) {

				//writer.write("TIME=  "		+((EndTime-toEndTime))	+"ms \n\n");



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


			file = new File("cdf_"+LOOPS+".txt");
			try (PrintWriter writer = new PrintWriter(file)) {


				for(int q=0;q<cdf.length;q++) {


					writer.write(q*step+" "+(cdf[q]/(i*RUNS_AT_A_TIME))   +"\n");

				}






			} catch (FileNotFoundException e) {
				System.out.println("errore");
				System.out.println(e.getMessage());
			}




		}
		String it="";
		if(iteration>0) {
			it="_"+iteration;
		}
		String g="";
		if(timeSS>0)
			g="sameTime_";

		String print= g+ "cycle_L"+LOOPS+"p-"+parallel+"_d-"+(depth+1)+"_s-"+seq+"_Final-"+lastB+"_l-"+numberLoopsPerLeaves+"_"+(i*RUNS_AT_A_TIME)+"_"+it+".txt";

		File file = new File(print);
		try (PrintWriter writer = new PrintWriter(file)) {

			//writer.write("TIME=  "		+((EndTime-toEndTime))	+"ms \n\n");



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




	}


	private static class ExpolSampler implements Sampler {

		private List<File> lf;
		private double[] arr;
		private int index;
		private int read=0;
		private int ind;
		private int total;
		public ExpolSampler(List<File> lf) {
			index=0;
			ind=0;
			total= lf.size();
			this.lf = lf;
			arr= new double[100000000];

			load();


		}

		@Override
		public BigDecimal getSample() {

			if(read>=ind-10) {
				load();
			}
			if(read>=99999900) {
				read=0;
			}


			read++;
			return BigDecimal.valueOf(arr[read]);
		}

		public void load() {

			BufferedReader reader = null;
			if(index>=total) {
				index=0;
			}
			if(ind>=99999999) {
				ind=0;
			}
			try {
				File file = lf.get(index);
				reader = new BufferedReader(new FileReader(file));

				String line;
				while ((line = reader.readLine()) != null) {
					String[] lines = line.split(" +");
					for(String s: lines) {
						arr[ind]=Double.valueOf(s);
						ind++;
					}

				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			index++;

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


	private static class DetSampler implements Sampler {

		private double det;
		public DetSampler(double f) {

			this.det = f;

		}

		@Override
		public BigDecimal getSample() {

			return new BigDecimal(det);
		}


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

	public static int samplerOut(int leaves, Map<String,Double> probabs, Sampler samplerS, int RUNS_AT_A_TIME,State initial, RegionType lastB) {

		int countDown = RUNS_AT_A_TIME;

		int c=0;
		while(countDown>0) {

			Map<State,Double> samp = new HashMap<State,Double>();
			Map<Region,Double> regSamp = new HashMap<Region,Double>();



			Set<State> compS = HSMP_JournalCycles.compS;

			Set<State> expS = HSMP_JournalCycles.expS;
			Set<State> det = HSMP_JournalCycles.det;

			LinkedList<State> fL = HSMP_JournalCycles.firstLeaf;
			LinkedList<State> sL = HSMP_JournalCycles.secondLeaf;
			LinkedList<State> tL= HSMP_JournalCycles.thirdLeaf;

			Map<State,Region> regMap = HSMP_JournalCycles.regMap;


			//inizializzo samples con TUTTI i composite (contiene anche S0 ma va ignorato, in ogni caso è 0)
			for(State s: compS) {
				samp.put(s, 0.0);
			}


			for(State s: det) {



				Region rparent= regMap.get(s);
				if(!regSamp.containsKey(rparent)) {
					regSamp.put(rparent, 0.0);
				}

				samp.put(s, 0.750);


				regSamp.put(rparent, regSamp.get(rparent)+0.750);

			}

			//campiono valori per le foglie a tutti i livelli e le sommo alla regioni corrispodenti
			for(State s: expS) {
				
				double d = Math.random();
				boolean expDiffBool = d<0.5;
				

				Sampler sampler=samplerS;




				Region rparent= regMap.get(s);
				if(!regSamp.containsKey(rparent)) {
					regSamp.put(rparent, 0.0);
				}

				State doub = HSMP_JournalCycles.doublesMap.get(s);
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

				for(int q=1;q<HSMP_JournalCycles.sequenceS;q++) {
					win= win.getNextStates().get(0);
					if(!expDiffBool) {
						v = sampler.getSample().doubleValue();
						samp.put(win, v);
						samp.put(HSMP_JournalCycles.doublesMap.get(win), 0.);
					}else {
						v= sampler.getSample().doubleValue();
						samp.put(win, v);
						samp.put(HSMP_JournalCycles.doublesMapFrom2.get(win), 0.);

					}
					regSamp.put(rparent, regSamp.get(rparent)+v);
				}


			}

			// sommo i valori di comp in ordine dal basso ai genitoriRegione!!
			for(int i=HSMP_JournalCycles.depthS+1;i>=0;i--) {
				for(State s: compS) {

					boolean sCycle = (fL.contains(s)|| (sL.contains(s)&&leaves>=2)||(tL.contains(s)&&leaves==3));


					if(s.getDepth()==i-1) {



						Double min=100000.;
						Double max=0.;

						if(!sCycle) {
							for(int r=0; r< HSMP_JournalCycles.parallelS;r++) {
								Double val= regSamp.get(((CompositeState)s).getRegions().get(r));
								if(val<min) min=val;
								if(val>max) max=val;
							}
							Region rparent= regMap.get(s);
							if(!regSamp.containsKey(rparent)) {
								regSamp.put(rparent, 0.0);
							}

							//final tutte assieme, exit la prima
							if(lastB == RegionType.FINAL) {
								samp.put(s,max);	
								if(s.getDepth()>0) {
									regSamp.put(rparent, regSamp.get(rparent)+max);
								}
							}else if(lastB== RegionType.EXIT) {
								samp.put(s, min);
								if(s.getDepth()>0)
									regSamp.put(rparent, regSamp.get(rparent)+min);
							}




						}else if(fL.get(0).equals(s)||(leaves>=2 && sL.get(0).equals(s))||(leaves==3 && tL.get(0).equals(s))){

							LinkedList<State> l;
							State cr=s;
							int ind=0;
							if(fL.get(0).equals(s)) {
								l=fL;
							}
							else if(sL.get(0).equals(s)) {
								l=sL;
							}else {
								l=tL;
							}
							Double val= regSamp.get(((CompositeState)s).getRegions().get(0));

							
							Region rparent= regMap.get(s);

							while(val>0.750 && ind<l.size()-1) {

								samp.put(cr, 0.750);
								ind++;
								cr=l.get(ind);
								val = regSamp.get(((CompositeState)cr).getRegions().get(0));
								regSamp.put(rparent, regSamp.get(rparent)+0.750);
							}
							double x=val<0.750?val:0.750;
							samp.put(cr, x);
							regSamp.put(rparent, regSamp.get(rparent)+x);

							
						}




					}
				}

			}
			//qui sono giunto a S0 corretto
			Double timeToEnd = samp.get(initial);
			
			
			int slot= (int) Math.floor(timeToEnd/step);
			while(slot<cdf.length) {
				cdf[slot]+=1;
				slot++;
			}
			
			if(indexSave<arrSave.length) {
				arrSave[indexSave]=timeToEnd;
				indexSave++;
			}
			add(initial.getName(), timeToEnd, probabs);

			for(Region r: ((CompositeState)initial).getRegions()) {
				State init = r.getInitialState();

				topDown(init,r, timeToEnd, samp, probabs);
			}


			countDown--;

		}


		return c;
	}





	private static void topDown(State initQ, Region r, Double timeToEnd, Map<State,Double> samp,Map<String,Double> probabs ) {

		State init;
		Set<String> setZero =HSMP_JournalCycles.zeros;
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
				if(HSMP_JournalCycles.loopsMap.containsKey(init)) {

					win = HSMP_JournalCycles.loopsMap.get(init);
					topDown(win, r, remaining, samp, probabs);
					return;

				}else if(StateUtils.isCompositeWithBorderExit(init)){
					for(State exitState : ((CompositeState)init).getNextStatesConditional().keySet()) {
						List<State> stL = ((CompositeState)init).getNextStatesConditional().get(exitState);
						win = samp.get(stL.get(1)) >0. ? stL.get(1) : stL.get(0);
						break;
					}
				}else {
					List<State> list= init.getNextStates();

					win = samp.get(list.get(0)) >0. ? list.get(0) : list.get(1);
				}

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
