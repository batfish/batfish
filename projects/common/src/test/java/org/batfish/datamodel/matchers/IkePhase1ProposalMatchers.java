package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IkeAuthenticationMethod;
import org.batfish.datamodel.IkeHashingAlgorithm;
import org.batfish.datamodel.IkePhase1Proposal;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class IkePhase1ProposalMatchers {

  /**
   * Provides a matcher that matches if the IKE Phase 1 Proposal's value of {@code
   * encryptionAlgorithm} matches specified {@code encryptionAlgorithm}
   */
  public static Matcher<IkePhase1Proposal> hasEncryptionAlgorithm(
      EncryptionAlgorithm encryptionAlgorithm) {
    return new HasEncryptionAlgorithm(equalTo(encryptionAlgorithm));
  }

  /**
   * Provides a matcher that matches if the IKE Phase 1 Proposal's value of {@code hashingAlgorithm}
   * matches specified {@code authenticationAlgorithm}
   */
  public static Matcher<IkePhase1Proposal> hasHashingAlgorithm(
      IkeHashingAlgorithm hashingAlgorithm) {
    return new HasHashingAlgorithm(equalTo(hashingAlgorithm));
  }

  /**
   * Provides a matcher that matches if the IKE Phase 1 Proposal's value of {@code
   * authenticationMethod} matches specified {@code authenticationMethod}
   */
  public static Matcher<IkePhase1Proposal> hasAuthenticationMethod(
      IkeAuthenticationMethod authenticationMethod) {
    return new HasAuthenticationMethod(equalTo(authenticationMethod));
  }

  /**
   * Provides a matcher that matches if the IKE Phase 1 Proposal's value of {@code
   * diffieHellmanGroup} matches specified {@code diffieHellmanGroup}
   */
  public static Matcher<IkePhase1Proposal> hasDiffieHellmanGroup(
      DiffieHellmanGroup diffieHellmanGroup) {
    return new HasDiffieHellmanGroup(equalTo(diffieHellmanGroup));
  }

  /**
   * Provides a matcher that matches if the IKE Phase 1 Proposal's value of {@code lifeTimeSeconds}
   * matches specified {@code lifeTimeSeconds}
   */
  public static Matcher<IkePhase1Proposal> hasLifeTimeSeconds(Integer lifeTimeSeconds) {
    return new HasLifeTimeSeconds(equalTo(lifeTimeSeconds));
  }

  /**
   * Provides a matcher that matches if the IKE Phase 1 Proposal's value of {@code name} matches
   * specified {@code name}
   */
  public static Matcher<IkePhase1Proposal> hasName(String name) {
    return new HasName(equalTo(name));
  }

  private IkePhase1ProposalMatchers() {}

  private static final class HasHashingAlgorithm
      extends FeatureMatcher<IkePhase1Proposal, IkeHashingAlgorithm> {
    HasHashingAlgorithm(@Nonnull Matcher<? super IkeHashingAlgorithm> subMatcher) {
      super(subMatcher, "An IKE Phase 1 Proposal with HashingAlgorithm:", "HashingAlgorithm");
    }

    @Override
    protected IkeHashingAlgorithm featureValueOf(IkePhase1Proposal actual) {
      return actual.getHashingAlgorithm();
    }
  }

  private static final class HasAuthenticationMethod
      extends FeatureMatcher<IkePhase1Proposal, IkeAuthenticationMethod> {
    HasAuthenticationMethod(@Nonnull Matcher<? super IkeAuthenticationMethod> subMatcher) {
      super(
          subMatcher, "An IKE Phase 1 Proposal with AuthenticationMethod:", "AuthenticationMethod");
    }

    @Override
    protected IkeAuthenticationMethod featureValueOf(IkePhase1Proposal actual) {
      return actual.getAuthenticationMethod();
    }
  }

  private static final class HasDiffieHellmanGroup
      extends FeatureMatcher<IkePhase1Proposal, DiffieHellmanGroup> {
    HasDiffieHellmanGroup(@Nonnull Matcher<? super DiffieHellmanGroup> subMatcher) {
      super(subMatcher, "An IKE Phase 1 Proposal with DiffieHellmanGroup:", "DiffieHellmanGroup");
    }

    @Override
    protected DiffieHellmanGroup featureValueOf(IkePhase1Proposal actual) {
      return actual.getDiffieHellmanGroup();
    }
  }

  private static final class HasEncryptionAlgorithm
      extends FeatureMatcher<IkePhase1Proposal, EncryptionAlgorithm> {
    HasEncryptionAlgorithm(@Nonnull Matcher<? super EncryptionAlgorithm> subMatcher) {
      super(subMatcher, "An IKE Phase 1 Proposal with EncryptionAlgorithm:", "EncryptionAlgorithm");
    }

    @Override
    protected EncryptionAlgorithm featureValueOf(IkePhase1Proposal actual) {
      return actual.getEncryptionAlgorithm();
    }
  }

  private static final class HasLifeTimeSeconds extends FeatureMatcher<IkePhase1Proposal, Integer> {
    HasLifeTimeSeconds(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "An IKE Phase 1 Proposal with LifetimeInSeconds:", "LifetimeInSeconds");
    }

    @Override
    protected Integer featureValueOf(IkePhase1Proposal actual) {
      return actual.getLifetimeSeconds();
    }
  }

  private static final class HasName extends FeatureMatcher<IkePhase1Proposal, String> {
    HasName(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IKE Phase 1 Proposal with Name:", "Name");
    }

    @Override
    protected String featureValueOf(IkePhase1Proposal actual) {
      return actual.getName();
    }
  }
}
