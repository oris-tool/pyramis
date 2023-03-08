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

package it.unifi.hierarchical.model.tse.trans;

import it.unifi.hierarchical.model.*;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.utils.StateUtils;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.function.GEN;

import java.math.BigDecimal;
import java.util.*;

/**
 * This class supports the definition of the HSMP models with cycles 
 * used in the case study on transient timed failure logic analysis of component based systems 
 * of the paper titled "Compositional Analysis of Hierarchical UML Statecharts".
 */
public class TFLCycles {

	public static int parallelS;
	public static int depthS;
	public static int sequenceS;

	private static int c;

	public static Set<String> statesS;

	public static Set<String> getStates(){
		return statesS;
	}

	public static LinkedList<Step> firstLeaf;

	public static LinkedList<Step> secondLeaf;

	public static LinkedList<Step> thirdLeaf;

	// for each step, all the (simple and composite) steps contained within its regions
	public static Map<Step,Set<Step>> map;
	public static Map<Step, Step> parentMap;
	public static Map<Step, Step> doublesMap;
	public static Map<Step, Step> doublesMapFrom2;
	public static Map<Step,Region> regMap;

	public static Map<String,String>  toCopy;

	public static Set<Step> compS;

	public static Set<Step> det;
	public static Map<Step, Step> loopsMap;
	
	// only the first step of the sequence is contained
	public static Set<Step> expDiffS;
	public static Set<Step> expS;

	public static Set<String> zeros;

	public static Integer LOOP;


	public static Set<Step> statesP;

	public static HSMP build(double rejPeriod, int leaf, int parallel, int depth, int sequence, boolean expolSame, RegionType regT, int LOOP) {
		TFLCycles.c=0;
		TFLCycles.map = new HashMap<Step,Set<Step>>();
		TFLCycles.compS= new HashSet<Step>();
		TFLCycles.expS= new HashSet<Step>();
		TFLCycles.expDiffS= new HashSet<Step>();
		
		TFLCycles.det= new HashSet<Step>();


		TFLCycles.firstLeaf= new LinkedList<Step>();
		TFLCycles.secondLeaf= new LinkedList<Step>();
		TFLCycles.thirdLeaf= new LinkedList<Step>();
		TFLCycles.LOOP= LOOP;

		TFLCycles.zeros= new HashSet<String>();

		// child, parent
		TFLCycles.parentMap= new HashMap<Step, Step>();
		TFLCycles.toCopy= new HashMap<String,String>();
		TFLCycles.loopsMap= new HashMap<Step, Step>();

		// old1, old2
		TFLCycles.doublesMap= new HashMap<Step, Step>();
		// old2,old1
		TFLCycles.doublesMapFrom2= new HashMap<Step, Step>();

		// step, region contained in the parent step
		TFLCycles.regMap= new HashMap<Step,Region>();

		TFLCycles.parallelS=parallel;
		TFLCycles.depthS=depth;
		TFLCycles.sequenceS=sequence;

		TFLCycles.statesS= new HashSet<String>();
		TFLCycles.statesP= new HashSet<Step>();

		List<Region> rListAll = new LinkedList<Region>();
		for(int i=0;i<parallelS;i++) {

			Region newR = new Region(null, regT);
			rListAll.add(newR);
		}

		//System.out.println("p="+parallelS+" d="+depthS+" s="+sequenceS);

		List<Step> nextStates = null;//Required to avoid ambiguity

		Step S0 = new CompositeStep(
				"S0",  
				rListAll, 
				nextStates, 
				null, 
				0);
		map.put(S0, new HashSet<Step>());
		compS.add(S0);
		statesS.add(S0.getName());
		statesP.add(S0);

		S0.setNextStates(Arrays.asList(S0), Arrays.asList(1.0));


		for(int i=0;i<parallelS;i++) {

			Step initial = buildInner(rejPeriod, leaf, rListAll.get(i), S0, 1, expolSame, regT, 0);
			rListAll.get(i).setInitialState(initial);
			System.out.println(rListAll.get(i));
		}

		System.out.println("c is "+ c);
		return new HSMP(S0);
	}

	private static Step buildInner(double rejPeriod, int leaf, Region parentRegion, Step parent, int currentDepth, boolean expolSame, RegionType regT, int inCycleStatus) {

		GEN expC = GEN.newExpolynomial("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2", OmegaBigDecimal.ZERO, OmegaBigDecimal.ONE);

		boolean first=false;
		boolean second =false;
		boolean third = false;
		boolean cycle=false;

		if(currentDepth==1) {
			if(firstLeaf.size()==secondLeaf.size() && firstLeaf.size()==thirdLeaf.size()) {
				first=true;
			}else if(firstLeaf.size() >secondLeaf.size()) {
				second=true;
			}else {
				third=true;
			}

			if(first || (second && leaf>=2) || leaf==3) {
				cycle=true;
			}
		}

		Step current = null;

		List<Step> nextStates = null;//Required to avoid ambiguity

		if(!cycle && inCycleStatus!=1) {

			if(currentDepth!=TFLCycles.depthS && inCycleStatus==0) {

				List<Region> rListAll = new LinkedList<Region>();
				for(int i=0;i<parallelS;i++) {

					Region newR = new Region(null, regT);
					rListAll.add(newR);
				}

				//System.out.println("p="+parallelS+" d="+depthS+" s="+sequenceS);

				current = new CompositeStep(
						"normalComposite_"+currentDepth+"_"+c++,  
						rListAll, 
						nextStates, 
						null, 
						currentDepth);

				compS.add(current);
				parentMap.put(current, parent);
				regMap.put(current, parentRegion);

				map.put(current, new HashSet<Step>());
				map.get(parent).add(current);
				statesS.add(current.getName());
				statesP.add(current);

				for(int i=0;i<parallelS;i++) {

					Step initial = buildInner(rejPeriod, leaf,rListAll.get(i), current, currentDepth+1, expolSame, regT, 0);

					rListAll.get(i).setInitialState(initial);
				}

			} else  {

				current = new SimpleStep(
						"zeroNotCycle_"+currentDepth+"_"+c++,
						GEN.newDeterministic(new BigDecimal(0)),
						nextStates,
						null, 
						currentDepth);	
				map.get(parent).add(current);
				parentMap.put(current, parent);
				regMap.put(current, parentRegion);

				statesP.add(current);
				statesS.add(current.getName());

				zeros.add(current.getName());
			}

			Step E = new FinalLocation("e_"+c++, currentDepth);

			Step stateD112 = new SimpleStep(
					"d1_"+currentDepth+"_"+c++,
					expC,
					Arrays.asList(E), 
					Arrays.asList(1.0), 
					currentDepth); 

			Step stateD122 = new SimpleStep(
					"d2_"+currentDepth+"_"+c++,
					expC,
					Arrays.asList(E), 
					Arrays.asList(1.0), 
					currentDepth); 

			Step old1= stateD112;
			Step old2= stateD122;
			map.get(parent).add(old1);
			map.get(parent).add(old2);
			statesS.add(old1.getName());
			statesS.add(old2.getName());
			doublesMap.put(old1, old2);
			doublesMapFrom2.put(old2, old1);

			statesP.add(old1);
			statesP.add(old2);
			parentMap.put(old1, parent);
			parentMap.put(old2, parent);
			regMap.put(old1, parentRegion);
			regMap.put(old2, parentRegion);

			for(int r=1;r<sequenceS;r++) {

				Step stateD111 = new SimpleStep(
						"d1--_"+currentDepth+"_"+c++,
						expC,
						Arrays.asList(old1), 
						Arrays.asList(1.0), 
						currentDepth); 

				Step stateD121 = new SimpleStep(
						"d2--_"+currentDepth+"_"+c++,
						expC,
						Arrays.asList(old2), 
						Arrays.asList(1.0), 
						currentDepth); 
				old1= stateD111;
				old2=stateD121;
				statesS.add(old1.getName());
				statesS.add(old2.getName());
				statesP.add(old1);
				statesP.add(old2);
				map.get(parent).add(old1);
				map.get(parent).add(old2);
				parentMap.put(old1, parent);
				parentMap.put(old2, parent);
				doublesMap.put(old1, old2);
				doublesMapFrom2.put(old2, old1);

				regMap.put(old1, parentRegion);
				regMap.put(old2, parentRegion);
			}

			expS.add(old1);
			expDiffS.add(old2);

			current.setNextStates(Arrays.asList(old1,old2), Arrays.asList(0.5,0.5));

		} else if(cycle) {
			
			current = new SimpleStep(
					"zeroCycle_"+currentDepth+"_"+c++,
					GEN.newDeterministic(new BigDecimal(0)),
					nextStates,
					null, 
					currentDepth);	
			
			map.get(parent).add(current);
			parentMap.put(current, parent);
			regMap.put(current, parentRegion);

			statesP.add(current);
			statesS.add(current.getName());

			zeros.add(current.getName());


			Step[] sA= new Step[LOOP];
			Step[] sEmpty = new Step[LOOP];

			Step[] End1= new Step[LOOP];
			Step[] End2= new Step[LOOP];

			for(int i=0;i<LOOP;i++) {
				
				Step empty = new SimpleStep(
						"emptyLoop_"+c++,
						GEN.newDeterministic(new BigDecimal(0)),
						nextStates,
						null, 
						currentDepth);
				sEmpty[i]=empty;

				zeros.add(empty.getName());
				
				List<Region> rListAll = new LinkedList<Region>();
				Region newR1 = new Region(null, RegionType.EXIT);
				Region newR2 = new Region(null, RegionType.EXIT);
				rListAll.add(newR1);
				rListAll.add(newR2);


				Step cyc = new CompositeStep(
						"loop_"+currentDepth+"_"+c++,  
						rListAll, 
						nextStates, 
						null, 
						currentDepth);
				
				if(first) {
					firstLeaf.add(cyc);
				}else if(second) {
					secondLeaf.add(cyc);
				}else if(third) {
					thirdLeaf.add(cyc);
				}
				
				if(second) {
					System.out.println("LOOP "+LOOP+" secondleaf "+cyc.getName());
					
				}

				compS.add(cyc);
				parentMap.put(cyc, parent);
				regMap.put(cyc, parentRegion);

				map.put(cyc, new HashSet<Step>());
				map.get(parent).add(cyc);
				statesS.add(cyc.getName());
				statesP.add(cyc);
				
				sA[i]=cyc;

				Step initial1 = buildInner(rejPeriod, leaf,rListAll.get(0), cyc, currentDepth+1, expolSame, regT, 1);
				
				rListAll.get(0).setInitialState(initial1);
				
				Step end1 = StateUtils.findEndState(rListAll.get(0));
				End1[i]=end1;
				
				Step endinit= new ExitState("eLoopDet_"+c++,currentDepth+1);
				End2[i]=endinit;
							
				Step initial2 = new SimpleStep(
						"init_"+currentDepth+"_"+c++,
						GEN.newDeterministic(new BigDecimal(rejPeriod)),
						Arrays.asList(endinit),
						Arrays.asList(1.0), 
						currentDepth+1);		
				
				map.get(cyc).add(initial2);
				parentMap.put(initial2, cyc);
				regMap.put(initial2, rListAll.get(1));

				statesP.add(initial2);
				statesS.add(initial2.getName());

				rListAll.get(1).setInitialState(initial2);
				
				det.add(initial2);

			}
			
			Step E = new ExitState("exitOfCycleRegion_"+c++, currentDepth);

			Step stateD112 = new SimpleStep(
					"d1CycleReg_"+currentDepth+"_"+c++,
					expC,
					Arrays.asList(E), 
					Arrays.asList(1.0), 
					currentDepth); 

			Step stateD122 = new SimpleStep(
					"d2CycleReg_"+currentDepth+"_"+c++,
					expC,
					Arrays.asList(E), 
					Arrays.asList(1.0), 
					currentDepth); 

			Step old1= stateD112;
			Step old2= stateD122;
			map.get(parent).add(old1);
			map.get(parent).add(old2);
			statesS.add(old1.getName());
			statesS.add(old2.getName());
			doublesMap.put(old1, old2);
			doublesMapFrom2.put(old2, old1);

			statesP.add(old1);
			statesP.add(old2);
			parentMap.put(old1, parent);
			parentMap.put(old2, parent);
			regMap.put(old1, parentRegion);
			regMap.put(old2, parentRegion);

			for(int r=1;r<sequenceS;r++) {

				Step stateD111 = new SimpleStep(
						"d1--_"+currentDepth+"_"+c++,
						expC,
						Arrays.asList(old1), 
						Arrays.asList(1.0), 
						currentDepth); 

				Step stateD121 = new SimpleStep(
						"d2--_"+currentDepth+"_"+c++,
						expC,
						Arrays.asList(old2), 
						Arrays.asList(1.0), 
						currentDepth); 
				old1= stateD111;
				old2=stateD121;
				statesS.add(old1.getName());
				statesS.add(old2.getName());
				statesP.add(old1);
				statesP.add(old2);
				map.get(parent).add(old1);
				map.get(parent).add(old2);
				parentMap.put(old1, parent);
				parentMap.put(old2, parent);
				doublesMap.put(old1, old2);
				doublesMapFrom2.put(old2, old1);

				regMap.put(old1, parentRegion);
				regMap.put(old2, parentRegion);
			}

			expS.add(old1);
			expDiffS.add(old2);
	
			for(int i=0;i<LOOP-1;i++) {
				loopsMap.put(sA[i], sA[i+1]);
				
				((CompositeStep) sA[i]).setNextStatesConditional(Map.of(
						End1[i],
						Arrays.asList(old1,old2),
						End2[i],
						Arrays.asList(sEmpty[i])));

				((CompositeStep) sA[i]).setBranchingProbsConditional(Map.of(
						End1[i],
						Arrays.asList(0.5,0.5),
						End2[i],
						Arrays.asList(1.0)));
				
				sEmpty[i].setNextStates(Arrays.asList(sA[i+1]), Arrays.asList(1.0));
			}
			
			sA[LOOP-1].setNextStates(Arrays.asList(old1,old2), Arrays.asList(0.5,0.5));
		
			current.setNextStates(Arrays.asList(sA[0]), Arrays.asList(1.0));

		} else if(inCycleStatus==1) {

			List<Region> rListAll = new LinkedList<Region>();
			for(int i=0;i<parallelS;i++) {

				Region newR = new Region(null, regT);
				rListAll.add(newR);
			}

			//System.out.println("p="+parallelS+" d="+depthS+" s="+sequenceS);
			
			Step E = new ExitState("exitInsideCycle_"+c++, currentDepth);

			current = new CompositeStep(
					"compInCycle_"+currentDepth+"_"+c++,  
					rListAll, 
					nextStates, 
					null, 
					currentDepth);

			compS.add(current);
			parentMap.put(current, parent);
			regMap.put(current, parentRegion);

			map.put(current, new HashSet<Step>());
			map.get(parent).add(current);
			statesS.add(current.getName());
			statesP.add(current);

			for(int i=0;i<parallelS;i++) {

				Step initial = buildInner(rejPeriod,leaf,rListAll.get(i), current, currentDepth+1, expolSame, regT, 2);

				rListAll.get(i).setInitialState(initial);
			}
			
			current.setNextStates(Arrays.asList(E), Arrays.asList(1.0));
		}
		return current;

	}
}
