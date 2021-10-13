package it.unifi.hierarchical.analysisScalability;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.function.Function;
import org.oristool.math.function.GEN;
import org.oristool.simulator.samplers.MetropolisHastings;
import org.oristool.simulator.samplers.Sampler;



/**
 * Execution of calculations
 *  
 */
public class PyramisCreateSampFiles {


	public static int c =10;
	


	public static void main(String[] args){

		NumberFormat formatter = new DecimalFormat("#0.00000");

		
		GEN exp10 = GEN.newExpolynomial("0.22517 * Exp[-0.311427 x] * x + -0.022517 * Exp[-0.311427 x] * x^2", OmegaBigDecimal.ZERO, OmegaBigDecimal.TEN); 
		GEN exp = GEN.newExpolynomial("22.517 * Exp[-3.11427 x] * x + -22.517 * Exp[-3.11427 x] * x^2", OmegaBigDecimal.ZERO, OmegaBigDecimal.ONE); 

		Sampler samp10 = new ExpolSampler(exp10);

		Sampler samp = new ExpolSampler(exp);

		for(int i=0;i<100;i++) {

			String print= "src//main//resources//samplesExpolinomial//sample"+(c*100+i)+".txt";

			File file = new File(print);
			try (PrintWriter writer = new PrintWriter(file)) {

				//1000 row 1000 samples per row
				for(int q=0;q<999;q++) {
					for(int j=0;j<999;j++) {
						writer.write(formatter.format(samp.getSample().floatValue())+" ");
					}
					writer.write(formatter.format(samp.getSample().floatValue()));
					writer.write("\n");
				}
				for(int j=0;j<999;j++) {
					writer.write(formatter.format(samp.getSample().floatValue())+" ");
				}
				writer.write(formatter.format(samp.getSample().floatValue()));


			} catch (FileNotFoundException e) {
				System.out.println("errore");
				System.out.println(e.getMessage());
			}

			print= "src//main//resources//samplesExpolinomial10//sample"+(c*100+i)+".txt";

			file = new File(print);
			try (PrintWriter writer = new PrintWriter(file)) {

				//1000 row 1000 samples per row
				for(int q=0;q<999;q++) {
					for(int j=0;j<999;j++) {
						writer.write(formatter.format(samp10.getSample().floatValue())+" ");
					}
					writer.write(formatter.format(samp10.getSample().floatValue()));
					writer.write("\n");
				}
				for(int j=0;j<999;j++) {
					writer.write(formatter.format(samp10.getSample().floatValue())+" ");
				}
				writer.write(formatter.format(samp10.getSample().floatValue()));


			} catch (FileNotFoundException e) {
				System.out.println("errore");
				System.out.println(e.getMessage());
			}


		}


	}








	private static class ExpolSampler implements Sampler {

		private MetropolisHastings mp;
		public ExpolSampler(Function f) {

			this.mp = new MetropolisHastings(f);

		}

		@Override
		public BigDecimal getSample() {

			return mp.getSample();
		}


	}






}
