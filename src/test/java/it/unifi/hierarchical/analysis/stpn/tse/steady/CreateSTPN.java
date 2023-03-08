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

package it.unifi.hierarchical.analysis.stpn.tse.steady;

import it.unifi.hierarchical.model.Region.RegionType;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.domain.DBMZone;
import org.oristool.math.expression.Expolynomial;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.GEN;
import org.oristool.math.function.PartitionedGEN;
import org.oristool.models.pn.PostUpdater;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

import java.math.BigDecimal;
import java.util.*;

/**
 * This class supports the definition of STPN model corresponding to the HSMP model
 * used in the case study on steady-state analysis of software rejuvenation in virtual servers 
 * of the paper titled "Compositional Analysis of Hierarchical UML Statecharts" (see Figure 8).
 */
public class CreateSTPN {

	public static int parallelS;
	public static int depthS;
	public static int sequenceS;
	public static PetriNet net;
	public static Marking marking;

	public static List<GEN> pGen;
	private static int c;

	public static Set<String> statesS;

	public static Set<String> getStates(){
		return statesS;
	}

	// for each stesp, all the (simple and composite) steps contained in its regions
	public static Map<Place,Set<Place>> map;
	public static Map<Place,Place> parentMap;

	public static void build(PetriNet net, Marking marking, int parallel, int depth, int sequence, boolean expolSame, RegionType regT, Map<Integer, Map<Integer, LinkedList<String>>> intLeaves) {
		CreateSTPN.c=0;

		// child, parent
		CreateSTPN.parentMap= new HashMap<Place,Place>();

		CreateSTPN.net=net;
		CreateSTPN.marking=marking;

		List<GEN> t2_gens = new ArrayList<>();

		DBMZone t2_d_0 = new DBMZone(new Variable("x"));
		Expolynomial t2_e_0 = Expolynomial.fromString("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2");
		t2_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("1"));
		t2_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("0"));
		GEN t2_gen_0 = new GEN(t2_d_0, t2_e_0);
		t2_gens.add(t2_gen_0);
	
		CreateSTPN.pGen = t2_gens;
		CreateSTPN.parallelS=parallel;
		CreateSTPN.depthS=depth;
		CreateSTPN.sequenceS=sequence;

		CreateSTPN.statesS= new HashSet<String>();

		//System.out.println("p="+parallelS+" d="+depthS+" s="+sequenceS);
		
		Place end= net.addPlace("end");

		Transition tEnd=null;
		if(regT==RegionType.FINAL) {
			tEnd= net.addTransition("tE_"+c);
			net.addPostcondition(tEnd, end);
			tEnd.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
			tEnd.addFeature(new Priority(0));
		}

		List<List<Place>> pLL= new LinkedList<List<Place>>();
		List<Transition> tL= new LinkedList<Transition>();
		for(int i=0;i<parallelS;i++) {

			List<Place> pList= new LinkedList<Place>();

			Transition t = buildInner(pList, end,tEnd, 1, expolSame, regT, intLeaves);
			pLL.add(pList);
			tL.add(t);
		}
		if(regT!=RegionType.FINAL) {
			for(int x=0;x<tL.size();x++) {

				String update="";
				for(int v=0;v<tL.size();v++) {					
					if(v!=x) {
						List<Place> ll=pLL.get(v);
						for(int k=0;k<ll.size();k++) {
							update+= ll.get(k).getName()+"=0,";
						}

					}
				}
				System.out.println(update);
				tL.get(x).addFeature(new PostUpdater(update,net));
			}
		}

		System.out.println("c is "+ c);
	}

	private static Transition buildInner(List<Place> pList, Place parent,Transition tPar, int currentDepth, boolean expolSame, RegionType regT, Map<Integer, Map<Integer, LinkedList<String>>> intLeaves ) {

		Place end = net.addPlace("p"+c++);

		pList.add(end);
		
		Transition t;
		if(regT!=RegionType.FINAL) {
			t=net.addTransition("t"+c);
			net.addPostcondition(t, parent);
			t.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
			t.addFeature(new Priority(0));

		}else {
			t=tPar;

		}
		net.addPrecondition(end, t);

		Place old1=end;
		Place old2=end;
		for(int i=0;i<sequenceS;i++) {

			Place p1= net.addPlace(intLeaves.get(currentDepth+1).get(sequenceS-1-i).pop());
			Transition t1= net.addTransition("t1_"+c++);
			Place p2= net.addPlace(intLeaves.get(currentDepth+1).get(sequenceS-1-i).pop());
			Transition t2= net.addTransition("t2_"+c++);

			pList.add(p1);
			pList.add(p2);

			net.addPostcondition(t1, old1);
			net.addPostcondition(t2, old2);
			net.addPrecondition(p1, t1);
			net.addPrecondition(p2, t2);
			old1=p1;
			old2=p2;
			StochasticTransitionFeature t2_feature = StochasticTransitionFeature.of(new PartitionedGEN(pGen));
			t2.addFeature(t2_feature);
			StochasticTransitionFeature t1_feature = StochasticTransitionFeature.of(new PartitionedGEN(pGen));
			t1.addFeature(t1_feature);
		}
		Place pE= net.addPlace("p"+c++);
		
		pList.add(pE);
		Transition t1= net.addTransition("t1_"+c);
		Transition t2= net.addTransition("t2_"+c);
		t1.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
		t1.addFeature(new Priority(0));
		t2.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
		t2.addFeature(new Priority(0));
		net.addPostcondition(t1, old1);
		net.addPostcondition(t2, old2);
		net.addPrecondition(pE, t1);
		net.addPrecondition(pE, t2);

		Transition tEnd=null;
		if(regT==RegionType.FINAL && currentDepth+1<CreateSTPN.depthS) {
			tEnd= net.addTransition("tE_"+c);
			net.addPostcondition(tEnd, pE);
			tEnd.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
			tEnd.addFeature(new Priority(0));

		}

		if(currentDepth+1<=CreateSTPN.depthS) {
			List<List<Place>> pLL= new LinkedList<List<Place>>();
			List<Transition> tL= new LinkedList<Transition>();
			for(int i=0;i<parallelS;i++) {

				List<Place> pLt= new LinkedList<Place>();

				Transition tT= buildInner(pLt,pE,tEnd, currentDepth+1, expolSame, regT, intLeaves);
				
				pList.addAll(pLt);
				pLL.add(pLt);
				tL.add(tT);

			}
			if(regT!=RegionType.FINAL && currentDepth+1<CreateSTPN.depthS) {
				for(int x=0;x<tL.size();x++) {

					String update="";
					for(int v=0;v<tL.size();v++) {					
						if(v!=x) {
							List<Place> ll=pLL.get(v);
							for(int k=0;k<ll.size();k++) {
								update+= ll.get(k).getName()+"=0,";
							}

						}
					}
					System.out.println(update);
					System.out.println(tL.size());
					System.out.println();
					System.out.println(x);
					tL.get(x).addFeature(new PostUpdater(update,net));
				}
			}

		}else {
			marking.addTokens(pE, 1);
		}

		return t;
	}
}
