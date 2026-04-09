package org.batfish.common.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings.ParseWarning;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

/** {@link Matcher Matchers} for {@link ParseWarning}. */
@ParametersAreNonnullByDefault
public class ParseWarningMatchers {

  /**
   * Provides a matcher that matches if the parse warning's comment exactly matches {@code comment}.
   */
  public static Matcher<ParseWarning> hasComment(String comment) {
    return new HasComment(equalTo(comment));
  }

  /**
   * Provides a matcher that matches if the parse warning's text is matched by the provided {@code
   * subMatcher}.
   */
  public static Matcher<ParseWarning> hasText(Matcher<? super String> subMatcher) {
    return new HasText(subMatcher);
  }

  /** Provides a matcher that matches if the parse warning's text is the given text. */
  public static Matcher<ParseWarning> hasText(String text) {
    return hasText(equalTo(text));
  }

  private ParseWarningMatchers() {}

  private static final class HasComment extends FeatureMatcher<ParseWarning, String> {
    HasComment(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "A parse warning with comment:", "comment");
    }

    @Override
    protected String featureValueOf(ParseWarning actual) {
      return actual.getComment();
    }
  }

  private static final class HasText extends FeatureMatcher<ParseWarning, String> {
    HasText(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "A parse warning with text:", "text:");
    }

    @Override
    protected String featureValueOf(ParseWarning actual) {
      return actual.getText();
    }
  }
}
