package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.EvpnType3Route;
import org.batfish.datamodel.EvpnType5Route;
import org.batfish.datamodel.HasReadableAsPath;
import org.batfish.datamodel.HasReadableCommunities;
import org.batfish.datamodel.HasReadableLocalPreference;
import org.batfish.datamodel.HasReadableOriginType;
import org.batfish.datamodel.HasReadableWeight;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.ReceivedFrom;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.HasAsPath;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.HasClusterList;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.HasCommunities;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.HasLocalPreference;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.HasOriginType;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.HasReceivedFrom;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.HasWeight;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.IsBgpv4RouteThat;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.IsEvpnType3RouteThat;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.IsEvpnType5RouteThat;
import org.batfish.datamodel.matchers.BgpRouteMatchersImpl.IsReceivedFromRouteReflectorClient;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.hamcrest.Matcher;

@ParametersAreNonnullByDefault
public final class BgpRouteMatchers {

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * HasReadableAsPath}'s as-path.
   */
  public static @Nonnull Matcher<HasReadableAsPath> hasAsPath(Matcher<? super AsPath> subMatcher) {
    return new HasAsPath(subMatcher);
  }

  public static @Nonnull Matcher<BgpRoute<?, ?>> hasClusterList(
      Matcher<? super Set<Long>> subMatcher) {
    return new HasClusterList(subMatcher);
  }

  /**
   * Provides a matcher that matches when the supplied {@code expectedCommunities} is equal to the
   * {@link HasReadableCommunities}'s communities.
   */
  public static @Nonnull Matcher<HasReadableCommunities> hasCommunities(
      CommunitySet expectedCommunities) {
    return new HasCommunities(equalTo(expectedCommunities));
  }

  /**
   * Provides a matcher that matches when the supplied communities comprise the {@link
   * HasReadableCommunities}'s communities.
   */
  public static @Nonnull Matcher<HasReadableCommunities> hasCommunities(
      Community... expectedCommunities) {
    return new HasCommunities(equalTo(CommunitySet.of(expectedCommunities)));
  }

  /**
   * Provides a matcher that matches when the supplied {@code expectedLocalPreference} is equal to
   * the {@link HasReadableLocalPreference}'s local preference.
   */
  public static @Nonnull Matcher<HasReadableLocalPreference> hasLocalPreference(
      long expectedLocalPreference) {
    return new HasLocalPreference(equalTo(expectedLocalPreference));
  }

  /**
   * Provides a matcher that matches when the supplied {@code expectedOriginType} is equal to the
   * {@link HasReadableOriginType}'s originType.
   */
  public static @Nonnull Matcher<HasReadableOriginType> hasOriginType(
      OriginType expectedOriginType) {
    return new HasOriginType(equalTo(expectedOriginType));
  }

  public static @Nonnull Matcher<BgpRoute<?, ?>> hasPathId(Matcher<? super Integer> subMatcher) {
    return new BgpRouteMatchersImpl.HasPathId(subMatcher);
  }

  public static @Nonnull Matcher<BgpRoute<?, ?>> hasNoPathId() {
    return hasPathId(nullValue());
  }

  public static @Nonnull Matcher<BgpRoute<?, ?>> hasReceivedFrom(
      Matcher<? super ReceivedFrom> subMatcher) {
    return new HasReceivedFrom(subMatcher);
  }

  public static @Nonnull Matcher<BgpRoute<?, ?>> isReceivedFromRouteReflectorClient() {
    return isReceivedFromRouteReflectorClient(true);
  }

  public static @Nonnull Matcher<BgpRoute<?, ?>> isReceivedFromRouteReflectorClient(boolean b) {
    return new IsReceivedFromRouteReflectorClient(equalTo(b));
  }

  /**
   * Provides a matcher that matches when the supplied {@code expectedWeight} is equal to the {@link
   * HasReadableWeight}'s weight.
   */
  public static @Nonnull Matcher<HasReadableWeight> hasWeight(int expectedWeight) {
    return new HasWeight(equalTo(expectedWeight));
  }

  /**
   * Provides a matcher that matches when the {@link AbstractRoute}'s weight is matched by the
   * provided {@code subMatcher}..
   */
  public static @Nonnull Matcher<HasReadableWeight> hasWeight(Matcher<? super Integer> subMatcher) {
    return new HasWeight(subMatcher);
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
