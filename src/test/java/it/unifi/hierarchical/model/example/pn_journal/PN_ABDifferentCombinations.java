package it.unifi.hierarchical.model.example.pn_journal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
import org.oristool.petrinet.EnablingFunction;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

public class PN_ABDifferentCombinations {
	public static void build(PetriNet net, Marking marking, 
			int RgtRej, int RgtRep, int weekA, int weekAB) {

		
		//Generating Nodes
		Place A1 = net.addPlace("A1");
		Place A1detectedRejuv = net.addPlace("A1detectedRejuv");
		Place A1detectedRepair = net.addPlace("A1detectedRepair");
		Place A1watchdog = net.addPlace("A1watchdog");
		Place A1watchdogEnd = net.addPlace("A1watchdogEnd");
		Place B1 = net.addPlace("B1");
		Place B1detectedRejuv = net.addPlace("B1detectedRejuv");
		Place B1detectedRepair = net.addPlace("B1detectedRepair");
		Place Wait = net.addPlace("Wait");
		Place WaitRejuvA = net.addPlace("WaitRejuvA");
		Place WaitRejuvB = net.addPlace("WaitRejuvB");
		Place WaitReset = net.addPlace("WaitReset");
		Place resetA1 = net.addPlace("resetA1");
		Place resetB1 = net.addPlace("resetB1");
		Transition failA1 = net.addTransition("failA1");
		Transition failB1 = net.addTransition("failB1");
		Transition rejuvA1 = net.addTransition("rejuvA1");
		Transition rejuvA1loop = net.addTransition("rejuvA1loop");
		Transition rejuvB1 = net.addTransition("rejuvB1");
		Transition rejuvResetA = net.addTransition("rejuvResetA");
		Transition rejuvResetB = net.addTransition("rejuvResetB");
		Transition repairA1 = net.addTransition("repairA1");
		Transition repairB1 = net.addTransition("repairB1");
		Transition tresetA1 = net.addTransition("tresetA1");
		Transition tresetB1 = net.addTransition("tresetB1");
		Transition tresetWait = net.addTransition("tresetWait");
		Transition wait8 = net.addTransition("wait8");
		Transition watchdogA = net.addTransition("watchdogA");

		//Generating Connectors
		net.addPostcondition(wait8, WaitRejuvA);
		net.addPrecondition(WaitRejuvB, rejuvResetB);
		net.addPostcondition(failB1, B1detectedRejuv);
		net.addPostcondition(watchdogA, A1watchdogEnd);
		net.addPrecondition(A1watchdog, watchdogA);
		net.addPrecondition(B1detectedRejuv, rejuvA1);
		net.addPostcondition(rejuvResetB, WaitReset);
		net.addPrecondition(A1watchdogEnd, rejuvA1loop);
		net.addPrecondition(A1, failA1);
		net.addPrecondition(A1detectedRepair, repairA1);
		net.addPostcondition(rejuvB1, resetA1);
		net.addPrecondition(Wait, wait8);
		net.addPrecondition(A1detectedRejuv, rejuvB1);
		net.addPostcondition(rejuvResetA, WaitReset);
		net.addPostcondition(repairA1, resetA1);
		net.addPrecondition(resetB1, tresetB1);
		net.addPostcondition(failA1, A1detectedRejuv);
		net.addPostcondition(rejuvA1, resetB1);
		net.addPrecondition(WaitRejuvA, rejuvResetA);
		net.addPostcondition(failB1, B1detectedRepair);
		net.addPrecondition(B1detectedRepair, repairB1);
		net.addPrecondition(WaitReset, tresetWait);
		net.addPrecondition(resetA1, tresetA1);
		net.addPostcondition(repairB1, resetB1);
		net.addPostcondition(wait8, WaitRejuvB);
		net.addPrecondition(B1, failB1);
		net.addPostcondition(failA1, A1detectedRepair);

		//Generating Properties
		marking.setTokens(A1, 1);
		marking.setTokens(A1detectedRejuv, 0);
		marking.setTokens(A1detectedRepair, 0);
		marking.setTokens(A1watchdog, 1);
		marking.setTokens(A1watchdogEnd, 0);
		marking.setTokens(B1, 1);
		marking.setTokens(B1detectedRejuv, 0);
		marking.setTokens(B1detectedRepair, 0);
		marking.setTokens(Wait, 1);
		marking.setTokens(WaitRejuvA, 0);
		marking.setTokens(WaitRejuvB, 0);
		marking.setTokens(WaitReset, 0);
		marking.setTokens(resetA1, 0);
		marking.setTokens(resetB1, 0);
		failA1.addFeature(new EnablingFunction("A1watchdogEnd==0"));
		failA1.addFeature(new PostUpdater("A1watchdog=0,B1=0,Wait=0", net));

		// The failure time is observed to be: 
		// lower than 72 h (3 days) with probability 0.001
		// lower than 144 h (6 days) with probability 0.006
		// lower than 216 h (9 days) with probability 0:016
		// larger than 216 h (9 days) with probability 0.984 and mean value equal to 672 h (28 days)

		// fail is a GEN transition with piece-wise distribution represented over 4 intervals
		List<GEN> failA1_gens = new ArrayList<>();

		DBMZone failA1_d_0 = new DBMZone(new Variable("x"));
		Expolynomial failA1_e_0 = Expolynomial.fromString("0.0000139");
		//Normalization
		failA1_e_0.multiply(new BigDecimal(0.9967107987202807));
		failA1_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("72"));
		failA1_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("0"));
		GEN failA1_gen_0 = new GEN(failA1_d_0, failA1_e_0);
		failA1_gens.add(failA1_gen_0);

		DBMZone failA1_d_1 = new DBMZone(new Variable("x"));
		Expolynomial failA1_e_1 = Expolynomial.fromString("0.0000694");
		//Normalization
		failA1_e_1.multiply(new BigDecimal(0.9967107987202807));
		failA1_d_1.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("144"));
		failA1_d_1.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-72"));
		GEN failA1_gen_1 = new GEN(failA1_d_1, failA1_e_1);
		failA1_gens.add(failA1_gen_1);

		DBMZone failA1_d_2 = new DBMZone(new Variable("x"));
		Expolynomial failA1_e_2 = Expolynomial.fromString("0.000139");
		//Normalization
		failA1_e_2.multiply(new BigDecimal(0.9967107987202807));
		failA1_d_2.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("216"));
		failA1_d_2.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-144"));
		GEN failA1_gen_2 = new GEN(failA1_d_2, failA1_e_2);
		failA1_gens.add(failA1_gen_2);

		DBMZone failA1_d_3 = new DBMZone(new Variable("x"));
		Expolynomial failA1_e_3 = Expolynomial.fromString("0.00347 * Exp[-0.00219 x]");
		//Normalization
		failA1_e_3.multiply(new BigDecimal(0.9967107987202807));
		failA1_d_3.setCoefficient(new Variable("x"), new Variable("t*"), OmegaBigDecimal.POSITIVE_INFINITY);
		failA1_d_3.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-216"));
		GEN failA1_gen_3 = new GEN(failA1_d_3, failA1_e_3);
		failA1_gens.add(failA1_gen_3);

		PartitionedGEN failA1_pFunction = new PartitionedGEN(failA1_gens);
		StochasticTransitionFeature failA1_feature = StochasticTransitionFeature.of(failA1_pFunction);
		failA1.addFeature(failA1_feature);

		failB1.addFeature(new PostUpdater("A1=0,A1watchdog=0,A1watchdogEnd=0,Wait=0", net));
		List<GEN> failB1_gens = new ArrayList<>();

		DBMZone failB1_d_0 = new DBMZone(new Variable("x"));
		Expolynomial failB1_e_0 = Expolynomial.fromString("0.0000139");
		failB1_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("72"));
		failB1_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("0"));
		GEN failB1_gen_0 = new GEN(failB1_d_0, failB1_e_0);
		failB1_gens.add(failB1_gen_0);

		DBMZone failB1_d_1 = new DBMZone(new Variable("x"));
		Expolynomial failB1_e_1 = Expolynomial.fromString("0.0000694");
		failB1_d_1.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("144"));
		failB1_d_1.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-72"));
		GEN failB1_gen_1 = new GEN(failB1_d_1, failB1_e_1);
		failB1_gens.add(failB1_gen_1);

		DBMZone failB1_d_2 = new DBMZone(new Variable("x"));
		Expolynomial failB1_e_2 = Expolynomial.fromString("0.000139");
		failB1_d_2.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("216"));
		failB1_d_2.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-144"));
		GEN failB1_gen_2 = new GEN(failB1_d_2, failB1_e_2);
		failB1_gens.add(failB1_gen_2);

		DBMZone failB1_d_3 = new DBMZone(new Variable("x"));
		Expolynomial failB1_e_3 = Expolynomial.fromString("0.00347 * Exp[-0.00219 x]");
		failB1_d_3.setCoefficient(new Variable("x"), new Variable("t*"), OmegaBigDecimal.POSITIVE_INFINITY);
		failB1_d_3.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-216"));
		GEN failB1_gen_3 = new GEN(failB1_d_3, failB1_e_3);
		failB1_gens.add(failB1_gen_3);

		PartitionedGEN failB1_pFunction = new PartitionedGEN(failB1_gens);
		StochasticTransitionFeature failB1_feature = StochasticTransitionFeature.of(failB1_pFunction);
		failB1.addFeature(failB1_feature);


    
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
	    StochasticTransitionFeature repair48_feature = StochasticTransitionFeature.of(repair48_pFunction);
	    
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
	    StochasticTransitionFeature repair72_feature = StochasticTransitionFeature.of(repair72_pFunction);
	   
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
	    StochasticTransitionFeature repair96_feature = StochasticTransitionFeature.of(repair96_pFunction);
	    

		StochasticTransitionFeature rejuvenationFeature;
		StochasticTransitionFeature reparationFeature;

		if(RgtRej == 16) {
			rejuvenationFeature = StochasticTransitionFeature.newUniformInstance(new BigDecimal("4"), new BigDecimal("16"));
		}else if(RgtRej == 20) {
			rejuvenationFeature = StochasticTransitionFeature.newUniformInstance(new BigDecimal("4"), new BigDecimal("20"));
		}else if(RgtRej == 24) {
			rejuvenationFeature = StochasticTransitionFeature.newUniformInstance(new BigDecimal("4"), new BigDecimal("24"));
		}else {
			System.out.println("Error: RgtRejuvenation can only accept 16 20 and 24");
			return;
		}

		if(RgtRep == 48) {
			reparationFeature = repair48_feature;
		}else if(RgtRep == 72) {
			reparationFeature = repair72_feature;
		}else if(RgtRep == 96) {
			reparationFeature = repair96_feature;
		}else {
			System.out.println("Error: RgtRepair can only accept 48 72 and 96");
			return;
		}


		rejuvA1.addFeature(rejuvenationFeature);
		rejuvA1loop.addFeature(new PostUpdater("A1watchdog=1,A1=1", net));
		rejuvA1loop.addFeature(rejuvenationFeature);
		rejuvB1.addFeature(rejuvenationFeature);
		rejuvResetA.addFeature(rejuvenationFeature);
		rejuvResetB.addFeature(rejuvenationFeature);
		repairA1.addFeature(reparationFeature);
		repairB1.addFeature(reparationFeature);
		tresetA1.addFeature(new EnablingFunction("resetA1==2"));
		tresetA1.addFeature(new PostUpdater("resetA1=0,A1=1,B1=1,A1watchdog=1,Wait=1", net));
		tresetA1.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
		tresetA1.addFeature(new Priority(0));
		tresetB1.addFeature(new EnablingFunction("resetB1==2"));
		tresetB1.addFeature(new PostUpdater("resetB1=0,A1=1,B1=1,A1watchdog=1,Wait=1", net));
		tresetB1.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
		tresetB1.addFeature(new Priority(0));
		tresetWait.addFeature(new EnablingFunction("WaitReset==2"));
		tresetWait.addFeature(new PostUpdater("A1=1,B1=1,A1watchdog=1,Wait=1,WaitReset=0", net));
		tresetWait.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
		tresetWait.addFeature(new Priority(0));
		wait8.addFeature(new PostUpdater("A1=0,A1watchdog=0,A1watchdogEnd=0,B1=0", net));
		wait8.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal(weekAB*168), MarkingExpr.from("1", net)));
		wait8.addFeature(new Priority(0));
		watchdogA.addFeature(new EnablingFunction("A1==1"));
		watchdogA.addFeature(new PostUpdater("A1=0", net));
		watchdogA.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal(weekA*168), MarkingExpr.from("1", net)));
		watchdogA.addFeature(new Priority(0));
	}
}
