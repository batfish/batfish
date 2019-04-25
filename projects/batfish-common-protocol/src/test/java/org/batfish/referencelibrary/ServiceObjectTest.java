package org.batfish.referencelibrary;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.junit.Test;

/** Tests for {@link ServiceObject} */
public class ServiceObjectTest {

  @Test
  public void testJavaSerialization() throws IOException {
    ServiceObject object =
        new ServiceObject(IpProtocol.ANY_0_HOP_PROTOCOL, "object", new SubRange(0, 1));
    assertThat(SerializationUtils.clone(object), equalTo(object));
  }
}
