package org.batfish.minesweeper.smt.matchers;

import java.util.Set;
import java.util.SortedMap;
import org.batfish.minesweeper.smt.VerificationResult;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class VerificationResultMatchersImpl {
  private VerificationResultMatchersImpl() {}

  public static final class HasFailures extends FeatureMatcher<VerificationResult, Set<String>> {

    public HasFailures(Matcher<? super Set<String>> subMatcher) {
      super(subMatcher, "A VerificationResult with failures: ", "failures");
    }

    @Override
    protected Set<String> featureValueOf(VerificationResult verificationResult) {
      return verificationResult.getFailures();
    }
  }

  public static final class HasIsVerified extends FeatureMatcher<VerificationResult, Boolean> {
    public HasIsVerified(Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A VerificationResult with isVerified: ", "isVerified");
    }

    @Override
    protected Boolean featureValueOf(VerificationResult verificationResult) {
      return verificationResult.isVerified();
    }
  }

  public static final class HasPacketModel
      extends FeatureMatcher<VerificationResult, SortedMap<String, String>> {
    public HasPacketModel(Matcher<? super SortedMap<String, String>> subMatcher) {
      super(subMatcher, "A VerificationResult with packetModel: ", "packetModel");
    }

    @Override
    protected SortedMap<String, String> featureValueOf(VerificationResult verificationResult) {
      return verificationResult.getPacketModel();
    }
  }
}
