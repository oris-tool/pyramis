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
import org.oristool.math.function.Erlang;
import org.oristool.math.function.GEN;
import org.oristool.math.function.PartitionedGEN;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;

public class HSMP_JournalVariableTicksOld {

	public static HierarchicalSMP build(double timeVMA, double timeR1, double timeVMMA, double timeEX, double timeRegions) {

		final double pstop=0.97;

		List<GEN> vmRejuvenating_gens = new ArrayList<>();

		DBMZone vmRejuvenating_d_0 = new DBMZone(new Variable("x"));
		Expolynomial vmRejuvenating_e_0 = Expolynomial.fromString("-318523 * Exp[-57.5509 x] + 4777840* Exp[-57.5509 x] * x + -15926100 * Exp[-57.5509 x] * x^2");
		//Normalization
		vmRejuvenating_e_0.multiply(new BigDecimal(0.9999893996609198));
		vmRejuvenating_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("0.2"));
		vmRejuvenating_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-0.1"));
		GEN vmRejuvenating_gen_0 = new GEN(vmRejuvenating_d_0, vmRejuvenating_e_0);
		vmRejuvenating_gens.add(vmRejuvenating_gen_0);

		PartitionedGEN vmRejuvenating_pFunction = new PartitionedGEN(vmRejuvenating_gens);
	
		List<GEN> vmRepairing_gens = new ArrayList<>();

		DBMZone vmRepairing_d_0 = new DBMZone(new Variable("x"));
		Expolynomial vmRepairing_e_0 = Expolynomial.fromString("-71.0006 * Exp[-6.41167 x] + 319.503 * Exp[-6.41167 x] * x + -142.001 * Exp[-6.41167 x] * x^2");
		//Normalization
		vmRepairing_e_0.multiply(new BigDecimal(0.9999962125608038));
		vmRepairing_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("2"));
		vmRepairing_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-0.25"));
		GEN vmRepairing_gen_0 = new GEN(vmRepairing_d_0, vmRepairing_e_0);
		vmRepairing_gens.add(vmRepairing_gen_0);

		PartitionedGEN vmRepairing_pFunction = new PartitionedGEN(vmRepairing_gens);
		

		List<GEN> vmRestarting_gens = new ArrayList<>();

		DBMZone vmRestarting_d_0 = new DBMZone(new Variable("x"));
		Expolynomial vmRestarting_e_0 = Expolynomial.fromString("-686.089 * Exp[-11.3061 x] + 3087.4 * Exp[-11.3061 x] * x + -1372.18 * Exp[-11.3061 x] * x^2");
		//Normalization
		vmRestarting_e_0.multiply(new BigDecimal(1.0000064788681096));
		vmRestarting_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("2"));
		vmRestarting_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-0.25"));
		GEN vmRestarting_gen_0 = new GEN(vmRestarting_d_0, vmRestarting_e_0);
		vmRestarting_gens.add(vmRestarting_gen_0);

		PartitionedGEN vmRestarting_pFunction = new PartitionedGEN(vmRestarting_gens);
		
		List<GEN> vmStopWaiting_gens = new ArrayList<>();

		DBMZone vmStopWaiting_d_0 = new DBMZone(new Variable("x"));
		Expolynomial vmStopWaiting_e_0 = Expolynomial.fromString("-21332 * Exp[ -36.68 x] + 319991 * Exp[-36.68 x] * x + -1066640 * Exp[-36.68 x] * x^2");
		//Normalization
		vmStopWaiting_e_0.multiply(new BigDecimal(0.9994014187474192));
		vmStopWaiting_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("0.2"));
		vmStopWaiting_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-0.1"));
		GEN vmStopWaiting_gen_0 = new GEN(vmStopWaiting_d_0, vmStopWaiting_e_0);
		vmStopWaiting_gens.add(vmStopWaiting_gen_0);

		PartitionedGEN vmStopWaiting_pFunction = new PartitionedGEN(vmStopWaiting_gens);
		

		List<GEN> vmmRejuvenating_gens = new ArrayList<>();

		DBMZone vmmRejuvenating_d_0 = new DBMZone(new Variable("x"));
		Expolynomial vmmRejuvenating_e_0 = Expolynomial.fromString("-10.24 * Exp[3.86164 x] + 136.534 * Exp[3.86164 x] * x + -341.335 * Exp[3.86164 x] * x^2");
		//Normalization
		vmmRejuvenating_e_0.multiply(new BigDecimal(0.9999779820608894));
		vmmRejuvenating_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("0.3"));
		vmmRejuvenating_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-0.1"));
		GEN vmmRejuvenating_gen_0 = new GEN(vmmRejuvenating_d_0, vmmRejuvenating_e_0);
		vmmRejuvenating_gens.add(vmmRejuvenating_gen_0);

		PartitionedGEN vmmRejuvenating_pFunction = new PartitionedGEN(vmmRejuvenating_gens);
		
		List<GEN> vmmRepairing_gens = new ArrayList<>();

		DBMZone vmmRepairing_d_0 = new DBMZone(new Variable("x"));
		Expolynomial vmmRepairing_e_0 = Expolynomial.fromString("-41.7333 * Exp[-3.22812 x] + 97.3777 * Exp[-3.22812 x] * x + -27.8222 * Exp[-3.22812 x] * x^2");
		//Normalization
		vmmRepairing_e_0.multiply(new BigDecimal(0.9999988824655345));
		vmmRepairing_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("3"));
		vmmRepairing_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-0.5"));
		GEN vmmRepairing_gen_0 = new GEN(vmmRepairing_d_0, vmmRepairing_e_0);
		vmmRepairing_gens.add(vmmRepairing_gen_0);

		PartitionedGEN vmmRepairing_pFunction = new PartitionedGEN(vmmRepairing_gens);
	
		
		List<GEN> vmmRestarting_gens = new ArrayList<>();

		DBMZone vmmRestarting_d_0 = new DBMZone(new Variable("x"));
		Expolynomial vmmRestarting_e_0 = Expolynomial.fromString("-1369.71 * Exp[-7.28859 x] + 3195.98 * Exp[-7.28859 x] * x + -913.137 * Exp[-7.28859 x] * x^2");
		//Normalization
		vmmRestarting_e_0.multiply(new BigDecimal(1.0000184105663141));
		vmmRestarting_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("3"));
		vmmRestarting_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-0.5"));
		GEN vmmRestarting_gen_0 = new GEN(vmmRestarting_d_0, vmmRestarting_e_0);
		vmmRestarting_gens.add(vmmRestarting_gen_0);

		PartitionedGEN vmmRestarting_pFunction = new PartitionedGEN(vmmRestarting_gens);
	




		int depth = 2;



		State E11 = new ExitState("E11",2);
		State E12 = new ExitState("E12",2);

		//
		State VmF = new SimpleState(
				"VmF", 
				new Erlang(Variable.X,1, new BigDecimal("0.014")), 
				Arrays.asList(E11), 
				Arrays.asList(1.0), 
				depth,
				timeVMA);

		//
		State VmRejW = new SimpleState(
				"VmRejW", 
				GEN.newDeterministic(new BigDecimal("24")), 
				Arrays.asList(E12), 
				Arrays.asList(1.0), 
				depth,
				timeVMA);


		//
		State VmA = new SimpleState(
				"VmA", 
				new Erlang(Variable.X,1, new BigDecimal("0.006")), 
				Arrays.asList(VmF), 
				Arrays.asList(1.0), 
				depth,
				timeVMA);


		Region region1 = new Region(VmA, RegionType.EXIT, timeVMA);
		Region region2 = new Region(VmRejW, RegionType.EXIT, timeVMA);

		//Level depth 1
		depth = 1;

		State E2 = new ExitState("E2",depth);
		State E3 = new ExitState("E3",depth);


		Map<State, List<State>> nextMap = null;
		Map<State, List<Double>> nextBranch=null;


		//
		State VmmF = new SimpleState(
				"VmmF", 
				new Erlang(Variable.X,1, new BigDecimal("0.006")), 
				Arrays.asList(E2), 
				Arrays.asList(1.0), 
				depth,
				timeRegions);

		//
		State VmmA = new SimpleState(
				"VmmA", 
				new Erlang(Variable.X,1, new BigDecimal("0.001")), 
				Arrays.asList(VmmF), 
				Arrays.asList(1.0), 
				depth,
				timeRegions);

		//
		State VmmRejW = new SimpleState(
				"VmmRejW", 
				GEN.newDeterministic(new BigDecimal("96")), 
				Arrays.asList(E3), 
				Arrays.asList(1.0), 
				depth,
				timeRegions);


		State Vm = new CompositeState(
				"Vm",  
				Arrays.asList(region1, region2), 
				nextMap,  
				nextBranch, 
				depth,
				timeVMA);

		//
		State VmR = new SimpleState(
				"VmR", 
				vmRepairing_pFunction, 
				Arrays.asList(Vm), 
				Arrays.asList(1.0), 
				depth,
				timeR1);

		//
		State VmFD = new SimpleState(
				"VmFD", 
				GEN.newUniform(new OmegaBigDecimal("0.0"), new OmegaBigDecimal("0.2")), 
				Arrays.asList(VmR), 
				Arrays.asList(1.0), 
				depth,
				timeR1);


		//
		State VmRej = new SimpleState(
				"VmRej", 
				vmRejuvenating_pFunction, 
				Arrays.asList(Vm), 
				Arrays.asList(1.0), 
				depth,
				timeR1);


		//
		State VmRes = new SimpleState(
				"VmRes", 
				vmRestarting_pFunction, 
				Arrays.asList(Vm), 
				Arrays.asList(1.0), 
				depth,
				timeR1);


		((CompositeState) Vm).setNextStatesConditional(Map.of(
				E11,
				Arrays.asList(VmFD),
				E12,
				Arrays.asList(VmRej)));

		((CompositeState) Vm).setBranchingProbsConditional(Map.of(
				E11,
				Arrays.asList(1.0),
				E12,
				Arrays.asList(1.0)));


		//Level depth 0
		depth = 0;

		Map<State, List<State>> nextStates = null;//Required to avoid ambiguity
		Region region3 = new Region(VmRes, RegionType.NEVER, timeR1 , true);
		Region region4 = new Region(VmmA, RegionType.EXIT, timeRegions);
		Region region5 = new Region(VmmRejW, RegionType.EXIT, timeRegions);




		State S0 = new CompositeState(
				"S0",  
				Arrays.asList(region3,region4,region5), 
				nextStates, 
				null, 
				depth,
				timeVMMA);


		//
		State VmmRes = new SimpleState(
				"VmmRes", 
				vmmRestarting_pFunction,
				Arrays.asList(S0), 
				Arrays.asList(1.0), 
				depth,
				timeEX);

		//
		State VmmR = new SimpleState(
				"VmmR", 
				vmmRepairing_pFunction,
				Arrays.asList(VmmRes), 
				Arrays.asList(1.0), 
				depth,
				timeEX);



		//
		State VmmFD = new SimpleState(
				"VmmFD", 
				GEN.newUniform(new OmegaBigDecimal("0.0"), new OmegaBigDecimal("0.2")), 
				Arrays.asList(VmmR), 
				Arrays.asList(1.0), 
				depth,
				timeEX);


		//
		State VmmRej = new SimpleState(
				"VmmRej", 
				vmmRejuvenating_pFunction,
				Arrays.asList(S0), 
				Arrays.asList(1.0), 
				depth,
				timeEX);


		//
		State VmSW = new SimpleState(
				"VmSW", 
				vmStopWaiting_pFunction, 
				Arrays.asList(VmmRej), 
				Arrays.asList(1.0), 
				depth,
				timeEX);



		((CompositeState) S0).setNextStatesConditional(Map.of(
				E2,
				Arrays.asList(VmmFD),
				E3,
				Arrays.asList(VmSW,VmmRej)));

		((CompositeState) S0).setBranchingProbsConditional(Map.of(
				E2,
				Arrays.asList(1.0),
				E3,
				Arrays.asList(pstop, 1.0-pstop)));



		Vm.setCyleLooping(true, false);
		VmFD.setCyleLooping(true, false);
		VmR.setCyleLooping(true, true);
		VmRej.setCyleLooping(true, true);


		return new HierarchicalSMP(S0);
	}
}
