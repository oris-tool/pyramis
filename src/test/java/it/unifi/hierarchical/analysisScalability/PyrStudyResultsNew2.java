package it.unifi.hierarchical.analysisScalability;

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
import java.util.Map;

import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region.RegionType;
import it.unifi.hierarchical.model.example.hsmp_scalability.HSMP_JournalScalabilityVerbose;

public class PyrStudyResultsNew2 {

	public PyrStudyResultsNew2() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args){


		DecimalFormat formatter = new DecimalFormat("#0.000000");
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		formatter.setDecimalFormatSymbols(symbols);

		String[] pathnamesSample;

		File fDirec = new File("src//main//resources//pyramisSimulation");

		// Populates the array with names of files and directories
		pathnamesSample = fDirec.list();

		for(String pathnameS: pathnamesSample) {

			Map<String,Double> mapSimul = new HashMap<String,Double>();
			Map<String,Double> mapAnalytic; 

			File sample = new File("src//main//resources//pyramisSimulation//"+pathnameS);

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

			//Build the model
			HierarchicalSMP model = HSMP_JournalScalabilityVerbose.build(3,2,3,true, RegionType.EXIT);

			Map<String,String> mapToCopy =HSMP_JournalScalabilityVerbose.toCopy;
			

			File file = new File("src//main//resources//results//"+pathnameS);
			try (PrintWriter writer = new PrintWriter(file)) {

				writer.write("\n\n");
				for(String x: mapSimul.keySet()) {
					
					Double val=mapSimul.get(x);
					if(mapToCopy.containsKey(x)) {
						val= val*(1-0.005)/0.5;
					} else if(mapToCopy.containsValue(x)) {
						val= val*(0.005)/0.5;
					}
					
					writer.write(x+" "+val+"\n");

					
				}


			} catch (FileNotFoundException e) {
				System.out.println("errore");
				System.out.println(e.getMessage());
			}

		}
	}





}
