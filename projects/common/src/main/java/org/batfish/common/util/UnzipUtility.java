package org.batfish.common.util;

import static com.google.common.io.MoreFiles.createParentDirectories;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
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
  private static void extractFile(InputStream zipIn, Path filePath) {
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
    // A stream can only be inflated in order on one thread; from a file the entries can be
    // inflated in parallel, which for a snapshot of thousands of files is several times faster.
    Path spooled = Files.createTempFile("unzip", ".zip");
    try {
      Files.copy(zipStream, spooled, StandardCopyOption.REPLACE_EXISTING);
      unzipFile(spooled, destDirectory);
    } finally {
      Files.deleteIfExists(spooled);
    }
  }

  private static void unzipFile(Path zipFile, Path destDirectory) throws IOException {
    try (ZipFile zip = new ZipFile(zipFile.toFile())) {
      // Directories first, so that files never race their own directory entries.
      List<? extends ZipEntry> entries = zip.stream().collect(Collectors.toList());
      for (ZipEntry entry : entries) {
        if (entry.isDirectory()) {
          Path outputPath = outputPath(entry, destDirectory);
          // Make the directory, including parent dirs.
          if (!outputPath.toFile().exists()) {
            if (!outputPath.toFile().mkdirs()) {
              throw new IOException("Unable to make directory " + outputPath);
            }
          }
        }
      }
      entries.parallelStream()
          .filter(entry -> !entry.isDirectory())
          .forEach(
              entry -> {
                try {
                  Path outputPath = outputPath(entry, destDirectory);
                  // Make sure parent directories exist, in case the zip does not contain dir
                  // entries
                  createParentDirectories(outputPath);
                  try (InputStream in = zip.getInputStream(entry)) {
                    extractFile(in, outputPath);
                  }
                } catch (IOException e) {
                  throw new UncheckedIOException(e);
                }
              });
    } catch (UncheckedIOException e) {
      throw e.getCause();
    }
  }

  /**
   * The path {@code entry} extracts to. The entry name may start with '/', so a little magic
   * ensures it is applied relatively against {@code destDirectory}.
   */
  private static Path outputPath(ZipEntry entry, Path destDirectory) throws IOException {
    return validatePath(
        new File(destDirectory.toFile(), new File(entry.getName()).getPath()).toPath(),
        destDirectory);
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

    if (!destDirectory.toFile().isDirectory()) {
      throw new IOException(
          String.format(
              "Output directory does not exist or is not a directory: %s", destDirectory));
    }
    unzipFile(zipFile, destDirectory);
  }

  // Prevent instantiation of utility class.
  private UnzipUtility() {}
}
