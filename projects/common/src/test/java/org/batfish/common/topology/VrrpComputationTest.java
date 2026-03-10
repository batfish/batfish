package org.batfish.common.topology;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

import com.google.common.collect.ImmutableSortedMap;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.VrrpGroup;
import org.batfish.datamodel.tracking.PreDataPlaneTrackMethodEvaluator;
import org.junit.Before;
import org.junit.Test;

/** Tests of VRRP computation in {@link org.batfish.common.topology.TopologyUtil} */
public class VrrpComputationTest {

  private NetworkFactory _nf;
  private Interface _i1;
  private Interface _i2;
  private final Ip _virtInterfaceAddr = Ip.parse("1.1.1.1");

  @Before
  public void setup() {
    _nf = new NetworkFactory();
  }

  /**
   * Setup up two nodes with two vrrp groups that own the same virtual IP.
   *
   * @param equalPriority whether vrrp group priority between two groups should be equal (will force
   *     IP-based tie breaking)
   */
  private Map<String, Configuration> setupVrrpTestCase(boolean equalPriority) {
    // Setup nodes and interface configs
    Configuration.Builder cb = _nf.configurationBuilder();
    cb.setConfigurationFormat(ConfigurationFormat.JUNIPER);

    ConcreteInterfaceAddress i1SourceAddress = ConcreteInterfaceAddress.parse("1.1.1.22/32");
    ConcreteInterfaceAddress i2SourceAddress = ConcreteInterfaceAddress.parse("1.1.1.33/32");

    String i1Name = "i1";
    String i2Name = "i2";

    int vrrpGroupId = 1;
    int priority = 100;
    VrrpGroup.Builder vg1 =
        VrrpGroup.builder()
            .setPriority(priority)
            .setSourceAddress(i1SourceAddress)
            .setVirtualAddresses(i1Name, _virtInterfaceAddr);
    VrrpGroup.Builder vg2 =
        VrrpGroup.builder()
            .setSourceAddress(i2SourceAddress)
            .setVirtualAddresses(i2Name, _virtInterfaceAddr);
    if (equalPriority) {
      vg2.setPriority(priority);
    } else {
      vg2.setPriority(priority + 1);
    }

    Configuration c1 = cb.setHostname("n1").build();
    Configuration c2 = cb.setHostname("n2").build();

    Interface.Builder ib = _nf.interfaceBuilder();

    _i1 =
        ib.setOwner(c1)
            .setAddress(i1SourceAddress)
            .setVrrpGroups(ImmutableSortedMap.of(vrrpGroupId, vg1.build()))
            .setName(i1Name)
            .build();

    _i2 =
        ib.setOwner(c2)
            .setAddress(i2SourceAddress)
            .setVrrpGroups(ImmutableSortedMap.of(vrrpGroupId, vg2.build()))
            .setName(i2Name)
            .build();

    return ImmutableSortedMap.of("n1", c1, "n2", c2);
  }

  /** Test that higher priority router wins */
  @Test
  public void testVrrpPriority() {
    Map<String, Configuration> configs = setupVrrpTestCase(false);

    Map<Ip, Set<String>> owners = new TestIpOwners(configs).getNodeOwners(false);

    assertThat(owners.get(_virtInterfaceAddr), contains("n2"));
  }

  /**
   * Test that VRRP tie breaking works correctly by picking higher IP in case of equal vrrp group
   * priority
   */
  @Test
  public void testVrrpPriorityTieBreaking() {
    Map<String, Configuration> configs = setupVrrpTestCase(true);

    Map<Ip, Set<String>> owners = new TestIpOwners(configs).getNodeOwners(false);

    // Ensure node that has higher interface IP wins
    assertThat(owners.get(_virtInterfaceAddr), contains("n2"));
  }

  @Test
  public void testIpInterfaceOwnersWithVrrp() {
    Map<String, Configuration> configs = setupVrrpTestCase(true);

    Map<Ip, Map<String, Set<String>>> interfaceOwners =
        new TestIpOwners(configs).getAllDeviceOwnedIps();

    assertThat(
        interfaceOwners,
        hasEntry(
            equalTo(_i1.getConcreteAddress().getIp()),
            hasEntry(equalTo(_i1.getOwner().getHostname()), contains(_i1.getName()))));
    assertThat(
        interfaceOwners,
        hasEntry(
            equalTo(_i2.getConcreteAddress().getIp()),
            hasEntry(equalTo(_i2.getOwner().getHostname()), contains(_i2.getName()))));
    assertThat(
        interfaceOwners,
        hasEntry(
            equalTo(_virtInterfaceAddr),
            hasEntry(equalTo(_i2.getOwner().getHostname()), contains(_i2.getName()))));
  }

  private static class TestIpOwners extends IpOwnersBaseImpl {
    protected TestIpOwners(Map<String, Configuration> configurations) {
      super(
          configurations,
          GlobalBroadcastNoPointToPoint.instance(),
          PreDataPlaneTrackMethodEvaluator::new,
          false);
    }
  }
}
