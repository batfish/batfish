package org.batfish.referencelibrary;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Tests for {@link FilterGroup} */
public class FilterGroupTest {

  @Test
  public void testJavaSerialization() {
    FilterGroup group = new FilterGroup(null, "group");
    assertThat(SerializationUtils.clone(group), equalTo(group));
  }
}
