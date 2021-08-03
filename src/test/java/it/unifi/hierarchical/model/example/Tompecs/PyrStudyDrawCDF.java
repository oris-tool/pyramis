package it.unifi.hierarchical.model.example.Tompecs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PyrStudyDrawCDF {

	public PyrStudyDrawCDF() {
		// TODO Auto-generated constructor stub
	}




	public static void main(String[] args){

		double step= 17./1000.;
		
		DecimalFormat formatter = new DecimalFormat("#0.000000000000000");
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		formatter.setDecimalFormatSymbols(symbols);

		String[] pathnamesSample;

		File fDirec = new File("src//main//resources//pyramisSimulation");

		// Populates the array with names of files and directories
		pathnamesSample = fDirec.list();

		for(String pathnameS: pathnamesSample) {
			System.out.println(pathnameS);

			int count=0;
			double[] mille = new double[1000];
			
			File sample = new File("src//main//resources//pyramisSimulation//"+pathnameS);

			try (BufferedReader br = new BufferedReader(new FileReader(sample))) {
				String line;
			
				while ((line = br.readLine()) != null) {
					
					String[] lines = line.split(" ");
					for(int i=0; i<lines.length;i++) {
						count++;
						Double val= Double.valueOf(lines[i]);
						int q = (int) Math.round(val/step);
							//find q
						while(q<1000) {
							mille[q]+=1;
							q++;							
						}
						
						
						
					}
					
					
				}


			} catch (IOException e) {

				e.printStackTrace();
			}

			
			for(int i=0;i<1000;i++) {
				mille[i]/=count;
			}
			
			
			File file = new File("src//main//resources//pyramisRes//cdf_"+pathnameS);
			try (PrintWriter writer = new PrintWriter(file)) {
				for(int i=0;i<1000;i++) {
					writer.write(i*step+" "+mille[i]+"\n");
				}
				
				


			} catch (FileNotFoundException e) {
				System.out.println("errore");
				System.out.println(e.getMessage());
			}
		
					

		}

	}
	private static void add(String s,Double v, Map<String,Double> m, Map<String,String> aliasSearch) {

		String alias = aliasSearch.get(s);
	
		if(alias!=null) {
			if(!m.containsKey(alias))
				m.put(alias, 0.0);

			Double d = v+m.get(alias);
			m.put(alias, d);
			
			System.out.println(s+" ->"+alias);
		}
	}


}
