package de.ismll.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Props extends Properties{

	static Logger logger = LogManager.getLogger(Props.class);

	
	private final File source;

	public Props(File source) throws FileNotFoundException, IOException {
		super();
		this.source = source;
		if (source.isFile())
			load(Buffer.newInputStream(source));
		else
			source.createNewFile();
	}

	private static Map<String, Props> propsMap = new HashMap<String, Props>();

	public static Props getProps(Object forInstance) {
		return getProps(forInstance.getClass());
	}

	static boolean shutdownHookRegistered=false;

	public static Props getProps(Class<?> forClass) {
		String identifier = forClass.getName();
		Props ret = propsMap.get(identifier);
		if (ret == null) {
			String simpleName = forClass.getSimpleName();
			if ("".equals(simpleName))
				throw new RuntimeException("No Properties available for anaonymous classes!");

			File baseDir = new File(System.getProperty("user.home"), ".acogpr");
			if (!baseDir.exists())
				baseDir.mkdirs();

			File loadFrom = new File(baseDir, simpleName);

			try {
				ret = new Props(loadFrom);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			propsMap.put(identifier, ret);
		}

		if (!shutdownHookRegistered) {
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

				@Override
				public void run() {
					for (Props p : propsMap.values()) {
						try {
							p.store(Buffer.newOutputStream(p.source), "");
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}));

			shutdownHookRegistered=true;
		}

		return ret;

	}
	
	/**
	 * Expands the properties in object in and returns a new properties object with expanded variable.
	 * Expansion in this context means, that all references in values of the given properties object defined as ${reference} are tried to get resolved by looking up the property of name 'reference' in the passed map. Resolution happens until no more replacements can be made.
	 * 
	 * Note that properties are just substituted in-place. If the reference does not exist as a key in the passed properties object, it does not get replaced either.
	 * 
	 * The implementation supports recursive substitutions up to a nesting level of 32 and throws a RuntimeException if the substitution exceeds that level.
	 * 
	 * @param in
	 * @return the expanded properties object 
	 */
	public static Properties expandProperties(Properties in) {
		int numChanged = 0;
		int loopCounter = 0;
		Properties p = new Properties();
		p.putAll(in);
		
		HashMap<Object, Object> replacedMap = new HashMap<Object, Object>();
		
		do {
			replacedMap.clear();
			numChanged = 0;
			for (Entry<Object, Object> e : p.entrySet()) {
				String key = e.getKey().toString();
				String value = e.getValue().toString();
				int start = value.indexOf("${");
				if (start < 0) continue;
				
				int end = value.indexOf("}", start);
				if (end < 0) {
					// Warning! illegal substitution? ${ detected, but no closing bracket!
					logger.warn("illegal substitution? ${ detected, but no closing bracket!");
					continue;
				}
				String variable = value.substring(start+2, end);
				logger.debug("Detected variable usage of " + variable);
				
				String propertyValue = p.getProperty(variable);
				if (propertyValue == null) {
					// Warning! variable not found!
					logger.warn("Warning! variable not found!");
					continue;
				}
				String newValue = value.replace("${" + variable + "}", propertyValue);
				// temporarily 
				replacedMap.put(key, newValue);
				numChanged++;
			} /*of for*/
			p.putAll(replacedMap);
			loopCounter++;
			if (loopCounter > 32)
				throw new RuntimeException("Maximal recursive replacement rule exceeded (" + loopCounter + ").");
		} while (numChanged>0); /*of do ... while*/
		
		return p;
	}
	
}
