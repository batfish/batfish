package org.batfish.common.util;

import static org.batfish.common.util.CollectionUtil.toImmutableMap;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import java.util.Map;
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
}
