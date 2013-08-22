/**
 * MajorityClassClassifier.java
 * 
 * history: 2009/04/02 1.0  LST created.
 */

package de.ismll.classifier;

import de.ismll.table.IntVector;
import de.ismll.table.IntVectors;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.impl.DefaultIntVector;

/**
 * A simplistic classifier.
 * 
 * TODO: generalize for multiclass.
 * 
 * @author Lars Schmidt-Thieme
 * @version 1.0
 */
public class MyMajorityClassClassifier implements INominalClassifier {

	public INominalClassifier copy() {
		MyMajorityClassClassifier ret = new MyMajorityClassClassifier();
		ret.setHyperparameters(getHyperparameters());
		return ret;
	}

	private Vector hyperparameters;

	public Vector getHyperparameters() {
		return hyperparameters;
	}

	public void setHyperparameters(Vector hyperparameters) {
		this.hyperparameters = hyperparameters;
	}

	private int majorityClassIndex;
	private int[] probabilityDistribution;

	@Override
	public void train(Matrix predictorsTrain, IntVector targetsTrain) {
		int numClasses = IntVectors.max(targetsTrain) + 1;
		probabilityDistribution = new int[numClasses];
		for (int i = 0; i < targetsTrain.size(); i++)
			probabilityDistribution[targetsTrain.get(i)]++;
		int maxPos = 0;
		int maxNumber = -1;
		for (int i = 0; i < probabilityDistribution.length; i++)
			if (maxNumber < probabilityDistribution[i]) {
				maxNumber = probabilityDistribution[i];
				maxPos = i;
			}
		this.majorityClassIndex = maxPos;
	}

	@Override
	public IntVector predict(Matrix predictorsTest) {
		DefaultIntVector ret = new DefaultIntVector(predictorsTest.getNumRows());
		for (int i = 0; i < ret.size(); i++)
			ret.set(i, majorityClassIndex);

		return ret;
	}

	@Override
	public int predict(Vector in) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

}