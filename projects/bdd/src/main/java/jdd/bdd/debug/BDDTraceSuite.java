
package jdd.bdd.debug;

import jdd.util.JDDConsole;
import jdd.util.Options;
import jdd.util.jre.JREInfo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class opens and runs a suite of BDD traces from a zip file.
 * <p>A trace file is assumed to have the '.trace' posfix. Any file with the 'README' posfix
 * is assumed to be an information file and is dumped to stdout.
 *
 */

public class BDDTraceSuite {

	/**
	 * filename is the name of zip file, initial_size is the suggested "base" for
	 * deciding the initial of the nodetable. if initial_size is -1, it is ignored
	 * and BDDTrace's default value is used
	 */
	public BDDTraceSuite(String filename, int initial_size) {

		try {
			InputStream is = new FileInputStream(filename);
			ZipInputStream zis = new ZipInputStream(is);

			JDDConsole.out.printf("\n***** [%s] *****\n", filename);
			JREInfo.show();

			ZipEntry ze = zis.getNextEntry();
			while(zis.available()!= 0) {

				String name = ze.getName();

				if(name.endsWith(".trace")) runTrace(name, zis, initial_size);
				else if(name.endsWith("README"))  showFile(name,zis);

				zis.closeEntry();
				ze = zis.getNextEntry();
			}
			zis.close();
			is.close();
		} catch(IOException exx) {
			JDDConsole.out.printf("FAILED: %s\n", exx.getMessage());
			exx.printStackTrace();
			System.exit(20);
		}
	}

	private void runTrace(String name, InputStream is, int size) {
		// enable verbose temporary!
		boolean save = Options.verbose;
		Options.verbose = true;
		try {
			if(size == -1) new BDDTrace(name, is);
			else new BDDTrace(name, is, size);
		} catch(Exception ex) {
			JDDConsole.out.printf("FAILED when running %s: %s\n", name, ex.getMessage());
			ex.printStackTrace();
		}

		Options.verbose = save;		// set back verbose to its old value
		// let's cleanup, so we dont affect the next run so much:
		for(int i = 0; i < 6; i++) System.gc();

		try { Thread.sleep(5000);  } catch(Exception ignored) { } // calm down!
	}

	private void showFile(String name, InputStream is) throws IOException {
		JDDConsole.out.printf("File %s\n", name);
		byte [] buffer = new byte[10240];

		for(;;) {
			int i = is.read(buffer, 0, buffer.length);
			if(i <= 0) return;
			JDDConsole.out.printf("%s\n", new String(buffer, 0, i));
		}
	}

	// -------------------------------------------------------------

	public static void main(String [] args) {
		if( args.length == 1) new BDDTraceSuite(args[0], -1);
		else if(args.length == 2) new BDDTraceSuite(args[0], Integer.parseInt(args[1]));
		else System.err.println("Usage: java BDDTraceSuite <trace-suite.zip> [initial size _base_]");
	}
}
