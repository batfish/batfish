package org.batfish.datamodel.matchers;

import static org.hamcrest.CoreMatchers.equalTo;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.IkeGateway;
import org.batfish.datamodel.IpsecProposal;
import org.batfish.datamodel.matchers.IpsecPolicyMatchersImpl.HasIkeGateway;
import org.batfish.datamodel.matchers.IpsecPolicyMatchersImpl.HasIpsecProposals;
import org.batfish.datamodel.matchers.IpsecPolicyMatchersImpl.HasName;
import org.batfish.datamodel.matchers.IpsecPolicyMatchersImpl.HasPfsKeyGroup;
import org.hamcrest.Matcher;

public final class IpsecPolicyMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the IPSec policy's
   * IPSec Proposals.
   */
  public static @Nonnull HasIpsecProposals hasIpsecProposals(
      @Nonnull Matcher<? super List<IpsecProposal>> subMatcher) {
    return new HasIpsecProposals(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the IPSec policy's
   * Ike Gateway
   */
  public static @Nonnull HasIkeGateway hasIkeGateway(
      @Nonnull Matcher<? super IkeGateway> subMatcher) {
    return new HasIkeGateway(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code pfsKeyGroup} matches the IPSec policy's
   * PfsKeyGroup
   */
  public static @Nonnull HasPfsKeyGroup hasPfsKeyGroup(DiffieHellmanGroup pfsKeyGroup) {
    return new HasPfsKeyGroup(equalTo(pfsKeyGroup));
  }

  /**
   * Provides a matcher that matches if the provided {@code name} matches the IPSec policy's name
   */
  public static @Nonnull HasName hasName(String name) {
    return new HasName(equalTo(name));
  }

  private IpsecPolicyMatchers() {}
}
