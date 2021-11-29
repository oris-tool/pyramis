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

package it.unifi.hierarchical.analysis.stpn.tse.steady;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.oristool.models.stpn.RewardRate;
import org.oristool.models.stpn.TransientSolution;
import org.oristool.models.stpn.trans.RegTransient;
import org.oristool.models.stpn.trees.DeterministicEnablingState;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;

import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.tse.trans.TFLVerbose;

/**
 * This class supports the analysis of the STPN model corresponding to the HSMP model
 * used in the case study on steady-state analysis of software rejuvenation in virtual servers 
 * of the paper titled "Compositional Analysis of Hierarchical UML Statecharts" (see Figure 8).
 * In particular, eahc marking of the STPN is associated with a location of the HSMP.
 */
public class PyramisSTPN implements Callable<Long> {

	//Need to be chosen according to the model, based on which is the maximum time elapsed in a region or a state
	private static final boolean expolSame = true;

	public static void main(String[] args){

		for(int last=0;last<2;last++) {
			for(int parallel=2;parallel<3; parallel++) {
				for(int depth=1;depth<2;depth++) {

					PyramisSTPN xpn = new PyramisSTPN(last, parallel, depth);

					xpn.call();
				}
			}
		}
	}

	public int parallel;
	public int depth;
	public int last;

	public PyramisSTPN(int las, int p, int d) {
		parallel=p;
		depth=d;
		last=las;
	}

	public Long call() {    

		RegionType lastB = (last==1)? RegionType.FINAL : RegionType.EXIT;

		for(int seq=2;seq<4;seq++) {

			String print= "XPN_p-"+parallel+"_d-"+(depth+1)+"_s-"+seq+"_Final-"+last+".txt";

			Date start = new Date();

			PetriNet net = new PetriNet();
			Marking m = new Marking();

//			HierarchicalSMP model = HSMP_ScalabilityVerbose.build(parallel,depth,seq,expolSame, lastB);

			Map<Integer, Map<Integer, LinkedList<String>>> internalLeavesPre = TFLVerbose.internalLeaves;

			Map<Integer, Map<Integer, LinkedList<String>>> internalLeaves = new HashMap<Integer, Map<Integer,LinkedList<String>>>();
			Map<Integer, Map<Integer, LinkedList<String>>> internalLeavesCopy = new HashMap<Integer, Map<Integer,LinkedList<String>>>();

			Map<String,String> link= new HashMap<String, String>();

			int c=0;
			for(Integer in: internalLeavesPre.keySet()) {

				internalLeaves.put(in, new HashMap<Integer, LinkedList<String>>());
				internalLeavesCopy.put(in, new HashMap<Integer, LinkedList<String>>());
				for(Integer in2: internalLeavesPre.get(in).keySet()) {
					internalLeaves.get(in).put(in2, new LinkedList<String>());
					internalLeavesCopy.get(in).put(in2, new LinkedList<String>());

					for(String s: internalLeavesPre.get(in).get(in2)) {
						String nName= "place"+c;
						internalLeaves.get(in).get(in2).add(nName);
						internalLeavesCopy.get(in).get(in2).add(nName);
						c++;
						link.put(nName, s);
					}
				}
			}

			CreateSTPN.build(net, m, parallel,depth,seq,expolSame,lastB, internalLeaves);

			BigDecimal timeBound= new BigDecimal(seq*(depth+1)+1);
			RegTransient.Builder builder = RegTransient.builder();
			builder.timeBound(timeBound);
			builder.timeStep(new BigDecimal("0.0025"));
			builder.greedyPolicy(timeBound, BigDecimal.ZERO);

			RegTransient analysis = builder.build();
//			TransientSolution<DeterministicEnablingState, Marking> probs =
//					analysis.compute(net, m);

			TransientSolution<DeterministicEnablingState, Marking> solution = 
					analysis.compute(net, m);

			// display transient probabilities
			//new TransientSolutionViewer(solution);

			List<String> rSet = new LinkedList<String>();

			for(int i=1; i<=depth+1;i++) {
				for(int q =0; q<seq;q++) {
					for(String s: internalLeavesCopy.get(i).get(q)) {

						rSet.add(s);
					}

				}
			}
			RewardRate[] rwArr=new RewardRate[rSet.size()];
			for(int i=0; i<rSet.size();i++) {

				rwArr[i]= RewardRate.fromString(rSet.get(i)+">0");
			}

			TransientSolution<DeterministicEnablingState, RewardRate>  rw =	TransientSolution.computeRewards(false, solution, "end>0");

			double[] Cdf = new double[rw.getSolution().length];
			for(int count = 0; count < rw.getSolution().length; count++){
				Cdf[count] = rw.getSolution()[count][0][0];
			}
			File file = new File("cdf_"+print);
			try (PrintWriter writer = new PrintWriter(file)) {
				writer.write(timeBound.toString()+"\n");
				writer.write("0.0025"+"\n");

				writer.write("\n");
				for(int count = 0; count < Cdf.length; count++){

					writer.write(Cdf[count]+"\n");
				}

			} catch (FileNotFoundException e) {
				System.out.println("errore");
				System.out.println(e.getMessage());
			}

			double old=0.;
			double sum=0.;
			for(int count = 0; count < Cdf.length; count++){

				sum+=(Cdf[count]-old)*0.0025*count;
				old=Cdf[count];					

			}

			TransientSolution<DeterministicEnablingState, RewardRate>  rwCumul =	TransientSolution.computeRewards(true, solution, rwArr);

			Date end = new Date();

			double time2 = end.getTime() - start.getTime();
			System.out.println("Time Regenerative SS  analysis:" + (time2) + "ms");

			System.out.println(sum);
			file = new File(print);
			try (PrintWriter writer = new PrintWriter(file)) {
				writer.write("TIME=  "		+time2 		+"ms \n\n");

				for(int count = 0; count < rSet.size(); count++){

					writer.write(link.get(rSet.get(count))+" "+rwCumul.getSolution()[rwCumul.getSolution().length-1][0][count]/sum+"\n");

				}
			} catch (FileNotFoundException e) {
				System.out.println("errore");
				System.out.println(e.getMessage());
			}
		}
		return (long) 1;
	}
}
