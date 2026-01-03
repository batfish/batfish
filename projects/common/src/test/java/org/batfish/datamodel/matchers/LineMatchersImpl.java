package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.AaaAuthenticationLoginList;
import org.batfish.datamodel.Line;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class LineMatchersImpl {

  static final class HasAuthenticationLoginList
      extends FeatureMatcher<Line, AaaAuthenticationLoginList> {
    HasAuthenticationLoginList(@Nonnull Matcher<? super AaaAuthenticationLoginList> subMatcher) {
      super(subMatcher, "a line with a AaaAuthenticationLoginList", "AaaAuthenticationLoginList");
    }

    @Override
    protected AaaAuthenticationLoginList featureValueOf(Line actual) {
      return actual.getAaaAuthenticationLoginList();
    }
  }

  static final class RequiresAuthentication extends FeatureMatcher<Line, Boolean> {
    RequiresAuthentication(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "a Line that requires authentication", "requires authentication");
    }

    @Override
    protected Boolean featureValueOf(Line actual) {
      return actual.requiresAuthentication();
    }
  }

  private LineMatchersImpl() {}
}
