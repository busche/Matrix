package de.ismll.experimental;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import de.ismll.bootstrap.BootstrapException;
import de.ismll.bootstrap.CommandLineParser;
import de.ismll.stub.AbstractProxy;

/**
 * 
 * A factory for Message consumer as required by the bootstrap library, see http://tech4research.wordpress.com/2013/05/06/indirect-support-for-interfaces-in-bootstrap/
 * 
 * @author Andre Busche
 *
 */
public class MessageConsumerFactory extends AbstractProxy<MessageConsumer>{


	private static final String SCHEME_FILE = "file";
	private static final String SCHEME_LOG = "log";

	private static final String[] SUPPORTED_SCHEMES = {
		SCHEME_FILE, SCHEME_LOG
	};

	public static MessageConsumerFactory convert(Object arg) {
		if (arg.toString().contains(" ")) {
			arg = arg.toString().replaceAll(" ", "%20");
		}
		URI where = (URI) CommandLineParser.convert(arg, URI.class);

		MessageConsumerFactory ret = new MessageConsumerFactory();

		String scheme = where.getScheme();
		switch (scheme) {
		case SCHEME_FILE:
			try {
				File file = new File(where);
				file.createNewFile();
				ret.setTarget(new FileConsumer(file));
			} catch (IOException e1) {
				throw new BootstrapException("Output file not found / accessible / valid", e1);
			}
			break;
		case SCHEME_LOG:
			try {
				ret.setTarget(new Log4JConsumer(where));
			} catch (ClassNotFoundException e) {
				throw new BootstrapException("Scheme-Specific part (" + where.getSchemeSpecificPart() + ") does not point to an valid class / could not found in the class path. ", e);
			}
			break;
		default:
			throw new BootstrapException("Unsupported target scheme: " + scheme + ". Supported are: " + Arrays.toString(SUPPORTED_SCHEMES));
		}
		return ret;
	}

}
