package de.ismll.table.dataset;

import de.ismll.table.Matrix;

public interface DatasetProvider {


	public Matrix getTrainingData();

	public Matrix getTestData();

}
