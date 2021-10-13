package org.batfish.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.dataplane.rib.RouteAdvertisement;
import org.batfish.matchers.RouteAdvertisementMatchersImpl.HasRoute;
import org.batfish.matchers.RouteAdvertisementMatchersImpl.IsWithdrawn;
import org.hamcrest.Matcher;

/** Matchers for {@link RouteAdvertisement} */
@ParametersAreNonnullByDefault
public final class RouteAdvertisementMatchers {
  /** Provides a matcher that matches a {@link RouteAdvertisement} that adds its route. */
  public static @Nonnull IsWithdrawn isAdding() {
    return new IsWithdrawn(equalTo(false));
  }

  /**
   * Provides a matcher that matches when the {@code submatcher} matches to the {@link
   * RouteAdvertisement}'s route.
   */
  public static @Nonnull HasRoute hasRoute(Matcher<? super AbstractRouteDecorator> subMatcher) {
    return new HasRoute(subMatcher);
  }

  private RouteAdvertisementMatchers() {}
}
