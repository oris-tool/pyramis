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
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * This class supports the comparison of the analysis results with the ground truth
 * for the HSMP model used in the case study on steady-state analysis of software rejuvenation in virtual servers 
 * of the paper titled "Compositional Analysis of Hierarchical UML Statecharts" (see Figure 8).
 */
public class SWRejSimulationAccuracy {

	public SWRejSimulationAccuracy() {
	}

	public static void main(String[] args){

		DecimalFormat formatter = new DecimalFormat("#0.0000E0");
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		formatter.setDecimalFormatSymbols(symbols);

		String[] pathnamesSample;

		File fDirec = new File("src//main//resources//pyramisCaseStudyGroundTruth");

		// Populate the array with the names of files and directories
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
		
		for(String pathnameS: pathnamesSample) {

			Map<String,Double> mapSimul = new HashMap<String,Double>();
			Map<String,LinkedList<Double>> mapAnalytic= new HashMap<String,LinkedList<Double>>();

			File sample = new File("src//main//resources//pyramisCaseStudyGroundTruth//"+pathnameS);

			try (BufferedReader br = new BufferedReader(new FileReader(sample))) {
				String line;
				line = br.readLine();
				line = br.readLine();
				while ((line = br.readLine()) != null) {
					String[] lines = line.split(" +");
					if(lines.length==2) {
						mapSimul.put(lines[0], Double.valueOf(lines[1]));
						mapAnalytic.put(lines[0], new LinkedList<Double>());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			String[] pathnames;

			File f = new File("src//main//resources//pyramisSameTime");

			// Populate the array with the names of files and directories
			pathnames = f.list();

			// For each pathname in the pathnames array
			for (String pathname : pathnames) {

				File in = new File("src//main//resources//pyramisSameTime//"+pathname);

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
							mapAnalytic.get(lines[0]).add(Double.valueOf(lines[1]));

						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			LinkedList<Double> d= new LinkedList<Double>();

			LinkedList<Double> sc= new LinkedList<Double>();

			for(String st: mapSimul.keySet()) {
				Double s= mapSimul.get(st);
				int count=0;

				Double average=0.;
				Double var=0.;

				for(Double ddd : mapAnalytic.get(st) ) {
					Double r;
					r= Math.abs(s-ddd)/s;
					var+= r*r;
					average+=r;
					count++;
				}
				average /=count;

				var= var/ (count-1);
				var-= average*average;

				var = Math.sqrt(var);
				d.add(average);
				sc.add(var);

			}

			File file = new File("src//main//resources//pyramisResSameTime//res_"+pathnames[0].substring(0,3));
			file.getParentFile().mkdirs();
			try (PrintWriter writer = new PrintWriter(file)) {
				int ccc=0;
				for(String st: mapSimul.keySet()) {
					writer.write(st+"    "+formatter.format(d.get(ccc))+"    "+formatter.format(sc.get(ccc))+"\n");
					ccc++;
				}

			} catch (FileNotFoundException e) {
				System.out.println("errore");
				System.out.println(e.getMessage());
			}
		}

		formatter = new DecimalFormat("#0.0000E0");
		symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		formatter.setDecimalFormatSymbols(symbols);

		fDirec = new File("src//main//resources//pyramisCaseStudyGroundTruth");

		// Populate the array with the names of files and directories
		pathnamesSample = fDirec.list();

		for(String pathnameS: pathnamesSample) {

			Map<String,Double> mapSimul = new HashMap<String,Double>();
			Map<String,LinkedList<Double>> mapAnalytic= new HashMap<String,LinkedList<Double>>();

			File sample = new File("src//main//resources//pyramisCaseStudyGroundTruth//"+pathnameS);

			try (BufferedReader br = new BufferedReader(new FileReader(sample))) {
				String line;
				line = br.readLine();
				line = br.readLine();
				while ((line = br.readLine()) != null) {
					String[] lines = line.split(" +");
					if(lines.length==2) {
						mapSimul.put(lines[0], Double.valueOf(lines[1]));
						mapAnalytic.put(lines[0], new LinkedList<Double>());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			String[] pathnames;

			File f = new File("src//main//resources//pyramisSameTime");

			// Populate the array with the names of files and directories
			pathnames = f.list();

			// For each pathname in the pathnames array
			for (String pathname : pathnames) {

				File in = new File("src//main//resources//pyramisSameTime//"+pathname);

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
							mapAnalytic.get(lines[0]).add(Double.valueOf(lines[1]));
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			LinkedList<Double> d= new LinkedList<Double>();

			LinkedList<Double> sc= new LinkedList<Double>();

			for(String st: mapSimul.keySet()) {
//				Double s= mapSimul.get(st);
				int count=0;

				Double average=0.;
				Double var=0.;

				for(Double ddd : mapAnalytic.get(st) ) {
					Double r;
					r= ddd;
					var+= r*r;
					average+=r;
					count++;
				}
				average /=count;

				var= var/ (count-1);
				var-= average*average;

				var = Math.sqrt(var);
				d.add(average);
				sc.add(var);
			}

			File file = new File("src//main//resources//pyramisResSameTime//prb_"+pathnames[0].substring(0,3));
			file.getParentFile().mkdirs();
			try (PrintWriter writer = new PrintWriter(file)) {
				int ccc=0;
				for(String st: mapSimul.keySet()) {
					writer.write(st+"    "+formatter.format(d.get(ccc))+"    "+formatter.format(sc.get(ccc))+"\n");
					ccc++;
				}
			} catch (FileNotFoundException e) {
				System.out.println("errore");
				System.out.println(e.getMessage());
			}
		}
	}
}