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
		test1();

	}


	public static void test1() {    

		//LOOPS determines the amount of iteration the cyle is executed for
		//cycleType identifies if the cycle is on a single branch(1), on 2 branches(2) or all branches(3) of the failure model
		for(int LOOPS=2;LOOPS<5;LOOPS++) {
			for(int cycleType=3;cycleType<4;cycleType++) {
				for(int rejPeriodI=0;rejPeriodI<4;rejPeriodI++) {

					double[] rejPeriodA= {0.5,0.75,1.,1.5};
					double rejPeriod= rejPeriodA[rejPeriodI];
					int parallel=3;
					int depth=2;
					int seq=3;
					RegionType lastB = RegionType.EXIT;
					int last =0;

					try {


						String print="src//main//resources//pyramisAnalyticCycles//"+ "cycle_"+TIME_STEP+"_L"+LOOPS+"_p-"+parallel+"_d-"+(depth+1)+"_s-"+seq+"_rp-"+rejPeriodI+"_Final-"+last+"_l-"+cycleType+".txt";

						//HSMP
						//Build the model
						HierarchicalSMP model = HSMP_JournalCycles.build(rejPeriod,cycleType,parallel,depth,seq,expolSame, lastB,LOOPS);

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
						file.getParentFile().mkdirs();
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


						PyramisSamplerCycles.sampleFromTime(rejPeriodI,LOOPS, cycleType, time, parallel,depth,seq,expolSame,lastB);


					}
					catch(Exception e){
						System.out.println(e.toString());
					}
				}
			}
		}
	}


}

