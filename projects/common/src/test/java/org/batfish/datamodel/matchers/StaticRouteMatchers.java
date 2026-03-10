package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.StaticRoute;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

@ParametersAreNonnullByDefault
public final class StaticRouteMatchers {

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches {@link
   * StaticRoute#getRecursive()}.
   */
  public static @Nonnull Matcher<StaticRoute> hasRecursive(Matcher<? super Boolean> subMatcher) {
    return new HasRecursive(subMatcher);
  }

  /**
   * Provides a matcher that matches when the supplied value matches {@link
   * StaticRoute#getRecursive()}.
   */
  public static @Nonnull Matcher<StaticRoute> hasRecursive(boolean value) {
    return hasRecursive(equalTo(value));
  }

  private static final class HasRecursive extends FeatureMatcher<StaticRoute, Boolean> {
    HasRecursive(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A StaticRoute with recursive:", "recursive");
    }

    @Override
    protected Boolean featureValueOf(StaticRoute actual) {
      return actual.getRecursive();
    }
  }

  private static final class HasTrack extends FeatureMatcher<StaticRoute, String> {
    HasTrack(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "A StaticRoute with track:", "track");
    }

    @Override
    protected String featureValueOf(StaticRoute actual) {
      return actual.getTrack();
    }
  }

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches {@link
   * StaticRoute#getTrack()}.
   */
  public static @Nonnull Matcher<StaticRoute> hasTrack(Matcher<? super String> subMatcher) {
    return new HasTrack(subMatcher);
  }

  /**
   * Provides a matcher that matches when the {@link StaticRoute#getTrack()} is equal to {@code
   * expectedTrack}.
   */
  public static @Nonnull Matcher<StaticRoute> hasTrack(String expectedTrack) {
    return hasTrack(equalTo(expectedTrack));
  }

  private StaticRouteMatchers() {}
}
