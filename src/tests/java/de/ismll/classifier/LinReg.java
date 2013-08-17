package de.ismll.classifier;

import java.io.File;
import java.io.IOException;

import de.ismll.evaluation.RmseEvaluator;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultVector;

public class LinReg {

	public static void main(String[] args) throws IOException {
		File base = new File("src/tests/data/linreg");
		DefaultMatrix train = DefaultMatrix.read(new File(base,"sample.data1"));
		DefaultMatrix test = DefaultMatrix.read(new File(base,"sample.data1.test"));
		DefaultVector trainLabels =DefaultVector.read(new File(base,"sample.labels1"));
		DefaultVector testLabels =DefaultVector.read(new File(base,"sample.labels1.test"));

		LinearRegression reg=  new LinearRegression();

		Vector classifyBinary = reg.classifyBinary(train, test, trainLabels);

		System.out.println("== Predictions ==");
		System.out.println(Vectors.toString(classifyBinary));

		System.out.println("== true labels ==");
		System.out.println(Vectors.toString(testLabels));

		RmseEvaluator e = new RmseEvaluator();
		float evaluate = e.evaluate(testLabels, classifyBinary);
		System.out.println(evaluate);
	}
}
