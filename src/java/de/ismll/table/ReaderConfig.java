package de.ismll.table;

/**
 * TODO: Add support for stripping leading and trailing whitespaces
 * 
 * @author Andre Busche
 */
public class ReaderConfig{
	public char fieldSeparator = ',';
	public int progressTicker = 100000;
	public int skipLines=0;
	public int numRows=-1;
	public int numColumns = -1;
	public boolean autodetectFormat=false;
	@Deprecated
	public int bufferSize=8192;
	// just in case the dense format contains no field information (e.g., two \t\t occurr in byte sequence), this value is used instead.
	public String defaultValue;
}