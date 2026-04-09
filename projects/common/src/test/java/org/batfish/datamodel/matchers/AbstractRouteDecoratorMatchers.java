package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.route.nh.NextHop;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class AbstractRouteDecoratorMatchers {

  /**
   * Provides a matcher that matches when the {@link AbstractRouteDecorator}'s administrative cost
   * is {@code expectedAdministrativeCost}.
   */
  public static @Nonnull Matcher<AbstractRouteDecorator> hasAdministrativeCost(
      long expectedAdministrativeCost) {
    return new HasAdministrativeCost(equalTo(expectedAdministrativeCost));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * AbstractRouteDecorator}'s administrative cost.
   */
  public static @Nonnull Matcher<AbstractRouteDecorator> hasAdministrativeCost(
      @Nonnull Matcher<? super Long> subMatcher) {
    return new HasAdministrativeCost(subMatcher);
  }

  /**
   * Provides a matcher that matches when the {@code expectedMetric} is equal to the {@link
   * AbstractRouteDecorator}'s metric.
   */
  public static @Nonnull Matcher<AbstractRouteDecorator> hasMetric(Long expectedMetric) {
    return new HasMetric(equalTo(expectedMetric));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * AbstractRouteDecorator}'s metric.
   */
  public static @Nonnull Matcher<AbstractRouteDecorator> hasMetric(
      @Nonnull Matcher<? super Long> subMatcher) {
    return new HasMetric(subMatcher);
  }

  /**
   * Provides a matcher that matches when the {@link AbstractRouteDecorator}'s nextHop is {@code
   * expectedNextHop}.
   */
  public static @Nonnull Matcher<AbstractRouteDecorator> hasNextHop(
      @Nonnull NextHop expectedNextHop) {
    return new HasNextHop(equalTo(expectedNextHop));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * AbstractRouteDecorator}'s nextHop.
   */
  public static @Nonnull Matcher<AbstractRouteDecorator> hasNextHop(
      @Nonnull Matcher<? super NextHop> subMatcher) {
    return new HasNextHop(subMatcher);
  }

  /**
   * Provides a matcher that matches when the route's nextHopInterface is {@code
   * expectedNextHopInterface}.
   */
  public static @Nonnull Matcher<AbstractRouteDecorator> hasNextHopInterface(
      @Nonnull String expectedNextHopInterface) {
    return new HasNextHopInterface(equalTo(expectedNextHopInterface));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * AbstractRouteDecorator}'s nextHopInterface.
   */
  public static @Nonnull Matcher<AbstractRouteDecorator> hasNextHopInterface(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasNextHopInterface(subMatcher);
  }

  /**
   * Provides a matcher that matches when the {@link AbstractRouteDecorator}'s nextHopIp is {@code
   * expectedNextHopIp}.
   */
  public static @Nonnull Matcher<AbstractRouteDecorator> hasNextHopIp(
      @Nonnull Ip expectedNextHopIp) {
    return new HasNextHopIp(equalTo(expectedNextHopIp));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * AbstractRouteDecorator}'s nextHopIp.
   */
  public static @Nonnull Matcher<AbstractRouteDecorator> hasNextHopIp(
      @Nonnull Matcher<? super Ip> subMatcher) {
    return new HasNextHopIp(subMatcher);
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * AbstractRouteDecorator}'s prefix.
   */
  public static @Nonnull Matcher<AbstractRouteDecorator> hasPrefix(
      @Nonnull Matcher<? super Prefix> subMatcher) {
    return new HasPrefix(subMatcher);
  }

  /**
   * Provides a matcher that matches when the {@code expectedPrefix} is equal to the {@link
   * AbstractRouteDecorator}'s prefix.
   */
  public static @Nonnull Matcher<AbstractRouteDecorator> hasPrefix(Prefix expectedPrefix) {
    return new HasPrefix(equalTo(expectedPrefix));
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * AbstractRouteDecorator}'s protocol.
   */
  public static Matcher<AbstractRouteDecorator> hasProtocol(
      @Nonnull Matcher<? super RoutingProtocol> subMatcher) {
    return new HasProtocol(subMatcher);
  }

  /**
   * Provides a matcher that matches when the {@code expectedProtocol} is equal to the {@link
   * AbstractRouteDecorator}'s protocol.
   */
  public static Matcher<AbstractRouteDecorator> hasProtocol(RoutingProtocol expectedProtocol) {
    return new HasProtocol(equalTo(expectedProtocol));
  }

  /**
   * A {@link Matcher} that matches when the supplied {@code subMatcher} matches the {@link
   * AbstractRouteDecorator}'s tag.
   */
  public static @Nonnull Matcher<AbstractRouteDecorator> hasTag(Matcher<? super Long> subMatcher) {
    return new HasTag(subMatcher);
  }

  /**
   * A {@link Matcher} that matches if the {@link AbstractRouteDecorator}'s tag is {@code
   * expectedTag}.
   */
  public static @Nonnull Matcher<AbstractRouteDecorator> hasTag(long expectedTag) {
    return hasTag(equalTo(expectedTag));
  }

  /**
   * Provides a matcher that matches when the supplied {@code nonForwarding} is equal to the {@link
   * AbstractRouteDecorator}'s nonForwarding.
   */
  public static Matcher<AbstractRouteDecorator> isNonForwarding(boolean nonForwarding) {
    return new IsNonForwarding(equalTo(nonForwarding));
  }

  /**
   * Provides a matcher that matches when the supplied {@code nonRouting} is equal to the {@link
   * AbstractRouteDecorator}'s nonRouting.
   */
  public static Matcher<AbstractRouteDecorator> isNonRouting(boolean nonRouting) {
    return new IsNonRouting(equalTo(nonRouting));
  }

  private AbstractRouteDecoratorMatchers() {}

  private static final class HasAdministrativeCost
      extends FeatureMatcher<AbstractRouteDecorator, Long> {
    HasAdministrativeCost(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "An AbstractRouteDecorator with administrativeCost:", "administrativeCost");
    }

    @Override
    protected Long featureValueOf(AbstractRouteDecorator actual) {
      return actual.getAbstractRoute().getAdministrativeCost();
    }
  }

  private static final class HasMetric extends FeatureMatcher<AbstractRouteDecorator, Long> {
    HasMetric(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "An AbstractRouteDecorator with metric:", "metric");
    }

    @Override
    protected Long featureValueOf(AbstractRouteDecorator actual) {
      return actual.getAbstractRoute().getMetric();
    }
  }

  private static final class HasNextHop extends FeatureMatcher<AbstractRouteDecorator, NextHop> {
    HasNextHop(@Nonnull Matcher<? super NextHop> subMatcher) {
      super(subMatcher, "An AbstractRouteDecorator with nextHop:", "nextHop");
    }

    @Override
    protected NextHop featureValueOf(AbstractRouteDecorator actual) {
      return actual.getAbstractRoute().getNextHop();
    }
  }

  private static final class HasNextHopInterface
      extends FeatureMatcher<AbstractRouteDecorator, String> {
    HasNextHopInterface(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An AbstractRouteDecorator with nextHopInterface:", "nextHopInterface");
    }

    @Override
    protected String featureValueOf(AbstractRouteDecorator actual) {
      return actual.getAbstractRoute().getNextHopInterface();
    }
  }

  private static final class HasNextHopIp extends FeatureMatcher<AbstractRouteDecorator, Ip> {
    HasNextHopIp(@Nonnull Matcher<? super Ip> subMatcher) {
      super(subMatcher, "An AbstractRouteDecorator with nextHopIp:", "nextHopIp");
    }

    @Override
    protected Ip featureValueOf(AbstractRouteDecorator actual) {
      return actual.getAbstractRoute().getNextHopIp();
    }
  }

  private static final class HasPrefix extends FeatureMatcher<AbstractRouteDecorator, Prefix> {
    HasPrefix(@Nonnull Matcher<? super Prefix> subMatcher) {
      super(subMatcher, "An AbstractRouteDecorator with network:", "network");
    }

    @Override
    protected Prefix featureValueOf(AbstractRouteDecorator actual) {
      return actual.getNetwork();
    }
  }

  private static final class HasProtocol
      extends FeatureMatcher<AbstractRouteDecorator, RoutingProtocol> {
    HasProtocol(@Nonnull Matcher<? super RoutingProtocol> subMatcher) {
      super(subMatcher, "An AbstractRouteDecorator with protocol:", "protocol");
    }

    @Override
    protected RoutingProtocol featureValueOf(AbstractRouteDecorator actual) {
      return actual.getAbstractRoute().getProtocol();
    }
  }

  private static final class HasTag extends FeatureMatcher<AbstractRouteDecorator, Long> {
    HasTag(Matcher<? super Long> subMatcher) {
      super(subMatcher, "An AbstractRouteDecorator with tag", "tag");
    }

    @Override
    protected Long featureValueOf(AbstractRouteDecorator actual) {
      return actual.getAbstractRoute().getTag();
    }
  }

  private static final class IsNonForwarding
      extends FeatureMatcher<AbstractRouteDecorator, Boolean> {
    IsNonForwarding(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An AbstractRouteDecorator with nonRouting:", "nonRouting");
    }

    @Override
    protected Boolean featureValueOf(AbstractRouteDecorator actual) {
      return actual.getAbstractRoute().getNonForwarding();
    }
  }

  private static final class IsNonRouting extends FeatureMatcher<AbstractRouteDecorator, Boolean> {
    IsNonRouting(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An AbstractRouteDecorator with nonRouting:", "nonRouting");
    }

    @Override
    protected Boolean featureValueOf(AbstractRouteDecorator actual) {
      return actual.getAbstractRoute().getNonRouting();
    }
  }
}
