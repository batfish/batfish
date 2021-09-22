package org.batfish.common.topology;

import static org.batfish.common.topology.IpOwners.computeIpInterfaceOwners;
import static org.batfish.common.topology.IpOwners.computeIpNodeOwners;
import static org.batfish.common.topology.TopologyUtil.computeNodeInterfaces;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSortedMap;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.VrrpGroup;
import org.junit.Before;
import org.junit.Test;

/** Tests of VRRP computation in {@link org.batfish.common.topology.TopologyUtil} */
public class VrrpComputationTest {

  private NetworkFactory _nf;
  private Interface _i1;
  private Interface _i2;
  private ConcreteInterfaceAddress _virtInterfaceAddr =
      ConcreteInterfaceAddress.create(Ip.parse("1.1.1.1"), Prefix.MAX_PREFIX_LENGTH);

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

    int vrrpGroupId = 1;
    int priority = 100;
    VrrpGroup vg1 = new VrrpGroup(vrrpGroupId);
    vg1.setPriority(priority);
    vg1.setVirtualAddress(_virtInterfaceAddr);
    VrrpGroup vg2 = new VrrpGroup(vrrpGroupId);
    if (equalPriority) {
      vg2.setPriority(priority);
    } else {
      vg2.setPriority(priority + 1);
    }
    vg2.setVirtualAddress(_virtInterfaceAddr);

    Configuration c1 = cb.setHostname("n1").build();
    Configuration c2 = cb.setHostname("n2").build();

    Interface.Builder ib = _nf.interfaceBuilder();

    _i1 =
        ib.setOwner(c1)
            .setAddress(ConcreteInterfaceAddress.parse("1.1.1.22/32"))
            .setVrrpGroups(ImmutableSortedMap.of(vrrpGroupId, vg1))
            .build();

    _i2 =
        ib.setOwner(c2)
            .setAddress(ConcreteInterfaceAddress.parse("1.1.1.33/32"))
            .setVrrpGroups(ImmutableSortedMap.of(vrrpGroupId, vg2))
            .build();

    return ImmutableSortedMap.of("n1", c1, "n2", c2);
  }

  /** Test that higher priority router wins */
  @Test
  public void testVrrpPriority() {
    Map<String, Configuration> configs = setupVrrpTestCase(false);

    Map<Ip, Set<String>> owners = computeIpNodeOwners(configs, false);

    assertThat(owners.get(_virtInterfaceAddr.getIp()), contains("n2"));
  }

  /**
   * Test that VRRP tie breaking works correctly by picking higher IP in case of equal vrrp group
   * priority
   */
  @Test
  public void testVrrpPriorityTieBreaking() {
    Map<String, Configuration> configs = setupVrrpTestCase(true);

    Map<Ip, Set<String>> owners = computeIpNodeOwners(configs, false);

    // Ensure node that has higher interface IP wins
    assertThat(owners.get(_virtInterfaceAddr.getIp()), contains("n2"));
  }

  @Test
  public void testIpInterfaceOwnersWithVrrp() {
    Map<String, Configuration> configs = setupVrrpTestCase(true);

    Map<Ip, Map<String, Set<String>>> interfaceOwners =
        computeIpInterfaceOwners(computeNodeInterfaces(configs), false);

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
            equalTo(_virtInterfaceAddr.getIp()),
            hasEntry(equalTo(_i2.getOwner().getHostname()), contains(_i2.getName()))));
  }
}
