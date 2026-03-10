package org.batfish.common.matchers;

import com.google.common.base.Throwables;
import javax.annotation.Nonnull;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class ThrowableMatchers {
  private static final class HasStackTrace extends FeatureMatcher<Throwable, String> {
    public HasStackTrace(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "A Throwable with stackTrace:", "stackTrace:");
    }

    @Override
    protected String featureValueOf(Throwable actual) {
      return Throwables.getStackTraceAsString(actual);
    }
  }

  /**
   * A matcher that matches a {@link Throwable} whose stack trace message is matched by the provided
   * {@code subMatcher}.
   */
  public static @Nonnull Matcher<Throwable> hasStackTrace(
      @Nonnull Matcher<? super String> subMatcher) {
    return new HasStackTrace(subMatcher);
  }

  private ThrowableMatchers() {}
}
