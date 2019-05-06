package org.batfish.minesweeper.smt.matchers;

import org.batfish.minesweeper.answers.SmtReachabilityAnswerElement;
import org.batfish.minesweeper.smt.VerificationResult;
import org.batfish.minesweeper.smt.matchers.SmtReachabilityAnswerElementMatchersImpl.HasVerificationResult;
import org.hamcrest.Matcher;

public final class SmtReachabilityAnswerElementMatchers {
  private SmtReachabilityAnswerElementMatchers() {}

  public static Matcher<SmtReachabilityAnswerElement> hasVerificationResult(
      Matcher<VerificationResult> matcher) {
    return new HasVerificationResult(matcher);
  }
}
