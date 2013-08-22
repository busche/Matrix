package de.ismll.messageSink;

import java.net.URI;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Log4JConsumer implements MessageConsumer {

	public Log4JConsumer(URI where) throws ClassNotFoundException {
		Class<?> forName = Class.forName(where.getSchemeSpecificPart());
		log = LogManager.getLogger(forName);
	}

	public Log4JConsumer(Logger log2) {
		log = log2;
	}

	protected final Logger log;

	@Override
	public void message(String string) {
		log.info(string);
	}

	public static MessageConsumer from(Logger log) {
		return new Log4JConsumer(log);
	}

}
