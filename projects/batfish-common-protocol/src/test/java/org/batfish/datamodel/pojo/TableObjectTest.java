package org.batfish.datamodel.pojo;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link TableObject}. */
@RunWith(JUnit4.class)
public class TableObjectTest {

  @Test
  public void testConstructorAndGetter() {
    TableObject fo = new TableObject("name", "content");
    assertThat(fo.getName(), equalTo("name"));
    assertThat(fo.getContent(), equalTo("content"));
  }

  @Test
  public void testToString() {
    TableObject fo = new TableObject("name", "content");
    assertThat(fo.toString(), equalTo("TableObject{name=name, content=content}"));
  }

  @Test
  public void testEquals() {
    TableObject fo = new TableObject("fileName", "fileContent");
    TableObject foCopy = new TableObject("fileName", "fileContent");
    TableObject foDifferentName = new TableObject("name", "fileContent");
    TableObject foDifferentContent = new TableObject("fileName", "content");
    new EqualsTester()
        .addEqualityGroup(fo, foCopy)
        .addEqualityGroup(foDifferentName)
        .addEqualityGroup(foDifferentContent)
        .testEquals();
  }
}
