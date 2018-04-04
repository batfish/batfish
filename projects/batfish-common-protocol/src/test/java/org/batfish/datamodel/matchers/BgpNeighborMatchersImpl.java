package org.batfish.datamodel.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpNeighbor;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class BgpNeighborMatchersImpl {

  static final class HasLocalAs extends FeatureMatcher<BgpNeighbor, Integer> {
    HasLocalAs(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A BgpNeighbor with localAs:", "localAs");
    }

    @Override
    protected Integer featureValueOf(BgpNeighbor actual) {
      return actual.getLocalAs();
    }
  }
}
