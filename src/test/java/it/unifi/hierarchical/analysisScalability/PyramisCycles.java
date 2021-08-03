package it.unifi.hierarchical.analysisScalability;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.Set;


import it.unifi.hierarchical.analysis.HierarchicalSMPAnalysis;
import it.unifi.hierarchical.analysis.NumericalValues;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.example.hsmp_scalability.HSMP_JournalCycles;


/**
 * Execution of calculations
 *  
 */
public class PyramisCycles {


	//Need to be chosen according to the model, based on which is the maximum time elapsed in a region or a state
	private static final double TIME_LIMIT =10;
	private static final double TIME_STEP = 0.0025;
	private static final boolean expolSame = true;



	public static void main(String[] args){
		//Integer.valueOf(args[0])
		
			test1(Integer.valueOf(args[0]));
		
	}


	public static void test1(int x) {    


		int cycles =3;

		for(int LOOPS=x;LOOPS<x+1;LOOPS++) {
			int parallel=3;
			int depth=2;
			int seq=3;
			RegionType lastB = RegionType.EXIT;
			int last =0;

			try {


				String print= "cycle_"+TIME_STEP+"_L"+LOOPS+"_p-"+parallel+"_d-"+(depth+1)+"_s-"+seq+"_Final-"+last+"_l-"+cycles+".txt";

				//HSMP
				//Build the model
				HierarchicalSMP model = HSMP_JournalCycles.build(cycles,parallel,depth,seq,expolSame, lastB,LOOPS);

				Set<String> sList = HSMP_JournalCycles.getStates();

				//Analyze
				Date start = new Date();
				HierarchicalSMPAnalysis analysis = new HierarchicalSMPAnalysis(model, 0);
				Map<String, Double> ssHSMP = analysis.evaluateSteadyState(TIME_STEP, TIME_LIMIT);


				Date end = new Date();
				long time = end.getTime() - start.getTime();
				System.out.println(parallel+ "  "+ depth+ "  "+ seq+" "+last );
				System.out.println("Time Hierarchical SMP analysis:" + time + "ms");
				Set<String> zeros =HSMP_JournalCycles.zeros;

				File file = new File(print);
				try (PrintWriter writer = new PrintWriter(file)) {
					writer.write("TIME=  "		+time 		+"ms \n\n");
					writer.write("p="+parallel+" d="+(depth+1)+" s="+seq+ "\n");
					for(String s: sList) {


						if(zeros.contains(s)) {
							writer.write(s+" "+0.0+"\n");


						} else {
							writer.write(s+" "+ssHSMP.get(s)+"\n");
						}
					}






				} catch (FileNotFoundException e) {
					System.out.println("errore");
					System.out.println(e.getMessage());
				}

				
				
				file = new File("cdf_"+print);
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
				
				
				PyramisSamplerCycles.sampleFromTime(LOOPS, cycles, time, parallel,depth,seq,expolSame,lastB, 1);


			}
			catch(Exception e){
				System.out.println(e.toString());
			}
		}

	}


}

