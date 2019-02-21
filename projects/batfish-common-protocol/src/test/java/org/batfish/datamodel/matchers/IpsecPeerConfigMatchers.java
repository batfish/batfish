package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpsecDynamicPeerConfig;
import org.batfish.datamodel.IpsecStaticPeerConfig;
import org.batfish.datamodel.matchers.IpsecPeerConfigMatchersImpl.HasDestinationAddress;
import org.batfish.datamodel.matchers.IpsecPeerConfigMatchersImpl.HasIkePhase1Policies;
import org.batfish.datamodel.matchers.IpsecPeerConfigMatchersImpl.HasIkePhase1Policy;
import org.batfish.datamodel.matchers.IpsecPeerConfigMatchersImpl.HasIpsecPolicy;
import org.batfish.datamodel.matchers.IpsecPeerConfigMatchersImpl.HasLocalAddress;
import org.batfish.datamodel.matchers.IpsecPeerConfigMatchersImpl.HasPolicyAccessList;
import org.batfish.datamodel.matchers.IpsecPeerConfigMatchersImpl.HasSourceInterface;
import org.batfish.datamodel.matchers.IpsecPeerConfigMatchersImpl.HasTunnelInterface;
import org.batfish.datamodel.matchers.IpsecPeerConfigMatchersImpl.IsIpsecDynamicPeerConfig;
import org.batfish.datamodel.matchers.IpsecPeerConfigMatchersImpl.IsIpsecStaticPeerConfig;
import org.hamcrest.Matcher;

public final class IpsecPeerConfigMatchers {

  /**
   * Provides a matcher that matches if the provided {@code localAddress} matches the IPSec peer
   * config's {@code localAddress}
   */
  public static @Nonnull HasLocalAddress hasLocalAddress(Ip localAddress) {
    return new HasLocalAddress(equalTo(localAddress));
  }

  /**
   * Provides a matcher that matches if the provided {@code sourceInterface} matches the IPSec peer
   * config's {@code sourceInterface}
   */
  public static @Nonnull HasSourceInterface hasSourceInterface(String sourceInterface) {
    return new HasSourceInterface(equalTo(sourceInterface));
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
   * config's {@code ikePhase1Policy}
   */
  public static @Nonnull HasIkePhase1Policy hasIkePhase1Policy(String ikePhase1Policy) {
    return new HasIkePhase1Policy(equalTo(ikePhase1Policy));
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

  /**
   * Provides a matcher that matches if the object is a {@link IpsecDynamicPeerConfig} matched by
   * the provided {@code subMatcher}.
   */
  public static IsIpsecDynamicPeerConfig isIpsecDynamicPeerConfigThat(
      @Nonnull Matcher<? super IpsecDynamicPeerConfig> subMatcher) {
    return new IsIpsecDynamicPeerConfig(subMatcher);
  }

  private IpsecPeerConfigMatchers() {}
}
