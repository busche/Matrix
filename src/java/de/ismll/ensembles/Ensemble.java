package de.ismll.ensembles;

import de.ismll.table.IntVector;
import de.ismll.table.Matrix;

public interface Ensemble {

	public IntVector ensemble(Matrix train, IntVector train_labels, Matrix test) throws EnsembleException;
}
