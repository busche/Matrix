package de.ismll.table.dataset;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.table.IntVector;
import de.ismll.table.Matrix;

public class AbstractPredictOnlyDatasetProvider implements CategoricalDatasetProvider{


	protected Matrix trainingData;
	protected Matrix testData;
	protected IntVector testLabels;
	protected IntVector trainingLabels;
	protected Logger log = LogManager.getLogger(getClass());

	@Override
	public Matrix getTrainingData() {
		return trainingData;
	}

	@Override
	public Matrix getTestData() {
		return testData;
	}

	@Override
	public IntVector getTestLabels() {
		return testLabels;
	}

	@Override
	public IntVector getTrainingLabels() {
		return trainingLabels;
	}

	public AbstractPredictOnlyDatasetProvider() {
		super();
	}


}