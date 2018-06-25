package org.batfish.dataplane.topology.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IsisLevel;
import org.batfish.dataplane.topology.IsisEdge;
import org.batfish.dataplane.topology.matchers.IsisEdgeMatchersImpl.HasCircuitType;
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
}
