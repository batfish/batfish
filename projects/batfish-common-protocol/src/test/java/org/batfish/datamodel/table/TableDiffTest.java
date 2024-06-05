package org.batfish.datamodel.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.junit.Test;

public class TableDiffTest {

  private static ColumnMetadata colBoth =
      new ColumnMetadata("both", Schema.STRING, "both", true, true);

  private static ColumnMetadata colKey =
      new ColumnMetadata("key", Schema.STRING, "key", true, false);

  private static ColumnMetadata colKeyStatus =
      new ColumnMetadata(
          TableDiff.COL_KEY_PRESENCE, Schema.STRING, TableDiff.COL_KEY_PRESENCE_DESC, false, true);

  private static ColumnMetadata colValue =
      new ColumnMetadata("value", Schema.STRING, "value", false, true);

  private static ColumnMetadata colNeither =
      new ColumnMetadata("neither", Schema.STRING, "neither", false, false);

  private static List<ColumnMetadata> mixColumns() {
    return ImmutableList.of(colKey, colBoth, colValue, colNeither);
  }

  private static Row mixRow(String key, String both, String value, String neither) {
    return Row.of("key", key, "both", both, "value", value, "neither", neither);
  }

  @Test
  public void buildMapNoKeyColumn() {
    // Column metadata has no keys; so all rows should match
    List<ColumnMetadata> columns =
        ImmutableList.of(new ColumnMetadata("k1", Schema.STRING, "desc", false, true));

    // empty maps is returned when there are no Rows
    assertThat(TableDiff.buildMap(new Rows(), columns), equalTo(ImmutableMap.of()));

    assertThat(
        TableDiff.buildMap(new Rows().add(Row.of("k1", "a")), columns),
        equalTo(ImmutableMap.of(new LinkedList<>(), ImmutableList.of(Row.of("k1", "a")))));
  }

  @Test
  public void buildMapMixKeyValueColumns() {
    List<ColumnMetadata> columns =
        ImmutableList.of(
            new ColumnMetadata("key", Schema.STRING, "desc", true, false),
            new ColumnMetadata("both", Schema.STRING, "desc", true, true),
            new ColumnMetadata("value", Schema.STRING, "desc", false, true));

    Row row1 = Row.of("key", "key1", "both", "both1", "value", "value1");
    Row row2 = Row.of("key", "key2", "both", "both2", "value", "value2");
    Rows rows = new Rows().add(row1).add(row2);

    Map<List<Object>, List<Row>> map = TableDiff.buildMap(rows, columns);

    // row1 should be returned
    assertThat(map.get(ImmutableList.of("key1", "both1")), equalTo(ImmutableList.of(row1)));

    // nothing should be returned since the key is "partial"
    assertNull(map.get(ImmutableList.of("key1")));

    // nothing should be returned since there is no matching key
    assertNull(map.get(ImmutableList.of("key1", "both2")));
  }

  @Test
  public void diffMetadata() {
    List<ColumnMetadata> inputColumns = mixColumns();

    /**
     * "key" and "both" should be present as keys; three versions of "value" should be present; and
     * two versions of "neither" should be present
     */
    TableMetadata expectedMetadata =
        new TableMetadata(
            ImmutableList.of(
                colKey,
                new ColumnMetadata("both", Schema.STRING, "both", true, false),
                colKeyStatus,
                new ColumnMetadata(
                    TableDiff.baseColumnName("value"), Schema.STRING, "value", false, false),
                new ColumnMetadata(
                    TableDiff.deltaColumnName("value"), Schema.STRING, "value", false, false),
                new ColumnMetadata(
                    TableDiff.baseColumnName("neither"), Schema.STRING, "neither", false, false),
                new ColumnMetadata(
                    TableDiff.deltaColumnName("neither"), Schema.STRING, "neither", false, false)),
            "[key, both]");

    assertThat(
        TableDiff.diffMetadata(new TableMetadata(inputColumns, "desc")), equalTo(expectedMetadata));
  }

  @Test
  public void diffRowValues() {
    Row baseRow = mixRow("key1", "both1", "value1", null);
    Row deltaRow = mixRow(null, null, null, "neither2");

    RowBuilder diff = Row.builder();
    TableDiff.diffRowValues(diff, baseRow, deltaRow, new TableMetadata(mixColumns(), "desc"));
    assertThat(
        diff.build(),
        equalTo(
            Row.of(
                TableDiff.COL_KEY_PRESENCE,
                TableDiff.COL_KEY_STATUS_BOTH,
                // value
                TableDiff.baseColumnName("value"),
                "value1",
                TableDiff.deltaColumnName("value"),
                null,
                // neither
                TableDiff.baseColumnName("neither"),
                null,
                TableDiff.deltaColumnName("neither"),
                "neither2")));
  }

  /** One of the rows is null */
  @Test
  public void diffRowValuesNull() {
    Row row = Row.of("key", "value");
    TableMetadata metadata =
        new TableMetadata(
            ImmutableList.of(new ColumnMetadata("key", Schema.STRING, "desc", true, false)),
            "desc");

    // delta is null
    RowBuilder diff1 = Row.builder();
    TableDiff.diffRowValues(diff1, row, null, metadata);
    assertThat(
        diff1.build(),
        equalTo(Row.of(TableDiff.COL_KEY_PRESENCE, TableDiff.COL_KEY_STATUS_ONLY_BASE)));

    // base is null
    RowBuilder diff2 = Row.builder();
    TableDiff.diffRowValues(diff2, null, row, metadata);
    assertThat(
        diff2.build(),
        equalTo(Row.of(TableDiff.COL_KEY_PRESENCE, TableDiff.COL_KEY_STATUS_ONLY_DELTA)));
  }

  /** Checks if we get back the expected number of rows based on ignoring missing keys. */
  @Test
  public void diffTablesTestIncludeOneTableKeys() {
    List<ColumnMetadata> columns =
        ImmutableList.of(
            new ColumnMetadata("key", Schema.STRING, "key", true, false),
            new ColumnMetadata("value", Schema.STRING, "value", false, true));
    TableMetadata metadata = new TableMetadata(columns, "desc");

    Row row1 = Row.of("key", "sameKey", "value", "value1");
    Row row2 = Row.of("key", "sameKey", "value", "value2");
    Row row3 = Row.of("key", "diffKey", "value", "value3");

    TableAnswerElement table12 = new TableAnswerElement(metadata).addRow(row1).addRow(row2);
    TableAnswerElement table3 = new TableAnswerElement(metadata).addRow(row3);
    TableAnswerElement table1 = new TableAnswerElement(metadata).addRow(row1);
    TableAnswerElement table23 = new TableAnswerElement(metadata).addRow(row2).addRow(row3);

    // includeOneTableKeys = false ==> 0 rows since the tables have no key in common
    assertThat(TableDiff.diffTables(table12, table3, false).getRows().size(), equalTo(0));
    assertThat(TableDiff.diffTables(table3, table12, false).getRows().size(), equalTo(0));

    // includeOneTableKeys = false ==> 1 row since the tables have a common key w/ different values
    assertThat(TableDiff.diffTables(table23, table1, false).getRows().size(), equalTo(1));
    assertThat(TableDiff.diffTables(table1, table23, false).getRows().size(), equalTo(1));

    // includeOneTableKsy = true ==> should get 3 rows
    assertThat(TableDiff.diffTables(table12, table3, true).getRows().size(), equalTo(3));
    assertThat(TableDiff.diffTables(table3, table12, true).getRows().size(), equalTo(3));
  }

  /**
   * Checks if we get back the expected number of rows. Multiple rows with same key within a table
   * should be thrown out, and the same value across two tables should not be reported.
   */
  @Test
  public void diffTablesTestRowCount() {
    List<ColumnMetadata> columns =
        ImmutableList.of(
            new ColumnMetadata("key", Schema.STRING, "key", true, false),
            new ColumnMetadata("value", Schema.STRING, "value", false, true));
    TableMetadata metadata = new TableMetadata(columns, "desc");

    Row row1 = Row.of("key", "sameKey", "value", "value1");
    Row row2 = Row.of("key", "sameKey", "value", "value2");
    Row row3 = Row.of("key", "diffKey", "value", "value3");

    TableAnswerElement table0 = new TableAnswerElement(metadata);
    TableAnswerElement table1 = new TableAnswerElement(metadata).addRow(row1);
    TableAnswerElement table2 = new TableAnswerElement(metadata).addRow(row2);
    TableAnswerElement table13 = new TableAnswerElement(metadata).addRow(row1).addRow(row3);
    TableAnswerElement table123 =
        new TableAnswerElement(metadata).addRow(row1).addRow(row2).addRow(row3);

    // expected count is 3 -- all rows from table123
    assertThat(TableDiff.diffTables(table123, table0, true).getRows().size(), equalTo(3));
    assertThat(TableDiff.diffTables(table0, table123, true).getRows().size(), equalTo(3));

    // expected count is 2: one for diffKey, one for sameKey
    assertThat(TableDiff.diffTables(table13, table2, true).getRows().size(), equalTo(2));
    assertThat(TableDiff.diffTables(table2, table13, true).getRows().size(), equalTo(2));

    // expected count is 1: one for diffKey; sameKey should be removed since values are same
    assertThat(TableDiff.diffTables(table1, table13, true).getRows().size(), equalTo(1));
    assertThat(TableDiff.diffTables(table13, table1, true).getRows().size(), equalTo(1));
  }

  /** Checks if the content of the Rows we get back after diffing tables is what we expect */
  @Test
  public void diffTablesTestRowContent() {
    List<ColumnMetadata> columns = mixColumns();
    TableMetadata metadata = new TableMetadata(columns, "desc");

    Row row1 = mixRow("key1", "both1", "value1", "neither1");
    Row row2 = mixRow("key1", "both1", "value2", "neither2");

    TableAnswerElement table1 = new TableAnswerElement(metadata).addRow(row1);
    TableAnswerElement table2 = new TableAnswerElement(metadata).addRow(row2);

    Rows expectedRows =
        new Rows()
            .add(
                Row.builder()
                    .put("key", "key1")
                    .put("both", "both1")
                    .put(TableDiff.COL_KEY_PRESENCE, TableDiff.COL_KEY_STATUS_BOTH)
                    .put(TableDiff.baseColumnName("value"), "value1")
                    .put(TableDiff.deltaColumnName("value"), "value2")
                    .put(TableDiff.baseColumnName("neither"), "neither1")
                    .put(TableDiff.deltaColumnName("neither"), "neither2")
                    .build());

    assertThat(TableDiff.diffTables(table1, table2, true).getRows(), equalTo(expectedRows));
  }

  /** Checks if we properly handle tables where all columns are keys */
  @Test
  public void diffTablesTestAllKeys() {
    TableMetadata noKeys =
        new TableMetadata(
            ImmutableList.of(new ColumnMetadata("key", Schema.STRING, "key", true, false)), "desc");

    Row row1 = Row.of("key", "key1");
    Row row2 = Row.of("key", "key2");

    TableAnswerElement table1 = new TableAnswerElement(noKeys).addRow(row1);
    TableAnswerElement table2 = new TableAnswerElement(noKeys).addRow(row2);

    // should get back no rows if we are not including one table keys
    assertThat(TableDiff.diffTables(table1, table2, false).getRows(), equalTo(new Rows()));

    // should get back two rows if we are including one table keys
    assertThat(
        TableDiff.diffTables(table1, table2, true).getRows(),
        equalTo(
            new Rows()
                .add(
                    Row.of(
                        "key",
                        "key1",
                        TableDiff.COL_KEY_PRESENCE,
                        TableDiff.COL_KEY_STATUS_ONLY_BASE))
                .add(
                    Row.of(
                        "key",
                        "key2",
                        TableDiff.COL_KEY_PRESENCE,
                        TableDiff.COL_KEY_STATUS_ONLY_DELTA))));
  }
}
