package org.batfish.datamodel.matchers;

import java.util.List;
import java.util.Set;
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

  static final class HasPfsKeyGroups
      extends FeatureMatcher<IpsecPhase2Policy, Set<DiffieHellmanGroup>> {
    HasPfsKeyGroups(@Nonnull Matcher<? super Set<DiffieHellmanGroup>> subMatcher) {
      super(subMatcher, "An IPSec phase 2 policy with PfsKeyGroups:", "PfsKeyGroups");
    }

    @Override
    protected Set<DiffieHellmanGroup> featureValueOf(IpsecPhase2Policy actual) {
      return actual.getPfsKeyGroups();
    }
  }
}
