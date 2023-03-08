package it.unifi.hierarchical.model.visitor;

import it.unifi.hierarchical.model.CompositeStep;
import it.unifi.hierarchical.model.HSMP;
import it.unifi.hierarchical.model.LogicalLocation;
import it.unifi.hierarchical.model.SimpleStep;
import org.apache.commons.math3.util.Pair;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class HSMPVisitor {

    private Set<LogicalLocation> locations;
    private LogicalLocation initialLocation;

    public HSMPVisitor() {
        locations = new LinkedHashSet<>();
    }

    public void visit(HSMP hsmp) {
        locations.clear();

        visit(hsmp.getInitialStep());
    }

    public void visit(LogicalLocation logicalLocation) {
        if (!locations.add(logicalLocation))
            return;

        if (logicalLocation instanceof SimpleStep) {
            logicalLocation.getNextLocations().forEach(this::visit);
        } else if (logicalLocation instanceof CompositeStep) {
            ((CompositeStep) logicalLocation).getRegions().forEach(region -> visit(region.getInitialStep()));

            if (((CompositeStep) logicalLocation).hasExitStepsOnBorder()) {
                for (List<Pair<LogicalLocation, Double>> entry : ((CompositeStep) logicalLocation).getExitSteps().values()) {
                    entry.forEach(pair -> visit(pair.getFirst()));
                }
            } else {
                logicalLocation.getNextLocations().forEach(this::visit);
            }
        }
    }

    public Set<LogicalLocation> getLocations() {
        return new LinkedHashSet<>(locations);
    }
}
