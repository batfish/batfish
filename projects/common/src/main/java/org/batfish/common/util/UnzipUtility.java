package org.batfish.common.util;

import static com.google.common.io.MoreFiles.createParentDirectories;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.batfish.common.BatfishException;

/**
 * This utility extracts files and directories of a standard zip file to a destination directory.
 *
 * @author www.codejava.net with minor local changes tagged with :ratul:
 */
public final class UnzipUtility {
  /**
   * Extracts a zip entry (file entry)
   *
   * @param zipIn The zip input stream providing the file data
   * @param filePath The path to write the output file
   */
  private static void extractFile(ZipInputStream zipIn, Path filePath) {
    try {
      Files.copy(zipIn, filePath, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new BatfishException("Error unzipping to output file: '" + filePath + "'", e);
    }
  }

  /**
   * Asserts that the given {@code outputPath} is actually inside of the given {@code enclosingDir}.
   */
  private static Path validatePath(Path outputPath, Path enclosingDir) throws IOException {
    File canonicalFile = outputPath.toFile().getCanonicalFile();
    File canonicalDir = enclosingDir.toFile().getCanonicalFile();

    if (canonicalFile.getCanonicalPath().startsWith(canonicalDir.getCanonicalPath())) {
      return canonicalFile.toPath();
    } else {
      throw new IOException(
          String.format(
              "Output file %s is outside extraction target directory %s.",
              outputPath, enclosingDir));
    }
  }

  /**
   * Extracts {@code zipStream} to a directory specified by {@code destDirectory}. The caller is
   * responsible for ensuring that the directory exists and is empty. If the directory is not empty,
   * behavior of this function is undefined.
   *
   * @throws IOException if {@code destDirectory} does not exist or there is any other errror
   */
  public static void unzip(InputStream zipStream, Path destDirectory) throws IOException {
    if (!destDirectory.toFile().isDirectory()) {
      throw new IOException(
          String.format(
              "Output directory does not exist or is not a directory: %s", destDirectory));
    }
    try (ZipInputStream zipIn = new ZipInputStream(zipStream)) {
      for (ZipEntry entry = zipIn.getNextEntry(); entry != null; entry = zipIn.getNextEntry()) {
        // entry may start with '/', so we do a little magic to ensure it is applied relatively
        // against destDirectory
        Path outputPath =
            validatePath(
                new File(destDirectory.toFile(), new File(entry.getName()).getPath()).toPath(),
                destDirectory);
        if (entry.isDirectory()) {
          // Make the directory, including parent dirs.
          if (!outputPath.toFile().exists()) {
            if (!outputPath.toFile().mkdirs()) {
              throw new IOException("Unable to make directory " + outputPath);
            }
          }
        } else {
          // Make sure parent directories exist, in case the zip does not contain dir entries
          createParentDirectories(outputPath);
          // Extract the file.
          extractFile(zipIn, outputPath);
        }
        zipIn.closeEntry();
      }
    }
  }

  /**
   * Extracts {@code zipFile} to a directory specified by {@code destDirectory}. The caller is
   * responsible for ensuring that the directory exists and is empty. If the directory is not empty,
   * behavior of this function is undefined.
   *
   * @throws IOException if {@code destDirectory} does not exist or there is any other errror
   */
  public static void unzip(Path zipFile, Path destDirectory) throws IOException {
    // :ratul:
    // this lets us check if the zip file is proper
    // for bad zip files this will throw an exception
    try (ZipFile zipTest = new ZipFile(zipFile.toFile())) {
      assert zipTest != null; // suppress unused warning
    }

    try (FileInputStream fis = new FileInputStream(zipFile.toFile())) {
      unzip(fis, destDirectory);
    }
  }

  // Prevent instantiation of utility class.
  private UnzipUtility() {}
}
