package org.batfish.referencelibrary;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSortedSet;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Tests for {@link InterfaceGroup} */
public class InterfaceGroupTest {

  @Test
  public void testJavaSerialization() {
    InterfaceGroup group = new InterfaceGroup(ImmutableSortedSet.of(), "group");
    assertThat(SerializationUtils.clone(group), equalTo(group));
  }
}
