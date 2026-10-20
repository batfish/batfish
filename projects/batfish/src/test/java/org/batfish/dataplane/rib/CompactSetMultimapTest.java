package org.batfish.dataplane.rib;

import static org.batfish.dataplane.rib.CompactSetMultimap.MAX_LIST_SIZE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;

/** Tests of {@link CompactSetMultimap}. */
public final class CompactSetMultimapTest {

  /** Enough values under one key to pass through every storage form going up and coming down. */
  private static final int MANY = 3 * MAX_LIST_SIZE;

  private static Integer[] array(List<Integer> values) {
    return values.toArray(new Integer[0]);
  }

  @Test
  public void testEmpty() {
    CompactSetMultimap<String, Integer> m = new CompactSetMultimap<>();

    assertFalse(m.containsKey("k"));
    assertThat(m.get("k"), empty());
    assertFalse(m.remove("k", 1));
  }

  @Test
  public void testSingleValue() {
    CompactSetMultimap<String, Integer> m = new CompactSetMultimap<>();

    assertTrue(m.put("k", 1));
    assertFalse(m.put("k", 1));
    assertTrue(m.containsKey("k"));
    assertThat(m.get("k"), contains(1));

    assertFalse(m.remove("k", 2));
    assertTrue(m.containsKey("k"));
    assertTrue(m.remove("k", 1));
    assertFalse(m.containsKey("k"));
    assertThat(m.get("k"), empty());
  }

  @Test
  public void testPutAndRemoveThroughEveryForm() {
    CompactSetMultimap<String, Integer> m = new CompactSetMultimap<>();
    List<Integer> expected = new ArrayList<>();
    for (int i = 0; i < MANY; i++) {
      assertTrue(m.put("k", i));
      assertFalse("already present", m.put("k", i));
      expected.add(i);
      assertTrue(m.containsKey("k"));
      assertThat(m.get("k"), hasSize(expected.size()));
      assertThat(m.get("k"), containsInAnyOrder(array(expected)));
    }
    for (int i = 0; i < MANY; i++) {
      assertFalse("never present", m.remove("k", -1));
      assertTrue(m.remove("k", i));
      assertFalse("already removed", m.remove("k", i));
      expected.remove(Integer.valueOf(i));
      assertThat(m.get("k"), hasSize(expected.size()));
      assertThat(m.get("k"), containsInAnyOrder(array(expected)));
    }
    assertFalse(m.containsKey("k"));
  }

  @Test
  public void testShrinkThenRegrow() {
    CompactSetMultimap<String, Integer> m = new CompactSetMultimap<>();
    List<Integer> expected = new ArrayList<>();
    for (int i = 0; i < MANY; i++) {
      m.put("k", i);
      expected.add(i);
    }
    for (int i = 1; i < MANY; i++) {
      m.remove("k", i);
      expected.remove(Integer.valueOf(i));
    }
    assertThat(m.get("k"), contains(0));
    for (int i = 1; i < MANY; i++) {
      assertTrue(m.put("k", i));
      expected.add(i);
      assertThat(m.get("k"), containsInAnyOrder(array(expected)));
    }
  }

  @Test
  public void testKeysAreIndependent() {
    CompactSetMultimap<String, Integer> m = new CompactSetMultimap<>();
    m.put("a", 1);
    m.put("b", 1);
    m.put("b", 2);

    assertTrue(m.remove("a", 1));
    assertFalse(m.containsKey("a"));
    assertThat(m.get("b"), containsInAnyOrder(1, 2));
  }

  @Test
  public void testGetIsReadOnly() {
    CompactSetMultimap<String, Integer> m = new CompactSetMultimap<>();
    for (int i = 0; i < MANY; i++) {
      m.put("k", i);
      Collection<Integer> view = m.get("k");
      assertThrows(UnsupportedOperationException.class, () -> view.add(-1));
      assertThrows(UnsupportedOperationException.class, () -> view.remove(0));
    }
  }
}
