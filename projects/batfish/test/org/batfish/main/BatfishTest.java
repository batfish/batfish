package org.batfish.main;

import org.batfish.common.BatfishException;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class BatfishTest {

    @Test(expected = BatfishException.class)
    public void testNotExistingPath() {
        List<Path> result = Batfish.listAllFiles(Paths.get("./test/notExist"));
    }


    @Test
    public void testNoFileUnderPath() {
        List<Path> result = Batfish.listAllFiles(Paths.get("./test/noFile"));
        Assert.assertEquals(0, result.size());
    }


    @Test
    public void testReadUnNestedPath() {
        List<Path> result = Batfish.listAllFiles(Paths.get("./test/unNestedDirectory"));
        Assert.assertEquals(3, result.size());
    }


    @Test
    public void testReadStartWithDotFile() {
        List<Path> result = Batfish.listAllFiles(Paths.get("./test/startWithDot"));
        Assert.assertEquals(0, result.size());
    }


    @Test
    public void testFileNameRead() {
        List<Path> result = Batfish.listAllFiles(Paths.get("./test/unNestedDirectory"));
        Assert.assertEquals("./test/unNestedDirectory/test1.cfg", result.get(0).toString());
        Assert.assertEquals("./test/unNestedDirectory/test2.cfg", result.get(1).toString());
        Assert.assertEquals("./test/unNestedDirectory/test3.cfg", result.get(2).toString());
    }


    @Test
    public void testReadNestedPath() {
        List<Path> result = Batfish.listAllFiles(Paths.get("./test/nestedDirectory"));
        Assert.assertEquals(5, result.size());
    }


    @Test
    public void testOrderInNestedPath() {
        List<Path> result = Batfish.listAllFiles(Paths.get("./test/nestedDirectory"));
        Assert.assertEquals("./test/nestedDirectory/aStart/e-test.cfg", result.get(0).toString());
        Assert.assertEquals("./test/nestedDirectory/b-test.cfg", result.get(1).toString());
        Assert.assertEquals("./test/nestedDirectory/d-test.cfg", result.get(2).toString());
        Assert.assertEquals("./test/nestedDirectory/innerDirectory/a-test.cfg", result.get(3).toString());
        Assert.assertEquals("./test/nestedDirectory/innerDirectory/c-test.cfg", result.get(4).toString());
    }


}