package org.batfish.common.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ParallelMapSerializerTest {

  private static <V> Map<String, V> roundTrip(Map<String, V> byKey) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
      ParallelMapSerializer.writeMap(out, byKey);
    }
    try (ObjectInputStream in =
        new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
      return ParallelMapSerializer.readMap(in);
    }
  }

  @Test
  public void testEmptyMap() throws IOException {
    assertThat(roundTrip(new TreeMap<String, String>()).keySet(), empty());
  }

  @Test
  public void testNullValue() throws IOException {
    Map<String, String> map = new TreeMap<>();
    map.put("key", null);
    assertThat(roundTrip(map), equalTo(map));
  }

  /**
   * Each value contains its own key, so this detects a value paired with the wrong key. The map is
   * a {@link HashMap}, whose iteration order differs from its keys' natural order, and is large
   * enough that its values are serialized in more than one chunk in parallel.
   */
  @Test
  public void testValuesPairedWithTheirOwnKeys() throws IOException {
    Map<String, List<String>> map = new HashMap<>();
    for (int i = 0; i < 10_000; i++) {
      String key = "key-" + i;
      map.put(key, ImmutableList.of(key));
    }
    Map<String, List<String>> read = roundTrip(map);
    assertThat(read, equalTo(map));
    read.forEach((key, value) -> assertThat(value, equalTo(ImmutableList.of(key))));
  }

  /** The map is read back with its entries in the order they were written. */
  @Test
  public void testPreservesIterationOrder() throws IOException {
    Map<String, Integer> map = new HashMap<>();
    for (int i = 0; i < 1_000; i++) {
      map.put("key-" + i, i);
    }
    assertThat(
        ImmutableList.copyOf(roundTrip(map).keySet()), equalTo(ImmutableList.copyOf(map.keySet())));
  }
}
