package de.ismll.storage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.bootstrap.Parameter;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;


public class FileStorageTarget implements StorageTarget{

	@Parameter(cmdline="targetDirectory")
	private File targetDirectory;
	
	protected static final Logger log = LogManager.getLogger(FileStorageTarget.class);
	
	@Override
	public boolean store(Map<String, Object> values) throws StorageException {
		if (targetDirectory == null)
			throw new StorageException("Storage directory not intiialized!");
		boolean override = Boolean.parseBoolean(System.getProperty("de.ismll.storage.FileStorageTarget.override"));
		
		if (targetDirectory.exists()) {
			if (override) {
				// all OK - should override resources
				boolean delete = targetDirectory.delete();
				if (!delete) {
					throw new StorageException("Storage directory " + targetDirectory + " already exists but could not be deleted (for an unknown reason; delete() returned false; This was tried due to the automatic override switch).");
				}
				boolean mkdirs = targetDirectory.mkdirs();
				if (!mkdirs) {
					throw new StorageException("Storage directory " + targetDirectory + " could not be re-created (for an unknown reason; mkdirs() returned false).");
				}
			} else {
				throw new StorageException("Storage directory " + targetDirectory + " already exists and it should not be overridden automatically. You may want to use \"-Dde.ismll.storage.FileStorageTarget.override=true\" to automatically override existing storage targets.");
			}
		} else {
			boolean targetDirectoriesCreated = targetDirectory.mkdirs();
			if (!targetDirectoriesCreated) {
				log.warn("Target directory " + targetDirectory + " could somehow not be created (mkdirs() returned false; though no Exception was thrown). This should lead to errors or exceptions soon, I suppose...");
			}
		}
		
		if (!targetDirectory.isDirectory())
			throw new StorageException("Storage directory " + targetDirectory + " does not point to a directory.");
		
		boolean ret = true;
		for (Entry<String, Object> entry : values.entrySet()) {
			File current = new File(targetDirectory, entry.getKey());
			try {
				store(current, entry.getValue());
			} catch (IOException e) {
				log.warn("Failed to store field " + current, e);
				ret = false;
			}
		}
		
		return ret;
	}

	public static void store(File targetFile, Object value) throws IOException {
		
		/*
		 * determine parent directory and create it, if it does not exist.
		 */
		File parent = targetFile.getParentFile();
		if (!parent.exists())
			parent.mkdirs();
		
		if (value instanceof String) {
			writeString(targetFile, (String)value);
		}
		if (value instanceof Number) {
			writeString(targetFile, ((Number)value).toString());
		}
		if (value instanceof Vector) {
			writeVector(targetFile, (Vector)value);
		}
		if (value instanceof Matrix) {
			writeMatrix(targetFile, (Matrix)value);
		}
			
	}

	public static void writeMatrix(File targetFile, Matrix value) throws IOException, NullPointerException {
		if (null == targetFile)
			throw new NullPointerException("Variable targetFile was null but should not have been null!");
		
		int numRows = value.getNumRows();
		int numColumns = value.getNumColumns();
		
		log.info("Storing a " + numRows + "x" + numColumns + " matrix to " + targetFile);
		Matrices.write(value, targetFile);
	}

	public static void writeVector(File targetFile, Vector value) throws IOException {
		log.info("Storing a vector of length " + value.size() + " to " + targetFile);
		Vectors.write(value, targetFile);
	}

	public static void writeString(File targetFile, String value) throws IOException {
		log.info("Storing a string of length " + value.length() + " to " + targetFile);
		
		try (FileWriter fw = new FileWriter(targetFile)){			
			fw.write(value);
		} catch (IOException e) {
			throw e;
		}
	}

	public File getTargetDirectory() {
		return targetDirectory;
	}

	public void setTargetDirectory(File targetDirectory) {
		this.targetDirectory = targetDirectory;
	}

}
