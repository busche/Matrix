package de.ismll.table.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeSet;

import org.junit.Test;

import de.ismll.bootstrap.CommandLineParser;
import de.ismll.runtime.Timer;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.MatrixTest;
import de.ismll.table.ReaderConfig;
import de.ismll.table.io.weka.ArffDataset;

public class RowMajorMatrixEquivalenceTest extends MatrixTest{
	

	@Test
	public void bootstrapTestLoadV04() throws IOException {
		int numRows=32*1024;
		int numCols=1024;
		DefaultMatrix dm = new DefaultMatrix(numRows, numCols);
		RowMajorMatrix rmm = new RowMajorMatrix(numRows, numCols);
		Matrices.fillUniformAtRandom(dm, 0, 10);
		Matrices.copy(dm, rmm);
		
		super.checkSame(dm, rmm);
		
		Timer newTimer = Timer.newTimer(getClass(), "basic operations");
		
		float max_dm=0;
		float max_rmm=0;
		newTimer.start("max-dm");
		for (int i =10; i >=0; i--)
		max_dm = Matrices.max(dm);
		newTimer.start("max-rmm");
		for (int i =10; i >=0; i--)
			max_rmm = Matrices.max(rmm);
		newTimer.end();
		
		System.out.println("max_dm = " + max_dm);
		System.out.println("max_rmm = " + max_rmm);
		System.out.println(newTimer.printStatistics());
		
		
	}

}
