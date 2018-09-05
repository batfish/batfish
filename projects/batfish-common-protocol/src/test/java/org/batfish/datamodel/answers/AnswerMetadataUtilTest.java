package org.batfish.datamodel.answers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.batfish.common.BatfishLogger;
import org.batfish.datamodel.questions.DisplayHints;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AnswerMetadataUtilTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private BatfishLogger _logger;

  @Before
  public void setup() {
    _logger = new BatfishLogger(BatfishLogger.LEVELSTR_OUTPUT, false);
  }

  @Test
  public void testComputeAnswerMetadata() throws IOException {
    String columnName = "col";
    int value = 5;

    Answer testAnswer = new Answer();
    testAnswer.addAnswerElement(
        new TableAnswerElement(
                new TableMetadata(
                    ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar")),
                    new DisplayHints().getTextDesc()))
            .addRow(Row.of(columnName, value)));
    testAnswer.setStatus(AnswerStatus.SUCCESS);

    assertThat(
        AnswerMetadataUtil.computeAnswerMetadata(testAnswer, _logger),
        equalTo(
            new AnswerMetadata(
                new Metrics(
                    ImmutableMap.of(columnName, ImmutableMap.of(Aggregation.MAX, value)),
                    ImmutableSet.of(),
                    1),
                AnswerStatus.SUCCESS)));
  }

  @Test
  public void testComputeAnswerMetadataUnsuccessfulAnswer() throws IOException {
    Answer testAnswer = new Answer();
    testAnswer.setStatus(AnswerStatus.FAILURE);

    assertThat(
        AnswerMetadataUtil.computeAnswerMetadata(testAnswer, _logger),
        equalTo(new AnswerMetadata(null, AnswerStatus.FAILURE)));
  }

  @Test
  public void testComputeAnswerMetadataInapplicable() throws IOException {
    String columnName = "col";
    List<Integer> value = ImmutableList.of(5);

    Answer testAnswer = new Answer();
    testAnswer.addAnswerElement(
        new TableAnswerElement(
                new TableMetadata(
                    ImmutableList.of(
                        new ColumnMetadata(columnName, Schema.list(Schema.INTEGER), "foobar")),
                    new DisplayHints().getTextDesc()))
            .addRow(Row.of(columnName, value)));
    testAnswer.setStatus(AnswerStatus.SUCCESS);

    assertThat(
        AnswerMetadataUtil.computeAnswerMetadata(testAnswer, _logger),
        equalTo(
            new AnswerMetadata(
                new Metrics(ImmutableMap.of(), ImmutableSet.of(), 1), AnswerStatus.SUCCESS)));
  }

  @Test
  public void testComputeColumnAggregations() {
    String columnName = "col";
    int value = 5;

    TableAnswerElement table =
        new TableAnswerElement(
                new TableMetadata(
                    ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar")),
                    new DisplayHints().getTextDesc()))
            .addRow(Row.of(columnName, value));
    List<ColumnAggregation> aggregations =
        ImmutableList.of(new ColumnAggregation(Aggregation.MAX, columnName));

    assertThat(
        AnswerMetadataUtil.computeColumnAggregations(table, aggregations, _logger),
        equalTo(ImmutableMap.of(columnName, ImmutableMap.of(Aggregation.MAX, value))));
  }

  @Test
  public void testComputeEmptyColumns() {
    String fullColumn = "full";
    String partialColumn = "partial";
    String emptyColumn = "empty";
    String val = "val";
    Map<String, ColumnMetadata> columnMetadata =
        ImmutableMap.of(
            fullColumn,
            new ColumnMetadata(fullColumn, Schema.STRING, fullColumn),
            partialColumn,
            new ColumnMetadata(partialColumn, Schema.STRING, partialColumn),
            emptyColumn,
            new ColumnMetadata(emptyColumn, Schema.STRING, emptyColumn));

    TableAnswerElement table =
        new TableAnswerElement(
            new TableMetadata(
                columnMetadata.values().stream().collect(ImmutableList.toImmutableList())));

    table.addRow(Row.builder(columnMetadata).put(fullColumn, val).put(partialColumn, val).build());
    table.addRow(Row.builder(columnMetadata).put(fullColumn, val).build());

    assertThat(
        AnswerMetadataUtil.computeEmptyColumns(table), equalTo(ImmutableSet.of(emptyColumn)));
  }

  @Test
  public void testComputeColumnAggregationMax() {
    String columnName = "col";
    int value = 5;

    TableAnswerElement table =
        new TableAnswerElement(
                new TableMetadata(
                    ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar")),
                    new DisplayHints().getTextDesc()))
            .addRow(Row.of(columnName, value));
    ColumnAggregation columnAggregation = new ColumnAggregation(Aggregation.MAX, columnName);

    assertThat(
        AnswerMetadataUtil.computeColumnAggregation(table, columnAggregation, _logger),
        equalTo(new ColumnAggregationResult(Aggregation.MAX, columnName, value)));
  }

  @Test
  public void testComputeColumnMaxOneRowInteger() {
    String columnName = "col";
    int value = 5;

    TableAnswerElement table =
        new TableAnswerElement(
                new TableMetadata(
                    ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar")),
                    new DisplayHints().getTextDesc()))
            .addRow(Row.of(columnName, value));

    assertThat(AnswerMetadataUtil.computeColumnMax(table, columnName, _logger), equalTo(value));
  }

  @Test
  public void testComputeColumnMaxOneRowIssue() {
    String columnName = "col";
    int severity = 5;
    Issue value = new Issue("blah", severity, new Issue.Type("1", "2"));

    TableAnswerElement table =
        new TableAnswerElement(
                new TableMetadata(
                    ImmutableList.of(new ColumnMetadata(columnName, Schema.ISSUE, "foobar")),
                    new DisplayHints().getTextDesc()))
            .addRow(Row.of(columnName, value));

    assertThat(AnswerMetadataUtil.computeColumnMax(table, columnName, _logger), equalTo(severity));
  }

  @Test
  public void testComputeColumnMaxTwoRows() {
    String columnName = "col";
    int value1 = 5;
    int value2 = 10;

    TableAnswerElement table =
        new TableAnswerElement(
                new TableMetadata(
                    ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar")),
                    new DisplayHints().getTextDesc()))
            .addRow(Row.of(columnName, value1))
            .addRow(Row.of(columnName, value2));

    assertThat(AnswerMetadataUtil.computeColumnMax(table, columnName, _logger), equalTo(value2));
  }

  @Test
  public void testComputeColumnMaxNoRows() {
    String columnName = "col";

    TableAnswerElement table =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar")),
                new DisplayHints().getTextDesc()));

    assertThat(AnswerMetadataUtil.computeColumnMax(table, columnName, _logger), nullValue());
  }

  @Test
  public void testComputeColumnMaxInvalidColumn() {
    String columnName = "col";
    String invalidColumnName = "invalid";
    int value = 5;

    TableAnswerElement table =
        new TableAnswerElement(
                new TableMetadata(
                    ImmutableList.of(new ColumnMetadata(columnName, Schema.INTEGER, "foobar")),
                    new DisplayHints().getTextDesc()))
            .addRow(Row.of(columnName, value));

    _thrown.expect(IllegalArgumentException.class);
    AnswerMetadataUtil.computeColumnMax(table, invalidColumnName, _logger);
  }

  @Test
  public void testComputeColumnMaxInvalidSchema() {
    String columnName = "col";
    String value = "hello";

    TableAnswerElement table =
        new TableAnswerElement(
                new TableMetadata(
                    ImmutableList.of(new ColumnMetadata(columnName, Schema.STRING, "foobar")),
                    new DisplayHints().getTextDesc()))
            .addRow(Row.of(columnName, value));

    assertThat(AnswerMetadataUtil.computeColumnMax(table, columnName, _logger), nullValue());
  }
}
