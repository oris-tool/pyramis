package it.unifi.hierarchical.analysis_journal;



import java.util.Date;
import java.util.Map;

import org.junit.Test;

import it.unifi.hierarchical.analysis.HierarchicalSMPAnalysis;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.example.hsmp_journal.HSMP_OnlyZero;


/**
 * Test to check correctness of analysis when there is a composite state inside another composite state.
 * This was the first test created. The idea was to check the correctness of the shift and project approach
 */
public class TestOnlyZero {

	private static final double TIME_STEP = 0.001;
	//Need to be chosen according to the model, based on which is the maximum time elapsed in a region or a state
	private static final double TIME_LIMIT = 4;

	@Test
	public void test1() {     
		//HSMP
		//Build the model
		HierarchicalSMP model = HSMP_OnlyZero.build();

		//Analyze
		Date start = new Date();
		HierarchicalSMPAnalysis analysis = new HierarchicalSMPAnalysis(model);
		Map<String, Double> ssHSMP = analysis.evaluateSteadyState(TIME_STEP, TIME_LIMIT);
		Double resultHSMP1 = ssHSMP.get("State1");
		Double resultHSMP2 = ssHSMP.get("State2");
		Double resultHSMP0 = ssHSMP.get("S0");
		Double resultHSMPTop = ssHSMP.get("StateTop");

		Date end = new Date();
		System.out.println("Time Hierarchical SMP analysis:" + (end.getTime() - start.getTime()) + "ms");


		//Compare results
		System.out.println("of 0.375");
		System.out.println("HSMP  state 1: " + resultHSMP1);

		System.out.println("of 0.625");
		System.out.println("HSMP  state 2: " + resultHSMP2);

		System.out.println("HSMP  state 0: " + resultHSMP0);
		System.out.println("HSMP  state Top: " + resultHSMPTop);



	}
}
