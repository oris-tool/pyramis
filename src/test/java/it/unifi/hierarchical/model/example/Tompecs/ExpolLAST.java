package it.unifi.hierarchical.model.example.Tompecs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.domain.DBMZone;
import org.oristool.math.expression.Expolynomial;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.GEN;
import org.oristool.math.function.PartitionedGEN;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

public class ExpolLAST {
  public static void build(PetriNet net, Marking marking) {

    //Generating Nodes
    Place p0 = net.addPlace("p0");
    Place p1 = net.addPlace("p1");
    Place p10 = net.addPlace("p10");
    Place p11 = net.addPlace("p11");
    Place p12 = net.addPlace("p12");
    Place p13 = net.addPlace("p13");
    Place p14 = net.addPlace("p14");
    Place p15 = net.addPlace("p15");
    Place p16 = net.addPlace("p16");
    Place p17 = net.addPlace("p17");
    Place p2 = net.addPlace("p2");
    Place p3 = net.addPlace("p3");
    Place p4 = net.addPlace("p4");
    Place p5 = net.addPlace("p5");
    Place p6 = net.addPlace("p6");
    Place p7 = net.addPlace("p7");
    Place p8 = net.addPlace("p8");
    Place p9 = net.addPlace("p9");
    Transition t0 = net.addTransition("t0");
    Transition t1 = net.addTransition("t1");
    Transition t10 = net.addTransition("t10");
    Transition t11 = net.addTransition("t11");
    Transition t13 = net.addTransition("t13");
    Transition t14 = net.addTransition("t14");
    Transition t15 = net.addTransition("t15");
    Transition t16 = net.addTransition("t16");
    Transition t17 = net.addTransition("t17");
    Transition t18 = net.addTransition("t18");
    Transition t19 = net.addTransition("t19");
    Transition t2 = net.addTransition("t2");
    Transition t3 = net.addTransition("t3");
    Transition t4 = net.addTransition("t4");
    Transition t5 = net.addTransition("t5");
    Transition t6 = net.addTransition("t6");
    Transition t7 = net.addTransition("t7");
    Transition t8 = net.addTransition("t8");
    Transition t9 = net.addTransition("t9");

    //Generating Connectors
    net.addPostcondition(t5, p5);
    net.addPostcondition(t14, p14);
    net.addPrecondition(p9, t8);
    net.addPrecondition(p6, t11);
    net.addPostcondition(t15, p15);
    net.addPostcondition(t0, p1);
    net.addPostcondition(t13, p13);
    net.addPrecondition(p3, t4);
    net.addPrecondition(p6, t10);
    net.addPrecondition(p7, t6);
    net.addPostcondition(t17, p17);
    net.addPrecondition(p12, t14);
    net.addPrecondition(p1, t2);
    net.addPostcondition(t2, p3);
    net.addPrecondition(p13, t15);
    net.addPrecondition(p0, t0);
    net.addPrecondition(p16, t18);
    net.addPostcondition(t6, p9);
    net.addPostcondition(t9, p11);
    net.addPostcondition(t16, p16);
    net.addPrecondition(p15, t17);
    net.addPostcondition(t18, p17);
    net.addPrecondition(p10, t9);
    net.addPostcondition(t11, p8);
    net.addPostcondition(t1, p2);
    net.addPostcondition(t10, p7);
    net.addPrecondition(p8, t7);
    net.addPrecondition(p4, t5);
    net.addPrecondition(p2, t3);
    net.addPrecondition(p0, t1);
    net.addPostcondition(t7, p10);
    net.addPrecondition(p14, t16);
    net.addPostcondition(t3, p4);
    net.addPostcondition(t4, p5);
    net.addPostcondition(t8, p11);
    net.addPrecondition(p12, t13);
    net.addPrecondition(p5, t19);
    net.addPrecondition(p11, t19);
    net.addPostcondition(t19, p12);

    //Generating Properties
    marking.setTokens(p0, 1);
    marking.setTokens(p1, 0);
    marking.setTokens(p10, 0);
    marking.setTokens(p11, 0);
    marking.setTokens(p12, 0);
    marking.setTokens(p13, 0);
    marking.setTokens(p14, 0);
    marking.setTokens(p15, 0);
    marking.setTokens(p16, 0);
    marking.setTokens(p17, 0);
    marking.setTokens(p2, 0);
    marking.setTokens(p3, 0);
    marking.setTokens(p4, 0);
    marking.setTokens(p5, 0);
    marking.setTokens(p6, 1);
    marking.setTokens(p7, 0);
    marking.setTokens(p8, 0);
    marking.setTokens(p9, 0);
    t0.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
    t0.addFeature(new Priority(0));
    t1.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
    t1.addFeature(new Priority(0));
    t10.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
    t10.addFeature(new Priority(0));
    t11.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
    t11.addFeature(new Priority(0));
    t13.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
    t13.addFeature(new Priority(0));
    t14.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
    t14.addFeature(new Priority(0));
    List<GEN> t15_gens = new ArrayList<>();

    DBMZone t15_d_0 = new DBMZone(new Variable("x"));
    Expolynomial t15_e_0 = Expolynomial.fromString("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2");
    t15_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("1"));
    t15_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("0"));
    GEN t15_gen_0 = new GEN(t15_d_0, t15_e_0);
    t15_gens.add(t15_gen_0);

    PartitionedGEN t15_pFunction = new PartitionedGEN(t15_gens);
    StochasticTransitionFeature t15_feature = StochasticTransitionFeature.of(t15_pFunction);
    t15.addFeature(t15_feature);

    List<GEN> t16_gens = new ArrayList<>();

    DBMZone t16_d_0 = new DBMZone(new Variable("x"));
    Expolynomial t16_e_0 = Expolynomial.fromString("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2");
    t16_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("1"));
    t16_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("0"));
    GEN t16_gen_0 = new GEN(t16_d_0, t16_e_0);
    t16_gens.add(t16_gen_0);

    PartitionedGEN t16_pFunction = new PartitionedGEN(t16_gens);
    StochasticTransitionFeature t16_feature = StochasticTransitionFeature.of(t16_pFunction);
    t16.addFeature(t16_feature);

    List<GEN> t17_gens = new ArrayList<>();

    DBMZone t17_d_0 = new DBMZone(new Variable("x"));
    Expolynomial t17_e_0 = Expolynomial.fromString("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2");
    t17_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("1"));
    t17_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("0"));
    GEN t17_gen_0 = new GEN(t17_d_0, t17_e_0);
    t17_gens.add(t17_gen_0);

    PartitionedGEN t17_pFunction = new PartitionedGEN(t17_gens);
    StochasticTransitionFeature t17_feature = StochasticTransitionFeature.of(t17_pFunction);
    t17.addFeature(t17_feature);

    List<GEN> t18_gens = new ArrayList<>();

    DBMZone t18_d_0 = new DBMZone(new Variable("x"));
    Expolynomial t18_e_0 = Expolynomial.fromString("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2");
    t18_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("1"));
    t18_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("0"));
    GEN t18_gen_0 = new GEN(t18_d_0, t18_e_0);
    t18_gens.add(t18_gen_0);

    PartitionedGEN t18_pFunction = new PartitionedGEN(t18_gens);
    StochasticTransitionFeature t18_feature = StochasticTransitionFeature.of(t18_pFunction);
    t18.addFeature(t18_feature);

    t19.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("0"), MarkingExpr.from("1", net)));
    t19.addFeature(new Priority(0));
    List<GEN> t2_gens = new ArrayList<>();

    DBMZone t2_d_0 = new DBMZone(new Variable("x"));
    Expolynomial t2_e_0 = Expolynomial.fromString("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2");
    //Normalization
    t2_e_0.multiply(new BigDecimal(1.0000003389652903));
    t2_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("1"));
    t2_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("0"));
    GEN t2_gen_0 = new GEN(t2_d_0, t2_e_0);
    t2_gens.add(t2_gen_0);

    PartitionedGEN t2_pFunction = new PartitionedGEN(t2_gens);
    StochasticTransitionFeature t2_feature = StochasticTransitionFeature.of(t2_pFunction);
    t2.addFeature(t2_feature);

    List<GEN> t3_gens = new ArrayList<>();

    DBMZone t3_d_0 = new DBMZone(new Variable("x"));
    Expolynomial t3_e_0 = Expolynomial.fromString("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2");
    t3_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("1"));
    t3_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("0"));
    GEN t3_gen_0 = new GEN(t3_d_0, t3_e_0);
    t3_gens.add(t3_gen_0);

    PartitionedGEN t3_pFunction = new PartitionedGEN(t3_gens);
    StochasticTransitionFeature t3_feature = StochasticTransitionFeature.of(t3_pFunction);
    t3.addFeature(t3_feature);

    List<GEN> t4_gens = new ArrayList<>();

    DBMZone t4_d_0 = new DBMZone(new Variable("x"));
    Expolynomial t4_e_0 = Expolynomial.fromString("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2");
    //Normalization
    t4_e_0.multiply(new BigDecimal(1.0000003389652903));
    t4_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("1"));
    t4_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("0"));
    GEN t4_gen_0 = new GEN(t4_d_0, t4_e_0);
    t4_gens.add(t4_gen_0);

    PartitionedGEN t4_pFunction = new PartitionedGEN(t4_gens);
    StochasticTransitionFeature t4_feature = StochasticTransitionFeature.of(t4_pFunction);
    t4.addFeature(t4_feature);

    List<GEN> t5_gens = new ArrayList<>();

    DBMZone t5_d_0 = new DBMZone(new Variable("x"));
    Expolynomial t5_e_0 = Expolynomial.fromString("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2");
    t5_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("1"));
    t5_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("0"));
    GEN t5_gen_0 = new GEN(t5_d_0, t5_e_0);
    t5_gens.add(t5_gen_0);

    PartitionedGEN t5_pFunction = new PartitionedGEN(t5_gens);
    StochasticTransitionFeature t5_feature = StochasticTransitionFeature.of(t5_pFunction);
    t5.addFeature(t5_feature);

    List<GEN> t6_gens = new ArrayList<>();

    DBMZone t6_d_0 = new DBMZone(new Variable("x"));
    Expolynomial t6_e_0 = Expolynomial.fromString("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2");
    t6_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("1"));
    t6_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("0"));
    GEN t6_gen_0 = new GEN(t6_d_0, t6_e_0);
    t6_gens.add(t6_gen_0);

    PartitionedGEN t6_pFunction = new PartitionedGEN(t6_gens);
    StochasticTransitionFeature t6_feature = StochasticTransitionFeature.of(t6_pFunction);
    t6.addFeature(t6_feature);

    List<GEN> t7_gens = new ArrayList<>();

    DBMZone t7_d_0 = new DBMZone(new Variable("x"));
    Expolynomial t7_e_0 = Expolynomial.fromString("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2");
    t7_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("1"));
    t7_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("0"));
    GEN t7_gen_0 = new GEN(t7_d_0, t7_e_0);
    t7_gens.add(t7_gen_0);

    PartitionedGEN t7_pFunction = new PartitionedGEN(t7_gens);
    StochasticTransitionFeature t7_feature = StochasticTransitionFeature.of(t7_pFunction);
    t7.addFeature(t7_feature);

    List<GEN> t8_gens = new ArrayList<>();

    DBMZone t8_d_0 = new DBMZone(new Variable("x"));
    Expolynomial t8_e_0 = Expolynomial.fromString("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2");
    t8_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("1"));
    t8_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("0"));
    GEN t8_gen_0 = new GEN(t8_d_0, t8_e_0);
    t8_gens.add(t8_gen_0);

    PartitionedGEN t8_pFunction = new PartitionedGEN(t8_gens);
    StochasticTransitionFeature t8_feature = StochasticTransitionFeature.of(t8_pFunction);
    t8.addFeature(t8_feature);

    List<GEN> t9_gens = new ArrayList<>();

    DBMZone t9_d_0 = new DBMZone(new Variable("x"));
    Expolynomial t9_e_0 = Expolynomial.fromString("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2");
    t9_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("1"));
    t9_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("0"));
    GEN t9_gen_0 = new GEN(t9_d_0, t9_e_0);
    t9_gens.add(t9_gen_0);

    PartitionedGEN t9_pFunction = new PartitionedGEN(t9_gens);
    StochasticTransitionFeature t9_feature = StochasticTransitionFeature.of(t9_pFunction);
    t9.addFeature(t9_feature);

  }
}
