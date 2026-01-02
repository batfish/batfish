package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchersImpl.HasAdministrativeCost;
import org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchersImpl.HasMetric;
import org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchersImpl.HasNextHop;
import org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchersImpl.HasNextHopInterface;
import org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchersImpl.HasNextHopIp;
import org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchersImpl.HasPrefix;
import org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchersImpl.HasProtocol;
import org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchersImpl.HasTag;
import org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchersImpl.IsNonForwarding;
import org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchersImpl.IsNonRouting;
import org.batfish.datamodel.route.nh.NextHop;
import org.hamcrest.Matcher;

public final class AbstractRouteDecoratorMatchers {

  /**
   * Provides a matcher that matches when the {@link AbstractRouteDecorator}'s administrative cost
   * is {@code expectedAdministrativeCost}.
   */
  public static @Nonnull HasAdministrativeCost hasAdministrativeCost(
      int expectedAdministrativeCost) {
    return new HasAdministrativeCost(equalTo(expectedAdministrativeCost));
  }

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
   * Provides a matcher that matches when the {@link AbstractRouteDecorator}'s nextHop is {@code
   * expectedNextHop}.
   */
  public static @Nonnull HasNextHop hasNextHop(@Nonnull NextHop expectedNextHop) {
    return new HasNextHop(equalTo(expectedNextHop));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * AbstractRouteDecorator}'s nextHop.
   */
  public static @Nonnull HasNextHop hasNextHop(@Nonnull Matcher<? super NextHop> subMatcher) {
    return new HasNextHop(subMatcher);
  }

  /**
   * Provides a matcher that matches when the route's nextHopInterface is {@code
   * expectedNextHopInterface}.
   */
  public static @Nonnull HasNextHopInterface hasNextHopInterface(
      @Nonnull String expectedNextHopInterface) {
    return new HasNextHopInterface(equalTo(expectedNextHopInterface));
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
   * Provides a matcher that matches when the {@link AbstractRouteDecorator}'s nextHopIp is {@code
   * expectedNextHopIp}.
   */
  public static @Nonnull HasNextHopIp hasNextHopIp(@Nonnull Ip expectedNextHopIp) {
    return new HasNextHopIp(equalTo(expectedNextHopIp));
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
   * A {@link Matcher} that matches when the supplied {@code subMatcher} matches the {@link
   * AbstractRouteDecorator}'s tag.
   */
  public static @Nonnull HasTag hasTag(Matcher<? super Long> subMatcher) {
    return new HasTag(subMatcher);
  }

  /**
   * A {@link Matcher} that matches if the {@link AbstractRouteDecorator}'s tag is {@code
   * expectedTag}.
   */
  public static @Nonnull HasTag hasTag(long expectedTag) {
    return hasTag(equalTo(expectedTag));
  }

  /**
   * Provides a matcher that matches when the supplied {@code nonForwarding} is equal to the {@link
   * AbstractRouteDecorator}'s nonForwarding.
   */
  public static IsNonForwarding isNonForwarding(boolean nonForwarding) {
    return new IsNonForwarding(equalTo(nonForwarding));
  }

  /**
   * Provides a matcher that matches when the supplied {@code nonRouting} is equal to the {@link
   * AbstractRouteDecorator}'s nonRouting.
   */
  public static IsNonRouting isNonRouting(boolean nonRouting) {
    return new IsNonRouting(equalTo(nonRouting));
  }

  private AbstractRouteDecoratorMatchers() {}
}
