package org.batfish.datamodel.matchers;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FibEntry;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

@ParametersAreNonnullByDefault
final class FibMatchersImpl {

  static class HasAllEntries extends FeatureMatcher<Fib, Set<FibEntry>> {

    HasAllEntries(Matcher<? super Set<FibEntry>> subMatcher) {
      super(subMatcher, "A Fib with allEntries", "allEntries");
    }

    @Override
    protected @Nonnull Set<FibEntry> featureValueOf(Fib actual) {
      return actual.allEntries();
    }
  }

  private FibMatchersImpl() {}
}
