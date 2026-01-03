package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.matchers.AnnotatedRouteMatchersImpl.HasRoute;
import org.batfish.datamodel.matchers.AnnotatedRouteMatchersImpl.HasSourceVrf;
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
}
