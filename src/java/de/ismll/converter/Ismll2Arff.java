package de.ismll.converter;

import java.io.File;
import java.io.IOException;

import de.ismll.bootstrap.CommandLineParser;
import de.ismll.bootstrap.Parameter;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.utilities.Assert;

public class Ismll2Arff implements Runnable {

	@Parameter(cmdline="input", description="Input file")
	private File input;

	@Parameter(cmdline="output", description="Output file")
	private File output;

	@Parameter(cmdline="binary", description="Whether ISMLL file is binary (true), or not")
	private boolean isBinary;

	@Parameter(cmdline="sparse", description="true, if the input file follows the sparse file format, false otherwise.")
	private boolean isSparse=false;

	@Parameter(cmdline="progress", description="progress counter, after how many instances a 'dot' is placed.")
	private int progressCounter = 100000;

	@Override
	public void run() {
		Assert.notNull(input, "input");
		Assert.notNull(output, "output");

		Matrix in;
		try {
			if (!isSparse())
				if (isBinary())
					in = Matrices.readBinary(input);
				else
					in = DefaultMatrix.read(input, getProgressCounter());
			else
				in = DefaultMatrix.readFromSparse(input);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		try {
			Matrices.writeArff(output, in, getProgressCounter());
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}


	public void setOutput(File output) {
		this.output = output;
	}


	public File getOutput() {
		return output;
	}


	public void setInput(File input) {
		this.input = input;
	}


	public File getInput() {
		return input;
	}

	public void setProgressCounter(int progressCounter) {
		this.progressCounter = progressCounter;
	}


	public int getProgressCounter() {
		return progressCounter;
	}


	public void setSparse(boolean isSparse) {
		this.isSparse = isSparse;
	}


	public boolean isSparse() {
		return isSparse;
	}


	public void setBinary(boolean isBinary) {
		this.isBinary = isBinary;
	}


	public boolean isBinary() {
		return isBinary;
	}

	public static void main(String[] args) {
		Ismll2Arff i = new Ismll2Arff();
		CommandLineParser.parseCommandLine(args, i);
		i.run();
	}

}
