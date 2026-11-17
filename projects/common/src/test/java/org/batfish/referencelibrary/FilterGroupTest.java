package org.batfish.referencelibrary;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Test;

/** Tests for {@link FilterGroup} */
class FilterGroupTest {

  @Test
  void testJavaSerialization() {
    FilterGroup group = new FilterGroup(null, "group");
    assertThat(SerializationUtils.clone(group), equalTo(group));
  }
}
