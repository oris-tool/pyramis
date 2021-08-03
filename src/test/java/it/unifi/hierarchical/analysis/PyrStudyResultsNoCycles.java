package it.unifi.hierarchical.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class PyrStudyResultsNoCycles {

	public PyrStudyResultsNoCycles() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args){
		String[] names = new String[] {"VmmA", "VmmF", "VmmRejW", "VmmFD", "VmmR", "VmmRes", "VmSW", "VmmRej"};
		Double[] probsSample = new Double[names.length]; 

		NumberFormat formatter = new DecimalFormat("#0.00%");

		File sample = new File("src//main//resources//pyramisNoCycle//sample.txt");

		try (BufferedReader br = new BufferedReader(new FileReader(sample))) {
			String line;
			int x=0;
			while ((line = br.readLine()) != null) {
				if(x>8 && x<17) {
					String[] lines = line.split(" +");

					probsSample[x-9]= Double.valueOf(lines[1]);
				}
				x++;
			}


		} catch (IOException e) {

			e.printStackTrace();
		}


		String[] pathnames;

		File f = new File("src//main//resources//pyramisNoCycle");

		// Populates the array with names of files and directories
		pathnames = f.list();

		// For each pathname in the pathnames array
		for (String pathname : pathnames) {

			if(!pathname.equals("sample.txt")) {

				File in = new File("src//main//resources//pyramisNoCycle//"+pathname);

				Double[] probs = new Double[names.length]; 
				try (BufferedReader br = new BufferedReader(new FileReader(in))) {
					String line;
					int x=0;
					while ((line = br.readLine()) != null) {
						if(x>8 && x<17) {
							String[] lines = line.split(" +");

							probs[x-9]= Double.valueOf(lines[1]);
						}
						x++;
					}


				} catch (IOException e) {

					e.printStackTrace();
				}

			
				double average =0.;
				File file = new File("src//main//resources//pyramisResNoCycle//"+pathname.substring(2));
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
					average/=15;
					writer.write("aver    "+ formatter.format(average)+"\n");
					
			
				} catch (FileNotFoundException e) {
					System.out.println("errore");
					System.out.println(e.getMessage());
				}
			
			}
		}


	}


}
