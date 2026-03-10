package org.batfish.datamodel.matchers;

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
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

@ParametersAreNonnullByDefault
final class BgpRouteMatchersImpl {
  static final class HasAsPath extends FeatureMatcher<HasReadableAsPath, AsPath> {
    HasAsPath(@Nonnull Matcher<? super AsPath> subMatcher) {
      super(subMatcher, "A HasReadableAsPath with as-path:", "as-path");
    }

    @Override
    protected AsPath featureValueOf(HasReadableAsPath actual) {
      return actual.getAsPath();
    }
  }

  static final class HasClusterList extends FeatureMatcher<BgpRoute<?, ?>, Set<Long>> {
    HasClusterList(@Nonnull Matcher<? super Set<Long>> subMatcher) {
      super(subMatcher, "A BgpRoute with cluster-list:", "cluster-list");
    }

    @Override
    protected Set<Long> featureValueOf(BgpRoute<?, ?> actual) {
      return actual.getClusterList();
    }
  }

  static final class HasCommunities extends FeatureMatcher<HasReadableCommunities, CommunitySet> {
    HasCommunities(@Nonnull Matcher<? super CommunitySet> subMatcher) {
      super(subMatcher, "A HasReadableCommunities with communities:", "communities");
    }

    @Override
    protected CommunitySet featureValueOf(HasReadableCommunities actual) {
      return actual.getCommunities();
    }
  }

  static final class HasLocalPreference extends FeatureMatcher<HasReadableLocalPreference, Long> {
    HasLocalPreference(@Nonnull Matcher<? super Long> subMatcher) {
      super(subMatcher, "A HasReadableLocalPreference with localPreference:", "localPreference");
    }

    @Override
    protected Long featureValueOf(HasReadableLocalPreference actual) {
      return actual.getLocalPreference();
    }
  }

  static final class HasOriginType extends FeatureMatcher<HasReadableOriginType, OriginType> {
    HasOriginType(@Nonnull Matcher<? super OriginType> subMatcher) {
      super(subMatcher, "A HasReadableOriginType with originType:", "originType");
    }

    @Override
    protected OriginType featureValueOf(HasReadableOriginType actual) {
      return actual.getOriginType();
    }
  }

  static final class HasPathId extends FeatureMatcher<BgpRoute<?, ?>, Integer> {
    HasPathId(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A BgpRoute with pathId:", "pathId");
    }

    @Override
    protected Integer featureValueOf(BgpRoute<?, ?> actual) {
      return actual.getPathId();
    }
  }

  static final class HasReceivedFrom extends FeatureMatcher<BgpRoute<?, ?>, ReceivedFrom> {
    HasReceivedFrom(@Nonnull Matcher<? super ReceivedFrom> subMatcher) {
      super(subMatcher, "A BgpRoute with receivedFrom:", "receivedFrom");
    }

    @Override
    protected ReceivedFrom featureValueOf(BgpRoute<?, ?> actual) {
      return actual.getReceivedFrom();
    }
  }

  static final class IsReceivedFromRouteReflectorClient
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

  static final class HasWeight extends FeatureMatcher<HasReadableWeight, Integer> {
    HasWeight(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A HasReadableWeight with weight:", "weight");
    }

    @Override
    protected Integer featureValueOf(HasReadableWeight actual) {
      return actual.getWeight();
    }
  }

  static final class IsBgpv4RouteThat extends IsInstanceThat<AbstractRoute, Bgpv4Route> {
    IsBgpv4RouteThat(Matcher<? super Bgpv4Route> subMatcher) {
      super(Bgpv4Route.class, subMatcher);
    }
  }

  static final class IsEvpnType3RouteThat extends IsInstanceThat<AbstractRoute, EvpnType3Route> {
    IsEvpnType3RouteThat(Matcher<? super EvpnType3Route> subMatcher) {
      super(EvpnType3Route.class, subMatcher);
    }
  }

  static final class IsEvpnType5RouteThat extends IsInstanceThat<AbstractRoute, EvpnType5Route> {
    IsEvpnType5RouteThat(Matcher<? super EvpnType5Route> subMatcher) {
      super(EvpnType5Route.class, subMatcher);
    }
  }

  private BgpRouteMatchersImpl() {}
}
