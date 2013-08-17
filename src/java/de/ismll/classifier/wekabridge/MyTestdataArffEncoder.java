package de.ismll.classifier.wekabridge;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.ismll.table.IsmllArffEncoderMapper;
import de.ismll.table.io.weka.ArffEncoder;

public class MyTestdataArffEncoder implements ArffEncoder,
IsmllArffEncoderMapper {

	private final MyArffEncoder target;
	public Map<Integer, String> map;

	public String getName() {
		return target.getName();
	}

	public Type getAttributeType(int column) {
		return target.getAttributeType(column);
	}

	public String getAttributeName(int column) {
		return target.getAttributeName(column);
	}

	public MyTestdataArffEncoder(MyArffEncoder target) {
		this.target = target;
		this.map = new HashMap<Integer, String>();
	}

	@Override
	public String encode(int column, float value) {
		if (column == target.columns - 1)
			return "?";
		return target.encode(column, value);
	}

	@Override
	public Map<Integer, String> getMap(int column) {
		if (column == target.columns - 1)
			return map;
		return Collections.emptyMap();
	}

	@Override
	public int getNumColumns() {
		return target.columns;
	}

}