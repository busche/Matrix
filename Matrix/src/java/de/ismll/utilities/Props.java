package de.ismll.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Props extends Properties{

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
}
