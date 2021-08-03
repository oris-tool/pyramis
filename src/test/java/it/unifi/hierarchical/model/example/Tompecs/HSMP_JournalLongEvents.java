package it.unifi.hierarchical.model.example.Tompecs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.domain.DBMZone;
import org.oristool.math.expression.Expolynomial;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.GEN;
import org.oristool.math.function.PartitionedGEN;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;

public class HSMP_JournalLongEvents {

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


	public static Set<State> compS;

	//contengono solo il primo della sequenza
	public static Set<State> expDiffS;
	public static Set<State> expS;

	public static Set<String> zeros;


	public static Set<State> firstLeaf;

	public static Set<State> secondLeaf;

	public static Set<State> thirdLeaf;


	public static Set<State> statesP;

	public static HierarchicalSMP build(int leaf, int parallel, int depth, int sequence, boolean expolSame, RegionType regT) {
		HSMP_JournalLongEvents.c=0;
		HSMP_JournalLongEvents.map = new HashMap<State,Set<State>>();
		HSMP_JournalLongEvents.compS= new HashSet<State>();
		HSMP_JournalLongEvents.expS= new HashSet<State>();
		HSMP_JournalLongEvents.expDiffS= new HashSet<State>();

		HSMP_JournalLongEvents.firstLeaf= new HashSet<State>();
		HSMP_JournalLongEvents.secondLeaf= new HashSet<State>();
		HSMP_JournalLongEvents.thirdLeaf= new HashSet<State>();


		HSMP_JournalLongEvents.zeros= new HashSet<String>();
		//figlio, genitore
		HSMP_JournalLongEvents.parentMap= new HashMap<State,State>();
		

		//old1, old2
		HSMP_JournalLongEvents.doublesMap= new HashMap<State,State>();
		//old2,old1
		HSMP_JournalLongEvents.doublesMapFrom2= new HashMap<State,State>();

		//stato, regione parte del genitore
		HSMP_JournalLongEvents.regMap= new HashMap<State,Region>();


		HSMP_JournalLongEvents.parallelS=parallel;
		HSMP_JournalLongEvents.depthS=depth;
		HSMP_JournalLongEvents.sequenceS=sequence;

		HSMP_JournalLongEvents.statesS= new HashSet<String>();
		HSMP_JournalLongEvents.statesP= new HashSet<State>();

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

			State initial = buildInner(leaf, rListAll.get(i), S0, 1, expolSame, regT);
			rListAll.get(i).setInitialState(initial);
		}

		System.out.println("c is "+ c);
		return new HierarchicalSMP(S0);



	}

	private static State buildInner(int leaf, Region parentRegion, State parent, int currentDepth, boolean expolSame, RegionType regT) {

		GEN exp = GEN.newExpolynomial("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2", OmegaBigDecimal.ZERO, OmegaBigDecimal.ONE);

		GEN expLong = GEN.newExpolynomial("0.22517 * Exp[-0.311427 x] * x + -0.022517 * Exp[-0.311427 x] * x^2", OmegaBigDecimal.ZERO, OmegaBigDecimal.TEN); 



		State current;

		List<State> nextStates = null;//Required to avoid ambiguity


		if(currentDepth!=HSMP_JournalLongEvents.depthS) {



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

				State initial = buildInner(leaf,rListAll.get(i), current, currentDepth+1, expolSame, regT);

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

		State E = new FinalState("e_"+c++, currentDepth);


		GEN expC=null;

		if(currentDepth==2 && firstLeaf.isEmpty()) {
			expC=expLong;
		}else if(currentDepth==1 && secondLeaf.isEmpty() && leaf>=2) {
				expC=expLong;
		}else {
			expC=exp;
		}




		State stateD112 = new SimpleState(
				"d1_"+currentDepth+"_"+c++,
				expC,
				Arrays.asList(E), 
				Arrays.asList(1.0), 
				currentDepth); 

		State stateD122 = new SimpleState(
				"d2_"+currentDepth+"_"+c++,
				expC,
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



		boolean first=false;
		boolean second =false;
		boolean third = false;


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

			State stateD111 = new SimpleState(
					"d1--_"+currentDepth+"_"+c++,
					expC,
					Arrays.asList(old1), 
					Arrays.asList(1.0), 
					currentDepth); 

			State stateD121 = new SimpleState(
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
		if(currentDepth==HSMP_JournalLongEvents.depthS) {
			return old1;
		}
		
		return current;

	}



}
