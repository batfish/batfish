package org.batfish.symbolic.smt.matchers;

import org.batfish.symbolic.answers.SmtReachabilityAnswerElement;
import org.batfish.symbolic.smt.VerificationResult;
import org.batfish.symbolic.smt.matchers.SmtReachabilityAnswerElementMatchersImpl.HasVerificationResult;
import org.hamcrest.Matcher;

public final class SmtReachabilityAnswerElementMatchers {
  private SmtReachabilityAnswerElementMatchers() {}

  public static Matcher<SmtReachabilityAnswerElement> hasVerificationResult(
      Matcher<VerificationResult> matcher) {
    return new HasVerificationResult(matcher);
  }
}
