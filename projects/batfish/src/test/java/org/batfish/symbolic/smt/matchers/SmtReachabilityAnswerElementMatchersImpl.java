package org.batfish.symbolic.smt.matchers;

import org.batfish.symbolic.answers.SmtReachabilityAnswerElement;
import org.batfish.symbolic.smt.VerificationResult;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class SmtReachabilityAnswerElementMatchersImpl {
  private SmtReachabilityAnswerElementMatchersImpl() {}

  public static final class HasVerificationResult
      extends FeatureMatcher<SmtReachabilityAnswerElement, VerificationResult> {

    public HasVerificationResult(Matcher<? super VerificationResult> subMatcher) {
      super(
          subMatcher,
          "An SmtReachabilityAnswerElement with VerificationResult: ",
          "verificationResult");
    }

    @Override
    protected VerificationResult featureValueOf(
        SmtReachabilityAnswerElement smtReachabilityAnswerElement) {
      return smtReachabilityAnswerElement.getResult();
    }
  }
}
