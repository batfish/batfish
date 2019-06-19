package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IkePhase1Proposal;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.matchers.IpsecSessionMatchersImpl.HasNegotiatedIkeP1Key;
import org.batfish.datamodel.matchers.IpsecSessionMatchersImpl.HasNegotiatedIkeP1Proposal;
import org.batfish.datamodel.matchers.IpsecSessionMatchersImpl.HasNegotiatedIpsecP2Proposal;
import org.hamcrest.Matcher;

public final class IpsecSessionMatchers {

  /**
   * Provides a matcher that matches if the provided {@code submatcher} matches the IPSec session's
   * {@code negotiatedIkeP1Key}
   */
  public static @Nonnull HasNegotiatedIkeP1Key hasNegotiatedIkeP1Key(
      @Nonnull Matcher<? super IkePhase1Key> subMatcher) {
    return new HasNegotiatedIkeP1Key(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code submatcher} matches the IPSec session's
   * {@code negotiatedIkeP1Proposal}
   */
  public static @Nonnull HasNegotiatedIkeP1Proposal hasNegotiatedIkeP1Proposal(
      @Nonnull Matcher<? super IkePhase1Proposal> subMatcher) {
    return new HasNegotiatedIkeP1Proposal(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code submatcher} matches the IPSec session's
   * {@code negotiatedIpsecP2Proposal}
   */
  public static @Nonnull HasNegotiatedIpsecP2Proposal hasNegotiatedIpsecP2Proposal(
      @Nonnull Matcher<? super IpsecPhase2Proposal> subMatcher) {
    return new HasNegotiatedIpsecP2Proposal(subMatcher);
  }

  private IpsecSessionMatchers() {}
}
