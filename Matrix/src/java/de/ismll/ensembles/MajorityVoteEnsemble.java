package de.ismll.ensembles;

import java.util.Arrays;

import de.ismll.table.IntMatrix;
import de.ismll.table.IntVector;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.impl.DefaultIntVector;
import de.ismll.table.projections.IntMatrixView;

public class MajorityVoteEnsemble implements Ensemble{

	@Override
	public IntVector ensemble(Matrix train, IntVector train_labels, Matrix t)
			throws EnsembleException {
		IntMatrix test = new IntMatrixView(t);

		int numRows = test.getNumRows();
		int numColumns = test.getNumColumns();
		IntVector ret = new DefaultIntVector(numRows);
		int max = Matrices.max(test);
		int[] counts = new int[max+1];

		for (int i = 0; i < numRows; i++) {
			Arrays.fill(counts, 0);

			for (int j = 0; j < numColumns; j++) {
				counts[test.get(i, j)]++;
			}
			int maxPos = -1;
			int maxValue = -1;

			for (int j = 0; j < counts.length; j++) {
				if (counts[j]>maxValue) {
					maxValue=counts[j];
					maxPos=j;
				}
			}

			ret.set(i, maxPos);
		}

		return ret;
	}

}
