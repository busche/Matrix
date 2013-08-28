package de.ismll.classifier;

import de.ismll.table.IntMatrix;
import de.ismll.table.Matrix;
import de.ismll.table.impl.DefaultMatrix;

public class AverageClassifier implements IBinaryIntbasedClassifier {

	
	public Matrix classifyBinary(IntMatrix predictorsTrain,
			IntMatrix predictorsTest, Matrix targetsTrain) {
		DefaultMatrix ret= new DefaultMatrix(predictorsTest.getNumRows(), 2);

		double sum = 0.;
		for (int i = 0; i < targetsTrain.getNumRows(); i++)
			sum += targetsTrain.get(i, 1);

		sum /=targetsTrain.getNumRows();
		for (int i = 0; i < predictorsTest.getNumRows(); i++) {
			ret.set(i,1, (float) sum);
			ret.set(i,0, predictorsTest.get(i, 0));
		}
		return ret;
	}

}
