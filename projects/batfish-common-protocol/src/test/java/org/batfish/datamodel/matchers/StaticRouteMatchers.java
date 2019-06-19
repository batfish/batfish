package org.batfish.datamodel.matchers;

import static org.batfish.datamodel.matchers.StaticRouteMatchersImpl.HasNextVrf;
import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.StaticRoute;
import org.hamcrest.Matcher;

/* Matchers for {@link StaticRoute}. */
@ParametersAreNonnullByDefault
public final class StaticRouteMatchers {

  /**
   * A {@link Matcher} that matches if the {@link StaticRoute}'s nextVrf is matched by the provided
   * {@code subMatcher}.
   */
  public static @Nonnull Matcher<StaticRoute> hasNextVrf(Matcher<? super String> subMatcher) {
    return new HasNextVrf(subMatcher);
  }

  /**
   * A {@link Matcher} that matches if the {@link StaticRoute}'s nextVrf is {@code expectedNextVrf}.
   */
  public static @Nonnull Matcher<StaticRoute> hasNextVrf(String expectedNextVrf) {
    return hasNextVrf(equalTo(expectedNextVrf));
  }

  private StaticRouteMatchers() {}
}
