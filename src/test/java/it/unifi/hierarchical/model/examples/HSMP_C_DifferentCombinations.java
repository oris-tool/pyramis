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

package it.unifi.hierarchical.model.example.hsmp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.domain.DBMZone;
import org.oristool.math.expression.Expolynomial;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.GEN;
import org.oristool.math.function.PartitionedGEN;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.Region.RegionType;

public class HSMP_C_DifferentCombinations {

    public static HierarchicalSMP build(int RgtRej,int RgtRep, int weekC) {
    	
    	
    	// median 524.8
    	// The failure time is observed to be: 
		// lower than 72 h (3 days) with probability 0.001
		// lower than 144 h (6 days) with probability 0.006
		// lower than 216 h (9 days) with probability 0:016
		// larger than 216 h (9 days) with probability 0.984 and mean value equal to 672 h (28 days)

		// fail is a GEN transition with piece-wise distribution represented over 4 intervals
		List<GEN> fail_gens = new ArrayList<>();

		// 1st interval: uniform distribution over [0,72]
		DBMZone fail_d_0 = new DBMZone(Variable.X);
		Expolynomial fail_e_0 = Expolynomial.fromString("0.0000139"); // constant
		fail_d_0.setCoefficient(Variable.X, Variable.TSTAR, new OmegaBigDecimal("72")); // t - tstar <= 72
		fail_d_0.setCoefficient(Variable.TSTAR, Variable.X, new OmegaBigDecimal("0"));  // t - tstart >= 0 (i.e., tstar - t <= 0)
		GEN fail_gen_0 = new GEN(fail_d_0, fail_e_0);
		fail_gens.add(fail_gen_0);

		// 2nd interval: uniform distribution over [72,144]
		DBMZone fail_d_1 = new DBMZone(Variable.X);
		Expolynomial fail_e_1 = Expolynomial.fromString("0.0000694"); // constant
		fail_d_1.setCoefficient(Variable.X, Variable.TSTAR, new OmegaBigDecimal("144")); // t - tstar <= 144
		fail_d_1.setCoefficient(Variable.TSTAR, Variable.X, new OmegaBigDecimal("-72")); // t - tstar >= 72
		GEN fail_gen_1 = new GEN(fail_d_1, fail_e_1);
		fail_gens.add(fail_gen_1);

		// 3rd interval: uniform distribution over [144,216]
		DBMZone fail_d_2 = new DBMZone(Variable.X);
		Expolynomial fail_e_2 = Expolynomial.fromString("0.000139"); // constant
		fail_d_2.setCoefficient(Variable.X, Variable.TSTAR, new OmegaBigDecimal("216"));  // t - tstar <= 216
		fail_d_2.setCoefficient(Variable.TSTAR, Variable.X, new OmegaBigDecimal("-144")); // t - tstar >= 144
		GEN fail_gen_2 = new GEN(fail_d_2, fail_e_2);
		fail_gens.add(fail_gen_2);

		// 4th interval: shifted exponential distribution over [216,infty)
		DBMZone fail_d_3 = new DBMZone(Variable.X);
		Expolynomial fail_e_3 = Expolynomial.fromString("0.00347 * Exp[-0.00219 x]"); // exponential
		fail_d_3.setCoefficient(Variable.X, Variable.TSTAR, OmegaBigDecimal.POSITIVE_INFINITY); // t - tstar < infty
		fail_d_3.setCoefficient(Variable.TSTAR, Variable.X, new OmegaBigDecimal("-216"));       // t - tstar >= 216
		GEN fail_gen_3 = new GEN(fail_d_3, fail_e_3);
		fail_gens.add(fail_gen_3);

		PartitionedGEN fail_pFunction = new PartitionedGEN(fail_gens);
        	  
	    List<GEN> repair48_gens = new ArrayList<>();

	    DBMZone repair48_d_0 = new DBMZone(new Variable("x"));
	    Expolynomial repair48_e_0 = Expolynomial.fromString("-0.033816 * Exp[-0.0378971 x]+0.0091585 * Exp[-0.0378971 x] *x^1 + -0.000176125 * Exp[-0.0378971 x] *x^2");
	    //Normalization
	    repair48_e_0.multiply(new BigDecimal(0.9999965462400165));
	    repair48_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("48"));
	    repair48_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-4"));
	    GEN repair48_gen_0 = new GEN(repair48_d_0, repair48_e_0);
	    repair48_gens.add(repair48_gen_0);

	    PartitionedGEN repair48_pFunction = new PartitionedGEN(repair48_gens);
	    List<GEN> repair72_gens = new ArrayList<>();

	    DBMZone repair72_d_0 = new DBMZone(new Variable("x"));
	    Expolynomial repair72_e_0 = Expolynomial.fromString("-0.0120921 * Exp[-0.0222461 x]+0.00319098 * Exp[-0.0222461 x] *x^1 + -0.0000419866 * Exp[-0.0222461 x] *x^2");
	    //Normalization
	    repair72_e_0.multiply(new BigDecimal(0.9999990347952876));
	    repair72_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("72"));
	    repair72_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-4"));
	    GEN repair72_gen_0 = new GEN(repair72_d_0, repair72_e_0);
	    repair72_gens.add(repair72_gen_0);

	    PartitionedGEN repair72_pFunction = new PartitionedGEN(repair72_gens);
	    List<GEN> repair96_gens = new ArrayList<>();

	    DBMZone repair96_d_0 = new DBMZone(new Variable("x"));
	    Expolynomial repair96_e_0 = Expolynomial.fromString("-0.00613993 * Exp[-0.0156263 x]+0.00159894 * Exp[-0.0156263 x] *x^1 + -0.0000159894 * Exp[-0.0156263 x]*x^2");
	    //Normalization
	    repair96_e_0.multiply(new BigDecimal(1.0000002067529676));
	    repair96_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("96"));
	    repair96_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-4"));
	    GEN repair96_gen_0 = new GEN(repair96_d_0, repair96_e_0);
	    repair96_gens.add(repair96_gen_0);

	    PartitionedGEN repair96_pFunction = new PartitionedGEN(repair96_gens);

	    GEN rejuvenationFunction;
	    PartitionedGEN reparationFunction;

		if(RgtRej == 16) {
			rejuvenationFunction = GEN.newUniform(new OmegaBigDecimal("4"), new OmegaBigDecimal("16"));
		}else if(RgtRej == 20) {
			rejuvenationFunction = GEN.newUniform(new OmegaBigDecimal("4"), new OmegaBigDecimal("20"));
		}else if(RgtRej == 24) {
			rejuvenationFunction = GEN.newUniform(new OmegaBigDecimal("4"), new OmegaBigDecimal("24"));
		}else {
			System.out.println("Error: RgtRejuvenation can only accept 16 20 and 24");
			return null;
		}

		if(RgtRep == 48) {
			reparationFunction = repair48_pFunction;
		}else if(RgtRep == 72) {
			reparationFunction = repair72_pFunction;
		}else if(RgtRep == 96) {
			reparationFunction = repair96_pFunction;
		}else {
			System.out.println("Error: RgtRepair can only accept 48 72 and 96");
			return null;
		}
		
		
		
      
        //Level depth 1
        int depth = 1;
        
        State E1 = new ExitState("E1",1);
        State E2 = new ExitState("E2",1);
                
        State state11 = new SimpleState(
                "State11", 
                fail_pFunction, 
                Arrays.asList(E1), 
                Arrays.asList(1.0), 
                depth);
        
        State state12 = new SimpleState(
                "State12", 
                GEN.newDeterministic(new BigDecimal(weekC*168)), 
                Arrays.asList(E2), 
                Arrays.asList(1.0), 
                depth);
        
        
        Region region1 = new Region(state11, RegionType.EXIT);
        Region region2 = new Region(state12, RegionType.EXIT);
      
        
        //Level depth 0
        depth = 0;
        
        List<State> nextStates = null;//Required to avoid ambiguity
        
        
        State state2 = new SimpleState(
                "State2", 
                reparationFunction, 
                nextStates, 
                null, 
                depth);
        State state3 = new SimpleState(
                "State3", 
                rejuvenationFunction, 
                nextStates, 
                null, 
                depth);
          
        
        State state1 = new CompositeState(
                "State1",  
                Arrays.asList(region1, region2), 
                Map.of(
                        E1,
                        Arrays.asList(state2),
                        E2,
                        Arrays.asList(state3)),  
                Map.of(
                        E1,
                        Arrays.asList(1.0),
                        E2,
                        Arrays.asList(1.0)), 
                depth);
        
        state2.setNextStates(Arrays.asList(state1), Arrays.asList(1.0));
        state3.setNextStates(Arrays.asList(state1), Arrays.asList(1.0));
        
        
        return new HierarchicalSMP(state1);
    }
}
