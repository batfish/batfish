package org.batfish.datamodel.table;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.batfish.common.AnswerRowsOptions;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.Schema;
import org.junit.Test;

public final class TableViewTest {

  private static final AnswerRowsOptions OPTIONS =
      new AnswerRowsOptions(
          ImmutableSet.of(), ImmutableList.of(), Integer.MAX_VALUE, 0, ImmutableList.of(), false);

  private static final TableMetadata METADATA =
      new TableMetadata(ImmutableList.of(new ColumnMetadata("c1", Schema.STRING, "desc")));

  @Test
  public void testSerialization() {
    Row row1 = Row.builder().put("key1", "v1").build();
    Row row2 = Row.builder().put("key1", "v2").build();
    TableView tableView =
        new TableView(
            OPTIONS,
            ImmutableList.of(new TableViewRow(0, row1), new TableViewRow(1, row2)),
            METADATA);

    TableView cycledTableView = BatfishObjectMapper.clone(tableView, TableView.class);

    assertThat(tableView.getOptions(), equalTo(cycledTableView.getOptions()));
    assertThat(tableView.getRows(), equalTo(cycledTableView.getRows()));
    assertThat(tableView.getTableMetadata(), equalTo(cycledTableView.getTableMetadata()));
  }
}
