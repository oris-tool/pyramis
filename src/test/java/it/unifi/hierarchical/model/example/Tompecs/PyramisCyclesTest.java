package it.unifi.hierarchical.model.example.Tompecs;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.Set;


import it.unifi.hierarchical.analysis.HierarchicalSMPAnalysis;
import it.unifi.hierarchical.analysis.HierarchicalSMPAnalysisForced;
import it.unifi.hierarchical.analysis.NumericalValues;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.example.hsmp_journal.HSMP_Cycles;
import it.unifi.hierarchical.model.example.hsmp_journal.HSMP_Journal;


/**
 * Execution of calculations
 *  
 */
public class PyramisCyclesTest {


	//Need to be chosen according to the model, based on which is the maximum time elapsed in a region or a state
	private static final double TIME_LIMIT =10;
	private static final double TIME_STEP = 0.0025;
	private static final boolean expolSame = true;



	public static void main(String[] args){
		//Integer.valueOf(args[0])

		test1();

	}


	public static void test1() {    

		int parallel=3;
		int seq=3;
		RegionType lastB = RegionType.EXIT;


		try {


			String print= "testcycles.txt";

			//HSMP
			//Build the model
			HierarchicalSMP model = HSMP_JournalCyclesTest.build(parallel,seq, lastB);


			//Analyze
			Date start = new Date();
			HierarchicalSMPAnalysis analysis = new HierarchicalSMPAnalysis(model, 0);
			Map<String, Double> ssHSMP = analysis.evaluateSteadyState(TIME_STEP, TIME_LIMIT);


			Date end = new Date();
			long time = end.getTime() - start.getTime();
			System.out.println("Time Hierarchical SMP analysis:" + time + "ms");
			Set<String> zeros =HSMP_JournalCycles.zeros;

			



			File file = new File("cdf_"+print);
			NumericalValues cdf = HierarchicalSMPAnalysis.cdf;
			double[] val= cdf.getValues();
			System.out.println(cdf.getStep());
			try (PrintWriter writer = new PrintWriter(file)) {


				for(int i=0;i<val.length;i++) {


					writer.write(i*cdf.getStep()+" "+val[i]+"\n");

				}






			} catch (FileNotFoundException e) {
				System.out.println("errore");
				System.out.println(e.getMessage());
			}


			//PyramisSamplerCyclesFromFiles.sampleFromTime(LOOPS, cycles, time/1000, parallel,depth,seq,expolSame,lastB, samplingBatch);


		}
		catch(Exception e){
			System.out.println(e.toString());
		}
	}

}




