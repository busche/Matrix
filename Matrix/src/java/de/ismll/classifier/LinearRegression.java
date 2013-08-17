package de.ismll.classifier;

import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultVector;
import de.ismll.table.projections.ColumnUnionMatrixView;

public class LinearRegression implements BinaryClassifier{

	public Vector classifyBinary(Matrix predictorsTrain, final Matrix predictorsTest,
			final Vector targetsTrain) {
		System.out.println("LinearRegression has not been tested intensivly! ");
		Vector ret = new DefaultVector(predictorsTest.getNumRows());
		DefaultMatrix constMatrix = Matrices.getConstMatrix(predictorsTrain.getNumRows(), 1, 1.f);

		predictorsTrain = new ColumnUnionMatrixView(new Matrix[] {predictorsTrain, constMatrix});

		QRDecomposition decomp = new QRDecomposition(predictorsTrain);

		Vector hyperparams = decomp.solve(targetsTrain);

		//		System.out.println("== Hyperparameters ==");
		//		System.out.println(Vectors.toString(hyperparams));

		for (int r = 0; r < predictorsTest.getNumRows(); r++) {
			double prediction=0.;
			for (int c = 0; c < hyperparams.size()-1; c++) {
				prediction += predictorsTest.get(r, c)*hyperparams.get(c);
			}
			prediction += hyperparams.get(hyperparams.size()-1);
			ret.set(r, (float) prediction);
		}
		return ret;
	}

	private Vector hyperparameters;

	public Vector getHyperparameters() {
		return hyperparameters;
	}

	public void setHyperparameters(Vector hyperparameters) {
		this.hyperparameters = hyperparameters;
	}

}
