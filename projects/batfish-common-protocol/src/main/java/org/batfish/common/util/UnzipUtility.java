package org.batfish.common.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.batfish.common.BatfishException;

/**
 * This utility extracts files and directories of a standard zip file to a destination directory.
 *
 * @author www.codejava.net with minor local changes tagged with :ratul:
 */
public class UnzipUtility {
  /** Size of the buffer to read/write data */
  private static final int BUFFER_SIZE = 4096;

  public static void unzip(Path zipFile, Path dstDir) {
    new UnzipUtility().unzipHelper(zipFile, dstDir);
  }

  /**
   * Extracts a zip entry (file entry)
   *
   * @param zipIn The zip input stream providing the file data
   * @param filePath The path to write the output file
   */
  private void extractFile(ZipInputStream zipIn, String filePath) {
    try (FileOutputStream fos = new FileOutputStream(filePath)) {
      try (BufferedOutputStream bos = new BufferedOutputStream(fos)) {
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        try {
          while ((read = zipIn.read(bytesIn)) != -1) {
            try {
              bos.write(bytesIn, 0, read);
            } catch (IOException e) {
              throw new BatfishException(
                  "Error writing to output file: '" + filePath + "' for current zipped file", e);
            }
          }
        } catch (IOException e) {
          throw new BatfishException("Error reading from zip stream", e);
        }
      }
    } catch (FileNotFoundException e) {
      throw new BatfishException(
          "Could not open output file in which to extract current zipped file: '" + filePath + "'",
          e);
    } catch (IOException e) {
      throw new BatfishException(
          "Could not close output file for current zipped file: '" + filePath + "'", e);
    }
  }

  /**
   * Extracts a zip file specified by the zipFilePath to a directory specified by destDirectory
   * (will be created if does not exists)
   *
   * @param zipFile The path to the input zip file
   * @param destDirectory The output directory in which to extract the zip
   */
  private void unzipHelper(Path zipFile, Path destDirectory) {
    try {
      // :ratul:
      // this lets us check if the zip file is proper
      // for bad zip files this will throw an exception
      ZipFile zipTest = new ZipFile(zipFile.toFile());
      zipTest.close();

      if (!Files.exists(destDirectory)) {
        destDirectory.toFile().mkdirs();
      }

      try (FileInputStream fis = new FileInputStream(zipFile.toFile())) {
        try (ZipInputStream zipIn = new ZipInputStream(fis)) {
          ZipEntry entry = zipIn.getNextEntry();

          // iterates over entries in the zip file
          while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
              // if the entry is a file, extracts it
              extractFile(zipIn, filePath);
            } else {
              // if the entry is a directory, make the directory
              File dir = new File(filePath);
              dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
          }
        }
      }
    } catch (IOException e) {
      throw new BatfishException(
          "Could not unzip: '" + zipFile + "' into: '" + destDirectory + "'", e);
    }
  }
}
