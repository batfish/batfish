package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.matchers.AnnotatedRouteMatchersImpl.HasSourceVrf;
import org.hamcrest.Matcher;

public class AnnotatedRouteMatchers {

  /**
   * Provides a matcher that matches when the supplied {@code subMatcher} matches the {@link
   * org.batfish.datamodel.AnnotatedRoute AnnotatedRoute}'s metric.
   */
  public static @Nonnull HasSourceVrf hasSourceVrf(@Nonnull Matcher<? super String> subMatcher) {
    return new HasSourceVrf(subMatcher);
  }
}
