package de.ismll;

import java.io.IOException;
import java.io.LineNumberReader;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.utilities.Props;

/**
 * BUG-Alert: There seems to be a JVM error in Java 7 b04 causing a wrong initialization of static fields (of subclasses)
 * 
 * 
 * @author Andre Busche
 *
 */
public abstract class AbstractLocalConfiguration {

	private static Logger logger = LogManager.getLogger(AbstractLocalConfiguration.class);

	public final static String KEY_NAMESPACE = "de.ismll.acogpr.LocalConfiguration";

	protected static final String INITIALIZED = KEY_NAMESPACE + ".initialized";


	protected static final Props p = Props.getProps(AbstractLocalConfiguration.class);


	static {
		String initializedIndicator = p.getProperty(INITIALIZED, null);
		if (initializedIndicator == null) {
			error("Your setup is incomplete. Call de.ismll.acogpr.LocalConfiguration");
		}

	}



	private static void o(String str) {
		logger.info(str);
	}


	//	protected static boolean checkSetup() {
	//		logger.fatal(AbstractLocalConfiguration.class + " should not be used directly. Use a corresponding subclass, and override checkSetup()");
	//		return false;
	//	}

	static void error(String string) {
		logger.error(string);
	}

	//	public static void main(String[] args) throws IOException {
	//		if (!(logger.isInfoEnabled())) {
	//			logger.error("Enable Info-Logging for de.ismll.acogpr.LocalConfiguration");
	//			System.err.println("Enable Info-Logging for de.ismll.acogpr.LocalConfiguration");
	//			System.exit(1);
	//		}
	//		LineNumberReader read = new LineNumberReader(new InputStreamReader(System.in));
	//
	//		o(" ===== INTERACTIVE     AcoGPR local setup =====");
	//		o();
	//		o("(warning: buggy implementation - does not check validity of input!)");
	//		o();
	//		query(read, KEY_BASE_DIR, false, "Directory where FaM02.MIS is located?");
	//
	//
	//	}


	public static void main2(String[] args) {
		if (!(logger.isInfoEnabled())) {
			logger.error("Enable Info-Logging for de.ismll.acogpr.LocalConfiguration");
			System.err.println("Enable Info-Logging for de.ismll.acogpr.LocalConfiguration");
			System.exit(1);
		}

		o(" ===== INTERACTIVE     AcoGPR local setup =====");
		o();
		o("(warning: buggy implementation - does not check validity of input!)");
		o();

	}


	static void query(LineNumberReader read, String key, boolean allowNull, String query) throws IOException {

		String defaultValue = p.getProperty(key);
		o(query + " (" + defaultValue + ")");

		String data = null;
		boolean correct = false;

		while (!correct) {
			data = read.readLine();

			if (data == null || data.trim().length()==0) {
				// set value to default, if necessary
				if (defaultValue!=null) {
					data = defaultValue;
				}
			}

			if (allowNull && data == null) {
				o("! Resetting value for " + key + " to null!");
				correct=true;
			} else if (data != null){
				correct=true;
			} else {
				o("Sorry, need a valid value for " + key + ". Please try again...");
			}


		} // of while

		p.setProperty(key, data);
	}

	private static void o() {
		o("");
	}
}
