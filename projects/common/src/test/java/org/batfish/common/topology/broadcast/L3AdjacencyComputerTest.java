package org.batfish.common.topology.broadcast;

import static org.batfish.common.topology.broadcast.L3AdjacencyComputer.BATFISH_GLOBAL_HUB;
import static org.batfish.common.topology.broadcast.L3AdjacencyComputer.computeEthernetHubs;
import static org.batfish.common.topology.broadcast.L3AdjacencyComputer.connectL2InterfaceToBroadcastDomain;
import static org.batfish.common.topology.broadcast.L3AdjacencyComputer.connectL3InterfaceToPhysicalOrDomain;
import static org.batfish.common.topology.broadcast.L3AdjacencyComputer.findCorrespondingPhysicalInterface;
import static org.batfish.common.topology.broadcast.L3AdjacencyComputer.shouldCreateL3Interface;
import static org.batfish.common.topology.broadcast.L3AdjacencyComputer.shouldCreatePhysicalInterface;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.batfish.datamodel.InterfaceType.LOGICAL;
import static org.batfish.datamodel.InterfaceType.LOOPBACK;
import static org.batfish.datamodel.InterfaceType.PHYSICAL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.topology.Layer1Topologies;
import org.batfish.common.topology.Layer1TopologiesFactory;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.TestInterface;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.batfish.datamodel.vxlan.VxlanTopology;
import org.batfish.datamodel.vxlan.VxlanTopologyUtils;
import org.junit.Test;

public class L3AdjacencyComputerTest {
  private static final InterfaceAddress CONCRETE = ConcreteInterfaceAddress.parse("1.2.3.4/24");

  private static Set<Dependency> dependsOn(Interface i) {
    return ImmutableSet.of(new Dependency(i.getName(), DependencyType.BIND));
  }

  @Test
  public void testFindCorrespondingPhysicalInterface_physical() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface physical = nf.interfaceBuilder().setOwner(c).setType(PHYSICAL).build();
    NodeInterfacePair nip = NodeInterfacePair.of(physical);
    PhysicalInterface iface = new PhysicalInterface(nip);
    // Correct
    assertThat(
        findCorrespondingPhysicalInterface(
            physical, nip, c.getAllInterfaces(), ImmutableMap.of(nip, iface)),
        equalTo(Optional.of(iface)));
    // Missing physical interface
    assertThat(
        findCorrespondingPhysicalInterface(physical, nip, c.getAllInterfaces(), ImmutableMap.of()),
        equalTo(Optional.empty()));
  }

  @Test
  public void testFindCorrespondingPhysicalInterface_subinterface() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface physical = nf.interfaceBuilder().setOwner(c).setType(PHYSICAL).build();
    NodeInterfacePair physicalNip = NodeInterfacePair.of(physical);
    PhysicalInterface physicalIface = new PhysicalInterface(physicalNip);

    // Correct
    Interface subif =
        nf.interfaceBuilder()
            .setOwner(c)
            .setType(LOGICAL)
            .setDependencies(dependsOn(physical))
            .build();
    assertThat(
        findCorrespondingPhysicalInterface(
            subif,
            NodeInterfacePair.of(subif),
            c.getAllInterfaces(),
            ImmutableMap.of(physicalNip, physicalIface)),
        equalTo(Optional.of(physicalIface)));

    // Dep not in configuration
    assertThat(
        findCorrespondingPhysicalInterface(
            subif,
            NodeInterfacePair.of(subif),
            ImmutableMap.of(subif.getName(), subif),
            ImmutableMap.of()),
        equalTo(Optional.empty()));
    // Dep not a physical interface
    assertThat(
        findCorrespondingPhysicalInterface(
            subif, NodeInterfacePair.of(subif), c.getAllInterfaces(), ImmutableMap.of()),
        equalTo(Optional.empty()));

    // No deps
    Interface subifNoDeps = nf.interfaceBuilder().setOwner(c).setType(LOGICAL).build();
    assertThat(
        findCorrespondingPhysicalInterface(
            subifNoDeps,
            NodeInterfacePair.of(subifNoDeps),
            c.getAllInterfaces(),
            ImmutableMap.of(physicalNip, physicalIface)),
        equalTo(Optional.empty()));
  }

  @Test
  public void testConnectL2InterfaceToBroadcastDomain_l3() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface l3 =
        nf.interfaceBuilder()
            .setOwner(c)
            .setAddress(CONCRETE)
            .setType(PHYSICAL)
            .setSwitchport(false)
            .setSwitchportMode(SwitchportMode.NONE)
            .build();
    NodeInterfacePair nip = NodeInterfacePair.of(l3);
    DeviceBroadcastDomain domain = new DeviceBroadcastDomain(c.getHostname());
    PhysicalInterface iface = new PhysicalInterface(nip);

    // Since L3 interface, no edges should be established.
    connectL2InterfaceToBroadcastDomain(
        l3, c.getAllInterfaces(), ImmutableMap.of(nip, iface), domain);
    assertThat(domain.getL3InterfacesForTest(), anEmptyMap());
    assertThat(domain.getPhysicalInterfacesForTest(), anEmptyMap());
    assertThat(iface.getAttachedHubForTest(), nullValue());
    assertThat(iface.getSwitchForTest(), nullValue());
    assertThat(iface.getL3InterfacesForTest(), anEmptyMap());
  }

  @Test
  public void testConnectL2InterfaceToBroadcastDomain_access() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface access =
        nf.interfaceBuilder()
            .setOwner(c)
            .setType(PHYSICAL)
            .setSwitchport(true)
            .setSwitchportMode(SwitchportMode.ACCESS)
            .setAccessVlan(3)
            .build();
    NodeInterfacePair nip = NodeInterfacePair.of(access);

    {
      // Access should be attached
      DeviceBroadcastDomain domain = new DeviceBroadcastDomain(c.getHostname());
      PhysicalInterface iface = new PhysicalInterface(nip);
      connectL2InterfaceToBroadcastDomain(
          access, c.getAllInterfaces(), ImmutableMap.of(nip, iface), domain);
      assertThat(domain.getL3InterfacesForTest(), anEmptyMap());
      assertThat(domain.getPhysicalInterfacesForTest().keySet(), contains(sameInstance(iface)));
      assertThat(iface.getAttachedHubForTest(), nullValue());
      assertThat(iface.getSwitchForTest(), sameInstance(domain));
      assertThat(iface.getL3InterfacesForTest(), anEmptyMap());
    }

    {
      // Missing access vlan should be no results
      DeviceBroadcastDomain domain = new DeviceBroadcastDomain(c.getHostname());
      PhysicalInterface iface = new PhysicalInterface(nip);
      access.setAccessVlan(null);
      connectL2InterfaceToBroadcastDomain(
          access, c.getAllInterfaces(), ImmutableMap.of(nip, iface), domain);
      assertThat(domain.getL3InterfacesForTest(), anEmptyMap());
      assertThat(domain.getPhysicalInterfacesForTest(), anEmptyMap());
      assertThat(iface.getAttachedHubForTest(), nullValue());
      assertThat(iface.getSwitchForTest(), nullValue());
      assertThat(iface.getL3InterfacesForTest(), anEmptyMap());
    }
  }

  @Test
  public void testConnectL2InterfaceToBroadcastDomain_trunk() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface trunk =
        nf.interfaceBuilder()
            .setOwner(c)
            .setType(PHYSICAL)
            .setSwitchport(true)
            .setSwitchportMode(SwitchportMode.TRUNK)
            .setAllowedVlans(IntegerSpace.of(3))
            .setNativeVlan(3)
            .build();
    NodeInterfacePair nip = NodeInterfacePair.of(trunk);

    // Trunk should be attached
    DeviceBroadcastDomain domain = new DeviceBroadcastDomain(c.getHostname());
    PhysicalInterface iface = new PhysicalInterface(nip);
    connectL2InterfaceToBroadcastDomain(
        trunk, c.getAllInterfaces(), ImmutableMap.of(nip, iface), domain);
    assertThat(domain.getL3InterfacesForTest(), anEmptyMap());
    assertThat(domain.getPhysicalInterfacesForTest().keySet(), contains(sameInstance(iface)));
    assertThat(iface.getAttachedHubForTest(), nullValue());
    assertThat(iface.getSwitchForTest(), sameInstance(domain));
    assertThat(iface.getL3InterfacesForTest(), anEmptyMap());
  }

  @Test
  public void testConnectL2InterfaceToBroadcastDomain_subif_access() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface physical = nf.interfaceBuilder().setOwner(c).setType(PHYSICAL).build();
    Interface subif =
        nf.interfaceBuilder()
            .setOwner(c)
            .setType(LOGICAL)
            .setSwitchport(true)
            .setSwitchportMode(SwitchportMode.ACCESS)
            .setDependencies(dependsOn(physical))
            .setAccessVlan(3)
            .build();
    NodeInterfacePair physicalNip = NodeInterfacePair.of(physical);

    {
      // Should be attached to physical iface
      DeviceBroadcastDomain domain = new DeviceBroadcastDomain(c.getHostname());
      PhysicalInterface iface = new PhysicalInterface(physicalNip);
      connectL2InterfaceToBroadcastDomain(
          subif, c.getAllInterfaces(), ImmutableMap.of(physicalNip, iface), domain);
      assertThat(domain.getL3InterfacesForTest(), anEmptyMap());
      assertThat(domain.getPhysicalInterfacesForTest().keySet(), contains(sameInstance(iface)));
      assertThat(iface.getAttachedHubForTest(), nullValue());
      assertThat(iface.getSwitchForTest(), sameInstance(domain));
      assertThat(iface.getL3InterfacesForTest(), anEmptyMap());
    }
    {
      // Clear the dependency, should find nothing
      DeviceBroadcastDomain domain = new DeviceBroadcastDomain(c.getHostname());
      PhysicalInterface iface = new PhysicalInterface(physicalNip);
      subif.setDependencies(ImmutableSet.of());
      connectL2InterfaceToBroadcastDomain(
          subif, c.getAllInterfaces(), ImmutableMap.of(physicalNip, iface), domain);
      assertThat(domain.getL3InterfacesForTest(), anEmptyMap());
      assertThat(domain.getPhysicalInterfacesForTest(), anEmptyMap());
      assertThat(iface.getAttachedHubForTest(), nullValue());
      assertThat(iface.getSwitchForTest(), nullValue());
      assertThat(iface.getL3InterfacesForTest(), anEmptyMap());
    }
  }

  @Test
  public void testConnectL2InterfaceToBroadcastDomain_unknown_mode() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface unhandledMode =
        nf.interfaceBuilder()
            .setOwner(c)
            .setType(PHYSICAL)
            .setSwitchport(true)
            .setSwitchportMode(SwitchportMode.MONITOR)
            .build();
    NodeInterfacePair nip = NodeInterfacePair.of(unhandledMode);

    DeviceBroadcastDomain domain = new DeviceBroadcastDomain(c.getHostname());
    PhysicalInterface iface = new PhysicalInterface(nip);
    connectL2InterfaceToBroadcastDomain(
        unhandledMode, c.getAllInterfaces(), ImmutableMap.of(nip, iface), domain);
    assertThat(domain.getL3InterfacesForTest(), anEmptyMap());
    assertThat(domain.getPhysicalInterfacesForTest(), anEmptyMap());
    assertThat(iface.getAttachedHubForTest(), nullValue());
    assertThat(iface.getSwitchForTest(), nullValue());
    assertThat(iface.getL3InterfacesForTest(), anEmptyMap());
  }

  @Test
  public void testShouldCreatePhysicalInterface() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    assertTrue(
        "physical should be created",
        shouldCreatePhysicalInterface(
            nf.interfaceBuilder().setOwner(c).setType(PHYSICAL).setAdminUp(true).build()));
    assertFalse(
        "physical but shutdown should not be created",
        shouldCreatePhysicalInterface(
            nf.interfaceBuilder().setOwner(c).setType(PHYSICAL).setAdminUp(false).build()));
    assertFalse(
        "physical, active but aggregated should not be created",
        shouldCreatePhysicalInterface(
            nf.interfaceBuilder()
                .setOwner(c)
                .setType(PHYSICAL)
                .setChannelGroup("Port-Channel1")
                .setAdminUp(true)
                .build()));
    assertFalse(
        "logical should not be created",
        shouldCreatePhysicalInterface(nf.interfaceBuilder().setOwner(c).setType(LOGICAL).build()));
    assertFalse(
        "vlan should not be created",
        shouldCreatePhysicalInterface(
            nf.interfaceBuilder()
                .setOwner(c)
                .setType(InterfaceType.VLAN)
                .setAdminUp(true)
                .build()));
    assertTrue(
        "aggregate,active should be created",
        shouldCreatePhysicalInterface(
            nf.interfaceBuilder()
                .setOwner(c)
                .setType(InterfaceType.AGGREGATED)
                .setAdminUp(true)
                .build()));
    assertFalse(
        "aggregate but shutdown should not be created",
        shouldCreatePhysicalInterface(
            nf.interfaceBuilder()
                .setOwner(c)
                .setType(InterfaceType.AGGREGATED)
                .setAdminUp(false)
                .build()));
    assertFalse(
        "aggregate child should not be created",
        shouldCreatePhysicalInterface(
            nf.interfaceBuilder()
                .setOwner(c)
                .setType(InterfaceType.AGGREGATE_CHILD)
                .setAdminUp(true)
                .build()));
  }

  @Test
  public void testShouldCreateL3Interface() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    assertTrue(
        "l3 should be created",
        shouldCreateL3Interface(
            nf.interfaceBuilder()
                .setOwner(c)
                .setType(PHYSICAL)
                .setSwitchport(false)
                .setAddress(CONCRETE)
                .setAdminUp(true)
                .build()));
    assertTrue(
        "l3 with LLA should be created",
        shouldCreateL3Interface(
            nf.interfaceBuilder()
                .setOwner(c)
                .setType(PHYSICAL)
                .setSwitchport(false)
                .setAddress(LinkLocalAddress.of(Ip.parse("169.254.0.1")))
                .setAdminUp(true)
                .build()));
    assertFalse(
        "l3 with no addresses should not be created",
        shouldCreateL3Interface(
            nf.interfaceBuilder()
                .setOwner(c)
                .setType(PHYSICAL)
                .setSwitchport(false)
                .setAdminUp(true)
                .build()));
    assertFalse(
        "l3 shutdown should not be created",
        shouldCreateL3Interface(
            nf.interfaceBuilder()
                .setOwner(c)
                .setType(PHYSICAL)
                .setSwitchport(false)
                .setAddress(CONCRETE)
                .setAdminUp(false)
                .build()));
    assertFalse(
        "l3 loopback should not be created",
        shouldCreateL3Interface(
            nf.interfaceBuilder()
                .setOwner(c)
                .setType(LOOPBACK)
                .setSwitchport(false)
                .setAddress(CONCRETE)
                .setAdminUp(true)
                .build()));
    assertFalse(
        "l3 in weird l3/switchport mode should not be created",
        shouldCreateL3Interface(
            nf.interfaceBuilder()
                .setOwner(c)
                .setType(PHYSICAL)
                .setSwitchport(true)
                .setSwitchportMode(SwitchportMode.ACCESS)
                .setAddress(CONCRETE)
                .setAdminUp(true)
                .build()));
  }

  @Test
  public void testConnectL3InterfaceToPhysicalOrDomain_physical() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface physical =
        nf.interfaceBuilder().setOwner(c).setType(PHYSICAL).setAddress(CONCRETE).build();
    {
      // Connect to physical interface and not domain
      L3Interface iface = new L3Interface(NodeInterfacePair.of(physical));
      PhysicalInterface physicalInterface = new PhysicalInterface(NodeInterfacePair.of(physical));
      DeviceBroadcastDomain domain = new DeviceBroadcastDomain(c.getHostname());
      connectL3InterfaceToPhysicalOrDomain(
          physical,
          iface,
          c.getAllInterfaces(),
          ImmutableMap.of(physicalInterface.getIface(), physicalInterface),
          ImmutableMap.of(domain.getHostname(), domain));
      assertThat(iface.getSendToInterfaceForTesting(), sameInstance(physicalInterface));
      assertThat(iface.getSendToSwitchForTesting(), nullValue());
      assertThat(physicalInterface.getL3InterfacesForTest().keySet(), contains(iface));
      assertThat(physicalInterface.getSwitchForTest(), nullValue());
      assertThat(domain.getPhysicalInterfacesForTest(), anEmptyMap());
      assertThat(domain.getL3InterfacesForTest(), anEmptyMap());
    }
    {
      // Missing physical interface
      L3Interface iface = new L3Interface(NodeInterfacePair.of(physical));
      PhysicalInterface physicalInterface = new PhysicalInterface(NodeInterfacePair.of(physical));
      DeviceBroadcastDomain domain = new DeviceBroadcastDomain(c.getHostname());
      connectL3InterfaceToPhysicalOrDomain(
          physical,
          iface,
          c.getAllInterfaces(),
          ImmutableMap.of(),
          ImmutableMap.of(domain.getHostname(), domain));
      assertThat(iface.getSendToInterfaceForTesting(), nullValue());
      assertThat(iface.getSendToSwitchForTesting(), nullValue());
      assertThat(physicalInterface.getL3InterfacesForTest(), anEmptyMap());
      assertThat(physicalInterface.getSwitchForTest(), nullValue());
      assertThat(domain.getPhysicalInterfacesForTest(), anEmptyMap());
      assertThat(domain.getL3InterfacesForTest(), anEmptyMap());
    }
  }

  @Test
  public void testConnectL3InterfaceToPhysicalOrDomain_subif() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface physical = nf.interfaceBuilder().setOwner(c).setType(PHYSICAL).build();
    Interface subif =
        nf.interfaceBuilder()
            .setOwner(c)
            .setType(LOGICAL)
            .setAddress(CONCRETE)
            .setDependencies(dependsOn(physical))
            .setEncapsulationVlan(3)
            .build();
    {
      // Connect to physical interface and not domain
      L3Interface iface = new L3Interface(NodeInterfacePair.of(subif));
      PhysicalInterface physicalInterface = new PhysicalInterface(NodeInterfacePair.of(physical));
      DeviceBroadcastDomain domain = new DeviceBroadcastDomain(c.getHostname());
      connectL3InterfaceToPhysicalOrDomain(
          subif,
          iface,
          c.getAllInterfaces(),
          ImmutableMap.of(physicalInterface.getIface(), physicalInterface),
          ImmutableMap.of(domain.getHostname(), domain));
      assertThat(iface.getSendToInterfaceForTesting(), sameInstance(physicalInterface));
      assertThat(iface.getSendToSwitchForTesting(), nullValue());
      assertThat(physicalInterface.getL3InterfacesForTest().keySet(), contains(iface));
      assertThat(physicalInterface.getSwitchForTest(), nullValue());
      assertThat(domain.getPhysicalInterfacesForTest(), anEmptyMap());
      assertThat(domain.getL3InterfacesForTest(), anEmptyMap());
    }
    {
      // Parent is missing on config
      L3Interface iface = new L3Interface(NodeInterfacePair.of(subif));
      PhysicalInterface physicalInterface = new PhysicalInterface(NodeInterfacePair.of(physical));
      DeviceBroadcastDomain domain = new DeviceBroadcastDomain(c.getHostname());
      connectL3InterfaceToPhysicalOrDomain(
          subif,
          iface,
          ImmutableMap.of(),
          ImmutableMap.of(physicalInterface.getIface(), physicalInterface),
          ImmutableMap.of(domain.getHostname(), domain));
      assertThat(iface.getSendToInterfaceForTesting(), nullValue());
      assertThat(iface.getSendToSwitchForTesting(), nullValue());
      assertThat(physicalInterface.getL3InterfacesForTest(), anEmptyMap());
      assertThat(physicalInterface.getSwitchForTest(), nullValue());
      assertThat(domain.getPhysicalInterfacesForTest(), anEmptyMap());
      assertThat(domain.getL3InterfacesForTest(), anEmptyMap());
    }
    {
      // Parent is missing PhysicalInterface
      L3Interface iface = new L3Interface(NodeInterfacePair.of(subif));
      PhysicalInterface physicalInterface = new PhysicalInterface(NodeInterfacePair.of(physical));
      DeviceBroadcastDomain domain = new DeviceBroadcastDomain(c.getHostname());
      connectL3InterfaceToPhysicalOrDomain(
          subif,
          iface,
          c.getAllInterfaces(),
          ImmutableMap.of(),
          ImmutableMap.of(domain.getHostname(), domain));
      assertThat(iface.getSendToInterfaceForTesting(), nullValue());
      assertThat(iface.getSendToSwitchForTesting(), nullValue());
      assertThat(physicalInterface.getL3InterfacesForTest(), anEmptyMap());
      assertThat(physicalInterface.getSwitchForTest(), nullValue());
      assertThat(domain.getPhysicalInterfacesForTest(), anEmptyMap());
      assertThat(domain.getL3InterfacesForTest(), anEmptyMap());
    }
    {
      // Subif handles untagged frames
      subif.setEncapsulationVlan(null);
      L3Interface iface = new L3Interface(NodeInterfacePair.of(subif));
      PhysicalInterface physicalInterface = new PhysicalInterface(NodeInterfacePair.of(physical));
      DeviceBroadcastDomain domain = new DeviceBroadcastDomain(c.getHostname());
      connectL3InterfaceToPhysicalOrDomain(
          subif,
          iface,
          c.getAllInterfaces(),
          ImmutableMap.of(physicalInterface.getIface(), physicalInterface),
          ImmutableMap.of(domain.getHostname(), domain));
      assertThat(iface.getSendToInterfaceForTesting(), sameInstance(physicalInterface));
      assertThat(iface.getSendToSwitchForTesting(), nullValue());
      assertThat(physicalInterface.getL3InterfacesForTest().keySet(), contains(iface));
      assertThat(physicalInterface.getSwitchForTest(), nullValue());
      assertThat(domain.getPhysicalInterfacesForTest(), anEmptyMap());
      assertThat(domain.getL3InterfacesForTest(), anEmptyMap());
    }
  }

  @Test
  public void testConnectL3InterfaceToPhysicalOrDomain_vlan() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface vlan =
        nf.interfaceBuilder()
            .setOwner(c)
            .setType(InterfaceType.VLAN)
            .setAddress(CONCRETE)
            .setVlan(4)
            .build();
    {
      // Connect to domain and not physical interface
      L3Interface iface = new L3Interface(NodeInterfacePair.of(vlan));
      // Should not create physical iface for VLAN, but ensure the connection doesn't happen anyway.
      PhysicalInterface physicalInterface = new PhysicalInterface(NodeInterfacePair.of(vlan));
      DeviceBroadcastDomain domain = new DeviceBroadcastDomain(c.getHostname());
      connectL3InterfaceToPhysicalOrDomain(
          vlan,
          iface,
          c.getAllInterfaces(),
          ImmutableMap.of(physicalInterface.getIface(), physicalInterface),
          ImmutableMap.of(domain.getHostname(), domain));
      assertThat(iface.getSendToInterfaceForTesting(), nullValue());
      assertThat(iface.getSendToSwitchForTesting(), sameInstance(domain));
      assertThat(physicalInterface.getL3InterfacesForTest(), anEmptyMap());
      assertThat(physicalInterface.getSwitchForTest(), nullValue());
      assertThat(domain.getPhysicalInterfacesForTest(), anEmptyMap());
      assertThat(domain.getL3InterfacesForTest().keySet(), contains(iface));
    }
    {
      // Missing domain
      L3Interface iface = new L3Interface(NodeInterfacePair.of(vlan));
      // Should not create physical iface for VLAN, but ensure the connection doesn't happen anyway.
      PhysicalInterface physicalInterface = new PhysicalInterface(NodeInterfacePair.of(vlan));
      connectL3InterfaceToPhysicalOrDomain(
          vlan,
          iface,
          c.getAllInterfaces(),
          ImmutableMap.of(physicalInterface.getIface(), physicalInterface),
          ImmutableMap.of());
      assertThat(iface.getSendToInterfaceForTesting(), nullValue());
      assertThat(iface.getSendToSwitchForTesting(), nullValue());
      assertThat(physicalInterface.getL3InterfacesForTest(), anEmptyMap());
      assertThat(physicalInterface.getSwitchForTest(), nullValue());
    }
    {
      // Missing VLAN
      vlan.setVlan(null);
      L3Interface iface = new L3Interface(NodeInterfacePair.of(vlan));
      // Should not create physical iface for VLAN, but ensure the connection doesn't happen anyway.
      PhysicalInterface physicalInterface = new PhysicalInterface(NodeInterfacePair.of(vlan));
      DeviceBroadcastDomain domain = new DeviceBroadcastDomain(c.getHostname());
      connectL3InterfaceToPhysicalOrDomain(
          vlan,
          iface,
          c.getAllInterfaces(),
          ImmutableMap.of(physicalInterface.getIface(), physicalInterface),
          ImmutableMap.of(domain.getHostname(), domain));
      assertThat(iface.getSendToInterfaceForTesting(), nullValue());
      assertThat(iface.getSendToSwitchForTesting(), nullValue());
      assertThat(physicalInterface.getL3InterfacesForTest(), anEmptyMap());
      assertThat(physicalInterface.getSwitchForTest(), nullValue());
      assertThat(domain.getPhysicalInterfacesForTest(), anEmptyMap());
      assertThat(domain.getL3InterfacesForTest(), anEmptyMap());
    }
  }

  @Test
  public void testConnectL3InterfaceToPhysicalOrDomain_unknown() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface loopback =
        nf.interfaceBuilder().setOwner(c).setType(LOOPBACK).setAddress(CONCRETE).build();
    {
      // Loopback should be filtered out ahead of time, but if it makes it here
      // then there should still be no issues.
      L3Interface iface = new L3Interface(NodeInterfacePair.of(loopback));
      DeviceBroadcastDomain domain = new DeviceBroadcastDomain(c.getHostname());
      connectL3InterfaceToPhysicalOrDomain(
          loopback,
          iface,
          c.getAllInterfaces(),
          ImmutableMap.of(),
          ImmutableMap.of(domain.getHostname(), domain));
      assertThat(iface.getSendToInterfaceForTesting(), nullValue());
      assertThat(iface.getSendToSwitchForTesting(), nullValue());
      assertThat(domain.getPhysicalInterfacesForTest(), anEmptyMap());
      assertThat(domain.getL3InterfacesForTest(), anEmptyMap());
    }
  }

  private static Map<NodeInterfacePair, PhysicalInterface> makePhysicalMap(
      Interface... interfaces) {
    return Arrays.stream(interfaces)
        .map(NodeInterfacePair::of)
        .collect(ImmutableMap.toImmutableMap(nip -> nip, PhysicalInterface::new));
  }

  private static Set<NodeInterfacePair> getPairs(Collection<PhysicalInterface> interfaces) {
    return interfaces.stream()
        .map(PhysicalInterface::getIface)
        .collect(ImmutableSet.toImmutableSet());
  }

  @Test
  public void testComputeEthernetHubs() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface i1 = nf.interfaceBuilder().setOwner(c).setType(PHYSICAL).build();
    NodeInterfacePair n1 = NodeInterfacePair.of(i1);
    Interface i2 = nf.interfaceBuilder().setOwner(c).setType(PHYSICAL).build();
    NodeInterfacePair n2 = NodeInterfacePair.of(i2);
    Interface i3 = nf.interfaceBuilder().setOwner(c).setType(PHYSICAL).build();
    NodeInterfacePair n3 = NodeInterfacePair.of(i3);
    {
      // No l1 topology.
      Map<NodeInterfacePair, PhysicalInterface> physical = makePhysicalMap(i1, i2, i3);
      Map<String, EthernetHub> hubs =
          computeEthernetHubs(ImmutableMap.of(c.getHostname(), c), physical, Layer1Topology.EMPTY);
      assertThat(hubs.keySet(), contains(BATFISH_GLOBAL_HUB));
      assertThat(
          getPairs(hubs.get(BATFISH_GLOBAL_HUB).getAttachedInterfacesForTesting().keySet()),
          containsInAnyOrder(n1, n2, n3));
    }
    {
      // One edge.
      Map<String, EthernetHub> hubs =
          computeEthernetHubs(
              ImmutableMap.of(c.getHostname(), c),
              makePhysicalMap(i1, i2, i3),
              new Layer1Topology(
                  ImmutableSet.of(
                      new Layer1Edge(
                          c.getHostname(), i1.getName(), c.getHostname(), i2.getName()))));
      assertThat(hubs, aMapWithSize(2));
      String otherKey =
          Iterables.getOnlyElement(
              Sets.difference(hubs.keySet(), ImmutableSet.of(BATFISH_GLOBAL_HUB)));
      assertThat(
          getPairs(hubs.get(BATFISH_GLOBAL_HUB).getAttachedInterfacesForTesting().keySet()),
          contains(n3));
      assertThat(
          getPairs(hubs.get(otherKey).getAttachedInterfacesForTesting().keySet()),
          containsInAnyOrder(n1, n2));
    }
    {
      // One interface with disconnected edge.
      Map<String, EthernetHub> hubs =
          computeEthernetHubs(
              ImmutableMap.of(c.getHostname(), c),
              makePhysicalMap(i1, i2, i3),
              new Layer1Topology(
                  ImmutableSet.of(
                      new Layer1Edge(
                          c.getHostname(), i1.getName(), "no-such-hostname", "no-such-iface"))));
      assertThat(hubs, aMapWithSize(2));
      String otherKey =
          Iterables.getOnlyElement(
              Sets.difference(hubs.keySet(), ImmutableSet.of(BATFISH_GLOBAL_HUB)));
      assertThat(
          getPairs(hubs.get(BATFISH_GLOBAL_HUB).getAttachedInterfacesForTesting().keySet()),
          containsInAnyOrder(n2, n3));
      assertThat(
          getPairs(hubs.get(otherKey).getAttachedInterfacesForTesting().keySet()), contains(n1));
    }
    {
      // Line.
      Map<String, EthernetHub> hubs =
          computeEthernetHubs(
              ImmutableMap.of(c.getHostname(), c),
              makePhysicalMap(i1, i2, i3),
              new Layer1Topology(
                  ImmutableSet.of(
                      new Layer1Edge(c.getHostname(), i1.getName(), c.getHostname(), i2.getName()),
                      new Layer1Edge(
                          c.getHostname(), i3.getName(), c.getHostname(), i2.getName()))));
      assertThat(hubs, aMapWithSize(1));
      assertThat(
          getPairs(
              Iterables.getOnlyElement(hubs.values()).getAttachedInterfacesForTesting().keySet()),
          containsInAnyOrder(n1, n2, n3));
    }
  }

  private static Map<String, Configuration> simple3InterfaceNetwork() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c1 = nf.configurationBuilder().setHostname("c1").build();
    nf.interfaceBuilder()
        .setOwner(c1)
        .setName("i1")
        .setType(PHYSICAL)
        .setAddress(ConcreteInterfaceAddress.parse("1.2.3.1/24"))
        .build();
    Configuration c2 = nf.configurationBuilder().setHostname("c2").build();
    nf.interfaceBuilder()
        .setOwner(c2)
        .setName("i2")
        .setType(PHYSICAL)
        .setAddress(ConcreteInterfaceAddress.parse("1.2.3.2/24"))
        .build();
    Configuration c3 = nf.configurationBuilder().setHostname("c3").build();
    nf.interfaceBuilder()
        .setOwner(c3)
        .setName("i3")
        .setType(PHYSICAL)
        .setAddress(ConcreteInterfaceAddress.parse("1.2.3.3/24"))
        .build();
    return ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2, c3.getHostname(), c3);
  }

  @Test
  public void testE2e_noL1() {
    Map<String, Configuration> configs = simple3InterfaceNetwork();
    // With no L1 topology, all 3 interfaces in same domain.
    L3AdjacencyComputer l3 =
        new L3AdjacencyComputer(configs, Layer1Topologies.empty(), VxlanTopology.EMPTY);
    Map<NodeInterfacePair, Integer> domains = l3.findAllBroadcastDomains();
    assertThat("all interfaces have a domain", domains, aMapWithSize(3));
    assertThat("all interfaces in same domain", ImmutableSet.copyOf(domains.values()), hasSize(1));
  }

  @Test
  public void testE2e_L1() {
    // With L1 topology, only connected interfaces in same domain.
    Map<String, Configuration> configs = simple3InterfaceNetwork();
    NodeInterfacePair n1 = NodeInterfacePair.of("c1", "i1");
    NodeInterfacePair n2 = NodeInterfacePair.of("c2", "i2");
    NodeInterfacePair n3 = NodeInterfacePair.of("c3", "i3");
    Layer1Topology physical =
        new Layer1Topology(
            new Layer1Edge(
                n1.getHostname(), n1.getInterface(), n3.getHostname(), n3.getInterface()));
    L3AdjacencyComputer l3 =
        new L3AdjacencyComputer(
            configs,
            Layer1TopologiesFactory.create(physical, Layer1Topology.EMPTY, configs),
            VxlanTopology.EMPTY);
    Map<NodeInterfacePair, Integer> domains = l3.findAllBroadcastDomains();
    assertThat("all interfaces have a domain", domains, aMapWithSize(3));
    assertThat("connected ifaces in same domain", domains.get(n1), equalTo(domains.get(n3)));
    assertThat(
        "global hub not connected to other interfaces",
        domains.get(n2),
        not(equalTo(domains.get(n1))));
  }

  @Test
  public void testE2e_disconnected() {
    Map<String, Configuration> configs = simple3InterfaceNetwork();
    NodeInterfacePair n1 = NodeInterfacePair.of("c1", "i1");
    NodeInterfacePair n2 = NodeInterfacePair.of("c2", "i2");
    NodeInterfacePair n3 = NodeInterfacePair.of("c3", "i3");
    // With L1 topology to disconnected interface, only global hub connected.
    Layer1Topology physical =
        new Layer1Topology(
            new Layer1Edge(n1.getHostname(), n1.getInterface(), "no-such-host", "no-such-iface"));
    L3AdjacencyComputer l3 =
        new L3AdjacencyComputer(
            configs,
            Layer1TopologiesFactory.create(physical, Layer1Topology.EMPTY, configs),
            VxlanTopology.EMPTY);
    Map<NodeInterfacePair, Integer> domains = l3.findAllBroadcastDomains();
    assertThat("all interfaces have a domain", domains, aMapWithSize(3));
    assertThat("global hub ifaces in same domain", domains.get(n2), equalTo(domains.get(n3)));
    assertThat(
        "n1 is disconnected, not connected to other interfaces",
        domains.get(n1),
        not(equalTo(domains.get(n2))));
  }

  @Test
  public void testE2e_encapsulation() {
    // Encapsulation vlan honored, even with no L1.
    Map<String, Configuration> configs = simple3InterfaceNetwork();
    NodeInterfacePair n1 = NodeInterfacePair.of("c1", "i1");
    NodeInterfacePair n2 = NodeInterfacePair.of("c2", "i2");
    NodeInterfacePair n3 = NodeInterfacePair.of("c3", "i3");
    configs.get(n1.getHostname()).getAllInterfaces().get(n1.getInterface()).setEncapsulationVlan(4);
    L3AdjacencyComputer l3 =
        new L3AdjacencyComputer(configs, Layer1Topologies.empty(), VxlanTopology.EMPTY);
    Map<NodeInterfacePair, Integer> domains = l3.findAllBroadcastDomains();
    assertThat("all interfaces have a domain", domains, aMapWithSize(3));
    assertThat(
        "unencapsulated interface in same domain", domains.get(n2), equalTo(domains.get(n3)));
    assertThat(
        "encapsulated interface not connected to unencapsulated interfaces",
        domains.get(n2),
        not(equalTo(domains.get(n1))));
  }

  @Test
  public void testE2e_vxlan() {
    // Topology:
    // h11 <=>           <=> h21
    //     vlan10     vlan10
    //         r1 <=> r2
    //     vlan20     vlan20
    // h12 <=>           <=> h22
    //
    // r1 and r2 only have underlay and vxlan tunnel between them - no vlans
    // vlans 10 and 20 should be bridged across vxlan so that:
    // - h11 and h21 are in the same broadcast domain
    // - h12 and h22 are in the same broadcast domain
    // vlan 10 corresponds to VNI 10010
    // vlan 20 corresponds to VNI 10020
    Configuration h11 =
        Configuration.builder().setHostname("h11").setConfigurationFormat(CISCO_IOS).build();
    Vrf h11Vrf = Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(h11).build();
    Interface h11i =
        TestInterface.builder()
            .setName("h11i")
            .setAddress(ConcreteInterfaceAddress.parse("10.0.10.1/24"))
            .setType(PHYSICAL)
            .setVrf(h11Vrf)
            .setOwner(h11)
            .build();

    Configuration h12 =
        Configuration.builder().setHostname("h12").setConfigurationFormat(CISCO_IOS).build();
    Vrf h12Vrf = Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(h12).build();
    Interface h12i =
        TestInterface.builder()
            .setName("h12i")
            .setAddress(ConcreteInterfaceAddress.parse("10.0.20.1/24"))
            .setType(PHYSICAL)
            .setVrf(h12Vrf)
            .setOwner(h12)
            .build();

    Configuration h21 =
        Configuration.builder().setHostname("h21").setConfigurationFormat(CISCO_IOS).build();
    Vrf h21Vrf = Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(h21).build();
    Interface h21i =
        TestInterface.builder()
            .setName("h21i")
            .setAddress(ConcreteInterfaceAddress.parse("10.0.10.2/24"))
            .setType(PHYSICAL)
            .setVrf(h21Vrf)
            .setOwner(h21)
            .build();

    Configuration h22 =
        Configuration.builder().setHostname("h22").setConfigurationFormat(CISCO_IOS).build();
    Vrf h22Vrf = Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(h22).build();
    Interface h22i =
        TestInterface.builder()
            .setName("h22i")
            .setAddress(ConcreteInterfaceAddress.parse("10.0.20.2/24"))
            .setType(PHYSICAL)
            .setVrf(h22Vrf)
            .setOwner(h22)
            .build();

    Configuration r1 =
        Configuration.builder().setHostname("r1").setConfigurationFormat(CISCO_IOS).build();
    Vrf r1Vrf = Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(r1).build();
    Interface r1h11 =
        TestInterface.builder()
            .setName("r1h11")
            .setSwitchport(true)
            .setSwitchportMode(SwitchportMode.ACCESS)
            .setAccessVlan(10)
            .setType(PHYSICAL)
            .setVrf(r1Vrf)
            .setOwner(r1)
            .build();
    Interface r1h12 =
        TestInterface.builder()
            .setName("r1h12")
            .setSwitchport(true)
            .setSwitchportMode(SwitchportMode.ACCESS)
            .setAccessVlan(20)
            .setType(PHYSICAL)
            .setVrf(r1Vrf)
            .setOwner(r1)
            .build();
    Interface r1r2 =
        TestInterface.builder()
            .setName("r1r2")
            .setAddress(ConcreteInterfaceAddress.parse("10.0.0.1/24"))
            .setType(PHYSICAL)
            .setVrf(r1Vrf)
            .setOwner(r1)
            .build();
    r1Vrf.addLayer2Vni(
        Layer2Vni.builder()
            .setVni(10010)
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSrcVrf(DEFAULT_VRF_NAME)
            .setVlan(10)
            .setSourceAddress(Ip.parse("10.0.0.1"))
            .setBumTransportIps(ImmutableSet.of(Ip.parse("10.0.0.2")))
            .build());
    r1Vrf.addLayer2Vni(
        Layer2Vni.builder()
            .setVni(10020)
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSrcVrf(DEFAULT_VRF_NAME)
            .setVlan(20)
            .setSourceAddress(Ip.parse("10.0.0.1"))
            .setBumTransportIps(ImmutableSet.of(Ip.parse("10.0.0.2")))
            .build());

    Configuration r2 =
        Configuration.builder().setHostname("r2").setConfigurationFormat(CISCO_IOS).build();
    Vrf r2Vrf = Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(r2).build();
    Interface r2h21 =
        TestInterface.builder()
            .setName("r2h21")
            .setSwitchport(true)
            .setSwitchportMode(SwitchportMode.ACCESS)
            .setAccessVlan(10)
            .setType(PHYSICAL)
            .setVrf(r2Vrf)
            .setOwner(r2)
            .build();
    Interface r2h22 =
        TestInterface.builder()
            .setName("r2h22")
            .setSwitchport(true)
            .setSwitchportMode(SwitchportMode.ACCESS)
            .setAccessVlan(20)
            .setType(PHYSICAL)
            .setVrf(r2Vrf)
            .setOwner(r2)
            .build();
    Interface r2r1 =
        TestInterface.builder()
            .setName("r2r1")
            .setAddress(ConcreteInterfaceAddress.parse("10.0.0.2/24"))
            .setType(PHYSICAL)
            .setVrf(r2Vrf)
            .setOwner(r2)
            .build();
    r2Vrf.addLayer2Vni(
        Layer2Vni.builder()
            .setVni(10010)
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSrcVrf(DEFAULT_VRF_NAME)
            .setVlan(10)
            .setSourceAddress(Ip.parse("10.0.0.2"))
            .setBumTransportIps(ImmutableSet.of(Ip.parse("10.0.0.1")))
            .build());
    r2Vrf.addLayer2Vni(
        Layer2Vni.builder()
            .setVni(10020)
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSrcVrf(DEFAULT_VRF_NAME)
            .setVlan(20)
            .setSourceAddress(Ip.parse("10.0.0.2"))
            .setBumTransportIps(ImmutableSet.of(Ip.parse("10.0.0.1")))
            .build());

    Map<String, Configuration> configurations =
        ImmutableMap.of(
            h11.getHostname(), h11,
            h12.getHostname(), h12,
            h21.getHostname(), h21,
            h22.getHostname(), h22,
            r1.getHostname(), r1,
            r2.getHostname(), r2);
    Set<Layer1Edge> l1EdgesOneSided =
        ImmutableSet.of(
            new Layer1Edge(h11.getHostname(), h11i.getName(), r1.getHostname(), r1h11.getName()),
            new Layer1Edge(h12.getHostname(), h12i.getName(), r1.getHostname(), r1h12.getName()),
            new Layer1Edge(r1.getHostname(), r1r2.getName(), r2.getHostname(), r2r1.getName()),
            new Layer1Edge(h21.getHostname(), h21i.getName(), r2.getHostname(), r2h21.getName()),
            new Layer1Edge(h22.getHostname(), h22i.getName(), r2.getHostname(), r2h22.getName()));
    Set<Layer1Edge> l1Edges =
        l1EdgesOneSided.stream()
            .flatMap(e -> Stream.of(e, e.reverse()))
            .collect(ImmutableSet.toImmutableSet());
    Layer1Topology l1 = new Layer1Topology(l1Edges);
    Layer1Topologies layer1Topologies =
        Layer1TopologiesFactory.create(l1, Layer1Topology.EMPTY, configurations);
    VxlanTopology vxlanTopology = VxlanTopologyUtils.computeInitialVxlanTopology(configurations);
    L3AdjacencyComputer l3 =
        new L3AdjacencyComputer(configurations, layer1Topologies, vxlanTopology);
    Map<NodeInterfacePair, Integer> bds = l3.findAllBroadcastDomains();

    assertThat(bds.get(NodeInterfacePair.of(h11i)), equalTo(bds.get(NodeInterfacePair.of(h21i))));
    assertThat(bds.get(NodeInterfacePair.of(h12i)), equalTo(bds.get(NodeInterfacePair.of(h22i))));
    assertThat(
        bds.get(NodeInterfacePair.of(h11i)), not(equalTo(bds.get(NodeInterfacePair.of(h22i)))));
  }
}
