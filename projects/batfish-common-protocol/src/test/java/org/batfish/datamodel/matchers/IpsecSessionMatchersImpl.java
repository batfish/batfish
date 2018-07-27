package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IkePhase1Proposal;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.IpsecSession;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IpsecSessionMatchersImpl {

  static class HasNegotiatedIkeP1Key extends FeatureMatcher<IpsecSession, IkePhase1Key> {

    HasNegotiatedIkeP1Key(@Nonnull Matcher<? super IkePhase1Key> subMatcher) {
      super(subMatcher, "An IPSec session with NegotiatedIkeP1Key:", "NegotiatedIkeP1Key");
    }

    @Override
    protected IkePhase1Key featureValueOf(IpsecSession actual) {
      return actual.getNegotiatedIkeP1Key();
    }
  }

  static class HasNegotiatedIkeP1Proposal extends FeatureMatcher<IpsecSession, IkePhase1Proposal> {

    HasNegotiatedIkeP1Proposal(@Nonnull Matcher<? super IkePhase1Proposal> subMatcher) {
      super(
          subMatcher, "An IPSec session with NegotiatedIkeP1Proposal:", "NegotiatedIkeP1Proposal");
    }

    @Override
    protected IkePhase1Proposal featureValueOf(IpsecSession actual) {
      return actual.getNegotiatedIkeP1Proposal();
    }
  }

  static class HasNegotiatedIpsecP2Proposal
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
