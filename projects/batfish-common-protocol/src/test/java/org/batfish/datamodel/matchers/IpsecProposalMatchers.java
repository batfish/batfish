package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;
import org.batfish.datamodel.IpsecProtocol;
import org.batfish.datamodel.matchers.IpsecProposalMatchersImpl.HasAuthenticationAlgorithm;
import org.batfish.datamodel.matchers.IpsecProposalMatchersImpl.HasEncryptionAlgorithm;
import org.batfish.datamodel.matchers.IpsecProposalMatchersImpl.HasName;
import org.batfish.datamodel.matchers.IpsecProposalMatchersImpl.HasProtocols;

public final class IpsecProposalMatchers {

  /**
   * Provides a matcher that matches if the IPSec Proposal's value of {@code encryptionAlgorithm}
   * matches specified {@code encryptionAlgorithm}
   */
  public static @Nonnull HasEncryptionAlgorithm hasEncryptionAlgorithm(
      EncryptionAlgorithm encryptionAlgorithm) {
    return new HasEncryptionAlgorithm(equalTo(encryptionAlgorithm));
  }

  /**
   * Provides a matcher that matches if the IPSec Proposal's value of {@code
   * authenticationAlgorithm} matches specified {@code authenticationAlgorithm}
   */
  public static @Nonnull HasAuthenticationAlgorithm hasAuthenticationAlgorithm(
      IpsecAuthenticationAlgorithm ipsecAuthenticationAlgorithm) {
    return new HasAuthenticationAlgorithm(equalTo(ipsecAuthenticationAlgorithm));
  }

  /**
   * Provides a matcher that matches if the IPSec Proposal's value of {@code name} matches specified
   * {@code name}
   */
  public static @Nonnull HasName hasName(String name) {
    return new HasName(equalTo(name));
  }

  /**
   * Provides a matcher that matches if the IPSec Proposal's value of {@code protocols} matches
   * specified {@code protocols}
   */
  public static HasProtocols hasProtocols(Set<IpsecProtocol> protocols) {
    return new HasProtocols(equalTo(protocols));
  }

  private IpsecProposalMatchers() {}
}
