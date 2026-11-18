package org.batfish.main;

import static org.batfish.main.CliUtils.readAllFiles;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.batfish.common.BatfishLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Test of {@link CliUtils}. */
final class CliUtilsTest {

  @TempDir public File _folder;
  private static final BatfishLogger LOGGER = new BatfishLogger(BatfishLogger.LEVELSTR_INFO, false);

  @Test
  void testReadNestedPath() throws IOException {
    Path nestedFolder = newFolder(_folder, "nestedDirectory").toPath();
    Set<Path> expected = new HashSet<>();
    expected.add(nestedFolder.resolve("b-test.cfg"));
    expected.add(nestedFolder.resolve("d-test.cfg"));
    expected.add(nestedFolder.resolve("aDirectory").resolve("e-test.cfg"));
    expected.add(nestedFolder.resolve("eDirectory").resolve("a-test.cfg"));
    expected.add(nestedFolder.resolve("eDirectory").resolve("c-test.cfg"));
    for (Path path : expected) {
      path.getParent().toFile().mkdir();
      assertThat(path.toFile().createNewFile(), is(true));
    }
    Set<Path> actual = readAllFiles(nestedFolder, LOGGER).keySet();
    assertThat(expected, equalTo(actual));
  }

  @Test
  void testReadStartWithDotFile() throws IOException {
    Path startWithDot = newFolder(_folder, "startWithDot").toPath();
    File file = startWithDot.resolve(".cfg").toFile();
    file.getParentFile().mkdir();
    assertThat(file.createNewFile(), is(true));
    Map<Path, String> result = readAllFiles(startWithDot, LOGGER);
    assertThat(result, anEmptyMap());
  }

  @Test
  void testReadUnNestedPath() throws IOException {
    Path unNestedFolder = newFolder(_folder, "unNestedDirectory").toPath();
    Set<Path> expected = new HashSet<>();
    expected.add(unNestedFolder.resolve("test1.cfg"));
    expected.add(unNestedFolder.resolve("test2.cfg"));
    expected.add(unNestedFolder.resolve("test3.cfg"));
    for (Path path : expected) {
      path.getParent().toFile().mkdir();
      assertThat(path.toFile().createNewFile(), is(true));
    }
    Set<Path> actual = readAllFiles(unNestedFolder, LOGGER).keySet();
    assertThat(expected, equalTo(actual));
  }

  @Test
  void testReadThrowsExceptionWithSpecificType() {
    Path nonExistPath = _folder.toPath().resolve("nonExistent");
    Throwable exception = assertThrows(IOException.class, () -> readAllFiles(nonExistPath, LOGGER));
    assertThat(exception.getMessage(), containsString(nonExistPath.toString()));
  }

  @Test
  void testNoFileUnderPath() throws IOException {
    Path emptyFolder = newFolder(_folder, "emptyFolder").toPath();
    Map<Path, String> result = readAllFiles(emptyFolder, LOGGER);
    assertThat(result, anEmptyMap());
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
