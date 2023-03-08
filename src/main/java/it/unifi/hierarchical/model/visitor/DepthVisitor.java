package it.unifi.hierarchical.model.visitor;

import it.unifi.hierarchical.model.CompositeStep;
import it.unifi.hierarchical.model.FinalLocation;
import it.unifi.hierarchical.model.LogicalLocation;
import it.unifi.hierarchical.model.SimpleStep;

public class DepthVisitor implements LogicalLocationVisitor {

    private int currentDepth;

    public DepthVisitor(int startingDepth) {
        currentDepth = startingDepth;
    }

    public void visit(LogicalLocation logicalLocation) {
        if (logicalLocation instanceof SimpleStep)
            visit((SimpleStep) logicalLocation);
        else if (logicalLocation instanceof CompositeStep)
            visit((CompositeStep) logicalLocation);
        else if (logicalLocation instanceof FinalLocation)
            visit((FinalLocation) logicalLocation);
    }


    @Override
    public void visit(SimpleStep simpleStep) {
        simpleStep.setDepth(currentDepth);

        simpleStep.getNextLocations().forEach(nextLocation -> nextLocation.accept(this));
    }

    @Override
    public void visit(CompositeStep compositeStep) {
        compositeStep.setDepth(currentDepth);

        currentDepth++;
        compositeStep.getRegions().forEach(region -> region.getInitialStep().accept(this));
        currentDepth--;

        if (compositeStep.hasExitStepsOnBorder()) {
            compositeStep.getExitStepsOnBorder().values().forEach(list ->
                    list.forEach(nextLocation -> nextLocation.accept(this)));
        } else {
            compositeStep.getNextLocations().forEach(nextLocation -> nextLocation.accept(this));
        }
    }

    @Override
    public void visit(FinalLocation finalLocation) {
        finalLocation.setDepth(currentDepth);
    }
}
