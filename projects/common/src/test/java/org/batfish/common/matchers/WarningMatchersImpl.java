package org.batfish.common.matchers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warning;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class WarningMatchersImpl {

  static final class HasTag extends FeatureMatcher<Warning, String> {
    HasTag(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "a Warning with tag:", "tag");
    }

    @Override
    protected @Nullable String featureValueOf(Warning actual) {
      return actual.getTag();
    }
  }

  static final class HasText extends FeatureMatcher<Warning, String> {
    HasText(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "a Warning with text:", "text");
    }

    @Override
    protected String featureValueOf(Warning actual) {
      return actual.getText();
    }
  }

  private WarningMatchersImpl() {}
}
