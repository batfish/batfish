package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.VniSettings;
import org.batfish.datamodel.matchers.VniSettingsMatchersImpl.HasBumTransportIps;
import org.batfish.datamodel.matchers.VniSettingsMatchersImpl.HasBumTransportMethod;
import org.batfish.datamodel.matchers.VniSettingsMatchersImpl.HasSourceAddress;
import org.batfish.datamodel.matchers.VniSettingsMatchersImpl.HasUdpPort;
import org.batfish.datamodel.matchers.VniSettingsMatchersImpl.HasVlan;
import org.hamcrest.Matcher;

public class VniSettingsMatchers {

  private VniSettingsMatchers() {}

  /**
   * Provides a matcher that matches if the {@link VniSettings}'s BUM transport IPs matches the
   * {@code subMatcher}.
   */
  public static @Nonnull Matcher<VniSettings> hasBumTransportIps(
      @Nonnull Matcher<? super Iterable<Ip>> subMatcher) {
    return new HasBumTransportIps(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link VniSettings}'s BUM transport method matches the
   * {@code subMatcher}.
   */
  public static @Nonnull Matcher<VniSettings> hasBumTransportMethod(
      @Nonnull Matcher<? super BumTransportMethod> subMatcher) {
    return new HasBumTransportMethod(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link VniSettings}'s source address matches the {@code
   * subMatcher}.
   */
  public static @Nonnull Matcher<VniSettings> hasSourceAddress(
      @Nonnull Matcher<? super Ip> subMatcher) {
    return new HasSourceAddress(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link VniSettings}'s UDP port matches the {@code
   * subMatcher}.
   */
  public static @Nonnull Matcher<VniSettings> hasUdpPort(
      @Nonnull Matcher<? super Integer> subMatcher) {
    return new HasUdpPort(subMatcher);
  }

  /**
   * Provides a matcher that matches if the {@link VniSettings}'s vlan matches the {@code
   * subMatcher}.
   */
  public static @Nonnull Matcher<VniSettings> hasVlan(
      @Nonnull Matcher<? super Integer> subMatcher) {
    return new HasVlan(subMatcher);
  }
}
