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

	@Deprecated
	public static Matrix read(File in) throws IOException{
		try(InputStream newInputStream = Buffer.newInputStream(in)) {
			return Matrices.readDense(newInputStream, ReaderConfig.CSV);
		} 
	}
}
