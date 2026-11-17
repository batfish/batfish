package org.batfish.question.specifiers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.IBatfishTestAdapter;
import org.junit.jupiter.api.Test;

/** Tests of {@link SpecifiersReachabilityAnswerer}. */
class SpecifiersReachabilityAnswererTest {

  @Test
  void answerDiffCrashes() {
    SpecifiersReachabilityQuestion defaultQ =
        new SpecifiersReachabilityQuestion(null, null, null, null, null, null);
    IBatfish batfish = new IBatfishTestAdapter();
    SpecifiersReachabilityAnswerer answerer = new SpecifiersReachabilityAnswerer(defaultQ, batfish);
    Throwable exception =
        assertThrows(
            Exception.class,
            () -> answerer.answerDiff(batfish.getSnapshot(), batfish.getReferenceSnapshot()));
    assertThat(
        exception.getMessage(),
        containsString("This question should not be run in differential mode."));
  }
}
