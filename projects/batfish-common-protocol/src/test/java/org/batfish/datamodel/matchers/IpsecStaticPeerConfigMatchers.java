package org.batfish.datamodel.matchers;

import static org.hamcrest.CoreMatchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpsecPeerConfig;
import org.batfish.datamodel.IpsecStaticPeerConfig;
import org.batfish.datamodel.matchers.IpsecStaticPeerConfigMatchersImpl.HasDestinationAddress;
import org.batfish.datamodel.matchers.IpsecStaticPeerConfigMatchersImpl.HasIkePhase1Policy;
import org.batfish.datamodel.matchers.IpsecStaticPeerConfigMatchersImpl.HasIpsecPolicy;
import org.batfish.datamodel.matchers.IpsecStaticPeerConfigMatchersImpl.HasPeerConfig;
import org.batfish.datamodel.matchers.IpsecStaticPeerConfigMatchersImpl.HasPhysicalInterface;
import org.batfish.datamodel.matchers.IpsecStaticPeerConfigMatchersImpl.HasPolicyAccessList;
import org.batfish.datamodel.matchers.IpsecStaticPeerConfigMatchersImpl.HasSourceAddress;
import org.batfish.datamodel.matchers.IpsecStaticPeerConfigMatchersImpl.HasTunnelInterface;
import org.batfish.datamodel.matchers.IpsecStaticPeerConfigMatchersImpl.IsIpsecStaticPeerConfig;
import org.hamcrest.Matcher;

public final class IpsecStaticPeerConfigMatchers {

  /**
   * Provides a matcher that matches if the provided {@code sourceAddress} matches the IPSec peer
   * config's {@code sourceAddress}
   */
  public static @Nonnull HasSourceAddress hasSourceAddress(Ip sourceAddress) {
    return new HasSourceAddress(equalTo(sourceAddress));
  }

  /**
   * Provides a matcher that matches if the provided {@code physicalInterface} matches the IPSec
   * peer config's {@code physicalInterface}
   */
  public static @Nonnull HasPhysicalInterface hasPhysicalInterface(String physicalInterface) {
    return new HasPhysicalInterface(equalTo(physicalInterface));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the IPSec peer
   * config's {@code tunnelInterface}
   */
  public static @Nonnull HasTunnelInterface hasTunnelInterface(Matcher<? super String> subMatcher) {
    return new HasTunnelInterface(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code ipsecPolicy} matches the IPSec peer
   * config's {@code ipsecPolicy}
   */
  public static @Nonnull HasIpsecPolicy hasIpsecPolicy(String ipsecPolicy) {
    return new HasIpsecPolicy(equalTo(ipsecPolicy));
  }

  /**
   * Provides a matcher that matches if the provided {@code submatcher} matches the IPSec peer
   * config's {@code policyAccessList}
   */
  public static @Nonnull HasPolicyAccessList hasPolicyAccessList(
      @Nonnull Matcher<? super IpAccessList> subMatcher) {
    return new HasPolicyAccessList(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code submatcher} matches the IPSec peer
   * config's {@code ikePhase1Policy}
   */
  public static @Nonnull HasIkePhase1Policy hasIkePhase1Policy(String ikePhase1Policy) {
    return new HasIkePhase1Policy(equalTo(ikePhase1Policy));
  }

  /**
   * Provides a matcher that matches if the provided {@code submatcher} matches the IPSec peer
   * config's {@code peerConfig}
   */
  public static @Nonnull HasPeerConfig hasPeerConfigs(
      @Nonnull Matcher<? super IpsecPeerConfig> subMatcher) {
    return new HasPeerConfig(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code destinationAddress} matches the IPSec
   * peer config's {@code destinationAddress}
   */
  public static @Nonnull HasDestinationAddress hasDestinationAddress(Ip destinationAddress) {
    return new HasDestinationAddress(equalTo(destinationAddress));
  }

  /**
   * Provides a matcher that matches if the object is a {@link IpsecStaticPeerConfig} matched by the
   * provided {@code subMatcher}.
   */
  public static IsIpsecStaticPeerConfig isIpsecStaticPeerConfigThat(
      @Nonnull Matcher<? super IpsecStaticPeerConfig> subMatcher) {
    return new IsIpsecStaticPeerConfig(subMatcher);
  }

  private IpsecStaticPeerConfigMatchers() {}
}
