package org.batfish.datamodel.matchers;

import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasMetric;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasNextHopIp;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasProtocol;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IsisRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class IsisRouteMatchers {

  public static @Nonnull Matcher<IsisRoute> hasDown() {
    return new HasDown(equalTo(true));
  }

  public static @Nonnull Matcher<IsisRoute> hasOverload(boolean expectedOverload) {
    return new HasOverload(equalTo(expectedOverload));
  }

  /** Matches with a route with the given prefix, next hop IP, and metric */
  public static Matcher<IsisRoute> isisRouteWith(Prefix prefix, Ip nextHopIp, long metric) {
    return allOf(hasPrefix(prefix), hasNextHopIp(equalTo(nextHopIp)), hasMetric(metric));
  }

  /** Matches with a route with the given prefix, next hop IP, metric, and overload bit */
  public static Matcher<IsisRoute> isisRouteWith(
      Prefix prefix, Ip nextHopIp, long metric, boolean overload, RoutingProtocol protocol) {
    return allOf(
        hasPrefix(prefix),
        hasNextHopIp(equalTo(nextHopIp)),
        hasMetric(metric),
        hasOverload(overload),
        hasProtocol(protocol));
  }

  public static @Nonnull Matcher<AbstractRouteDecorator> isIsisRouteThat(
      @Nonnull Matcher<? super IsisRoute> subMatcher) {
    return new IsIsisRouteThat(subMatcher);
  }

  private IsisRouteMatchers() {}

  private static final class HasDown extends FeatureMatcher<IsisRoute, Boolean> {
    HasDown(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An IsisRoute with down:", "down");
    }

    @Override
    protected Boolean featureValueOf(IsisRoute actual) {
      return actual.getDown();
    }
  }

  private static final class HasOverload extends FeatureMatcher<IsisRoute, Boolean> {
    HasOverload(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An IsisRoute with overload:", "overload");
    }

    @Override
    protected Boolean featureValueOf(IsisRoute actual) {
      return actual.getOverload();
    }
  }

  private static final class IsIsisRouteThat
      extends IsInstanceThat<AbstractRouteDecorator, IsisRoute> {
    IsIsisRouteThat(Matcher<? super IsisRoute> subMatcher) {
      super(IsisRoute.class, subMatcher);
    }
  }
}
