package org.batfish.datamodel.matchers;

import static org.hamcrest.CoreMatchers.equalTo;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpsecDynamicPeerConfig;
import org.batfish.datamodel.IpsecPeerConfig;
import org.batfish.datamodel.matchers.IpsecDynamicPeerConfigMatchersImpl.HasIkePhase1Policies;
import org.batfish.datamodel.matchers.IpsecDynamicPeerConfigMatchersImpl.HasIpsecPolicy;
import org.batfish.datamodel.matchers.IpsecDynamicPeerConfigMatchersImpl.HasPeerConfigs;
import org.batfish.datamodel.matchers.IpsecDynamicPeerConfigMatchersImpl.HasPhysicalInterface;
import org.batfish.datamodel.matchers.IpsecDynamicPeerConfigMatchersImpl.HasPolicyAccessList;
import org.batfish.datamodel.matchers.IpsecDynamicPeerConfigMatchersImpl.HasSourceAddress;
import org.batfish.datamodel.matchers.IpsecDynamicPeerConfigMatchersImpl.HasTunnelInterface;
import org.batfish.datamodel.matchers.IpsecDynamicPeerConfigMatchersImpl.IsIpsecDynamicPeerConfig;
import org.hamcrest.Matcher;

public final class IpsecDynamicPeerConfigMatchers {

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
   * Provides a matcher that matches if the provided {@code submatcher} matches the IPSec peer
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
   * config's {@code ikePhase1Policies}
   */
  public static @Nonnull HasIkePhase1Policies hasIkePhase1Policies(
      @Nonnull Matcher<? super List<String>> subMatcher) {
    return new HasIkePhase1Policies(subMatcher);
  }

  /**
   * Provides a matcher that matches if the provided {@code submatcher} matches the IPSec peer
   * config's {@code peerConfigs}
   */
  public static @Nonnull HasPeerConfigs hasPeerConfigs(
      @Nonnull Matcher<? super List<IpsecPeerConfig>> subMatcher) {
    return new HasPeerConfigs(subMatcher);
  }

  /**
   * Provides a matcher that matches if the object is a {@link IpsecDynamicPeerConfig} matched by
   * the provided {@code subMatcher}.
   */
  public static IsIpsecDynamicPeerConfig isIpsecDynamicPeerConfigThat(
      @Nonnull Matcher<? super IpsecDynamicPeerConfig> subMatcher) {
    return new IsIpsecDynamicPeerConfig(subMatcher);
  }

  private IpsecDynamicPeerConfigMatchers() {}
}
