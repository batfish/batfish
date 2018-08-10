package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IkeAuthenticationMethod;
import org.batfish.datamodel.IkeHashingAlgorithm;
import org.batfish.datamodel.IkeProposal;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IkeProposalMatchersImpl {

  static final class HasAuthenticationAlgorithm
      extends FeatureMatcher<IkeProposal, IkeHashingAlgorithm> {
    HasAuthenticationAlgorithm(@Nonnull Matcher<? super IkeHashingAlgorithm> subMatcher) {
      super(subMatcher, "An IKE Proposal with Auth algo:", "AuthenticationAlgorithm");
    }

    @Override
    protected IkeHashingAlgorithm featureValueOf(IkeProposal actual) {
      return actual.getAuthenticationAlgorithm();
    }
  }

  static final class HasAuthenticationMethod
      extends FeatureMatcher<IkeProposal, IkeAuthenticationMethod> {
    HasAuthenticationMethod(@Nonnull Matcher<? super IkeAuthenticationMethod> subMatcher) {
      super(subMatcher, "An IKE Proposal with Auth method:", "AuthenticationMethod");
    }

    @Override
    protected IkeAuthenticationMethod featureValueOf(IkeProposal actual) {
      return actual.getAuthenticationMethod();
    }
  }

  static final class HasDiffieHellmanGroup extends FeatureMatcher<IkeProposal, DiffieHellmanGroup> {
    HasDiffieHellmanGroup(@Nonnull Matcher<? super DiffieHellmanGroup> subMatcher) {
      super(subMatcher, "An IKE Proposal with group:", "DiffieHellmanGroup");
    }

    @Override
    protected DiffieHellmanGroup featureValueOf(IkeProposal actual) {
      return actual.getDiffieHellmanGroup();
    }
  }

  static final class HasEncryptionAlgorithm
      extends FeatureMatcher<IkeProposal, EncryptionAlgorithm> {
    HasEncryptionAlgorithm(@Nonnull Matcher<? super EncryptionAlgorithm> subMatcher) {
      super(subMatcher, "An IKE Proposal with encryption algo:", "EncryptionAlgorithm");
    }

    @Override
    protected EncryptionAlgorithm featureValueOf(IkeProposal actual) {
      return actual.getEncryptionAlgorithm();
    }
  }

  static final class HasLifeTimeSeconds extends FeatureMatcher<IkeProposal, Integer> {
    HasLifeTimeSeconds(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "An IKE Proposal with lifetime in seconds:", "Lifetime");
    }

    @Override
    protected Integer featureValueOf(IkeProposal actual) {
      return actual.getLifetimeSeconds();
    }
  }
}
