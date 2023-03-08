/* This program is part of the PYRAMIS library for compositional analysis of hierarchical UML statecharts.
 * Copyright (C) 2019-2023 The PYRAMIS Authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.unifi.hierarchical.model.tse.steady;

import it.unifi.hierarchical.analysis.ErlangExp;
import it.unifi.hierarchical.model.*;
import it.unifi.hierarchical.model.Region;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.domain.DBMZone;
import org.oristool.math.expression.Expolynomial;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.GEN;
import org.oristool.math.function.PartitionedGEN;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class supports the definition of the HSMP model
 * used in the case study on steady-state analysis of software rejuvenation in virtual servers 
 * of the paper titled "Compositional Analysis of Hierarchical UML Statecharts" (see Figure 8).
 */
public class SWRej {
	/*

	public static HSMP build() {

		final double pstop=0.9997;

		List<GEN> vmRejuvenating_gens = new ArrayList<>();

		DBMZone vmRejuvenating_d_0 = new DBMZone(new Variable("x"));
		
		//Expolynomial vmRejuvenating_e_0 = Expolynomial.fromString("-318523 * Exp[-57.5509 x] + 4777840* Exp[-57.5509 x] * x + -15926100 * Exp[-57.5509 x] * x^2");
		
		Expolynomial vmRejuvenating_e_0 = Expolynomial.fromString("48.6526* Exp[-1.33757 x] * x + -121.632 * Exp[-1.33757 x] * x^2");
		//Normalization
		vmRejuvenating_e_0.multiply(new BigDecimal(1.0000077901973556));
		vmRejuvenating_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("0.4"));
		vmRejuvenating_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("0.0"));
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

//		List<GEN> vmStopWaiting_gens = new ArrayList<>();
//
//		DBMZone vmStopWaiting_d_0 = new DBMZone(new Variable("x"));
//		Expolynomial vmStopWaiting_e_0 = Expolynomial.fromString("-21332 * Exp[ -36.68 x] + 319991 * Exp[-36.68 x] * x + -1066640 * Exp[-36.68 x] * x^2");
//		//Normalization
//		vmStopWaiting_e_0.multiply(new BigDecimal(0.9994014187474192));
//		vmStopWaiting_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("0.2"));
//		vmStopWaiting_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-0.1"));
//		GEN vmStopWaiting_gen_0 = new GEN(vmStopWaiting_d_0, vmStopWaiting_e_0);
//		vmStopWaiting_gens.add(vmStopWaiting_gen_0);
//
//		PartitionedGEN vmStopWaiting_pFunction = new PartitionedGEN(vmStopWaiting_gens);

		List<GEN> vmmRejuvenating_gens = new ArrayList<>();

		DBMZone vmmRejuvenating_d_0 = new DBMZone(new Variable("x"));
		//Expolynomial vmmRejuvenating_e_0 = Expolynomial.fromString("-10.24 * Exp[3.86164 x] + 136.534 * Exp[3.86164 x] * x + -341.335 * Exp[3.86164 x] * x^2");
		
		Expolynomial vmmRejuvenating_e_0 = Expolynomial.fromString("+ 30.575 * Exp[1.00078 x] * x + -76.4374 * Exp[1.00078 x] * x^2");
		//Normalization
		vmmRejuvenating_e_0.multiply(new BigDecimal(0.9999954102775833));
		vmmRejuvenating_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("0.4"));
		vmmRejuvenating_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("0.0"));
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

		Step E11 = new ExitState("E11",2);
		Step E12 = new ExitState("E12",2);

		// VmF corresponds to VM_Failing in Figure 8
		Step VmF = new SimpleStep(
				"VmF", 
				new ErlangExp(new BigDecimal("0.0738004"), new BigDecimal("0.0171087")), 
				Arrays.asList(E11), 
				Arrays.asList(1.0), 
				depth);

		// VmRejW corresponds to VM_Rejuvenation_Waiting in Figure 8
		Step VmRejW = new SimpleStep(
				"VmRejW", 
				GEN.newDeterministic(new BigDecimal("24")), 
				Arrays.asList(E12), 
				Arrays.asList(1.0), 
				depth);

		// VmA corresponds to VM_Aging in Figure 8
		Step VmA = new SimpleStep(
				"VmA", 
				new ErlangExp( new BigDecimal("0.0138889"),new BigDecimal("0.0104167")), 
				Arrays.asList(VmF), 
				Arrays.asList(1.0), 
				depth);

		Region region1 = new Region(VmA, RegionType.EXIT);
		Region region2 = new Region(VmRejW, RegionType.EXIT);

		// Level depth 1
		depth = 1;

		Step E2 = new ExitState("E2",depth);
		Step E3 = new ExitState("E3",depth);
		
		Step Enever = new ExitState("Enever",depth);

		Map<Step, List<Step>> nextMap = null;
		Map<Step, List<Double>> nextBranch=null;

		// VmmF corresponds to VMM_Failing in Figure 8
		Step VmmF = new SimpleStep(
				"VmmF", 
				new ErlangExp( new BigDecimal("0.0378072"), new BigDecimal("0.00706464")), 
				Arrays.asList(E2), 
				Arrays.asList(1.0), 
				depth);

		// VmmA corresponds to VMM_Aging in Figure 8
		Step VmmA = new SimpleStep(
				"VmmA", 
				new ErlangExp( new BigDecimal("0.00460392"), new BigDecimal("0.0021988")), 
				Arrays.asList(VmmF), 
				Arrays.asList(1.0), 
				depth);

		// VmmRejW corresponds to VMM_Rejuvenation_Waiting in Figure 8
		Step VmmRejW = new SimpleStep(
				"VmmRejW", 
				GEN.newDeterministic(new BigDecimal("96")), 
				Arrays.asList(E3), 
				Arrays.asList(1.0), 
				depth);

		// Vm corresponds to VM_Rejuvenation_Waiting in Figure 8
		Step Vm = new CompositeStep(
				"Vm",  
				Arrays.asList(region1, region2), 
				nextMap,  
				nextBranch, 
				depth);

		// VmR corresponds to VM_Repairing in Figure 8
		Step VmR = new SimpleStep(
				"VmR", 
				vmRepairing_pFunction, 
				Arrays.asList(Enever), 
				Arrays.asList(1.0), 
				depth);

		// VmFD corresponds to VM_Failure_Detecting in Figure 8
		Step VmFD = new SimpleStep(
				"VmFD", 
				GEN.newUniform(new OmegaBigDecimal("0.0"), new OmegaBigDecimal("0.4")), 
				Arrays.asList(VmR), 
				Arrays.asList(1.0), 
				depth);

		// VmRej corresponds to VM_Rejuvenating in Figure 8
		Step VmRej = new SimpleStep(
				"VmRej", 
				vmRejuvenating_pFunction, 
				Arrays.asList(Enever), 
				Arrays.asList(1.0), 
				depth);

		// VmRes corresponds to VM_Restarting in Figure 8
		Step VmRes = new SimpleStep(
				"VmRes", 
				vmRestarting_pFunction, 
				Arrays.asList(Vm), 
				Arrays.asList(1.0), 
				depth);

		((CompositeStep) Vm).setNextStatesConditional(Map.of(
				E11,
				Arrays.asList(VmFD),
				E12,
				Arrays.asList(VmRej)));

		((CompositeStep) Vm).setBranchingProbsConditional(Map.of(
				E11,
				Arrays.asList(1.0),
				E12,
				Arrays.asList(1.0)));

		// Level depth 0
		depth = 0;

		Map<Step, List<Step>> nextStates = null;//Required to avoid ambiguity
		Region region3 = new Region(VmRes, RegionType.NEVER, true);
		Region region4 = new Region(VmmA, RegionType.EXIT);
		Region region5 = new Region(VmmRejW, RegionType.EXIT);

		Step S0 = new CompositeStep(
				"S0",  
				Arrays.asList(region3,region4,region5), 
				nextStates, 
				null, 
				depth);

		// VmmRes corresponds to VMM_Restarting in Figure 8
		Step VmmRes = new SimpleStep(
				"VmmRes", 
				vmmRestarting_pFunction,
				Arrays.asList(S0), 
				Arrays.asList(1.0), 
				depth);

		// VmmR corresponds to VMM_Repairing in Figure 8
		Step VmmR = new SimpleStep(
				"VmmR", 
				vmmRepairing_pFunction,
				Arrays.asList(VmmRes), 
				Arrays.asList(1.0), 
				depth);

		// VmmFD corresponds to VMM_Failure_Detecting in Figure 8
		Step VmmFD = new SimpleStep(
				"VmmFD", 
				GEN.newUniform(new OmegaBigDecimal("0.0"), new OmegaBigDecimal("0.4")), 
				Arrays.asList(VmmR), 
				Arrays.asList(1.0), 
				depth);

		// VmmRej corresponds to VMM_Rejuvenating in Figure 8
		Step VmmRej = new SimpleStep(
				"VmmRej", 
				vmmRejuvenating_pFunction,
				Arrays.asList(S0), 
				Arrays.asList(1.0), 
				depth);

		// VmSW corresponds to VM_Stop_Waiting in Figure 8
		Step VmSW = new SimpleStep(
				"VmSW", 
				GEN.newUniform(new OmegaBigDecimal("0.1"), new OmegaBigDecimal("0.2")), 
				Arrays.asList(VmmRej), 
				Arrays.asList(1.0), 
				depth);

		((CompositeStep) S0).setNextStatesConditional(Map.of(
				E2,
				Arrays.asList(VmmFD),
				E3,
				Arrays.asList(VmSW,VmmRej)));

		((CompositeStep) S0).setBranchingProbsConditional(Map.of(
				E2,
				Arrays.asList(1.0),
				E3,
				Arrays.asList(pstop, 1.0-pstop)));

		Vm.setCyleLooping(true, false);
		VmFD.setCyleLooping(true, false);
		VmR.setCyleLooping(true, true);
		VmRej.setCyleLooping(true, true);

		return new HSMP(S0);
	}

	 */
}
