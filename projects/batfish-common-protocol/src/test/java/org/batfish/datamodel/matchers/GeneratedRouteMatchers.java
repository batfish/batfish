package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.matchers.GeneratedRouteMatchersImpl.HasDiscard;
import org.hamcrest.Matcher;

/** Matchers for {@link GeneratedRoute} */
public final class GeneratedRouteMatchers {

  /**
   * Provides a matcher that matches when the {@code discard} is equal to the {@link
   * GeneratedRoute}'s discard.
   */
  public static @Nonnull HasDiscard hasDiscard(@Nonnull Matcher<? super Boolean> subMatcher) {
    return new HasDiscard(subMatcher);
  }

  /**
   * Provides a matcher that matches when a {@link GeneratedRoute} has {@code discard} equal to true
   */
  public static @Nonnull HasDiscard isDiscard() {
    return new HasDiscard(equalTo(true));
  }

  private GeneratedRouteMatchers() {}
}
