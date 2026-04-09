package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.GeneratedRoute;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

/** Matchers for {@link GeneratedRoute} */
public final class GeneratedRouteMatchers {

  /**
   * Provides a matcher that matches when the {@code discard} is equal to the {@link
   * GeneratedRoute}'s discard.
   */
  public static @Nonnull Matcher<GeneratedRoute> hasDiscard(
      @Nonnull Matcher<? super Boolean> subMatcher) {
    return new HasDiscard(subMatcher);
  }

  /**
   * Provides a matcher that matches when a {@link GeneratedRoute} has {@code discard} equal to true
   */
  public static @Nonnull Matcher<GeneratedRoute> isDiscard() {
    return new HasDiscard(equalTo(true));
  }

  private GeneratedRouteMatchers() {}

  private static final class HasDiscard extends FeatureMatcher<GeneratedRoute, Boolean> {
    HasDiscard(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "An AbstractRoute with discard:", "discard");
    }

    @Override
    protected Boolean featureValueOf(@Nonnull GeneratedRoute actual) {
      return actual.getDiscard();
    }
  }
}
