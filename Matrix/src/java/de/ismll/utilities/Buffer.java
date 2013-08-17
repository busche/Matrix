package de.ismll.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Buffer {

	private static final int MOUNT_POINT_COLUMN_FSTAB = 1;

	private static final String BUFFER_METADATA_FILENAME = "buffersizes";

	static Logger logger = LogManager.getLogger(Buffer.class);

	private static Map<String, Integer> bufferSizes;

	private static int default_buffer=8192;

	public static int getBufferSize(File f) {
		return getBufferSize(f, false);
	}

	public static int getBufferSize(File f, boolean measure) {

		if (!f.exists()) {
			return default_buffer;
		}
		// drive letter on windows, mount point on linux
		String domainIdentifier = getDomainIdentifier(f);
		int ret = default_buffer;
		if (domainIdentifier!=null) {
			if (!measure && bufferSizes.containsKey(domainIdentifier)) {
				ret = bufferSizes.get(domainIdentifier).intValue();
				logger.debug("Buffer size already measured. Using " + ret + ".");
			}
			else {
				int endAfter = 1024*1024*100;

				// new drive ... determine buffer size ...
				logger.info("Buffer size not yet measured for '" + domainIdentifier + "' Starting tests.");
				logger.info("It would be nice, if someone implements a stop()-method for this ... ;-)");
				logger.debug("Running " + numTimes + " repetitions");
				logger.debug("Max reading size (will break after this): " + endAfter + " bytes (50 MB)");

				long testFileSize=f.length();
				logger.debug("File-length: " + testFileSize);
				TreeMap<Integer, Long> buffersize2time = new TreeMap<Integer, Long>();

				outer:for (int bufferSize : bufferTestSizes){
					logger.info("Measuring buffer size " + bufferSize);

					long readSum = 0l;
					for (int i = 0; i < numTimes; i++){
						FileInputStream fis;
						try {
							fis = new FileInputStream(f);
						} catch (FileNotFoundException e) {
							throw new RuntimeException(e);
						}
						int numRead = 0;
						BufferedInputStream bis = new BufferedInputStream(fis, bufferSize);
						byte[] buf  = new byte[bufferSize];

						long start = System.nanoTime();
						int current=-1;
						try {
							while ((current = bis.read(buf))>=0){
								numRead+=current;
								if (numRead>=endAfter) {
									logger.debug("File size limit reached.");
									break;
								}
							}
						} catch (IOException e) {
							logger.warn(e.getMessage(), e);
							break outer;
						} finally {
							try {
								bis.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						long end = System.nanoTime();
						if (i > 0) // allow JIT-Compiler to work in first run.
							readSum += end-start;
					} // of numLoops


					buffersize2time.put(Integer.valueOf(bufferSize), Long.valueOf((readSum)));
				}

				long bestTime = Long.MAX_VALUE;
				int bestBuffer = -1;

				testFileSize = Math.min(testFileSize, endAfter);

				logger.info(" = = =  Statistics  = = = ");
				logger.info("<Buffer size>  -->  <speed>");
				for (Entry<Integer, Long> e : buffersize2time.entrySet()) {
					long time = e.getValue().longValue();
					if (time < bestTime) {
						bestTime=time;
						bestBuffer=e.getKey().intValue();
					}
					//					double mbps = (testFileSize * 1000000000) / (double) (time*1000000);
					double mbps = (testFileSize * 1000) / (double) (time*1);
					long mbps2 = (testFileSize * 1000) / (time*1);
					logger.info(e.getKey() + " --> " + String.format("%4.4s", mbps) + " MB/s");
					logger.info(e.getKey() + " --> " + mbps2 + " MB/s");
				}
				bufferSizes.put(domainIdentifier, Integer.valueOf(bestBuffer));

				ret = bestBuffer;
				saveOnExit();
			}

		} // of domain identifier != null
		else {
			logger.warn("Domain identifier for file not present. Using unoptimized buffer size " + ret + "!");
		}
		return ret;
	}

	private static boolean exitThreadStarted=false;

	private static void saveOnExit() {
		if (!exitThreadStarted) {

			Runnable r = new Runnable() {

				@Override
				public void run() {
					File targetFile = new File(System.getProperty("user.home") + "/." + BUFFER_METADATA_FILENAME);
					if (!targetFile.exists())
						try {
							targetFile.createNewFile();
						} catch (IOException e) {
							logger.warn(e.getMessage(), e);
						}
					PrintStream ps = null;
					try {
						ps = new PrintStream(targetFile);
						for (Entry<String, Integer> buffers : bufferSizes.entrySet()) {
							ps.println(buffers.getKey() + "=" + buffers.getValue());
						}
					} catch (IOException e) {
						logger.warn(e.getMessage(), e);
					} finally {
						if (ps != null)
							ps.close();
					}


				}
			};
			Thread t = new Thread(r);
			Runtime.getRuntime().addShutdownHook(t);

			exitThreadStarted=true;
		}
	}

	enum OS{
		Linux, Windows, unknown;
	}

	private static OS os_string=OS.unknown;

	private static boolean fstab_present = false;
	private static List<String> fstab;
	static {
		determineOs();
		loadBufferMetadata();
	}

	private static String getDomainIdentifier(File f) {
		logger.debug("Returning domain identifier for " + f);
		switch (os_string) {
		case Windows:
			return getUpmostParent(f).getPath();
		case Linux:
			if (!fstab_present ) {
				readFStab();
			}
			return matchFStab(f);
		case unknown:
			logger.warn("Unidentified operating system. Cannot extract domain identifier for " + f);
			break;
		}

		return null;
	}

	private static String matchFStab(File f) {
		String absoluteFile = f.getAbsolutePath();
		if (fstab.contains(absoluteFile))
			return absoluteFile;
		File parentFile = f.getParentFile();
		if (parentFile == null) {
			logger.warn("Warning: no parent folder for " + f);
			return "";
		}
		return matchFStab(parentFile);
	}

	/**
	 * reads output of /etc/fstab on linux and uses it as domain identifiers
	 */
	private static void readFStab() {
		logger.info("Reading /etc/fstab...");
		fstab = Collections.emptyList();
		Process exec;
		int waitFor;
		try {
			logger.debug("Running `cat /etc/fstab`");
			exec = Runtime.getRuntime().exec("cat /etc/fstab");
			logger.debug("Awaiting termination");
			waitFor = exec.waitFor();
			logger.debug("Exit value is " + waitFor);
			if (waitFor==0) {
				// normal termination
				fstab = new ArrayList<String>();
				logger.debug("Parsing fstab output...");
				InputStream inputStream = exec.getInputStream();
				LineNumberReader read = new LineNumberReader(new InputStreamReader(inputStream));
				String line = null;
				while ((line = read.readLine())!=null) {
					int oldLen = Integer.MAX_VALUE;
					do {
						oldLen = line.length();
						line = line.replaceAll("  ", " ");
					} while (oldLen>line.length());

					String[] split = line.split(" ");
					if (split.length<MOUNT_POINT_COLUMN_FSTAB+1)
						continue;
					String mountPoint = split[MOUNT_POINT_COLUMN_FSTAB];
					logger.info("Found mount point " + mountPoint);
					if (!mountPoint.startsWith("/"))
						continue;
					logger.debug("Using mount point " + mountPoint);
					fstab.add(mountPoint);
				}
				fstab_present=true;
			}
		} catch (InterruptedException e) {
			logger.info(e.getMessage(),e);
		} catch (IOException e) {
			logger.info(e.getMessage(),e);
		}
	}

	private static void loadBufferMetadata() {
		bufferSizes=new TreeMap<String, Integer>();
		File bufferMetadataFile = findFile(BUFFER_METADATA_FILENAME, false);
		if (bufferMetadataFile!=null) {
			// read file
			try {
				LineNumberReader read = new LineNumberReader(new FileReader(bufferMetadataFile));
				String line=null;
				while ((line = read.readLine())!=null) {
					String[] split = line.split("=");
					bufferSizes.put(split[0], Integer.valueOf(split[1]));
				}
			} catch (IOException e) {
				logger.warn(e.getMessage(), e);
			}

		}
	}

	private static File findFile(String name, boolean terminate) {
		File ret = null;
		File prop_file0 = new File(name);
		File prop_file_eclipse = new File("scripts/" + name);
		File prop_file1 = new File("src/java/" + name);
		File prop_file2 = new File(System.getProperty("user.home") + "/." + name);
		String property = System.getProperty("de.ismll.utilities.Buffer." + name);
		File prop_file3 = null;
		if (property!=null) {
			prop_file3 = new File(property);
		}
		if (prop_file0.isFile())
			ret = prop_file0;
		if (prop_file_eclipse.isFile())
			ret = prop_file_eclipse;
		if (prop_file1.isFile())
			ret = prop_file1;
		if (prop_file2.isFile())
			ret = prop_file2;
		if (prop_file3!=null && !prop_file3.isFile()) {
			logger.warn("buffer size file was specified through a System property, but could not be found directly. Searching in lookup-paths ...");
			File findFile = findFile(property, false);
			if (findFile!=null)
				prop_file3=findFile;
		}
		if (prop_file3!=null && prop_file3.isFile())
			ret = prop_file3;

		if (ret == null) {
			logger.fatal("File " + name + " was not found at the following locations:");
			logger.fatal("\t" + prop_file0);
			logger.fatal("\t" + prop_file_eclipse);
			logger.fatal("\t" + prop_file1);
			logger.fatal("\t" + prop_file2);
			if (prop_file3!=null)
				logger.fatal("\t" + prop_file3);
			if (terminate) {
				System.exit(1);
				throw new RuntimeException("No " + name + " file found. You may want to define -Dde.ismll.utilities.Buffer." + name + " system property.");
			}
		}
		logger.debug("Using " + name + " from " + ret);
		return ret;
	}

	private static File getUpmostParent(File f) {
		File parentFile = f.getParentFile();
		if (parentFile==null)
			return f;
		return getUpmostParent(parentFile);
	}

	private static void determineOs() {
		String osstring = System.getProperty("os.name").toLowerCase();
		logger.debug("Detecting operating system ... " + osstring);
		if(osstring.contains("linux")) {
			os_string=OS.Linux;
		}
		if(osstring.contains("win")) {
			os_string=OS.Windows;
		}
		if (os_string==null) {
			os_string=OS.unknown;
			logger.warn("Operating system was not identified: " + osstring + ". Please contribute and implement Buffer.determineOs() for your platform NOW!");
		}
	}

	public static int getBufferSize(URL u) {
		String protocol = u.getProtocol();
		if (protocol.equals("file")) {
			try {
				return getBufferSize(new File(u.toURI()));
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}
		// TODO: implement!
		logger.warn("Implement " + Buffer.class.getCanonicalName() + ".getBufferSize(URL) (for other protocols than <file:>)!");
		return default_buffer;
	}

	static int[] bufferTestSizes=
			new int[]{
		//512
		//,(int) Math.pow(2, 10)
		//,(int) Math.pow(2, 11)
		//,(int) Math.pow(2, 12)
		(int) Math.pow(2, 13)
		,(int) Math.pow(2, 14)
		,(int) Math.pow(2, 15)
		,
		(int) Math.pow(2, 16)
		,(int) Math.pow(2, 17)
		,(int) Math.pow(2, 18)
		,(int) Math.pow(2, 19)
		,(int) Math.pow(2, 20)
	};
	static int numTimes=20;

	//	static void determineBuffersize(){
	//		File remoteFile = null;
	////		remoteFile = new File("C:/Users/John/en_windows_xp_professional_with_service_pack_3_x86_cd_x14-80428.iso");
	//		remoteFile = new File("C:/Users/John/rock_n_roll.wav");
	////		remoteFile = new File("S:/s1/JGR0NE~9.BIN");
	//		long testFileSize=-1; // 30 MB
	//		if (remoteFile==null) {
	//			testFileSize=1024*1024*100; // 30 MB
	//				try {
	//				remoteFile = File.createTempFile("test", "file");
	//				remoteFile.deleteOnExit();
	//			} catch (IOException e) {
	//				throw new RuntimeException(e);
	//			}
	//			System.out.println("Filling test file " + remoteFile + "...");
	//			PrintStream ps;
	//			try {
	//				ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(remoteFile), 65536));
	//			} catch (FileNotFoundException e) {
	//				throw new RuntimeException(e);
	//			}
	//			for (int i = 0; i < testFileSize; i++)
	//				ps.print("t");
	//			ps.close();
	//
	//		} else {
	//			testFileSize=remoteFile.length();
	//		}
	//		System.out.println("File size: " + testFileSize);
	//
	//		TreeMap<Long, Integer> time2Buffersize = new TreeMap<Long, Integer>();
	//		TreeMap<Integer, Long> buffersize2time = new TreeMap<Integer, Long>();
	//
	//		for (int bufferSize : bufferTestSizes){
	//			System.out.println("Buffer size " + bufferSize);
	//
	//			long start = System.nanoTime();
	//			for (int i = 0; i < numTimes; i++){
	//				FileInputStream fis;
	//				try {
	//					fis = new FileInputStream(remoteFile);
	//				} catch (FileNotFoundException e) {
	//					throw new RuntimeException(e);
	//				}
	//				BufferedInputStream bis = new BufferedInputStream(fis, bufferSize);
	//				byte[] buf  = new byte[bufferSize];
	//				int current=-1;
	//				try {
	//					while ((current = bis.read(buf))>=0){
	//
	//					}
	//				} catch (IOException e) {
	//					throw new RuntimeException(e);
	//				}
	//				try {
	//					bis.close();
	//				} catch (IOException e) {
	//					e.printStackTrace();
	//				}
	//			}
	//
	//			long end = System.nanoTime();
	//			time2Buffersize.put(Long.valueOf((end-start)), Integer.valueOf(bufferSize));
	//			buffersize2time.put(Integer.valueOf(bufferSize), Long.valueOf((end-start)));
	//		}
	//
	//		for (Entry<Integer, Long> e : buffersize2time.entrySet()) {
	//			long time = e.getValue().longValue();
	//			double mbps = (testFileSize * 1000000000) / (double) (time*1000000);
	//			System.out.println(e.getKey() + " --> " + String.format("%4.4s", mbps) + " MB/s");
	//		}
	//		System.out.println(time2Buffersize.toString());
	//
	//	}


	//	private static void read1(int numTimes, File tmpFile, int bufferSize) {
	//		for (int i = 0; i < numTimes; i++){
	//			FileInputStream fis;
	//			try {
	//				fis = new FileInputStream(tmpFile);
	//			} catch (FileNotFoundException e) {
	//				throw new RuntimeException(e);
	//			}
	//			BufferedInputStream bis = new BufferedInputStream(fis, bufferSize);
	//
	//			int current=-1;
	//			try {
	//				while ((current = bis.read())>=0){
	//
	//				}
	//			} catch (IOException e) {
	//				throw new RuntimeException(e);
	//			}
	//			try {
	//				bis.close();
	//			} catch (IOException e) {
	//				e.printStackTrace();
	//			}
	//		}
	//	}


	public static void main(String[] args) {
		if (args.length<1) {
			System.err.println("Need exactly one parameter: the measurement file!");
			System.exit(1);
		}
		boolean justMeasure=false;
		if (args.length>1) {
			int pos = 1;
			if ("-m".equals(args[pos])){
				justMeasure=true;
			}
		}
		// TODO: implement command line switch to override auto-load of buffers.
		System.out.println(Buffer.getBufferSize(new File(args[0]), justMeasure));
	}

	public static InputStream newInputStream(File file) throws FileNotFoundException {
		if (logger.isDebugEnabled())
			logger.debug("Returning new Input Stream for " + file);
		int bufferSize = getBufferSize(file);
		BufferedInputStream ret = new BufferedInputStream(new FileInputStream(file), bufferSize);
		return ret;
	}

	public static OutputStream newOutputStream(File file, boolean append) throws FileNotFoundException {
		if (logger.isDebugEnabled())
			logger.debug("Returning new Output Stream for " + file);
		int bufferSize = getBufferSize(file);
		BufferedOutputStream ret = new BufferedOutputStream(new FileOutputStream(file, append), bufferSize);
		return ret;
	}

	public static OutputStream newOutputStream(File file) throws FileNotFoundException {
		return newOutputStream(file, false);
	}

}
