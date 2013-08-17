package de.ismll.runtime.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 * contents of a text file, row-wise
 * 
 * reloads automatically, if contents are changed.
 * 
 * @author busche
 *
 */
public class TextFile {
	private File csvFile;

	protected final ArrayList<String> rows = new ArrayList<String>();
	public final Logger log = LogManager.getLogger(getClass());
	long lastFileLoad = -1;

	private void checkForFileUpdate() throws IOException {
		final File csvFile2 = getCsvFile();
		final long lastModified = csvFile2.lastModified();
		if (lastModified > lastFileLoad) {
			loadFile();

			lastFileLoad = lastModified;
		}
	}

	public File getCsvFile() {
		return csvFile;
	}

	public synchronized List<String> getData() throws IOException {
		log.debug("entering getToolsList method");
		checkForFileUpdate();
		return rows;
	}

	// reads the input file and puts each line in one string and a list
	// surrounds all of strings
	protected synchronized void loadFile() throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(getCsvFile())
				));
		rows.clear();
		
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				rows.add(line.trim());
			}
		} catch (final IOException e) {
			throw e;
		} finally {
			reader.close();
		}

	}

	public void setCsvFile(File csvFile) {
		log.info("Csv-Repository is initialized to be "	+ csvFile);		
		this.csvFile = csvFile;
	}

}
