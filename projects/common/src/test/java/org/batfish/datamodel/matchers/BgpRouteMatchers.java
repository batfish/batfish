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
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.hamcrest.FeatureMatcher;
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
    return new HasPathId(subMatcher);
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

  private static final class HasAsPath extends FeatureMatcher<HasReadableAsPath, AsPath> {
    HasAsPath(@Nonnull Matcher<? super AsPath> subMatcher) {
      super(subMatcher, "A HasReadableAsPath with as-path:", "as-path");
    }

    @Override
    protected AsPath featureValueOf(HasReadableAsPath actual) {
      return actual.getAsPath();
    }
  }

  private static final class HasClusterList extends FeatureMatcher<BgpRoute<?, ?>, Set<Long>> {
    HasClusterList(@Nonnull Matcher<? super Set<Long>> subMatcher) {
      super(subMatcher, "A BgpRoute with cluster-list:", "cluster-list");
    }

    @Override
    protected Set<Long> featureValueOf(BgpRoute<?, ?> actual) {
      return actual.getClusterList();
    }
  }

  private static final class HasCommunities
      extends FeatureMatcher<HasReadableCommunities, CommunitySet> {
    HasCommunities(@Nonnull Matcher<? super CommunitySet> subMatcher) {
      super(subMatcher, "A HasReadableCommunities with communities:", "communities");
    }

    @Override
    protected CommunitySet featureValueOf(HasReadableCommunities actual) {
      return actual.getCommunities();
    }
  }

  private static final class HasLocalPreference
      extends FeatureMatcher<HasReadableLocalPreference, Long> {
    HasLocalPreference(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "A HasReadableLocalPreference with localPreference:", "localPreference");
    }

    @Override
    protected Long featureValueOf(HasReadableLocalPreference actual) {
      return actual.getLocalPreference();
    }
  }

  private static final class HasOriginType
      extends FeatureMatcher<HasReadableOriginType, OriginType> {
    HasOriginType(@Nonnull Matcher<? super OriginType> subMatcher) {
      super(subMatcher, "A HasReadableOriginType with originType:", "originType");
    }

    @Override
    protected OriginType featureValueOf(HasReadableOriginType actual) {
      return actual.getOriginType();
    }
  }

  private static final class HasPathId extends FeatureMatcher<BgpRoute<?, ?>, Integer> {
    HasPathId(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A BgpRoute with pathId:", "pathId");
    }

    @Override
    protected Integer featureValueOf(BgpRoute<?, ?> actual) {
      return actual.getPathId();
    }
  }

  private static final class HasReceivedFrom extends FeatureMatcher<BgpRoute<?, ?>, ReceivedFrom> {
    HasReceivedFrom(@Nonnull Matcher<? super ReceivedFrom> subMatcher) {
      super(subMatcher, "A BgpRoute with receivedFrom:", "receivedFrom");
    }

    @Override
    protected ReceivedFrom featureValueOf(BgpRoute<?, ?> actual) {
      return actual.getReceivedFrom();
    }
  }

  private static final class IsReceivedFromRouteReflectorClient
      extends FeatureMatcher<BgpRoute<?, ?>, Boolean> {

    IsReceivedFromRouteReflectorClient(Matcher<? super Boolean> subMatcher) {
      super(
          subMatcher,
          "A BgpRoute with receivedFromRouteReflectorClient",
          "receivedFromRouteReflectorClient");
    }

    @Override
    protected Boolean featureValueOf(BgpRoute<?, ?> actual) {
      return actual.getReceivedFromRouteReflectorClient();
    }
  }

  private static final class HasWeight extends FeatureMatcher<HasReadableWeight, Integer> {
    HasWeight(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A HasReadableWeight with weight:", "weight");
    }

    @Override
    protected Integer featureValueOf(HasReadableWeight actual) {
      return actual.getWeight();
    }
  }

  private static final class IsBgpv4RouteThat extends IsInstanceThat<AbstractRoute, Bgpv4Route> {
    IsBgpv4RouteThat(Matcher<? super Bgpv4Route> subMatcher) {
      super(Bgpv4Route.class, subMatcher);
    }
  }

  private static final class IsEvpnType3RouteThat
      extends IsInstanceThat<AbstractRoute, EvpnType3Route> {
    IsEvpnType3RouteThat(Matcher<? super EvpnType3Route> subMatcher) {
      super(EvpnType3Route.class, subMatcher);
    }
  }

  private static final class IsEvpnType5RouteThat
      extends IsInstanceThat<AbstractRoute, EvpnType5Route> {
    IsEvpnType5RouteThat(Matcher<? super EvpnType5Route> subMatcher) {
      super(EvpnType5Route.class, subMatcher);
    }
  }
}
