package it.unifi.hierarchical.analysis_journal;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

import org.junit.Test;

import it.unifi.hierarchical.analysis.HierarchicalSMPAnalysis;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.example.hsmp_journal.HSMP_TestDepthNever;


/**
 * Test to check correctness of analysis when there is a composite state inside another composite state.
 * This was the first test created. The idea was to check the correctness of the shift and project approach
 */
public class TestDepthNeverending {
    	
    private static final double TIME_STEP = 0.01;
    //Need to be chosen according to the model, based on which is the maximum time elapsed in a region or a state
    private static final double TIME_LIMIT = 20;
    
    @Test
    public void test1() {     
        //HSMP
        //Build the model
        HierarchicalSMP model = HSMP_TestDepthNever.build();
        
        //Analyze
        Date start = new Date();
        HierarchicalSMPAnalysis analysis = new HierarchicalSMPAnalysis(model);
        Map<String, Double> ssHSMP = analysis.evaluateSteadyState(TIME_STEP, TIME_LIMIT);
        Double rsA = ssHSMP.get("A");
        Double rsA1 = ssHSMP.get("A1");
        Double rsA2 = ssHSMP.get("A2");
        Double rsB = ssHSMP.get("B");
        Double rsC = ssHSMP.get("C");
        Double rsS0 = ssHSMP.get("S0");
        Double rsS1 = ssHSMP.get("S1");
        Double rsE = ssHSMP.get("E");
        Double rsD = ssHSMP.get("D");
	

		Date end = new Date();
		long time = end.getTime() - start.getTime();
		System.out.println("Time Hierarchical SMP analysis:" + time + "ms");



		File file = new File("DEPTHNEVER"+TIME_STEP+".txt");
		try (PrintWriter writer = new PrintWriter(file)) {

			writer.write("time,"+time+"\n");
			writer.write("A,  "+rsA+"\n");
			writer.write("A1, "+rsA1+"\n");
			writer.write("A2, "+rsA2+"\n");
			writer.write("B,  "+rsB+"\n");
			writer.write("C,  "+rsC+"\n");
			writer.write("S0, "+rsS0+"\n");
			writer.write("S1, "+rsS1+"\n");
			writer.write("E,  "+rsE+"\n");
			writer.write("D,  "+rsD+"\n");
 

		} catch (FileNotFoundException e) {
			System.out.println("errore");
			System.out.println(e.getMessage());
		}

		
		
      
        
    }
}
