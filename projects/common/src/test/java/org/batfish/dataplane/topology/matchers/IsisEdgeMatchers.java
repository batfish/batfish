package org.batfish.dataplane.topology.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.isis.IsisEdge;
import org.batfish.datamodel.isis.IsisLevel;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class IsisEdgeMatchers {

  /**
   * Provides a matcher that matches if the {@link IsisEdge}'s circuitType is {@code
   * expectedCircuitType}.
   */
  public static @Nonnull Matcher<IsisEdge> hasCircuitType(@Nonnull IsisLevel expectedLevel) {
    return new HasCircuitType(equalTo(expectedLevel));
  }

  private IsisEdgeMatchers() {}

  private static final class HasCircuitType extends FeatureMatcher<IsisEdge, IsisLevel> {
    public HasCircuitType(@Nonnull Matcher<? super IsisLevel> subMatcher) {
      super(subMatcher, "An IsisEdge with circuitType:", "circuitType");
    }

    @Override
    protected IsisLevel featureValueOf(IsisEdge actual) {
      return actual.getCircuitType();
    }
  }
}
