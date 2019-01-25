package org.batfish.main;

import static org.batfish.main.BatfishParsing.listAllFiles;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.batfish.common.BatfishException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests of {@link BatfishParsing}. */
public class BatfishParsingTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testNoFileUnderPath() throws IOException {
    Path emptyFolder = _folder.newFolder("emptyFolder").toPath();
    List<Path> result = listAllFiles(emptyFolder);
    assertThat(result, empty());
  }

  @Test
  public void testReadNestedPath() throws IOException {
    Path nestedFolder = _folder.newFolder("nestedDirectory").toPath();
    List<Path> expected = new ArrayList<>();
    expected.add(nestedFolder.resolve("b-test.cfg"));
    expected.add(nestedFolder.resolve("d-test.cfg"));
    expected.add(nestedFolder.resolve("aDirectory").resolve("e-test.cfg"));
    expected.add(nestedFolder.resolve("eDirectory").resolve("a-test.cfg"));
    expected.add(nestedFolder.resolve("eDirectory").resolve("c-test.cfg"));
    for (Path path : expected) {
      path.getParent().toFile().mkdir();
      assertThat(path.toFile().createNewFile(), is(true));
    }
    List<Path> actual = listAllFiles(nestedFolder);
    Collections.sort(expected);
    assertThat(expected, equalTo(actual));
  }

  @Test
  public void testReadStartWithDotFile() throws IOException {
    Path startWithDot = _folder.newFolder("startWithDot").toPath();
    File file = startWithDot.resolve(".cfg").toFile();
    file.getParentFile().mkdir();
    assertThat(file.createNewFile(), is(true));
    List<Path> result = listAllFiles(startWithDot);
    assertThat(result, is(empty()));
  }

  @Test
  public void testReadUnNestedPath() throws IOException {
    Path unNestedFolder = _folder.newFolder("unNestedDirectory").toPath();
    List<Path> expected = new ArrayList<>();
    expected.add(unNestedFolder.resolve("test1.cfg"));
    expected.add(unNestedFolder.resolve("test2.cfg"));
    expected.add(unNestedFolder.resolve("test3.cfg"));
    for (Path path : expected) {
      path.getParent().toFile().mkdir();
      assertThat(path.toFile().createNewFile(), is(true));
    }
    List<Path> actual = listAllFiles(unNestedFolder);
    Collections.sort(expected);
    assertThat(expected, equalTo(actual));
  }

  @Test
  public void throwsExceptionWithSpecificType() {
    Path nonExistPath = _folder.getRoot().toPath().resolve("nonExistent");
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage("Failed to walk path: " + nonExistPath);
    listAllFiles(nonExistPath);
  }
}
