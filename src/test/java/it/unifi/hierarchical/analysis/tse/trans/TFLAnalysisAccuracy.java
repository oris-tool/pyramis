/* This program is part of the PYRAMIS library for compositional analysis of hierarchical UML statecharts.
 * Copyright (C) 2019-2021 The PYRAMIS Authors.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;

/**
 * This class supports the comparison of the analysis results with the ground truth
 * for the HSMP models of the basic pattern (and its variants
 * depending on the behaviour length, the parallelism degree, the hierarchy depth, and the composite type)
 * and for the HSMP models with large support PDFs and with rate events
 * used in the case study on transient timed failure logic analysis of component based systems 
 * of the paper titled "Compositional Analysis of Hierarchical UML Statecharts" (see Figure 4).
 */
public class TFLAnalysisAccuracy {

	public TFLAnalysisAccuracy() {
	}

	public static void main(String[] args){

		String[] suffix = {"","Long","Rare"};

		for(int i=0;i<3;i++) {
			DecimalFormat formatter = new DecimalFormat("#0.000000");
			DecimalFormatSymbols symbols = new DecimalFormatSymbols();
			symbols.setDecimalSeparator('.');
			formatter.setDecimalFormatSymbols(symbols);

			String[] pathnamesSample;

			File fDirec = new File("src//main//resources//pyramisSimulation"+suffix[i]);

			// Populate the array with the names of files and directories
			pathnamesSample = fDirec.list();
			if(pathnamesSample==null) {
				System.out.println("Directory src/main/resources/pyramisSimulation"+suffix[i]+" is missing.\n"
						+ "Follow README.md to execute each step needed to repeat the experiments.");
				return;
			}

			if(pathnamesSample.length==0) {
				System.out.println("Directory empty: I was expecting a file in "+"src//main//resources//pyramisSimulation"+suffix[i]);
				return;
			}

			for(String pathnameS: pathnamesSample) {

				Map<String,Double> mapSimul = new HashMap<String,Double>();
				Map<String,Double> mapAnalytic; 

				File sample = new File("src//main//resources//pyramisSimulation"+suffix[i]+"//"+pathnameS);

				try (BufferedReader br = new BufferedReader(new FileReader(sample))) {
					String line;
					line = br.readLine();
					line = br.readLine();
					while ((line = br.readLine()) != null) {
						String[] lines = line.split(" +");
						if(lines.length==2)
							mapSimul.put(lines[0], Double.valueOf(lines[1]));
					}
				} catch (IOException e) {

					e.printStackTrace();
				}

				int p = Integer.valueOf(pathnameS.substring(2, 3));
				int d= Integer.valueOf(pathnameS.substring(6, 7));
				int ss= Integer.valueOf(pathnameS.substring(10, 11));
				int Final= pathnameS.contains("EXIT")? 0:1;

				String[] pathnames;

				String suffixExtra ="";
				for(int q=0;q<2;q++) {
					if(q==1) suffixExtra="SameTime";

					File f = new File("src//main//resources//pyramisAnalytic"+suffixExtra+suffix[i]);

					// Populate the array with the names of files and directories
					pathnames = f.list();

					// For each pathname in the pathnames array
					for (String pathname : pathnames) {

						mapAnalytic = new HashMap<String,Double>();
						String ex= (Final>0)? "FINAL" : "EXIT";
						boolean aa = pathname.contains("p-"+p+"_d-"+d+"_s-"+ss+"_Final-"+Final);
						boolean bb =  pathname.contains("sameTime_p-"+p+"_d-"+d+"_s-"+ss+"_Final-"+ex);

						if(aa || bb) {

							File in = new File("src//main//resources//pyramisAnalytic"+suffixExtra+suffix[i]+"//"+pathname);

									try (BufferedReader br = new BufferedReader(new FileReader(in))) {
										String line;
										line = br.readLine();
										line = br.readLine();
										while ((line = br.readLine()) != null) {
											if(line.startsWith("p")) {
												continue;
											}
											line=line.replaceAll("0[.]", " 0.");
											String[] lines = line.split(" +");
											if(lines.length==2) {
												mapAnalytic.put(lines[0], Double.valueOf(lines[1]));
											}
										}
									} catch (IOException e) {
										e.printStackTrace();
									}

							Double min=10.;
							Double max=0.;
							Double average=0.;

							int count=0;

							double scarto=0.;

							System.out.println(pathname);
							System.out.println(mapAnalytic);
							for(String st: mapSimul.keySet()) {
								System.out.println(st);

								if(mapAnalytic.containsKey(st)) {
									Double s= mapSimul.get(st);
									Double a= mapAnalytic.get(st);
									Double r;
									if(s>0.0) {
										r= Math.abs(s-a)/s;
										scarto+= r*r;
										System.out.println(r+" "+s+" "+ a);
										if(!(r>0.))
											continue;
										min= Math.min(min, r);
										max=Math.max(max, r);
										average+=r;
										count++;

									}
								}
							}

							average /=count;

							scarto= scarto/ (count-1);
							scarto-= average*average;

							scarto = Math.sqrt(scarto);

							File file = new File("src//main//resources//pyramisRes"+suffixExtra+suffix[i]+"//res_"+pathname);
							file.getParentFile().mkdirs();

							try (PrintWriter writer = new PrintWriter(file)) {

								writer.write(formatter.format(min)+", "+ formatter.format(max)+", "+formatter.format(average)+", "+formatter.format(scarto));


							} catch (FileNotFoundException e) {
								System.out.println("errore");
								System.out.println(e.getMessage());
							}

						}
					}
				}
			}
		}
	}
}