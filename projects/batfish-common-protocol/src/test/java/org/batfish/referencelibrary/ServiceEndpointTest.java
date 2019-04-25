package org.batfish.referencelibrary;

import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

/** Tests for {@link ServiceEndpoint} */
public class ServiceEndpointTest {

  @Test
  public void testJavaSerialization() throws IOException {
    ServiceEndpoint point = new ServiceEndpoint("addr", "group", "svc");
    assertThat(SerializationUtils.clone(point), CoreMatchers.equalTo(point));
  }
}
