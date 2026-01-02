package org.batfish.common;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public final class ColumnSortOptionTest {

  @Test
  public void testEquals() {
    ColumnSortOption group1Elem1 = new ColumnSortOption("a", false);
    ColumnSortOption group1Elem2 = new ColumnSortOption("a", false);
    ColumnSortOption group2Elem1 = new ColumnSortOption("a", true);
    ColumnSortOption group3Elem1 = new ColumnSortOption("b", false);

    new EqualsTester()
        .addEqualityGroup(group1Elem1, group1Elem2)
        .addEqualityGroup(group2Elem1)
        .addEqualityGroup(group3Elem1)
        .testEquals();
  }
}
