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

public class PN_ARejuvProbability {
	public static void build(PetriNet net, Marking marking, int weeks) {

		 //Generating Nodes
	    Place A1 = net.addPlace("A1");
	    Place A1failed = net.addPlace("A1failed");
	    Place A1rejuv = net.addPlace("A1rejuv");
	    Place A1watchdog = net.addPlace("A1watchdog");
	    Transition failA1 = net.addTransition("failA1");
	    Transition t0 = net.addTransition("t0");
	    Transition waitA = net.addTransition("waitA");

	    //Generating Connectors
	    net.addPrecondition(A1watchdog, waitA);
	    net.addPostcondition(failA1, A1failed);
	    net.addPostcondition(waitA, A1rejuv);
	    net.addPrecondition(A1, failA1);
	    net.addPostcondition(t0, A1);
	    net.addPostcondition(t0, A1watchdog);

	    //Generating Properties
	    marking.setTokens(A1, 1);
	    marking.setTokens(A1failed, 0);
	    marking.setTokens(A1rejuv, 0);
	    marking.setTokens(A1watchdog, 1);
	   


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
		StochasticTransitionFeature fail_feature = StochasticTransitionFeature.of(fail_pFunction);
		failA1.addFeature(fail_feature);
		failA1.addFeature(new PostUpdater("A1watchdog=0", net));

		t0.addFeature(new EnablingFunction("A1failed||A1rejuv"));
		t0.addFeature(new PostUpdater("A1failed=0,A1rejuv=0", net));
		t0.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("10"), MarkingExpr.from("1", net)));
		t0.addFeature(new Priority(0));
		waitA.addFeature(new PostUpdater("A1=0", net));
		waitA.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal(168*weeks), MarkingExpr.from("1", net)));
		waitA.addFeature(new Priority(0));
	}
}
