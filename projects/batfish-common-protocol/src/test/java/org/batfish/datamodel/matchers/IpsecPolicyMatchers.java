package org.batfish.datamodel.matchers;

import static org.hamcrest.CoreMatchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.IkeGateway;
import org.batfish.datamodel.IpsecProposal;
import org.batfish.datamodel.matchers.IpsecPolicyMatchersImpl.HasIkeGateway;
import org.batfish.datamodel.matchers.IpsecPolicyMatchersImpl.HasIpsecProposal;
import org.batfish.datamodel.matchers.IpsecPolicyMatchersImpl.HasPfsKeyGroup;
import org.hamcrest.Matcher;

public final class IpsecPolicyMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the Ipsec policy's
   * Ipsec Proposal with specified name.
   */
  public static HasIpsecProposal hasIpsecProposal(
      @Nonnull String name, @Nonnull Matcher<? super IpsecProposal> subMatcher) {
    return new HasIpsecProposal(name, subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the Ipsec policy's
   * Ike Gateway
   */
  public static HasIkeGateway hasIkeGateway(@Nonnull Matcher<? super IkeGateway> subMatcher) {
    return new HasIkeGateway(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the Ipsec policy's
   * PfsKeyGroupy
   */
  public static HasPfsKeyGroup hasPfsKeyGroup(DiffieHellmanGroup pfsKeyGroup) {
    return new HasPfsKeyGroup(equalTo(pfsKeyGroup));
  }

  private IpsecPolicyMatchers() {}
}
