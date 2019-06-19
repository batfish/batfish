package org.batfish.question.filtertable;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.Rows;
import org.junit.Test;

public class FilterTableAnswererTest {

  @Test
  public void filterRows() {
    Filter filter = new Filter("col >= 42");
    Rows rows = new Rows();
    rows.add(Row.builder().put("col", null).build());
    rows.add(Row.builder().put("col", 41).build());
    rows.add(Row.builder().put("col", 42).build());
    rows.add(Row.builder().put("col", 43).build());
    Multiset<Row> filteredRows = FilterTableAnswerer.filterRows(filter, rows.getData());

    // we should have the two rows with values >= 42
    assertThat(
        filteredRows,
        equalTo(
            new ImmutableMultiset.Builder<Row>()
                .add(Row.builder().put("col", 42).build())
                .add(Row.builder().put("col", 43).build())
                .build()));
  }

  @Test
  public void selectColumns() {
    Multiset<Row> rows =
        ImmutableMultiset.of(
            Row.builder().put("col1", null).put("col2", 12).put("col3", 13).build(),
            Row.builder().put("col1", 41).put("col2", 24).put("col3", 32).build());

    Multiset<Row> selectedRows = FilterTableAnswerer.selectColumns(ImmutableSet.of("col1"), rows);

    // only col1 should remain with expected values
    assertThat(
        selectedRows,
        equalTo(
            new ImmutableMultiset.Builder<Row>()
                .add(Row.builder().put("col1", 41).build())
                .add(Row.builder().put("col1", null).build())
                .build()));
  }
}
