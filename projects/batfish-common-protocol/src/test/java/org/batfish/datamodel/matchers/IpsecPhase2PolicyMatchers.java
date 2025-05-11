package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.contains;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.matchers.IpsecPhase2PolicyMatchersImpl.HasIpsecProposals;
import org.batfish.datamodel.matchers.IpsecPhase2PolicyMatchersImpl.HasPfsKeyGroups;
import org.hamcrest.Matcher;

public final class IpsecPhase2PolicyMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the IPSec phase 2
   * policy's IPSec Proposals.
   */
  public static @Nonnull HasIpsecProposals hasIpsecProposals(
      @Nonnull Matcher<? super List<String>> subMatcher) {
    return new HasIpsecProposals(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the IPSec phase 2
   * policy's PfsKeyGroup.
   */
  public static @Nonnull HasPfsKeyGroups hasPfsKeyGroup(
      Matcher<? super DiffieHellmanGroup> subMatcher) {
    return hasPfsKeyGroups(contains(subMatcher));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the IPSec phase 2
   * policy's PfsKeyGroups
   */
  public static @Nonnull HasPfsKeyGroups hasPfsKeyGroups(
      Matcher<? super Set<DiffieHellmanGroup>> subMatcher) {
    return new HasPfsKeyGroups(subMatcher);
  }

  private IpsecPhase2PolicyMatchers() {}
}
