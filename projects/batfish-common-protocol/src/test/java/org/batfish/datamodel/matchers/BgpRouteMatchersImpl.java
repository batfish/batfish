package org.batfish.datamodel.matchers;

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
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

@ParametersAreNonnullByDefault
final class BgpRouteMatchersImpl {
  static final class HasCommunities extends FeatureMatcher<BgpRoute<?, ?>, Set<Community>> {
    HasCommunities(@Nonnull Matcher<? super Set<Community>> subMatcher) {
      super(subMatcher, "A BgpRoute with communities:", "communities");
    }

    @Override
    protected Set<Community> featureValueOf(BgpRoute<?, ?> actual) {
      return actual.getCommunities();
    }
  }

  static final class HasOriginType extends FeatureMatcher<BgpRoute<?, ?>, OriginType> {
    HasOriginType(@Nonnull Matcher<? super OriginType> subMatcher) {
      super(subMatcher, "A BgpRoute with originType:", "originType");
    }

    @Override
    protected OriginType featureValueOf(BgpRoute<?, ?> actual) {
      return actual.getOriginType();
    }
  }

  static final class HasRouteDistinguisher
      extends FeatureMatcher<EvpnRoute<?, ?>, RouteDistinguisher> {
    HasRouteDistinguisher(@Nonnull Matcher<? super RouteDistinguisher> subMatcher) {
      super(subMatcher, "An EvpnRoute with routeDistinguisher:", "routeDistinguisher");
    }

    @Override
    protected RouteDistinguisher featureValueOf(EvpnRoute<?, ?> actual) {
      return actual.getRouteDistinguisher();
    }
  }

  static final class HasWeight extends FeatureMatcher<BgpRoute<?, ?>, Integer> {
    HasWeight(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A BgpRoute with weight:", "weight");
    }

    @Override
    protected Integer featureValueOf(BgpRoute<?, ?> actual) {
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
