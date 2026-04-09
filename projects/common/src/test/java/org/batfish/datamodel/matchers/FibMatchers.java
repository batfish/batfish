package org.batfish.datamodel.matchers;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FibEntry;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

/** Matchers for {@link Fib}. */
@ParametersAreNonnullByDefault
public final class FibMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the {@link Fib}'s
   * allEntries.
   */
  public static Matcher<Fib> hasAllEntries(Matcher<? super Set<FibEntry>> subMatcher) {
    return new HasAllEntries(subMatcher);
  }

  private FibMatchers() {}

  private static final class HasAllEntries extends FeatureMatcher<Fib, Set<FibEntry>> {

    HasAllEntries(Matcher<? super Set<FibEntry>> subMatcher) {
      super(subMatcher, "A Fib with allEntries", "allEntries");
    }

    @Override
    protected @Nonnull Set<FibEntry> featureValueOf(Fib actual) {
      return actual.allEntries();
    }
  }
}
