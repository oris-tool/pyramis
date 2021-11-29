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

package it.unifi.hierarchical.model.tse.trans;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.function.GEN;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.Region.RegionType;

/**
 * This class supports the definition of the HSMP models of the basic pattern (and its variants
 * depending on the behaviour length, the parallelism degree, the hierarchy depth, and the composite type)
 * used in the case study on transient timed failure logic analysis of component based systems 
 * of the paper titled "Compositional Analysis of Hierarchical UML Statecharts" (see Figure 4).
 */
public class TFL {

	public static int parallelS;
	public static int depthS;
	public static int sequenceS;

	private static int c;

	public static Set<String> statesS;

	public static Set<String> getStates(){
		return statesS;
	}
	
	// for each step, all the (simple and composite) steps contained within its regions
	public static Map<State,Set<State>> map;
	public static Map<State,State> parentMap;
	public static Map<State,State> doublesMap;
	public static Map<State,State> doublesMapFrom2;
	public static Map<State,Region> regMap;
	
	public static Map<State,State>  toCopy;
	
	public static Set<State> compS;
	
	// only the first step of the sequence is contained
	public static Set<State> expDiffS;
	public static Set<State> expS;
	
	public static Set<State> statesP;

	public static HierarchicalSMP build(int parallel, int depth, int sequence, boolean expolSame, RegionType regT) {
		TFL.c=0;
		TFL.map = new HashMap<State,Set<State>>();
		TFL.compS= new HashSet<State>();
		TFL.expS= new HashSet<State>();
		TFL.expDiffS= new HashSet<State>();
		
		// child, parent
		TFL.parentMap= new HashMap<State,State>();
		TFL.toCopy= new HashMap<State,State>();		
		
		// old1, old2
		TFL.doublesMap= new HashMap<State,State>();
		// old2,old1
		TFL.doublesMapFrom2= new HashMap<State,State>();
		
		// step, region contained in the parent step
		TFL.regMap= new HashMap<State,Region>();
		
		TFL.parallelS=parallel;
		TFL.depthS=depth;
		TFL.sequenceS=sequence;

		TFL.statesS= new HashSet<String>();
		TFL.statesP= new HashSet<State>();

		List<Region> rListAll = new LinkedList<Region>();
		for(int i=0;i<parallelS;i++) {

			Region newR = new Region(null, regT);
			rListAll.add(newR);
		}

		//System.out.println("p="+parallelS+" d="+depthS+" s="+sequenceS);

		List<State> nextStates = null;//Required to avoid ambiguity

		State S0 = new CompositeState(
				"S0",  
				rListAll, 
				nextStates, 
				null, 
				0);
		map.put(S0, new HashSet<State>());
		compS.add(S0);
		statesS.add(S0.getName());
		statesP.add(S0);

		S0.setNextStates(Arrays.asList(S0), Arrays.asList(1.0));

		for(int i=0;i<parallelS;i++) {

			State initial = buildInner(rListAll.get(i), S0, 1, expolSame, regT);
			rListAll.get(i).setInitialState(initial);
		}

		System.out.println("c is "+ c);
		return new HierarchicalSMP(S0);
	}

	private static State buildInner(Region parentRegion, State parent, int currentDepth, boolean expolSame, RegionType regT) {

		GEN exp = GEN.newExpolynomial("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2", OmegaBigDecimal.ZERO, OmegaBigDecimal.ONE);
//exp= GEN.newDeterministic(BigDecimal.valueOf(0.5));
		
		GEN expDiff;

		if(expolSame)
			expDiff= exp;
		else {
			expDiff = GEN.newExpolynomial("0.22517 * Exp[-0.311427 x] * x + -0.022517 * Exp[-0.311427 x] * x^2", OmegaBigDecimal.ZERO, OmegaBigDecimal.TEN); 
		}

		State current;

		List<State> nextStates = null;//Required to avoid ambiguity

		if(currentDepth!=TFL.depthS) {
			
			List<Region> rListAll = new LinkedList<Region>();
			for(int i=0;i<parallelS;i++) {

				Region newR = new Region(null, regT);
				rListAll.add(newR);
			}

			//System.out.println("p="+parallelS+" d="+depthS+" s="+sequenceS);

			current = new CompositeState(
					"comp_"+currentDepth+"_"+c++,  
					rListAll, 
					nextStates, 
					null, 
					currentDepth);
			
			compS.add(current);
			parentMap.put(current, parent);
			regMap.put(current, parentRegion);
			
			map.put(current, new HashSet<State>());
			map.get(parent).add(current);
			statesS.add(current.getName());
			statesP.add(current);
			
			for(int i=0;i<parallelS;i++) {

				State initial = buildInner(rListAll.get(i), current, currentDepth+1, expolSame, regT);

				rListAll.get(i).setInitialState(initial);
			}

		} else {

			current = new SimpleState(
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

		State E = new FinalState("e_"+c++, currentDepth);

		State stateD112 = new SimpleState(
				"d1_"+currentDepth+"_"+c++,
				exp,
				Arrays.asList(E), 
				Arrays.asList(1.0), 
				currentDepth); 
		
		State stateD122 = new SimpleState(
				"d2_"+currentDepth+"_"+c++,
				expDiff,
				Arrays.asList(E), 
				Arrays.asList(1.0), 
				currentDepth); 

		State old1= stateD112;
		State old2= stateD122;
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

			State stateD111 = new SimpleState(
					"d1--_"+currentDepth+"_"+c++,
					exp,
					Arrays.asList(old1), 
					Arrays.asList(1.0), 
					currentDepth); 

			State stateD121 = new SimpleState(
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

		current.setNextStates(Arrays.asList(old1,old2), Arrays.asList(0.5,0.5));

		return current;
	}
}
