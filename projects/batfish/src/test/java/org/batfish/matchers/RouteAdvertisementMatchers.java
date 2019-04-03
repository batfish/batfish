package org.batfish.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.dataplane.rib.RouteAdvertisement;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;
import org.batfish.matchers.RouteAdvertisementMatchersImpl.HasReason;
import org.batfish.matchers.RouteAdvertisementMatchersImpl.HasRoute;
import org.hamcrest.Matcher;

/** Matchers for {@link RouteAdvertisement} */
@ParametersAreNonnullByDefault
public final class RouteAdvertisementMatchers {
  /**
   * Provides a matcher that matches when the {@code reason} is equal to the {@link
   * RouteAdvertisement}'s reason.
   */
  public static @Nonnull HasReason hasReason(Reason reason) {
    return new HasReason(equalTo(reason));
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
