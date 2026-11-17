package org.batfish.referencelibrary;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/** Tests for {@link ServiceObjectGroup} */
class ServiceObjectGroupTest {

  @Test
  void testJavaSerialization() {
    ServiceObjectGroup group = new ServiceObjectGroup("group", null);
    assertThat(SerializationUtils.clone(group), equalTo(group));
  }
}
