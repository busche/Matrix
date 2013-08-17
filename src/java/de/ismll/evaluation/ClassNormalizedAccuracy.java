package de.ismll.evaluation;

import java.util.Arrays;

import de.ismll.experimental.Output;
import de.ismll.experimental.SinkTest;
import de.ismll.table.IntVector;
import de.ismll.table.IntVectors;
import de.ismll.table.Matrices;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultIntMatrix;
import de.ismll.table.impl.DefaultVector;
import de.ismll.utilities.Assert;

public class ClassNormalizedAccuracy implements Evaluator{

	public static class NominalPerformanceStatistics {

		public double accuracy;
		public double[] precisionPerClass;
		public double unnormalizedError;
		public double unnormalizedCorrect;
		public double[] errorsPerClass;
		public double classnormalizedError;
		public double classnormalizedCorrect;
		public double[] recallPerClass;
		public double[] fmeasurePerClass;
		public double matthewsCorrelationCoefficient;
		public double cohensKappa;
		public boolean binaryProblem;

		/**
		 * Aggregates individual statistics (e.g., per fold) and computes variances.
		 * 
		 * @param perf
		 * @return
		 */
		public static AggregatedNominalPerformanceStatistics aggregate(
				NominalPerformanceStatistics[] perf) {
			AggregatedNominalPerformanceStatistics ret = new AggregatedNominalPerformanceStatistics();

			//
			//
			//			double sum_accuracy=0;
			//			double[] sum_precisionPerClass=new double[perf[0].precisionPerClass.length];
			//			double sum_unnormalizedError=0;
			//			double sum_unnormalizedCorrect=0;
			//			double[] sum_errorsPerClass=new double[perf[0].errorsPerClass.length];
			//			double sum_classnormalizedError=0;
			//			double sum_classnormalizedCorrect=0;
			//			double[] sum_recallPerClass=new double[perf[0].recallPerClass.length];
			//			double[] sum_fmeasurePerClass=new double[perf[0].fmeasurePerClass.length];
			//			double sum_matthewsCorrelationCoefficient=0;
			//			double sum_cohensKappa=0;
			//
			//
			//			for (NominalPerformanceStatistics c : perf) {
			//				sum_accuracy += c.accuracy;
			//				add(sum_precisionPerClass,  c.precisionPerClass);
			//				sum_unnormalizedError += c.unnormalizedError;
			//				sum_unnormalizedCorrect += c.unnormalizedCorrect;
			//				add(sum_errorsPerClass,  c.errorsPerClass);
			//
			//				sum_classnormalizedError += c.classnormalizedError;
			//				sum_classnormalizedCorrect += c.classnormalizedCorrect;
			//				add(sum_recallPerClass,  c.recallPerClass);
			//
			//				add(sum_fmeasurePerClass,  c.fmeasurePerClass);
			//
			//				sum_matthewsCorrelationCoefficient += c.matthewsCorrelationCoefficient;
			//				sum_cohensKappa += c.cohensKappa;
			//
			//			}

			Vector accuracies = new DefaultVector(perf.length);

			Vector unnormalizedError= new DefaultVector(perf.length);
			Vector unnormalizedCorrect= new DefaultVector(perf.length);
			Vector classnormalizedError= new DefaultVector(perf.length);
			Vector classnormalizedCorrect= new DefaultVector(perf.length);
			Vector matthewsCorrelationCoefficient= new DefaultVector(perf.length);
			Vector cohensKappa= new DefaultVector(perf.length);

			boolean binary=true;

			for (int i = 0; i < perf.length; i++) {
				accuracies.set(i, (float) perf[i].accuracy);
				unnormalizedError.set(i, (float) perf[i].unnormalizedError);
				unnormalizedCorrect.set(i, (float) perf[i].unnormalizedCorrect);
				classnormalizedError.set(i, (float) perf[i].classnormalizedError);
				classnormalizedCorrect.set(i, (float) perf[i].classnormalizedCorrect);
				matthewsCorrelationCoefficient.set(i, (float) perf[i].matthewsCorrelationCoefficient);
				cohensKappa.set(i, (float) perf[i].cohensKappa);
				binary &= perf[i].binaryProblem;
			}

			ret.accuracy = Vectors.average(accuracies);
			ret.accuracyVar = Vectors.variance(accuracies);
			ret.unnormalizedError = Vectors.average(unnormalizedError);
			ret.unnormalizedErrorVar = Vectors.variance(unnormalizedError);
			ret.unnormalizedCorrect = Vectors.average(unnormalizedCorrect);
			ret.unnormalizedCorrectVar = Vectors.variance(unnormalizedCorrect);
			ret.classnormalizedError = Vectors.average(classnormalizedError);
			ret.classnormalizedErrorVar = Vectors.variance(classnormalizedError);
			ret.classnormalizedCorrect = Vectors.average(classnormalizedCorrect);
			ret.classnormalizedCorrectVar = Vectors.variance(classnormalizedCorrect);
			ret.matthewsCorrelationCoefficient = Vectors.average(matthewsCorrelationCoefficient);
			ret.matthewsCorrelationCoefficientVar = Vectors.variance(matthewsCorrelationCoefficient);
			ret.cohensKappa = Vectors.average(cohensKappa);
			ret.cohensKappaVar = Vectors.variance(cohensKappa);
			ret.binaryProblem=binary;
			// TODO: Aggregate arrays

			ret.precisionPerClass=new double[0];
			ret.errorsPerClass=new double[0];
			ret.recallPerClass=new double[0];
			ret.fmeasurePerClass=new double[0];

			return ret;
		}

		//		private static void add(double[] sum_precisionPerClass,
		//				double[] precisionPerClass2) {
		//			for (int i = 0; i < precisionPerClass2.length; i++) {
		//				sum_precisionPerClass[i] += precisionPerClass2[i];
		//			}
		//		}

	}

	public static class AggregatedNominalPerformanceStatistics extends NominalPerformanceStatistics {

		public double cohensKappaVar;
		public double matthewsCorrelationCoefficientVar;
		public double classnormalizedCorrectVar;
		public double classnormalizedErrorVar;
		public double unnormalizedCorrectVar;
		public double unnormalizedErrorVar;
		public double accuracyVar;

	}

	private SinkTest reporter;


	@Override
	public float evaluate(Vector targetsTestTrue, Vector targetsTestPredicted) {
		int a = targetsTestTrue.size();
		int b = targetsTestPredicted.size();
		Assert.assertTrue(a == b, "vectors have equal length (" + a + "!=" + b + ")");

		int same = 0;

		for (int i = b - 1; i >= 0; i--) {
			if (Math.abs(targetsTestPredicted.get(i)-targetsTestTrue.get(i))<0.00001) {
				same ++;
			}
		}
		return ((float)same)/b;
	}

	@Override
	public float compare(float score1, float score2) {
		return score1 > score2 ? +1 : score2 > score1 ? -1 : 0;
	}

	public float evaluate(IntVector targetsTestTrue, IntVector targetsTestPredicted) {
		int a = targetsTestTrue.size();
		int b = targetsTestPredicted.size();
		Assert.assertTrue(a == b, "vectors have equal length (" + a + "!=" + b + ")");

		Output reporterTarget = reporter.getTarget();

		int maxtrue = IntVectors.max(targetsTestTrue);
		int maxpred = IntVectors.max(targetsTestPredicted);
		int mintrue = IntVectors.min(targetsTestTrue);
		int minpred = IntVectors.min(targetsTestPredicted);

		int calcOffset = 0;
		if (mintrue < 0 || minpred < 0) {
			calcOffset = -1 * Math.min(minpred, mintrue);
		}

		int maxIdx = Math.max(maxpred+calcOffset+1, maxtrue+calcOffset+1);
		//		maxIdx++; // sums per row / col

		int[][] counts = new int[maxIdx][maxIdx];
		for (int i = 0; i < a; i++) {
			int actual = targetsTestTrue.get(i);
			int predicted = targetsTestPredicted.get(i);
			counts[predicted+calcOffset][actual+calcOffset]++;
		}

		printStatistics(reporterTarget, counts);

		int same = 0;

		for (int i = b - 1; i >= 0; i--) {
			if (Math.abs(targetsTestPredicted.get(i)-targetsTestTrue.get(i))<0.00001) {
				same ++;
			}
		}
		return ((float)same)/b;
	}

	/**
	 * @param t where to log / print results to
	 * @param raw_counts a 2-d array class confusion matrix
	 */
	public static void printStatistics(Output t, int[][] raw_counts) {
		computeAndprintStatistics(t, raw_counts, null);
	}

	/**
	 * @param t where to log / print results to
	 * @param raw_counts a 2-d array class confusion matrix
	 * @param classname optionally includes the class name on the output
	 */
	public static void printStatistics(Output t, NominalPerformanceStatistics statistics, String[] classlabels) {


		t.message("Accuracy: " + statistics.accuracy);
		if (classlabels != null)
			t.message("Class labels: " + Arrays.toString(classlabels));
		t.message("Precision per class: " + Arrays.toString(statistics.precisionPerClass));
		t.message("UnnormalizedError: " + statistics.unnormalizedError);
		t.message("UnnormalizedCorrect: " + statistics.unnormalizedCorrect);
		t.message("Errors per class: " + Arrays.toString(statistics.errorsPerClass));
		t.message("ClassnormalizedError: " + statistics.classnormalizedError);
		t.message("ClassnormalizedCorrect: " + statistics.classnormalizedCorrect);
		t.message("Correct per class: " + Arrays.toString(statistics.precisionPerClass));

		t.message("Recall per class: " + Arrays.toString(statistics.recallPerClass));
		t.message("F-Measure per class: " + Arrays.toString(statistics.fmeasurePerClass));


		if (statistics.binaryProblem) {
			// binary problem!

			t.message("MatthewsCorrelationCoefficient: " + statistics.matthewsCorrelationCoefficient);
			t.message("CohensKappa: " + statistics.cohensKappa);
		}

	}
	/**
	 * @param t where to log / print results to
	 * @param raw_counts a 2-d array class confusion matrix
	 * @param classname optionally includes the class name on the output
	 */
	public static void printStatistics(Output t, AggregatedNominalPerformanceStatistics statistics, String[] classlabels) {

		//		NominalPerformanceStatistics statistics = computeStatistics(raw_counts);

		t.message("Accuracy: " + statistics.accuracy + " (" + statistics.accuracyVar + ")");
		if (classlabels != null)
			t.message("Class labels: " + Arrays.toString(classlabels));
		t.message("Precision per class: " + Arrays.toString(statistics.precisionPerClass));
		t.message("UnnormalizedError: " + statistics.unnormalizedError + " (" + statistics.unnormalizedErrorVar + ")");
		t.message("UnnormalizedCorrect: " + statistics.unnormalizedCorrect + " (" + statistics.unnormalizedCorrectVar + ")");
		t.message("Errors per class: " + Arrays.toString(statistics.errorsPerClass));
		t.message("ClassnormalizedError: " + statistics.classnormalizedError + " (" + statistics.classnormalizedErrorVar + ")");
		t.message("ClassnormalizedCorrect: " + statistics.classnormalizedCorrect + " (" + statistics.classnormalizedCorrectVar + ")");
		t.message("Correct per class: " + Arrays.toString(statistics.precisionPerClass));

		t.message("Recall per class: " + Arrays.toString(statistics.recallPerClass));
		t.message("F-Measure per class: " + Arrays.toString(statistics.fmeasurePerClass));


		if (statistics.binaryProblem) {
			// binary problem!

			t.message("MatthewsCorrelationCoefficient: " + statistics.matthewsCorrelationCoefficient + " (" + statistics.matthewsCorrelationCoefficientVar + ")");
			t.message("CohensKappa: " + statistics.cohensKappa + " (" + statistics.cohensKappaVar + ")");
		}

	}


	/**
	 * @param t where to log / print results to
	 * @param raw_counts a 2-d array class confusion matrix
	 * @param classname optionally includes the class name on the output
	 */
	public static void computeAndprintStatistics(Output t, int[][] raw_counts, String[] classlabels) {

		NominalPerformanceStatistics statistics = computeStatistics(raw_counts);

		printStatistics(t, statistics, classlabels);

		//		t.message("Accuracy: " + statistics.accuracy);
		//		if (classlabels != null)
		//			t.message("Class labels: " + Arrays.toString(classlabels));
		//		t.message("Precision per class: " + Arrays.toString(statistics.precisionPerClass));
		//		t.message("UnnormalizedError: " + statistics.unnormalizedError);
		//		t.message("UnnormalizedCorrect: " + statistics.unnormalizedCorrect);
		//		t.message("Errors per class: " + Arrays.toString(statistics.errorsPerClass));
		//		t.message("ClassnormalizedError: " + statistics.classnormalizedError);
		//		t.message("ClassnormalizedCorrect: " + statistics.classnormalizedCorrect);
		//		t.message("Correct per class: " + Arrays.toString(statistics.precisionPerClass));
		//
		//		t.message("Recall per class: " + Arrays.toString(statistics.recallPerClass));
		//		t.message("F-Measure per class: " + Arrays.toString(statistics.fmeasurePerClass));
		//
		//
		//		if (statistics.binaryProblem) {
		//			// binary problem!
		//
		//			t.message("MatthewsCorrelationCoefficient: " + statistics.matthewsCorrelationCoefficient);
		//			t.message("CohensKappa: " + statistics.cohensKappa);
		//		}

	}


	/**
	 * @param t where to log / print results to
	 * @param raw_counts a 2-d array class confusion matrix
	 * @param classname optionally includes the class name on the output
	 */
	public static NominalPerformanceStatistics computeStatistics(int[][] raw_counts) {

		NominalPerformanceStatistics  ret = new NominalPerformanceStatistics();

		int maxIdx = raw_counts.length+1;

		int[][] counts = new int[maxIdx][maxIdx];

		// copy content
		for (int i = 0; i < maxIdx-1; i++) {
			for (int j = 0; j < maxIdx-1; j++) {
				counts[i][j] += raw_counts[i][j];
			}
		}

		// row / col sums
		for (int i = 0; i < maxIdx-1; i++) {
			for (int j = 0; j < maxIdx-1; j++) {
				counts[i][maxIdx-1] += counts[i][j];
				counts[maxIdx-1][j] += counts[i][j];
			}
		}
		counts[maxIdx-1][maxIdx-1]=(int) IntVectors.sum(Matrices.col(DefaultIntMatrix.createMatrix(counts), maxIdx-1));

		int accuracySum =0;
		for (int i = 0; i < maxIdx-1; i++)
			accuracySum += counts[i][i]; // count diagonal
		double accuracy = (float)accuracySum / (float)counts[maxIdx-1][maxIdx-1];
		ret.accuracy = accuracy;

		double[] precisionPerClass = new double[maxIdx-1];
		for (int i = 0; i < maxIdx-1; i++) {
			if (counts[i][maxIdx-1]==0) continue;
			precisionPerClass[i] = (double)counts[i][i] / (double)counts[i][maxIdx-1];

		}
		ret.precisionPerClass = precisionPerClass;

		// uerrors:
		int uerrorsCnt=0;
		for (int i = 0; i < maxIdx-1; i++) {
			for (int j = 0; j < maxIdx-1; j++) {
				if (counts[i][maxIdx-1]==0) continue;
				if (i==j) continue;
				uerrorsCnt += counts[i][j];
			}
		}
		double unnormalizedError = (double)uerrorsCnt / (double)counts[maxIdx-1][maxIdx-1];
		ret.unnormalizedError = unnormalizedError;
		ret.unnormalizedCorrect = 1-unnormalizedError;

		double[] errorsPerClass = new double[maxIdx-1];
		for (int i = 0; i < maxIdx-1; i++) {
			if (counts[i][maxIdx-1]==0) continue;
			errorsPerClass[i] = 1 - (double)counts[i][i] / (double)counts[i][maxIdx-1];
		}
		ret.errorsPerClass = errorsPerClass;
		// eblearn test correct aka. precision

		double classnormalizedError = avg(errorsPerClass);
		ret.classnormalizedError = classnormalizedError;
		ret.classnormalizedCorrect = 1-classnormalizedError;

		double[] correctPerClass = precisionPerClass;


		double[] recallPerClass = new double[maxIdx-1];
		for (int i = 0; i < maxIdx-1; i++) {
			if (counts[maxIdx-1][i]==0) continue;
			recallPerClass[i] =   (double)counts[i][i] / (double)counts[maxIdx-1][i];
		}
		ret.recallPerClass = recallPerClass;


		double[] fmeasurePerClass = new double[maxIdx-1];
		for (int i = 0; i < maxIdx-1; i++) {
			if (precisionPerClass[i]+recallPerClass[i]==0) continue;
			fmeasurePerClass[i] =   2*(precisionPerClass[i]*recallPerClass[i])/(precisionPerClass[i]+recallPerClass[i]);
		}
		ret.fmeasurePerClass = fmeasurePerClass;


		if (maxIdx == 3) {
			// binary problem!
			ret.binaryProblem = true;

			double tp=counts[0][0];
			double tn=counts[1][1];
			double fn=counts[0][1];
			double fp=counts[1][0];

			//			Matthews correlation coefficient
			//			http://en.wikipedia.org/wiki/Matthews_correlation_coefficient
			double denom=(Math.sqrt((tp+fp)*(tp+fn)*(tn+fp)*(tn+fn)));
			if (tp+fp == 0) denom=1;
			if (tp+fn == 0) denom=1;
			if (tn+fp == 0) denom=1;
			if (tn+fn == 0) denom=1;

			double mcc = (tp*tn-fp*fn)/denom;
			ret.matthewsCorrelationCoefficient = mcc;

			// Cohen's Kappa
			// http://en.wikipedia.org/wiki/Cohen%27s_kappa

			double pr_a = (tp+tn)/counts[2][2];
			double a_yes = counts[0][2] / counts[2][2];
			double a_no = counts[1][2] / counts[2][2];
			double b_yes = counts[2][0] / counts[2][2];
			double b_no = counts[2][1] / counts[2][2];

			double pr_e = a_yes * b_yes + a_no*b_no;

			double kappa = (pr_a-pr_e)/(1-pr_e);
			ret.cohensKappa = kappa;
		}

		return ret;
	}


	private static double avg(double[] errorsPerClass) {
		double ret = 0;
		for (double d : errorsPerClass)
			ret += d;
		return ret/errorsPerClass.length;
	}

	private static double sum(double[] errorsPerClass) {

		double ret = 0;
		for (double d : errorsPerClass)
			ret += d;
		return ret;
	}

	public SinkTest getReporter() {
		return reporter;
	}

	public void setReporter(SinkTest reporter) {
		this.reporter = reporter;
	}

}
