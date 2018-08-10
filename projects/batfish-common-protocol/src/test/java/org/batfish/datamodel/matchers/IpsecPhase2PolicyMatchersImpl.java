package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.IpsecPhase2Policy;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IpsecPhase2PolicyMatchersImpl {

  static class HasIpsecProposals extends FeatureMatcher<IpsecPhase2Policy, List<String>> {

    public HasIpsecProposals(@Nonnull Matcher<? super List<String>> subMatcher) {
      super(subMatcher, "An IPSec phase 2 policy with IpsecProposals:", "IpsecProposals");
    }

    @Override
    protected List<String> featureValueOf(IpsecPhase2Policy actual) {
      return actual.getProposals();
    }
  }

  static final class HasPfsKeyGroup extends FeatureMatcher<IpsecPhase2Policy, DiffieHellmanGroup> {
    HasPfsKeyGroup(@Nonnull Matcher<? super DiffieHellmanGroup> subMatcher) {
      super(subMatcher, "An IPSec phase 2 policy with PfsKeyGroup:", "PfsKeyGroup");
    }

    @Override
    protected DiffieHellmanGroup featureValueOf(IpsecPhase2Policy actual) {
      return actual.getPfsKeyGroup();
    }
  }
}
