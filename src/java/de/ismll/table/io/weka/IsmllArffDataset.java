package de.ismll.table.io.weka;

import de.ismll.table.IntVector;
import de.ismll.table.Matrices.IsmllArffEncoder;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.impl.DefaultIntVector;

public class IsmllArffDataset extends ArffDataset{

	private final IsmllArffEncoder encoder;

	public IsmllArffDataset(Matrix data, IsmllArffEncoder enc) {
		super(data, enc);
		encoder = enc;
	}

	public IsmllArffEncoder getEncoder() {
		return encoder;
	}

	public ArffEncoder getEncoder(Vector attributesFiltered)  {
		return getEncoder(new DefaultIntVector(attributesFiltered));
	}

	public ArffEncoder getEncoder(IntVector attributesFiltered)  {
		return new FilteredArffEncoder(encoder, attributesFiltered);
	}
}
