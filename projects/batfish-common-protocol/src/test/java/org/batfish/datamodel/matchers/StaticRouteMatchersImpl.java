package org.batfish.datamodel.matchers;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.StaticRoute;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

@ParametersAreNonnullByDefault
final class StaticRouteMatchersImpl {

  static final class HasNextVrf extends FeatureMatcher<StaticRoute, String> {
    HasNextVrf(Matcher<? super String> subMatcher) {
      super(subMatcher, "A StaticRoute with nextVrf", "nextVrf");
    }

    @Override
    protected @Nullable String featureValueOf(StaticRoute actual) {
      return actual.getNextVrf();
    }
  }
}
