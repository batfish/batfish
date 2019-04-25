package org.batfish.datamodel.matchers;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.OriginType;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

@ParametersAreNonnullByDefault
final class BgpRouteMatchersImpl {
  static final class HasCommunities extends FeatureMatcher<Bgpv4Route, Set<Long>> {
    HasCommunities(@Nonnull Matcher<? super Set<Long>> subMatcher) {
      super(subMatcher, "A Bgpv4Route with communities:", "communities");
    }

    @Override
    protected Set<Long> featureValueOf(Bgpv4Route actual) {
      return actual.getCommunities();
    }
  }

  static final class HasOriginType extends FeatureMatcher<Bgpv4Route, OriginType> {
    HasOriginType(@Nonnull Matcher<? super OriginType> subMatcher) {
      super(subMatcher, "A Bgpv4Route with originType:", "originType");
    }

    @Override
    protected OriginType featureValueOf(Bgpv4Route actual) {
      return actual.getOriginType();
    }
  }

  static final class HasWeight extends FeatureMatcher<Bgpv4Route, Integer> {
    HasWeight(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A Bgpv4Route with weight:", "weight");
    }

    @Override
    protected Integer featureValueOf(Bgpv4Route actual) {
      return actual.getWeight();
    }
  }

  static final class IsBgpRouteThat extends IsInstanceThat<AbstractRoute, Bgpv4Route> {
    IsBgpRouteThat(Matcher<? super Bgpv4Route> subMatcher) {
      super(Bgpv4Route.class, subMatcher);
    }
  }

  private BgpRouteMatchersImpl() {}
}
