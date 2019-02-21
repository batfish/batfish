package org.batfish.datamodel.matchers;

import org.batfish.datamodel.matchers.PairMatchersImpl.HasFirst;
import org.hamcrest.Matcher;

public class PairMatchers {

  /**
   * Provides a matcher that matches if the provided {@code subMatcher} matches the pair's first
   * element
   */
  public static <T1 extends Comparable<? super T1>, T2 extends Comparable<? super T2>>
      HasFirst<T1, T2> hasFirst(Matcher<? super T1> subMatcher) {
    return new HasFirst<T1, T2>(subMatcher);
  }

  private PairMatchers() {}
}
