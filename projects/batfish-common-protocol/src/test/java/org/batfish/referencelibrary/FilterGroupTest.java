package org.batfish.referencelibrary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.junit.Test;

/** Tests for {@link FilterGroup} */
public class FilterGroupTest {

  @Test
  public void testJavaSerialization() throws IOException {
    FilterGroup group = new FilterGroup(null, "group");
    new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(group);
  }
}
