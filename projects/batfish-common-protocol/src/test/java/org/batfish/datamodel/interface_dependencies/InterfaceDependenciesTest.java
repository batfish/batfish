package org.batfish.datamodel.interface_dependencies;

import static org.batfish.datamodel.Interface.DependencyType.AGGREGATE;
import static org.batfish.datamodel.Interface.DependencyType.BIND;
import static org.batfish.datamodel.interface_dependency.InterfaceDependencies.getInterfacesToDeactivate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.common.topology.L3Adjacencies;
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
  private static final class MockL3Adjacencies implements L3Adjacencies {
    BiMap<NodeInterfacePair, NodeInterfacePair> _inSameBroadcastDomain;

    MockL3Adjacencies(BiMap<NodeInterfacePair, NodeInterfacePair> inSameBroadcastDomain) {
      _inSameBroadcastDomain = inSameBroadcastDomain;
    }

    @Override
    public boolean inSameBroadcastDomain(NodeInterfacePair i1, NodeInterfacePair i2) {
      throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Optional<NodeInterfacePair> pairedPointToPointL3Interface(NodeInterfacePair iface) {
      NodeInterfacePair pairedIface = _inSameBroadcastDomain.get(iface);
      if (pairedIface == null) {
        pairedIface = _inSameBroadcastDomain.inverse().get(iface);
      }
      return Optional.ofNullable(pairedIface);
    }
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
    L3Adjacencies l3Adjacencies =
        new MockL3Adjacencies(
            ImmutableBiMap.of(NodeInterfacePair.of(i1), NodeInterfacePair.of(i2)));

    // i2 is inactive and i1 and i2 are in the same p2p broadcast domain, so deactivate i1.
    assertThat(getInterfacesToDeactivate(configs, l3Adjacencies), empty());

    i2.setActive(false);
    // i2 is inactive and i1 and i2 are in the same p2p broadcast domain, so deactivate i1.
    assertThat(
        getInterfacesToDeactivate(configs, l3Adjacencies), contains(NodeInterfacePair.of(i1)));
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
    L3Adjacencies l3Adjacencies = new MockL3Adjacencies(ImmutableBiMap.of());

    i1.setActive(false);
    // only i1 is inactive, so i3 is up
    assertThat(getInterfacesToDeactivate(configs, l3Adjacencies), empty());

    i2.setActive(false);
    // i1 and i2 are inactive, so i3 is down
    assertThat(
        getInterfacesToDeactivate(configs, l3Adjacencies), contains(NodeInterfacePair.of(i3)));
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
    L3Adjacencies l3Adjacencies = new MockL3Adjacencies(ImmutableBiMap.of());

    // only i1 is active, so i2 is up
    assertThat(getInterfacesToDeactivate(configs, l3Adjacencies), empty());

    i1.setActive(false);
    // i1 and i2 are inactive, so i3 is down
    assertThat(
        getInterfacesToDeactivate(configs, l3Adjacencies), contains(NodeInterfacePair.of(i2)));
  }

  @Test
  public void testGetInterfacesToDeactivate_missing_bind() {
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
            .setDependencies(
                ImmutableList.of(
                    new Dependency(i1.getName(), BIND), new Dependency("missing", BIND)))
            .build();

    Map<String, Configuration> configs = ImmutableMap.of(n1.getHostname(), n1);
    L3Adjacencies l3Adjacencies = new MockL3Adjacencies(ImmutableBiMap.of());

    // i2 is down due to missing BIND dependency
    assertThat(
        getInterfacesToDeactivate(configs, l3Adjacencies), contains(NodeInterfacePair.of(i2)));
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
    L3Adjacencies l3Adjacencies = new MockL3Adjacencies(ImmutableBiMap.of());

    // both AGGREGATE deps are missing, so deactivate
    assertThat(
        getInterfacesToDeactivate(configs, l3Adjacencies), contains(NodeInterfacePair.of(i1)));

    nf.interfaceBuilder().setOwner(n1).setVrf(v1).setName("missing1").build();
    // one of the AGGREGATE deps is present (and active), so don't deactivate
    assertThat(getInterfacesToDeactivate(configs, l3Adjacencies), empty());
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
    L3Adjacencies l3Adjacencies =
        new MockL3Adjacencies(
            ImmutableBiMap.of(
                NodeInterfacePair.of(m1), NodeInterfacePair.of(m2), // m1 -- m2
                NodeInterfacePair.of(m3), NodeInterfacePair.of(m4), // m3 -- m4
                NodeInterfacePair.of(pc1), NodeInterfacePair.of(pc2) // pc1 -- pc2
                ));

    // m1 is inactive, so m2 becomes inactive too
    m1.setActive(false);
    assertThat(
        getInterfacesToDeactivate(configs, l3Adjacencies), contains(NodeInterfacePair.of(m2)));

    // m4 is inactive so m3 becomes inactive too. that brings down the port-channels and their
    // subinterfaces
    m4.setActive(false);
    assertThat(
        getInterfacesToDeactivate(configs, l3Adjacencies),
        containsInAnyOrder(
            NodeInterfacePair.of(m2),
            NodeInterfacePair.of(m3),
            NodeInterfacePair.of(pc1),
            NodeInterfacePair.of(pc1_1),
            NodeInterfacePair.of(pc2),
            NodeInterfacePair.of(pc2_1)));

    // now member interfaces are active but one of the port-channels is inactive.
    // deactivate the other port-channel and both subinterfaces
    m1.setActive(true);
    m4.setActive(true);
    pc1.setActive(false);
    assertThat(
        getInterfacesToDeactivate(configs, l3Adjacencies),
        containsInAnyOrder(
            NodeInterfacePair.of(pc1_1), NodeInterfacePair.of(pc2), NodeInterfacePair.of(pc2_1)));
  }
}
