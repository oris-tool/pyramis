package it.unifi.hierarchical.model.example.hsmp_journal;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.Erlang;
import org.oristool.math.function.GEN;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.FinalState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;

public class HSMP_JournalForNever {


	public static HierarchicalSMP build() {

		int depth = 2;

		State E11 = new ExitState("E11",2);
		State E12 = new ExitState("E12",2);

		//

		//
		State Vm2 = new SimpleState(
				"Vm2", 
				GEN.newDeterministic(new BigDecimal("2")), 
				Arrays.asList(E12), 
				Arrays.asList(1.0), 
				depth);
		
		
		
		State VmF = new SimpleState(
				"VmF", 
				new Erlang(Variable.X,1, new BigDecimal("1")), 
				Arrays.asList(E11), 
				Arrays.asList(1.0), 
				depth);


		//
		State VmA = new SimpleState(
				"VmA", 
				new Erlang(Variable.X,1, new BigDecimal("1")), 
				Arrays.asList(VmF), 
				Arrays.asList(1.0), 
				depth);


		Region region1 = new Region(VmA, RegionType.EXIT);
		


	
		Region region2 = new Region(Vm2, RegionType.EXIT);

		//Level depth 1
		depth = 1;

		State E3 = new ExitState("E3",depth);

		
		State Enever = new ExitState("Enever",depth);


		//
		State V2 = new SimpleState(
				"V2", 
				GEN.newDeterministic(new BigDecimal("8")), 
				Arrays.asList(E3), 
				Arrays.asList(1.0), 
				depth);

		
		

		//
		State Vpost = new SimpleState(
				"Vpost", 
				GEN.newDeterministic(new BigDecimal("2")), 
				Arrays.asList(Enever), 
				Arrays.asList(1.0), 
				depth);

		
		
		
		Map<State, List<State>> nextStates = null;//Required to avoid ambiguity
		

		
		State Vm = new CompositeState(
				"Vm",  
				Arrays.asList(region1, region2), 
				nextStates, 
				null, 
				depth);



		((CompositeState) Vm).setNextStatesConditional(Map.of(
				E11,
				Arrays.asList(Vpost),
				E12,
				Arrays.asList(Vpost)));

		((CompositeState) Vm).setBranchingProbsConditional(Map.of(
				E11,
				Arrays.asList(1.0),
				E12,
				Arrays.asList(1.0)));
		
		
		//
		State VmRes = new SimpleState(
				"VmRes", 
				GEN.newUniform(new OmegaBigDecimal("1"), new OmegaBigDecimal("2")), 
				Arrays.asList(Vm), 
				Arrays.asList(1.0), 
				depth);


		
		//Level depth 0
		depth = 0;

		List<State> nextStates1 = null;//Required to avoid ambiguity
		List<Double> n=null;
		Region region0 = new Region(VmRes, RegionType.NEVER, true);
		Region region01 = new Region(V2, RegionType.EXIT);
		

		State S0 = new CompositeState(
				"S0",  
				Arrays.asList(region0,region01), 
				nextStates1, 
				n, 
				depth);


		S0.setNextStates(Arrays.asList(S0), Arrays.asList(1.0));


		return new HierarchicalSMP(S0);

	}
}
