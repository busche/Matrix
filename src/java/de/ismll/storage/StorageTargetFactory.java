package de.ismll.storage;

import java.io.File;
import java.net.URI;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.bootstrap.BootstrapException;
import de.ismll.bootstrap.CommandLineParser;
import de.ismll.stub.AbstractProxy;

public class StorageTargetFactory extends AbstractProxy<StorageTarget>{

	private static Logger log = LogManager.getLogger(StorageTargetFactory.class);

	public static StorageTargetFactory convert(Object in) {
		log.warn("The usage of " + StorageTarget.class + " is EXPERIMENTAL!");
		StorageTargetFactory ret = new StorageTargetFactory();
		StorageTarget impl = null;
		URI u = (URI) CommandLineParser.convert(in, URI.class);
		
		switch (u.getScheme()) {
		case "file":
			impl = new FileStorageTarget();
			((FileStorageTarget)impl).setTargetDirectory(new File(u));
			break;	
		case "memory":
			impl = new InMemoryStorage();
			break;
		case "log":
			try {
				impl = new LogStorage(u.getSchemeSpecificPart());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				BootstrapException toThrow = new BootstrapException("Unable to determine valid class for logfile target: " + u.getSchemeSpecificPart(), e);
//				toThrow.setBootstrapEnabledObject(StorageTargetFactory.class);
				throw toThrow;
			}
			break;
		}
		
		ret.setTarget(impl);
		return ret;		
	}
}
