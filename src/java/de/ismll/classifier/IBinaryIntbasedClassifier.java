package de.ismll.classifier;

import de.ismll.table.IntMatrix;
import de.ismll.table.Matrix;

/**
 * 
 * @author Andre Busche
 * @version 1.0
 */
public interface IBinaryIntbasedClassifier {

	public Matrix classifyBinary(IntMatrix predictorsTrain, IntMatrix predictorsTest, Matrix targetsTrain);


}