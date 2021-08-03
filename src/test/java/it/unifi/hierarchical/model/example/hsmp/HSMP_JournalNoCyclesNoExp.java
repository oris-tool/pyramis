package it.unifi.hierarchical.model.example.hsmp;

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

public class HSMP_JournalNoCyclesNoExp {

	public static HierarchicalSMP build() {

		final double pstop=0.97;

		int depth = 2;



		State E11 = new ExitState("E11",2);
		State E12 = new ExitState("E12",2);

		//
		State VmF = new SimpleState(
				"VmF", 
				new Erlang(Variable.X,1, new BigDecimal("0.014")), 
				Arrays.asList(E11), 
				Arrays.asList(1.0), 
				depth);

		//
		State VmRejW = new SimpleState(
				"VmRejW", 
				GEN.newDeterministic(new BigDecimal("24")), 
				Arrays.asList(E12), 
				Arrays.asList(1.0), 
				depth);


		//
		State VmA = new SimpleState(
				"VmA", 
				new Erlang(Variable.X,1, new BigDecimal("0.006")), 
				Arrays.asList(VmF), 
				Arrays.asList(1.0), 
				depth);


		Region region1 = new Region(VmA, RegionType.EXIT);
		Region region2 = new Region(VmRejW, RegionType.EXIT);

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
				depth);

		//
		State VmmA = new SimpleState(
				"VmmA", 
				new Erlang(Variable.X,1, new BigDecimal("0.001")), 
				Arrays.asList(VmmF), 
				Arrays.asList(1.0), 
				depth);

		//
		State VmmRejW = new SimpleState(
				"VmmRejW", 
				GEN.newDeterministic(new BigDecimal("168")), 
				Arrays.asList(E3), 
				Arrays.asList(1.0), 
				depth);


		State Vm = new CompositeState(
				"Vm",  
				Arrays.asList(region1, region2), 
				nextMap,  
				nextBranch, 
				depth);

		//
		State VmR = new SimpleState(
				"VmR", 
				GEN.newUniform(new OmegaBigDecimal("0.25"), new OmegaBigDecimal("2")), 
				Arrays.asList(Vm), 
				Arrays.asList(1.0), 
				depth);

		//
		State VmFD = new SimpleState(
				"VmFD", 
				GEN.newUniform(new OmegaBigDecimal("0.0"), new OmegaBigDecimal("0.2")), 
				Arrays.asList(VmR), 
				Arrays.asList(1.0), 
				depth);


		//
		State VmRej = new SimpleState(
				"VmRej", 
				GEN.newUniform(new OmegaBigDecimal("0.1"), new OmegaBigDecimal("0.2")), 
				Arrays.asList(Vm), 
				Arrays.asList(1.0), 
				depth);


		//
		State VmRes = new SimpleState(
				"VmRes", 
				GEN.newUniform(new OmegaBigDecimal("0.25"), new OmegaBigDecimal("2")), 
				Arrays.asList(Vm), 
				Arrays.asList(1.0), 
				depth);


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
	//	Region region3 = new Region(VmRes, RegionType.NEVER, true);
	
		Region region4 = new Region(VmmA, RegionType.EXIT);
		Region region5 = new Region(VmmRejW, RegionType.EXIT);




		State S0 = new CompositeState(
				"S0",  
				Arrays.asList(region4,region5), 
				nextStates, 
				null, 
				depth);


		//
		State VmmRes = new SimpleState(
				"VmmRes", 
				GEN.newUniform(new OmegaBigDecimal("0.5"), new OmegaBigDecimal("3")),
				Arrays.asList(S0), 
				Arrays.asList(1.0), 
				depth);

		//
		State VmmR = new SimpleState(
				"VmmR", 
				GEN.newUniform(new OmegaBigDecimal("0.5"), new OmegaBigDecimal("3")),
				Arrays.asList(VmmRes), 
				Arrays.asList(1.0), 
				depth);



		//
		State VmmFD = new SimpleState(
				"VmmFD", 
				GEN.newUniform(new OmegaBigDecimal("0.0"), new OmegaBigDecimal("0.2")), 
				Arrays.asList(VmmR), 
				Arrays.asList(1.0), 
				depth);


		//
		State VmmRej = new SimpleState(
				"VmmRej", 
				GEN.newUniform(new OmegaBigDecimal("0.1"), new OmegaBigDecimal("0.3")),
				Arrays.asList(S0), 
				Arrays.asList(1.0), 
				depth);


		//
		State VmSW = new SimpleState(
				"VmSW", 
				GEN.newUniform(new OmegaBigDecimal("0.1"), new OmegaBigDecimal("0.2")), 
				Arrays.asList(VmmRej), 
				Arrays.asList(1.0), 
				depth);



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


//
//		Vm.setCyleLooping(true, false);
//		VmFD.setCyleLooping(true, false);
//		VmR.setCyleLooping(true, true);
//		VmRej.setCyleLooping(true, true);


		return new HierarchicalSMP(S0);
	}
}
