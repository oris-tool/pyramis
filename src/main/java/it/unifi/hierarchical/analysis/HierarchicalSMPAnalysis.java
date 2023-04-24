package it.unifi.hierarchical.analysis;

import it.unifi.hierarchical.model.*;
import it.unifi.hierarchical.utils.NumericalUtils;
import it.unifi.hierarchical.utils.StateUtils;
import org.oristool.models.gspn.chains.DTMC;
import org.oristool.models.gspn.chains.DTMCStationary;

import java.util.*;

/**
 * Notes:
 * - we assume the embedded DTMC to be irreducible
 */
public class HierarchicalSMPAnalysis {

    private static final double DTMC_STRUCTURE_ALLOWED_EPSILON = 0.00000001;

    public static NumericalValues cdf;

    private final int CYCLE_UNROLLING;

    private final HSMP model;
    private Map<LogicalLocation, NumericalValues> sojournTimeDistributions;
    private Map<Region, NumericalValues> regionSojournTimeDistributions;
    private Map<Region, TransientAnalyzer> regionTransientProbabilities;
    private Map<LogicalLocation, Double> meanSojournTimes;
    private Map<LogicalLocation, Double> emcSolution;

    //for cycle unrolling
    private Map<LogicalLocation, List<LogicalLocation>> aliasStates;
    private Map<Region, List<Region>> aliasRegion;
    private boolean compositeCycles = false;

    public HierarchicalSMPAnalysis(HSMP model) {
        this(model, 0);
    }

    //if CYCLE==0 then the standard analyzer method is called, otherwise the one specific for cycles
    public HierarchicalSMPAnalysis(HSMP model, int CYCLE) {
        this.model = model;
        this.CYCLE_UNROLLING = CYCLE;
    }

    /**
     * If it not exists, create an edge in the DTMC between from and to with specified value.
     * If the edge already exists sums values
     */
    private static void addEdgeValue(DTMC<LogicalLocation> dtmc, LogicalLocation from, LogicalLocation to, double prob) {
        Optional<Double> old = dtmc.probsGraph().edgeValue(from, to);
        double newProb = prob;
        if (old.isPresent()) {
            newProb += old.get();
        }
        dtmc.probsGraph().putEdgeValue(from, to, newProb);
    }

    private static Map<LogicalLocation, Double> evaluateDTMCSteadyState(DTMC<LogicalLocation> dtmc) {
        DTMCStationary<LogicalLocation> DTMCss = DTMCStationary.<LogicalLocation>builder().epsilon(DTMC_STRUCTURE_ALLOWED_EPSILON).build();
        return DTMCss.apply(dtmc.probsGraph());
    }

    /**
     * @param timeStep  discretization step for the numerical passes, -1.0 is used to operate on different timeStep per state and regions
     * @param timeLimit longest meaningful interval of time, in a state or region,
     *                  over which the calculation must be executed, in case of unbounded distributions a truncation point must be
     *                  identified in an educated way
     */
    public Map<String, Double> evaluate(double timeStep, double timeLimit) {

        //0 - checking model does not contains exits on borders of initial states
        //We assume that the first step is not a composite step of type FIRST with different next step pdf for each region
        boolean noExitInitials;
        Set<LogicalLocation> offendingStateSet = new HashSet<>();
        noExitInitials = checkInitialsNoBorder(offendingStateSet);
        if (!noExitInitials) {
            java.lang.System.out.println("Initial state/s composite with border exit found, for the moment not implemented, "
                    + "shortcut through introducing a fake initial state with duration 0");

			/*for(LogicalLocation s: offendingStateSet) {
				System.out.println(s.getName());
			}*/

            return null;
        }

        //0.1 - unrolling all cycles forcibly: use a fixed number of unrollings,
        //TODO add treatment to specify a confidence and identify correct number of unrolls
        //     (the user specifies how many times a cycle is unrolled)
        //TODO add correct treatment if cycles are present in multiple levels
        //TODO add correct treatment if multiple cycles connect the same nodes
        //TODO add correct treatment for cycles in regions containing nodes not in the cycle

        // We consider single cycles within neverending regions and composite steps that contain only simple steps
        if (CYCLE_UNROLLING != 0)
            identifyAndUnrollCycles();

        //long time;
        //Date d1 = new Date();

        //		System.out.println("Evaluate Sojourn Time Distributions");
        //1- Sojourn times distribution
        evaluateSojournTimeDistributions(timeStep, timeLimit);

        //Date d2 = new Date();

        //time = d2.getTime() - d1.getTime();
        //System.out.println(time+ "  sojournXXX");

        //		System.out.println("Evaluate Mean Sojourn Times");
        //2- Mean sojourn times
        evaluateMeanSojournTimes(timeStep, timeLimit);

        //Date d3 = new Date();

        //time = d3.getTime() - d2.getTime();
        //System.out.println(time+ "  meanXXX");

        //		System.out.println("Solve Embedded");
        //3- Solve EMC
        solveEmbeddedDTMC(timeStep);

        //Date d4 = new Date();

        //time = d4.getTime() - d3.getTime();
        //System.out.println(time+ "  solveDTMCXXX");

        //		System.out.println("Evaluate Steady State");
        //4- steady state (Use solution of 2 and 3 to evaluate steady)
        Map<String, Double> result = evaluate();

        //This is used to map alias' probabilities over original steps
        if (compositeCycles) {
            for (LogicalLocation s : aliasStates.keySet()) {
                Double res = 0.;
                for (LogicalLocation t : aliasStates.get(s)) {
                    res += result.get(t.getName());
                }
                result.put(s.getName(), res);
            }
        }

        return result;
    }

    private void identifyAndUnrollCycles() {

        CycleVisitor visitor = new CycleVisitor();

        model.getInitialStep().accept(visitor);
        if (visitor.containsCompositeCycles()) {
            compositeCycles = true;
            Map<Region, Set<LogicalLocation>> toDuplicateMap = new HashMap<>(visitor.getMap());

            aliasStates = new HashMap<>();
            aliasRegion = new HashMap<>();

            for (Region r : toDuplicateMap.keySet()) {
                for (LogicalLocation s : toDuplicateMap.get(r)) {
                    if (s.isCycle()) {
                        createStateCopy(s);
                    }

                }
                for (LogicalLocation s : toDuplicateMap.get(r)) {
                    linkStates(s, false);
                }
            }
        }
    }

    //This method makes a copy or arcs (inner is true if we're inside a cycle, false otherwise).
    //We assume that cycle's first state is a simple step.
    private void linkStates(LogicalLocation s, boolean inner) {

        //System.out.println("linko "+s.getName()+" "+inner);

        if (!inner && !s.isCycle()) {
            if (s instanceof SimpleStep) {
                List<LogicalLocation> nextStates = s.getNextLocations();
                List<Double> branchP = ((SimpleStep) s).getBranchingProbs();

                List<LogicalLocation> newNext = new LinkedList<>();
                for (LogicalLocation st : nextStates) {
                    if (st.isCycle()) {
                        newNext.add(aliasStates.get(st).get(0));
                    } else {
                        newNext.add(st);
                    }
                }
                ((SimpleStep) s).setNextLocations(newNext, branchP);

            }//else if composite / compositeOnBorder
            return;
        }

        boolean loop = s.isLooping();

        if (s instanceof SimpleStep) {
            for (int i = 0; i < CYCLE_UNROLLING; i++) {
                int pp;

                if (!inner)
                    pp = loop ? i + 1 : i;
                else
                    pp = i;

                SimpleStep curr = (SimpleStep) aliasStates.get(s).get(i);

                List<LogicalLocation> nextStates = curr.getNextLocations();
                List<LogicalLocation> newStates = new LinkedList<>();
                List<Double> branchP = curr.getBranchingProbs();

                if (pp == CYCLE_UNROLLING) {
                    branchP = new LinkedList<>();
                } else {
                    for (LogicalLocation nextState : nextStates) {
                        List<LogicalLocation> rr = aliasStates.get(nextState);
                        LogicalLocation nn = rr.get(pp);
                        newStates.add(nn);
                    }
                }

                curr.setNextLocations(newStates, branchP);
            }
        } else if (s instanceof CompositeStep) {

            for (Region region : ((CompositeStep) s).getRegions()) {
                LogicalLocation init = region.getInitialStep();
                List<LogicalLocation> reach = StateUtils.getReachableStates(init);
                for (LogicalLocation li : reach) {
                    linkStates(li, true);
                }
            }

            for (int i = 0; i < CYCLE_UNROLLING; i++) {
                CompositeStep curr = (CompositeStep) aliasStates.get(s).get(i);

                LogicalLocation init;

                //reset (correct region's) initial state
                for (Region region : curr.getRegions()) {
                    init = region.getInitialStep();
                    region.setInitialStep(aliasStates.get(init).get(i));
                }

                //linka il composite e poi linka gli stati interni
                int pp;
                if (!inner)
                    pp = loop ? i + 1 : i;
                else
                    pp = i;

                if (!StateUtils.isCompositeWithBorderExit(s)) {
                    List<LogicalLocation> nextStates = curr.getNextLocations();
                    List<LogicalLocation> newStates = new LinkedList<>();
                    List<Double> branchP = curr.getBranchingProbs();

                    if (pp == CYCLE_UNROLLING) {
                        branchP = new LinkedList<>();
                    } else {

                        for (LogicalLocation nextState : nextStates) {
                            newStates.add(aliasStates.get(nextState).get(pp));
                        }
                    }

                    curr.setNextLocations(newStates, branchP);

                } else {

                    Map<LogicalLocation, List<LogicalLocation>> nextStatesMap = curr.getExitStepsOnBorder();
                    Map<LogicalLocation, List<LogicalLocation>> nextStatesLinked = new HashMap<>();

                    Map<LogicalLocation, List<Double>> nextBranchMap = curr.getExitStatesProbabilities();
                    Map<LogicalLocation, List<Double>> nextBranchLinked = new HashMap<>();

                    for (LogicalLocation in : nextStatesMap.keySet()) {
                        LogicalLocation news = aliasStates.get(in).get(i);
                        nextStatesLinked.put(news, new LinkedList<>());

                        for (LogicalLocation out : nextStatesMap.get(in)) {
                            nextStatesLinked.get(news).add(aliasStates.get(out).get(pp));
                        }
                    }

                    for (LogicalLocation in : nextBranchMap.keySet()) {
                        LogicalLocation news = aliasStates.get(in).get(i);
                        nextBranchLinked.put(news, new LinkedList<>());

                        for (Double out : nextBranchMap.get(in)) {
                            nextBranchLinked.get(news).add(out);
                        }
                    }

                    nextStatesLinked.keySet().forEach(key ->
                            curr.addExitStep(key, nextStatesLinked.get(key), nextBranchLinked.get(key)));
                }
            }

        }

    }

    private void createCopyRegion(LogicalLocation s) {

        Set<LogicalLocation> visited = new HashSet<>();
        Stack<LogicalLocation> toBeVisited = new Stack<>();
        toBeVisited.add(s);
        while (!toBeVisited.isEmpty()) {
            LogicalLocation current = toBeVisited.pop();
            visited.add(current);

            if (StateUtils.isCompositeWithBorderExit(current)) {
                CompositeStep cState = (CompositeStep) current;

                for (LogicalLocation e : cState.getExitSteps().keySet()) {
                    for (LogicalLocation successor : cState.getNextLocations(e)) {
                        if (visited.contains(successor) || toBeVisited.contains(successor))
                            continue;
                        toBeVisited.push(successor);
                    }
                }
                //STANDARD CASE
            } else {
                for (LogicalLocation successor : current.getNextLocations()) {
                    if (visited.contains(successor) || toBeVisited.contains(successor))
                        continue;
                    toBeVisited.push(successor);
                }
            }
        }

        for (LogicalLocation v : visited) {
            createStateCopy(v);
        }
    }

    //This method creates a copy of the parameter s
    // (if s is a composite step, then the method copies regions; the method does not copy arcs)
    private void createStateCopy(LogicalLocation s) {

        aliasStates.put(s, new LinkedList<>());

        if (s instanceof SimpleStep) {
            // Viene fatto un unrolling del ciclo per capire quante volte si transita negli stati
            for (int i = 0; i < CYCLE_UNROLLING; i++) {
                aliasStates.get(s).add(((SimpleStep) s).makeCopy(i));
            }
        } else if (s instanceof CompositeStep) {

            CompositeStep sC = (CompositeStep) s;

            for (Region r : sC.getRegions()) {
                aliasRegion.put(r, new LinkedList<>());
                createCopyRegion(r.getInitialStep());
            }

            for (int i = 0; i < CYCLE_UNROLLING; i++) {
                List<Region> listR = new LinkedList<>();
                for (Region r : sC.getRegions()) {
                    Region copy = r.makeCopy();
                    listR.add(copy);
                    aliasRegion.get(r).add(copy);
                }

                // We duplicate regions too
                aliasStates.get(s).add(sC.makeCopy(i, listR));
            }


        } else if (s instanceof FinalLocation) {
            for (int i = 0; i < CYCLE_UNROLLING; i++) {
                aliasStates.get(s).add(((FinalLocation) s).makeCopy(i));
            }
        }
    }

    private boolean checkInitialsNoBorder(Set<LogicalLocation> offenderSet) {

        RegionVisitor visitor = new RegionVisitor();

        model.getInitialStep().accept(visitor);
        offenderSet.addAll(visitor.getOffenderSet());

        //the only border state accepted in the initial position is the initialState of the toplevel
        return visitor.isModelCorrect();
    }

    private void evaluateSojournTimeDistributions(double timeStep, double timeLimit) {

        SojournTimeEvaluatorVisitor visitor = new SojournTimeEvaluatorVisitor(timeStep, timeLimit);
        model.getInitialStep().accept(visitor);
        this.sojournTimeDistributions = visitor.getSojournTimeDistributions();
        this.regionSojournTimeDistributions = visitor.getRegionSojournTimeDistributions();
        this.regionTransientProbabilities = visitor.getRegionTransientProbabilities();
        HierarchicalSMPAnalysis.cdf = sojournTimeDistributions.get(model.getInitialStep());
    }

    private void evaluateMeanSojournTimes(double timeStep, double timeLimit) {

        boolean variableTimeStep = (timeStep < 0.0);
        MeanSojournTimeEvaluatorVisitor visitor = new MeanSojournTimeEvaluatorVisitor(model.getInitialStep(), sojournTimeDistributions, regionSojournTimeDistributions, regionTransientProbabilities, variableTimeStep, timeLimit);
        model.getInitialStep().accept(visitor);
        this.meanSojournTimes = visitor.getMeanSojournTimes();
    }

    private void solveEmbeddedDTMC(double timeStep) {
        //3.1- Build DTMC
        DTMC<LogicalLocation> dtmc = buildEDTMC(timeStep);
        //3.2- Evaluate steady state considering only branching probabilities and neglecting sojourn times
        this.emcSolution = evaluateDTMCSteadyState(dtmc);
    }

    private DTMC<LogicalLocation> buildEDTMC(double timeStep) {
        DTMC<LogicalLocation> dtmc = DTMC.create();

        //Initial state
        dtmc.initialStates().add(model.getInitialStep());
        dtmc.initialProbs().add(1.0);

        //transition probabilities
        Set<LogicalLocation> visited = new HashSet<>();
        Stack<LogicalLocation> toBeVisited = new Stack<>();
        toBeVisited.add(model.getInitialStep());
        dtmc.probsGraph().addNode(model.getInitialStep());
        while (!toBeVisited.isEmpty()) {
            LogicalLocation current = toBeVisited.pop();
            visited.add(current);

            //CASE OF COMPOSITE STATE WITH EXIT STATES ON THE BORDER
            if (StateUtils.isCompositeWithBorderExit(current)) {
                CompositeStep cState = (CompositeStep) current;
                //Add missing children to the dtmc
                for (LogicalLocation e : cState.getExitSteps().keySet()) {
                    for (LogicalLocation successor : cState.getNextLocations(e)) {
                        if (visited.contains(successor) || toBeVisited.contains(successor))
                            continue;
                        dtmc.probsGraph().addNode(successor);
                        toBeVisited.push(successor);
                    }
                }
                //Evaluate successors probabilities
                List<NumericalValues> distributions = new ArrayList<>();
                for (Region region : cState.getRegions()) {
                    if (region.getType() != RegionType.NEVERENDING)
                        distributions.add(regionSojournTimeDistributions.get(region));
                }

                double time;
                if (timeStep < 0) {
                    time = cState.getTimeStep();
                } else {
                    time = -1.0;
                }
                List<Double> fireFirstProb = NumericalUtils.evaluateFireFirstProbabilities(distributions, time);

                Map<LogicalLocation, Double> fireFirstProbMap = new HashMap<>();

                int count = 0;
                for (Region region : cState.getRegions()) {
                    if (region.getType() != RegionType.NEVERENDING) {
                        LogicalLocation endState = StateUtils.findEndState(region);
                        assert fireFirstProb != null;
                        fireFirstProbMap.put(endState, fireFirstProb.get(count));
                        count++;
                    }
                }
                //Add edges
                for (LogicalLocation exitState : cState.getExitSteps().keySet()) {
                    for (int b = 0; b < cState.getBranchingProbabilities(exitState).size(); b++) {
                        double prob = cState.getBranchingProbabilities(exitState).get(b) * fireFirstProbMap.get(exitState);
                        addEdgeValue(dtmc, current, cState.getNextLocations(exitState).get(b), prob);
                    }
                }
                //STANDARD CASE
            } else {
                //Add missing children to the dtmc
                for (LogicalLocation successor : current.getNextLocations()) {
                    if (visited.contains(successor) || toBeVisited.contains(successor))
                        continue;
                    dtmc.probsGraph().addNode(successor);
                    toBeVisited.push(successor);
                }
                //Add edges
                for (int i = 0; i < current.getBranchingProbabilities().size(); i++) {
                    addEdgeValue(dtmc, current, current.getNextLocations().get(i), current.getBranchingProbabilities().get(i));
                }
            }
        }
        return dtmc;
    }

    private Map<String, Double> evaluate() {
        //4.1- At higher level use the standard solution method for SS of an SMP
        Map<String, Double> ss = new HashMap<>();
        double denominator = 0.0;
        for (LogicalLocation higherLevelState : emcSolution.keySet()) {
            denominator += meanSojournTimes.get(higherLevelState) * emcSolution.get(higherLevelState);
        }

        for (LogicalLocation higherLevelState : emcSolution.keySet()) {


            double numerator = meanSojournTimes.get(higherLevelState) * emcSolution.get(higherLevelState);
            ss.put(higherLevelState.getName(), numerator / denominator);
        }


        //4.2- At lower level recursively go down:
        //The SS of a sub-state in a region can be obtained by multiplying the steady-state probability of the surrounding composite state with the
        //fraction of mean sojourn time in the sub-state and the surrounding composite state
        for (LogicalLocation higherLevelState : emcSolution.keySet()) {
            //			System.out.println("higher "+higherLevelState.getName());


            SubstatesSteadyStateEvaluatorVisitor visitor =
                    new SubstatesSteadyStateEvaluatorVisitor(
                            higherLevelState,
                            ss.get(higherLevelState.getName()),
                            meanSojournTimes.get(higherLevelState)
                            , meanSojournTimes);
            higherLevelState.accept(visitor);
            ss.putAll(visitor.getSubStateSSProbs());
        }

        return ss;
    }

}
