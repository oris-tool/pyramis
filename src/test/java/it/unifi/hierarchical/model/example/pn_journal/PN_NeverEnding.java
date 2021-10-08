package it.unifi.hierarchical.model.example.pn_journal;


import java.lang.Integer;
import java.math.BigDecimal;
import org.oristool.models.pn.PostUpdater;
import org.oristool.models.pn.Priority;
import org.oristool.models.stpn.MarkingExpr;
import org.oristool.models.stpn.trees.StochasticTransitionFeature;
import org.oristool.petrinet.Marking;
import org.oristool.petrinet.PetriNet;
import org.oristool.petrinet.Place;
import org.oristool.petrinet.Transition;

public class PN_NeverEnding {
  public static void build(PetriNet net, Marking marking) {

    //Generating Nodes
    Place VmAging = net.addPlace("VmAging");
    Place VmFailing = net.addPlace("VmFailing");
    Place VmFailureDetecting = net.addPlace("VmFailureDetecting");
    Place VmRejWaiting = net.addPlace("VmRejWaiting");
    Place VmRejuvenating = net.addPlace("VmRejuvenating");
    Place VmRepairing = net.addPlace("VmRepairing");
    Place VmRestarting = net.addPlace("VmRestarting");
    Transition vmAging = net.addTransition("vmAging");
    Transition vmFailing = net.addTransition("vmFailing");
    Transition vmFailuredetecting = net.addTransition("vmFailuredetecting");
    Transition vmRejWaiting = net.addTransition("vmRejWaiting");
    Transition vmRejuvenating = net.addTransition("vmRejuvenating");
    Transition vmRepairing = net.addTransition("vmRepairing");
    Transition vmRestarting = net.addTransition("vmRestarting");

    //Generating Connectors
    net.addPrecondition(VmAging, vmAging);
    net.addPostcondition(vmRejWaiting, VmRejuvenating);
    net.addPostcondition(vmFailuredetecting, VmRepairing);
    net.addPrecondition(VmFailing, vmFailing);
    net.addPrecondition(VmRejuvenating, vmRejuvenating);
    net.addPrecondition(VmRepairing, vmRepairing);
    net.addPrecondition(VmRestarting, vmRestarting);
    net.addPrecondition(VmRejWaiting, vmRejWaiting);
    net.addPostcondition(vmRestarting, VmAging);
    net.addPrecondition(VmFailureDetecting, vmFailuredetecting);
    net.addPostcondition(vmRestarting, VmRejWaiting);
    net.addPostcondition(vmAging, VmFailing);
    net.addPostcondition(vmFailing, VmFailureDetecting);

    //Generating Properties
    marking.setTokens(VmAging, 0);
    marking.setTokens(VmFailing, 0);
    marking.setTokens(VmFailureDetecting, 0);
    marking.setTokens(VmRejWaiting, 0);
    marking.setTokens(VmRejuvenating, 0);
    marking.setTokens(VmRepairing, 0);
    marking.setTokens(VmRestarting, 1);
    vmAging.addFeature(StochasticTransitionFeature.newErlangInstance(new Integer("4"), new BigDecimal("0.024")));
    vmFailing.addFeature(new PostUpdater("VmRejWaiting=0", net));
    vmFailing.addFeature(StochasticTransitionFeature.newErlangInstance(new Integer("4"), new BigDecimal("0.056")));
    vmFailuredetecting.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("0"), new BigDecimal("0.2")));
    vmRejWaiting.addFeature(new PostUpdater("VmAging=0;VmFailing=0;", net));
    vmRejWaiting.addFeature(StochasticTransitionFeature.newDeterministicInstance(new BigDecimal("24"), MarkingExpr.from("1", net)));
    vmRejWaiting.addFeature(new Priority(0));
    vmRejuvenating.addFeature(new PostUpdater("VmAging=1;VmRejWaiting=1;", net));
    vmRejuvenating.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("0.1"), new BigDecimal("0.2")));
    vmRepairing.addFeature(new PostUpdater("VmAging=1;VmRejWaiting=1;", net));
    vmRepairing.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("0.2"), new BigDecimal("1")));
    vmRestarting.addFeature(StochasticTransitionFeature.newUniformInstance(new BigDecimal("0.2"), new BigDecimal("1")));
  }
}
