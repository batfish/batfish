package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.ospf.StubSettings;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class StubSettingsMatchers {
  /**
   * Provides a matcher that matches if the the {@link StubSettings}'s suppressType3 is {@code
   * true}.
   */
  public static @Nonnull Matcher<StubSettings> hasSuppressType3() {
    return new HasSuppressType3(equalTo(true));
  }

  /**
   * Provides a matcher that matches if the the {@link StubSettings}'s suppressType3 is {@code
   * expectedSuppressType3}.
   */
  public static @Nonnull Matcher<StubSettings> hasSuppressType3(boolean expectedSuppressType3) {
    return new HasSuppressType3(equalTo(expectedSuppressType3));
  }

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link
   * StubSettings}'s suppressType3.
   */
  public static @Nonnull Matcher<StubSettings> hasSuppressType3(
      @Nonnull Matcher<? super Boolean> subMatcher) {
    return new HasSuppressType3(subMatcher);
  }

  private StubSettingsMatchers() {}

  private static final class HasSuppressType3 extends FeatureMatcher<StubSettings, Boolean> {
    HasSuppressType3(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A StubSettings with suppressType3:", "suppressType3");
    }

    @Override
    protected Boolean featureValueOf(StubSettings actual) {
      return actual.getSuppressType3();
    }
  }
}
