package org.batfish.datamodel.answers;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

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
}
