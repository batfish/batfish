package org.batfish.datamodel.table;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.table.Row.RowBuilder;
import org.junit.Test;

public class TableDiffTest {

  @Test
  public void diffCells() {
    /** One value is null */
    assertThat(
        TableDiff.diffCells(null, new Integer(1), Schema.INTEGER),
        equalTo(TableDiff.resultDifferent(TableDiff.NULL_VALUE_BASE)));

    /** Both values are null */
    assertThat(TableDiff.diffCells(null, null, Schema.INTEGER), equalTo(TableDiff.RESULT_SAME));

    /** Integer difference */
    assertThat(
        TableDiff.diffCells(new Integer(1), new Integer(2), Schema.INTEGER),
        equalTo(TableDiff.resultDifferent("-1")));

    /** Set difference */
    String diffValue =
        TableDiff.diffCells(
            ImmutableSet.of(1, 2), ImmutableSet.of(2, 3), Schema.set(Schema.INTEGER));
    assertThat(diffValue, containsString("+ [1]"));
    assertThat(diffValue, containsString("- [3]"));

    /** String (and other types) */
    assertThat(TableDiff.diffCells("a", "b", Schema.STRING), equalTo(TableDiff.RESULT_DIFFERENT));
    assertThat(TableDiff.diffCells("a", "a", Schema.STRING), equalTo(TableDiff.RESULT_SAME));
  }

  @Test
  public void diffMetadata() {
    List<ColumnMetadata> inputColumns =
        ImmutableList.of(
            new ColumnMetadata("key", Schema.INTEGER, "key", true, false),
            new ColumnMetadata("value", Schema.INTEGER, "value", false, true),
            new ColumnMetadata("both", Schema.INTEGER, "both", true, true),
            new ColumnMetadata("neither", Schema.INTEGER, "neither", false, false));

    /**
     * "key" and "both" should be present as keys; three versions of "value" should be present; and
     * "neither" should be missing
     */
    TableMetadata expectedMetadata =
        new TableMetadata(
            ImmutableList.of(
                new ColumnMetadata("key", Schema.INTEGER, "key", true, false),
                new ColumnMetadata("both", Schema.INTEGER, "both", true, false),
                new ColumnMetadata(
                    TableDiff.diffColumnName("value"),
                    Schema.STRING,
                    TableDiff.diffColumnDescription("value"),
                    false,
                    true),
                new ColumnMetadata(
                    TableDiff.baseColumnName("value"), Schema.INTEGER, "value", false, false),
                new ColumnMetadata(
                    TableDiff.deltaColumnName("value"), Schema.INTEGER, "value", false, false)),
            new DisplayHints(null, null, "[key, both]"));

    assertThat(
        TableDiff.diffMetadata(new TableMetadata(inputColumns, null)), equalTo(expectedMetadata));
  }

  @Test
  public void diffRowValues() {
    Row baseRow = Row.builder().put("key1", "value1").put("key2", null).build();
    Row deltaRow = Row.builder().put("key1", null).put("key2", "value2").build();
    List<ColumnMetadata> cols =
        ImmutableList.of(
            new ColumnMetadata("key1", Schema.STRING, "desc1"),
            new ColumnMetadata("key2", Schema.STRING, "desc2"));

    RowBuilder diff = Row.builder();
    TableDiff.diffRowValues(diff, baseRow, deltaRow, cols);
    assertThat(
        diff.build(),
        equalTo(
            Row.of(
                // key1
                TableDiff.diffColumnName("key1"),
                TableDiff.diffCells("value1", null, Schema.STRING),
                TableDiff.baseColumnName("key1"),
                "value1",
                TableDiff.deltaColumnName("key1"),
                null,
                // key2
                TableDiff.diffColumnName("key2"),
                TableDiff.diffCells(null, "value2", Schema.STRING),
                TableDiff.baseColumnName("key2"),
                null,
                TableDiff.deltaColumnName("key2"),
                "value2")));
  }

  /** One of the rows is null */
  @Test
  public void diffRowValuesNull() {
    Row row = Row.of("key", "value");
    List<ColumnMetadata> cols = ImmutableList.of(new ColumnMetadata("key", Schema.STRING, "desc"));

    // delta is null
    RowBuilder diff1 = Row.builder();
    TableDiff.diffRowValues(diff1, row, null, cols);
    assertThat(
        diff1.build(),
        equalTo(
            Row.of(
                TableDiff.diffColumnName("key"),
                TableDiff.resultDifferent(TableDiff.MISSING_KEY_DELTA),
                TableDiff.baseColumnName("key"),
                "value",
                TableDiff.deltaColumnName("key"),
                null)));

    // base is null
    RowBuilder diff2 = Row.builder();
    TableDiff.diffRowValues(diff2, null, row, cols);
    assertThat(
        diff2.build(),
        equalTo(
            Row.of(
                TableDiff.diffColumnName("key"),
                TableDiff.resultDifferent(TableDiff.MISSING_KEY_BASE),
                TableDiff.baseColumnName("key"),
                null,
                TableDiff.deltaColumnName("key"),
                "value")));
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
    TableMetadata metadata = new TableMetadata(columns, null);

    Row row1 = Row.of("key", "sameKey", "value", "value1");
    Row row2 = Row.of("key", "sameKey", "value", "value2");
    Row row3 = Row.of("key", "diffKey", "value", "value3");

    TableAnswerElement table0 = new TableAnswerElement(metadata);
    TableAnswerElement table123 =
        new TableAnswerElement(metadata).addRow(row1).addRow(row2).addRow(row3);

    // expected count is 2: Rows with identical keys (1,2) are compressed
    assertThat(TableDiff.diffTables(table123, table0).getRows().size(), equalTo(2));
    assertThat(TableDiff.diffTables(table0, table123).getRows().size(), equalTo(2));

    TableAnswerElement table13 = new TableAnswerElement(metadata).addRow(row1).addRow(row3);
    TableAnswerElement table2 = new TableAnswerElement(metadata).addRow(row2);

    // expected count is 2: one for diffKey, one for sameKey
    assertThat(TableDiff.diffTables(table13, table2).getRows().size(), equalTo(2));
    assertThat(TableDiff.diffTables(table2, table13).getRows().size(), equalTo(2));

    TableAnswerElement table1 = new TableAnswerElement(metadata).addRow(row1);

    // expected count is 1: one for diffKey; sameKey should be removed since values are same
    assertThat(TableDiff.diffTables(table1, table13).getRows().size(), equalTo(1));
    assertThat(TableDiff.diffTables(table13, table1).getRows().size(), equalTo(1));
  }

  @Test
  public void diffTablesTestRowComposition() {
    List<ColumnMetadata> columns =
        ImmutableList.of(
            new ColumnMetadata("key", Schema.STRING, "key", true, false),
            new ColumnMetadata("both", Schema.STRING, "both", true, true),
            new ColumnMetadata("value", Schema.STRING, "value", false, true),
            new ColumnMetadata("neither", Schema.STRING, "neither", false, false));
    TableMetadata metadata = new TableMetadata(columns, null);

    Row row1 =
        Row.builder()
            .put("key", "key1")
            .put("both", "both1")
            .put("value", "value1")
            .put("neither", "neither1")
            .build();
    Row row2 =
        Row.builder()
            .put("key", "key1")
            .put("both", "both1")
            .put("value", "value2")
            .put("neither", "neither2")
            .build();

    TableAnswerElement table1 = new TableAnswerElement(metadata).addRow(row1);
    TableAnswerElement table2 = new TableAnswerElement(metadata).addRow(row2);

    Rows expectedRows =
        new Rows()
            .add(
                Row.builder()
                    .put("key", "key1")
                    .put("both", "both1")
                    .put(TableDiff.diffColumnName("value"), TableDiff.RESULT_DIFFERENT)
                    .put(TableDiff.baseColumnName("value"), "value1")
                    .put(TableDiff.deltaColumnName("value"), "value2")
                    .build());

    assertThat(TableDiff.diffTables(table1, table2).getRows(), equalTo(expectedRows));
  }
}
