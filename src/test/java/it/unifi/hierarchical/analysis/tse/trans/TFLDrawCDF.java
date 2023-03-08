/* This program is part of the PYRAMIS library for compositional analysis of hierarchical UML statecharts.
 * Copyright (C) 2019-2023 The PYRAMIS Authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.unifi.hierarchical.analysis.tse.trans;

import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * This class supports the derivation of the Cumulative Distribution Function (CDF) of the failure process
 * for each HSMP analysis and each HSMP simulation performed 
 * in the case study on transient timed failure logic analysis of component based systems 
 * of the paper titled "Compositional Analysis of Hierarchical UML Statecharts" (see Figure 7).
 */
public class TFLDrawCDF {

	public TFLDrawCDF() {
	}

	public static void main(String[] args){

		double step= 17./1000.;
		
		DecimalFormat formatter = new DecimalFormat("#0.000000000000000");
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		formatter.setDecimalFormatSymbols(symbols);

		String[] pathnamesSample;

		File fDirec = new File("src//main//resources//groundTruthDistributions");

		// Populate the array with the names of files and directories
		pathnamesSample = fDirec.list();
		
		if(pathnamesSample==null) {
			System.out.println("Directory src/main/resources/groundTruthDistributions is missing.\n"
					+ "Follow README.md to execute each step needed to repeat the experiments.");
			return;
		}

		if(pathnamesSample.length==0) {
			System.out.println("Directory empty: I was expecting a file in "+"src//main//resources//groundTruthDistributions");
			return;
		}
		
		for(String pathnameS: pathnamesSample) {
			System.out.println(pathnameS);

			int count=0;	
			double[] mille = new double[1000];
			
			File sample = new File("src//main//resources//groundTruthDistributions//"+pathnameS);

			try (BufferedReader br = new BufferedReader(new FileReader(sample))) {
				String line;
			
				while ((line = br.readLine()) != null) {
					
					String[] lines = line.split(" ");
					for(int i=0; i<lines.length;i++) {
						count++;
						Double val= Double.valueOf(lines[i]);
						int q = (int) Math.round(val/step);
						// Find q
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
			
			File file = new File("src//main//resources//groundTruthCDF//cdf_"+pathnameS);
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
	
//	private static void add(String s,Double v, Map<String,Double> m, Map<String,String> aliasSearch) {
//
//		String alias = aliasSearch.get(s);
//	
//		if(alias!=null) {
//			if(!m.containsKey(alias))
//				m.put(alias, 0.0);
//
//			Double d = v+m.get(alias);
//			m.put(alias, d);
//			
//			System.out.println(s+" ->"+alias);
//		}
//	}
}
