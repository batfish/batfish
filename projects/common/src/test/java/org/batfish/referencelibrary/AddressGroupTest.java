package org.batfish.referencelibrary;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/** Tests for {@link AddressGroup} */
class AddressGroupTest {

  @Test
  void testJavaSerialization() {
    AddressGroup group = new AddressGroup("group", null, null);
    assertThat(SerializationUtils.clone(group), equalTo(group));
  }
}
