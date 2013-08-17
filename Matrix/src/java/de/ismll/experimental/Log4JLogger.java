package de.ismll.experimental;

import java.net.URI;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Log4JLogger implements Output  {

	public Log4JLogger(URI where) throws ClassNotFoundException {
		Class<?> forName = Class.forName(where.getSchemeSpecificPart());
		log=LogManager.getLogger(forName);
	}

	public Log4JLogger(Logger log2) {
		log = log2;
	}

	protected final Logger log;


	@Override
	public void message(String string) {
		log.info(string);
	}

	public static Output from(Logger log) {
		return new Log4JLogger(log);
	}


}
