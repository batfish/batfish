package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.bgp.EvpnAddressFamily;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.matchers.BgpNeighborMatchersImpl.HasClusterId;
import org.batfish.datamodel.matchers.BgpNeighborMatchersImpl.HasDescription;
import org.batfish.datamodel.matchers.BgpNeighborMatchersImpl.HasEnforceFirstAs;
import org.batfish.datamodel.matchers.BgpNeighborMatchersImpl.HasEvpnAddressFamily;
import org.batfish.datamodel.matchers.BgpNeighborMatchersImpl.HasIpv4UnicastAddressFamily;
import org.batfish.datamodel.matchers.BgpNeighborMatchersImpl.HasLocalAs;
import org.batfish.datamodel.matchers.BgpNeighborMatchersImpl.HasLocalIp;
import org.batfish.datamodel.matchers.BgpNeighborMatchersImpl.HasRemoteAs;
import org.hamcrest.Matcher;

/** Matchers for {@link BgpPeerConfig} */
@ParametersAreNonnullByDefault
public class BgpNeighborMatchers {
  /**
   * Provides a matcher that matches if the {@link BgpPeerConfig}'s clusterId is {@code
   * expectedClusterId}.
   */
  public static @Nonnull Matcher<BgpPeerConfig> hasClusterId(@Nullable Long expectedClusterId) {
    return new HasClusterId(equalTo(expectedClusterId));
  }

  /**
   * Provides a matcher that matches if the {@link BgpPeerConfig} has the specified {@code
   * expectedDescription}.
   */
  public static @Nonnull Matcher<BgpPeerConfig> hasDescription(String expectedDescription) {
    return new HasDescription(equalTo(expectedDescription));
  }

  /**
   * Provides a matcher that matches if the {@link BgpPeerConfig} has a {@link
   * Ipv4UnicastAddressFamily} that matches {@code submatcher}.
   */
  public static @Nonnull Matcher<BgpPeerConfig> hasIpv4UnicastAddressFamily(
      @Nonnull Matcher<? super Ipv4UnicastAddressFamily> subMatcher) {
    return new HasIpv4UnicastAddressFamily(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link BgpPeerConfig} has a {@link EvpnAddressFamily}
   * that matches {@code submatcher}.
   */
  public static @Nonnull Matcher<BgpPeerConfig> hasEvpnAddressFamily(
      @Nonnull Matcher<? super EvpnAddressFamily> subMatcher) {
    return new HasEvpnAddressFamily(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link BgpPeerConfig}'s localIp is matched by the
   * provided {@code subMatcher}.
   */
  public static @Nonnull Matcher<BgpPeerConfig> hasLocalIp(Matcher<? super Ip> subMatcher) {
    return new HasLocalIp(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link BgpPeerConfig} has the specified {@code
   * expectedLocalIp}.
   */
  public static @Nonnull Matcher<BgpPeerConfig> hasLocalIp(Ip expectedLocalIp) {
    return new HasLocalIp(equalTo(expectedLocalIp));
  }

  /** Provides a matcher that matches if the BGP neighbor has the specified localAs. */
  public static HasLocalAs hasLocalAs(Long localAs) {
    return new HasLocalAs(equalTo(localAs));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the BGP neighbor's
   * localAs.
   */
  public static HasLocalAs hasLocalAs(Matcher<? super Long> subMatcher) {
    return new HasLocalAs(subMatcher);
  }

  /**
   * Provides a matcher that matches if the BGP neighbor's value of {@code enforceFirstAs} matches
   * {@code subMatcher}
   */
  public static HasEnforceFirstAs hasEnforceFirstAs(Matcher<? super Boolean> subMatcher) {
    return new HasEnforceFirstAs(subMatcher);
  }

  /**
   * Provides a matcher that matches if the BGP neighbor's value of {@code enforceFirstAs} is {@code
   * true}.
   */
  public static HasEnforceFirstAs hasEnforceFirstAs() {
    return new HasEnforceFirstAs(equalTo(true));
  }

  /**
   * Provides a matcher that matches if the {@link BgpPeerConfig}'s remoteAs contains exactly
   * singleton {@code expectedRemoteAs}.
   */
  public static Matcher<BgpPeerConfig> hasRemoteAs(Long expectedRemoteAs) {
    return new HasRemoteAs(equalTo(LongSpace.of(expectedRemoteAs)));
  }

  /**
   * Provides a matcher that matches if the {@link BgpPeerConfig}'s remoteAs is {@code
   * expectedRemoteAs}.
   */
  public static Matcher<BgpPeerConfig> hasRemoteAs(LongSpace expectedRemoteAs) {
    return new HasRemoteAs(equalTo(expectedRemoteAs));
  }

  /**
   * Provides a matcher that matches if the {@link BgpPeerConfig}'s remoteAs is matched by the
   * provded {@code subMatcher}.
   */
  public static Matcher<BgpPeerConfig> hasRemoteAs(Matcher<? super LongSpace> subMatcher) {
    return new HasRemoteAs(subMatcher);
  }
}
