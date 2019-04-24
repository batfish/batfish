package org.batfish.referencelibrary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.junit.Test;

/** Tests for {@link ServiceEndpoint} */
public class ServiceEndpointTest {

  @Test
  public void testJavaSerialization() throws IOException {
    ServiceEndpoint point = new ServiceEndpoint("addr", "group", "svc");
    new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(point);
  }
}
