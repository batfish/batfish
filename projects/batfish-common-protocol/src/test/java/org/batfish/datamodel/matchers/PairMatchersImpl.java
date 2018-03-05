package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.common.Pair;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class PairMatchersImpl {

  static final class HasFirst<T1 extends Comparable<? super T1>, T2 extends Comparable<? super T2>>
      extends FeatureMatcher<Pair<T1, T2>, T1> {
    HasFirst(@Nonnull Matcher<? super T1> subMatcher) {
      super(subMatcher, "A Pair with first:", "first");
    }

    @Override
    protected T1 featureValueOf(Pair<T1, T2> actual) {
      return actual.getFirst();
    }
  }

  static final class HasSecond<T1 extends Comparable<? super T1>, T2 extends Comparable<? super T2>>
      extends FeatureMatcher<Pair<T1, T2>, T2> {
    HasSecond(@Nonnull Matcher<? super T2> subMatcher) {
      super(subMatcher, "A Pair with second:", "second");
    }

    @Override
    protected T2 featureValueOf(Pair<T1, T2> actual) {
      return actual.getSecond();
    }
  }

  private PairMatchersImpl() {}
}
