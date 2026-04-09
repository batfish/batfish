package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.answers.Issue;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class IssueMatchers {

  /**
   * Provides a matcher that matches if the major type of {@link Issue} equals {@code minorType}.
   */
  public static @Nonnull Matcher<Issue> hasMajorType(@Nonnull String majorType) {
    return new HasMajorType(equalTo(majorType));
  }

  /**
   * Provides a matcher that matches if the minor type of {@link Issue} equals {@code minorType}.
   */
  public static @Nonnull Matcher<Issue> hasMinorType(@Nonnull String minorType) {
    return new HasMinorType(equalTo(minorType));
  }

  private IssueMatchers() {}

  private static final class HasMajorType extends FeatureMatcher<Issue, String> {
    HasMajorType(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "an Issue with majorType:", "majorType");
    }

    @Override
    protected String featureValueOf(Issue actual) {
      return actual.getType().getMajor();
    }
  }

  private static final class HasMinorType extends FeatureMatcher<Issue, String> {
    HasMinorType(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "an Issue with minorType:", "minorType");
    }

    @Override
    protected String featureValueOf(Issue actual) {
      return actual.getType().getMinor();
    }
  }
}
