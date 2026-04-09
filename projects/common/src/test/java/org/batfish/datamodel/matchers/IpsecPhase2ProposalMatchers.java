package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;
import org.batfish.datamodel.IpsecEncapsulationMode;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.IpsecProtocol;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class IpsecPhase2ProposalMatchers {

  /**
   * Provides a matcher that matches if the IPSec Phase 2 Proposal's value of {@code
   * encryptionAlgorithm} matches specified {@code encryptionAlgorithm}
   */
  public static @Nonnull Matcher<IpsecPhase2Proposal> hasEncryptionAlgorithm(
      EncryptionAlgorithm encryptionAlgorithm) {
    return new HasEncryptionAlgorithm(equalTo(encryptionAlgorithm));
  }

  /**
   * Provides a matcher that matches if the IPSec Phase 2 Proposal's value of {@code
   * authenticationAlgorithm} matches specified {@code authenticationAlgorithm}
   */
  public static @Nonnull Matcher<IpsecPhase2Proposal> hasAuthenticationAlgorithm(
      IpsecAuthenticationAlgorithm ipsecAuthenticationAlgorithm) {
    return new HasAuthenticationAlgorithm(equalTo(ipsecAuthenticationAlgorithm));
  }

  /**
   * Provides a matcher that matches if the IPSec Phase 2 Proposal's value of {@code protocols}
   * matches specified {@code protocols}
   */
  public static Matcher<IpsecPhase2Proposal> hasProtocols(Set<IpsecProtocol> protocols) {
    return new HasProtocols(equalTo(protocols));
  }

  /**
   * Provides a matcher that matches if the IPSec Phase 2 Proposal's value of {@code
   * ipsecEncapsulationMode} matches specified {@code ipsecEncapsulationMode}
   */
  public static Matcher<IpsecPhase2Proposal> hasIpsecEncapsulationMode(
      IpsecEncapsulationMode ipsecEncapsulationMode) {
    return new HasIpsecEncapsulationMode(equalTo(ipsecEncapsulationMode));
  }

  private IpsecPhase2ProposalMatchers() {}

  private static final class HasAuthenticationAlgorithm
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

  private static final class HasEncryptionAlgorithm
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

  private static final class HasIpsecEncapsulationMode
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

  private static final class HasProtocols
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
