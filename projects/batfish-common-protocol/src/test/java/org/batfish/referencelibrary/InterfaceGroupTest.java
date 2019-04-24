package org.batfish.referencelibrary;

import com.google.common.collect.ImmutableSortedSet;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.junit.Test;

/** Tests for {@link InterfaceGroup} */
public class InterfaceGroupTest {

  @Test
  public void testJavaSerialization() throws IOException {
    InterfaceGroup group = new InterfaceGroup(ImmutableSortedSet.of(), "group");
    new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(group);
  }
}
