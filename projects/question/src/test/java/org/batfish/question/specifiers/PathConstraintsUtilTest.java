package org.batfish.question.specifiers;

import static org.junit.Assert.assertEquals;

import org.batfish.datamodel.PathConstraints;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.NoNodesNodeSpecifier;
import org.junit.Test;

/** Test for {@link PathConstraintsUtil}. */
public final class PathConstraintsUtilTest {
  @Test
  public void testUnconstrained() {
    PathConstraints pathConstraints =
        PathConstraintsUtil.createPathConstraints(PathConstraintsInput.unconstrained());

    assertEquals(LocationSpecifier.ALL_LOCATIONS, pathConstraints.getStartLocation());
    assertEquals(NoNodesNodeSpecifier.INSTANCE, pathConstraints.getForbiddenLocations());
    assertEquals(NoNodesNodeSpecifier.INSTANCE, pathConstraints.getTransitLocations());
    assertEquals(AllNodesNodeSpecifier.INSTANCE, pathConstraints.getEndLocation());
  }
}
