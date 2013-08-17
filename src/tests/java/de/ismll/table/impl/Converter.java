package de.ismll.table.impl;

import java.io.File;

import de.ismll.table.Matrices;
import de.ismll.table.Matrices.FileType;
import de.ismll.table.Matrix;


public class Converter {

	//	@Test
	public void doit() throws Exception {
		File in = new File("C:\\work\\ismis2011\\genresTrain-categoricalTargets.csv.0\\matrix.data");
		File outputFile = new File("sparse.train.data");
		Matrix read = Matrices.read(in, FileType.Ismll, 10000);
		//		Vector labels = null;
		Matrices.writeSparse(read, outputFile);

	}
}
