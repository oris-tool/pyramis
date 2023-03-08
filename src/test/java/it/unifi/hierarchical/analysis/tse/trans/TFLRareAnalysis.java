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

import it.unifi.hierarchical.analysis.HierarchicalSMPAnalysis;
import it.unifi.hierarchical.analysis.NumericalValues;
import it.unifi.hierarchical.model.HSMP;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.tse.trans.TFLRare;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * This class supports the analysis of the HSMP models with rare events
 * used in the case study on transient timed failure logic analysis of component based systems 
 * of the paper titled "Compositional Analysis of Hierarchical UML Statecharts".
 * For each model, 100 simulation runs are also performed, with each run lasting at least as long as the analysis.
 */
public class TFLRareAnalysis {

	// Chosen according to the model, based on the maximum time elapsed in a region or a state
	private static  double TIME_LIMIT =17;
	private static final double TIME_STEP = 0.0025;
	private static final boolean expolSame = true;

	public static void main(String[] args){

		Integer x = null;
		Integer y= null;
		
		x=Integer.valueOf(100);
		y = Integer.valueOf(1);

		test1(x,y);		
	}

	public static void test1(Integer x, Integer y) {    

		for(int last=0; last>-1;last--) {
			RegionType lastB = (last==1)? RegionType.FINAL : RegionType.EXIT;

			for(int parallel=3;parallel<4; parallel++) {
				for(int depth=2;depth<3;depth++) {
					for(int seq=3;seq<4;seq++) {
						try {
							String print="src//main//resources//pyramisAnalyticRare//"+ ""+TIME_STEP+"_p-"+parallel+"_d-"+(depth+1)+"_s-"+seq+"_Final-"+last+".txt";

							//HSMP
							//Build the model
							HSMP model = TFLRare.build(parallel,depth,seq,expolSame, lastB);

							Set<String> sList = TFLRare.getStates();

							//Analyze
							Date start = new Date();
							HierarchicalSMPAnalysis analysis = new HierarchicalSMPAnalysis(model, 0);
							Map<String, Double> ssHSMP = analysis.evaluateSteadyState(TIME_STEP, TIME_LIMIT);

							Date end = new Date();
							long time = end.getTime() - start.getTime();
							System.out.println(parallel+ "  "+ depth+ "  "+ seq+" "+last );
							System.out.println("Time Hierarchical SMP analysis:" + time + "ms");
							
							File file = new File(print);
							file.getParentFile().mkdirs();
							
							try (PrintWriter writer = new PrintWriter(file)) {
								writer.write("TIME=  "		+time 		+"ms \n\n");
								writer.write("p="+parallel+" d="+(depth+1)+" s="+seq+ "\n");
								for(String s: sList) {
										writer.write(s+" "+ssHSMP.get(s)+"\n");
								}
							} catch (FileNotFoundException e) {
								System.out.println("errore");
								System.out.println(e.getMessage());
							}
							
							file = new File("cdf_"+print);
							NumericalValues cdf = HierarchicalSMPAnalysis.cdf;
							double[] val= cdf.getValues();
							System.out.println(cdf.getStep());
							try (PrintWriter writer = new PrintWriter(file)) {
								
								for(int i=0;i<val.length;i++) {
									writer.write(i*cdf.getStep()+" "+val[i]+"\n");
								}
							} catch (FileNotFoundException e) {
								System.out.println("errore");
								System.out.println(e.getMessage());
							}

							TFLRareGroundTruth.sampleFromTime(time, parallel,depth,seq,expolSame,lastB, x,y);
						}
						catch(Exception e){
							System.out.println(e.toString());
						}
					}
				}
			}
		}
	}
}
