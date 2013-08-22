package de.ismll.classifier;

import de.ismll.sampling.StratifiedSample;
import de.ismll.table.IntVector;
import de.ismll.table.IntVectors;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.impl.DefaultIntVector;
import de.ismll.table.projections.ColumnSubsetMatrixView;
import de.ismll.table.projections.ColumnUnionMatrixView;
import de.ismll.table.projections.IntMatrixView;
import de.ismll.table.projections.MatrixView;
import de.ismll.table.projections.RowSubsetIntVector;
import de.ismll.table.projections.RowSubsetMatrixView;
import de.ismll.utilities.Assert;

public class MyUtilityClassifier implements INominalClassifier {

	private INominalClassifier baseClassifier;

	private Vector hyperparameters;

	public MyUtilityClassifier copy() {
		MyUtilityClassifier ret = new MyUtilityClassifier();
		ret.setHyperparameters(getHyperparameters());
		return ret;
	}

	@Override
	public Vector getHyperparameters() {
		return hyperparameters;
	}

	@Override
	public void setHyperparameters(Vector hyperparameters) {
		this.hyperparameters = hyperparameters;
	}

	private int weakClassIdx=-1;

	@Override
	public void train(Matrix predictorsTrain, IntVector targetsTrain)
			throws Exception {
		Assert.notNull(baseClassifier, "baseClassifier");

		if (weakClassIdx<0) {
			Vector oldHyperparameters = baseClassifier.getHyperparameters();

			/*
			 * train base classifier on 50 % of the original data
			 */
			StratifiedSample ss = new StratifiedSample();
			ss.setColumns(new int[] {predictorsTrain.getNumColumns()});
			Matrix union = new ColumnUnionMatrixView(new Matrix[] {predictorsTrain, new MatrixView(new IntMatrixView(targetsTrain))});
			RowSubsetMatrixView stratified = ss.getStratified(union, 0.5f);
			ColumnSubsetMatrixView traindata = ColumnSubsetMatrixView.create(stratified, new int[] {stratified.getNumColumns()-1});
			RowSubsetIntVector trainlabels = new RowSubsetIntVector(targetsTrain, stratified.getIndex());

			baseClassifier.train(traindata, trainlabels);


			RowSubsetMatrixView holdout = RowSubsetMatrixView.create(stratified.getMatrix(), stratified.getIndex());
			// remove last column (class) for predictions.
			ColumnSubsetMatrixView holdoutdata = ColumnSubsetMatrixView.create(holdout, new int[] {holdout.getNumColumns()-1});

			/*
			 * predict on the remaining data
			 */
			IntVector predict = baseClassifier.predict(holdoutdata);
			IntVector trueValues = new RowSubsetIntVector(targetsTrain, holdout.getIndex());
			Assert.assertTrue(predict.size()==trueValues.size(), "debugging - consistency check");

			int max = IntVectors.max(trueValues)+1;
			int[] errorCounterOnClasses = new int[max];
			for (int i = 0; i < predict.size(); i++) {
				int p = predict.get(i);
				int t = trueValues.get(i);
				if (p != t)
					errorCounterOnClasses[p]++;
			}

			int maxIdx=-1;
			int maxAmount=-1;
			for (int i = 0; i < errorCounterOnClasses.length; i++) {
				if (errorCounterOnClasses[i] > maxAmount) {
					maxAmount=errorCounterOnClasses[i];
					maxIdx=i;
				}
			}
			this.weakClassIdx=maxIdx;

			baseClassifier = baseClassifier.copy();
			baseClassifier.setHyperparameters(oldHyperparameters);
		}

		// convert problem into 1-vs-rest problem against class maxIdx.

		DefaultIntVector newTrainLabels = new DefaultIntVector(targetsTrain.size());
		for (int i = 0; i < targetsTrain.size(); i++)
			if (targetsTrain.get(i) == weakClassIdx)
				newTrainLabels.data[i] = 1;
			else
				newTrainLabels.data[i] = 0;

		baseClassifier.train(predictorsTrain, newTrainLabels);
	}

	@Override
	public IntVector predict(Matrix predictorsTest) throws Exception {
		return baseClassifier.predict(predictorsTest);
	}

	public void setBaseClassifier(INominalClassifier baseClassifier) {
		this.baseClassifier = baseClassifier;
	}

	public INominalClassifier getBaseClassifier() {
		return baseClassifier;
	}

	public void setWeakClassIdx(int weakClassIdx) {
		this.weakClassIdx = weakClassIdx;
	}

	public int getWeakClassIdx() {
		return weakClassIdx;
	}

	@Override
	public int predict(Vector in) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

}
