package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.table.Rows;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class RowsMatchersImpl {

  static final class HasSize extends FeatureMatcher<Rows, Integer> {

    public HasSize(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "Rows with size:", "size");
    }

    @Override
    protected Integer featureValueOf(Rows actual) {
      return actual.size();
    }
  }
}
