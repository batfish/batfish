package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.EvpnType3Route;
import org.batfish.datamodel.EvpnType5Route;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.HasCommunities;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.HasOriginType;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.HasRouteDistinguisher;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.HasWeight;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.IsBgpv4RouteThat;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.IsEvpnType3RouteThat;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.IsEvpnType5RouteThat;
import org.hamcrest.Matcher;

@ParametersAreNonnullByDefault
public final class BgpRouteMatchers {

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * BgpRoute}'s communities.
   */
  public static @Nonnull Matcher<BgpRoute<?, ?>> hasCommunities(
      Matcher<? super Set<Community>> subMatcher) {
    return new HasCommunities(subMatcher);
  }

  /**
   * Provides a matcher that matches when the supplied {@code expectedOriginType} is equal to the
   * {@link BgpRoute}'s originType.
   */
  public static @Nonnull Matcher<BgpRoute<?, ?>> hasOriginType(OriginType expectedOriginType) {
    return new HasOriginType(equalTo(expectedOriginType));
  }

  /**
   * Provides a matcher that matches when the supplied {@code expectedRouteDistinguisher} is equal
   * to the {@link EvpnRoute}'s route distinguisher.
   */
  public static @Nonnull Matcher<EvpnRoute<?, ?>> hasRouteDistinguisher(
      RouteDistinguisher expectedRouteDistinguisher) {
    return new HasRouteDistinguisher(equalTo(expectedRouteDistinguisher));
  }

  /**
   * Provides a matcher that matches when the supplied {@code expectedWeight} is equal to the {@link
   * BgpRoute}'s weight.
   */
  public static @Nonnull Matcher<BgpRoute<?, ?>> hasWeight(int expectedWeight) {
    return new HasWeight(equalTo(expectedWeight));
  }

  /**
   * Provides a matcher that matches when the {@link AbstractRoute} is a {@link Bgpv4Route} matched
   * by the provided {@code subMatcher}.
   */
  public static @Nonnull Matcher<AbstractRoute> isBgpv4RouteThat(
      Matcher<? super Bgpv4Route> subMatcher) {
    return new IsBgpv4RouteThat(subMatcher);
  }

  /**
   * Provides a matcher that matches when the {@link AbstractRoute} is a {@link EvpnType3Route}
   * matched by the provided {@code subMatcher}.
   */
  public static @Nonnull Matcher<AbstractRoute> isEvpnType3RouteThat(
      Matcher<? super EvpnType3Route> subMatcher) {
    return new IsEvpnType3RouteThat(subMatcher);
  }

  /**
   * Provides a matcher that matches when the {@link AbstractRoute} is a {@link EvpnType5Route}
   * matched by the provided {@code subMatcher}.
   */
  public static @Nonnull Matcher<AbstractRoute> isEvpnType5RouteThat(
      Matcher<? super EvpnType5Route> subMatcher) {
    return new IsEvpnType5RouteThat(subMatcher);
  }

  private BgpRouteMatchers() {}
}
