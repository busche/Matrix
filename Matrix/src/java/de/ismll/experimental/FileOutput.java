package de.ismll.experimental;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.WeakReference;

public class FileOutput implements Output {

	private File file;
	private WeakReference<PrintStream> ref;

	public FileOutput(File file) throws IOException {
		this.file = file;
		if (!file.exists()) throw new FileNotFoundException(file.getAbsolutePath());
		if (!file.canWrite()) throw new IOException("Cannot write to File");
	}

	@Override
	public void message(String string) {
		PrintStream ps = ensurePrintStream();

		if (ps == null) {
			System.out.println("aaarg!");
			return;
		}
		ps.println(string);
	}

	int cnt =0;
	private PrintStream ensurePrintStream() {
		PrintStream ret =null;
		if (ref != null) {
			ret = ref.get();
		}

		if (ret  == null) {
			PrintStream printStream;
			try {
				printStream = new PrintStream(file);
			} catch (FileNotFoundException e) {
				//				never happens.
				throw new RuntimeException(e);
			}
			ref = new WeakReference<PrintStream>(printStream);
			ret =printStream;
		} else {
			System.out.println("recycling PrintStream");
			//			if (cnt++ == 3) {
			//				System.gc();
			//				try {
			//					Thread.sleep(1000);
			//				} catch (InterruptedException e) {
			//					// TODO Auto-generated catch block
			//					e.printStackTrace();
			//				}
			//
			//			}
		}

		return ret;
	}

}
