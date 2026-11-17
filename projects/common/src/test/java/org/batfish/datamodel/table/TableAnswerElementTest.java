package org.batfish.datamodel.table;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.AnswerSummary;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.Assertion;
import org.batfish.datamodel.questions.Assertion.AssertionType;
import org.junit.jupiter.api.Test;

/** Tests for {@link TableAnswerElement} */
class TableAnswerElementTest {

  private static final ColumnMetadata key1ColMetadata =
      new ColumnMetadata("key1", Schema.STRING, "desc1");
  private static final TableMetadata oneKeyTableMetadata =
      new TableMetadata(ImmutableList.of(key1ColMetadata));
  private static final TableMetadata twoKeyTableMetadata =
      new TableMetadata(
          ImmutableList.of(key1ColMetadata, new ColumnMetadata("key2", Schema.STRING, "desc2")));

  /** Does computerSummary compute the correct summary? */
  @Test
  void testComputeSummary() {
    // generate an answer with two rows
    TableAnswerElement answer =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(new ColumnMetadata("col", Schema.STRING, "desc")), "no desc"));
    answer.addRow(Row.builder().put("col", "val").build());
    answer.addRow(Row.builder().put("col", "val").build());

    Assertion assertion = new Assertion(AssertionType.countequals, new IntNode(1)); // wrong count
    AnswerSummary summary = answer.computeSummary(assertion);

    assertThat(summary.getNumResults(), equalTo(2));
    assertThat(summary.getNumFailed(), equalTo(1));
    assertThat(summary.getNumPassed(), equalTo(0));
  }

  /** Does evaluateAssertion do the right thing for counting assertions? */
  @Test
  void testEvaluateAssertionCount() {
    Assertion twoCount = new Assertion(AssertionType.countequals, new IntNode(2));

    TableAnswerElement oneRow =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(new ColumnMetadata("col", Schema.STRING, "desc")), "no desc"));
    oneRow.addRow(Row.builder().put("col", "val").build());

    TableAnswerElement twoRows =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(new ColumnMetadata("col", Schema.STRING, "desc")), "no desc"));
    twoRows.addRow(Row.builder().put("col", "val").build());
    twoRows.addRow(Row.builder().put("col", "val").build());

    assertThat(oneRow.evaluateAssertion(twoCount), equalTo(false));
    assertThat(twoRows.evaluateAssertion(twoCount), equalTo(true));
  }

  /** Does evaluateAssertion do the right thing for equality assertions? */
  @Test
  void testEvaluateAssertionEqualsFalse() throws IOException {
    Assertion assertion =
        new Assertion(
            AssertionType.equals,
            BatfishObjectMapper.mapper()
                .readValue("[{\"key1\": \"value1\"}, {\"key1\": \"value2\"}]", JsonNode.class));

    // adding rows in different order shouldn't matter
    TableAnswerElement otherRows = new TableAnswerElement(oneKeyTableMetadata);
    otherRows.addRow(Row.builder().put("key1", "value2").build());
    otherRows.addRow(Row.builder().put("key1", "value1").build());

    assertThat(otherRows.evaluateAssertion(assertion), equalTo(true));

    // adding another duplicate row should matter
    otherRows.addRow(Row.builder().put("key1", "value2").build());

    assertThat(otherRows.evaluateAssertion(assertion), equalTo(false));
  }

  @Test
  void testAddEmptyRow() {
    TableAnswerElement table = new TableAnswerElement(oneKeyTableMetadata);
    Row row = Row.builder().build();

    Throwable exception = assertThrows(IllegalArgumentException.class, () -> table.addRow(row));
    assertThat(
        exception.getMessage(),
        containsString(
            String.format(
                "Row columns %s do not match metadata columns metadata %s",
                row.getColumnNames(), oneKeyTableMetadata.toColumnMap().keySet())));
  }

  @Test
  void testAddRowMissingColumn() {
    TableAnswerElement table = new TableAnswerElement(twoKeyTableMetadata);
    Row row = Row.builder().put("key1", null).build();

    Throwable exception = assertThrows(IllegalArgumentException.class, () -> table.addRow(row));
    assertThat(
        exception.getMessage(),
        containsString(
            String.format(
                "Row columns %s do not match metadata columns metadata %s",
                row.getColumnNames(), twoKeyTableMetadata.toColumnMap().keySet())));
  }

  @Test
  void testAddValidRow() {
    TableAnswerElement table = new TableAnswerElement(twoKeyTableMetadata);
    Row row = Row.builder().put("key1", null).put("key2", null).build();
    table.addRow(row);
    assertThat(table.getRows().getData(), contains(row));
  }

  @Test
  void testAddRowExtraColumn() {
    TableAnswerElement table = new TableAnswerElement(twoKeyTableMetadata);

    Row row = Row.builder().put("key1", null).put("key2", null).put("key3", null).build();
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> table.addRow(row));
    assertThat(
        exception.getMessage(),
        containsString(
            String.format(
                "Row columns %s do not match metadata columns metadata %s",
                row.getColumnNames(), twoKeyTableMetadata.toColumnMap().keySet())));
  }
}
