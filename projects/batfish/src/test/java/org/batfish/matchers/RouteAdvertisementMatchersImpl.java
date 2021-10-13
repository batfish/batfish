package org.batfish.matchers;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.dataplane.rib.RouteAdvertisement;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

@ParametersAreNonnullByDefault
public final class RouteAdvertisementMatchersImpl {

  static final class IsWithdrawn
      extends FeatureMatcher<RouteAdvertisement<? extends AbstractRouteDecorator>, Boolean> {
    IsWithdrawn(Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A RouteAdvertisement with withdrawn:", "withdrawn");
    }

    @Override
    protected Boolean featureValueOf(RouteAdvertisement<? extends AbstractRouteDecorator> actual) {
      return actual.isWithdrawn();
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
