package it.unifi.hierarchical.analysisScalability;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedList;

import it.unifi.hierarchical.model.Region.RegionType;

public class PyrStudyResultsVarianceOverSimulations {

	public PyrStudyResultsVarianceOverSimulations() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args){


		DecimalFormat formatter = new DecimalFormat("#0.000000");
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		formatter.setDecimalFormatSymbols(symbols);

		String[] pathnamesSample;

		File fDirec = new File("src//main//resources//pyramisRes");

		// Populates the array with names of files and directories
		pathnamesSample = fDirec.list();


		for(int last=1; last>-1;last--) {
			for(int parallel=2;parallel<5; parallel++) {
				for(int depth=2;depth<5;depth++) {
					for(int seq=2;seq<5;seq++) {


						boolean any=false;

						LinkedList<Double> min = new LinkedList<Double>();
						LinkedList<Double> max = new LinkedList<Double>();
						LinkedList<Double> ave = new LinkedList<Double>();
						LinkedList<Double> scarto = new LinkedList<Double>();





						for(String pathnameS: pathnamesSample) {

							int xx=8;
							
							int p = Integer.valueOf(pathnameS.substring(15+xx, 16+xx));
							int d= Integer.valueOf(pathnameS.substring(19+xx, 20+xx));
							int ss= Integer.valueOf(pathnameS.substring(23+xx, 24+xx));
							int Final= pathnameS.contains("EXIT")? 0:1;


							if(p==parallel && d==depth && ss==seq && Final==last) {
								any=true;

								File sample = new File("src//main//resources//pyramisRes//"+pathnameS);

								try (BufferedReader br = new BufferedReader(new FileReader(sample))) {
									String line;
									line = br.readLine();
									String[] lines = line.split(",");
									min.add( Double.valueOf(lines[0]));
									max.add( Double.valueOf(lines[1]));
									ave.add( Double.valueOf(lines[2]));
									scarto.add( Double.valueOf(lines[3]));
								} catch (IOException e) {

									e.printStackTrace();
								}
							}
						}

						if(any) {

							double sumMin=0.;
							double scMin=0.;

							double sumMax=0.;
							double scMax=0.;

							double sumAve=0.;
							double scAve=0.;

							double sumSc=0.;
							double scSc=0.;
							for(int i=0;i<min.size();i++) {

								sumMin+=min.get(i);
								scMin +=min.get(i)*min.get(i); 

								sumMax+=max.get(i);
								scMax +=max.get(i)*max.get(i); 

								sumAve+=ave.get(i);
								scAve +=ave.get(i)*ave.get(i); 

								sumSc +=scarto.get(i); 
								scSc  +=scarto.get(i)*scarto.get(i);  
							}

							sumMin/=min.size();
							sumMax/=min.size();
							sumAve/=min.size();
							sumSc /=min.size();

							scMin/=(min.size()-1);
							scMax/=(min.size()-1);
							scAve/=(min.size()-1);
							scSc /=(min.size()-1);

							scMin-= sumMin*sumMin;
							scMax-= sumMax*sumMax;
							scAve-= sumAve*sumAve;
							scSc -= sumSc *sumSc ;

							scMin= Math.sqrt(scMin);
							scMax= Math.sqrt(scMax);
							scAve= Math.sqrt(scAve);
							scSc = Math.sqrt(scSc );

							File file = new File("src//main//resources//summa//summa_"+"_p-"+parallel+"_d-"+(depth)+"_s-"+seq+"_Final-"+last+".txt");
							try (PrintWriter writer = new PrintWriter(file)) {

								writer.write(formatter.format(sumMin)+", "+ formatter.format(scMin)+",");
								writer.write(formatter.format(sumMax)+", "+ formatter.format(scMax)+",");
								writer.write(formatter.format(sumAve)+", "+ formatter.format(scAve)+",");
								writer.write(formatter.format(sumSc)+", "+ formatter.format(scSc));


							} catch (FileNotFoundException e) {
								System.out.println("errore");
								System.out.println(e.getMessage());
							}

						}
					}}}}

	}


}
