package org.batfish.referencelibrary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.junit.Test;

/** Tests for {@link ServiceObjectGroup} */
public class ServiceObjectGroupTest {

  @Test
  public void testJavaSerialization() throws IOException {
    ServiceObjectGroup group = new ServiceObjectGroup("group", null);
    new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(group);
  }
}
