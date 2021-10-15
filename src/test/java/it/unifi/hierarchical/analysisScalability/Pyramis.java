package it.unifi.hierarchical.analysisScalability;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import it.unifi.hierarchical.analysis.HierarchicalSMPAnalysis;
import it.unifi.hierarchical.analysis.HierarchicalSMPAnalysisForced;
import it.unifi.hierarchical.analysis.NumericalValues;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.example.hsmp_scalability.HSMP_JournalScalabilityVerbose;
import it.unifi.hierarchical.model.example.hsmp_scalability.HSMP_JournalScalability;


/**
 * Execution of calculations
 *  
 */
public class Pyramis {


	//Need to be chosen according to the model, based on which is the maximum time elapsed in a region or a state
	private static  double TIME_LIMIT =17;
	private static final double TIME_STEP = 0.0025;
	
	
	

	public static void main(String[] args){

		Integer repetitionsOfSampling= null;
		if(args.length>0) {
			repetitionsOfSampling = Integer.valueOf(100);
		}
		
		test1(repetitionsOfSampling);
		
	}


	public static void test1(Integer repetitionsOfSampling) {    


		for(int last=1; last>-1;last--) {
			RegionType lastB = (last==1)? RegionType.FINAL : RegionType.EXIT;

			for(int parallel=2;parallel<5; parallel++) {
				for(int depth=1;depth<4;depth++) {
					for(int seq=2;seq<5;seq++) {



						try {


							String print= "src//main//resources//pyramisAnalytic//"+""+TIME_STEP+"_p-"+parallel+"_d-"+(depth+1)+"_s-"+seq+"_Final-"+last+".txt";


							//HSMP
							//Build the model
							HierarchicalSMP model = HSMP_JournalScalability.build(parallel,depth,seq,true, lastB);

							Set<String> sList = HSMP_JournalScalability.getStates();

							//Analyze
							Date start = new Date();
							HierarchicalSMPAnalysis analysis = new HierarchicalSMPAnalysis(model, 0);
							Map<String, Double> ssHSMP = analysis.evaluateSteadyState(TIME_STEP, TIME_LIMIT);


							Date end = new Date();
							long time = end.getTime() - start.getTime();
							System.out.println(parallel+ "  "+ depth+ "  "+ seq+" "+last );
							System.out.println("Time Hierarchical SMP analysis:" + time + "ms");
							
							File file = new File(print);
							file.getParentFile().mkdirs();
							try (PrintWriter writer = new PrintWriter(file)) {
								writer.write("TIME=  "		+time 		+"ms \n\n");
								writer.write("p="+parallel+" d="+(depth+1)+" s="+seq+ "\n");
								for(String s: sList) {

									writer.write(s+" "+ssHSMP.get(s)+"\n");
									
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
							

							PyramisSampler.sampleFromTime(time, parallel,depth,seq,true,lastB, repetitionsOfSampling);


						}
						catch(Exception e){
							System.out.println(e.toString());
						}

					}

				}
			}
		}
	}

}

