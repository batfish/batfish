package org.batfish.datamodel.interface_dependencies;

import static org.batfish.common.topology.Layer1Topologies.INVALID_INTERFACE;
import static org.batfish.datamodel.InactiveReason.AGGREGATE_NEIGHBOR_DOWN;
import static org.batfish.datamodel.InactiveReason.BIND_DOWN;
import static org.batfish.datamodel.InactiveReason.LACP_FAILURE;
import static org.batfish.datamodel.InactiveReason.NO_ACTIVE_MEMBERS;
import static org.batfish.datamodel.InactiveReason.PARENT_DOWN;
import static org.batfish.datamodel.InactiveReason.PHYSICAL_NEIGHBOR_DOWN;
import static org.batfish.datamodel.Interface.DependencyType.AGGREGATE;
import static org.batfish.datamodel.Interface.DependencyType.BIND;
import static org.batfish.datamodel.interface_dependency.InterfaceDependencies.getInterfacesToDeactivate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.topology.Layer1Node;
import org.batfish.common.topology.Layer1Topologies;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

public class InterfaceDependenciesTest {
  Layer1Topologies layer1Topologies(Layer1Topology canonicalUserL1, Layer1Topology logicalL1) {
    return new Layer1Topologies(
        canonicalUserL1, Layer1Topology.EMPTY, logicalL1, Layer1Topology.EMPTY);
  }

  Layer1Node l1Node(Interface i1) {
    return new Layer1Node(i1.getOwner().getHostname(), i1.getName());
  }

  Layer1Edge l1Edge(Interface i1, Interface i2) {
    return new Layer1Edge(l1Node(i1), l1Node(i2));
  }

  @Test
  public void testGetInterfacesToDeactivate_p2p() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration n1 = cb.setHostname("n1").build();
    Vrf v1 = nf.vrfBuilder().setOwner(n1).build();
    Interface i1 =
        nf.interfaceBuilder().setType(InterfaceType.PHYSICAL).setOwner(n1).setVrf(v1).build();

    Configuration n2 = cb.setHostname("n2").build();
    Vrf v2 = nf.vrfBuilder().setOwner(n2).build();
    Interface i2 =
        nf.interfaceBuilder().setType(InterfaceType.PHYSICAL).setOwner(n2).setVrf(v2).build();

    Map<String, Configuration> configs =
        ImmutableMap.of(n1.getHostname(), n1, n2.getHostname(), n2);
    Layer1Topologies layer1Topologies =
        layer1Topologies(new Layer1Topology(l1Edge(i1, i2)), Layer1Topology.EMPTY);

    assertThat(getInterfacesToDeactivate(configs, layer1Topologies), anEmptyMap());

    i2.adminDown();
    // i2 is inactive and i1 and i2 are in the same p2p broadcast domain, so deactivate i1.
    assertThat(
        getInterfacesToDeactivate(configs, layer1Topologies),
        equalTo(ImmutableMap.of(NodeInterfacePair.of(i1), PHYSICAL_NEIGHBOR_DOWN)));
  }

  @Test
  public void testGetInterfacesToDeactivate_aggregate() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration n1 = cb.setHostname("n1").build();
    Vrf v1 = nf.vrfBuilder().setOwner(n1).build();

    Interface i1 =
        nf.interfaceBuilder().setType(InterfaceType.PHYSICAL).setOwner(n1).setVrf(v1).build();
    Interface i2 =
        nf.interfaceBuilder().setType(InterfaceType.PHYSICAL).setOwner(n1).setVrf(v1).build();
    Interface i3 =
        nf.interfaceBuilder()
            .setType(InterfaceType.AGGREGATED)
            .setOwner(n1)
            .setVrf(v1)
            .setDependencies(
                ImmutableList.of(
                    new Dependency(i1.getName(), AGGREGATE),
                    new Dependency(i2.getName(), AGGREGATE)))
            .build();

    Map<String, Configuration> configs = ImmutableMap.of(n1.getHostname(), n1);

    i1.adminDown();
    // only i1 is inactive, so i3 is up
    assertThat(getInterfacesToDeactivate(configs, Layer1Topologies.empty()), anEmptyMap());

    i2.adminDown();
    // i1 and i2 are inactive, so i3 is down
    assertThat(
        getInterfacesToDeactivate(configs, Layer1Topologies.empty()),
        equalTo(ImmutableMap.of(NodeInterfacePair.of(i3), NO_ACTIVE_MEMBERS)));
  }

  @Test
  public void testGetInterfacesToDeactivate_bind() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration n1 = cb.setHostname("n1").build();
    Vrf v1 = nf.vrfBuilder().setOwner(n1).build();

    // i2 has a local BIND dependency on i1, e.g. i2 is a subinterface of i1
    Interface i1 =
        nf.interfaceBuilder().setType(InterfaceType.PHYSICAL).setOwner(n1).setVrf(v1).build();
    Interface i2 =
        nf.interfaceBuilder()
            .setType(InterfaceType.LOGICAL)
            .setOwner(n1)
            .setVrf(v1)
            .setDependencies(ImmutableList.of(new Dependency(i1.getName(), BIND)))
            .build();

    Map<String, Configuration> configs = ImmutableMap.of(n1.getHostname(), n1);

    // only i1 is active, so i2 is up
    assertThat(getInterfacesToDeactivate(configs, Layer1Topologies.empty()), anEmptyMap());

    i1.adminDown();
    // i1 and i2 are inactive, so i3 is down
    assertThat(
        getInterfacesToDeactivate(configs, Layer1Topologies.empty()),
        equalTo(ImmutableMap.of(NodeInterfacePair.of(i2), PARENT_DOWN)));
  }

  @Test
  public void testGetInterfacesToDeactivate_missing_bind_physical_unknown() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration n1 = cb.setHostname("n1").build();
    Vrf v1 = nf.vrfBuilder().setOwner(n1).build();

    // i2 has a BIND dependency on i1 and on a missing interface
    Interface i1 = nf.interfaceBuilder().setOwner(n1).setVrf(v1).build();
    Interface i2 =
        nf.interfaceBuilder()
            .setOwner(n1)
            .setType(InterfaceType.PHYSICAL)
            .setVrf(v1)
            .setDependencies(
                ImmutableList.of(
                    new Dependency(i1.getName(), BIND), new Dependency("missing", BIND)))
            .build();
    Interface i3 =
        nf.interfaceBuilder()
            .setOwner(n1)
            .setType(InterfaceType.UNKNOWN)
            .setVrf(v1)
            .setDependencies(
                ImmutableList.of(
                    new Dependency(i1.getName(), BIND), new Dependency("missing", BIND)))
            .build();

    Map<String, Configuration> configs = ImmutableMap.of(n1.getHostname(), n1);
    Layer1Topologies layer1Topologies =
        layer1Topologies(Layer1Topology.EMPTY, Layer1Topology.EMPTY);

    // i2 and i3 are down due to missing BIND dependency
    assertThat(
        getInterfacesToDeactivate(configs, layer1Topologies),
        equalTo(
            ImmutableMap.of(
                NodeInterfacePair.of(i2),
                PHYSICAL_NEIGHBOR_DOWN,
                NodeInterfacePair.of(i3),
                PHYSICAL_NEIGHBOR_DOWN)));
  }

  @Test
  public void testGetInterfacesToDeactivate_missing_bind_aggregated_redundant() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration n1 = cb.setHostname("n1").build();
    Vrf v1 = nf.vrfBuilder().setOwner(n1).build();

    // i2 has a BIND dependency on i1 and on a missing interface
    Interface i1 = nf.interfaceBuilder().setOwner(n1).setVrf(v1).build();
    Interface i2 =
        nf.interfaceBuilder()
            .setOwner(n1)
            .setVrf(v1)
            .setType(InterfaceType.AGGREGATED)
            .setDependencies(
                ImmutableList.of(
                    new Dependency(i1.getName(), BIND),
                    new Dependency("missing", BIND),
                    new Dependency(i1.getName(), AGGREGATE)))
            .build();
    Interface i3 =
        nf.interfaceBuilder()
            .setOwner(n1)
            .setVrf(v1)
            .setType(InterfaceType.REDUNDANT)
            .setDependencies(
                ImmutableList.of(
                    new Dependency(i1.getName(), BIND),
                    new Dependency("missing", BIND),
                    new Dependency(i1.getName(), AGGREGATE)))
            .build();

    Map<String, Configuration> configs = ImmutableMap.of(n1.getHostname(), n1);
    Layer1Topologies layer1Topologies =
        layer1Topologies(Layer1Topology.EMPTY, Layer1Topology.EMPTY);

    // i2 and i3 are down due to missing BIND dependency
    assertThat(
        getInterfacesToDeactivate(configs, layer1Topologies),
        equalTo(
            ImmutableMap.of(
                NodeInterfacePair.of(i2),
                AGGREGATE_NEIGHBOR_DOWN,
                NodeInterfacePair.of(i3),
                AGGREGATE_NEIGHBOR_DOWN)));
  }

  @Test
  public void testGetInterfacesToDeactivate_missing_bind_tunnel() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration n1 = cb.setHostname("n1").build();
    Vrf v1 = nf.vrfBuilder().setOwner(n1).build();

    // i2 has a BIND dependency on i1 and on a missing interface
    Interface i1 = nf.interfaceBuilder().setOwner(n1).setVrf(v1).build();
    Interface i2 =
        nf.interfaceBuilder()
            .setOwner(n1)
            .setVrf(v1)
            .setType(InterfaceType.TUNNEL)
            .setDependencies(
                ImmutableList.of(
                    new Dependency(i1.getName(), BIND), new Dependency("missing", BIND)))
            .build();

    Map<String, Configuration> configs = ImmutableMap.of(n1.getHostname(), n1);
    Layer1Topologies layer1Topologies =
        layer1Topologies(Layer1Topology.EMPTY, Layer1Topology.EMPTY);

    // i2 and i3 are down due to missing BIND dependency
    assertThat(
        getInterfacesToDeactivate(configs, layer1Topologies),
        equalTo(ImmutableMap.of(NodeInterfacePair.of(i2), BIND_DOWN)));
  }

  @Test
  public void testGetInterfacesToDeactivate_missing_aggregate() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration n1 = cb.setHostname("n1").build();
    Vrf v1 = nf.vrfBuilder().setOwner(n1).build();

    // i2 has a BIND dependency on i1 and on a missing interface
    Interface i1 =
        nf.interfaceBuilder()
            .setOwner(n1)
            .setVrf(v1)
            .setDependencies(
                ImmutableList.of(
                    new Dependency("missing1", AGGREGATE), new Dependency("missing2", AGGREGATE)))
            .build();

    Map<String, Configuration> configs = ImmutableMap.of(n1.getHostname(), n1);

    // both AGGREGATE deps are missing, so deactivate
    assertThat(
        getInterfacesToDeactivate(configs, Layer1Topologies.empty()),
        equalTo(ImmutableMap.of(NodeInterfacePair.of(i1), NO_ACTIVE_MEMBERS)));

    nf.interfaceBuilder().setOwner(n1).setVrf(v1).setName("missing1").build();
    // one of the AGGREGATE deps is present (and active), so don't deactivate
    assertThat(getInterfacesToDeactivate(configs, Layer1Topologies.empty()), anEmptyMap());
  }

  /**
   * Nodes N1 and N2 are connected via two physical edges forming a port-channel. If one of the
   * physical interfaces on each edge is inactive, all of them will be deactivated, as will the
   * port-channels themselves, and any subinterfaces of the port-channels.
   */
  @Test
  public void testGetInterfacesToDeactivate_transitive1() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration n1 = cb.setHostname("n1").build();
    Vrf v1 = nf.vrfBuilder().setOwner(n1).build();
    // member interfaces
    Interface m1 =
        nf.interfaceBuilder()
            .setType(InterfaceType.PHYSICAL)
            .setOwner(n1)
            .setVrf(v1)
            .setName("m1")
            .build();
    Interface m3 =
        nf.interfaceBuilder()
            .setType(InterfaceType.PHYSICAL)
            .setOwner(n1)
            .setVrf(v1)
            .setName("m3")
            .build();
    // port-channel interface
    Interface pc1 =
        nf.interfaceBuilder()
            .setType(InterfaceType.AGGREGATED)
            .setOwner(n1)
            .setVrf(v1)
            .setName("pc1")
            .setDependencies(
                ImmutableList.of(
                    new Dependency(m1.getName(), AGGREGATE),
                    new Dependency(m3.getName(), AGGREGATE)))
            .build();
    // port-channel subinterface
    Interface pc1_1 =
        nf.interfaceBuilder()
            .setType(InterfaceType.AGGREGATE_CHILD)
            .setOwner(n1)
            .setVrf(v1)
            .setName("pc1.1")
            .setDependencies(ImmutableList.of(new Dependency(pc1.getName(), BIND)))
            .build();

    Configuration n2 = cb.setHostname("n2").build();
    Vrf v2 = nf.vrfBuilder().setOwner(n2).build();
    // member interfaces
    Interface m2 =
        nf.interfaceBuilder()
            .setType(InterfaceType.PHYSICAL)
            .setOwner(n2)
            .setVrf(v2)
            .setName("m2")
            .build();
    Interface m4 =
        nf.interfaceBuilder()
            .setType(InterfaceType.PHYSICAL)
            .setOwner(n2)
            .setVrf(v2)
            .setName("m4")
            .build();
    // port-channel interface
    Interface pc2 =
        nf.interfaceBuilder()
            .setType(InterfaceType.AGGREGATED)
            .setOwner(n2)
            .setVrf(v2)
            .setName("pc2")
            .setDependencies(
                ImmutableList.of(
                    new Dependency(m2.getName(), AGGREGATE),
                    new Dependency(m4.getName(), AGGREGATE)))
            .build();
    // port-channel subinterface
    Interface pc2_1 =
        nf.interfaceBuilder()
            .setType(InterfaceType.AGGREGATE_CHILD)
            .setOwner(n2)
            .setVrf(v2)
            .setName("pc2.1")
            .setDependencies(ImmutableList.of(new Dependency(pc2.getName(), BIND)))
            .build();

    Map<String, Configuration> configs =
        ImmutableMap.of(n1.getHostname(), n1, n2.getHostname(), n2);
    Layer1Topologies layer1Topologies =
        layer1Topologies(
            new Layer1Topology(l1Edge(m1, m2), l1Edge(m3, m4)),
            new Layer1Topology(l1Edge(pc1, pc2)));

    // m1 is inactive, so m2 becomes inactive too
    m1.adminDown();
    assertThat(
        getInterfacesToDeactivate(configs, layer1Topologies),
        equalTo(ImmutableMap.of(NodeInterfacePair.of(m2), PHYSICAL_NEIGHBOR_DOWN)));

    // m4 is inactive so m3 becomes inactive too. that brings down the port-channels and their
    // subinterfaces
    m4.adminDown();
    assertThat(
        getInterfacesToDeactivate(configs, layer1Topologies),
        equalTo(
            ImmutableMap.of(
                NodeInterfacePair.of(m2), PHYSICAL_NEIGHBOR_DOWN,
                NodeInterfacePair.of(m3), PHYSICAL_NEIGHBOR_DOWN,
                NodeInterfacePair.of(pc1), NO_ACTIVE_MEMBERS,
                NodeInterfacePair.of(pc1_1), PARENT_DOWN,
                NodeInterfacePair.of(pc2), NO_ACTIVE_MEMBERS,
                NodeInterfacePair.of(pc2_1), PARENT_DOWN)));

    // now member interfaces are active but one of the port-channels is inactive.
    // deactivate the other port-channel and both subinterfaces
    m1.activateForTest();
    m4.activateForTest();
    pc1.adminDown();
    assertThat(
        getInterfacesToDeactivate(configs, layer1Topologies),
        equalTo(
            ImmutableMap.of(
                NodeInterfacePair.of(pc1_1),
                PARENT_DOWN,
                NodeInterfacePair.of(pc2),
                AGGREGATE_NEIGHBOR_DOWN,
                NodeInterfacePair.of(pc2_1),
                PARENT_DOWN)));
  }

  /**
   * If an LACP interface should have a neighbor (because at least one of its members does) but
   * doesn't, it should be deactivated.
   * https://github.com/intentionet/batfish-enterprise/issues/6157
   */
  @Test
  public void testGetInterfacesToDeactivate_LACP_with_missing_neighbor() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration n1 = cb.setHostname("n1").build();
    Vrf v1 = nf.vrfBuilder().setOwner(n1).build();
    // member interface
    Interface m1 =
        nf.interfaceBuilder()
            .setType(InterfaceType.PHYSICAL)
            .setOwner(n1)
            .setVrf(v1)
            .setName("m1")
            .build();
    // port-channel interface
    Interface pc1 =
        nf.interfaceBuilder()
            .setType(InterfaceType.AGGREGATED)
            .setOwner(n1)
            .setVrf(v1)
            .setName("pc1")
            .setDependencies(ImmutableList.of(new Dependency(m1.getName(), AGGREGATE)))
            .build();

    Configuration n2 = cb.setHostname("n2").build();
    Vrf v2 = nf.vrfBuilder().setOwner(n2).build();
    // neighbor of the member interface
    Interface m2 =
        nf.interfaceBuilder()
            .setType(InterfaceType.PHYSICAL)
            .setOwner(n2)
            .setVrf(v2)
            .setName("m2")
            .build();

    Map<String, Configuration> configs =
        ImmutableMap.of(n1.getHostname(), n1, n2.getHostname(), n2);
    Layer1Topologies layer1Topologies =
        layer1Topologies(new Layer1Topology(l1Edge(m1, m2)), Layer1Topology.EMPTY);

    // pc1 deactivated because it has no neighbor (and it should)
    assertThat(
        getInterfacesToDeactivate(configs, layer1Topologies),
        equalTo(ImmutableMap.of(NodeInterfacePair.of(pc1), LACP_FAILURE)));
  }

  /**
   * The logical L1 topology will assign physical nodes as neighbors of an LACP interface. We should
   * exclude those when looking for an unambiguous neighbor.
   */
  @Test
  public void testGetInterfacesToDeactivate_LACP_PhysicalNeighborsInLogicalL1() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration n1 = cb.setHostname("n1").build();
    Vrf v1 = nf.vrfBuilder().setOwner(n1).build();
    // member interface
    Interface m1 =
        nf.interfaceBuilder()
            .setType(InterfaceType.PHYSICAL)
            .setOwner(n1)
            .setVrf(v1)
            .setName("m1")
            .build();
    // port-channel interface
    Interface pc1 =
        nf.interfaceBuilder()
            .setType(InterfaceType.AGGREGATED)
            .setOwner(n1)
            .setVrf(v1)
            .setName("pc1")
            .setDependencies(ImmutableList.of(new Dependency(m1.getName(), AGGREGATE)))
            .build();

    Configuration n2 = cb.setHostname("n2").build();
    Vrf v2 = nf.vrfBuilder().setOwner(n2).build();
    // neighbor of the member interface
    Interface m2 =
        nf.interfaceBuilder()
            .setType(InterfaceType.PHYSICAL)
            .setOwner(n2)
            .setVrf(v2)
            .setName("m2")
            .build();
    // port-channel interface
    Interface pc2 =
        nf.interfaceBuilder()
            .setType(InterfaceType.AGGREGATED)
            .setOwner(n2)
            .setVrf(v2)
            .setName("pc2")
            .setDependencies(ImmutableList.of(new Dependency(m2.getName(), AGGREGATE)))
            .build();

    Map<String, Configuration> configs =
        ImmutableMap.of(n1.getHostname(), n1, n2.getHostname(), n2);
    Layer1Topologies layer1Topologies =
        layer1Topologies(
            new Layer1Topology(l1Edge(m1, m2)),
            new Layer1Topology(l1Edge(m1, m2), l1Edge(m1, pc2), l1Edge(pc1, m2), l1Edge(pc1, pc2)));

    assertThat(getInterfacesToDeactivate(configs, layer1Topologies), anEmptyMap());
  }

  /** An LACP interface without a neighbor can be active if it's on the network boundary. */
  @Test
  public void testGetInterfacesToDeactivate_LACP_on_network_boundary() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration n1 = cb.setHostname("n1").build();
    Vrf v1 = nf.vrfBuilder().setOwner(n1).build();
    // member interface
    Interface m1 =
        nf.interfaceBuilder()
            .setType(InterfaceType.PHYSICAL)
            .setOwner(n1)
            .setVrf(v1)
            .setName("m1")
            .build();
    // port-channel interface
    nf.interfaceBuilder()
        .setType(InterfaceType.AGGREGATED)
        .setOwner(n1)
        .setVrf(v1)
        .setName("pc1")
        .setDependencies(ImmutableList.of(new Dependency(m1.getName(), AGGREGATE)))
        .build();

    Map<String, Configuration> configs = ImmutableMap.of(n1.getHostname(), n1);
    Layer1Topologies layer1Topologies = Layer1Topologies.empty();

    // pc1 is still active because it's on the network boundary
    assertThat(getInterfacesToDeactivate(configs, layer1Topologies), anEmptyMap());
  }

  /**
   * Our VI modeling and dependency tracking do not handle virtual portchannels. Skip portchannels
   * that look like VPCs.
   */
  @Test
  public void testGetInterfacesToDeactivate_VirtualPortChannel() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    // n1
    Configuration n1 = cb.setHostname("n1").build();
    Vrf v1 = nf.vrfBuilder().setOwner(n1).build();
    // member interface to n2
    Interface n1i1 =
        nf.interfaceBuilder()
            .setType(InterfaceType.PHYSICAL)
            .setOwner(n1)
            .setVrf(v1)
            .setName("i1")
            .build();
    // member interface to n3
    Interface n1i2 =
        nf.interfaceBuilder()
            .setType(InterfaceType.PHYSICAL)
            .setOwner(n1)
            .setVrf(v1)
            .setName("i2")
            .build();
    // port-channel interface
    Interface n1pc =
        nf.interfaceBuilder()
            .setType(InterfaceType.AGGREGATED)
            .setOwner(n1)
            .setVrf(v1)
            .setName("pc")
            .setDependencies(
                ImmutableList.of(
                    new Dependency(n1i1.getName(), AGGREGATE),
                    new Dependency(n1i2.getName(), AGGREGATE)))
            .build();

    // n2
    Configuration n2 = cb.setHostname("n2").build();
    Vrf v2 = nf.vrfBuilder().setOwner(n1).build();
    // member interface to n1.i1
    Interface n2i1 =
        nf.interfaceBuilder()
            .setType(InterfaceType.PHYSICAL)
            .setOwner(n2)
            .setVrf(v2)
            .setName("i1")
            .build();
    // port-channel interface
    Interface n2pc =
        nf.interfaceBuilder()
            .setType(InterfaceType.AGGREGATED)
            .setOwner(n2)
            .setVrf(v2)
            .setName("pc")
            .setDependencies(ImmutableList.of(new Dependency(n2i1.getName(), AGGREGATE)))
            .build();

    // n3
    Configuration n3 = cb.setHostname("n3").build();
    Vrf v3 = nf.vrfBuilder().setOwner(n1).build();
    // member interface to n1.i2
    Interface n3i1 =
        nf.interfaceBuilder()
            .setType(InterfaceType.PHYSICAL)
            .setOwner(n3)
            .setVrf(v3)
            .setName("i1")
            .build();
    // port-channel interface
    Interface n3pc =
        nf.interfaceBuilder()
            .setType(InterfaceType.AGGREGATED)
            .setOwner(n3)
            .setVrf(v3)
            .setName("pc")
            .setDependencies(ImmutableList.of(new Dependency(n3i1.getName(), AGGREGATE)))
            .build();

    Map<String, Configuration> configs =
        ImmutableMap.of(n1.getHostname(), n1, n2.getHostname(), n2, n3.getHostname(), n3);
    Layer1Topologies layer1Topologies =
        layer1Topologies(
            new Layer1Topology(l1Edge(n1i1, n2i1), l1Edge(n1i2, n3i1)),
            new Layer1Topology(l1Edge(n1pc, n2pc), l1Edge(n1pc, n3pc)));

    // nothing is deactivated, even though n1pc has an ambiguous neighbor, since it looks like a VPC
    assertThat(getInterfacesToDeactivate(configs, layer1Topologies), anEmptyMap());
  }

  /** Test robustness to {@link Layer1Topologies#INVALID_INTERFACE} in the l1 topology. */
  @Test
  public void testGetInterfacesToDeactivate_InvalidInterface() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration n1 = cb.setHostname("n1").build();
    Vrf v1 = nf.vrfBuilder().setOwner(n1).build();
    Interface i1 =
        nf.interfaceBuilder()
            .setType(InterfaceType.PHYSICAL)
            .setOwner(n1)
            .setVrf(v1)
            .setName("i1")
            .build();

    Map<String, Configuration> configs = ImmutableMap.of(n1.getHostname(), n1);
    Layer1Topologies layer1Topologies =
        layer1Topologies(
            new Layer1Topology(new Layer1Edge(l1Node(i1), INVALID_INTERFACE)),
            Layer1Topology.EMPTY);

    // pc1 is still active because it's on the network boundary
    assertThat(getInterfacesToDeactivate(configs, layer1Topologies), anEmptyMap());
  }
}
