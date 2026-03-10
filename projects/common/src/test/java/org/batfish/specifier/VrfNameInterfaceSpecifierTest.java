package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.TestInterface;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

public class VrfNameInterfaceSpecifierTest {

  @Test
  public void resolve() {

    Configuration node1 = new Configuration("node1", ConfigurationFormat.CISCO_IOS);
    Configuration node2 = new Configuration("node2", ConfigurationFormat.CISCO_IOS);

    Vrf vrf1node1 = new Vrf("vrf1");
    Vrf vrf2node1 = new Vrf("vrf2");
    Vrf vrf1node2 = new Vrf("vrf1");

    Interface iface11 =
        TestInterface.builder().setName("iface11").setOwner(node1).setVrf(vrf1node1).build();
    TestInterface.builder().setName("iface12").setOwner(node1).setVrf(vrf2node1).build();
    Interface iface2 =
        TestInterface.builder().setName("iface2").setOwner(node2).setVrf(vrf1node2).build();

    node1.setVrfs(ImmutableSortedMap.of("vrf1", vrf1node1, "vrf2", vrf2node1));
    node2.setVrfs(ImmutableSortedMap.of("vrf1", vrf1node2));

    MockSpecifierContext ctxt =
        MockSpecifierContext.builder()
            .setConfigs(ImmutableMap.of("node1", node1, "node2", node2))
            .build();

    // vrf1 on node1 should yield one interface
    assertThat(
        new VrfNameInterfaceSpecifier("vrf1").resolve(ImmutableSet.of("node1"), ctxt),
        contains(NodeInterfacePair.of(iface11)));

    // vrf1 on node1, node2 should yield one interface
    assertThat(
        new VrfNameInterfaceSpecifier("vrf1").resolve(ImmutableSet.of("node1", "node2"), ctxt),
        containsInAnyOrder(NodeInterfacePair.of(iface11), NodeInterfacePair.of(iface2)));

    // case insensitivity
    assertThat(
        new VrfNameInterfaceSpecifier("VrF1").resolve(ImmutableSet.of("node1"), ctxt),
        contains(NodeInterfacePair.of(iface11)));

    // bad names
    assertThat(
        new VrfNameInterfaceSpecifier("vrf").resolve(ImmutableSet.of("node1"), ctxt), empty());
    assertThat(
        new VrfNameInterfaceSpecifier("v.*").resolve(ImmutableSet.of("node1"), ctxt), empty());
  }
}
