package de.ismll.table.processing;

import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultVector;

public class Normalizer {

	final double average;
	final double variance;

	public Normalizer(Vector v) {
		this(Vectors.average(v), Vectors.variance(v));
	}

	public Normalizer(double average, double variance) {
		super();
		this.average = average;
		this.variance = variance;
	}

	public Vector normalize(Vector in) {
		DefaultVector ret = new DefaultVector(in);
		normalizeInPlace(in);
		return ret;
	}

	public void normalizeInPlace(Vector in) {
		for (int i = 0; i < in.size(); i++) {
			float value = in.get(i);
			in.set(i, (float) ((value-average)/variance));
			if (Float.isNaN(in.get(i)))
				in.set(i, 0);
		}
	}

	public float normalize(float value) {
		return (float) ((value-average)/variance);
	}

}
