package it.unifi.hierarchical.analysis_journal;



import java.util.Date;
import java.util.Map;

import org.junit.Test;

import it.unifi.hierarchical.analysis.HierarchicalSMPAnalysis;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.example.hsmp_journal.HSMP_OnlyLoop;


/**
 * Test to check correctness of analysis when there is a composite state inside another composite state.
 * This was the first test created. The idea was to check the correctness of the shift and project approach
 */
public class TestOnlyLoop {

	private static final double TIME_STEP = 0.1;
	private static final double TIME_LIMIT = 200;

	@Test
	public void test1() {     
		//HSMP
		//Build the model
		HierarchicalSMP model = HSMP_OnlyLoop.build();

		//Analyze
		Date start = new Date();
		HierarchicalSMPAnalysis analysis = new HierarchicalSMPAnalysis(model);
		Map<String, Double> ssHSMP = analysis.evaluateSteadyState(TIME_STEP, TIME_LIMIT);
		Double resultHSMP1 = ssHSMP.get("State1");
		Double resultHSMP2 = ssHSMP.get("State2");

		Date end = new Date();
		System.out.println("Time Hierarchical SMP analysis:" + (end.getTime() - start.getTime()) + "ms");


		//Compare results
		System.out.println("of 0.5");
		System.out.println("HSMP  state 1: " + resultHSMP1);

		System.out.println("of 0.5");
		System.out.println("HSMP  state 2: " + resultHSMP2);





	}
}
