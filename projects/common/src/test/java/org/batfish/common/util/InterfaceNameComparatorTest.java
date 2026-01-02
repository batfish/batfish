package org.batfish.common.util;

import static org.batfish.common.util.InterfaceNameComparator.compareStringVsNum;
import static org.batfish.common.util.InterfaceNameComparator.split;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.sameInstance;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

public class InterfaceNameComparatorTest {
  @Test
  public void testSplit() {
    assertThat(split("Ethernet1"), contains("Ethernet", "1"));
    assertThat(split("Ethernet10/30"), contains("Ethernet", "10", "/", "30"));
    assertThat(split("10/30"), contains("", "10", "/", "30"));
  }

  @Test
  public void testStringVsNum() {
    assertThat(compareStringVsNum("a", "A"), equalTo(0));
    assertThat(compareStringVsNum("a", "b"), lessThan(0));
    assertThat(compareStringVsNum("b", "a"), greaterThan(0));
    assertThat(compareStringVsNum("1", "01"), equalTo(0));
    assertThat(compareStringVsNum("2", "10"), lessThan(0));
    assertThat(compareStringVsNum("10", "2"), greaterThan(0));
    assertThat(compareStringVsNum("a", "2"), greaterThan(0));
    assertThat(compareStringVsNum("2", "a"), lessThan(0));
  }

  @Test
  public void testOrder() {
    Comparator<String> comp = InterfaceNameComparator.instance();
    List<String> input =
        ImmutableList.of(
            "1",
            "Ethernet1",
            "Ethernet1/0/4",
            "Ethernet02",
            "Ethernet2",
            "ethernet02",
            "ethernet2",
            "Ethernet2.1",
            "ethernet2abcd",
            "ethernet20",
            "Ethernet24",
            "Ethernet99",
            "Ethernet104",
            "TenGigabitEthernet1");
    for (int i = 0; i < input.size(); ++i) {
      //noinspection EqualsWithItself -- that's what we're testing.
      assertThat(comp.compare(input.get(i), input.get(i)), equalTo(0));
      String istr = input.get(i);
      for (int j = i + 1; j < input.size(); ++j) {
        String jstr = input.get(j);
        assertThat(istr + '<' + jstr, comp.compare(input.get(i), input.get(j)), lessThan(0));
        assertThat(jstr + '>' + istr, comp.compare(input.get(j), input.get(i)), greaterThan(0));
      }
    }
  }

  @Test
  public void testSerialization() {
    InterfaceNameComparator comp = InterfaceNameComparator.instance();
    assertThat(SerializationUtils.clone(comp), sameInstance(comp));
  }
}
