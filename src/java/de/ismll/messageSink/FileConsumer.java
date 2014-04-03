package de.ismll.messageSink;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.WeakReference;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class FileConsumer implements MessageConsumer {

	protected Logger logger = LogManager.getLogger(getClass());

	private File file;
	private WeakReference<PrintStream> ref;

	public FileConsumer(File file) throws IOException {
		this.file = file;
		if (!file.exists())
			throw new FileNotFoundException(file.getAbsolutePath());
		if (!file.canWrite())
			throw new IOException("Cannot write to File");
	}

	@Override
	public void message(String string) {
		PrintStream ps = ensurePrintStream();

		ps.println(string);
	}

	private PrintStream ensurePrintStream() {
		PrintStream ret = null;
		if (ref != null) {
			ret = ref.get();
		}

		if (ret == null) {
			PrintStream printStream;
			try {
				printStream = new PrintStream(file);
			} catch (FileNotFoundException e) {
				// never happens.
				throw new RuntimeException(e);
			}
			ref = new WeakReference<PrintStream>(printStream);
			ret = printStream;
		} else {
			logger.debug("recycling PrintStream");
		}

		return ret;
	}

}
