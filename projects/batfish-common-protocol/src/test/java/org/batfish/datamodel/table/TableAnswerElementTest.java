package org.batfish.datamodel.table;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.google.common.collect.ImmutableList;
import com.sun.tools.javac.util.Pair;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.answers.AnswerSummary;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.questions.Assertion;
import org.batfish.datamodel.questions.Assertion.AssertionType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link TableAnswerElement} */
public class TableAnswerElementTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  /** Does computerSummary compute the correct summary? */
  @Test
  public void testComputeSummary() {
    // generate an answer with two rows
    TableAnswerElement answer = new TableAnswerElement(new TableMetadata(null, "no desc"));
    answer.addRow(Row.builder().build());
    answer.addRow(Row.builder().build());

    Assertion assertion = new Assertion(AssertionType.countequals, new IntNode(1)); // wrong count
    AnswerSummary summary = answer.computeSummary(assertion);

    assertThat(summary.getNumResults(), equalTo(2));
    assertThat(summary.getNumFailed(), equalTo(1));
    assertThat(summary.getNumPassed(), equalTo(0));
  }

  /** Does evaluateAssertion do the right thing for counting assertions? */
  @Test
  public void testEvaluateAssertionCount() {
    Assertion twoCount = new Assertion(AssertionType.countequals, new IntNode(2));

    TableAnswerElement oneRow = new TableAnswerElement(new TableMetadata(null, "no desc"));
    oneRow.addRow(Row.builder().build());

    TableAnswerElement twoRows = new TableAnswerElement(new TableMetadata(null, "no desc"));
    twoRows.addRow(Row.builder().build());
    twoRows.addRow(Row.builder().build());

    assertThat(oneRow.evaluateAssertion(twoCount), equalTo(false));
    assertThat(twoRows.evaluateAssertion(twoCount), equalTo(true));
  }

  /** Does evaluateAssertion do the right thing for equality assertions? */
  @Test
  public void testEvaluateAssertionEqualsFalse() throws IOException {
    Assertion assertion =
        new Assertion(
            AssertionType.equals,
            BatfishObjectMapper.mapper()
                .readValue("[{\"key1\": \"value1\"}, {\"key2\": \"value2\"}]", JsonNode.class));

    // adding rows in different order shouldn't matter
    TableAnswerElement otherRows = new TableAnswerElement(new TableMetadata(null, "no desc"));
    otherRows.addRow(Row.builder().put("key2", "value2").build());
    otherRows.addRow(Row.builder().put("key1", "value1").build());

    assertThat(otherRows.evaluateAssertion(assertion), equalTo(true));

    // adding another duplicate row should matter
    otherRows.addRow(Row.builder().put("key1", "value1").build());

    assertThat(otherRows.evaluateAssertion(assertion), equalTo(false));
  }

  @Test
  public void testAutoComplete() {
    String columnName = "column";
    TableAnswerElement table =
        new TableAnswerElement(
            new TableMetadata(
                ImmutableList.of(new ColumnMetadata(columnName, Schema.STRING, "foobar")),
                "no desc"));

    int entryNum = 1;
    for (int i = 0; i < 3; ++i, ++entryNum) {
      String entry = "entry " + entryNum;
      table.addRow(Row.of(columnName, entry));
    }

    table.addRow(Row.of(columnName, "entry 1"));
    table.addRow(Row.of(columnName, "entry 1"));
    table.addRow(Row.of(columnName, "entry 1"));
    table.addRow(Row.of(columnName, "entry 1"));
    table.addRow(Row.of(columnName, "other entry"));

    Pair<List<AutocompleteSuggestion>, Boolean> truncatedSuggestions =
        table.autoComplete(columnName, "entry", 2);
    Pair<List<AutocompleteSuggestion>, Boolean> nonTruncatedSuggestions =
        table.autoComplete(columnName, "", 10);

    assertThat(truncatedSuggestions.fst.size(), equalTo(2));
    assertThat(truncatedSuggestions.snd, equalTo(true));

    assertThat(
        nonTruncatedSuggestions.fst,
        equalTo(
            Arrays.asList(
                new AutocompleteSuggestion("entry 1", true),
                new AutocompleteSuggestion("entry 2", true),
                new AutocompleteSuggestion("entry 3", true),
                new AutocompleteSuggestion("other entry", true))));
    assertThat(nonTruncatedSuggestions.snd, equalTo(false));
  }
}
