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
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.function.GEN;

import java.math.BigDecimal;
import java.util.*;

/**
 * This class supports the definition of the HSMP models with rare events
 * used in the case study on transient timed failure logic analysis of component based systems 
 * of the paper titled "Compositional Analysis of Hierarchical UML Statecharts".
 */
public class TFLRare {

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
	
	public static Map<Step, Step>  toCopy;
	
	public static Set<Step> compS;
	
	// only the first step of the sequence is contained
	public static Set<Step> expDiffS;
	public static Set<Step> expS;
	
	public static Set<Step> statesP;

	public static HSMP build(int parallel, int depth, int sequence, boolean expolSame, RegionType regT) {
		TFLRare.c=0;
		TFLRare.map = new HashMap<Step,Set<Step>>();
		TFLRare.compS= new HashSet<Step>();
		TFLRare.expS= new HashSet<Step>();
		TFLRare.expDiffS= new HashSet<Step>();
		
		// child, parent
		TFLRare.parentMap= new HashMap<Step, Step>();
		TFLRare.toCopy= new HashMap<Step, Step>();
		
		// old1, old2
		TFLRare.doublesMap= new HashMap<Step, Step>();

		// old2,old1
		TFLRare.doublesMapFrom2= new HashMap<Step, Step>();
		
		// step, region contained in the parent step
		TFLRare.regMap= new HashMap<Step,Region>();
		
		TFLRare.parallelS=parallel;
		TFLRare.depthS=depth;
		TFLRare.sequenceS=sequence;

		TFLRare.statesS= new HashSet<String>();
		TFLRare.statesP= new HashSet<Step>();

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

			Step initial = buildInner(rListAll.get(i), S0, 1, expolSame, regT);
			rListAll.get(i).setInitialState(initial);
		}
		System.out.println("c is "+ c);
		return new HSMP(S0);
	}

	private static Step buildInner(Region parentRegion, Step parent, int currentDepth, boolean expolSame, RegionType regT) {

		GEN exp = GEN.newExpolynomial("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2", OmegaBigDecimal.ZERO, OmegaBigDecimal.ONE);
//exp= GEN.newDeterministic(BigDecimal.valueOf(0.5));
		
		GEN expDiff;

		if(expolSame)
			expDiff= exp;
		else {
			expDiff = GEN.newExpolynomial("0.22517 * Exp[-0.311427 x] * x + -0.022517 * Exp[-0.311427 x] * x^2", OmegaBigDecimal.ZERO, OmegaBigDecimal.TEN); 
		}

		Step current;
		List<Step> nextStates = null;//Required to avoid ambiguity

		if(currentDepth!=TFLRare.depthS) {
			
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

				Step initial = buildInner(rListAll.get(i), current, currentDepth+1, expolSame, regT);

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
		}

		Step E = new FinalLocation("e_"+c++, currentDepth);

		Step stateD112 = new SimpleStep(
				"d1_"+currentDepth+"_"+c++,
				exp,
				Arrays.asList(E), 
				Arrays.asList(1.0), 
				currentDepth); 
		
		Step stateD122 = new SimpleStep(
				"d2_"+currentDepth+"_"+c++,
				expDiff,
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
					exp,
					Arrays.asList(old1), 
					Arrays.asList(1.0), 
					currentDepth); 

			Step stateD121 = new SimpleStep(
					"d2--_"+currentDepth+"_"+c++,
					expDiff,
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

		current.setNextStates(Arrays.asList(old1,old2), Arrays.asList(1-0.005,0.005));

		return current;
	}
}
