package de.ismll.table.io.weka;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.table.IntVector;
import de.ismll.table.IsmllArffEncoderMapper;
import de.ismll.table.Matrices;
import de.ismll.table.Matrices.IsmllArffEncoder;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.io.weka.ArffEncoder.Type;
import de.ismll.utilities.Buffer;

/**
 * 
 * 
 * @author John
 *
 */
public class ArffDataset {

	protected Logger logger = LogManager.getLogger(getClass());

	public static final class FilteredArffEncoder implements ArffEncoder, IsmllArffEncoderMapper {

		private final IntVector f;
		private final IsmllArffEncoder target;

		public FilteredArffEncoder(IsmllArffEncoder target, IntVector attr) {
			this.target = target;
			this.f = attr;
		}

		@Override
		public Type getAttributeType(int column) {
			return target.getAttributeType(f.get(column));
		}

		@Override
		public String getAttributeName(int column) {
			return target.getAttributeName(f.get(column));
		}

		@Override
		public String encode(int column, float value) {
			return target.encode(f.get(column), value);
		}

		@Override
		public String getName() {
			return target.getName();
		}

		@Override
		public Map<Integer, String> getMap(int column) {
			return target.getMap(f.get(column));
		}

		@Override
		public int getNumColumns() {
			return f.size();
		}
	}

	public static ArffDataset convert(Object source) {
		if (source == null)
			return null;
		InputStream is = null;

		if (source instanceof File) {
			try {
				is = Buffer.newInputStream((File)source);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		if (source instanceof InputStream)
			is = (InputStream) source;

		if (is == null) {
			// try String
			String s = source.toString();
			try {
				is = Buffer.newInputStream(new File(s));
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		if (is==null)
			throw new RuntimeException("Source " + source + " coould not be converted into a valid input stream.");
		try {
			return Matrices.readWeka(is, -1);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private ArffEncoder enc;

	public ArffDataset(Matrix data, ArffEncoder enc) {
		super();
		this.data = data;
		this.enc = enc;
		if (enc instanceof IsmllArffEncoderMapper) {
			if (((IsmllArffEncoderMapper)enc).getNumColumns() != data.getNumColumns())
				logger.warn("ISMLL-ArffEncoder reportes non-equal amount of columns, compared to the data array (" + ((IsmllArffEncoderMapper)enc).getNumColumns() + "!=" + data.getNumColumns() + "). This *might* be OK, but I suspect that there is a problem in your implementation (have you used a ColumnSubsetView to filter the Columns of the data array??) If so, consider cloning the IsmllArffEncoder using the getEncoder(Vector attributes) method to create a filtered encoder!.");
		}
	}

	public ArffDataset(Matrix data) {
		/*
		 * only numerical columns ...
		 */
		this(data, new ArffEncoder() {


			@Override
			public Type getAttributeType(int column) {
				return Type.Numeric;
			}

			@Override
			public String getAttributeName(int column) {
				return "attr" + column;
			}

			@Override
			public String encode(int column, float value) {
				return value + "";
			}

			@Override
			public String getName() {
				return "anonymous-all numerical";
			}

		});
	}

	public final Matrix data;

	public boolean isNumeric(int col) {
		return enc.getAttributeType(col).equals(Type.Numeric);
	}

	public boolean isNominal(int col) {
		return enc.getAttributeType(col).equals(Type.Nominal);
	}

	/**
	 * WARNING: The encoder is dependant on the number of attributes! If you alter the underlying attributes, consider filtering the Encoder using getEncoder(Vector)!!!
	 * 
	 */
	public ArffEncoder getEncoder() {
		return enc;
	}

	public String unmap(int col, float value){
		return enc.encode(col, value);
	}


	/**
	 * @return name of the dataset
	 */
	public String getName() {
		return enc.getName();
	}

}
