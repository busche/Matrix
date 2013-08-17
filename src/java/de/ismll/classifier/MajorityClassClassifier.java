/**
 * MajorityClassClassifier.java
 * 
 * history: 2009/04/02 1.0  LST created.
 */

package de.ismll.classifier;

import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultVector;


/**
 * A simplistic classifier.
 * 
 * TODO: generalize for multiclass.
 * 
 * @author Lars Schmidt-Thieme
 * @version 1.0
 */
public class MajorityClassClassifier implements BinaryClassifier {

	/**
	 * Get the actual vector of hyperparameters.
	 */
	public Vector getHyperparameters() {
		return new DefaultVector(0);
	}

	/**
	 * Set the vector of hyperparameters.
	 */
	public void setHyperparameters(Vector hyperparameters) {}

	/**
	 * Learn a binary classifier: predictors --> targets (0 or 1)
	 * on all training examples (i.e., having split 0),
	 * apply it to all test examples (having split 1) and
	 * return the probability (or a score).
	 */
	public Vector classifyBinary(Matrix predictorsTrain, Matrix predictorsTest, Vector targetsTrain) {
		float numClasses = Vectors.max(targetsTrain) + 1;
		if (numClasses != 2) {
			System.err.println("ERROR: classifyBinary with more than 2 classes: " + numClasses);
			System.exit(-1);
		}
		Vector targetsTest = new DefaultVector(predictorsTest.getNumRows());
		float classProb = Vectors.sum(targetsTrain) * 1.0f / targetsTrain.size();
		Vectors.set(targetsTest, classProb);
		return targetsTest;
	}



}