package org.batfish.question.specifiers;

import org.batfish.common.plugin.IBatfishTestAdapter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link SpecifiersReachabilityAnswerer}. */
public class SpecifiersReachabilityAnswererTest {
  @Rule public ExpectedException _expected = ExpectedException.none();

  @Test
  public void answerDiffCrashes() {
    SpecifiersReachabilityQuestion defaultQ =
        new SpecifiersReachabilityQuestion(null, null, null, null, null, null);
    SpecifiersReachabilityAnswerer answerer =
        new SpecifiersReachabilityAnswerer(defaultQ, new IBatfishTestAdapter());
    _expected.expectMessage("This question should not be run in differential mode.");
    answerer.answerDiff();
  }
}
