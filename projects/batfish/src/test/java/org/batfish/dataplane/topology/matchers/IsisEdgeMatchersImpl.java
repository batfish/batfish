package org.batfish.dataplane.topology.matchers;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IsisLevel;
import org.batfish.dataplane.topology.IsisEdge;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class IsisEdgeMatchersImpl {

  static final class HasCircuitType extends FeatureMatcher<IsisEdge, IsisLevel> {
    public HasCircuitType(@Nonnull Matcher<? super IsisLevel> subMatcher) {
      super(subMatcher, "An IsisEdge with circuitType:", "circuitType");
    }

    @Override
    protected IsisLevel featureValueOf(IsisEdge actual) {
      return actual.getCircuitType();
    }
  }

  private IsisEdgeMatchersImpl() {}
}
