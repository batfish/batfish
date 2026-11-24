package org.batfish.referencelibrary;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/** Tests for {@link ServiceEndpoint} */
class ServiceEndpointTest {

  @Test
  void testJavaSerialization() {
    ServiceEndpoint point = new ServiceEndpoint("addr", "group", "svc");
    assertThat(SerializationUtils.clone(point), equalTo(point));
  }
}
