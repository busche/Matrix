package de.ismll.ensembles;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.classifier.NominalClassifier;
import de.ismll.evaluation.Accuracy;
import de.ismll.sampling.StratifiedSample;
import de.ismll.table.IntMatrix;
import de.ismll.table.IntVector;
import de.ismll.table.Matrix;
import de.ismll.table.impl.DefaultIntVector;
import de.ismll.table.projections.ColumnSubsetIntVectorView;
import de.ismll.table.projections.ColumnUnionIntMatrixView;
import de.ismll.table.projections.ColumnUnionMatrixView;
import de.ismll.table.projections.IntMatrixView;
import de.ismll.table.projections.IntVectorAsIntMatrixView;
import de.ismll.table.projections.MatrixView;
import de.ismll.table.projections.RowSubsetIntMatrix;
import de.ismll.table.projections.RowSubsetIntVector;
import de.ismll.table.projections.RowSubsetMatrixView;

public class Boosting implements Ensemble {

	private Logger logger = LogManager.getLogger(getClass());

	private int numRuns=10000;

	private NominalClassifier baseClassifier;

	private boolean reportAccuracy=true;

	@Override
	public IntVector ensemble(Matrix train, IntVector train_labels, Matrix test)
			throws EnsembleException {
		logger.info("Ensembling called with " + train.getNumRows() + " training instances, " + test.getNumRows() + " test instances.");

		float ratio1=0.3f;

		StratifiedSample sample1 = new StratifiedSample();
		sample1.setNumRuns(getNumRuns());
		sample1.setSeed(0);
		sample1.setColumns(new int[] {train.getNumColumns()}); // intentionally! *would* cause AIOOB-Exception, but works, as labels are merged afterwards.
		RowSubsetMatrixView train1 = sample1.getStratified(new ColumnUnionMatrixView(new Matrix[] {train, new MatrixView(new IntMatrixView(train_labels))}), ratio1);
		/*
		 * train classifier 1:
		 */
		DefaultIntVector labels1 = new DefaultIntVector(new RowSubsetIntVector (train_labels, train1.getIndex()));
		//		RowSubsetIntVector testlabels1 = RowSubsetIntVector.create(train_labels, train1.getIndex());
		Matrix trainData1 = new RowSubsetMatrixView(train, train1.getIndex());
		NominalClassifier classifier1;
		try {
			classifier1 = getBaseClassifier().copy();
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
		logger.debug("Training part-1 classifier on " + trainData1.getNumRows() + " instances.");
		try {
			classifier1.train(trainData1, labels1);
		} catch (Exception e) {
			throw new EnsembleException(e);
		}

		Matrix allTrainWithoutPart1 = RowSubsetMatrixView.create(train, train1.getIndex());

		/*
		 * "randomly select instances from the remaining part such that half of
		 * them are correctly classified by the first classifier."
		 * 
		 * -> how to determine them???
		 * 
		 * First implementation approach: take 40% of the remaining instances:
		 * 
		 * 0.30 + (1.0-0.3)*0.4 -> 0.3+0.28= 0.58 used in total.
		 */
		float ratio2 = 0.4f;
		RowSubsetIntMatrix trainLabels1 = RowSubsetIntMatrix.create(new IntMatrixView(train_labels), train1.getIndex());
		StratifiedSample sample2 = new StratifiedSample();
		sample2.setNumRuns(getNumRuns());
		sample2.setSeed(0);
		sample2.setColumns(new int[] {train.getNumColumns()}); // intentionally! *would* cause AIOOB-Exception, but works, as labels are merged afterwards.
		RowSubsetMatrixView train2 = sample2.getStratified(
				new ColumnUnionMatrixView(
						new Matrix[] {
								allTrainWithoutPart1,
								new MatrixView(trainLabels1)
						}
						),ratio2);
		/*
		 * train classifier 2:
		 */
		IntVector labels2 = new RowSubsetIntVector(new ColumnSubsetIntVectorView(trainLabels1, 0), train2.getIndex());
		Matrix trainData2 = new RowSubsetMatrixView(allTrainWithoutPart1, train2.getIndex());

		NominalClassifier classifier2;
		try {
			classifier2 = getBaseClassifier().copy();
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			return null;
		}
		logger.debug("Training part-2 classifier on " + trainData2.getNumRows() + " instances.");
		try {
			classifier2.train(trainData2, labels2);
		} catch (Exception e) {
			throw new EnsembleException(e);
		}

		RowSubsetMatrixView holdoutTestMatrix = RowSubsetMatrixView.create(allTrainWithoutPart1, train2.getIndex());
		RowSubsetIntVector internalTestLabels = RowSubsetIntVector.create(new ColumnSubsetIntVectorView(trainLabels1, 0), train2.getIndex());
		/*
		 * determine incorrectly classified samples:
		 */
		IntVector predictions1;
		IntVector predictions2;
		try {
			predictions1 = classifier1.predict(holdoutTestMatrix);
			predictions2 = classifier2.predict(holdoutTestMatrix);
		} catch (Exception e) {
			throw new EnsembleException(e);
		}

		if (reportAccuracy) {
			Accuracy a = new Accuracy();
			float accuracy1 = a.evaluate(internalTestLabels, predictions1);
			logger.info("Accuracy for Classifier 1: " + accuracy1);
			float accuracy2 = a.evaluate(internalTestLabels, predictions2);
			logger.info("Accuracy for Classifier 2: " + accuracy2);

		}

		List<Integer> incorrectPointers = new ArrayList<Integer>();
		for (int i = 0; i < holdoutTestMatrix.getNumRows(); i++) {
			// classify with classifier1:
			float predicted1=predictions1.get(i);
			// classify with classifier1:
			float predicted2=predictions2.get(i);

			if (Math.abs(predicted1-predicted2)<0.001)
				incorrectPointers.add(Integer.valueOf(i));
		}

		DefaultIntVector incorrectVector = new DefaultIntVector(incorrectPointers.size());
		for (int i = 0; i < incorrectPointers.size(); i++)
			incorrectVector.data[i] = incorrectPointers.get(i).intValue();

		RowSubsetMatrixView incorrectlyClassifiedInstances = new RowSubsetMatrixView(holdoutTestMatrix, incorrectVector);
		/*
		 * train 3rd classifier on incorrectly classified instances:
		 */
		IntVector labels3 = new RowSubsetIntVector(new ColumnSubsetIntVectorView(trainLabels1, 0), incorrectlyClassifiedInstances.getIndex());

		NominalClassifier classifier3;
		try {
			classifier3 = getBaseClassifier().copy();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		logger.debug("Training part-3 classifier on " + incorrectlyClassifiedInstances.getNumRows() + " instances.");
		try {
			classifier3.train(incorrectlyClassifiedInstances, labels3);
		} catch (Exception e) {
			throw new EnsembleException(e);
		}

		logger.debug("Finished training of base classifiers ... Performing predictions on test data...");

		IntVector testPredictions1;
		IntVector testPredictions2;
		IntVector testPredictions3;
		try {
			testPredictions1 = classifier1.predict(test);
			testPredictions2 = classifier2.predict(test);
			testPredictions3 = classifier3.predict(test);
		} catch (Exception e) {
			throw new EnsembleException(e);
		}

		logger.debug("Using majority voting to infer final prediction.");

		ColumnUnionIntMatrixView v = new ColumnUnionIntMatrixView(new IntMatrix[] {
				new IntVectorAsIntMatrixView(testPredictions1),
				new IntVectorAsIntMatrixView(testPredictions2),
				new IntVectorAsIntMatrixView(testPredictions3)
		});
		/*
		 * make predictions using majority voting:
		 */
		MajorityVoteEnsemble majority = new MajorityVoteEnsemble();
		IntVector ensemble = majority.ensemble(null, null, new MatrixView(v));

		return ensemble;
	}

	public void setBaseClassifier(NominalClassifier baseClassifier) {
		this.baseClassifier = baseClassifier;
	}

	public NominalClassifier getBaseClassifier() {
		return baseClassifier;
	}

	public void setNumRuns(int numRuns) {
		this.numRuns = numRuns;
	}

	public int getNumRuns() {
		return numRuns;
	}

}
