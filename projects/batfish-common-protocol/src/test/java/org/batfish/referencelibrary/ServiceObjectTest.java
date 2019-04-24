package org.batfish.referencelibrary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.junit.Test;

/** Tests for {@link ServiceObject} */
public class ServiceObjectTest {

  @Test
  public void testJavaSerialization() throws IOException {
    ServiceObject object =
        new ServiceObject(IpProtocol.ANY_0_HOP_PROTOCOL, "object", new SubRange(0, 1));
    new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(object);
  }
}
