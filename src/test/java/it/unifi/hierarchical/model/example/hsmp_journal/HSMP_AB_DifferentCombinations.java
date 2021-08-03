package it.unifi.hierarchical.model.example.hsmp_journal;

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
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;

public class HSMP_AB_DifferentCombinations {

    public static HierarchicalSMP build(int RgtRej, int RgtRep, int weekA, int weekAB) {
    	
    		
    	PartitionedGEN failA1_pFunction=null;
    	double probRejuv=0.0;
    	double probFail=0.0;
    	
    	if(weekA!=1 && weekA!=2 && weekA!=4) {
    		System.out.println("Error: only 1,2 or 4 weeks accepted for the Rejuvenation of A");
    		return null;
    		
    	}else if(weekA==1) {
    	
    	probRejuv = 0.9906971000890645;
    	probFail = 1.0 - probRejuv;
    	
    	
    	//conditioned transition FOR WEEKA = 168
     	// The failure time is observed to be: 
    	// lower than 72 h (3 days) with probability 0.0811
    	// lower than 144 h (6 days) with probability 0.4865
    	// lower than 168 h (7 days) with probability 0.4324
    	// larger than 168 h (7 days) with probability 0 
 	
    	List<GEN> failA1_gens = new ArrayList<>();

        DBMZone failA1_d_0 = new DBMZone(new Variable("x"));
        Expolynomial failA1_e_0 = Expolynomial.fromString("0.001126");
        //Normalization
        failA1_e_0.multiply(new BigDecimal(1.0000160002560041));
        failA1_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("72"));
        failA1_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("0"));
        GEN failA1_gen_0 = new GEN(failA1_d_0, failA1_e_0);
        failA1_gens.add(failA1_gen_0);

        DBMZone failA1_d_1 = new DBMZone(new Variable("x"));
        Expolynomial failA1_e_1 = Expolynomial.fromString("0.006757");
        //Normalization
        failA1_e_1.multiply(new BigDecimal(1.0000160002560041));
        failA1_d_1.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("144"));
        failA1_d_1.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-72"));
        GEN failA1_gen_1 = new GEN(failA1_d_1, failA1_e_1);
        failA1_gens.add(failA1_gen_1);

        DBMZone failA1_d_2 = new DBMZone(new Variable("x"));
        Expolynomial failA1_e_2 = Expolynomial.fromString("0.018017");
        //Normalization
        failA1_e_2.multiply(new BigDecimal(1.0000160002560041));
        failA1_d_2.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("168"));
        failA1_d_2.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-144"));
        GEN failA1_gen_2 = new GEN(failA1_d_2, failA1_e_2);
        failA1_gens.add(failA1_gen_2);

        DBMZone failA1_d_3 = new DBMZone(new Variable("x"));
        Expolynomial failA1_e_3 = Expolynomial.fromString("0");
        //Normalization
        failA1_e_3.multiply(new BigDecimal(1.0000160002560041));
        failA1_d_3.setCoefficient(new Variable("x"), new Variable("t*"), OmegaBigDecimal.POSITIVE_INFINITY);
        failA1_d_3.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-168"));
        GEN failA1_gen_3 = new GEN(failA1_d_3, failA1_e_3);
        failA1_gens.add(failA1_gen_3);

        failA1_pFunction = new PartitionedGEN(failA1_gens);
   	
    	}else if(weekA==2) {
    	 	probRejuv = 0.7566295036778917;
        	probFail = 1.0 - probRejuv;
        	
        	//conditioned transition FOR WEEKA = 168*2=336
         	// The failure time is observed to be: 
        	// lower than 72 h (3 days) with probability 0.0811
        	// lower than 144 h (6 days) with probability 0.4865
        	// lower than 216 h (9 days) with probability 0.4324
        	// larger than 336 h (14 days) with probability 0 
     	
        	List<GEN> failA1_gens = new ArrayList<>();

            DBMZone failA1_d_0 = new DBMZone(new Variable("x"));
            Expolynomial failA1_e_0 = Expolynomial.fromString("0.0000139");
            //Normalization
            failA1_e_0.multiply(new BigDecimal(62.47813265357125));
            failA1_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("72"));
            failA1_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("0"));
            GEN failA1_gen_0 = new GEN(failA1_d_0, failA1_e_0);
            failA1_gens.add(failA1_gen_0);

            DBMZone failA1_d_1 = new DBMZone(new Variable("x"));
            Expolynomial failA1_e_1 = Expolynomial.fromString("0.0000694");
            //Normalization
            failA1_e_1.multiply(new BigDecimal(62.47813265357125));
            failA1_d_1.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("144"));
            failA1_d_1.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-72"));
            GEN failA1_gen_1 = new GEN(failA1_d_1, failA1_e_1);
            failA1_gens.add(failA1_gen_1);

            DBMZone failA1_d_2 = new DBMZone(new Variable("x"));
            Expolynomial failA1_e_2 = Expolynomial.fromString("0.000139");
            //Normalization
            failA1_e_2.multiply(new BigDecimal(62.47813265357125));
            failA1_d_2.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("216"));
            failA1_d_2.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-144"));
            GEN failA1_gen_2 = new GEN(failA1_d_2, failA1_e_2);
            failA1_gens.add(failA1_gen_2);

            DBMZone failA1_d_3 = new DBMZone(new Variable("x"));
            Expolynomial failA1_e_3 = Expolynomial.fromString("0.00347 * Exp[-0.00219 x]");
            //Normalization
            failA1_e_3.multiply(new BigDecimal(62.47813265357125));
            failA1_d_3.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("336"));
            failA1_d_3.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-216"));
            GEN failA1_gen_3 = new GEN(failA1_d_3, failA1_e_3);
            failA1_gens.add(failA1_gen_3);

            failA1_pFunction = new PartitionedGEN(failA1_gens);
    		
    	}else if(weekA==4) {
    	 	probRejuv = 0.362503346696791;
        	probFail = 1.0 - probRejuv;
        	
        	//conditioned transition FOR WEEKA = 168*4=672
         	// The failure time is observed to be: 
        	// lower than 72 h (3 days) with probability 0.0811
        	// lower than 144 h (6 days) with probability 0.4865
        	// lower than 168 h (7 days) with probability 0.4324
        	// larger than 168 h (7 days) with probability 0 
     	
        	List<GEN> failA1_gens = new ArrayList<>();

            DBMZone failA1_d_0 = new DBMZone(new Variable("x"));
            Expolynomial failA1_e_0 = Expolynomial.fromString("0.0000139");
            //Normalization
            failA1_e_0.multiply(new BigDecimal(1.5634761273738331));
            failA1_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("72"));
            failA1_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("0"));
            GEN failA1_gen_0 = new GEN(failA1_d_0, failA1_e_0);
            failA1_gens.add(failA1_gen_0);

            DBMZone failA1_d_1 = new DBMZone(new Variable("x"));
            Expolynomial failA1_e_1 = Expolynomial.fromString("0.0000694");
            //Normalization
            failA1_e_1.multiply(new BigDecimal(1.5634761273738331));
            failA1_d_1.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("144"));
            failA1_d_1.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-72"));
            GEN failA1_gen_1 = new GEN(failA1_d_1, failA1_e_1);
            failA1_gens.add(failA1_gen_1);

            DBMZone failA1_d_2 = new DBMZone(new Variable("x"));
            Expolynomial failA1_e_2 = Expolynomial.fromString("0.000139");
            //Normalization
            failA1_e_2.multiply(new BigDecimal(1.5634761273738331));
            failA1_d_2.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("216"));
            failA1_d_2.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-144"));
            GEN failA1_gen_2 = new GEN(failA1_d_2, failA1_e_2);
            failA1_gens.add(failA1_gen_2);

            DBMZone failA1_d_3 = new DBMZone(new Variable("x"));
            Expolynomial failA1_e_3 = Expolynomial.fromString("0.00347 * Exp[-0.00219 x]");
            //Normalization
            failA1_e_3.multiply(new BigDecimal(1.5634761273738331));
            failA1_d_3.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("672"));
            failA1_d_3.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-216"));
            GEN failA1_gen_3 = new GEN(failA1_d_3, failA1_e_3);
            failA1_gens.add(failA1_gen_3);

            failA1_pFunction = new PartitionedGEN(failA1_gens);
    		
    	}
    	
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

		PartitionedGEN fail_pFunctionB = new PartitionedGEN(fail_gens);
        
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
        State E3 = new ExitState("E3",1);
        
        State E21 = new FinalState("E21",1);
        State E22 = new FinalState("E22",1);
        
        State E31 = new FinalState("E31",1);
        State E32 = new FinalState("E32",1);
        
        State E41 = new FinalState("E41",1);
        State E42 = new FinalState("E42",1);
        
        State state211 = new SimpleState(
                "State211", 
                reparationFunction, 
                Arrays.asList(E21), 
                Arrays.asList(1.0), 
                depth);
        State state212 = new SimpleState(
                "State212", 
                rejuvenationFunction, 
                Arrays.asList(E22), 
                Arrays.asList(1.0), 
                depth);
        
        State state311 = new SimpleState(
                "State311", 
                reparationFunction, 
                Arrays.asList(E31), 
                Arrays.asList(1.0), 
                depth);
        State state312 = new SimpleState(
                "State312", 
                rejuvenationFunction, 
                Arrays.asList(E32), 
                Arrays.asList(1.0), 
                depth);
        
        State state411 = new SimpleState(
                "State411", 
                rejuvenationFunction, 
                Arrays.asList(E41), 
                Arrays.asList(1.0), 
                depth);
        State state412 = new SimpleState(
                "State412", 
                rejuvenationFunction, 
                Arrays.asList(E42), 
                Arrays.asList(1.0), 
                depth);
        
       
        State stateS1 = new SimpleState(
                "StateS1", 
                GEN.newDeterministic(new BigDecimal("0")), 
                null,
                null,
                depth);
        
        State state22 = new SimpleState(
                "State22", 
                rejuvenationFunction, 
                Arrays.asList(stateS1),
                Arrays.asList(1.0), 
                depth);
        
        State state23 = new SimpleState(
                "State23", 
                fail_pFunctionB, 
                Arrays.asList(E2), 
                Arrays.asList(1.0), 
                depth);
        
        State state24 = new SimpleState(
                "State24", 
                GEN.newDeterministic(new BigDecimal(weekAB*168)), 
                Arrays.asList(E3), 
                Arrays.asList(1.0), 
                depth);
        
        
        State state31 = new SimpleState(
                "State31", 
                failA1_pFunction, 
                Arrays.asList(E1), 
                Arrays.asList(1.0), 
                depth);
        
        State state32 = new SimpleState(
                "State32", 
                GEN.newDeterministic(new BigDecimal(weekA*168)), 
                Arrays.asList(state22), 
                Arrays.asList(1.0), 
                depth);
        
       stateS1.setNextStates(Arrays.asList(state31,state32), 
                Arrays.asList(probFail,probRejuv));
        
        Region region1 = new Region(stateS1, RegionType.EXIT);
        Region region2 = new Region(state23, RegionType.EXIT);
        Region region3 = new Region(state24, RegionType.EXIT);
        
        Region region21 = new Region(state211, RegionType.FINAL);
        Region region22 = new Region(state212, RegionType.FINAL);

        Region region31 = new Region(state311, RegionType.FINAL);
        Region region32 = new Region(state312, RegionType.FINAL);

        Region region41 = new Region(state411, RegionType.FINAL);
        Region region42 = new Region(state412, RegionType.FINAL);
           
        //Level depth 0
        depth = 0;
        
        List<State> nextStates = null;//Required to avoid ambiguity
        
        
        State state12 = new CompositeState(
                "State12",  
                Arrays.asList(region21, region22), 
                nextStates, 
                null, 
                depth);
        
        State state13 = new CompositeState(
                "State13",  
                Arrays.asList(region31, region32), 
                nextStates, 
                null, 
                depth);
        
        State state14 = new CompositeState(
                "State14",  
                Arrays.asList(region41, region42), 
                nextStates, 
                null, 
                depth);
        
        
        State state11 = new CompositeState(
                "State11",  
                Arrays.asList(region1, region2, region3), 
                Map.of(
                        E1,
                        Arrays.asList(state12),
                        E2,
                        Arrays.asList(state13),
                        E3,
                        Arrays.asList(state14)),  
                Map.of(
                        E1,
                        Arrays.asList(1.0),
                        E2,
                        Arrays.asList(1.0),
                        E3,
                        Arrays.asList(1.0)), 
                depth);
        
        state12.setNextStates(Arrays.asList(state11), Arrays.asList(1.0));
        state13.setNextStates(Arrays.asList(state11), Arrays.asList(1.0));
        state14.setNextStates(Arrays.asList(state11), Arrays.asList(1.0));
        
        
        return new HierarchicalSMP(state11);
    }
}
