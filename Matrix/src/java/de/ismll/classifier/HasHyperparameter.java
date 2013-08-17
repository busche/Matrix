package de.ismll.classifier;

import de.ismll.table.Vector;

public interface HasHyperparameter<T> {
	/**
	 * Get the actual vector of hyperparameters.
	 */
	public Vector getHyperparameters();

	/**
	 * Set the vector of hyperparameters.
	 */
	public void setHyperparameters(Vector hyperparameters);

	public T copy() throws Exception;
}
