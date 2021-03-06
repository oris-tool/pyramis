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

package it.unifi.hierarchical.analysis.tse.steady;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * This class supports the comparison of the analysis results with the ground truth
 * for the HSMP model used in the case study on steady-state analysis of software rejuvenation in virtual servers 
 * of the paper titled "Compositional Analysis of Hierarchical UML Statecharts" (see Figures 8 and 10, and Table 9).
 */
public class SWRejAnalysisAccuracy {

	public SWRejAnalysisAccuracy() {
	}

	public static final int millions=2197; 

	public static void main(String[] args){
		String[] names = new String[] {"VmRejW", "VmR", "VmFD", "VmRej", "VmF", "VmA", "VmRes", "VmmA", "VmmF", "VmmRejW", "VmmFD", "VmmR", "VmmRes", "VmSW", "VmmRej"};
		Double[] probsSample = new Double[names.length]; 

		NumberFormat formatter = new DecimalFormat("#0.00%");

		String[] pathnamesSample;

		File fDirec = new File("src//main//resources//pyramisCaseStudyGroundTruth");

		// Populates the array with names of files and directories
		pathnamesSample = fDirec.list();

		if(pathnamesSample==null) {
			System.out.println("Directory src/main/resources/pyramisCaseStudyGroundTruth is missing.\n"
					+ "Follow README.md to execute each step needed to repeat the experiments.");
			return;
		}
		
		if(pathnamesSample.length==0) {
			System.out.println("Directory empty: I was expecting a file in src//main//resources//pyramisCaseStudyGroundTruth ");
			return;
		}

		File sample = new File("src//main//resources//pyramisCaseStudyGroundTruth//"+pathnamesSample[0]);

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

		String[] pathnames;

		File f = new File("src//main//resources//pyramis");

		// Populate the array with the names of files and directories
		pathnames = f.list();

		// For each pathname in the pathnames array
		for (String pathname : pathnames) {

			Double Vm=0.;
			Double Va=0.;
			Double Vf=0.;

			int indexVa=0;
			int indexVf=0;

			File in = new File("src//main//resources//pyramis//"+pathname);

			Double[] probs = new Double[names.length]; 
			try (BufferedReader br = new BufferedReader(new FileReader(in))) {
				String line;
				int x=0;
				while ((line = br.readLine()) != null) {
					if(x>1 && x<17) {
						String[] lines = line.split(" +");
						if(lines[0].equals("Vm")) {
							Vm=Double.valueOf(lines[1]);
						}else if(lines[0].equals("VmA")) {
							Va=Double.valueOf(lines[1]);
							indexVa=x-2;
						}else if(lines[0].equals("VmF")) {
							Vf=Double.valueOf(lines[1]);
							indexVf=x-2;
						}
						probs[x-2]= Double.valueOf(lines[1]);
					}
					x++;
				}
			} catch (IOException e) {

				e.printStackTrace();
			}

			probs[indexVa] = Va/(Va+Vf)*Vm;
			probs[indexVf] = Vf/(Va+Vf)*Vm;

			double average =0.;
			File file = new File("src//main//resources//pyramisCaseStudyRes//"+pathname.substring(0, pathname.length()-4)+"_"+millions+".txt");
			file.getParentFile().mkdirs();
			
			try (PrintWriter writer = new PrintWriter(file)) {

				for(int ii=0;ii<names.length;ii++) {

					int n = names[ii].length();
					String x = names[ii];
					for(int r=0;r<8-n;r++) {
						x=x+" ";
					}

					double val = Math.abs(((probsSample[ii]-probs[ii])/probsSample[ii]));

					average+=val;

					writer.write(x+ formatter.format(val)+"\n");
				}
				average/=names.length;
				writer.write("aver    "+ formatter.format(average)+"\n");


			} catch (FileNotFoundException e) {
				System.out.println("errore");
				System.out.println(e.getMessage());
			}
		}
	}
}