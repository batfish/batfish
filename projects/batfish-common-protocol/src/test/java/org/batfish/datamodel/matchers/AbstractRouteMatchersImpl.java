package org.batfish.datamodel.matchers;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Prefix;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

@ParametersAreNonnullByDefault
final class AbstractRouteMatchersImpl {

  static final class HasNetwork extends FeatureMatcher<AbstractRoute, Prefix> {

    public HasNetwork(Matcher<? super Prefix> subMatcher) {
      super(subMatcher, "An AbstractRoute with network:", "network:");
    }

    @Override
    protected Prefix featureValueOf(AbstractRoute actual) {
      return actual.getNetwork();
    }
  }

  private AbstractRouteMatchersImpl() {}
}
