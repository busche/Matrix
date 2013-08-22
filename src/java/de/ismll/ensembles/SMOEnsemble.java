package de.ismll.ensembles;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.core.Instance;
import weka.core.Instances;
import de.ismll.evaluation.Accuracy;
import de.ismll.evaluation.IEvaluator;
import de.ismll.table.IntVector;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.impl.DefaultIntVector;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultVector;
import de.ismll.table.projections.ColumnSubsetVectorView;
import de.ismll.table.projections.ColumnUnionMatrixView;
import de.ismll.table.projections.IntMatrixView;
import de.ismll.table.projections.MatrixView;

public class SMOEnsemble implements Ensemble{

	boolean search_best_smo_hyperparam = true;
	private int c;
	private int e;
	private int threads=2;

	ExecutorService p = Executors.newFixedThreadPool(threads);
	private boolean evaluateOnTrain = true;
	private IEvaluator internal_measure;

	public SMOEnsemble() {
		internal_measure = new Accuracy();
	}

	public Matrix predictSMOGreedyOpt(Matrix train, Matrix train_labels, Matrix test) throws Exception
	{
		int best_c = 0;
		int best_e = 0;
		String domain = "{0.0,1.0,2.0,3.0,4.0,5.0}";

		DefaultMatrix predictions;
		DefaultMatrix fakeLabels = new DefaultMatrix(test.getNumRows(), 1);

		Matrix train_with_labels     = new ColumnUnionMatrixView(new Matrix[] {train, train_labels});
		Matrix test_with_fake_labels = new ColumnUnionMatrixView(new Matrix[] {test, fakeLabels});


		Instances train_data = Matrices.wekaInstances(train_with_labels, domain);
		train_data.setClassIndex(train_data.numAttributes()-1);

		Instances test_data = Matrices.wekaInstances(test_with_fake_labels, domain);
		test_data.setClassIndex(test_data.numAttributes()-1);

		predictions= new DefaultMatrix(test_with_fake_labels.getNumRows(), 1);

		double rmse_best = Double.MAX_VALUE;

		final Instances train0 = train_data.trainCV(2, 0);
		final Instances train1 = train_data.testCV(2, 0);

		double rmse_c_pos = -1;
		double rmse_c_neg = -1;
		double rmse_e_pos = -1;
		double rmse_e_neg = -1;
		double rmse = -1;


		if (search_best_smo_hyperparam) {
			System.out.println("Building initial model...");
			{
				SMO smo = new SMO();
				smo.setC(Math.pow(2.0,getC()));
				PolyKernel kernel = (PolyKernel) smo.getKernel();
				kernel.setExponent(Math.pow(2.0,getE()));
				smo.setKernel(kernel);
				smo.buildClassifier(train0);

				weka.classifiers.Evaluation eval = new weka.classifiers.Evaluation(train0);
				eval.evaluateModel(smo, train1);
				rmse = eval.incorrect();
			}
			System.out.println("Grid Searching...");


			while (rmse < rmse_best) {

				System.out.println("centered around: c="+getC()+"  e="+getE()+"  "+rmse);

				rmse_best = rmse;
				best_e = getE();
				best_c = getC();


				Callable<Double> crmse_c_neg = null;
				Callable<Double> crmse_c_pos = null;
				Callable<Double> crmse_e_neg = null;
				Callable<Double> crmse_e_pos = null;

				if (rmse_c_neg==-1) {
					crmse_c_neg = new Callable<Double>() {

						@Override
						public Double call() throws Exception {
							Instances in = new Instances(train0);
							Instances te = new Instances(train1);
							SMO smo = new SMO();
							smo.setC(Math.pow(2.0,getC()-1));
							PolyKernel kernel = (PolyKernel) smo.getKernel();
							kernel.setExponent(Math.pow(2.0,getE()));
							smo.setKernel(kernel);
							smo.buildClassifier(in);

							weka.classifiers.Evaluation eval = new weka.classifiers.Evaluation(in);
							eval.evaluateModel(smo, te);
							double incorrect = eval.incorrect();

							System.out.println(" c="+(getC()-1)+"  e="+getE()+"  "+incorrect);
							return Double.valueOf(incorrect);
						}
					};
				}


				if (rmse_c_pos==-1) {
					crmse_c_pos = new Callable<Double>() {

						@Override
						public Double call() throws Exception {
							Instances in = new Instances(train0);
							Instances te = new Instances(train1);
							SMO smo = new SMO();
							smo.setC(Math.pow(2.0,getC()+1));
							PolyKernel kernel = (PolyKernel) smo.getKernel();
							kernel.setExponent(Math.pow(2.0,getE()));
							smo.setKernel(kernel);
							smo.buildClassifier(in);

							weka.classifiers.Evaluation eval = new weka.classifiers.Evaluation(in);
							eval.evaluateModel(smo, te);
							double incorrect = eval.incorrect();

							System.out.println(" c="+(getC()+1)+"  e="+getE()+"  "+incorrect);
							return Double.valueOf(incorrect);
						}
					};
				}


				if (rmse_e_pos==-1) {
					crmse_e_pos = new Callable<Double>() {

						@Override
						public Double call() throws Exception {
							Instances in = new Instances(train0);
							Instances te = new Instances(train1);
							SMO smo = new SMO();
							smo.setC(Math.pow(2.0,getC()));
							PolyKernel kernel = (PolyKernel) smo.getKernel();
							kernel.setExponent(Math.pow(2.0,getE()+1));
							smo.setKernel(kernel);
							smo.buildClassifier(in);

							weka.classifiers.Evaluation eval = new weka.classifiers.Evaluation(in);
							eval.evaluateModel(smo, te);
							double incorrect = eval.incorrect();

							System.out.println(" c="+(getC())+"  e="+(getE()+1)+"  "+incorrect);
							return Double.valueOf(incorrect);
						}
					};
				}


				if (rmse_e_neg==-1) {
					crmse_e_neg = new Callable<Double>() {

						@Override
						public Double call() throws Exception {
							Instances in = new Instances(train0);
							Instances te = new Instances(train1);
							SMO smo = new SMO();
							smo.setC(Math.pow(2.0,getC()));
							PolyKernel kernel = (PolyKernel) smo.getKernel();
							kernel.setExponent(Math.pow(2.0,getE()-1));
							smo.setKernel(kernel);
							smo.buildClassifier(in);

							weka.classifiers.Evaluation eval = new weka.classifiers.Evaluation(in);
							eval.evaluateModel(smo, te);
							double incorrect = eval.incorrect();

							System.out.println(" c="+(getC())+"  e="+(getE()-1)+"  "+incorrect);
							return Double.valueOf(incorrect);
						}
					};

				}

				Future<Double> frmse_c_neg = null;
				Future<Double> frmse_c_pos = null;
				Future<Double> frmse_e_neg = null;
				Future<Double> frmse_e_pos = null;
				if (crmse_c_neg!= null)
					frmse_c_neg = p.submit(crmse_c_neg);
				if (crmse_c_pos!= null)
					frmse_c_pos=p.submit(crmse_c_pos);
				if (crmse_e_neg!= null)
					frmse_e_neg=p.submit(crmse_e_neg);
				if (crmse_e_pos!= null)
					frmse_e_pos=p.submit(crmse_e_pos);

				if (frmse_c_neg!= null)
					rmse_c_neg = frmse_c_neg.get().doubleValue();
				if (frmse_c_pos!=null)
					rmse_c_pos = frmse_c_pos.get().doubleValue();
				if (frmse_e_neg!=null)
					rmse_e_neg = frmse_e_neg.get().doubleValue();
				if(frmse_e_pos!=null)
					rmse_e_pos = frmse_e_pos.get().doubleValue();
				// greedy-search for better e/c

				if ((rmse_c_neg < rmse)
						&& (rmse_c_neg < rmse_c_pos)
						&& (rmse_c_neg < rmse_e_pos)
						&& (rmse_c_neg < rmse_e_neg)) {
					setC(getC() - 1);
					rmse_c_pos = rmse;
					rmse = rmse_c_neg;
					rmse_c_neg = -1;
					rmse_e_neg = -1;
					rmse_e_pos = -1;
				}

				if ((rmse_c_pos < rmse) && (rmse_c_pos < rmse_c_neg)
						&& (rmse_c_pos < rmse_e_pos)
						&& (rmse_c_pos < rmse_e_neg)) {
					setC(getC() + 1);
					rmse_c_neg = rmse;
					rmse = rmse_c_pos;
					rmse_c_pos = -1;
					rmse_e_neg = -1;
					rmse_e_pos = -1;
				}

				if ((rmse_e_neg < rmse) && (rmse_e_neg < rmse_c_neg)
						&& (rmse_e_neg < rmse_c_pos)
						&& (rmse_e_neg < rmse_e_pos)) {
					setE(getE() - 1);
					rmse_e_pos = rmse;
					rmse = rmse_e_neg;
					rmse_c_pos = -1;
					rmse_c_neg = -1;
					rmse_e_neg = -1;
				}

				if ((rmse_e_pos < rmse) && (rmse_e_pos < rmse_c_neg)
						&& (rmse_e_pos < rmse_c_pos)
						&& (rmse_e_pos < rmse_e_neg)) {
					setE(getE() + 1);
					rmse_e_neg = rmse;
					rmse = rmse_e_pos;
					rmse_c_pos = -1;
					rmse_c_neg = -1;
					rmse_e_pos = -1;
				}


			}

		}



		SMO smo = new SMO();
		smo.setC(Math.pow(2.0,best_c));
		PolyKernel kernel = (PolyKernel) smo.getKernel();
		kernel.setExponent(Math.pow(2.0,best_e));
		smo.setKernel(kernel);
		smo.buildClassifier(train_data);

		if (isEvaluateOnTrain() ) {
			evaluate(train_labels, train_data, smo);
		}

		for (int i=0;i<test_data.numInstances();i++) {
			float prediction = (float) smo.classifyInstance(test_data.instance(i));

			predictions.set(i,0,prediction);
		}

		return predictions;
	}

	/**
	 * TODO: externalize, replace/substitute type of train_data to Matrix ...
	 * 
	 * @param train_labels
	 * @param train_data
	 * @param smo
	 * @throws Exception
	 */
	private void evaluate(Matrix train_labels, Instances train_data, Classifier smo)
			throws Exception {
		Vector trainResults = new DefaultVector(train_data.numInstances());
		for (int i = 0; i < trainResults.size(); i++) {
			float prediction = (float) smo.classifyInstance(train_data.instance(i));

			trainResults.set(i,prediction);
		}


		ColumnSubsetVectorView v = new ColumnSubsetVectorView(train_labels, 0);
		float evaluate = internal_measure.evaluate(v, trainResults);
		System.out.println(internal_measure.getClass().getName() +  " on train data (WARNING: trained on this, should be 100%!): " + evaluate);
	}

	public Matrix predictSMOGreedyOpt2(Matrix train, Matrix train_labels, Matrix test, double[] targetDistribution) throws Exception
	{
		Set<String> classes = new TreeSet<String>();
		for (int i = 0; i < train_labels.getNumRows(); i++) {
			classes.add(Float.toString(train.get(i, 0)));
		}

		String[] s = new String[classes.size()];
		classes.toArray(s);
		return predictSMOGreedyOpt2(train, train_labels, test, s, targetDistribution);
	}

	enum Best{
		EP,EM,CP,CM
	}

	public Matrix predictSMOGreedyOpt2(Matrix train, Matrix train_labels, Matrix test, String[] nominals, double[] targetDistribution) throws Exception
	{
		if (nominals.length != targetDistribution.length)
			throw new Exception();
		String domain = "{";
		int j = 0;
		for (String d : nominals) {

			if (j>0)
				domain+=",";
			domain+=d;
			j++;
		}
		domain += "}";

		int best_c = 0;
		int best_e = 0;
		// collect classes

		DefaultMatrix predictions;
		DefaultMatrix fakeLabels = new DefaultMatrix(test.getNumRows(), 1);

		Matrix train_with_labels     = new ColumnUnionMatrixView(new Matrix[] {train, train_labels});
		Matrix test_with_fake_labels = new ColumnUnionMatrixView(new Matrix[] {test, fakeLabels});

		Instances tmp0_data = Matrices.wekaInstances(train_with_labels, domain);
		tmp0_data.setClassIndex(tmp0_data.numAttributes()-1);

		Instances tmp1_data = Matrices.wekaInstances(test_with_fake_labels, domain);
		tmp1_data.setClassIndex(tmp1_data.numAttributes()-1);

		predictions= new DefaultMatrix(test_with_fake_labels.getNumRows(), 1);

		double rmse_best = Double.MAX_VALUE;

		Instances train0 = tmp0_data.trainCV(2, 0);
		Instances train1 = tmp0_data.testCV(2, 0);

		double rmse_c_pos = -1;
		double rmse_c_neg = -1;
		double rmse_e_pos = -1;
		double rmse_e_neg = -1;
		double rmse = -1;



		if (search_best_smo_hyperparam) {

			rmse = measurePerformance(train0, train1, targetDistribution, getC(), getE()-1).get().doubleValue();

			while (rmse < rmse_best) {

				System.out.println("centered around: c="+getC()+"  e="+getE()+"  "+rmse);

				rmse_best = rmse;
				best_e = getE();
				best_c = getC();

				Future<Double> frmse_c_neg = null;
				Future<Double> frmse_c_pos = null;
				Future<Double> frmse_e_neg = null;
				Future<Double> frmse_e_pos = null;

				if (rmse_c_neg==-1) {
					frmse_c_neg = measurePerformance(train0, train1, targetDistribution, getC()-1, getE());
				}

				if (rmse_c_pos==-1) {
					frmse_c_pos = measurePerformance(train0, train1, targetDistribution, getC()+1, getE());
				}

				if (rmse_e_pos==-1) {
					frmse_e_pos = measurePerformance(train0, train1, targetDistribution, getC(), getE()+1);
				}

				if (rmse_e_neg==-1) {
					frmse_e_neg = measurePerformance(train0, train1, targetDistribution, getC(), getE()-1);
				}

				if (frmse_c_neg!= null)
					rmse_c_neg = frmse_c_neg.get().doubleValue();
				if (frmse_c_pos!=null)
					rmse_c_pos = frmse_c_pos.get().doubleValue();
				if (frmse_e_neg!=null)
					rmse_e_neg = frmse_e_neg.get().doubleValue();
				if(frmse_e_pos!=null)
					rmse_e_pos = frmse_e_pos.get().doubleValue();

				System.out.println(" c="+(getC()-1)+"  e="+getE()+"  "+rmse_c_neg);
				System.out.println(" c="+(getC()+1)+"  e="+getE()+"  "+rmse_c_pos);
				System.out.println(" c="+(getC())+"  e="+(getE()-1)+"  "+rmse_e_neg);
				System.out.println(" c="+(getC())+"  e="+(getE()+1)+"  "+rmse_e_pos);

				Best b = null;
				if (rmse_c_neg < rmse) {
					b = Best.CM;
					rmse=rmse_c_neg;
				}
				if (rmse_c_pos < rmse) {
					b = Best.CP;
					rmse=rmse_c_pos;
				}
				if (rmse_e_neg < rmse) {
					b = Best.EM;
					rmse=rmse_e_neg;
				}
				if (rmse_e_pos < rmse) {
					b = Best.EP;
					rmse=rmse_e_pos;
				}

				if (b != null) {
					switch (b) {
					case CM:
						setC(best_c - 1);
						rmse_c_pos = rmse;
						rmse = rmse_c_neg;
						rmse_c_neg = -1;
						rmse_e_neg = -1;
						rmse_e_pos = -1;
						break;
					case CP:
						setC(best_c + 1);
						rmse_c_neg = rmse;
						rmse = rmse_c_pos;
						rmse_c_pos = -1;
						rmse_e_neg = -1;
						rmse_e_pos = -1;
						break;
					case EM:
						setE(best_e - 1);
						rmse_e_pos = rmse;
						rmse = rmse_e_neg;
						rmse_c_pos = -1;
						rmse_c_neg = -1;
						rmse_e_neg = -1;
						break;
					case EP:
						setE(best_e + 1);
						rmse_e_neg = rmse;
						rmse = rmse_e_pos;
						rmse_c_pos = -1;
						rmse_c_neg = -1;
						rmse_e_pos = -1;
						break;
					}
				}
			}
		}

		System.out.println("Building final ensemble.");

		SMO smo = new SMO();
		smo.setC(Math.pow(2.0,best_c));
		PolyKernel kernel = (PolyKernel) smo.getKernel();
		kernel.setExponent(Math.pow(2.0,best_e));
		smo.setKernel(kernel);
		smo.buildClassifier(tmp0_data);

		if (isEvaluateOnTrain() ) {
			evaluate(train_labels, tmp0_data, smo);
		}

		for (int i=0;i<tmp1_data.numInstances();i++) {
			float prediction = (float) smo.classifyInstance(tmp1_data.instance(i));

			predictions.set(i,0,prediction);
		}

		return predictions;
	}
	public static enum Aggregate{
		maxDiff,
		avgDiff,
		incorrect_times_avgDiff,
		incorrect_times_maxDiff

	}
	private Aggregate aggregate;

	private Future<Double> measurePerformance(final Instances train2, final Instances test2, final double[] targetDistribution, final int c, final int e)
			throws Exception {

		Callable<Double> callable = new Callable<Double>() {

			@Override
			public Double call() throws Exception {

				Instances test = new Instances(test2);
				Instances train = new Instances(train2);
				int numTest = test.size();

				SMO smo = new SMO();
				smo.setC(Math.pow(2.0,c));
				PolyKernel kernel = (PolyKernel) smo.getKernel();
				kernel.setExponent(Math.pow(2.0,e-1));
				smo.setKernel(kernel);
				smo.buildClassifier(train);


				double[] predictedDistribution = new double[targetDistribution.length];
				int numIncorrect=0;
				for (Instance i : test) {
					double prediction = smo.classifyInstance(i);
					double trueValue = i.classValue();
					boolean correct = Math.abs(prediction-trueValue)<0.001;
					if (!correct)
						numIncorrect++;
					predictedDistribution[(int) prediction]++;
				}
				// normalize distribution

				double incorrectRatio = (double)numIncorrect/(double) numTest;

				for (int i = 0; i < predictedDistribution.length; i++)
					predictedDistribution[i] /= numTest;

				/*
				 * alternatives:
				 * 
				 * maxDiff, or
				 * avgDiff, or
				 * mult incorrect<>avgDiff
				 * mult incorrect<>maxDiff
				 * 
				 * weight?
				 */
				double maxDifference=0;
				double diffSum=0;
				for (int i = 0; i < predictedDistribution.length; i++) {
					double difference = Math.abs(predictedDistribution[i]-targetDistribution[i]);
					System.out.println("Divergence for class number " + i + " is " + difference);
					maxDifference = Math.max(maxDifference, difference);
					diffSum+=difference;
				}
				diffSum/=predictedDistribution.length;
				System.out.println("Avg distribution distance is " + diffSum);

				switch (getAggregate()) {
				case avgDiff:
					return diffSum;
				case maxDiff:
					return maxDifference;
				case incorrect_times_avgDiff:
					return incorrectRatio*diffSum;
				case incorrect_times_maxDiff:
					return maxDifference * maxDifference;
				}
				return Double.valueOf(incorrectRatio * diffSum);
			}
		};
		return p.submit(callable);
	}

	public synchronized void setC(int c) {
		this.c = c;
	}

	public synchronized int getC() {
		return c;
	}

	public synchronized void setE(int e) {
		this.e = e;
	}

	public synchronized int getE() {
		return e;
	}

	public void close() {
		p.shutdown();
	}

	@Override
	public IntVector ensemble(Matrix train, IntVector train_labels, Matrix test)
			throws EnsembleException {
		try {
			Matrix predictSMOGreedyOpt = predictSMOGreedyOpt(train, new MatrixView(new IntMatrixView(train_labels)), test);
			ColumnSubsetVectorView v = new ColumnSubsetVectorView(predictSMOGreedyOpt, 0);
			return new DefaultIntVector(v);
		} catch (Exception e) {
			throw new EnsembleException(e);
		}
	}

	public void setEvaluateOnTrain(boolean evaluateOnTrain) {
		this.evaluateOnTrain = evaluateOnTrain;
	}

	public boolean isEvaluateOnTrain() {
		return evaluateOnTrain;
	}

	public void setAggregate(Aggregate aggregate) {
		this.aggregate = aggregate;
	}

	public Aggregate getAggregate() {
		return aggregate;
	}


}
