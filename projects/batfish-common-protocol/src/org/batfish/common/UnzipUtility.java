package org.batfish.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This utility extracts files and directories of a standard zip file to a
 * destination directory.
 *
 * @author www.codejava.net
 *
 */
public class UnzipUtility {
   /**
    * Size of the buffer to read/write data
    */
   private static final int BUFFER_SIZE = 4096;

   /**
    * Extracts a zip entry (file entry)
    *
    * @param zipIn
    * @param filePath
    * @throws IOException
    */
   private void extractFile(ZipInputStream zipIn, String filePath)
         throws IOException {
      BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(
            filePath));
      byte[] bytesIn = new byte[BUFFER_SIZE];
      int read = 0;
      while ((read = zipIn.read(bytesIn)) != -1) {
         bos.write(bytesIn, 0, read);
      }
      bos.close();
   }

   /**
    * Extracts a zip file specified by the zipFilePath to a directory specified
    * by destDirectory (will be created if does not exists)
    *
    * @param zipFile
    * @param destDirectory
    * @throws IOException
    */
   public void unzip(File zipFile, String destDirectory) throws IOException {
      File destDir = new File(destDirectory);
      if (!destDir.exists()) {
         destDir.mkdir();
      }

      ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile));

      ZipEntry entry = zipIn.getNextEntry();

      // iterates over entries in the zip file
      while (entry != null) {
         String filePath = destDirectory + File.separator + entry.getName();
         if (!entry.isDirectory()) {
            // if the entry is a file, extracts it
            extractFile(zipIn, filePath);
         }
         else {
            // if the entry is a directory, make the directory
            File dir = new File(filePath);
            dir.mkdir();
         }
         zipIn.closeEntry();
         entry = zipIn.getNextEntry();
      }
      zipIn.close();
   }
}
