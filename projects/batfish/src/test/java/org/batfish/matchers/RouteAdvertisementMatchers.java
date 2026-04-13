package org.batfish.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.dataplane.rib.RouteAdvertisement;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

/** Matchers for {@link RouteAdvertisement} */
@ParametersAreNonnullByDefault
public final class RouteAdvertisementMatchers {
  /**
   * Provides a matcher that matches when the {@code reason} is equal to the {@link
   * RouteAdvertisement}'s reason.
   */
  public static @Nonnull Matcher<RouteAdvertisement<? extends AbstractRouteDecorator>> hasReason(
      Reason reason) {
    return new HasReason(equalTo(reason));
  }

  /**
   * Provides a matcher that matches when the {@code submatcher} matches to the {@link
   * RouteAdvertisement}'s route.
   */
  public static @Nonnull Matcher<RouteAdvertisement<? extends AbstractRouteDecorator>> hasRoute(
      Matcher<? super AbstractRouteDecorator> subMatcher) {
    return new HasRoute(subMatcher);
  }

  private RouteAdvertisementMatchers() {}

  private static final class HasReason
      extends FeatureMatcher<RouteAdvertisement<? extends AbstractRouteDecorator>, Reason> {
    HasReason(Matcher<? super Reason> subMatcher) {
      super(subMatcher, "A RouteAdvertisement with reason:", "reason");
    }

    @Override
    protected Reason featureValueOf(RouteAdvertisement<? extends AbstractRouteDecorator> actual) {
      return actual.getReason();
    }
  }

  private static final class HasRoute
      extends FeatureMatcher<
          RouteAdvertisement<? extends AbstractRouteDecorator>, AbstractRouteDecorator> {
    HasRoute(Matcher<? super AbstractRouteDecorator> subMatcher) {
      super(subMatcher, "A RouteAdvertisement with route:", "route");
    }

    @Override
    protected AbstractRouteDecorator featureValueOf(
        RouteAdvertisement<? extends AbstractRouteDecorator> actual) {
      return actual.getRoute();
    }
  }
}
