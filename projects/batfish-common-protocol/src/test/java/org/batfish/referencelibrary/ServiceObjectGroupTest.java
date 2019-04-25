package org.batfish.referencelibrary;

import static org.junit.Assert.assertThat;

import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

/** Tests for {@link ServiceObjectGroup} */
public class ServiceObjectGroupTest {

  @Test
  public void testJavaSerialization() throws IOException {
    ServiceObjectGroup group = new ServiceObjectGroup("group", null);
    assertThat(SerializationUtils.clone(group), CoreMatchers.equalTo(group));
  }
}
