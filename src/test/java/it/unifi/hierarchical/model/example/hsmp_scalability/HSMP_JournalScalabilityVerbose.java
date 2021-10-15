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

package it.unifi.hierarchical.model.example.hsmp_scalability;

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
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.Region.RegionType;

public class HSMP_JournalScalabilityVerbose {

	public static int parallelS;
	public static int depthS;
	public static int sequenceS;

	private static int c;

	public static Set<String> statesS;

	public static Set<String> getStates(){
		return statesS;
	}
	
	
	//per ogni stato tutti gli stati dentro le sue regioni (composite e simple)
	public static Map<State,Set<State>> map;
	public static Map<State,State> parentMap;
	public static Map<State,State> doublesMap;
	public static Map<State,State> doublesMapFrom2;
	public static Map<State,Region> regMap;
	
	public static Set<String> zeros;
	
	public static Map<String,String>  toCopy;
	
	public static Set<State> compS;
	
	//contengono solo il primo della sequenza
	public static Set<State> expDiffS;
	public static Set<State> expS;
	
	public static Map<Integer,Map<Integer,LinkedList<String>>> internalLeaves;
	public static Map<String,String>  couple;
	
	
	public static Set<State> statesP;

	public static HierarchicalSMP build(int parallel, int depth, int sequence, boolean expolSame, RegionType regT) {
		HSMP_JournalScalabilityVerbose.c=0;
		HSMP_JournalScalabilityVerbose.map = new HashMap<State,Set<State>>();
		HSMP_JournalScalabilityVerbose.compS= new HashSet<State>();
		HSMP_JournalScalabilityVerbose.expS= new HashSet<State>();
		HSMP_JournalScalabilityVerbose.expDiffS= new HashSet<State>();
		HSMP_JournalScalabilityVerbose.zeros= new HashSet<String>();
		
		//figlio, genitore
		HSMP_JournalScalabilityVerbose.parentMap= new HashMap<State,State>();
		HSMP_JournalScalabilityVerbose.toCopy= new HashMap<String,String>();
		
		HSMP_JournalScalabilityVerbose.internalLeaves= new HashMap<Integer,Map<Integer,LinkedList<String>>>();
		
		for(int i=1; i<=depth+1;i++) {
		
			HashMap<Integer,LinkedList<String>> x = new HashMap<Integer,LinkedList<String>>();
			
			for(int k=0; k<sequence;k++) {
				x.put(k, new LinkedList<String>());
			}
			internalLeaves.put(i, x);
		}
		
		
		
		//old1, old2
		HSMP_JournalScalabilityVerbose.doublesMap= new HashMap<State,State>();
		//old2,old1
		HSMP_JournalScalabilityVerbose.doublesMapFrom2= new HashMap<State,State>();
		
		//stato, regione parte del genitore
		HSMP_JournalScalabilityVerbose.regMap= new HashMap<State,Region>();
		
		
		HSMP_JournalScalabilityVerbose.parallelS=parallel;
		HSMP_JournalScalabilityVerbose.depthS=depth;
		HSMP_JournalScalabilityVerbose.sequenceS=sequence;

		HSMP_JournalScalabilityVerbose.statesS= new HashSet<String>();
		HSMP_JournalScalabilityVerbose.statesP= new HashSet<State>();

		//TODO make pyramis like: al top level sta direttamente il primo AND,
		// richiede di partire da depth>=2
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

		GEN exp =GEN.newExpolynomial("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2", OmegaBigDecimal.ZERO, OmegaBigDecimal.ONE);
		
		GEN expDiff;

		if(expolSame)
			expDiff= exp;
		else {
			expDiff = GEN.newExpolynomial("0.22517 * Exp[-0.311427 x] * x + -0.022517 * Exp[-0.311427 x] * x^2", OmegaBigDecimal.ZERO, OmegaBigDecimal.TEN); 
		}


		State current;

		List<State> nextStates = null;//Required to avoid ambiguity


		if(currentDepth!=HSMP_JournalScalabilityVerbose.depthS) {

			
			
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
			
			zeros.add(current.getName());
			
		}

		State E;
		
		if(regT==RegionType.FINAL) {
		E= new FinalState("e_"+c++, currentDepth);
		}else {
			E=new ExitState("e_"+c++, currentDepth);
		}


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
		internalLeaves.get(currentDepth+1).get(sequenceS-1).add(old1.getName()) ;
		internalLeaves.get(currentDepth+1).get(sequenceS-1).add(old2.getName()) ;
		
		//currentDepth==HSMP_Journal0TimeOnDet.depthS
		
		if(true) {
			toCopy.put(old1.getName(), old2.getName());
		}
		
		
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
			//currentDepth==HSMP_Journal0TimeOnDet.depthS
			if(true) {
				toCopy.put(old1.getName(), old2.getName());
			}
			
			internalLeaves.get(currentDepth+1).get(sequenceS-1-r).add(old1.getName()) ;
			internalLeaves.get(currentDepth+1).get(sequenceS-1-r).add(old2.getName()) ;
			
			
			
		}
		
		expS.add(old1);
		expDiffS.add(old2);
		
		
		
		
		current.setNextStates(Arrays.asList(old1,old2), Arrays.asList(0.5,0.5));

		
		
		return current;

	}



}
