/**
 * BinaryClassifier.java
 * 
 * history: 2009/04/02 1.0  LST created.
 */

package de.ismll.classifier;

import de.ismll.table.Matrix;
import de.ismll.table.Vector;

/**
 * A generic interface for binary classifiers.
 * 
 * @author Lars Schmidt-Thieme
 * @version 1.0
 */
public interface BinaryClassifier {

	/**
	 * Learn a binary classifier: predictors --> targets (0 or 1)
	 * on all training examples
	 * apply it to all test examples and
	 * return the probability of class 1.
	 */
	public Vector classifyBinary(Matrix predictorsTrain, Matrix predictorsTest, Vector targetsTrain);


	/**
	 * Get the actual vector of hyperparameters.
	 */
	public Vector getHyperparameters();

	/**
	 * Set the vector of hyperparameters.
	 */
	public void setHyperparameters(Vector hyperparameters);

}