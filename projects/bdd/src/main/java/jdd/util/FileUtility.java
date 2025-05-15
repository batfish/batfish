
package jdd.util;


import java.io.File;

/**
 * Utility functions related to files are gathered here
 *
 */
public class FileUtility {

	/**
	 * returns true if the filename contains something we don't like,
	 * such as shell characters.
	 */
	public static boolean invalidFilename(String file) {
		if(file.length() == 0)
			return true;

		// lets say \xxx is only invalid if we are not using windows ??
		if(File.separatorChar != '\\' && file.indexOf('\\') != -1 )
			return false;

		final char [] badchars = "\"\';&|<>#!$".toCharArray();

		for(char badchar : badchars) {
			if(file.indexOf(badchar) != -1)
				return true;
		}
		return false;
	}

	/**
	 * Delete a file.
	 * @return true if deletion succeeded
	 */
	public static boolean delete(String filename) {
		File f = new File(filename);
		return f.delete();
	}


}