package org.batfish.main;

import static org.batfish.main.CliUtils.readAllFiles;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.batfish.common.BatfishLogger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Test of {@link CliUtils}. */
public final class CliUtilsTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();
  private static final BatfishLogger LOGGER = new BatfishLogger(BatfishLogger.LEVELSTR_INFO, false);

  @Test
  public void testReadNestedPath() throws IOException {
    Path nestedFolder = _folder.newFolder("nestedDirectory").toPath();
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
  public void testReadStartWithDotFile() throws IOException {
    Path startWithDot = _folder.newFolder("startWithDot").toPath();
    File file = startWithDot.resolve(".cfg").toFile();
    file.getParentFile().mkdir();
    assertThat(file.createNewFile(), is(true));
    Map<Path, String> result = readAllFiles(startWithDot, LOGGER);
    assertThat(result, anEmptyMap());
  }

  @Test
  public void testReadUnNestedPath() throws IOException {
    Path unNestedFolder = _folder.newFolder("unNestedDirectory").toPath();
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
  public void testReadThrowsExceptionWithSpecificType() throws IOException {
    Path nonExistPath = _folder.getRoot().toPath().resolve("nonExistent");
    _thrown.expect(IOException.class);
    _thrown.expectMessage(nonExistPath.toString());
    readAllFiles(nonExistPath, LOGGER);
  }

  @Test
  public void testNoFileUnderPath() throws IOException {
    Path emptyFolder = _folder.newFolder("emptyFolder").toPath();
    Map<Path, String> result = readAllFiles(emptyFolder, LOGGER);
    assertThat(result, anEmptyMap());
  }
}
