package org.batfish.datamodel.matchers;

import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;
import org.batfish.datamodel.IpsecEncapsulationMode;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.IpsecProtocol;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IpsecPhase2ProposalMatchersImpl {

  static final class HasAuthenticationAlgorithm
      extends FeatureMatcher<IpsecPhase2Proposal, IpsecAuthenticationAlgorithm> {
    HasAuthenticationAlgorithm(@Nonnull Matcher<? super IpsecAuthenticationAlgorithm> subMatcher) {
      super(
          subMatcher,
          "An IPSec Phase2 Proposal with AuthenticationAlgorithm:",
          "AuthenticationAlgorithm");
    }

    @Override
    protected IpsecAuthenticationAlgorithm featureValueOf(IpsecPhase2Proposal actual) {
      return actual.getAuthenticationAlgorithm();
    }
  }

  static final class HasEncryptionAlgorithm
      extends FeatureMatcher<IpsecPhase2Proposal, EncryptionAlgorithm> {
    HasEncryptionAlgorithm(@Nonnull Matcher<? super EncryptionAlgorithm> subMatcher) {
      super(
          subMatcher, "An IPSec Phase2 Proposal with EncryptionAlgorithm:", "EncryptionAlgorithm");
    }

    @Override
    protected EncryptionAlgorithm featureValueOf(IpsecPhase2Proposal actual) {
      return actual.getEncryptionAlgorithm();
    }
  }

  static final class HasIpsecEncapsulationMode
      extends FeatureMatcher<IpsecPhase2Proposal, IpsecEncapsulationMode> {
    HasIpsecEncapsulationMode(@Nonnull Matcher<? super IpsecEncapsulationMode> subMatcher) {
      super(
          subMatcher,
          "An IPSec Phase2 Proposal with IpsecEncapsulationMode:",
          "IpsecEncapsulationMode");
    }

    @Override
    protected IpsecEncapsulationMode featureValueOf(IpsecPhase2Proposal actual) {
      return actual.getIpsecEncapsulationMode();
    }
  }

  static final class HasProtocols
      extends FeatureMatcher<IpsecPhase2Proposal, SortedSet<IpsecProtocol>> {
    HasProtocols(@Nonnull Matcher<? super SortedSet<IpsecProtocol>> subMatcher) {
      super(subMatcher, "An IPSec Phase2 Proposal with protocols:", "protocols");
    }

    @Override
    protected SortedSet<IpsecProtocol> featureValueOf(IpsecPhase2Proposal actual) {
      return actual.getProtocols();
    }
  }
}
