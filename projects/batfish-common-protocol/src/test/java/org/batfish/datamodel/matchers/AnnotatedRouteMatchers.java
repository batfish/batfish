package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.matchers.AnnotatedRouteMatchersImpl.HasSourceVrf;

/** Matchers for testing {@link org.batfish.datamodel.AnnotatedRoute AnnotatedRoute} objects */
public final class AnnotatedRouteMatchers {

  /**
   * Provides a matcher that matches when the supplied {@code expectedSourceVrf} is equal to the
   * {@link org.batfish.datamodel.AnnotatedRoute AnnotatedRoute}'s source VRF.
   */
  public static @Nonnull HasSourceVrf hasSourceVrf(@Nonnull String expectedSourceVrf) {
    return new HasSourceVrf(equalTo(expectedSourceVrf));
  }
}
