package org.batfish.common.util;

import com.google.common.io.Closer;
import com.google.errorprone.annotations.MustBeClosed;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;

// code from
// http://stackoverflow.com/questions/740375/directories-in-a-zip-file-when-using-java-util-zip-zipoutputstream
// minor local changes -- search for ratul

public class ZipUtility {
  /*
   * recursively add files to the zip files
   */
  private static void addFileToZip(String path, String srcFile, ZipOutputStream zip, boolean flag)
      throws Exception {
    /*
     * create the file object for inputs
     */
    File folder = new File(srcFile);

    /*
     * if the folder is empty add empty folder to the Zip file
     */
    if (flag) {
      zip.putNextEntry(new ZipEntry(path + "/" + folder.getName() + "/"));
    } else {
      /*
       * if the current name is directory, recursively traverse it to get
       * the files
       */
      if (folder.isDirectory()) {
        /*
         * if folder is not empty
         */
        addFolderToZip(path, srcFile, zip);
      } else {
        /*
         * write the file to the output
         */
        byte[] buf = new byte[1024];
        int len;
        try (FileInputStream in = new FileInputStream(srcFile)) {
          zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
          while ((len = in.read(buf)) > 0) {
            /*
             * Write the Result
             */
            zip.write(buf, 0, len);
          }
        }
      }
    }
  }

  /*
   * add folder to the zip file
   */
  private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip)
      throws Exception {
    File folder = new File(srcFolder);

    /*
     * check the empty folder
     */

    // ratul comments the lines below
    // if (folder.list().length == 0) {
    // System.out.println(folder.getName());
    addFileToZip(path, srcFolder, zip, true);
    // } else {
    /*
     * list the files in the folder
     */
    for (String fileName : folder.list()) {
      if (path.equals("")) {
        addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip, false);
      } else {
        addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip, false);
      }
    }
    // }
  }

  /** Zip {@code srcFolder} and write to {@code destZipFile} */
  @SuppressWarnings("PMD.CloseResource") // PMD does not understand Closer
  public static void zipFiles(Path srcFolder, Path destZipFile) {
    try (Closer closer = Closer.create()) {
      OutputStream fos = closer.register(Files.newOutputStream(destZipFile));
      zipFolder(srcFolder.toString(), fos, closer);
    } catch (Exception e) {
      throw new BatfishException(
          "Could not zip folder: '" + srcFolder + "' into: '" + destZipFile + "'", e);
    }
  }

  /** Zip {@code srcFolder} in memory and return input stream from which zip may be read. */
  @MustBeClosed
  public static @Nonnull InputStream zipFilesToInputStream(Path srcFolder) {
    ByteArrayOutputStream baos;
    try (Closer closer = Closer.create()) {
      baos = closer.register(new ByteArrayOutputStream());
      zipFolder(srcFolder.toString(), baos, closer);
    } catch (Exception e) {
      throw new BatfishException("Could not zip folder: '" + srcFolder + "'", e);
    }
    return new ByteArrayInputStream(baos.toByteArray());
  }

  /*
   * zip the folders
   */
  @SuppressWarnings("PMD.CloseResource") // PMD does not understand Closer
  private static void zipFolder(String srcFolder, OutputStream outputStream, Closer closer)
      throws Exception {
    ZipOutputStream zip = null;
    /*
     * create the output stream to zip file result
     */
    zip = closer.register(new ZipOutputStream(outputStream));
    /*
     * add the folder to the zip
     */
    addFolderToZip("", srcFolder, zip);
  }
}
