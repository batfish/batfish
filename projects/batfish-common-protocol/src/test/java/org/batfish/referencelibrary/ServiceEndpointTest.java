package org.batfish.referencelibrary;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Tests for {@link ServiceEndpoint} */
public class ServiceEndpointTest {

  @Test
  public void testJavaSerialization() {
    ServiceEndpoint point = new ServiceEndpoint("addr", "group", "svc");
    assertThat(SerializationUtils.clone(point), equalTo(point));
  }
}
