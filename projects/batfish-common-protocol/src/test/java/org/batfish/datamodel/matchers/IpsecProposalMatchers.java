package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import java.util.Set;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;
import org.batfish.datamodel.IpsecProtocol;
import org.batfish.datamodel.matchers.IpsecProposalMatchersImpl.HasAuthenticationAlgorithm;
import org.batfish.datamodel.matchers.IpsecProposalMatchersImpl.HasEncryptionAlgorithm;
import org.batfish.datamodel.matchers.IpsecProposalMatchersImpl.HasProtocols;

public final class IpsecProposalMatchers {

  /**
   * Provides a matcher that matches if the Ipsec Proposal's value of {@code encryptionAlgorithm}
   * matches specified {@code encryptionAlgorithm}
   */
  public static HasEncryptionAlgorithm hasEncryptionAlgorithm(
      EncryptionAlgorithm encryptionAlgorithm) {
    return new HasEncryptionAlgorithm(equalTo(encryptionAlgorithm));
  }

  /**
   * Provides a matcher that matches if the Ipsec Proposal's value of {@code
   * authenticationAlgorithm} matches specified {@code authenticationAlgorithm}
   */
  public static HasAuthenticationAlgorithm hasAuthenticationAlgorithm(
      IpsecAuthenticationAlgorithm ipsecAuthenticationAlgorithm) {
    return new HasAuthenticationAlgorithm(equalTo(ipsecAuthenticationAlgorithm));
  }

  /**
   * Provides a matcher that matches if the Ipsec Proposal's value of {@code protocols} matches
   * specified {@code protocols}
   */
  public static HasProtocols hasProtocols(Set<IpsecProtocol> protocols) {
    return new HasProtocols(equalTo(protocols));
  }

  private IpsecProposalMatchers() {}
}
