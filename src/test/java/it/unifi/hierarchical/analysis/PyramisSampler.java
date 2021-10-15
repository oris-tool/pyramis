package it.unifi.hierarchical.analysis;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jfree.util.StringUtils;
import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.domain.DBMZone;
import org.oristool.math.expression.Expolynomial;
import org.oristool.math.expression.Variable;
import org.oristool.math.function.Erlang;
import org.oristool.math.function.Function;
import org.oristool.math.function.GEN;
import org.oristool.simulator.samplers.ErlangSampler;
import org.oristool.simulator.samplers.ExponentialSampler;
import org.oristool.simulator.samplers.MetropolisHastings;
import org.oristool.simulator.samplers.Sampler;
import org.oristool.simulator.samplers.UniformSampler;


/**
 * Execution of calculations
 *  
 */
//for changed_from(24) / 168 first 
public class PyramisSampler {

	public static void main(String[] args){
		int[] i96b = new int[] {10,15,20,25};
		for(int i=0;i<i96b.length;i++) {
			sample(i96b[i]);
		}
	}

	public static void sample(int MILLIONS){
		long EndTime = MILLIONS*1000000;
		long toEndTime = EndTime;

		//dove raccolgo
		Map<String,Double> probabs = new HashMap<>();
		Map<String,Sampler> samplers = new HashMap<>();

		initialize(24,probabs,samplers);
		
		probabs.put("2", 0.0);
		probabs.put("3", 0.0);
		
		probabs.put("4", 0.0);
		
		probabs.put("n", 0.0);
		probabs.put("availableAt168", 0.0);
		probabs.put("n168", 0.0);

		
		int i=0;
		int num=0;
		while(toEndTime>0) {
			System.out.println("in "+i);
			i++;
			//Analyze
			Date start = new Date();

			double TIME_LIMIT=1000000; //prints intermediate results every 1000 seconds

			String print="src//main//resources//pyramisCaseStudyGrundTruth//"+"s24high_"+MILLIONS+"_"+(i*TIME_LIMIT)+".txt";


		


			num+= samplerOut(probabs, samplers, TIME_LIMIT);



			Date end = new Date();
			long time = end.getTime() - start.getTime();
			System.out.println("Time Hierarchical SMP analysis:" + time + "ms");

			toEndTime-=num;

			File file = new File(print);
			try (PrintWriter writer = new PrintWriter(file)) {

				writer.write("TIME=  "		+((EndTime-toEndTime)/1000)	+"sec \n\n");

				
				String[] names = new String[] {"VmRejW", "VmR", "VmFD", "VmRej", "VmF", "VmA", "VmRes", "VmmA", "VmmF", "VmmRejW", "VmmFD", "VmmR", "VmmRes", "VmSW", "VmmRej"};

				for(int ii=0;ii<names.length;ii++) {
					
					int n = names[ii].length();
					String x = names[ii];
					for(int r=0;r<8-n;r++) {
						x=x+" ";
					}
					
					writer.write(x+ (probabs.get(names[ii])/ (i*TIME_LIMIT) )+"\n");
				}
				writer.write("\n");
				writer.write(num+"\n");
				writer.write(probabs.get("2")/probabs.get("n")+"\n");
				writer.write(probabs.get("3")/probabs.get("n")+"\n");
				writer.write(probabs.get("4")/probabs.get("n")+"\n");
				
				writer.write(probabs.get("availableAt168")/probabs.get("n")+"\n");
				
				

			} catch (FileNotFoundException e) {
				System.out.println("errore");
				System.out.println(e.getMessage());
			}

		}
	}


	private static void add(String s,Double v, Map<String,Double> m) {
		Double d = v+m.get(s);
		m.put(s, d);
	}


	private static class DetSampler implements Sampler {

		private double det;
		public DetSampler(double f) {

			this.det = f;

		}

		@Override
		public BigDecimal getSample() {

			return new BigDecimal(det);
		}


	}

	private static class ExpolSampler implements Sampler {

		private MetropolisHastings mp;
		public ExpolSampler(Function f) {

			this.mp = new MetropolisHastings(f);

		}

		@Override
		public BigDecimal getSample() {

//			for(int i=0; i<100;i++) {
//				mp.getSample();
//			}

			return mp.getSample();
		}


	}

	private static class ErlExpSampler implements Sampler {

		private Sampler erl;
		private Sampler exp;
		public ErlExpSampler(int shape, BigDecimal rateErl, BigDecimal rateExp) {

			this.erl = new ErlangSampler(new Erlang(Variable.X,shape, rateErl));
			this.exp = new ExponentialSampler(rateExp);
		}

		@Override
		public BigDecimal getSample() {

			BigDecimal sample = erl.getSample();
			sample = sample.add(exp.getSample());

			return sample;
		}


	}

	
	private static class ExponErlangSampler implements Sampler {

		private Sampler samp1;
		private Sampler samp2;

		public ExponErlangSampler(Sampler s1, Sampler s2) {


			this.samp1 = s1;
			this.samp2=s2;

		}

		public ExponErlangSampler(double s1, double s2) {


			this.samp1 = new ExponentialSampler(new BigDecimal(s1));
			this.samp2= new ExponentialSampler(new BigDecimal(s2));

		}

		@Override
		public BigDecimal getSample() {



			return samp1.getSample().add(samp2.getSample());
		}


	}


	private static void initialize(int VMMTIME,Map<String,Double> probs, Map<String,Sampler> samplers) {

		DBMZone vmRejuvenating_d_0 = new DBMZone(new Variable("x"));
		Expolynomial vmRejuvenating_e_0 = Expolynomial.fromString("-318523 * Exp[-57.5509 x] + 4777840* Exp[-57.5509 x] * x + -15926100 * Exp[-57.5509 x] * x^2");
		//Normalization
		vmRejuvenating_e_0.multiply(new BigDecimal(0.9999893996609198));
		vmRejuvenating_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("0.2"));
		vmRejuvenating_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-0.1"));
		GEN vmRejuvenating_pFunction = new GEN(vmRejuvenating_d_0, vmRejuvenating_e_0);



		DBMZone vmRepairing_d_0 = new DBMZone(new Variable("x"));
		Expolynomial vmRepairing_e_0 = Expolynomial.fromString("-71.0006 * Exp[-6.41167 x] + 319.503 * Exp[-6.41167 x] * x + -142.001 * Exp[-6.41167 x] * x^2");
		//Normalization
		vmRepairing_e_0.multiply(new BigDecimal(0.9999962125608038));
		vmRepairing_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("2"));
		vmRepairing_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-0.25"));
		GEN vmRepairing_pFunction = new GEN(vmRepairing_d_0, vmRepairing_e_0);



		DBMZone vmRestarting_d_0 = new DBMZone(new Variable("x"));
		Expolynomial vmRestarting_e_0 = Expolynomial.fromString("-686.089 * Exp[-11.3061 x] + 3087.4 * Exp[-11.3061 x] * x + -1372.18 * Exp[-11.3061 x] * x^2");
		//Normalization
		vmRestarting_e_0.multiply(new BigDecimal(1.0000064788681096));
		vmRestarting_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("2"));
		vmRestarting_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-0.25"));
		GEN vmRestarting_pFunction = new GEN(vmRestarting_d_0, vmRestarting_e_0);



		DBMZone vmStopWaiting_d_0 = new DBMZone(new Variable("x"));
		Expolynomial vmStopWaiting_e_0 = Expolynomial.fromString("-21332 * Exp[ -36.68 x] + 319991 * Exp[-36.68 x] * x + -1066640 * Exp[-36.68 x] * x^2");
		//Normalization
		vmStopWaiting_e_0.multiply(new BigDecimal(0.9994014187474192));
		vmStopWaiting_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("0.2"));
		vmStopWaiting_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-0.1"));
		GEN vmStopWaiting_pFunction = new GEN(vmStopWaiting_d_0, vmStopWaiting_e_0);



		DBMZone vmmRejuvenating_d_0 = new DBMZone(new Variable("x"));
		Expolynomial vmmRejuvenating_e_0 = Expolynomial.fromString("-10.24 * Exp[3.86164 x] + 136.534 * Exp[3.86164 x] * x + -341.335 * Exp[3.86164 x] * x^2");
		//Normalization
		vmmRejuvenating_e_0.multiply(new BigDecimal(0.9999779820608894));
		vmmRejuvenating_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("0.3"));
		vmmRejuvenating_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-0.1"));
		GEN vmmRejuvenating_pFunction = new GEN(vmmRejuvenating_d_0, vmmRejuvenating_e_0);


		DBMZone vmmRepairing_d_0 = new DBMZone(new Variable("x"));
		Expolynomial vmmRepairing_e_0 = Expolynomial.fromString("-41.7333 * Exp[-3.22812 x] + 97.3777 * Exp[-3.22812 x] * x + -27.8222 * Exp[-3.22812 x] * x^2");
		//Normalization
		vmmRepairing_e_0.multiply(new BigDecimal(0.9999988824655345));
		vmmRepairing_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("3"));
		vmmRepairing_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-0.5"));
		GEN vmmRepairing_pFunction = new GEN(vmmRepairing_d_0, vmmRepairing_e_0);


		DBMZone vmmRestarting_d_0 = new DBMZone(new Variable("x"));
		Expolynomial vmmRestarting_e_0 = Expolynomial.fromString("-1369.71 * Exp[-7.28859 x] + 3195.98 * Exp[-7.28859 x] * x + -913.137 * Exp[-7.28859 x] * x^2");
		//Normalization
		vmmRestarting_e_0.multiply(new BigDecimal(1.0000184105663141));
		vmmRestarting_d_0.setCoefficient(new Variable("x"), new Variable("t*"), new OmegaBigDecimal("3"));
		vmmRestarting_d_0.setCoefficient(new Variable("t*"), new Variable("x"), new OmegaBigDecimal("-0.5"));
		GEN vmmRestarting_pFunction = new GEN(vmmRestarting_d_0, vmmRestarting_e_0);


		String[] names = new String[] {"VmRejW", "VmR", "VmFD", "VmRej", "VmF", "VmA", "VmRes", "VmmA", "VmmF", "VmmRejW", "VmmFD", "VmmR", "VmmRes", "VmSW", "VmmRej"};

		for(int i=0;i<names.length;i++) {
			probs.put(names[i], 0.0);
		}

		samplers.put("VmRejW", new DetSampler(VMMTIME));
		samplers.put("VmR", new ExpolSampler(vmRepairing_pFunction));
		samplers.put("VmFD", new UniformSampler(new BigDecimal("0.0"), new BigDecimal("0.4")));
		samplers.put("VmRej", new ExpolSampler(vmRejuvenating_pFunction));
	
		samplers.put("VmF", new ExponErlangSampler(0.0738004, 0.0171087));
		samplers.put("VmA", new ExponErlangSampler(0.0138889, 0.0104167));
		
		
		samplers.put("VmRes", new ExpolSampler(vmRestarting_pFunction));
		
//		samplers.put("VmmA", new ExponentialSampler(new BigDecimal("0.001")));
//		samplers.put("VmmF", new ExponentialSampler(new BigDecimal("0.006")));
		samplers.put("VmmF", new ExponErlangSampler(0.0378072, 0.00706464));
		samplers.put("VmmA", new ExponErlangSampler(0.00460392, 0.0021988));
		samplers.put("VmmRejW", new DetSampler(96));
		
		samplers.put("VmmFD", new UniformSampler(new BigDecimal("0.0"), new BigDecimal("0.4")));
		samplers.put("VmmR", new ExpolSampler(vmmRepairing_pFunction));
		samplers.put("VmmRes", new ExpolSampler(vmmRestarting_pFunction));
		samplers.put("VmSW", new UniformSampler(new BigDecimal("0.1"), new BigDecimal("0.2")));
		samplers.put("VmmRej", new ExpolSampler(vmmRejuvenating_pFunction));


		

	}


	public static int samplerOut(Map<String,Double> probabs, Map<String,Sampler> samplers, double TIME_LIMIT) {

		double countDown = TIME_LIMIT;

		String cur = "Start";
		
		int c=0;
		while(countDown>0.) {

			double actualTime=0.0;
						
			double time=0.0;
			if(cur.equals("Start")) {
				c++;
				//System.out.println("in "+c);

				double rej = samplers.get("VmmRejW").getSample().doubleValue();
				double age = samplers.get("VmmA").getSample().doubleValue();
				double age2 = samplers.get("VmmF").getSample().doubleValue(); 

				
				
				
				time = Math.min(rej, age+age2);
				actualTime=Math.min(countDown,time);
				
				if(actualTime>age) {
					add("VmmA",age, probabs);
					add("VmmF",actualTime-age, probabs);
				}else {
					add("VmmA",actualTime, probabs);
				}
				add("VmmRejW",actualTime, probabs);

				samplerIn(probabs,samplers, actualTime);

				if(time!=rej) {
					cur="VmmFD";
				}else {
					double d = Math.random();
					if(d<0.9997) {
						cur="VmSW";
					}else {
						cur="VmmRej";
					}
				}



			}else if (cur.equals("VmSW")){
				time = samplers.get("VmSW").getSample().doubleValue();
				actualTime=Math.min(countDown,time);
				add("VmSW",actualTime, probabs);
				cur = "VmmRej";

			}else if (cur.equals("VmmRej")){
				time = samplers.get("VmmRej").getSample().doubleValue();
				actualTime=Math.min(countDown,time);
				add("VmmRej",actualTime, probabs);
				cur = "Start";
			}else if (cur.equals("VmmFD")){
				time = samplers.get("VmmFD").getSample().doubleValue();
				actualTime=Math.min(countDown,time);
				add("VmmFD",actualTime, probabs);
				cur = "VmmR";
			}else if (cur.equals("VmmR")){
				time = samplers.get("VmmR").getSample().doubleValue();
				actualTime=Math.min(countDown,time);
				add("VmmR",actualTime, probabs);
				cur = "VmmRes";
			}else if (cur.equals("VmmRes")){
				time = samplers.get("VmmRes").getSample().doubleValue();
				actualTime=Math.min(countDown,time);
				add("VmmRes",actualTime, probabs);
				cur = "Start";
			}

			countDown-=time;

		}


		return c;
	}

	public static void samplerIn(Map<String,Double> probabs, Map<String,Sampler> samplers, double TIME_LIMIT) {

		add("n",1., probabs);
		
		
		double countDown = TIME_LIMIT;

		int count=0;
		boolean lastVm =false;
		
		String cur = "VmRes";
		while(countDown>0.) {

			double actualTime;	
			
			double time=0.0;
			if(cur.equals("Vm")) {
				
				lastVm =true;
				count++;

				double rej = samplers.get("VmRejW").getSample().doubleValue();
				double age = samplers.get("VmA").getSample().doubleValue();
				double age2 = samplers.get("VmF").getSample().doubleValue(); 

				time = Math.min(rej, age+age2);
				actualTime=Math.min(countDown,time);
											
				if(actualTime>age) {
					add("VmA",age, probabs);
					add("VmF",actualTime-age, probabs);
				}else {
					add("VmA",actualTime, probabs);
				}
				add("VmRejW",actualTime, probabs);

				if(time!=rej) {
					cur="VmFD";
				}else {
					cur="VmRej";

				}

			}else if (cur.equals("VmRej")){
				lastVm =false;
				time = samplers.get("VmRej").getSample().doubleValue();
				actualTime=Math.min(countDown,time);
				add("VmRej",actualTime, probabs);
				cur = "Vm";
			}else if (cur.equals("VmFD")){
				lastVm =false;
				time = samplers.get("VmFD").getSample().doubleValue();
				actualTime=Math.min(countDown,time);
				add("VmFD",actualTime, probabs);
				cur = "VmR";
			}else if (cur.equals("VmR")){
				lastVm =false;
				time = samplers.get("VmR").getSample().doubleValue();
				actualTime=Math.min(countDown,time);
				add("VmR",actualTime, probabs);
				cur = "Vm";
			}else if (cur.equals("VmRes")){
				lastVm =false;
				time = samplers.get("VmRes").getSample().doubleValue();
				actualTime=Math.min(countDown,time);
				add("VmRes",actualTime, probabs);
				cur = "Vm";
			}

			countDown-=time;

		}
		if(count<3)
			add("2",1., probabs);
		if(count==3)
			add("3",1., probabs);
		if(count>3)
			add("4",1., probabs);
		
		if(lastVm)
			add("availableAt168",1., probabs);

	}


}
