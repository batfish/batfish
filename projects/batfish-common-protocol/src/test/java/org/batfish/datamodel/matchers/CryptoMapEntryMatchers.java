package org.batfish.datamodel.matchers;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.IkeGateway;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpsecProposal;
import org.batfish.datamodel.matchers.CryptoMapEntryMatchersImpl.HasAccessList;
import org.batfish.datamodel.matchers.CryptoMapEntryMatchersImpl.HasDynamic;
import org.batfish.datamodel.matchers.CryptoMapEntryMatchersImpl.HasIkeGateway;
import org.batfish.datamodel.matchers.CryptoMapEntryMatchersImpl.HasPeer;
import org.batfish.datamodel.matchers.CryptoMapEntryMatchersImpl.HasPfsKeyGroup;
import org.batfish.datamodel.matchers.CryptoMapEntryMatchersImpl.HasProposals;
import org.batfish.datamodel.matchers.CryptoMapEntryMatchersImpl.HasReferredDynamicMapSet;
import org.batfish.datamodel.matchers.CryptoMapEntryMatchersImpl.HasSequenceNumber;
import org.hamcrest.Matcher;

public final class CryptoMapEntryMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the Crypto-Map
   * entry's Ip Access List.
   */
  public static HasAccessList hasAccessList(@Nonnull Matcher<? super IpAccessList> subMatcher) {
    return new HasAccessList(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the Crypto-Map
   * entry's Dynamic value.
   */
  public static HasDynamic hasDynamic(@Nonnull Matcher<? super Boolean> subMatcher) {
    return new HasDynamic(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the Crypto-Map
   * entry's Ike Gateway.
   */
  public static HasIkeGateway hasIkeGateway(@Nonnull Matcher<? super IkeGateway> subMatcher) {
    return new HasIkeGateway(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the Crypto-Map
   * entry's peer.
   */
  public static HasPeer hasPeer(@Nonnull Matcher<? super Ip> subMatcher) {
    return new HasPeer(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the Crypto-Map
   * entry's proposals.
   */
  public static HasProposals hasProposals(
      @Nonnull Matcher<? super List<IpsecProposal>> subMatcher) {
    return new HasProposals(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the Crypto-Map
   * entry's referred dynamic set.
   */
  public static HasReferredDynamicMapSet hasReferredDynamicMapSet(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasReferredDynamicMapSet(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the Crypto-Map
   * entry's sequence number.
   */
  public static HasSequenceNumber hasSequenceNumber(@Nonnull Matcher<? super Integer> subMatcher) {
    return new HasSequenceNumber(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the Crypto-Map
   * entry's Pfs Keygroup.
   */
  public static HasPfsKeyGroup hasPfsKeyGroup(
      @Nonnull Matcher<? super DiffieHellmanGroup> subMatcher) {
    return new HasPfsKeyGroup(subMatcher);
  }

  private CryptoMapEntryMatchers() {}
}
