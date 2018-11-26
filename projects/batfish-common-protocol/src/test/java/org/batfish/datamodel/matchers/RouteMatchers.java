package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.matchers.RouteMatchersImpl.HasAdministrativeCost;
import org.batfish.datamodel.matchers.RouteMatchersImpl.HasMetric;
import org.batfish.datamodel.matchers.RouteMatchersImpl.HasNextHop;
import org.batfish.datamodel.matchers.RouteMatchersImpl.HasNextHopInterface;
import org.batfish.datamodel.matchers.RouteMatchersImpl.HasNextHopIp;
import org.batfish.datamodel.matchers.RouteMatchersImpl.HasPrefix;
import org.batfish.datamodel.matchers.RouteMatchersImpl.HasProtocol;
import org.hamcrest.Matcher;

/** Matchers for {@link Route}. */
public final class RouteMatchers {
  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * Route}'s administrative cost.
   */
  public static @Nonnull HasAdministrativeCost hasAdministrativeCost(
      @Nonnull Matcher<? super Integer> subMatcher) {
    return new HasAdministrativeCost(subMatcher);
  }

  /**
   * Provides a matcher that matches when the {@code expectedMetric} is equal to the {@link Route}'s
   * metric.
   */
  public static @Nonnull HasMetric hasMetric(Long expectedMetric) {
    return new HasMetric(equalTo(expectedMetric));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * Route}'s metric.
   */
  public static @Nonnull HasMetric hasMetric(@Nonnull Matcher<? super Long> subMatcher) {
    return new HasMetric(subMatcher);
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * Route}'s nextHopInterface.
   */
  public static @Nonnull HasNextHop hasNextHop(@Nonnull Matcher<? super String> subMatcher) {
    return new HasNextHop(subMatcher);
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * Route}'s nextHopInterface.
   */
  public static @Nonnull HasNextHopInterface hasNextHopInterface(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasNextHopInterface(subMatcher);
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * Route}'s nextHopIp.
   */
  public static @Nonnull HasNextHopIp hasNextHopIp(@Nonnull Matcher<? super Ip> subMatcher) {
    return new HasNextHopIp(subMatcher);
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * Route}'s prefix.
   */
  public static @Nonnull HasPrefix hasPrefix(@Nonnull Matcher<? super Prefix> subMatcher) {
    return new HasPrefix(subMatcher);
  }

  /**
   * Provides a matcher that matches when the {@code expectedPrefix} is equal to the {@link Route}'s
   * prefix.
   */
  public static @Nonnull HasPrefix hasPrefix(Prefix expectedPrefix) {
    return new HasPrefix(equalTo(expectedPrefix));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * Route}'s protocol.
   */
  public static HasProtocol hasProtocol(@Nonnull Matcher<? super RoutingProtocol> subMatcher) {
    return new HasProtocol(subMatcher);
  }

  /**
   * Provides a matcher that matches when the {@code expectedProtocol} is equal to the {@link
   * Route}'s protocol.
   */
  public static HasProtocol hasProtocol(RoutingProtocol expectedProtocol) {
    return new HasProtocol(equalTo(expectedProtocol));
  }

  private RouteMatchers() {}
}
