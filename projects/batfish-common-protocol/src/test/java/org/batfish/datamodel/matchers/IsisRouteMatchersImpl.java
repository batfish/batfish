package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.AbstractRouteDecorator;
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

  static final class HasOverload extends FeatureMatcher<IsisRoute, Boolean> {
    HasOverload(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An IsisRoute with overload:", "overload");
    }

    @Override
    protected Boolean featureValueOf(IsisRoute actual) {
      return actual.getOverload();
    }
  }

  static final class IsIsisRouteThat extends IsInstanceThat<AbstractRouteDecorator, IsisRoute> {
    IsIsisRouteThat(Matcher<? super IsisRoute> subMatcher) {
      super(IsisRoute.class, subMatcher);
    }
  }

  private IsisRouteMatchersImpl() {}
}
