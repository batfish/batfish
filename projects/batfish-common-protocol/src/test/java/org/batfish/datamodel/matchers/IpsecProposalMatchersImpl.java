package org.batfish.datamodel.matchers;

import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;
import org.batfish.datamodel.IpsecProposal;
import org.batfish.datamodel.IpsecProtocol;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IpsecProposalMatchersImpl {

  static final class HasAuthenticationAlgorithm
      extends FeatureMatcher<IpsecProposal, IpsecAuthenticationAlgorithm> {
    HasAuthenticationAlgorithm(@Nonnull Matcher<? super IpsecAuthenticationAlgorithm> subMatcher) {
      super(
          subMatcher, "An IPSec Proposal with AuthenticationAlgorithm:", "AuthenticationAlgorithm");
    }

    @Override
    protected IpsecAuthenticationAlgorithm featureValueOf(IpsecProposal actual) {
      return actual.getAuthenticationAlgorithm();
    }
  }

  static final class HasEncryptionAlgorithm
      extends FeatureMatcher<IpsecProposal, EncryptionAlgorithm> {
    HasEncryptionAlgorithm(@Nonnull Matcher<? super EncryptionAlgorithm> subMatcher) {
      super(subMatcher, "An IPSec Proposal with EncryptionAlgorithm:", "EncryptionAlgorithm");
    }

    @Override
    protected EncryptionAlgorithm featureValueOf(IpsecProposal actual) {
      return actual.getEncryptionAlgorithm();
    }
  }

  static final class HasName extends FeatureMatcher<IpsecProposal, String> {
    HasName(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec Proposal with Name:", "Name");
    }

    @Override
    protected String featureValueOf(IpsecProposal actual) {
      return actual.getName();
    }
  }

  static final class HasProtocols extends FeatureMatcher<IpsecProposal, SortedSet<IpsecProtocol>> {
    HasProtocols(@Nonnull Matcher<? super SortedSet<IpsecProtocol>> subMatcher) {
      super(subMatcher, "An IPSec Proposal with protocols:", "protocols");
    }

    @Override
    protected SortedSet<IpsecProtocol> featureValueOf(IpsecProposal actual) {
      return actual.getProtocols();
    }
  }
}
