package org.batfish.main;

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

/**
 * Tests for {@link Batfish}.
 */
public class BatfishTest {

   @Rule
   public TemporaryFolder folder = new TemporaryFolder();

   @Rule
   public ExpectedException thrown = ExpectedException.none();

   @Test
   public void throwsExceptionWithSpecificType() {
      Path nonExistPath = folder.getRoot().toPath().resolve("nonExistent");
      thrown.expect(BatfishException.class);
      thrown.expectMessage("Failed to walk path: " + nonExistPath);
      Batfish.listAllFiles(nonExistPath);
   }

   @Test
   public void testNoFileUnderPath() throws IOException {
      Path emptyFolder = folder.newFolder("emptyFolder").toPath();
      List<Path> result = Batfish.listAllFiles(emptyFolder);
      assertThat(result, empty());
   }

   @Test
   public void testReadStartWithDotFile() throws IOException {
      Path startWithDot = folder.newFolder("startWithDot").toPath();
      File file = startWithDot.resolve(".cfg").toFile();
      file.getParentFile().mkdir();
      assertThat(file.createNewFile(), is(true));
      List<Path> result = Batfish.listAllFiles(startWithDot);
      assertThat(result, is(empty()));
   }

   @Test
   public void testReadUnNestedPath() throws IOException {
      Path unNestedFolder = folder.newFolder("unNestedDirectory").toPath();
      List<Path> expected = new ArrayList<>();
      expected.add(unNestedFolder.resolve("test1.cfg"));
      expected.add(unNestedFolder.resolve("test2.cfg"));
      expected.add(unNestedFolder.resolve("test3.cfg"));
      for (Path path : expected) {
         path.getParent().toFile().mkdir();
         assertThat(path.toFile().createNewFile(), is(true));
      }
      List<Path> actual = Batfish.listAllFiles(unNestedFolder);
      Collections.sort(expected);
      assertThat(expected, equalTo(actual));
   }

   @Test
   public void testReadNestedPath() throws IOException {
      Path nestedFolder = folder.newFolder("nestedDirectory").toPath();
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
      List<Path> actual = Batfish.listAllFiles(nestedFolder);
      Collections.sort(expected);
      assertThat(expected, equalTo(actual));
   }

}