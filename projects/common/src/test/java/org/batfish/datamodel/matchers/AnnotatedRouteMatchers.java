package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

/** Matchers for testing {@link AnnotatedRoute} objects */
public final class AnnotatedRouteMatchers {
  /**
   * Provides a matcher that matches when the supplied route matcher matches the {@link
   * AnnotatedRoute}'s route.
   */
  public static @Nonnull <R extends AbstractRoute> Matcher<AnnotatedRoute<R>> hasRoute(
      @Nonnull Matcher<? super R> routeMatcher) {
    return new HasRoute<R>(routeMatcher);
  }

  /**
   * Provides a matcher that matches when the supplied {@code expectedSourceVrf} is equal to the
   * {@link AnnotatedRoute}'s source VRF.
   */
  public static @Nonnull Matcher<AnnotatedRoute<?>> hasSourceVrf(
      @Nonnull String expectedSourceVrf) {
    return new HasSourceVrf(equalTo(expectedSourceVrf));
  }

  private static final class HasRoute<R extends AbstractRoute>
      extends FeatureMatcher<AnnotatedRoute<R>, R> {
    HasRoute(@Nonnull Matcher<? super R> subMatcher) {
      super(subMatcher, "An AnnotatedRoute with route:", "route");
    }

    @Override
    protected R featureValueOf(AnnotatedRoute<R> actual) {
      return actual.getRoute();
    }
  }

  private static final class HasSourceVrf extends FeatureMatcher<AnnotatedRoute<?>, String> {
    HasSourceVrf(@Nonnull Matcher<? super String> subMatcher) {
      super(subMatcher, "An AnnotatedRoute with sourceVrf:", "sourceVrf");
    }

    @Override
    protected String featureValueOf(AnnotatedRoute<?> actual) {
      return actual.getSourceVrf();
    }
  }
}
