package it.unifi.hierarchical.analysisScalability;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.Set;


import it.unifi.hierarchical.analysis.HierarchicalSMPAnalysis;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.example.hsmp_scalability.HSMP_JournalLongEvents;


/**
 * Execution of calculations
 *  
 */
public class PyramisLongEvents {


	//Need to be chosen according to the model, based on which is the maximum time elapsed in a region or a state
	 
	private static final double TIME_STEP = 0.0025;
	private static final boolean expolSame = true;


	public static void main(String[] args){
		test1(3,100, 1);
				
	}


	public static void test1(int l, int y, int iter) {    


		for(int last=0; last>-1;last--) {
			RegionType lastB = (last==1)? RegionType.FINAL : RegionType.EXIT;

			for(int parallel=3;parallel<4; parallel++) {
				for(int depth=2;depth<3;depth++) {
					for(int seq=3;seq<4;seq++) {

						double TIME_LIMIT =1+3*(l*10+3-l);

						try {


							String print="src//main//resources//pyramisAnalyticLong//"+ ""+TIME_STEP+"_p-"+parallel+"_d-"+(depth+1)+"_s-"+seq+"_Final-"+last+"_l-"+l+"-"+iter+".txt";


							//HSMP
							//Build the model
							HierarchicalSMP model = HSMP_JournalLongEvents.build(l,parallel,depth,seq,expolSame, lastB);

							Set<String> sList = HSMP_JournalLongEvents.getStates();

							//Analyze
							Date start = new Date();
							HierarchicalSMPAnalysis analysis = new HierarchicalSMPAnalysis(model, 0);
							Map<String, Double> ssHSMP = analysis.evaluateSteadyState(TIME_STEP, TIME_LIMIT);


							Date end = new Date();
							long time = end.getTime() - start.getTime();
							System.out.println(parallel+ "  "+ depth+ "  "+ seq+" "+last );
							System.out.println("Time Hierarchical SMP analysis:" + time + "ms");
							Set<String> zeros =HSMP_JournalLongEvents.zeros;

							File file = new File(print);
							file.getParentFile().mkdirs();
							try (PrintWriter writer = new PrintWriter(file)) {
								writer.write("TIME=  "		+time 		+"ms \n\n");
								writer.write("p="+parallel+" d="+(depth+1)+" s="+seq+ "\n");
								for(String s: sList) {

									if(zeros.contains(s)) {
										writer.write(s+" "+0.0+"\n");
										continue;

									}else {
										writer.write(s+" "+ssHSMP.get(s)+"\n");
									}
								}






							} catch (FileNotFoundException e) {
								System.out.println("errore");
								System.out.println(e.getMessage());
							}

							PyramisSamplerLongEvents.sampleFromTime(l, time, parallel,depth,seq,expolSame,lastB, y);


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

