package org.batfish.common;

import static org.junit.Assert.assertTrue;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.table.Row;
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

  @Test
  public void testMatches() {
    String columnName = "column";
    String filterText = "bLah";
    ColumnFilter filter = new ColumnFilter(columnName, filterText);
    Row row = Row.builder().put(columnName, "BlaHah").build();
    assertTrue(filter.matches(row));
  }
}
