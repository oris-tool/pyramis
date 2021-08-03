package it.unifi.hierarchical.model.example.hsmp_scalability;

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
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.utils.StateUtils;

public class HSMP_JournalCycles {

	public static int parallelS;
	public static int depthS;
	public static int sequenceS;

	private static int c;

	public static Set<String> statesS;

	public static Set<String> getStates(){
		return statesS;
	}

	public static LinkedList<State> firstLeaf;

	public static LinkedList<State> secondLeaf;

	public static LinkedList<State> thirdLeaf;


	//per ogni stato tutti gli stati dentro le sue regioni (composite e simple)
	public static Map<State,Set<State>> map;
	public static Map<State,State> parentMap;
	public static Map<State,State> doublesMap;
	public static Map<State,State> doublesMapFrom2;
	public static Map<State,Region> regMap;

	public static Map<String,String>  toCopy;

	public static Set<State> compS;

	public static Set<State> det;
	public static Map<State,State> loopsMap;
	
	
	//contengono solo il primo della sequenza
	public static Set<State> expDiffS;
	public static Set<State> expS;

	public static Set<String> zeros;

	public static Integer LOOP;


	public static Set<State> statesP;

	public static HierarchicalSMP build(int leaf, int parallel, int depth, int sequence, boolean expolSame, RegionType regT, int LOOP) {
		HSMP_JournalCycles.c=0;
		HSMP_JournalCycles.map = new HashMap<State,Set<State>>();
		HSMP_JournalCycles.compS= new HashSet<State>();
		HSMP_JournalCycles.expS= new HashSet<State>();
		HSMP_JournalCycles.expDiffS= new HashSet<State>();
		
		HSMP_JournalCycles.det= new HashSet<State>();


		HSMP_JournalCycles.firstLeaf= new LinkedList<State>();
		HSMP_JournalCycles.secondLeaf= new LinkedList<State>();
		HSMP_JournalCycles.thirdLeaf= new LinkedList<State>();
		HSMP_JournalCycles.LOOP= LOOP;

		HSMP_JournalCycles.zeros= new HashSet<String>();
		//figlio, genitore
		HSMP_JournalCycles.parentMap= new HashMap<State,State>();
		HSMP_JournalCycles.toCopy= new HashMap<String,String>();
		HSMP_JournalCycles.loopsMap= new HashMap<State,State>();
		

		//old1, old2
		HSMP_JournalCycles.doublesMap= new HashMap<State,State>();
		//old2,old1
		HSMP_JournalCycles.doublesMapFrom2= new HashMap<State,State>();

		//stato, regione parte del genitore
		HSMP_JournalCycles.regMap= new HashMap<State,Region>();


		HSMP_JournalCycles.parallelS=parallel;
		HSMP_JournalCycles.depthS=depth;
		HSMP_JournalCycles.sequenceS=sequence;

		HSMP_JournalCycles.statesS= new HashSet<String>();
		HSMP_JournalCycles.statesP= new HashSet<State>();

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

			State initial = buildInner(leaf, rListAll.get(i), S0, 1, expolSame, regT, 0);
			rListAll.get(i).setInitialState(initial);
			System.out.println(rListAll.get(i));
		}

		System.out.println("c is "+ c);
		return new HierarchicalSMP(S0);



	}

	private static State buildInner(int leaf, Region parentRegion, State parent, int currentDepth, boolean expolSame, RegionType regT, int inCycleStatus) {

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






		State current = null;

		List<State> nextStates = null;//Required to avoid ambiguity

		if(!cycle && inCycleStatus!=1) {


			if(currentDepth!=HSMP_JournalCycles.depthS && inCycleStatus==0) {



				List<Region> rListAll = new LinkedList<Region>();
				for(int i=0;i<parallelS;i++) {

					Region newR = new Region(null, regT);
					rListAll.add(newR);
				}

				//System.out.println("p="+parallelS+" d="+depthS+" s="+sequenceS);



				current = new CompositeState(
						"normalComposite_"+currentDepth+"_"+c++,  
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

					State initial = buildInner(leaf,rListAll.get(i), current, currentDepth+1, expolSame, regT, 0);

					rListAll.get(i).setInitialState(initial);
				}


			} else  {

				current = new SimpleState(
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

			State E = new FinalState("e_"+c++, currentDepth);



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

				
			}

			expS.add(old1);
			expDiffS.add(old2);


			current.setNextStates(Arrays.asList(old1,old2), Arrays.asList(0.5,0.5));

		

		}else if(cycle) {
			
			current = new SimpleState(
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


			State[] sA= new State[LOOP];
			State[] sEmpty = new State[LOOP];

			State[] End1= new State[LOOP];
			State[] End2= new State[LOOP];

			for(int i=0;i<LOOP;i++) {
				
				State empty = new SimpleState(
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


				State cyc = new CompositeState(
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

				map.put(cyc, new HashSet<State>());
				map.get(parent).add(cyc);
				statesS.add(cyc.getName());
				statesP.add(cyc);
				
				sA[i]=cyc;

				State initial1 = buildInner(leaf,rListAll.get(0), cyc, currentDepth+1, expolSame, regT, 1);

				
				rListAll.get(0).setInitialState(initial1);
				
				State end1 = StateUtils.findEndState(rListAll.get(0));
				End1[i]=end1;
				
				State endinit= new ExitState("eLoopDet_"+c++,currentDepth+1);
				End2[i]=endinit;
				
							
				State initial2 = new SimpleState(
						"init_"+currentDepth+"_"+c++,
						GEN.newDeterministic(new BigDecimal(0.5)),
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
			
			State E = new ExitState("exitOfCycleRegion_"+c++, currentDepth);



			State stateD112 = new SimpleState(
					"d1CycleReg_"+currentDepth+"_"+c++,
					expC,
					Arrays.asList(E), 
					Arrays.asList(1.0), 
					currentDepth); 

			State stateD122 = new SimpleState(
					"d2CycleReg_"+currentDepth+"_"+c++,
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

				
			}

			expS.add(old1);
			expDiffS.add(old2);
	
	
			for(int i=0;i<LOOP-1;i++) {
				loopsMap.put(sA[i], sA[i+1]);
				
				((CompositeState) sA[i]).setNextStatesConditional(Map.of(
						End1[i],
						Arrays.asList(old1,old2),
						End2[i],
						Arrays.asList(sEmpty[i])));

				((CompositeState) sA[i]).setBranchingProbsConditional(Map.of(
						End1[i],
						Arrays.asList(0.5,0.5),
						End2[i],
						Arrays.asList(1.0)));
				
				sEmpty[i].setNextStates(Arrays.asList(sA[i+1]), Arrays.asList(1.0));
			}
			
			sA[LOOP-1].setNextStates(Arrays.asList(old1,old2), Arrays.asList(0.5,0.5));
		
			current.setNextStates(Arrays.asList(sA[0]), Arrays.asList(1.0));

			

		}else if(inCycleStatus==1) {

			
			

			List<Region> rListAll = new LinkedList<Region>();
			for(int i=0;i<parallelS;i++) {

				Region newR = new Region(null, regT);
				rListAll.add(newR);
			}

			//System.out.println("p="+parallelS+" d="+depthS+" s="+sequenceS);
			
			State E = new ExitState("exitInsideCycle_"+c++, currentDepth);



			current = new CompositeState(
					"compInCycle_"+currentDepth+"_"+c++,  
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

				State initial = buildInner(leaf,rListAll.get(i), current, currentDepth+1, expolSame, regT, 2);

				rListAll.get(i).setInitialState(initial);
			}
			
			current.setNextStates(Arrays.asList(E), Arrays.asList(1.0));

			
			
		}


		return current;

	}



}
