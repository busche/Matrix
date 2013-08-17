package de.ismll.table.dataset;

import de.ismll.table.IntVector;

public interface CategoricalDatasetProvider extends DatasetProvider{

	public IntVector getTestLabels();

	public IntVector getTrainingLabels();

}
