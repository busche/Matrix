package de.ismll.converter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import weka.core.Instances;
import de.ismll.bootstrap.Parameter;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.utilities.Assert;
import de.ismll.utilities.Buffer;

public class Arff2Ismll implements Runnable{

	@Parameter(cmdline="input")
	private File input;

	@Parameter(cmdline="output")
	private File output;

	@Parameter(cmdline="sparse", description="whether to write sparse files")
	private boolean sparse;

	@Parameter(cmdline="binary", description="whether to write binary file format")
	private int binary=-1;

	@Parameter(cmdline="progress")
	private int progressCounter=100000;

	@Override
	public void run() {
		Assert.notNull(getInput(), "input");
		Assert.notNull(getOutput(), "output");

		System.out.println("Input file:  " + input);
		System.out.println("Output file: " + output);

		InputStream is;
		Matrix in;
		try {
			is = Buffer.newInputStream(input);
			in = convert(is);
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}

		try {
			if (isSparse()) {
				System.out.println("Writing in sparse format.");
				Matrices.writeSparse(in, getOutput());
			} else {
				if (getBinary()>=0) {
					System.out.println("Writing binary format.");
					Matrices.writeBinary(in, getOutput(), (byte)getBinary());
				} else {
					System.out.println("Writing csv-exchange format.");
					Matrices.write(in, getOutput());
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private Matrix convert(InputStream is) throws IOException {
		return Matrices.readWeka0(is, getProgressCounter());

	}

	public void setInput(File input) {
		this.input = input;
	}

	public File getInput() {
		return input;
	}

	public void setOutput(File output) {
		this.output = output;
	}

	public File getOutput() {
		return output;
	}

	public void setSparse(boolean sparse) {
		this.sparse = sparse;
	}

	public boolean isSparse() {
		return sparse;
	}

	public void setBinary(int binary) {
		this.binary = binary;
	}

	public int getBinary() {
		return binary;
	}

	public void setProgressCounter(int progressCounter) {
		this.progressCounter = progressCounter;
	}

	public int getProgressCounter() {
		return progressCounter;
	}

	public static Matrix convert(Instances inst) {
		Arff2Ismll instance = new Arff2Ismll();

		ByteArrayInputStream bais = new ByteArrayInputStream(inst.toString().getBytes());
		try{
			return instance.convert(bais);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				bais.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
