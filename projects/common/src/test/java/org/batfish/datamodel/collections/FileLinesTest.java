package org.batfish.datamodel.collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

class FileLinesTest {
  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(1)
        .addEqualityGroup(
            new FileLines("a", ImmutableSortedSet.of(1)),
            new FileLines("a", ImmutableSortedSet.of(1)))
        .addEqualityGroup(new FileLines("b", ImmutableSortedSet.of(1)))
        .addEqualityGroup(new FileLines("b", ImmutableSortedSet.of(2)))
        .testEquals();
  }

  @Test
  void testJsonSerialization() {
    FileLines f = new FileLines("a", ImmutableSortedSet.of(1));
    assertThat(f, equalTo(BatfishObjectMapper.clone(f, FileLines.class)));
  }
}
