package org.batfish.vendor.a10.vrrp_e2e;

import static org.batfish.vendor.a10.representation.A10Configuration.getInterfaceName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.Ip;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.main.TestrigText;
import org.batfish.vendor.a10.representation.Interface.Type;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** End-to-end tests of vrrp-a on A10v5 with paired devices. */
public final class A10VrrpATest {

  private static final String SNAPSHOT_FOLDER = "org/batfish/vendor/a10/vrrp_e2e/snapshots/vrrp_a";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Batfish _batfish;

  private static final String R1 = "r1";
  private static final String R2 = "r2";

  /*
   * Topology:
   * r1 <==================> r2
   *    Ethernet1  Ethernet1
   *    .1  192.0.2.0/30  .2 (on respective IRB interfaces)
   * - Ethernet1 on each device is the HA heartbeat interface
   * - Ethernet1 is a trunk switchport with tagged vlan 4094
   * - The router-interface (IRB) for VLAN 4094 is VirtualEthernet4094
   * - Each device has an L3 interface Ethernet2 with a concrete IP
   *   - ri[Ethernet2] has 10.0.1.1/24
   *   - r2[Ethernet2] has 10.0.2.1/24
   * - Each device has a VIP with IP 10.1.0.1
   * - Each device has an SNAT pool with subnet 10.2.0.1/32
   * - Each device has a floating IP 10.3.0.1
   * - A VrrpGroup should be installed on Ethernet1 on each device
   *   - r1 has higher priority
   *   - On the winner (r1), the addresses for the VIP, SNAT pool, and floating IPs should be
   *     installed on Ethernet2
   */
  @Before
  public void setup() throws IOException {
    _batfish =
        BatfishTestUtils.getBatfishFromTestrigText(
            TestrigText.builder()
                .setConfigurationFiles(SNAPSHOT_FOLDER, ImmutableSet.of(R1, R2))
                .setLayer1TopologyPrefix(String.format("%s/batfish", SNAPSHOT_FOLDER))
                .build(),
            _folder);
    _batfish.computeDataPlane(_batfish.getSnapshot());
  }

  @Test
  public void testIpOwners() {
    Map<String, Map<String, Set<Ip>>> ipOwners =
        _batfish
            .getTopologyProvider()
            .getInitialIpOwners(_batfish.getSnapshot())
            .getInterfaceOwners(true);
    Map<String, Set<Ip>> r1IpsByInterface = ipOwners.get(R1);
    Map<String, Set<Ip>> r2IpsByInterface = ipOwners.get(R2);
    String ve4094Name = getInterfaceName(Type.VE, 4094);
    String e2Name = getInterfaceName(Type.ETHERNET, 2);

    assertThat(r1IpsByInterface.get(ve4094Name), containsInAnyOrder(Ip.parse("192.0.2.1")));
    assertThat(
        r1IpsByInterface.get(e2Name),
        containsInAnyOrder(
            Ip.parse("10.0.1.1"), // interface addresss
            Ip.parse("10.1.0.1"), // VIP
            Ip.parse("10.2.0.1"), // SNAT pool
            Ip.parse("10.3.0.1") // floating IP
            ));
    assertThat(r2IpsByInterface.get(ve4094Name), containsInAnyOrder(Ip.parse("192.0.2.2")));
    assertThat(
        r2IpsByInterface.get(e2Name),
        containsInAnyOrder(
            Ip.parse("10.0.2.1") // interface addresss
            ));
  }
}
