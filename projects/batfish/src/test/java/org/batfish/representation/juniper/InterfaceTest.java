package org.batfish.representation.juniper;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class InterfaceTest {

  @Test
  public void testInheritUnsetPhysicalFields() {
    // only set one of the fields
    Interface i1 = new Interface("i1");
    i1.setMtu(1200);

    // set twp fields here, one overlapping
    Interface bestower = new Interface("bestower");
    bestower.setMtu(1000);
    bestower.setDescription("dodo");

    i1.inheritUnsetPhysicalFields(bestower);

    // retain original MTU
    assertThat(i1.getMtu(), equalTo(1200));

    // inherit description
    assertThat(i1.getDescription(), equalTo("dodo"));

    // leave redundant parent along
    assertNull(i1.getRedundantParentInterface());
  }
}
