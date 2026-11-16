package org.batfish.common.util;

import static org.batfish.common.util.CollectionUtil.copyMapAndAdd;
import static org.batfish.common.util.CollectionUtil.hashCodeWithEnumOrdinals;
import static org.batfish.common.util.CollectionUtil.maxValues;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;
import static org.batfish.common.util.CollectionUtil.toMap;
import static org.batfish.common.util.CollectionUtil.toOrderedHashCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.Test;

/** Tests of {@link CollectionUtil}. */
public class CollectionUtilTest {
  private enum Value {
    FIRST,
    SECOND,
    THIRD
  }

  @Test
  public void testHashCodeWithEnumOrdinals() {
    assertThat(hashCodeWithEnumOrdinals(null), equalTo(0));
    assertThat(hashCodeWithEnumOrdinals("value"), equalTo("value".hashCode()));
    assertThat(hashCodeWithEnumOrdinals(Value.SECOND), equalTo(1));
  }

  @Test
  public void testHashCodeWithEnumOrdinalsCollections() {
    assertThat(
        hashCodeWithEnumOrdinals(ImmutableList.of(Value.SECOND, Value.THIRD)),
        equalTo(ImmutableList.of(1, 2).hashCode()));
    assertThat(
        hashCodeWithEnumOrdinals(ImmutableList.of(Value.THIRD, Value.SECOND)),
        not(hashCodeWithEnumOrdinals(ImmutableList.of(Value.SECOND, Value.THIRD))));
    assertThat(
        hashCodeWithEnumOrdinals(ImmutableSet.of(Value.SECOND, Value.THIRD)),
        equalTo(hashCodeWithEnumOrdinals(ImmutableSet.of(Value.THIRD, Value.SECOND))));
    assertThat(
        hashCodeWithEnumOrdinals(ImmutableSet.of(Value.SECOND, Value.THIRD)),
        equalTo(ImmutableSet.of(1, 2).hashCode()));
  }

  @Test
  public void testHashCodeWithEnumOrdinalsNestedMap() {
    assertThat(
        hashCodeWithEnumOrdinals(
            ImmutableMap.of("key", ImmutableMap.of(Value.SECOND, ImmutableList.of(Value.THIRD)))),
        equalTo(ImmutableMap.of("key", ImmutableMap.of(1, ImmutableList.of(2))).hashCode()));
  }

  @Test
  public void testCopyMapAndAdd() {
    Map<String, Integer> m = ImmutableMap.of("a", 1, "b", 2);
    ImmutableMap<String, Integer> newM = copyMapAndAdd(m, "a", 3);
    // Does not crash and does contain new value.
    assertThat(newM, equalTo(ImmutableMap.of("a", 3, "b", 2)));
  }

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
    assertThat(
        Stream.of(Value.SECOND, Value.THIRD).collect(toOrderedHashCode()),
        equalTo(ImmutableList.of(1, 2).hashCode()));
    assertThat(
        Stream.of(Value.SECOND, Value.THIRD).parallel().collect(toOrderedHashCode()),
        equalTo(ImmutableList.of(1, 2).hashCode()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToOrderedHashCodeRejectsStreams() {
    int ignored = Stream.of(Stream.of(1)).collect(toOrderedHashCode());
  }

  @Test
  public void testToMap() {
    Set<String> m = ImmutableSet.of("a", "b");

    assertThat(
        toMap(m, Function.identity(), x -> x.equals("a") ? 0 : 1),
        equalTo(ImmutableMap.of("a", 0, "b", 1)));
  }

  @Test
  public void testMaxValues() {
    Comparator<String> longest = Comparator.comparing(String::length);
    List<String> strings = ImmutableList.of("a", "abc", "ab", "efg", "");
    assertThat(maxValues(strings, longest), contains("abc", "efg"));
  }
}
