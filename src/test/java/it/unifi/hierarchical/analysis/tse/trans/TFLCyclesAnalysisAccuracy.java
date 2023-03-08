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

package it.unifi.hierarchical.analysis.tse.trans;

import it.unifi.hierarchical.model.CompositeStep;
import it.unifi.hierarchical.model.HSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.Step;
import it.unifi.hierarchical.model.tse.trans.TFLCycles;
import it.unifi.hierarchical.utils.StateUtils;

import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class supports the comparison of the analysis results with the ground truth
 * for the HSMP models with cycles
 * used in the case study on transient timed failure logic analysis of component based systems 
 * of the paper titled "Compositional Analysis of Hierarchical UML Statecharts" (see Figure 4).
 */
public class TFLCyclesAnalysisAccuracy {

	public TFLCyclesAnalysisAccuracy() {
	}

	public static final boolean extraStates=false;

	public static void main(String[] args){
		DecimalFormat formatter = new DecimalFormat("#0.000000000000000");
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		formatter.setDecimalFormatSymbols(symbols);

		String[] pathnamesSample;

		File fDirec = new File("src//main//resources//pyramisSimulationCycles");

		// Populate the array with the names of files and directories
		pathnamesSample = fDirec.list();

		if(pathnamesSample==null) {
			System.out.println("Directory src/main/resources/pyramisSimulationCycles is missing.\n"
					+ "Follow README.md to execute each step needed to repeat the experiments.");
			return;
		}
		
		if(pathnamesSample.length==0) {
			System.out.println("Directory empty: I was expecting a file in "+"src//main//resources//pyramisSimulationCycles");
			return;
		}
		
		for(String pathnameS: pathnamesSample) {
			System.out.println(pathnameS);

			// prefix of cycles names is 8 characters
			int xx=8;

			int p = Integer.valueOf(pathnameS.substring(2+xx, 3+xx));
			int dep= Integer.valueOf(pathnameS.substring(6+xx, 7+xx));
			int ss= Integer.valueOf(pathnameS.substring(10+xx, 11+xx));
			int rp= Integer.valueOf(pathnameS.substring(15+xx, 16+xx));
			int Final= pathnameS.contains("EXIT")? 0:1;

			int l;
			if(	pathnameS.contains("EXIT_l-1") ) {
				l=1;
			}else if(pathnameS.contains("EXIT_l-2") ) {
				l=2;
			}else {
				l=3;
			}

			double[] rpA= {0.5,0.75,1.,1.5};
			double rpV= rpA[rp];

//			Map<String,String> Aliases= new HashMap<>();
			Map<String,String> searchAliases;
			
			@SuppressWarnings("unchecked")
			Map<String,String>[] store=new Map[10]; 

			for(int i=2;i<10;i++) {
				
				searchAliases= new HashMap<>();
				store[i]=searchAliases;

				System.out.println("nide");
				HSMP model1 = TFLCycles.build(rpV,l,3,2,3,true, RegionType.EXIT, i);
				Step initial1 = model1.getInitialState();

				searchAliases.put(initial1.getName(), "s0");
				for(int r=0;r<3;r++) {
					
					Region reg = ((CompositeStep) initial1).getRegions().get(r);

					Step in = reg.getInitialState();
					searchAliases.put(in.getName(), r+"in");
					if(in instanceof CompositeStep) {
						for(int r2=0;r2<3;r2++) {
							Region reg2 = ((CompositeStep) in).getRegions().get(r2);
							Step in2 =reg2.getInitialState();
							searchAliases.put(in2.getName(), r2+"_"+r+"in");
							Step a = in2.getNextStates().get(0);
							Step b = in2.getNextStates().get(1);
							Step c = a.getNextStates().get(0);
							Step d = b.getNextStates().get(0);
							Step e = c.getNextStates().get(0);
							Step f = d.getNextStates().get(0);

							searchAliases.put(a.getName(), r2+"_"+r+"ain");
							searchAliases.put(b.getName(), r2+"_"+r+"bin");
							searchAliases.put(c.getName(), r2+"_"+r+"cin");
							searchAliases.put(d.getName(), r2+"_"+r+"din");
							searchAliases.put(e.getName(), r2+"_"+r+"ein");
							searchAliases.put(f.getName(), r2+"_"+r+"fin");
						}
						Step a = in.getNextStates().get(0);
						Step b = in.getNextStates().get(1);
						Step c = a.getNextStates().get(0);
						Step d = b.getNextStates().get(0);
						Step e = c.getNextStates().get(0);
						Step f = d.getNextStates().get(0);
						searchAliases.put(a.getName(), r+"ain");
						searchAliases.put(b.getName(), r+"bin");
						searchAliases.put(c.getName(), r+"cin");
						searchAliases.put(d.getName(), r+"din");
						searchAliases.put(e.getName(), r+"ein");
						searchAliases.put(f.getName(), r+"fin");

					}else {
						Step loop= in.getNextStates().get(0);
						System.out.println(r+ "loop");
						
						searchAliases.put(loop.getName(), r+"loopComp");

						Step cycleIn = ((CompositeStep)loop).getRegions().get(0).getInitialState();
						searchAliases.put(cycleIn.getName(), r+"cycleIn");

						for(int rC=0;rC<3;rC++) {
							Region regCyc = ((CompositeStep) cycleIn).getRegions().get(rC);

							Step inIn = regCyc.getInitialState();
							searchAliases.put(inIn.getName(), r+"inCyc"+rC+"in");

							Step a = inIn.getNextStates().get(0);
							Step b = inIn.getNextStates().get(1);
							Step c = a.getNextStates().get(0);
							Step d = b.getNextStates().get(0);
							Step e = c.getNextStates().get(0);
							Step f = d.getNextStates().get(0);
							searchAliases.put(e.getName(), r+"inCyc"+rC+"ein");
							searchAliases.put(f.getName(),r+"inCyc"+rC+"fin");
							searchAliases.put(a.getName(), r+"inCyc"+rC+"ain");
							searchAliases.put(b.getName(), r+"inCyc"+rC+"bin");
							searchAliases.put(c.getName(), r+"inCyc"+rC+"cin");
							searchAliases.put(d.getName(), r+"inCyc"+rC+"din");
						}

						Step det = ((CompositeStep)loop).getRegions().get(1).getInitialState();
						searchAliases.put(det.getName(), r+"det");

						Map<Step, List<Step>>  mapCyc =((CompositeStep)loop).getNextStatesConditional();
						List<Step> list2= mapCyc.get(StateUtils.findEndState(((CompositeStep)loop).getRegions().get(0)));
						List<Step> list1= mapCyc.get(StateUtils.findEndState(((CompositeStep)loop).getRegions().get(1)));

						Step a = list2.get(0);
						Step b = list2.get(1);
						Step c = a.getNextStates().get(0);
						Step d = b.getNextStates().get(0);
						Step e = c.getNextStates().get(0);
						Step f = d.getNextStates().get(0);

						searchAliases.put(a.getName(), r+"ain");
						searchAliases.put(b.getName(), r+"bin");
						searchAliases.put(c.getName(), r+"cin");
						searchAliases.put(d.getName(), r+"din");
						searchAliases.put(e.getName(), r+"ein");
						searchAliases.put(f.getName(),r+"fin");

						for(int k=1;k<i;k++) {

							System.out.println(i+" loop "+ k);

							Step loop2=list1.get(0).getNextStates().get(0);
							System.out.println(loop2.getName());

							searchAliases.put(loop2.getName(), r+"loopComp");

							Step cycleInDouble = ((CompositeStep)loop2).getRegions().get(0).getInitialState();
							searchAliases.put(cycleInDouble.getName(), r+"cycleIn");

							for(int rC=0;rC<3;rC++) {
								Region regCyc = ((CompositeStep) cycleInDouble).getRegions().get(rC);

								Step inIn = regCyc.getInitialState();
								searchAliases.put(inIn.getName(), r+"inCyc"+rC+"in");

								Step a2 = inIn.getNextStates().get(0);
								Step b2 = inIn.getNextStates().get(1);
								Step c2 = a2.getNextStates().get(0);
								Step d2 = b2.getNextStates().get(0);
//								State e2 = c.getNextStates().get(0);
//								State f2 = d.getNextStates().get(0);

								searchAliases.put(a2.getName(), r+"inCyc"+rC+"ain");
								searchAliases.put(b2.getName(), r+"inCyc"+rC+"bin");
								searchAliases.put(c2.getName(), r+"inCyc"+rC+"cin");
								searchAliases.put(d2.getName(), r+"inCyc"+rC+"din");
								searchAliases.put(e.getName(), r+"inCyc"+rC+"ein");
								searchAliases.put(f.getName(),r+"inCyc"+rC+"fin");
							}

							Step det2 = ((CompositeStep)loop2).getRegions().get(1).getInitialState();
							searchAliases.put(det2.getName(), r+"det");

							Map<Step, List<Step>>  mapCyc2 =((CompositeStep)loop2).getNextStatesConditional();

							if(k<i-1)
								list1= mapCyc2.get(StateUtils.findEndState(((CompositeStep)loop2).getRegions().get(1)));
						}
					}
				}
			}		

			Map<String,Double> mapSimul = new HashMap<String,Double>();
			Map<String,Double> mapAnalytic; 

			File sample = new File("src//main//resources//pyramisSimulationCycles//"+pathnameS);

			try (BufferedReader br = new BufferedReader(new FileReader(sample))) {
				String line;
				line = br.readLine();
				line = br.readLine();
				while ((line = br.readLine()) != null) {
					
					int ind= line.indexOf(".");
					if(ind>0)
						line=line.substring(0,ind-1)+" "+line.substring(ind-1);
					
					String[] lines = line.split(" +");
					System.out.println(line);
					if(lines.length==2)
						add(lines[0], Double.valueOf(lines[1]),mapSimul, store[8]);

				}
			} catch (IOException e) {

				e.printStackTrace();
			}
							
			String[] pathnames;

			File f = new File("src//main//resources//pyramisAnalyticCycles");

			// Populate the array with names of files and directories
			pathnames = f.list();

			// For each pathname in the pathnames array
			for (String pathname : pathnames) {

				mapAnalytic = new HashMap<String,Double>();
				String ex= (Final>0)? "FINAL" : "EXIT";
				boolean aa =pathname.contains("p-"+p+"_d-"+dep+"_s-"+ss+"_rp-"+rp+"_Final-"+Final+"_l-"+l);
				boolean bb =pathname.contains(p+"_d-"+dep+"_s-"+ss+"_rp-"+rp+"_Final-"+ex+"_l-"+l);

				if(aa || bb) {

					int LOOPSn= aa? Integer.valueOf(pathname.substring(14, 15)): Integer.valueOf(pathname.substring(16, 17));

					Map<String,String> alias = store[LOOPSn];

					File in = new File("src//main//resources//pyramisAnalyticCycles//"+pathname);

					try (BufferedReader br = new BufferedReader(new FileReader(in))) {
						String line;
						line = br.readLine();
						line = br.readLine();
						while ((line = br.readLine()) != null) {
							if(line.startsWith("p")) {
								continue;
							}
							int ind= line.indexOf(".");
							if(ind>0)
								line=line.substring(0,ind-1)+" "+line.substring(ind-1);
							
							
							String[] lines = line.split(" +");
							
							if(lines.length>1) {
								add(lines[0], Double.valueOf(lines[1]),mapAnalytic, alias);

							}

						}

					} catch (IOException e) {

						e.printStackTrace();
					}

					Double min=10.;
					Double max=0.;
					Double average=0.;

					int count=0;

					double scarto=0.;

					System.out.println(pathname);
					System.out.println(mapAnalytic);
					for(String st: mapSimul.keySet()) {
						System.out.println(st);

						if(mapAnalytic.containsKey(st)) {
							Double s= mapSimul.get(st);
							Double a= mapAnalytic.get(st);
							Double r;
							if(s>0.0) {
								r= Math.abs(s-a)/s;
								scarto+= r*r;
								System.out.println(r+" "+s+" "+ a);
								if(!(r>0.))
									continue;
								min= Math.min(min, r);
								max=Math.max(max, r);
								average+=r;
								count++;

							}
						}

					}
					if(extraStates) {
						average+=min+min+min+min;
						scarto+=min*min+min*min+min*min+min*min;
						count+=4;

					}

					average /=count;

					scarto= scarto/ (count-1);
					scarto-= average*average;

					scarto = Math.sqrt(scarto);

					File file = new File("src//main//resources//pyramisResCycles//res_"+pathname);
					file.getParentFile().mkdirs();

					try (PrintWriter writer = new PrintWriter(file)) {
						writer.write(formatter.format(min)+", "+ formatter.format(max)+", "+formatter.format(average)+", "+formatter.format(scarto));

					} catch (FileNotFoundException e) {
						System.out.println("errore");
						System.out.println(e.getMessage());
					}
				}
			}
			f = new File("src//main//resources//pyramisAnalyticSameTimeCycles");

			// Populate the array with names of files and directories
			pathnames = f.list();

			// For each pathname in the pathnames array
			for (String pathname : pathnames) {

				mapAnalytic = new HashMap<String,Double>();
				String ex= (Final>0)? "FINAL" : "EXIT";
				boolean aa =pathname.contains("p-"+p+"_d-"+dep+"_s-"+ss+"_rp-"+rp+"_Final-"+Final+"_l-"+l);
				boolean bb =pathname.contains(p+"_d-"+dep+"_s-"+ss+"_rp-"+rp+"_Final-"+ex+"_l-"+l);

				if(aa || bb) {
					
					int LOOPSn= aa? Integer.valueOf(pathname.substring(14, 15)): Integer.valueOf(pathname.substring(16, 17));

					Map<String,String> alias = store[LOOPSn];

					File in = new File("src//main//resources//pyramisAnalyticSameTimeCycles//"+pathname);

					try (BufferedReader br = new BufferedReader(new FileReader(in))) {
						String line;
						line = br.readLine();
						line = br.readLine();
						while ((line = br.readLine()) != null) {
							if(line.startsWith("p")) {
								continue;
							}
							int ind= line.indexOf(".");
							if(ind>0)
								line=line.substring(0,ind-1)+" "+line.substring(ind-1);
							
							String[] lines = line.split(" +");
							
							if(lines.length>1) {
								add(lines[0], Double.valueOf(lines[1]),mapAnalytic, alias);

							}
						}
					} catch (IOException e) {

						e.printStackTrace();
					}

					Double min=10.;
					Double max=0.;
					Double average=0.;

					int count=0;

					double scarto=0.;

					System.out.println(pathname);
					System.out.println(mapAnalytic);
					for(String st: mapSimul.keySet()) {
						System.out.println(st);

						if(mapAnalytic.containsKey(st)) {
							Double s= mapSimul.get(st);
							Double a= mapAnalytic.get(st);
							Double r;
							if(s>0.0) {
								r= Math.abs(s-a)/s;
								scarto+= r*r;
								System.out.println(r+" "+s+" "+ a);
								if(!(r>0.))
									continue;
								min= Math.min(min, r);
								max=Math.max(max, r);
								average+=r;
								count++;

							}
						}

					}
					if(extraStates) {
						average+=min+min+min+min;
						scarto+=min*min+min*min+min*min+min*min;
						count+=4;
					}
					average /=count;

					scarto= scarto/ (count-1);
					scarto-= average*average;
					scarto = Math.sqrt(scarto);

					File file = new File("src//main//resources//pyramisResSameTimeCycles//res_"+pathname);
					
					file.getParentFile().mkdirs();

					try (PrintWriter writer = new PrintWriter(file)) {

						writer.write(formatter.format(min)+", "+ formatter.format(max)+", "+formatter.format(average)+", "+formatter.format(scarto));


					} catch (FileNotFoundException e) {
						System.out.println("errore");
						System.out.println(e.getMessage());
					}
				}
			}
		}
	}

	private static void add(String s,Double v, Map<String,Double> m, Map<String,String> aliasSearch) {

		String alias = aliasSearch.get(s);
	
		if(alias!=null) {
			if(!m.containsKey(alias))
				m.put(alias, 0.0);

			Double d = v+m.get(alias);
			m.put(alias, d);
			
			System.out.println(s+" ->"+alias);
		}
	}
}
