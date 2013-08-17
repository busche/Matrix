package de.ismll.distance;

import de.ismll.table.Vector;
import de.ismll.table.Vectors;

public class Covariance implements DistanceMeasure {

	@Override
	public double distance(Vector a, Vector b) {
		return Vectors.covariance(a, b);
	}

}
