package de.ismll.classifier;

import de.ismll.table.IntVector;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;

/**
 * 
 * typical usage:
 * 
 * train(...)
 * serializeHyperparameters with getHyperparameters
 * restore hyperparameters
 * predict(...)
 * 
 * @author John
 *
 */
public interface NominalClassifier extends HasHyperparameter<NominalClassifier> {

	public void train(Matrix predictorsTrain, IntVector targetsTrain) throws Exception;

	public IntVector predict(Matrix predictorsTest) throws Exception;

	public int predict(Vector in) throws Exception;

}
