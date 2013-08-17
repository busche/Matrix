package de.ismll.runtime.files;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class XmlRepository {
	private File xmlFile;

	public File getXmlFile() {
		return xmlFile;
	}

	public final Logger log = LogManager.getLogger(getClass());

	private HashMap<String, Query> queries;

	long lastFileLoad = -1;

	private void checkForFileUpdate() throws IOException {
		final long lastModified = xmlFile.lastModified();
		if (lastModified > lastFileLoad) {
			loadFile();
			lastFileLoad = lastModified;
		}
	}

	public synchronized String compileQuery(String queryIdentifier,
			Object... parameters) throws IOException,
			NoQueryFoundException {
		log.debug("Query requested: " + queryIdentifier);

		checkForFileUpdate();

		final String rawQuery = getRawQuery(queryIdentifier);
		log.debug("Raw query is " + rawQuery);
		final String compiled = String.format(rawQuery, parameters);
		log.debug("Compiled query is " + compiled);
		return compiled;
	}

	public synchronized String countQuery(String queryIdentifier,
			Object... parameters) throws IOException,
			NoQueryFoundException {

		return "SELECT COUNT(*) as cnt FROM (" + compileQuery(queryIdentifier, parameters) + ") as t";
	}
	
	public String getRawQuery(String queryIdentifier)
			throws NoQueryFoundException {
		try {
			checkForFileUpdate();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		final Query query = queries.get(queryIdentifier);
		if (query == null) {
			throw new NoQueryFoundException("No query named " + queryIdentifier
					+ " found!");
		}

		return query.getImplementation();
	}

	private void loadFile() throws IOException {
		if (queries == null)
			queries = new HashMap<String, Query>();
		else
			queries.clear();
		final Class<Queries> c = Queries.class;
		final String packageName = c.getPackage().getName();
		JAXBContext jc;
		Unmarshaller u = null;
		JAXBElement<Queries> doc;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(xmlFile);
			jc = JAXBContext.newInstance(packageName);
			u = jc.createUnmarshaller();
			doc = (JAXBElement<Queries>) u.unmarshal(fis);
		} catch (final JAXBException e) {
			log.error(e.getMessage(), e);
			throw new IOException(e.getMessage());
		} finally {
			if (fis != null)
				fis.close();
		}

		for (final Query q : doc.getValue().getQuery())
			queries.put(q.getId(), q);

	}

	public void setXmlFile(File xmlFile) {
		log.info("XML-Repository is initialized to be " + xmlFile);
		
		this.xmlFile = xmlFile;
	}
}
