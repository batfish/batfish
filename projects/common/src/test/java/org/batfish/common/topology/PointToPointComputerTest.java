package org.batfish.common.topology;

import static org.batfish.common.topology.Layer1Topologies.INVALID_INTERFACE;
import static org.batfish.common.topology.PointToPointComputer.computeInterfaceToParent;
import static org.batfish.common.topology.PointToPointComputer.pointToPointInterfaces;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

public class PointToPointComputerTest {
  @Test
  public void testComputePhysicalPointToPoint() {
    Layer1Node n1 = new Layer1Node("a", "b");
    NodeInterfacePair i1 = n1.asNodeInterfacePair();
    Layer1Node n2 = new Layer1Node("c", "d");
    NodeInterfacePair i2 = n2.asNodeInterfacePair();
    Layer1Node n3 = new Layer1Node("e", "f");
    // One-sided
    assertThat(
        pointToPointInterfaces(new Layer1Topology(new Layer1Edge(n1, n2))),
        equalTo(ImmutableMap.of(i1, i2, i2, i1)));
    // Bidir
    assertThat(
        pointToPointInterfaces(new Layer1Topology(new Layer1Edge(n1, n2), new Layer1Edge(n2, n1))),
        equalTo(ImmutableMap.of(i1, i2, i2, i1)));
    // 3 nodes
    assertThat(
        pointToPointInterfaces(new Layer1Topology(new Layer1Edge(n1, n2), new Layer1Edge(n2, n3))),
        equalTo(ImmutableMap.of()));
    // INVALID_INTERFACE still invalidates p2p.
    assertThat(
        pointToPointInterfaces(
            new Layer1Topology(new Layer1Edge(n1, n2), new Layer1Edge(n2, INVALID_INTERFACE))),
        equalTo(ImmutableMap.of()));
    // INVALID_INTERFACE can't be in p2p.
    assertThat(
        pointToPointInterfaces(
            new Layer1Topology(
                new Layer1Edge(INVALID_INTERFACE, n1), new Layer1Edge(n1, INVALID_INTERFACE))),
        equalTo(ImmutableMap.of()));
  }

  @Test
  public void testComputeSubinterfaceToParent() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    // Inactive interface, so not valid.
    nf.interfaceBuilder()
        .setOwner(c)
        .setAdminUp(false)
        .setAddress(LinkLocalAddress.of(Ip.parse("169.254.0.1")))
        .setSwitchport(false)
        .setType(InterfaceType.PHYSICAL)
        .build();
    Interface physical =
        nf.interfaceBuilder()
            .setOwner(c)
            .setAdminUp(true)
            .setAddress(ConcreteInterfaceAddress.parse("10.0.0.1/24"))
            .setSwitchport(false)
            .setType(InterfaceType.PHYSICAL)
            .build();
    Interface lla =
        nf.interfaceBuilder()
            .setOwner(c)
            .setAdminUp(true)
            .setAddress(LinkLocalAddress.of(Ip.parse("169.254.0.1")))
            .setSwitchport(false)
            .setType(InterfaceType.PHYSICAL)
            .build();
    Interface agg =
        nf.interfaceBuilder()
            .setOwner(c)
            .setAdminUp(true)
            .setAddress(LinkLocalAddress.of(Ip.parse("169.254.0.1")))
            .setSwitchport(false)
            .setType(InterfaceType.AGGREGATED)
            .build();
    Interface aggChild =
        nf.interfaceBuilder()
            .setOwner(c)
            .setAdminUp(true)
            .setAddress(LinkLocalAddress.of(Ip.parse("169.254.0.1")))
            .setSwitchport(false)
            .setType(InterfaceType.AGGREGATE_CHILD)
            .setDependencies(ImmutableSet.of(new Dependency(agg.getName(), DependencyType.BIND)))
            .build();
    // A virtual interface like a VLAN has no connection
    nf.interfaceBuilder()
        .setOwner(c)
        .setAdminUp(true)
        .setAddress(ConcreteInterfaceAddress.parse("1.2.3.4/24"))
        .setSwitchport(false)
        .setType(InterfaceType.VLAN)
        .build();
    assertThat(
        computeInterfaceToParent(ImmutableMap.of(c.getHostname(), c)),
        equalTo(
            ImmutableMap.of(
                NodeInterfacePair.of(physical),
                NodeInterfacePair.of(physical),
                NodeInterfacePair.of(lla),
                NodeInterfacePair.of(lla),
                NodeInterfacePair.of(agg),
                NodeInterfacePair.of(agg),
                NodeInterfacePair.of(aggChild),
                NodeInterfacePair.of(agg))));
  }
}
