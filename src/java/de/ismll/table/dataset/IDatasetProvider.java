package de.ismll.table.dataset;

import de.ismll.table.Matrix;

public interface IDatasetProvider {


	public Matrix getTrainingData();

	public Matrix getTestData();

}
