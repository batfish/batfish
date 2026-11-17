package org.batfish.common.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests of {@link UnzipUtility}. */
class UnzipUtilityTest {
  @TempDir public File _folder;

  @Test
  void testUnzipEmptyFile() throws IOException {
    File empty = newFile(_folder, "empty");
    try (FileOutputStream fos = new FileOutputStream(empty);
        ZipOutputStream ignored = new ZipOutputStream(fos)) {
      // Opening and closing the stream produces an empty zip file.
    }

    File dest = newFolder(_folder, "dest");
    UnzipUtility.unzip(empty.toPath(), dest.toPath());
    assertThat("output folder exists", dest.exists(), is(true));
    assertThat("output folder is a directory", dest.isDirectory(), is(true));
    assertThat("output folder is empty", dest.list(), equalTo(new String[0]));
  }

  @Test
  void testUnzipDirectoriesMissing() throws IOException {
    byte[] contents = "contents of file.txt".getBytes();
    File directoriesMissing = newFile(_folder, "directories_missing");
    try (FileOutputStream fos = new FileOutputStream(directoriesMissing);
        ZipOutputStream out = new ZipOutputStream(fos)) {
      out.putNextEntry(new ZipEntry("missing_dir/child_dir/file.txt"));
      out.write(contents);
    }

    File dest = newFolder(_folder, "dest");
    UnzipUtility.unzip(directoriesMissing.toPath(), dest.toPath());

    File file =
        dest.toPath().resolve("missing_dir").resolve("child_dir").resolve("file.txt").toFile();
    assertThat("output file exists", file.exists(), is(true));
    assertThat("output file is a file", file.isDirectory(), is(false));
    assertThat("output file contents", Files.readAllBytes(file.toPath()), equalTo(contents));
  }

  @Test
  void testUnzipPathViolation() throws IOException {
    File pathViolation = newFile(_folder, "pathViolation");
    try (FileOutputStream fos = new FileOutputStream(pathViolation);
        ZipOutputStream out = new ZipOutputStream(fos)) {
      out.putNextEntry(new ZipEntry("../../"));
      out.putNextEntry(new ZipEntry("../../file.txt"));
      out.write("contents".getBytes());
    }

    File dest = newFolder(_folder, "dest");
    assertThrows(
        IOException.class, () -> UnzipUtility.unzip(pathViolation.toPath(), dest.toPath()));
  }

  /**
   * Test that unzipping does not assume a specific order of zip entries. In particular that entries
   * for parent directories appear before file entries.
   */
  @Test
  void testUnzipEntriesOutOfOrder() throws IOException {
    byte[] contents = "contents of file.txt".getBytes();
    File notOrdered = newFile(_folder, "not_ordered");
    try (FileOutputStream fos = new FileOutputStream(notOrdered);
        ZipOutputStream out = new ZipOutputStream(fos)) {
      out.putNextEntry(new ZipEntry("dir/file.txt"));
      out.write(contents);
      out.putNextEntry(new ZipEntry("dir/"));
    }

    File dest = newFolder(_folder, "dest");
    // Don't crash
    UnzipUtility.unzip(notOrdered.toPath(), dest.toPath());
  }

  /**
   * Test that a corrupt entry is rejected. Reading through a {@link java.util.zip.ZipFile} does not
   * check an entry's CRC on its own.
   */
  @Test
  void testUnzipCorruptEntry() throws IOException {
    StringBuilder contents = new StringBuilder();
    Random random = new Random(1);
    for (int i = 0; i < 200000; i++) {
      contents.append((char) ('a' + random.nextInt(26)));
    }
    File corrupt = newFile(_folder, "corrupt");
    try (FileOutputStream fos = new FileOutputStream(corrupt);
        ZipOutputStream out = new ZipOutputStream(fos)) {
      out.putNextEntry(new ZipEntry("dir/file.txt"));
      out.write(contents.toString().getBytes(UTF_8));
    }
    // Corrupt the entry's data, leaving the central directory at the end of the file intact.
    byte[] zip = Files.readAllBytes(corrupt.toPath());
    zip[zip.length / 2] ^= 0x5A;
    Files.write(corrupt.toPath(), zip);

    File dest = newFolder(_folder, "dest");
    Throwable exception =
        assertThrows(IOException.class, () -> UnzipUtility.unzip(corrupt.toPath(), dest.toPath()));
    assertThat(exception.getMessage(), containsString("Invalid CRC for zip entry 'dir/file.txt'"));
  }

  /** Test that unzipping still throws IO exception on legitimate file system access errors */
  @Test
  void testUnzipThrowsOnMkdirErrors() {
    File dest;
    File test_zip;
    try {
      test_zip = newFile(_folder, "test_zip");
      try (FileOutputStream fos = new FileOutputStream(test_zip);
          ZipOutputStream out = new ZipOutputStream(fos)) {
        out.putNextEntry(new ZipEntry("dir/"));
      }
      dest = newFolder(_folder, "dest");
      dest.setWritable(false);
    } catch (IOException e) {
      throw new AssertionError("test failed with IOException for the wrong reason");
    }
    try {
      UnzipUtility.unzip(test_zip.toPath(), dest.toPath());
      fail(); // should not reach
    } catch (IOException e) {
      assertThat(e.getMessage(), containsString("Unable to make directory"));
    }
  }

  private static File newFile(File parent, String child) throws IOException {
    File result = new File(parent, child);
    result.createNewFile();
    return result;
  }

  private static File newFolder(File root, String... subDirs) throws IOException {
    String subFolder = String.join("/", subDirs);
    File result = new File(root, subFolder);
    if (!result.mkdirs()) {
      throw new IOException("Couldn't create folders " + root);
    }
    return result;
  }
}
