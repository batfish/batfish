package org.batfish.referencelibrary;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Tests for {@link AddressGroup} */
public class AddressGroupTest {

  @Test
  public void testJavaSerialization() {
    AddressGroup group = new AddressGroup("group", null, null);
    assertThat(SerializationUtils.clone(group), equalTo(group));
  }
}
