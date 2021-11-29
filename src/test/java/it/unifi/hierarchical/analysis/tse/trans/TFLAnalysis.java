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
import java.util.Date;
import java.util.Map;
import java.util.Set;

import it.unifi.hierarchical.analysis.HierarchicalSMPAnalysis;
import it.unifi.hierarchical.analysis.NumericalValues;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.models.tse.trans.TFL;

/**
 * This class supports the analysis of the HSMP models of the basic pattern (and its variants
 * depending on the behaviour length, the parallelism degree, the hierarchy depth, and the composite type)
 * used in the case study on transient timed failure logic analysis of component based systems 
 * of the paper titled "Compositional Analysis of Hierarchical UML Statecharts" (see Figure 4).
 * For each model, 100 simulation runs are also performed, with each run lasting at least as long as the analysis.
 */
public class TFLAnalysis {

	// Chosen according to the model, based on the maximum time elapsed in a region or a step
	private static  double TIME_LIMIT = 17;
	private static final double TIME_STEP = 0.0025;
	
	public static void main(String[] args){

		Integer repetitionsOfSampling= null;
		if(args.length>0) {
			repetitionsOfSampling = Integer.valueOf(100);
		}
		
		test1(repetitionsOfSampling);
	}

	public static void test1(Integer repetitionsOfSampling) {    

		for(int last=1; last>-1;last--) {
			RegionType lastB = (last==1)? RegionType.FINAL : RegionType.EXIT;

			for(int parallel=2;parallel<5; parallel++) {
				for(int depth=1;depth<4;depth++) {
					for(int seq=2;seq<5;seq++) {

						try {
							String print= "src//main//resources//pyramisAnalytic//"+""+TIME_STEP+"_p-"+parallel+"_d-"+(depth+1)+"_s-"+seq+"_Final-"+last+".txt";

							//HSMP
							//Build the model
							HierarchicalSMP model = TFL.build(parallel,depth,seq,true, lastB);

							Set<String> sList = TFL.getStates();

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
							
							TFLGroundTruth.sampleFromTime(time, parallel,depth,seq,true,lastB, repetitionsOfSampling);
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