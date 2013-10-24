package de.ismll.table;

/**
 * TODO: Add support for stripping leading and trailing whitespaces
 * 
 * @author Andre Busche
 */
public class ReaderConfig{
	public enum FileType {
		Binary, Ismll, Arff, Csv, HeaderlessCsv, AnnotatedIsmll
	}

	public char fieldSeparator = ',';
	public int progressTicker = 100000;
	public int skipLines=0;
	public int numRows=-1;
	public int numColumns = -1;
	public boolean autodetectFormat=false;
	public FileType type;
	// just in case the dense format contains no field information (e.g., two \t\t occurr in byte sequence), this value is used instead.
	public String defaultValue;
	
	public static final ReaderConfig CSV;
	static {
		CSV = new ReaderConfig();
		CSV.autodetectFormat=true;
	}
	public static final ReaderConfig ISMLL_ANNOTATED_MATRIX;
	
	static {
		ISMLL_ANNOTATED_MATRIX = new ReaderConfig();
		ISMLL_ANNOTATED_MATRIX.type=FileType.AnnotatedIsmll;
		ISMLL_ANNOTATED_MATRIX.fieldSeparator='\t';
	}
	
	
	public ReaderConfig copy() {
		ReaderConfig ret = new ReaderConfig();
		ret.fieldSeparator=fieldSeparator;
		ret.progressTicker=progressTicker;
		ret.skipLines = skipLines;
		ret.numRows = numRows;
		ret.numColumns = numColumns;
		ret.autodetectFormat = autodetectFormat;
		ret.type= type;
		ret.defaultValue = defaultValue;
		return ret;
	}
}