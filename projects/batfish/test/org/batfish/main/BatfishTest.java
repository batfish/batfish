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
        File emptyFolder= folder.newFolder("emptyFolder");
        List<Path> resultList = Batfish.listAllFiles(emptyFolder.toPath());
        assertThat(resultList, empty());
    }

    @Test
    public void testReadStartWithDotFile() throws IOException {
        File startWithDot= folder.newFolder("startWithDot");
        File file = new File(startWithDot + "/.cfg");
        file.getParentFile().mkdir();
        assertThat(file.createNewFile(), is(true));
        List<Path> resultList = Batfish.listAllFiles(startWithDot.toPath());
        assertThat(resultList, is(empty()));
    }

    @Test
    public void testReadUnNestedPath() throws IOException {
        File unNestedFolder= folder.newFolder("unNestedDirectory");
        List<Path> fileList = new ArrayList<>();
        fileList.add(new File(unNestedFolder + "/test1.cfg").toPath());
        fileList.add(new File(unNestedFolder + "/test2.cfg").toPath());
        fileList.add(new File(unNestedFolder + "/test3.cfg").toPath());
        for (Path file : fileList) {
            file.getParent().toFile().mkdir();
            assertThat(file.toFile().createNewFile(), is(true));
        }
        List<Path> resultList = Batfish.listAllFiles(unNestedFolder.toPath());
        List<Path> expectedList = new ArrayList<Path>();
        expectedList.add(Paths.get(unNestedFolder + "/test1.cfg"));
        expectedList.add(Paths.get(unNestedFolder + "/test2.cfg"));
        expectedList.add(Paths.get(unNestedFolder + "/test3.cfg"));
        Collections.sort(expectedList);
        assertThat(expectedList, equalTo(resultList));
    }

    @Test
    public void testReadNestedPath() throws IOException {
        File nestedFolder= folder.newFolder("nestedDirectory");
        List<Path> fileList = new ArrayList<>();
        fileList.add(new File(nestedFolder + "/b-test.cfg").toPath());
        fileList.add(new File(nestedFolder + "/d-test.cfg").toPath());
        fileList.add(new File(nestedFolder + "/aDirectory/e-test.cfg").toPath());
        fileList.add(new File(nestedFolder + "/eDirectory/a-test.cfg").toPath());
        fileList.add(new File(nestedFolder + "/eDirectory/c-test.cfg").toPath());
        for (Path file : fileList) {
            file.getParent().toFile().mkdir();
            assertThat(file.toFile().createNewFile(), is(true));
        }
        List<Path> resultList = Batfish.listAllFiles(nestedFolder.toPath());
        List<Path> expectedList = new ArrayList<Path>();
        expectedList.add(Paths.get(nestedFolder + "/b-test.cfg"));
        expectedList.add(Paths.get(nestedFolder + "/d-test.cfg"));
        expectedList.add(Paths.get(nestedFolder + "/aDirectory/e-test.cfg"));
        expectedList.add(Paths.get(nestedFolder + "/eDirectory/a-test.cfg"));
        expectedList.add(Paths.get(nestedFolder + "/eDirectory/c-test.cfg"));
        Collections.sort(expectedList);
        assertThat(expectedList, equalTo(resultList));
    }

}