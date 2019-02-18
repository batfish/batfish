package org.batfish.specifier;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.junit.Test;

public class NameInterfaceSpecifierTest {

  @Test
  public void testResolve() {

    Configuration node1 = new Configuration("node1", ConfigurationFormat.CISCO_IOS);
    Configuration node2 = new Configuration("node2", ConfigurationFormat.CISCO_IOS);

    Interface iface1node1 = Interface.builder().setName("iface1").setOwner(node1).build();
    Interface iface2node1 = Interface.builder().setName("iface2").setOwner(node1).build();
    Interface iface1node2 = Interface.builder().setName("iface1").setOwner(node2).build();

    node1.setInterfaces(
        ImmutableSortedMap.of(
            iface1node1.getName(), iface1node1, iface2node1.getName(), iface2node1));
    node2.setInterfaces(ImmutableSortedMap.of(iface1node2.getName(), iface1node2));

    MockSpecifierContext ctxt =
        MockSpecifierContext.builder()
            .setConfigs(ImmutableMap.of(node1.getHostname(), node1, node2.getHostname(), node2))
            .build();

    assertThat(
        new NameInterfaceSpecifier("iface1").resolve(ImmutableSet.of("node1"), ctxt),
        equalTo(ImmutableSet.of(iface1node1)));

    assertThat(
        new NameInterfaceSpecifier("iface1").resolve(ImmutableSet.of("node1", "node2"), ctxt),
        equalTo(ImmutableSet.of(iface1node1, iface1node2)));

    // bad name
    assertThat(
        new NameInterfaceSpecifier("iface").resolve(ImmutableSet.of("node1"), ctxt),
        equalTo(ImmutableSet.of()));

    // case insensitive
    assertThat(
        new NameInterfaceSpecifier("IfACe1").resolve(ImmutableSet.of("node1"), ctxt),
        equalTo(ImmutableSet.of(iface1node1)));
  }
}
