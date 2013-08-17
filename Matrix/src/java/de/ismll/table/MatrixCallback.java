package de.ismll.table;

public interface MatrixCallback {

	void meta(int numRows, int numColumns);

	void setField(int row, int col, String string);

}
