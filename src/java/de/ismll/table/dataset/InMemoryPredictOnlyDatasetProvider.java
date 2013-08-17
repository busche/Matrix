package de.ismll.table.dataset;

import de.ismll.table.IntVector;
import de.ismll.table.Matrix;

public class InMemoryPredictOnlyDatasetProvider extends AbstractPredictOnlyDatasetProvider{

	public InMemoryPredictOnlyDatasetProvider(
			Matrix trainData,
			IntVector trainLabels,
			Matrix testData,
			IntVector testLabels
			) {
		super();
		super.trainingData=trainData;
		super.trainingLabels=trainLabels;
		super.testData=testData;
		super.testLabels=testLabels;


	}
}
