package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IkeAuthenticationMethod;
import org.batfish.datamodel.IkeHashingAlgorithm;
import org.batfish.datamodel.matchers.IkeProposalMatchersImpl.HasAuthenticationAlgorithm;
import org.batfish.datamodel.matchers.IkeProposalMatchersImpl.HasAuthenticationMethod;
import org.batfish.datamodel.matchers.IkeProposalMatchersImpl.HasDiffieHellmanGroup;
import org.batfish.datamodel.matchers.IkeProposalMatchersImpl.HasEncryptionAlgorithm;
import org.batfish.datamodel.matchers.IkeProposalMatchersImpl.HasLifeTimeSeconds;

public final class IkeProposalMatchers {

  /**
   * Provides a matcher that matches if the IKE Proposal's value of {@code encryptionAlgorithm}
   * matches specified {@code encryptionAlgorithm}
   */
  public static HasEncryptionAlgorithm hasEncryptionAlgorithm(
      EncryptionAlgorithm encryptionAlgorithm) {
    return new HasEncryptionAlgorithm(equalTo(encryptionAlgorithm));
  }

  /**
   * Provides a matcher that matches if the IKE Proposal's value of {@code authenticationAlgorithm}
   * matches specified {@code authenticationAlgorithm}
   */
  public static HasAuthenticationAlgorithm hasAuthenticationAlgorithm(
      IkeHashingAlgorithm ikeHashingAlgorithm) {
    return new HasAuthenticationAlgorithm(equalTo(ikeHashingAlgorithm));
  }

  /**
   * Provides a matcher that matches if the IKE Proposal's value of {@code authenticationMethod}
   * matches specified {@code authenticationMethod}
   */
  public static HasAuthenticationMethod hasAuthenticationMethod(
      IkeAuthenticationMethod ikeAuthenticationMethod) {
    return new HasAuthenticationMethod(equalTo(ikeAuthenticationMethod));
  }

  /**
   * Provides a matcher that matches if the IKE Proposal's value of {@code diffieHellmanGroup}
   * matches specified {@code diffieHellmanGroup}
   */
  public static HasDiffieHellmanGroup hasDiffieHellmanGroup(DiffieHellmanGroup diffieHellmanGroup) {
    return new HasDiffieHellmanGroup(equalTo(diffieHellmanGroup));
  }

  /**
   * Provides a matcher that matches if the IKE Proposal's value of {@code lifeTimeSeconds} matches
   * specified {@code lifeTimeSeconds}
   */
  public static HasLifeTimeSeconds hasLifeTimeSeconds(Integer lifeTimeSeconds) {
    return new HasLifeTimeSeconds(equalTo(lifeTimeSeconds));
  }

  private IkeProposalMatchers() {}
}
