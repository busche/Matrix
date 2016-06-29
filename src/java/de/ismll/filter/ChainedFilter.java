package de.ismll.filter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.bootstrap.CommandLineParser;
import de.ismll.table.Matrix;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.utilities.Buffer;

/**
 * applies multiple Filters.
 * 
 * Needs to load the filters from a Properties-Object using {@link #loadFromProperties(Properties)}, otherwise just passes through the given Matrix
 * 
 * @author Andre Busche
 *
 */
public class ChainedFilter implements Filter{

	protected Logger log = LogManager.getLogger(getClass());

	private static final String KEY_START = "global.filter";

	private String identifier = null;

	public ChainedFilter() {
		super();
	}

	public ChainedFilter(File configuration) throws FileNotFoundException, IOException {
		this(Buffer.newInputStream(configuration));
		identifier=configuration.getName();
	}

	/**
	 * @throws NullPointerException if is is null
	 */
	public ChainedFilter(InputStream is) throws IOException, NullPointerException {
		this(loadProperties(is));

	}

	public ChainedFilter(Properties properties) {
		this();
		loadFromProperties(properties);
	}

	/**
	 * @param string a valid resource which can be found in the class path
	 */
	public ChainedFilter(String string) throws IOException {
		this(resolveResource(string));
		identifier=string;
	}

	private static InputStream resolveResource(String reference) throws IOException {
		try (InputStream resourceAsStream = ChainedFilter.class.getResourceAsStream(reference)) {
			if (resourceAsStream!=null)
				return resourceAsStream;
		}
		return null;
	}

	/**
	 * interprets the given properties
	 * 
	 */
	public void loadFromProperties(Properties loadProperties) {
		log.info("Loading Properties...");

		for (Entry<Object, Object> e : loadProperties.entrySet()) {
			if (!(e.getKey() instanceof String)) continue;
			if (!(e.getValue() instanceof String)) continue;
			String key = (String) e.getKey();
			String value = (String) e.getValue();

			if (!key.startsWith(KEY_START)) {
				log.debug("Skipping property " + key + ". Does not start with " + KEY_START);
				continue;
			}

			Integer position = Integer.valueOf(key.substring(KEY_START.length()+1, key.length()));

			if (filters.containsKey(position)) {
				log.warn("Overriding filter definition at position " + position + ". Is this really intended????");
			}

			String filterClazz;
			String configuration = null;
			if (value.contains("[")) {
				filterClazz = value.substring(0, value.indexOf('['));
				configuration = value.substring(value.indexOf('[')+1, value.length()-1);
			}
			else
				filterClazz = value;

			Object filterObject ;
			try {
				filterObject = Class.forName(filterClazz).newInstance();
			} catch (InstantiationException | IllegalAccessException
					| ClassNotFoundException e1) {
				log.error("Error while instantiating " + filterClazz, e1);
				continue;
			}

			if (!(filterObject instanceof Filter)) {
				log.error("Object of class " + filterClazz + " does not implement " + Filter.class + ". cannot use it!");
				continue;
			}
			Filter filter = (Filter) filterObject;


			if (configuration!=null) {
				Map<String, Object> parseMap = CommandLineParser.parseMap(configuration);
				CommandLineParser.applyArguments(filterObject, parseMap);
			}
			log.debug("Adding filter " + filter.getVisibleName() + " as position " + position + " to the filter chain.");
			filters.put(position, filter);
		}
	}

	private TreeMap<Integer, Filter> filters = new TreeMap<>();

	private static Properties loadProperties(InputStream is) throws IOException, NullPointerException {
		if (is == null) throw new NullPointerException("Input Parameter is null");
		Properties prop = new Properties();
		prop.load(is);
		return prop;
	}

	public static ChainedFilter convert(Object in) throws Exception {
		if (in instanceof File)
			return new ChainedFilter((File)in);
		if (in instanceof InputStream)
			return new ChainedFilter((InputStream)in);
		if (in instanceof Properties)
			return new ChainedFilter((Properties)in);
		String reference;
		if (in instanceof String)
			reference = (String)in;
		else
			reference = in.toString();

		// check if the reference can be found in the classpath
		try (InputStream resourceAsStream = ChainedFilter.class.getResourceAsStream(reference)) {
			if (resourceAsStream!=null)
				return new ChainedFilter(resourceAsStream);
		}

		// check whether it is a reference to a file
		File f = new File(reference);
		if (f.exists())
			return new ChainedFilter(f);

		// give up
		throw new RuntimeException("Failed to interpret " + in.toString() + " as a valid ChainedGprFilter source. It is neither a File, an InputStream, a Properties object, an existing resource in the class path, nor a valid pointer to a file in the file system");
	}

	@Override
	public String getVisibleName() {
		return "Filter chain" + (identifier!=null?" (" + identifier + ")":"");
	}

	@Override
	public Matrix apply(Matrix in) {
		if (filters.isEmpty()) return in;
		Matrix tmp = new DefaultMatrix(in);

		for (Entry<Integer, Filter> current : filters.entrySet()) {
			tmp = current.getValue().apply(tmp);
		}
		return tmp;
	}

	public Filter setFilter(Integer position, Filter filter) {
		return filters.put(position, filter);
	}

}