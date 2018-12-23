package org.batfish.representation.juniper;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public final class LogicalSystemTest {

  @Test
  public void testExpandInterfaceRanges() {
    LogicalSystem ls = new LogicalSystem("ls");

    // add an explicit interface with a configured MTU
    Interface iface = new Interface("xe-0/0/0");
    iface.setMtu(9000);
    ls.getInterfaces().put("xe-0/0/0", iface);

    // create a range that has the interface above and one other member interface
    InterfaceRange irange =
        ls.getInterfaceRanges().computeIfAbsent("irange", i -> new InterfaceRange("irange"));
    irange.getMembers().add(new InterfaceRangeMember("xe-0/0/0"));
    irange.getMembers().add(new InterfaceRangeMember("xe-0/0/1"));
    irange.setMtu(8000);

    ls.expandInterfaceRanges();

    // the MTU of the explicitly defined interface should be what was configured
    org.batfish.representation.juniper.Interface xe000 = ls.getInterfaces().get("xe-0/0/0");
    assertThat(xe000.getMtu(), equalTo(9000));

    // there should be another interface and its MTU should have been inherited
    org.batfish.representation.juniper.Interface xe001 = ls.getInterfaces().get("xe-0/0/1");
    assertThat(xe001.getMtu(), equalTo(8000));
  }
}
