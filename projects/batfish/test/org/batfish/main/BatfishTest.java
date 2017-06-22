package org.batfish.main;

import org.batfish.common.BatfishException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link Batfish#listAllFiles(Path)}.
 */
public class BatfishTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void throwsExceptionWithSpecificType() {
        thrown.expect(BatfishException.class);
        thrown.expectMessage("Failed to walk path: ./test/notExist");
        Batfish.listAllFiles(Paths.get("./test/notExist"));
    }

    @Test
    public void testNoFileUnderPath() throws IOException {
        File emptyFolder = folder.newFolder("emptyFolder");
        List<Path> result = Batfish.listAllFiles(emptyFolder.toPath());
        assertThat(result, empty());
    }

    @Test
    public void testReadStartWithDotFile() throws IOException {
        File startWithDot = folder.newFolder("startWithDot");
        File file = new File(startWithDot + "/.cfg");
        file.getParentFile().mkdir();
        assertThat(file.createNewFile(), is(true));
        List<Path> result = Batfish.listAllFiles(startWithDot.toPath());
        assertThat(result, is(empty()));
    }

    @Test
    public void testReadUnNestedPath() throws IOException {
        File unNestedFolder= folder.newFolder("unNestedDirectory");
        List<Path> expected = new ArrayList<>();
        expected.add(new File(unNestedFolder + "/test1.cfg").toPath());
        expected.add(new File(unNestedFolder + "/test2.cfg").toPath());
        expected.add(new File(unNestedFolder + "/test3.cfg").toPath());
        for (Path path : expected) {
            path.getParent().toFile().mkdir();
            assertThat(path.toFile().createNewFile(), is(true));
        }
        List<Path> actual = Batfish.listAllFiles(unNestedFolder.toPath());
        Collections.sort(expected);
        assertThat(expected, equalTo(actual));
    }

    @Test
    public void testReadNestedPath() throws IOException {
        File nestedFolder = folder.newFolder("nestedDirectory");
        List<Path> expected = new ArrayList<>();
        expected.add(new File(nestedFolder + "/b-test.cfg").toPath());
        expected.add(new File(nestedFolder + "/d-test.cfg").toPath());
        expected.add(new File(nestedFolder + "/aDirectory/e-test.cfg").toPath());
        expected.add(new File(nestedFolder + "/eDirectory/a-test.cfg").toPath());
        expected.add(new File(nestedFolder + "/eDirectory/c-test.cfg").toPath());
        for (Path path : expected) {
            path.getParent().toFile().mkdir();
            assertThat(path.toFile().createNewFile(), is(true));
        }
        List<Path> actual = Batfish.listAllFiles(nestedFolder.toPath());
        Collections.sort(expected);
        assertThat(expected, equalTo(actual));
    }

}