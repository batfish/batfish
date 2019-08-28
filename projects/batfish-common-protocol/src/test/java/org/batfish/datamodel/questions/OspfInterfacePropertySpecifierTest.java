package org.batfish.datamodel.questions;

import static org.batfish.datamodel.questions.OspfInterfacePropertySpecifier.ALL;
import static org.batfish.datamodel.questions.OspfInterfacePropertySpecifier.PROPERTIES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import org.junit.Test;

/** Test for {@link OspfInterfacePropertySpecifier} */
public class OspfInterfacePropertySpecifierTest {
  @Test
  public void testOrderedProperties() {
    // Useful for confirming we have not forgotten to update the ordered list of properties when a
    // new property is added

    // Confirm ordered properties list contains all supported properties
    assertThat(PROPERTIES, containsInAnyOrder(ALL.getMatchingProperties().toArray()));
  }
}
