package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IkePhase1Proposal;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.IpsecSession;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class IpsecSessionMatchers {

  /**
   * Provides a matcher that matches if the provided {@code submatcher} matches the IPSec session's
   * {@code negotiatedIkeP1Key}
   */
  public static @Nonnull Matcher<IpsecSession> hasNegotiatedIkeP1Key(
      @Nonnull Matcher<? super IkePhase1Key> subMatcher) {
    return new HasNegotiatedIkeP1Key(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code submatcher} matches the IPSec session's
   * {@code negotiatedIkeP1Proposal}
   */
  public static @Nonnull Matcher<IpsecSession> hasNegotiatedIkeP1Proposal(
      @Nonnull Matcher<? super IkePhase1Proposal> subMatcher) {
    return new HasNegotiatedIkeP1Proposal(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code submatcher} matches the IPSec session's
   * {@code negotiatedIpsecP2Proposal}
   */
  public static @Nonnull Matcher<IpsecSession> hasNegotiatedIpsecP2Proposal(
      @Nonnull Matcher<? super IpsecPhase2Proposal> subMatcher) {
    return new HasNegotiatedIpsecP2Proposal(subMatcher);
  }

  private IpsecSessionMatchers() {}

  private static final class HasNegotiatedIkeP1Key
      extends FeatureMatcher<IpsecSession, IkePhase1Key> {

    HasNegotiatedIkeP1Key(@Nonnull Matcher<? super IkePhase1Key> subMatcher) {
      super(subMatcher, "An IPSec session with NegotiatedIkeP1Key:", "NegotiatedIkeP1Key");
    }

    @Override
    protected IkePhase1Key featureValueOf(IpsecSession actual) {
      return actual.getNegotiatedIkeP1Key();
    }
  }

  private static final class HasNegotiatedIkeP1Proposal
      extends FeatureMatcher<IpsecSession, IkePhase1Proposal> {

    HasNegotiatedIkeP1Proposal(@Nonnull Matcher<? super IkePhase1Proposal> subMatcher) {
      super(
          subMatcher, "An IPSec session with NegotiatedIkeP1Proposal:", "NegotiatedIkeP1Proposal");
    }

    @Override
    protected IkePhase1Proposal featureValueOf(IpsecSession actual) {
      return actual.getNegotiatedIkeP1Proposal();
    }
  }

  private static final class HasNegotiatedIpsecP2Proposal
      extends FeatureMatcher<IpsecSession, IpsecPhase2Proposal> {

    HasNegotiatedIpsecP2Proposal(@Nonnull Matcher<? super IpsecPhase2Proposal> subMatcher) {
      super(
          subMatcher,
          "An IPSec session with NegotiatedIpsecP2Proposal:",
          "NegotiatedIpsecP2Proposal");
    }

    @Override
    protected IpsecPhase2Proposal featureValueOf(IpsecSession actual) {
      return actual.getNegotiatedIpsecP2Proposal();
    }
  }
}
