package it.unifi.hierarchical.model;

import java.util.List;

import org.oristool.math.function.PartitionedFunction;

import it.unifi.hierarchical.model.visitor.StateVisitor;

public class SimpleState extends State{

    private PartitionedFunction densityFunction;
    
    private double upperBound=-1.;
    
    
    public double getUpperBound() {
		return upperBound;
	}


	public SimpleState(String name, PartitionedFunction densityFunction, List<State> nextStates, List<Double> branchingProbs, int depth, double timeStep, double bound) {
    	this(name, densityFunction, nextStates, branchingProbs, depth, timeStep);
    	this.upperBound= bound;
    }
    
    
    public SimpleState(String name, PartitionedFunction densityFunction, List<State> nextStates, List<Double> branchingProbs, int depth, double timeStep) {
        super(name, depth, timeStep);
        this.setNextStates(nextStates, branchingProbs);
        this.densityFunction = densityFunction;
    }
    
    public SimpleState(String name, PartitionedFunction densityFunction, List<State> nextStates, List<Double> branchingProbs, int depth) {
        this(name, densityFunction, nextStates, branchingProbs, depth, -1.0);
    }

    public PartitionedFunction getDensityFunction() {
        return densityFunction;
    }

    @Override
    public String toString() {
        return "SimpleState [densityFunction=" + densityFunction + ", numberOfNextStates=" + (nextStates != null? nextStates.size():0) + ", depth=" + depth
                + ", name=" + name + "]";
    }
    
    @Override
    public void accept(StateVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public State makeCopy() {
        return new SimpleState(name, densityFunction, nextStates, branchingProbs, depth, timeStep);
    }
    
    public State makeCopy(int id) {
        return new SimpleState(name+"_"+id, densityFunction, nextStates, branchingProbs, depth, timeStep);
    }
}
