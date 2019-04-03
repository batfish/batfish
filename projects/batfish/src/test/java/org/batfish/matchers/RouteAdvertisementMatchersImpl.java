package org.batfish.matchers;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.dataplane.rib.RouteAdvertisement;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

@ParametersAreNonnullByDefault
public final class RouteAdvertisementMatchersImpl {

  static final class HasReason
      extends FeatureMatcher<RouteAdvertisement<? extends AbstractRouteDecorator>, Reason> {
    HasReason(Matcher<? super Reason> subMatcher) {
      super(subMatcher, "A RouteAdvertisement with reason:", "reason");
    }

    @Override
    protected Reason featureValueOf(RouteAdvertisement<? extends AbstractRouteDecorator> actual) {
      return actual.getReason();
    }
  }

  static final class HasRoute
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

  private RouteAdvertisementMatchersImpl() {}
}
