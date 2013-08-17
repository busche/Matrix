package de.ismll.classifier;

import de.ismll.functions.LineFunction;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultVector;

public class Regression {



	private double xmean;
	private double ymean;
	private Vector x;
	private Vector y;

	public Regression(Vector x, Vector y) {
		super();
		this.x = x;
		this.y = y;
	}


	public LineFunction calculateLine() {

		xmean = Vectors.average(x);
		ymean = Vectors.average(y);

		double slope = calculateSlope();

		double yintercept = calculateYintercept(slope);

		LineFunction line = new LineFunction((float)slope, (float)yintercept);

		return line;
	}

	private double calculateSlope() {

		double numerator = 0;
		for (int i = 0; i < x.size(); i++) {
			numerator = numerator + ((x.get(i) - xmean) * (y.get(i) - ymean));

		}

		double denominator = 0;
		for (int i = 0; i < x.size(); i++) {
			denominator = denominator + ((x.get(i) - xmean) * (x.get(i) - xmean));
		}

		double m = numerator / denominator;

		return m;
	}

	private double calculateYintercept(double slope) {

		double yintercept = ymean - (slope * xmean);

		return  yintercept;
	}

	public static void main(String[] args) {

		DefaultVector dv1 = new DefaultVector(200);
		DefaultVector dv2 = new DefaultVector(200);
		Vectors.fillUniformAtRandom(dv1, 0, 10);
		Vectors.fillUniformAtRandom(dv2, 0, 10);

		Regression reg = new Regression(dv1, dv2);
		LineFunction line = reg.calculateLine();

		System.out.println("xquer: " + Vectors.average(dv1));
		System.out.println("yquer: " + Vectors.average(dv2));
		System.out.println("m:" + line.getSlope());
		System.out.println("b:" + line.getIntercept());
	}
}