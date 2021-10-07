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

  private StaticRouteMatchers() {}
}
