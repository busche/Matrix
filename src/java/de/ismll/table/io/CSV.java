package de.ismll.table.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.ReaderConfig;
import de.ismll.utilities.Buffer;


/**
 * 
 * 
 * @author Andre Busche
 *
 */
@Deprecated
public class CSV {

	static ReaderConfig csvReaderConfig;
	static {
		csvReaderConfig = new ReaderConfig();
		csvReaderConfig.autodetectFormat=true;


	}
	@Deprecated
	public static Matrix read(File in) throws IOException{
		InputStream newInputStream = Buffer.newInputStream(in);
		try {
			return Matrices.readDense(newInputStream, csvReaderConfig);

		} finally {
			try {
				newInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
