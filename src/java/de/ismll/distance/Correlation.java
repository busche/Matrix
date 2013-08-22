package de.ismll.distance;

import de.ismll.table.Vector;
import de.ismll.table.Vectors;

public class Correlation implements IDistanceMeasure{

	@Override
	public double distance(Vector a, Vector b) {
		return Vectors.correlation(a,b);
	}

}
