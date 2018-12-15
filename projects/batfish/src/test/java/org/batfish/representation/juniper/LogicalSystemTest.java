package org.batfish.representation.juniper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class LogicalSystemTest {

  @Test
  public void testExpandInterfaceRangeInterfaceInheritance() {
    // only set one of the fields
    Interface i1 = new Interface("i1");
    i1.setMtu(1200);

    // set other fields here, one overlapping
    InterfaceRange irange = new InterfaceRange("irange");
    irange.setMtu(1000);
    irange.setDescription("dodo");
    irange.set8023adInterface("ae1");

    LogicalSystem.expandInterfaceRangeInterface(irange, i1);

    // retain original MTU
    assertThat(i1.getMtu(), equalTo(1200));

    // insert description and 8023ad
    assertThat(i1.getDescription(), equalTo("dodo"));
    assertThat(i1.get8023adInterface(), equalTo("ae1"));

    // leave redundant parent along
    assertNull(i1.getRedundantParentInterface());
  }
}
