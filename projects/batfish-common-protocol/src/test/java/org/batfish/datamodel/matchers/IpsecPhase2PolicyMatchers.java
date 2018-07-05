package org.batfish.datamodel.matchers;

import static org.hamcrest.CoreMatchers.equalTo;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.matchers.IpsecPhase2PolicyMatchersImpl.HasIpsecProposals;
import org.batfish.datamodel.matchers.IpsecPhase2PolicyMatchersImpl.HasPfsKeyGroup;
import org.hamcrest.Matcher;

public final class IpsecPhase2PolicyMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the Ipsec phase 2
   * policy's Ipsec Proposals.
   */
  public static @Nonnull HasIpsecProposals hasIpsecProposals(
      @Nonnull Matcher<? super List<String>> subMatcher) {
    return new HasIpsecProposals(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code pfsKeyGroup} matches the Ipsec phase 2
   * policy's PfsKeyGroup
   */
  public static @Nonnull HasPfsKeyGroup hasPfsKeyGroup(DiffieHellmanGroup pfsKeyGroup) {
    return new HasPfsKeyGroup(equalTo(pfsKeyGroup));
  }

  private IpsecPhase2PolicyMatchers() {}
}
