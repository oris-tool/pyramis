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
import it.unifi.hierarchical.model.Region;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.function.GEN;

import java.math.BigDecimal;
import java.util.*;

/**
 * This class supports the definition of the HSMP models with large support PDFs 
 * used in the case study on transient timed failure logic analysis of component based systems 
 * of the paper titled "Compositional Analysis of Hierarchical UML Statecharts".
 */
public class TFLLargeSupport {
/*
	public static int parallelS;
	public static int depthS;
	public static int sequenceS;

	private static int c;

	public static Set<String> statesS;

	public static Set<String> getStates(){
		return statesS;
	}

	// for each step, all the (simple and composite) steps contained within its regions
	public static Map<Step,Set<Step>> map;
	public static Map<Step, Step> parentMap;
	public static Map<Step, Step> doublesMap;
	public static Map<Step, Step> doublesMapFrom2;
	public static Map<Step,Region> regMap;

	public static Set<Step> compS;

	// only the first step of the sequence is contained
	public static Set<Step> expDiffS;
	public static Set<Step> expS;

	public static Set<String> zeros;

	public static Set<Step> firstLeaf;
	public static Set<Step> secondLeaf;
	public static Set<Step> thirdLeaf;

	public static Set<Step> statesP;

	public static HSMP build(int leaf, int parallel, int depth, int sequence, boolean expolSame, RegionType regT) {
		TFLLargeSupport.c=0;
		TFLLargeSupport.map = new HashMap<Step,Set<Step>>();
		TFLLargeSupport.compS= new HashSet<Step>();
		TFLLargeSupport.expS= new HashSet<Step>();
		TFLLargeSupport.expDiffS= new HashSet<Step>();

		TFLLargeSupport.firstLeaf= new HashSet<Step>();
		TFLLargeSupport.secondLeaf= new HashSet<Step>();
		TFLLargeSupport.thirdLeaf= new HashSet<Step>();

		TFLLargeSupport.zeros= new HashSet<String>();

		// child, parent
		TFLLargeSupport.parentMap= new HashMap<Step, Step>();

		// old1, old2
		TFLLargeSupport.doublesMap= new HashMap<Step, Step>();
		// old2,old1
		TFLLargeSupport.doublesMapFrom2= new HashMap<Step, Step>();

		// step, region contained in the parent step
		TFLLargeSupport.regMap= new HashMap<Step,Region>();

		TFLLargeSupport.parallelS=parallel;
		TFLLargeSupport.depthS=depth;
		TFLLargeSupport.sequenceS=sequence;

		TFLLargeSupport.statesS= new HashSet<String>();
		TFLLargeSupport.statesP= new HashSet<Step>();

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

			Step initial = buildInner(leaf, rListAll.get(i), S0, 1, expolSame, regT);
			rListAll.get(i).setInitialState(initial);
		}

		System.out.println("c is "+ c);
		return new HSMP(S0);
	}

	private static Step buildInner(int leaf, Region parentRegion, Step parent, int currentDepth, boolean expolSame, RegionType regT) {

		GEN exp = GEN.newExpolynomial("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2", OmegaBigDecimal.ZERO, OmegaBigDecimal.ONE);

		GEN expLong = GEN.newExpolynomial("0.22517 * Exp[-0.311427 x] * x + -0.022517 * Exp[-0.311427 x] * x^2", OmegaBigDecimal.ZERO, OmegaBigDecimal.TEN); 

		Step current;

		List<Step> nextStates = null;//Required to avoid ambiguity

		if(currentDepth!=TFLLargeSupport.depthS) {
			List<Region> rListAll = new LinkedList<Region>();
			for(int i=0;i<parallelS;i++) {

				Region newR = new Region(null, regT);
				rListAll.add(newR);
			}

			//System.out.println("p="+parallelS+" d="+depthS+" s="+sequenceS);

			current = new CompositeStep(
					"comp_"+currentDepth+"_"+c++,  
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

				Step initial = buildInner(leaf,rListAll.get(i), current, currentDepth+1, expolSame, regT);

				rListAll.get(i).setInitialState(initial);
			}
		} else {

			current = new SimpleStep(
					"zero_"+currentDepth+"_"+c++,
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

		GEN expC=null;

		if(currentDepth==2 && firstLeaf.isEmpty()) {
			expC=expLong;
		} else if(currentDepth==1 && secondLeaf.isEmpty() && leaf>=2) {
				expC=expLong;
		} else {
			expC=exp;
		}

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

		boolean first=false;
		boolean second =false;

		if(currentDepth==2 && firstLeaf.isEmpty()) {
			first=true;
			firstLeaf.add(old1);
			firstLeaf.add(old2);
		}else if(currentDepth==1 && secondLeaf.isEmpty() && leaf>=2) {
				second=true;
				secondLeaf.add(old1);
				secondLeaf.add(old2);
		}

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

			if(first) {
				firstLeaf.add(old1);
				firstLeaf.add(old2);
			}else if(second) {
				secondLeaf.add(old1);
				secondLeaf.add(old2);
			}
		}

		expS.add(old1);
		expDiffS.add(old2);

		current.setNextStates(Arrays.asList(old1,old2), Arrays.asList(0.5,0.5));

		//test
		if(currentDepth==TFLLargeSupport.depthS) {
			return old1;
		}
		
		return current;
	}

 */
}
