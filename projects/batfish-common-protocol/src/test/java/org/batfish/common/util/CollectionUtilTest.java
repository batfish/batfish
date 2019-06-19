package org.batfish.common.util;

import static org.batfish.common.util.CollectionUtil.toImmutableMap;
import static org.batfish.common.util.CollectionUtil.toOrderedHashCode;
import static org.batfish.common.util.CollectionUtil.toUnorderedHashCode;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.Test;

/** Tests of {@link CollectionUtil}. */
public class CollectionUtilTest {
  @Test
  public void testToImmutableMapDoesNotCopy() {
    Map<String, Integer> m = ImmutableMap.of("a", 1, "b", 2);
    Map<String, Integer> ms = ImmutableSortedMap.of("a", 1, "b", 2);
    assertThat(m, equalTo(ms));

    assertThat(toImmutableMap(m), sameInstance(m));
    assertThat(toImmutableMap(ms), sameInstance(ms));
  }

  @Test
  public void testToImmutableMapDoesCopy() {
    Map<String, Integer> m = Maps.newHashMap();
    m.put("a", 1);
    m.put("b", 2);

    assertThat(toImmutableMap(m), not(sameInstance(m)));
    assertThat(toImmutableMap(m), instanceOf(ImmutableMap.class));
  }

  @Test
  public void testToOrderedHashCode() {
    assertThat(Stream.of().collect(toOrderedHashCode()), equalTo(ImmutableList.of().hashCode()));
    assertThat(Stream.of(1).collect(toOrderedHashCode()), equalTo(ImmutableList.of(1).hashCode()));
    assertThat(
        Stream.of(1, 2).collect(toOrderedHashCode()), equalTo(ImmutableList.of(1, 2).hashCode()));
    assertThat(
        Stream.of(2, 1).collect(toOrderedHashCode()), equalTo(ImmutableList.of(2, 1).hashCode()));
  }

  @Test
  public void testToUnorderedHashCode() {
    assertThat(Stream.of().collect(toUnorderedHashCode()), equalTo(ImmutableSet.of().hashCode()));
    assertThat(Stream.of(1).collect(toUnorderedHashCode()), equalTo(ImmutableSet.of(1).hashCode()));
    assertThat(
        Stream.of(1, 2).collect(toUnorderedHashCode()), equalTo(ImmutableSet.of(2, 1).hashCode()));
  }
}
