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

  static final class HasEnforceFirstAs extends FeatureMatcher<BgpNeighbor, Boolean> {
    HasEnforceFirstAs(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A BgpNeighbor with enforce-first-as:", "enforce-first-as");
    }

    @Override
    protected Boolean featureValueOf(BgpNeighbor actual) {
      return actual.getEnforceFirstAs();
    }
  }

  static final class HasRemoteAs extends FeatureMatcher<BgpNeighbor, Integer> {
    HasRemoteAs(@Nonnull Matcher<? super Integer> subMatcher) {
      super(subMatcher, "A BgpNeighbor with remoteAs:", "remoteAs");
    }

    @Override
    protected Integer featureValueOf(BgpNeighbor actual) {
      return actual.getRemoteAs();
    }
  }

  static final class IsDynamic extends FeatureMatcher<BgpNeighbor, Boolean> {
    IsDynamic(@Nonnull Matcher<? super Boolean> subMatcher) {
      super(subMatcher, "A BgpNeighbor with dynamic set to:", "dynamic");
    }

    @Override
    protected Boolean featureValueOf(BgpNeighbor actual) {
      return actual.getDynamic();
    }
  }
}
