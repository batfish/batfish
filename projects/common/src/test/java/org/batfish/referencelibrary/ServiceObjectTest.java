package org.batfish.referencelibrary;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.junit.jupiter.api.Test;

/** Tests for {@link ServiceObject} */
class ServiceObjectTest {

  @Test
  void testJavaSerialization() {
    ServiceObject object =
        new ServiceObject(IpProtocol.ANY_0_HOP_PROTOCOL, "object", new SubRange(0, 1));
    assertThat(SerializationUtils.clone(object), equalTo(object));
  }
}
