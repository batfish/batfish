package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.HasCommunities;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.HasOriginType;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.HasWeight;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.IsBgpRouteThat;
import org.hamcrest.Matcher;

@ParametersAreNonnullByDefault
public final class BgpRouteMatchers {

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * Bgpv4Route}'s communities.
   */
  public static @Nonnull Matcher<Bgpv4Route> hasCommunities(Matcher<? super Set<Long>> subMatcher) {
    return new HasCommunities(subMatcher);
  }

  /**
   * Provides a matcher that matches when the supplied {@code expectedOriginType} is equal to the
   * {@link Bgpv4Route}'s originType.
   */
  public static @Nonnull Matcher<Bgpv4Route> hasOriginType(OriginType expectedOriginType) {
    return new HasOriginType(equalTo(expectedOriginType));
  }

  /**
   * Provides a matcher that matches when the supplied {@code expectedWeight} is equal to the {@link
   * Bgpv4Route}'s weight.
   */
  public static @Nonnull Matcher<Bgpv4Route> hasWeight(int expectedWeight) {
    return new HasWeight(equalTo(expectedWeight));
  }

  /**
   * Provides a matcher that matches when the {@link AbstractRoute} is a {@link Bgpv4Route} matched
   * by the provided {@code subMatcher}.
   */
  public static @Nonnull Matcher<AbstractRoute> isBgpRouteThat(
      Matcher<? super Bgpv4Route> subMatcher) {
    return new IsBgpRouteThat(subMatcher);
  }

  private BgpRouteMatchers() {}
}
