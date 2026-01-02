package org.batfish.common.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warning;
import org.batfish.common.matchers.WarningMatchersImpl.HasTag;
import org.batfish.common.matchers.WarningMatchersImpl.HasText;
import org.hamcrest.Matcher;

/** {@link Matcher Matchers} for {@link Warning}. */
@ParametersAreNonnullByDefault
public class WarningMatchers {

  /**
   * Provides a matcher that matches if the warning has text matched by the provided * {@code
   * subMatcher}.
   */
  public static Matcher<Warning> hasText(Matcher<? super String> subMatcher) {
    return new HasText(subMatcher);
  }

  /** Provides a matcher that matches if the warning's text is the given text. */
  public static Matcher<Warning> hasText(String text) {
    return hasText(equalTo(text));
  }

  /**
   * Provides a matcher that matches if the warning has a tag matched by the provided {@code
   * subMatcher}.
   */
  public static Matcher<Warning> hasTag(Matcher<? super String> subMatcher) {
    return new HasTag(subMatcher);
  }

  /** Provides a matcher that matches if the warning's tag is the given text. */
  public static Matcher<Warning> hasTag(String tag) {
    return hasText(equalTo(tag));
  }

  private WarningMatchers() {}
}
