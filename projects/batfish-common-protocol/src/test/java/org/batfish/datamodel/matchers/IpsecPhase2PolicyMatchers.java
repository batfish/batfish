package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.matchers.IpsecPhase2PolicyMatchersImpl.HasIpsecProposals;
import org.batfish.datamodel.matchers.IpsecPhase2PolicyMatchersImpl.HasPfsKeyGroup;
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
   * policy's PfsKeyGroup
   */
  public static @Nonnull HasPfsKeyGroup hasPfsKeyGroup(
      Matcher<? super DiffieHellmanGroup> subMatcher) {
    return new HasPfsKeyGroup(subMatcher);
  }

  private IpsecPhase2PolicyMatchers() {}
}
