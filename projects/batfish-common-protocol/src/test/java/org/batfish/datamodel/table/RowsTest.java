package org.batfish.datamodel.table;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.answers.Schema;
import org.junit.Test;

public class RowsTest {

  @Test
  public void getRowTestNoKeyColumn() {
    // Column metadata has no keys; so all rows should match
    List<ColumnMetadata> columns =
        ImmutableList.of(new ColumnMetadata("k1", Schema.STRING, "desc", false, true));

    Object rowKey = ImmutableList.of();

    // null is returned when there are no Rows
    assertThat(new Rows().getRow(rowKey, columns), equalTo(null));

    // a matching row is returned
    assertThat(new Rows().add(Row.of("l", "a")).getRow(rowKey, columns), equalTo(Row.of("l", "a")));
  }

  @Test
  public void getRowTestMixKeyValueColumns() {
    List<ColumnMetadata> columns =
        ImmutableList.of(
            new ColumnMetadata("key", Schema.STRING, "desc", true, false),
            new ColumnMetadata("both", Schema.STRING, "desc", true, true),
            new ColumnMetadata("value", Schema.STRING, "desc", false, true));

    Row row1 = Row.of("key", "key1", "both", "both1", "value", "value1");
    Row row2 = Row.of("key", "key2", "both", "both2", "value", "value2");
    Rows rows = new Rows().add(row1).add(row2);

    // row1 should be returned
    assertThat(rows.getRow(ImmutableList.of("key1", "both1"), columns), equalTo(row1));

    // nothing should be returned since the key is "partial"
    assertThat(rows.getRow(ImmutableList.of("key1"), columns), equalTo(null));

    // nothing should be returned since there is no matching key
    assertThat(rows.getRow(ImmutableList.of("key1", "both2"), columns), equalTo(null));
  }
}
