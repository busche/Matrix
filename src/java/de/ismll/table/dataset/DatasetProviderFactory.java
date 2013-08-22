package de.ismll.table.dataset;

import java.util.Map;

import de.ismll.bootstrap.BootstrapException;
import de.ismll.bootstrap.CommandLineParser;
import de.ismll.bootstrap.InvalidConversionFormatException;
import de.ismll.stub.AbstractProxy;

/**
 * 
 * Bootstrap-semantics: dataset=de.ismll.table.dataset.CsvPredictOnlyDatasetProvider[train=c:\work\0\validation.csv,test=c:\work\0\test.csv,clazz=0]
 * 
 * @author Andre Busche
 *
 */
public class DatasetProviderFactory extends AbstractProxy<IDatasetProvider>{

	public static final String CONVERSION_PATTERN = "<classname>\"[\"key1\"=\"value1[,key2\"=\"value]*]";

	public DatasetProviderFactory(IDatasetProvider p) {
		super(p);
	}

	public static DatasetProviderFactory convert(Object in) {
		String source = null;
		if (in instanceof String) {
			source = (String) in;
		} else {
			source = in.toString();
		}

		String[] split = source.split("\\[", 2);
		if (split == null || split.length!=2) {
			throw new InvalidConversionFormatException(null, source, CONVERSION_PATTERN);
		}
		Class<?> c;
		try {
			c = Class.forName(split[0]);
		} catch (ClassNotFoundException e) {
			throw new InvalidConversionFormatException(null, in, CONVERSION_PATTERN);
		}
		Class<? extends IDatasetProvider> asSubclass = c.asSubclass(IDatasetProvider.class);
		IDatasetProvider instance;
		try {
			instance = asSubclass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new BootstrapException("Could not instantiate class " + asSubclass.toString(), e);
		}

		Map<String, Object> parseMap = CommandLineParser.parseMap(split[1].substring(0, split[1].length()-1));
		CommandLineParser.applyArguments(instance, parseMap);
		return new DatasetProviderFactory(instance);
	}

}
