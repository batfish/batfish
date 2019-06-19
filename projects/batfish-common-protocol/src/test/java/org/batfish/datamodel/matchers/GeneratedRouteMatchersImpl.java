package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.GeneratedRoute;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class GeneratedRouteMatchersImpl {
  static final class HasDiscard extends FeatureMatcher<GeneratedRoute, Boolean> {
    HasDiscard(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An AbstractRoute with discard:", "discard");
    }

    @Override
    protected Boolean featureValueOf(@Nonnull GeneratedRoute actual) {
      return actual.getDiscard();
    }
  }

  private GeneratedRouteMatchersImpl() {}
}
