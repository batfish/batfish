package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.AnnotatedRoute;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class AnnotatedRouteMatchersImpl {

  static final class HasSourceVrf extends FeatureMatcher<AnnotatedRoute<?>, String> {
    HasSourceVrf(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An AnnotatedRoute with sourceVrf:", "sourceVrf");
    }

    @Override
    protected String featureValueOf(AnnotatedRoute<?> actual) {
      return actual.getSourceVrf();
    }
  }

  private AnnotatedRouteMatchersImpl() {}
}
