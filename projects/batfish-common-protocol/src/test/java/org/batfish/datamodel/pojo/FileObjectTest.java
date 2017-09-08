package org.batfish.datamodel.pojo;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link FileObject}. */
@RunWith(JUnit4.class)
public class FileObjectTest {

  @Test
  public void testConstructorAndGetter() {
    FileObject fo = new FileObject("name", "content");
    assertThat(fo.getName(), equalTo("name"));
    assertThat(fo.getContent(), equalTo("content"));
  }

  @Test
  public void testToString() {
    FileObject fo = new FileObject("name", "content");
    assertThat(fo.toString(), equalTo("FileObject{name=name, content=content}"));
  }

  @Test
  public void testEquals() {
    FileObject fo = new FileObject("fileName", "fileContent");
    FileObject foCopy = new FileObject("fileName", "fileContent");
    FileObject foDifferentName = new FileObject("name", "fileContent");
    FileObject foDifferentContent = new FileObject("fileName", "content");
    new EqualsTester()
        .addEqualityGroup(fo, foCopy)
        .addEqualityGroup(foDifferentName)
        .addEqualityGroup(foDifferentContent)
        .testEquals();
  }
}
