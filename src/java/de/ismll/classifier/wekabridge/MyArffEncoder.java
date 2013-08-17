package de.ismll.classifier.wekabridge;

import de.ismll.table.io.weka.ArffEncoder;

public class MyArffEncoder implements ArffEncoder {

	public MyArffEncoder(int columns) {
		this.columns = columns;
	}

	int columns;

	@Override
	public String getName() {
		return "wrapped Arff-Dataset";
	}

	@Override
	public Type getAttributeType(int column) {
		if (column == columns - 1)
			return Type.Nominal;
		return Type.Numeric;
	}

	@Override
	public String getAttributeName(int column) {
		if (column == columns - 1)
			return "clazz";
		return "attr" + column;
	}

	@Override
	public String encode(int column, float value) {
		// if (column==columns)
		// return "?";
		return value + "";
	}

}