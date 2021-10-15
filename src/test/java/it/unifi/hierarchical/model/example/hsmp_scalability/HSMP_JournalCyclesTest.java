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
import java.util.LinkedList;
import java.util.List;
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

public class HSMP_JournalCyclesTest {

	public static int parallelS;
	public static int depthS;
	public static int sequenceS;



	//per ogni stato tutti gli stati dentro le sue regioni (composite e simple)

	public static Integer LOOP;


	public static Set<State> statesP;

	public static HierarchicalSMP build(int parallel, int sequence,RegionType regT) {

		
		GEN expC = GEN.newExpolynomial("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2", OmegaBigDecimal.ZERO, OmegaBigDecimal.ONE);




		HSMP_JournalCyclesTest.parallelS=parallel;
		HSMP_JournalCyclesTest.sequenceS=sequence;

		List<Region> rListAll = new LinkedList<Region>();
		for(int i=0;i<4;i++) {

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

		int c=0;

		S0.setNextStates(Arrays.asList(S0), Arrays.asList(1.0));


		for(int i=0;i<parallelS;i++) {


			State current = new SimpleState(
					"zeroNotCycle_"+1+"_"+c++,
					GEN.newDeterministic(new BigDecimal(0)),
					nextStates,
					null, 
					1);	


			State E = new FinalState("e_"+c++, 1);



			State stateD112 = new SimpleState(
					"d1_"+1+"_"+c++,
					expC,
					Arrays.asList(E), 
					Arrays.asList(1.0), 
					1); 

			State stateD122 = new SimpleState(
					"d2_"+1+"_"+c++,
					expC,
					Arrays.asList(E), 
					Arrays.asList(1.0), 
					1); 

			State old1= stateD112;
			State old2= stateD122;
			
			

			for(int r=1;r<sequenceS;r++) {

				State stateD111 = new SimpleState(
						"d1--_"+1+"_"+c++,
						expC,
						Arrays.asList(old1), 
						Arrays.asList(1.0), 
						1); 

				State stateD121 = new SimpleState(
						"d2--_"+1+"_"+c++,
						expC,
						Arrays.asList(old2), 
						Arrays.asList(1.0), 
						1); 
				old1= stateD111;
				old2=stateD121;
				

			}

			
			current.setNextStates(Arrays.asList(old1,old2), Arrays.asList(0.5,0.5));


			rListAll.get(i).setInitialState(current);
			
		}
		State E = new FinalState("e_"+c++, 1);


		State initial2 = new SimpleState(
				"init_"+1+"_"+c++,
				GEN.newDeterministic(new BigDecimal(0.75)),
				Arrays.asList(E),
				Arrays.asList(1.0), 
				1);		

		rListAll.get(3).setInitialState(initial2);



		System.out.println("c is "+ c);
		return new HierarchicalSMP(S0);



	}





}
