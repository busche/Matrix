package de.ismll.table;

import java.util.Map;

public interface IsmllArffEncoderMapper {

	public abstract Map<Integer, String> getMap(int column);

	public int getNumColumns();
}