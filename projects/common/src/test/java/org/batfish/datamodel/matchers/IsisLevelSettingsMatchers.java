package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.isis.IsisLevelSettings;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class IsisLevelSettingsMatchers {
  private IsisLevelSettingsMatchers() {}

  /**
   * Provides a matcher that matches when the {@link IsisLevelSettings}'s wideMetricsOnly flag is
   * true.
   */
  public static @Nonnull Matcher<IsisLevelSettings> hasWideMetricsOnly() {
    return new HasWideMetricsOnly(equalTo(true));
  }

  /**
   * Provides a matcher that matches when the provided {@code subMatcher} matches the {@link
   * IsisLevelSettings}'s wideMetricsOnly.
   */
  public static @Nonnull Matcher<IsisLevelSettings> hasWideMetricsOnly(
      @Nonnull Matcher<? super Boolean> subMatcher) {
    return new HasWideMetricsOnly(subMatcher);
  }

  private static final class HasWideMetricsOnly extends FeatureMatcher<IsisLevelSettings, Boolean> {
    HasWideMetricsOnly(Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An IsisLevelSettings with wideMetricsOnly:", "wideMetricsOnly");
    }

    @Override
    protected Boolean featureValueOf(IsisLevelSettings actual) {
      return actual.getWideMetricsOnly();
    }
  }
}
