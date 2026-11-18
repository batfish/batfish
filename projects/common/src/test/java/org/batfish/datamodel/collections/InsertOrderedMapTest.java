package org.batfish.datamodel.collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.testing.EqualsTester;
import java.util.AbstractMap.SimpleEntry;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests of {@link InsertOrderedMap} */
class InsertOrderedMapTest {

  private InsertOrderedMap<String, String> _testMap;

  @BeforeEach
  void setUp() {
    _testMap = new InsertOrderedMap<>();
  }

  @Test
  void testIsEmptyOnCreation() {
    assertTrue(_testMap.isEmpty());
  }

  @Test
  void testPut() {
    _testMap.put("key", "value");
    assertThat(_testMap.get("key"), equalTo("value"));
  }

  @Test
  void testPutAsReplace() {
    _testMap.put("key", "value");
    _testMap.put("key2", "value2");
    _testMap.put("key", "newValue");
    // Expect new value at key; key moves to the end of the map.
    assertThat(_testMap.get("key"), equalTo("newValue"));
    assertThat(_testMap.keySet(), contains("key2", "key"));
  }

  @Test
  void testPutRejectsNullKey() {
    assertThrows(IllegalArgumentException.class, () -> _testMap.put(null, "value"));
  }

  @Test
  void testPutRejectsNullValue() {
    assertThrows(IllegalArgumentException.class, () -> _testMap.put("key", null));
  }

  @Test
  void testPutAll() {
    _testMap.putAll(ImmutableSortedMap.of("a", "1", "b", "2", "c", "3"));
    assertThat(_testMap.keySet(), contains("a", "b", "c"));
  }

  @Test
  void testRemove() {
    String key = "key";
    _testMap.put(key, "value");
    _testMap.remove(key);
    assertTrue(_testMap.isEmpty());
    // removing again should simply return null.
    assertThat(_testMap.remove(key), nullValue());
  }

  @Test
  void testClear() {
    _testMap.put("key", "value");
    _testMap.clear();
    assertTrue(_testMap.isEmpty());
  }

  @Test
  void tetKeySetIterationOrder() {
    _testMap.put("first", "1");
    _testMap.put("second", "2");
    _testMap.put("last", "3");
    assertThat(_testMap.keySet(), contains("first", "second", "last"));
  }

  @Test
  void testValuesIterationOrder() {
    _testMap.put("first", "1");
    _testMap.put("second", "2");
    _testMap.put("last", "3");
    assertThat(_testMap.values(), contains("1", "2", "3"));
  }

  @Test
  void tetEntrySetIterationOrder() {
    _testMap.put("first", "1");
    _testMap.put("second", "2");
    _testMap.put("last", "3");
    assertThat(
        _testMap.entrySet(),
        contains(
            new SimpleEntry<>("first", "1"),
            new SimpleEntry<>("second", "2"),
            new SimpleEntry<>("last", "3")));
  }

  @Test
  void testPutFirst() {
    _testMap.put("key", "value");
    _testMap.putFirst("first", "1");
    assertThat(_testMap.keySet(), contains("first", "key"));
  }

  @Test
  void testMoveFirst() {
    _testMap.put("key1", "value");
    _testMap.put("key2", "value");
    _testMap.put("key3", "value");
    _testMap.moveFirst("key3");
    assertThat(_testMap.keySet(), contains("key3", "key1", "key2"));
  }

  @Test
  void testMoveFirstRejectNonexistent() {
    assertThrows(IllegalArgumentException.class, () -> _testMap.moveFirst("key3"));
  }

  @Test
  void testMoveLast() {
    _testMap.put("key1", "value");
    _testMap.put("key2", "value");
    _testMap.put("key3", "value");
    _testMap.moveLast("key1");
    assertThat(_testMap.keySet(), contains("key2", "key3", "key1"));
  }

  @Test
  void testMoveLastRejectNonexistent() {
    assertThrows(IllegalArgumentException.class, () -> _testMap.moveLast("key3"));
  }

  @Test
  void testMoveBefore() {
    _testMap.put("key1", "value");
    _testMap.put("key2", "value2");
    _testMap.moveBefore("key2", "key1");
    assertThat(_testMap.keySet(), contains("key2", "key1"));
  }

  @Test
  void testMoveBeforeSameKey() {
    _testMap.put("key1", "value");
    _testMap.put("key2", "value2");
    // This should have no effect
    _testMap.moveBefore("key1", "key1");
    assertThat(_testMap.keySet(), contains("key1", "key2"));
  }

  @Test
  void testMoveBeforeRejectNonExistentKey() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          _testMap.put("key1", "value");
          _testMap.moveBefore("notThere", "key1");
        });
  }

  @Test
  void testMoveBeforeRejectNonExistentPivot() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          _testMap.put("key1", "value");
          _testMap.moveBefore("key1", "notThere");
        });
  }

  @Test
  void testMoveAfter() {
    _testMap.put("key1", "value");
    _testMap.put("key2", "value");
    _testMap.moveAfter("key1", "key2");
    assertThat(_testMap.keySet(), contains("key2", "key1"));
  }

  @Test
  void testMoveAfterSameKey() {
    _testMap.put("key1", "value");
    _testMap.put("key2", "value2");
    // This should have no effect
    _testMap.moveAfter("key1", "key1");
    assertThat(_testMap.keySet(), contains("key1", "key2"));
  }

  @Test
  void testMoveAfterRejectNonExistentKey() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          _testMap.put("key1", "value");
          _testMap.moveAfter("notThere", "key1");
        });
  }

  @Test
  void testMoveAfterRejectNonExistentPivot() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          _testMap.put("key1", "value");
          _testMap.moveAfter("key1", "notThere");
        });
  }

  @Test
  void testEquals() {
    _testMap.put("first", "val1");
    _testMap.put("second", "val2");
    InsertOrderedMap<String, String> shouldEqual = new InsertOrderedMap<>();
    shouldEqual.put("first", "val1");
    shouldEqual.put("second", "val2");
    InsertOrderedMap<String, String> m1 = new InsertOrderedMap<>();
    m1.put("first", "val1");
    InsertOrderedMap<String, String> m2 = new InsertOrderedMap<>();
    m2.put("second", "val2");
    m2.put("first", "val1");
    new EqualsTester()
        .addEqualityGroup(_testMap, _testMap, shouldEqual)
        .addEqualityGroup(m1)
        .addEqualityGroup(m2)
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  void testJavaSerialization() {
    _testMap.put("key1", "value");
    assertThat(SerializationUtils.clone(_testMap), equalTo(_testMap));
  }
}
