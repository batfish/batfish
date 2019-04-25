package org.batfish.common.matchers;

import javax.annotation.Nonnull;
import org.batfish.common.Warnings.ParseWarning;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class ParseWarningMatchersImpl {

  static final class HasComment extends FeatureMatcher<ParseWarning, String> {
    HasComment(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "A parse warning with comment:", "comment");
    }

    @Override
    protected String featureValueOf(ParseWarning actual) {
      return actual.getComment();
    }
  }

  static final class HasText extends FeatureMatcher<ParseWarning, String> {
    HasText(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "A parse warning with text:", "text:");
    }

    @Override
    protected String featureValueOf(ParseWarning actual) {
      return actual.getText();
    }
  }

  private ParseWarningMatchersImpl() {}
}
