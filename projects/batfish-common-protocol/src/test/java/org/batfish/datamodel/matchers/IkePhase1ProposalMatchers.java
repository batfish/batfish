package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IkeAuthenticationMethod;
import org.batfish.datamodel.IkeHashingAlgorithm;
import org.batfish.datamodel.matchers.IkePhase1ProposalMatchersImpl.HasAuthenticationMethod;
import org.batfish.datamodel.matchers.IkePhase1ProposalMatchersImpl.HasDiffieHellmanGroup;
import org.batfish.datamodel.matchers.IkePhase1ProposalMatchersImpl.HasEncryptionAlgorithm;
import org.batfish.datamodel.matchers.IkePhase1ProposalMatchersImpl.HasHashingAlgorithm;
import org.batfish.datamodel.matchers.IkePhase1ProposalMatchersImpl.HasLifeTimeSeconds;
import org.batfish.datamodel.matchers.IkePhase1ProposalMatchersImpl.HasName;

public final class IkePhase1ProposalMatchers {

  /**
   * Provides a matcher that matches if the IKE Phase 1 Proposal's value of {@code
   * encryptionAlgorithm} matches specified {@code encryptionAlgorithm}
   */
  public static HasEncryptionAlgorithm hasEncryptionAlgorithm(
      EncryptionAlgorithm encryptionAlgorithm) {
    return new HasEncryptionAlgorithm(equalTo(encryptionAlgorithm));
  }

  /**
   * Provides a matcher that matches if the IKE Phase 1 Proposal's value of {@code hashingAlgorithm}
   * matches specified {@code authenticationAlgorithm}
   */
  public static HasHashingAlgorithm hasHashingAlgorithm(IkeHashingAlgorithm hashingAlgorithm) {
    return new HasHashingAlgorithm(equalTo(hashingAlgorithm));
  }

  /**
   * Provides a matcher that matches if the IKE Phase 1 Proposal's value of {@code
   * authenticationMethod} matches specified {@code authenticationMethod}
   */
  public static HasAuthenticationMethod hasAuthenticationMethod(
      IkeAuthenticationMethod authenticationMethod) {
    return new HasAuthenticationMethod(equalTo(authenticationMethod));
  }

  /**
   * Provides a matcher that matches if the IKE Phase 1 Proposal's value of {@code
   * diffieHellmanGroup} matches specified {@code diffieHellmanGroup}
   */
  public static HasDiffieHellmanGroup hasDiffieHellmanGroup(DiffieHellmanGroup diffieHellmanGroup) {
    return new HasDiffieHellmanGroup(equalTo(diffieHellmanGroup));
  }

  /**
   * Provides a matcher that matches if the IKE Phase 1 Proposal's value of {@code lifeTimeSeconds}
   * matches specified {@code lifeTimeSeconds}
   */
  public static HasLifeTimeSeconds hasLifeTimeSeconds(Integer lifeTimeSeconds) {
    return new HasLifeTimeSeconds(equalTo(lifeTimeSeconds));
  }

  /**
   * Provides a matcher that matches if the IKE Phase 1 Proposal's value of {@code name} matches
   * specified {@code name}
   */
  public static HasName hasName(String name) {
    return new HasName(equalTo(name));
  }

  private IkePhase1ProposalMatchers() {}
}
