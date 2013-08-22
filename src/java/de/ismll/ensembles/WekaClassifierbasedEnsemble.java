package de.ismll.ensembles;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import weka.classifiers.Classifier;
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
import de.ismll.table.io.weka.AllNominalEncoder;
import de.ismll.table.projections.ColumnSubsetVectorView;
import de.ismll.table.projections.ColumnUnionMatrixView;
import de.ismll.table.projections.IntMatrixView;
import de.ismll.table.projections.MatrixView;

public abstract class WekaClassifierbasedEnsemble implements Ensemble{

	protected Logger logger = LogManager.getLogger(getClass());

	private boolean evaluateOnTrain = true;

	protected WekaClassifierbasedEnsemble() {
		super();
		internal_measure = new Accuracy();
	}

	/**
	 * TODO: externalize, replace/substitute type of train_data to Matrix ...
	 * 
	 * @param matrixView
	 * @param train_data
	 * @param smo
	 * @throws Exception
	 */
	private void evaluate(MatrixView matrixView, Instances train_data, Classifier smo)
			throws Exception {
		Vector trainResults = new DefaultVector(train_data.numInstances());
		for (int i = 0; i < trainResults.size(); i++) {
			float prediction = (float) smo.classifyInstance(train_data.instance(i));

			trainResults.set(i,prediction);
		}


		ColumnSubsetVectorView v = new ColumnSubsetVectorView(matrixView, 0);
		float evaluate = internal_measure.evaluate(v, trainResults);
		System.out.println(internal_measure.getClass().getName() +  " on train data (WARNING: trained on this, should be 100%!): " + evaluate);
	}

	private IEvaluator internal_measure;


	@Override
	public IntVector ensemble(Matrix train, IntVector train_labels, Matrix test)
			throws EnsembleException {
		MatrixView matrixView = new MatrixView(new IntMatrixView(train_labels));


		DefaultIntVector predictions = new DefaultIntVector(test.getNumRows());
		DefaultMatrix unknown = new DefaultMatrix(test.getNumRows(), 1);
		Matrices.set(unknown, -1);

		AllNominalEncoder enc = new AllNominalEncoder();

		Instances wtrain = Matrices.wekaInstances(new ColumnUnionMatrixView(
				new Matrix[] { train, matrixView }), enc);

		Instances wtest = Matrices.wekaInstances(new ColumnUnionMatrixView(
				new Matrix[] { test, unknown }), enc);

		wtrain.setClassIndex(wtrain.numAttributes()-1);
		wtest.setClassIndex(wtrain.numAttributes()-1);

		Classifier c = buildClassifier(wtrain);


		if (evaluateOnTrain) {
			try {
				evaluate(matrixView, wtrain, c);
			} catch (Exception e) {
				logger.error("Error evaluating performance on train data: " + e.getMessage(), e);
			}
		}

		try {

			for (int i = 0; i < wtest.numInstances(); i++) {
				float prediction = (float) c.classifyInstance(wtest.instance(i));

				predictions.set(i, (int) prediction);
			}
		} catch (Exception e) {
			throw new EnsembleException(e);
		}

		return predictions;
	}

	protected abstract Classifier buildClassifier(Instances wtrain) throws EnsembleException;

	public void setEvaluateOnTrain(boolean evaluateOnTrain) {
		this.evaluateOnTrain = evaluateOnTrain;
	}

	public boolean isEvaluateOnTrain() {
		return evaluateOnTrain;
	}

}
