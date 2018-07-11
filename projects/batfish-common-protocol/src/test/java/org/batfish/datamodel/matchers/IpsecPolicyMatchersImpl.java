package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.IkeGateway;
import org.batfish.datamodel.IpsecPolicy;
import org.batfish.datamodel.IpsecProposal;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IpsecPolicyMatchersImpl {

  static class HasIpsecProposals extends FeatureMatcher<IpsecPolicy, List<IpsecProposal>> {

    public HasIpsecProposals(@Nonnull Matcher<? super List<IpsecProposal>> subMatcher) {
      super(subMatcher, "An IpsecPolicy with IpsecProposals:", "IpsecProposals");
    }

    @Override
    protected List<IpsecProposal> featureValueOf(IpsecPolicy actual) {
      return actual.getProposals();
    }
  }

  static final class HasPfsKeyGroup extends FeatureMatcher<IpsecPolicy, DiffieHellmanGroup> {
    HasPfsKeyGroup(@Nonnull Matcher<? super DiffieHellmanGroup> subMatcher) {
      super(subMatcher, "An IPSec policy with PfsKeyGroup:", "PfsKeyGroup");
    }

    @Override
    protected DiffieHellmanGroup featureValueOf(IpsecPolicy actual) {
      return actual.getPfsKeyGroup();
    }
  }

  static final class HasIkeGateway extends FeatureMatcher<IpsecPolicy, IkeGateway> {
    HasIkeGateway(@Nonnull Matcher<? super IkeGateway> subMatcher) {
      super(subMatcher, "An IPSec policy with IkeGateway:", "IkeGateway");
    }

    @Override
    protected IkeGateway featureValueOf(IpsecPolicy actual) {
      return actual.getIkeGateway();
    }
  }

  static final class HasName extends FeatureMatcher<IpsecPolicy, String> {
    HasName(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An IPSec policy with Name:", "Name");
    }

    @Override
    protected String featureValueOf(IpsecPolicy actual) {
      return actual.getName();
    }
  }
}
