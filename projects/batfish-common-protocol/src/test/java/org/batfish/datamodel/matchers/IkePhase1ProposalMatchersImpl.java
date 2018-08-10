package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IkeAuthenticationMethod;
import org.batfish.datamodel.IkeHashingAlgorithm;
import org.batfish.datamodel.IkePhase1Proposal;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IkePhase1ProposalMatchersImpl {

  static final class HasHashingAlgorithm
      extends FeatureMatcher<IkePhase1Proposal, IkeHashingAlgorithm> {
    HasHashingAlgorithm(@Nonnull Matcher<? super IkeHashingAlgorithm> subMatcher) {
      super(subMatcher, "An IKE Phase 1 Proposal with HashingAlgorithm:", "HashingAlgorithm");
    }

    @Override
    protected IkeHashingAlgorithm featureValueOf(IkePhase1Proposal actual) {
      return actual.getHashingAlgorithm();
    }
  }

  static final class HasAuthenticationMethod
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

  static final class HasDiffieHellmanGroup
      extends FeatureMatcher<IkePhase1Proposal, DiffieHellmanGroup> {
    HasDiffieHellmanGroup(@Nonnull Matcher<? super DiffieHellmanGroup> subMatcher) {
      super(subMatcher, "An IKE Phase 1 Proposal with DiffieHellmanGroup:", "DiffieHellmanGroup");
    }

    @Override
    protected DiffieHellmanGroup featureValueOf(IkePhase1Proposal actual) {
      return actual.getDiffieHellmanGroup();
    }
  }

  static final class HasEncryptionAlgorithm
      extends FeatureMatcher<IkePhase1Proposal, EncryptionAlgorithm> {
    HasEncryptionAlgorithm(@Nonnull Matcher<? super EncryptionAlgorithm> subMatcher) {
      super(subMatcher, "An IKE Phase 1 Proposal with EncryptionAlgorithm:", "EncryptionAlgorithm");
    }

    @Override
    protected EncryptionAlgorithm featureValueOf(IkePhase1Proposal actual) {
      return actual.getEncryptionAlgorithm();
    }
  }

  static final class HasLifeTimeSeconds extends FeatureMatcher<IkePhase1Proposal, Integer> {
    HasLifeTimeSeconds(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "An IKE Phase 1 Proposal with LifetimeInSeconds:", "LifetimeInSeconds");
    }

    @Override
    protected Integer featureValueOf(IkePhase1Proposal actual) {
      return actual.getLifetimeSeconds();
    }
  }

  static final class HasName extends FeatureMatcher<IkePhase1Proposal, String> {
    HasName(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IKE Phase 1 Proposal with Name:", "Name");
    }

    @Override
    protected String featureValueOf(IkePhase1Proposal actual) {
      return actual.getName();
    }
  }
}
