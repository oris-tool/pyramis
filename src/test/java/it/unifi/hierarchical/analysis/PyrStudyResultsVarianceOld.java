package it.unifi.hierarchical.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class PyrStudyResultsVarianceOld {

	public PyrStudyResultsVarianceOld() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args){


		DecimalFormat formatter = new DecimalFormat("#0.0000E0");
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		formatter.setDecimalFormatSymbols(symbols);

		String[] pathnamesSample;

		File fDirec = new File("src//main//resources//pyramis");

		// Populates the array with names of files and directories
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

			File f = new File("src//main//resources//pyramis");

			// Populates the array with names of files and directories
			pathnames = f.list();

			// For each pathname in the pathnames array
			for (String pathname : pathnames) {

				File in = new File("src//main//resources//pyramis//"+pathname);

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

			File file = new File("src//main//resources//pyramisCaseStudyRes//res_"+pathnames[0].substring(0,3));
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
