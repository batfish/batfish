package org.batfish.common.util;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import org.junit.Test;

public class InterfaceNameComparatorTest {
  @Test
  public void testSplit() {
    assertThat(InterfaceNameComparator.split("Ethernet1"), contains("Ethernet", "1"));
    assertThat(
        InterfaceNameComparator.split("Ethernet10/30"), contains("Ethernet", "10", "/", "30"));
    assertThat(InterfaceNameComparator.split("10/30"), contains("", "10", "/", "30"));
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
}
