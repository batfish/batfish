package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.contains;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.IpsecPhase2Policy;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class IpsecPhase2PolicyMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the IPSec phase 2
   * policy's IPSec Proposals.
   */
  public static @Nonnull Matcher<IpsecPhase2Policy> hasIpsecProposals(
      @Nonnull Matcher<? super List<String>> subMatcher) {
    return new HasIpsecProposals(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the IPSec phase 2
   * policy's PfsKeyGroup.
   */
  public static @Nonnull Matcher<IpsecPhase2Policy> hasPfsKeyGroup(
      Matcher<? super DiffieHellmanGroup> subMatcher) {
    return hasPfsKeyGroups(contains(subMatcher));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the IPSec phase 2
   * policy's PfsKeyGroups
   */
  public static @Nonnull Matcher<IpsecPhase2Policy> hasPfsKeyGroups(
      Matcher<? super Set<DiffieHellmanGroup>> subMatcher) {
    return new HasPfsKeyGroups(subMatcher);
  }

  private IpsecPhase2PolicyMatchers() {}

  private static final class HasIpsecProposals
      extends FeatureMatcher<IpsecPhase2Policy, List<String>> {

    public HasIpsecProposals(@Nonnull Matcher<? super List<String>> subMatcher) {
      super(subMatcher, "An IPSec phase 2 policy with IpsecProposals:", "IpsecProposals");
    }

    @Override
    protected List<String> featureValueOf(IpsecPhase2Policy actual) {
      return actual.getProposals();
    }
  }

  private static final class HasPfsKeyGroups
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
