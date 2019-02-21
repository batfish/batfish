package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchersImpl.HasAdministrativeCost;
import org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchersImpl.HasMetric;
import org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchersImpl.HasNextHopInterface;
import org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchersImpl.HasNextHopIp;
import org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchersImpl.HasPrefix;
import org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchersImpl.HasProtocol;
import org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchersImpl.IsNonForwarding;
import org.hamcrest.Matcher;

public final class AbstractRouteDecoratorMatchers {

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * AbstractRouteDecorator}'s administrative cost.
   */
  public static @Nonnull HasAdministrativeCost hasAdministrativeCost(
      @Nonnull Matcher<? super Integer> subMatcher) {
    return new HasAdministrativeCost(subMatcher);
  }

  /**
   * Provides a matcher that matches when the {@code expectedMetric} is equal to the {@link
   * AbstractRouteDecorator}'s metric.
   */
  public static @Nonnull HasMetric hasMetric(Long expectedMetric) {
    return new HasMetric(equalTo(expectedMetric));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * AbstractRouteDecorator}'s metric.
   */
  public static @Nonnull HasMetric hasMetric(@Nonnull Matcher<? super Long> subMatcher) {
    return new HasMetric(subMatcher);
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * AbstractRouteDecorator}'s nextHopInterface.
   */
  public static @Nonnull HasNextHopInterface hasNextHopInterface(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasNextHopInterface(subMatcher);
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * AbstractRouteDecorator}'s nextHopIp.
   */
  public static @Nonnull HasNextHopIp hasNextHopIp(@Nonnull Matcher<? super Ip> subMatcher) {
    return new HasNextHopIp(subMatcher);
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * AbstractRouteDecorator}'s prefix.
   */
  public static @Nonnull HasPrefix hasPrefix(@Nonnull Matcher<? super Prefix> subMatcher) {
    return new HasPrefix(subMatcher);
  }

  /**
   * Provides a matcher that matches when the {@code expectedPrefix} is equal to the {@link
   * AbstractRouteDecorator}'s prefix.
   */
  public static @Nonnull HasPrefix hasPrefix(Prefix expectedPrefix) {
    return new HasPrefix(equalTo(expectedPrefix));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * AbstractRouteDecorator}'s protocol.
   */
  public static HasProtocol hasProtocol(@Nonnull Matcher<? super RoutingProtocol> subMatcher) {
    return new HasProtocol(subMatcher);
  }

  /**
   * Provides a matcher that matches when the {@code expectedProtocol} is equal to the {@link
   * AbstractRouteDecorator}'s protocol.
   */
  public static HasProtocol hasProtocol(RoutingProtocol expectedProtocol) {
    return new HasProtocol(equalTo(expectedProtocol));
  }

  /**
   * Provides a matcher that matches when the supplied {@code nonForwarding} is equal to the {@link
   * AbstractRouteDecorator}'s nonForwarding.
   */
  public static IsNonForwarding isNonForwarding(boolean nonForwarding) {
    return new IsNonForwarding(equalTo(nonForwarding));
  }

  private AbstractRouteDecoratorMatchers() {}
}
