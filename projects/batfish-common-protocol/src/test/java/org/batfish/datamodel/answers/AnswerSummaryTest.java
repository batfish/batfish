package org.batfish.datamodel.answers;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.node.IntNode;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.questions.Assertion;
import org.batfish.datamodel.questions.Assertion.AssertionType;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.junit.Test;

/** Tests for {@link AnswerSummary} */
public class AnswerSummaryTest {

  @Test
  public void combineTest() {
    AnswerSummary summary = new AnswerSummary("notes1", 1, 2, 3);
    AnswerSummary summaryOther = new AnswerSummary("notes2", 4, 5, 6);
    summary.combine(summaryOther);
    assertThat(summary.getNotes(), equalTo("notes1; notes2"));
    assertThat(summary.getNumFailed(), equalTo(5));
    assertThat(summary.getNumPassed(), equalTo(7));
    assertThat(summary.getNumResults(), equalTo(9));
  }

  @Test
  public void constructFromTable() {
    // generate an answer with two rows
    TableAnswerElement answer = new TableAnswerElement(new TableMetadata());
    answer.addRow(BatfishObjectMapper.mapper().createObjectNode());
    answer.addRow(BatfishObjectMapper.mapper().createObjectNode());

    Assertion assertion = new Assertion(AssertionType.countequals, new IntNode(1)); // wrong count
    AnswerSummary summary = new AnswerSummary(answer, assertion);

    assertThat(summary.getNumResults(), equalTo(2));
    assertThat(summary.getNumFailed(), equalTo(1));
    assertThat(summary.getNumPassed(), equalTo(0));
  }

  @Test
  public void deserializationTest() throws IOException {
    String summaryStr =
        "{\"notes\" : \"notes1\", \"numFailed\" : 21, \"numPassed\" : 23, " + "\"numResults\": 42}";
    AnswerSummary summary = BatfishObjectMapper.mapper().readValue(summaryStr, AnswerSummary.class);
    assertThat(summary.getNotes(), equalTo("notes1"));
    assertThat(summary.getNumFailed(), equalTo(21));
    assertThat(summary.getNumPassed(), equalTo(23));
    assertThat(summary.getNumResults(), equalTo(42));
  }

  @Test
  public void serializationTest() throws IOException {
    AnswerSummary summary = new AnswerSummary("notes1", 21, 23, 42);

    // The summary should survive cloning through JSON.
    AnswerSummary summaryAfter = BatfishObjectMapper.clone(summary, AnswerSummary.class);

    assertThat(summaryAfter.getNotes(), equalTo("notes1"));
    assertThat(summaryAfter.getNumFailed(), equalTo(21));
    assertThat(summaryAfter.getNumPassed(), equalTo(23));
    assertThat(summaryAfter.getNumResults(), equalTo(42));
  }
}
