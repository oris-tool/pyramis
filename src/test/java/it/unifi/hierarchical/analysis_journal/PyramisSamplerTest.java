package it.unifi.hierarchical.analysis_journal;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;



/**
 * Execution of calculations
 *  
 */
public class PyramisSamplerTest {

	public static void main(String[] args){


		int i=0;
		while(true) {
			i++;
			System.out.println(i*87600);
			String[] names = new String[] {"VmRejW", "VmR", "VmFD", "VmRej", "VmF", "VmA", "VmRes", "VmmA", "VmmF", "VmmRejW", "VmmFD", "VmmR", "VmmRes", "VmSW", "VmmRej"};

			Double[] probsSample = new Double[names.length]; 
			Double[] probsSample1 = new Double[names.length]; 

			File sample = new File("src//main//resources//samplers//s_rel_"+i*87600+".0.txt");

			try (BufferedReader br = new BufferedReader(new FileReader(sample))) {
				String line;
				int x=0;
				while ((line = br.readLine()) != null) {
					if(x>1 && x<17) {
						String[] lines = line.split(" +");

						probsSample[x-2]= Double.valueOf(lines[1]);
					}
					x++;
				}


			} catch (IOException e) {

				e.printStackTrace();
			}

			File sample1 = new File("src//main//resources//samplers//s_rel_"+(i+1)*87600+".0.txt");

			try (BufferedReader br = new BufferedReader(new FileReader(sample1))) {
				String line;
				int x=0;
				while ((line = br.readLine()) != null) {
					if(x>1 && x<17) {
						String[] lines = line.split(" +");

						probsSample1[x-2]= Double.valueOf(lines[1]);
					}
					x++;
				}


			} catch (IOException e) {

				e.printStackTrace();
			}

			System.out.println(i+" "+isChanged(probsSample,probsSample1));
		}

	}

	private static double isChanged(Double[] probabs,Double[] probOld) {
		
		double maxChange=0.;

		for(int i=0;i<probabs.length;i++) {

			
			double x = Math.abs((probabs[i]-probOld[i])/probabs[i]) ;
			if(x>maxChange)
				maxChange=x;
				
				
		
		}
	
		return maxChange;
	}


	private static void deepCopy(Map<String,Double> probabs,Map<String,Double> probOld) {
		for(String s: probabs.keySet()) {
			probOld.put(s, new Double(probabs.get(s)));
		}

	}

	private static void add(String s,Double v, Map<String,Double> m) {
		Double d = v+m.get(s);
		m.put(s, d);
	}


}
