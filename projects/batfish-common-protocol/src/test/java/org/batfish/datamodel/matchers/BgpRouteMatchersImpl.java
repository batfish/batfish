package org.batfish.datamodel.matchers;

import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpRoute;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class BgpRouteMatchersImpl {
  static final class HasCommunities extends FeatureMatcher<BgpRoute, Set<Long>> {
    HasCommunities(@Nonnull Matcher<? super Set<Long>> subMatcher) {
      super(subMatcher, "A BgpRoute with communities:", "communities");
    }

    @Override
    protected Set<Long> featureValueOf(BgpRoute actual) {
      return actual.getCommunities();
    }
  }

  private BgpRouteMatchersImpl() {}
}
