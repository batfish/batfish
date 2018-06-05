package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.IkeGateway;
import org.batfish.datamodel.IpsecPolicy;
import org.batfish.datamodel.IpsecProposal;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IpsecPolicyMatchersImpl {

  static final class HasIpsecProposal extends FeatureMatcher<IpsecPolicy, IpsecProposal> {
    private final String _name;

    HasIpsecProposal(@Nonnull String name, @Nonnull Matcher<? super IpsecProposal> subMatcher) {
      super(
          subMatcher,
          String.format("An Ipsec policy with IpsecProposal %s:", name),
          String.format("IpsecProposal %s", name));
      _name = name;
    }

    @Override
    protected IpsecProposal featureValueOf(IpsecPolicy actual) {
      return actual.getProposals().get(_name);
    }
  }

  static final class HasPfsKeyGroup extends FeatureMatcher<IpsecPolicy, DiffieHellmanGroup> {
    HasPfsKeyGroup(@Nonnull Matcher<? super DiffieHellmanGroup> subMatcher) {
      super(subMatcher, "An Ipsec policy with PfsKeyGroup:", "PfsKeyGroup");
    }

    @Override
    protected DiffieHellmanGroup featureValueOf(IpsecPolicy actual) {
      return actual.getPfsKeyGroup();
    }
  }

  static final class HasIkeGateway extends FeatureMatcher<IpsecPolicy, IkeGateway> {
    HasIkeGateway(@Nonnull Matcher<? super IkeGateway> subMatcher) {
      super(subMatcher, "An Ipsec policy with IkeGateway:", "IkeGateway");
    }

    @Override
    protected IkeGateway featureValueOf(IpsecPolicy actual) {
      return actual.getIkeGateway();
    }
  }
}
