package org.batfish.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.table.Row;
import org.junit.Test;

public final class ColumnFilterTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new ColumnFilter("a", "1", false), new ColumnFilter("a", "1", false))
        .addEqualityGroup(new ColumnFilter("b", "1", false))
        .addEqualityGroup(new ColumnFilter("b", "2", false))
        .addEqualityGroup(new ColumnFilter("b", "2", true))
        .testEquals();
  }

  @Test
  public void testMatchesContainsIgnoreCase() {
    String columnName = "column";
    String filterText = "bLah";
    ColumnFilter filter = new ColumnFilter(columnName, filterText, false);
    assertTrue(filter.matches(Row.builder().put(columnName, "xxBlaHxx").build()));
    assertFalse(filter.matches(Row.builder().put(columnName, "xxBlaxx").build()));
    assertFalse(filter.matches(Row.builder().put(columnName, "xxBlaxxh").build()));
  }

  @Test
  public void testMatchesExactIgnoreCase() {
    String columnName = "column";
    ColumnFilter filter = new ColumnFilter(columnName, "bLah", true);
    assertFalse(filter.matches(Row.builder().put(columnName, "xxBlaHxx").build()));
    assertTrue(filter.matches(Row.builder().put(columnName, "BlaH").build()));

    // Also true if the user happens to anticipate JSON stringification format.
    ColumnFilter filterLiteral = new ColumnFilter(columnName, "\"bLah\"", true);
    assertTrue(filterLiteral.matches(Row.builder().put(columnName, "BlaH").build()));

    // Test integers
    ColumnFilter filterInt = new ColumnFilter(columnName, "123", true);
    assertTrue(filterInt.matches(Row.builder().put(columnName, 123).build()));
    assertFalse(filterInt.matches(Row.builder().put(columnName, 1234).build()));

    // Verify regex-like text is not filtered
    ColumnFilter filterRegex = new ColumnFilter(columnName, "1.3", true);
    assertTrue(filterRegex.matches(Row.builder().put(columnName, "1.3").build()));
    assertFalse(filterRegex.matches(Row.builder().put(columnName, "123").build()));
  }

  @Test
  public void testSerialization() {
    ColumnFilter cf = new ColumnFilter("a", "b", true);
    assertThat(BatfishObjectMapper.clone(cf, ColumnFilter.class), equalTo(cf));
  }
}
