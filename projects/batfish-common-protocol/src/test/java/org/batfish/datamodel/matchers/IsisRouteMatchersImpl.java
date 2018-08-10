package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.IsisRoute;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IsisRouteMatchersImpl {

  static final class HasDown extends FeatureMatcher<IsisRoute, Boolean> {
    HasDown(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An IsisRoute with down:", "down");
    }

    @Override
    protected Boolean featureValueOf(IsisRoute actual) {
      return actual.getDown();
    }
  }

  static final class IsIsisRouteThat extends IsInstanceThat<AbstractRoute, IsisRoute> {
    IsIsisRouteThat(Matcher<? super IsisRoute> subMatcher) {
      super(IsisRoute.class, subMatcher);
    }
  }

  private IsisRouteMatchersImpl() {}
}
