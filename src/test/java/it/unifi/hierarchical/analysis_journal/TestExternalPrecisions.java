package it.unifi.hierarchical.analysis_journal;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.domain.DBMZone;
import org.oristool.math.expression.Expolynomial;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.GEN;
import org.oristool.math.function.PartitionedGEN;

import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.example.hsmp_journal.HSMP_Cycles;
import it.unifi.hierarchical.utils.NumericalUtils;


/**
 * To test if external states can be forced to arbitrary precisions
 *  
 */
public class TestExternalPrecisions {
    

	
	
	private static int LOOPS= 2;  
	
	
    private static final double TIME_STEP = 0.01;
    //Need to be chosen according to the model, based on which is the maximum time elapsed in a region or a state
    private static final double TIME_LIMIT = 20;
    
    @Test
    public void test1() {     
        //HSMP
        //Build the model
        
        //Analyze
        Date start = new Date();
        
        
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

        
        
    	SimpleState state = new SimpleState(
				"VmmRes", 
				vmmRestarting_pFunction,
				null, 
				null, 
				0,
				0.2, 3.1);
        
        double up= 3;
        double step = up/50000;
        
        up=96;
        step = 0.005;
        double[] values = NumericalUtils.evaluateFunction(state.getDensityFunction(), new OmegaBigDecimal(""+up), new BigDecimal(""+step));
		values = NumericalUtils.computeCDFFromPDF(values,  new BigDecimal(""+step));
	
		System.out.println(step);
//		System.out.println(Arrays.toString(values));

		Date end = new Date();
		long time = end.getTime() - start.getTime();
		System.out.println("Time Hierarchical SMP analysis:" + time + "ms");



		
      
        
    }
}
