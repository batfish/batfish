package org.batfish.question.specifiers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.batfish.datamodel.PathConstraints;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.NoNodesNodeSpecifier;
import org.junit.jupiter.api.Test;

/** Test for {@link PathConstraintsUtil}. */
final class PathConstraintsUtilTest {
  @Test
  void testUnconstrained() {
    PathConstraints pathConstraints =
        PathConstraintsUtil.createPathConstraints(PathConstraintsInput.unconstrained());

    assertEquals(LocationSpecifier.ALL_LOCATIONS, pathConstraints.getStartLocation());
    assertEquals(NoNodesNodeSpecifier.INSTANCE, pathConstraints.getForbiddenLocations());
    assertEquals(NoNodesNodeSpecifier.INSTANCE, pathConstraints.getTransitLocations());
    assertEquals(AllNodesNodeSpecifier.INSTANCE, pathConstraints.getEndLocation());
  }
}
