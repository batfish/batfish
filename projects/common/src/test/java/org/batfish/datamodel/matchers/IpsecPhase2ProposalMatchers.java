package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;
import org.batfish.datamodel.IpsecEncapsulationMode;
import org.batfish.datamodel.IpsecProtocol;
import org.batfish.datamodel.matchers.IpsecPhase2ProposalMatchersImpl.HasAuthenticationAlgorithm;
import org.batfish.datamodel.matchers.IpsecPhase2ProposalMatchersImpl.HasEncryptionAlgorithm;
import org.batfish.datamodel.matchers.IpsecPhase2ProposalMatchersImpl.HasIpsecEncapsulationMode;
import org.batfish.datamodel.matchers.IpsecPhase2ProposalMatchersImpl.HasProtocols;

public final class IpsecPhase2ProposalMatchers {

  /**
   * Provides a matcher that matches if the IPSec Phase 2 Proposal's value of {@code
   * encryptionAlgorithm} matches specified {@code encryptionAlgorithm}
   */
  public static @Nonnull HasEncryptionAlgorithm hasEncryptionAlgorithm(
      EncryptionAlgorithm encryptionAlgorithm) {
    return new HasEncryptionAlgorithm(equalTo(encryptionAlgorithm));
  }

  /**
   * Provides a matcher that matches if the IPSec Phase 2 Proposal's value of {@code
   * authenticationAlgorithm} matches specified {@code authenticationAlgorithm}
   */
  public static @Nonnull HasAuthenticationAlgorithm hasAuthenticationAlgorithm(
      IpsecAuthenticationAlgorithm ipsecAuthenticationAlgorithm) {
    return new HasAuthenticationAlgorithm(equalTo(ipsecAuthenticationAlgorithm));
  }

  /**
   * Provides a matcher that matches if the IPSec Phase 2 Proposal's value of {@code protocols}
   * matches specified {@code protocols}
   */
  public static HasProtocols hasProtocols(Set<IpsecProtocol> protocols) {
    return new HasProtocols(equalTo(protocols));
  }

  /**
   * Provides a matcher that matches if the IPSec Phase 2 Proposal's value of {@code
   * ipsecEncapsulationMode} matches specified {@code ipsecEncapsulationMode}
   */
  public static HasIpsecEncapsulationMode hasIpsecEncapsulationMode(
      IpsecEncapsulationMode ipsecEncapsulationMode) {
    return new HasIpsecEncapsulationMode(equalTo(ipsecEncapsulationMode));
  }

  private IpsecPhase2ProposalMatchers() {}
}
