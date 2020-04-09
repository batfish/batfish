package org.batfish.common.topology;

import static org.batfish.common.topology.IpOwners.computeInterfaceHostSubnetIps;
import static org.batfish.common.topology.IpOwners.computeIpIfaceOwners;
import static org.batfish.common.topology.IpOwners.computeIpVrfOwners;
import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.junit.Before;
import org.junit.Test;

/** Tests of {@link IpOwners}. */
public class IpOwnersTest {
  private Configuration.Builder _cb;
  private Interface.Builder _ib;
  private Vrf.Builder _vb;
  private static final Prefix P1 = Prefix.parse("1.0.0.0/8");
  private static final Prefix P2 = Prefix.parse("2.0.0.0/16");

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    _cb = nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _vb = nf.vrfBuilder();
    _ib = nf.interfaceBuilder();
  }

  @Test
  public void testComputeInterfaceHostSubnetIps() {
    Configuration c1 = _cb.build();
    Map<String, Configuration> configs = ImmutableMap.of(c1.getHostname(), c1);
    Vrf vrf1 = _vb.setOwner(c1).build();
    Interface i1 =
        _ib.setOwner(c1)
            .setVrf(vrf1)
            .setAddress(ConcreteInterfaceAddress.create(P1.getStartIp(), P1.getPrefixLength()))
            .build();
    Interface i2 =
        _ib.setOwner(c1)
            .setVrf(vrf1)
            .setAddress(ConcreteInterfaceAddress.create(P2.getStartIp(), P2.getPrefixLength()))
            .setActive(false)
            .build();

    // Test with only active IPs.
    assertThat(
        computeInterfaceHostSubnetIps(configs, true),
        hasEntry(
            equalTo(c1.getHostname()),
            allOf(
                hasEntry(
                    equalTo(i1.getName()),
                    allOf(
                        not(containsIp(P1.getStartIp())),
                        containsIp(Ip.create(P1.getStartIp().asLong() + 1)),
                        containsIp(Ip.create(P1.getEndIp().asLong() - 1)),
                        not(containsIp(P1.getEndIp())))),
                not(hasKey(i2.getName())))));

    // Test including inactive IPs.
    assertThat(
        computeInterfaceHostSubnetIps(configs, false),
        hasEntry(
            equalTo(c1.getHostname()),
            allOf(
                hasEntry(
                    equalTo(i1.getName()),
                    allOf(
                        not(containsIp(P1.getStartIp())),
                        containsIp(Ip.create(P1.getStartIp().asLong() + 1)),
                        containsIp(Ip.create(P1.getEndIp().asLong() - 1)),
                        not(containsIp(P1.getEndIp())))),
                hasEntry(
                    equalTo(i2.getName()),
                    allOf(
                        not(containsIp(P2.getStartIp())),
                        containsIp(Ip.create(P2.getStartIp().asLong() + 1)),
                        containsIp(Ip.create(P2.getEndIp().asLong() - 1)),
                        not(containsIp(P2.getEndIp())))))));
  }

  @Test
  public void testComputeInterfaceHostSubnetIpsWithPrefixLength31() {
    Configuration c1 = _cb.build();
    Map<String, Configuration> configs = ImmutableMap.of(c1.getHostname(), c1);
    Vrf vrf1 = _vb.setOwner(c1).build();
    Prefix prefix = Prefix.parse("1.0.0.1/31");
    Interface i1 =
        _ib.setOwner(c1)
            .setVrf(vrf1)
            .setAddress(
                ConcreteInterfaceAddress.create(prefix.getStartIp(), prefix.getPrefixLength()))
            .build();
    Map<String, Map<String, IpSpace>> interfaceHostSubnetIps =
        computeInterfaceHostSubnetIps(configs, false);

    assertThat(
        interfaceHostSubnetIps,
        hasEntry(
            equalTo(c1.getHostname()),
            hasEntry(
                equalTo(i1.getName()),
                allOf(containsIp(prefix.getStartIp()), containsIp(prefix.getEndIp())))));
  }

  @Test
  public void testComputeIpIfaceOwners() {
    String node = "node";
    String vrfName = "vrf";
    Vrf vrf = new Vrf(vrfName);
    String activeName = "active";
    Ip activeIp = Ip.parse("1.1.1.1");
    String inactiveName = "inactive";
    Ip inactiveIp = Ip.parse("2.2.2.2");
    int networkBits = 31;
    Map<String, Set<Interface>> allInterfaces =
        ImmutableMap.of(
            node,
            ImmutableSet.of(
                Interface.builder()
                    .setAddress(ConcreteInterfaceAddress.create(activeIp, networkBits))
                    .setVrf(vrf)
                    .setName(activeName)
                    .build(),
                Interface.builder()
                    .setAddress(ConcreteInterfaceAddress.create(inactiveIp, networkBits))
                    .setActive(false)
                    .setName(inactiveName)
                    .setVrf(vrf)
                    .build()));
    Map<Ip, Map<String, Set<String>>> activeIps =
        ImmutableMap.of(activeIp, ImmutableMap.of(node, ImmutableSet.of(activeName)));

    // Test
    Map<Ip, Map<String, Map<String, Set<String>>>> ipIfaceOwners =
        computeIpIfaceOwners(allInterfaces, activeIps);

    assertThat(
        ipIfaceOwners,
        equalTo(
            ImmutableMap.of(
                activeIp,
                ImmutableMap.of(node, ImmutableMap.of(vrfName, ImmutableSet.of(activeName))))));
  }

  @Test
  public void testComputeIpVrfOwners() {
    String node = "node";
    String vrf1 = "vrf1";
    String vrf2 = "vrf2";
    Ip ip = Ip.parse("1.1.1.1");

    Map<Ip, Map<String, Map<String, Set<String>>>> ipIfaceOwners =
        ImmutableMap.of(
            ip,
            ImmutableMap.of(
                node,
                ImmutableMap.of(
                    vrf1, ImmutableSet.of("iface1", "iface2"), vrf2, ImmutableSet.of("iface3"))));

    Map<Ip, Map<String, Set<String>>> ipVrfOwners = computeIpVrfOwners(ipIfaceOwners);
    assertThat(
        ipVrfOwners,
        equalTo(ImmutableMap.of(ip, ImmutableMap.of(node, ImmutableSet.of(vrf1, vrf2)))));
  }
}
