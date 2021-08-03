package it.unifi.hierarchical.analysis_journal;

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

public class PyrStudyResults {

	public PyrStudyResults() {
		// TODO Auto-generated constructor stub
	}

	
	public static final int millions=2197; 
	
	//FIXME Va e Vf non ricostruiscono in modo corretto Vm... c'è un qualche valore che non fa sommare a 1
	// qui li rinormalizzo (SONO EXIT PERÒ QUINDI NON SI PASSA TEMPO IN FINAL!!
	public static void main(String[] args){
		String[] names = new String[] {"VmRejW", "VmR", "VmFD", "VmRej", "VmF", "VmA", "VmRes", "VmmA", "VmmF", "VmmRejW", "VmmFD", "VmmR", "VmmRes", "VmSW", "VmmRej"};
		Double[] probsSample = new Double[names.length]; 

		NumberFormat formatter = new DecimalFormat("#0.00%");

		File sample = new File("src//main//resources//pyramis//sample.txt");

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

		// Populates the array with names of files and directories
		pathnames = f.list();

		// For each pathname in the pathnames array
		for (String pathname : pathnames) {

			Double Vm=0.;
			Double Va=0.;
			Double Vf=0.;
			
			int indexVa=0;

			int indexVf=0;
			
			if(!pathname.equals("sample.txt")) {

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
				File file = new File("src//main//resources//pyramisRes//"+pathname.substring(0, pathname.length()-4)+"_"+millions+".txt");
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
