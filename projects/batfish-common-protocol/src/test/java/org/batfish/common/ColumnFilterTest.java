package org.batfish.common;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public final class ColumnFilterTest {

  @Test
  public void testEquals() {
    ColumnFilter group1Elem1 = new ColumnFilter("a", "1");
    ColumnFilter group1Elem2 = new ColumnFilter("a", "1");
    ColumnFilter group2Elem1 = new ColumnFilter("a", "2");
    ColumnFilter group3Elem1 = new ColumnFilter("b", "1");

    new EqualsTester()
        .addEqualityGroup(group1Elem1, group1Elem2)
        .addEqualityGroup(group2Elem1)
        .addEqualityGroup(group3Elem1)
        .testEquals();
  }
}
