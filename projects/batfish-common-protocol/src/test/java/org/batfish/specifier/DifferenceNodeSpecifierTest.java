package org.batfish.specifier;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public final class DifferenceNodeSpecifierTest {

  @Test
  public void testEquals() {
    DifferenceNodeSpecifier group1Elem1 =
        new DifferenceNodeSpecifier(AllNodesNodeSpecifier.INSTANCE, AllNodesNodeSpecifier.INSTANCE);
    DifferenceNodeSpecifier group1Elem2 =
        new DifferenceNodeSpecifier(AllNodesNodeSpecifier.INSTANCE, AllNodesNodeSpecifier.INSTANCE);
    DifferenceNodeSpecifier group2Elem1 =
        new DifferenceNodeSpecifier(AllNodesNodeSpecifier.INSTANCE, NoNodesNodeSpecifier.INSTANCE);
    DifferenceNodeSpecifier group3Elem1 =
        new DifferenceNodeSpecifier(NoNodesNodeSpecifier.INSTANCE, NoNodesNodeSpecifier.INSTANCE);
    new EqualsTester()
        .addEqualityGroup(group1Elem1, group1Elem2)
        .addEqualityGroup(group2Elem1)
        .addEqualityGroup(group3Elem1)
        .testEquals();
  }
}
