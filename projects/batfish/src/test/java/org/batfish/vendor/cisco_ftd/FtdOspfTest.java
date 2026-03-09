package org.batfish.vendor.cisco_ftd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.vendor.cisco_ftd.representation.FtdConfiguration;
import org.batfish.vendor.cisco_ftd.representation.FtdOspfNetwork;
import org.batfish.vendor.cisco_ftd.representation.FtdOspfProcess;
import org.junit.Test;

/** Tests for FTD OSPF parsing. */
public class FtdOspfTest extends FtdGrammarTest {

  private static final String ROUTER_OSPF_100 = "router ospf 100";
  private static final String ROUTER_ID_1_1_1_1 = "  router-id 1.1.1.1";
  private static final String NETWORK_192_168_1_0_AREA_0 =
      "  network 192.168.1.0 255.255.255.0 area 0";
  private static final String PASSIVE_INTERFACE_INSIDE = "  passive-interface inside";
  private static final String ROUTER_ID_2_2_2_2 = "  router-id 2.2.2.2";
  private static final String ROUTER_OSPF_200 = "router ospf 200";
  private static final String NETWORK_192_168_2_0_AREA_0 =
      "  network 192.168.2.0 255.255.255.0 area 0";

  @Test
  public void testOspfBasic() {
    String config = join("router ospf 100", "  router-id 1.1.1.1");

    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getOspfProcesses(), hasKey("100"));

    FtdOspfProcess ospf = vc.getOspfProcesses().get("100");
    assertThat(ospf, notNullValue());
    assertThat(ospf.getRouterId(), equalTo(Ip.parse("1.1.1.1")));
  }

  @Test
  public void testOspfNetwork() {
    String config = join("router ospf 100", "  network 192.168.1.0 255.255.255.0 area 0");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdOspfProcess ospf = vc.getOspfProcesses().get("100");
    assertThat(ospf.getNetworks(), hasSize(1));

    FtdOspfNetwork network = ospf.getNetworks().get(0);
    assertThat(network.getIp(), equalTo(Ip.parse("192.168.1.0")));
    assertThat(network.getMask(), equalTo(Ip.parse("255.255.255.0")));
    assertThat(network.getAreaId(), equalTo(0L));
  }

  @Test
  public void testOspfMultipleNetworks() {
    String config =
        join(
            ROUTER_OSPF_100,
            "  network 192.168.1.0 255.255.255.0 area 0",
            "  network 192.168.2.0 255.255.255.0 area 1",
            "  network 10.1.1.0 255.255.255.0 area 2");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdOspfProcess ospf = vc.getOspfProcesses().get("100");
    assertThat(ospf.getNetworks(), hasSize(3));

    assertThat(ospf.getNetworks().get(0).getAreaId(), equalTo(0L));
    assertThat(ospf.getNetworks().get(1).getAreaId(), equalTo(1L));
    assertThat(ospf.getNetworks().get(2).getAreaId(), equalTo(2L));
  }

  @Test
  public void testOspfPassiveInterface() {
    String config = join(ROUTER_OSPF_100, PASSIVE_INTERFACE_INSIDE, "  passive-interface dmz");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdOspfProcess ospf = vc.getOspfProcesses().get("100");
    Set<String> passiveIfaces = ospf.getPassiveInterfaces();

    assertThat(passiveIfaces, hasSize(2));
    assertThat(passiveIfaces.contains("inside"), equalTo(true));
    assertThat(passiveIfaces.contains("dmz"), equalTo(true));
  }

  @Test
  public void testOspfRouterId() {
    String config = join(ROUTER_OSPF_100, ROUTER_ID_1_1_1_1, ROUTER_ID_2_2_2_2);

    FtdConfiguration vc = parseVendorConfig(config);

    FtdOspfProcess ospf = vc.getOspfProcesses().get("100");
    assertThat(ospf.getRouterId(), equalTo(Ip.parse("2.2.2.2")));
  }

  @Test
  public void testOspfMultipleProcesses() {
    String config =
        join(
            "router ospf 100",
            "  router-id 1.1.1.1",
            "  network 192.168.1.0 255.255.255.0 area 0",
            "router ospf 200",
            "  router-id 2.2.2.2",
            "  network 192.168.2.0 255.255.255.0 area 0");

    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getOspfProcesses(), hasKey("100"));
    assertThat(vc.getOspfProcesses(), hasKey("200"));

    FtdOspfProcess ospf1 = vc.getOspfProcesses().get("100");
    assertThat(ospf1.getRouterId(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(ospf1.getNetworks(), hasSize(1));

    FtdOspfProcess ospf2 = vc.getOspfProcesses().get("200");
    assertThat(ospf2.getRouterId(), equalTo(Ip.parse("2.2.2.2")));
    assertThat(ospf2.getNetworks(), hasSize(1));
  }

  @Test
  public void testOspfComplexConfiguration() {
    String config =
        join(
            ROUTER_OSPF_100,
            "  router-id 10.1.1.1",
            NETWORK_192_168_1_0_AREA_0,
            "  network 192.168.2.0 255.255.255.0 area 1",
            "  network 10.1.1.0 255.255.255.0 area 0",
            PASSIVE_INTERFACE_INSIDE,
            "  passive-interface dmz");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdOspfProcess ospf = vc.getOspfProcesses().get("100");
    assertThat(ospf.getRouterId(), equalTo(Ip.parse("10.1.1.1")));
    assertThat(ospf.getNetworks(), hasSize(3));
    assertThat(ospf.getPassiveInterfaces(), hasSize(2));
  }

  @Test
  public void testOspfDifferentAreas() {
    String config =
        join(
            ROUTER_OSPF_100,
            NETWORK_192_168_1_0_AREA_0,
            "  network 192.168.2.0 255.255.255.0 area 1",
            "  network 192.168.3.0 255.255.255.0 area 2",
            "  network 10.0.0.0 255.0.0.0 area 0");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdOspfProcess ospf = vc.getOspfProcesses().get("100");
    assertThat(ospf.getNetworks(), hasSize(4));

    long areaCount =
        ospf.getNetworks().stream().mapToLong(FtdOspfNetwork::getAreaId).distinct().count();

    assertThat(areaCount, equalTo(3L));
  }

  @Test
  public void testOspfWithLargeAreaNumber() {
    String config = join(ROUTER_OSPF_100, "  network 192.168.1.0 255.255.255.0 area 100");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdOspfProcess ospf = vc.getOspfProcesses().get("100");
    assertThat(ospf.getNetworks().get(0).getAreaId(), equalTo(100L));
  }

  @Test
  public void testOspfSerialization() {
    String config =
        join(
            ROUTER_OSPF_100,
            ROUTER_ID_1_1_1_1,
            NETWORK_192_168_1_0_AREA_0,
            PASSIVE_INTERFACE_INSIDE);

    FtdConfiguration vc = parseVendorConfigWithSerialization(config);

    FtdOspfProcess ospf = vc.getOspfProcesses().get("100");
    assertThat(ospf.getRouterId(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(ospf.getNetworks(), hasSize(1));
    assertThat(ospf.getPassiveInterfaces(), hasSize(1));
  }

  @Test
  public void testOspfVendorConversion() {
    String config =
        join(
            "router ospf 100", "  router-id 1.1.1.1", "  network 192.168.1.0 255.255.255.0 area 0");

    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    assertThat(c, notNullValue());
  }

  @Test
  public void testOspfEmptyProcess() {
    String config = "router ospf 100\n";

    FtdConfiguration vc = parseVendorConfig(config);

    FtdOspfProcess ospf = vc.getOspfProcesses().get("100");
    assertThat(ospf, notNullValue());
    assertThat(ospf.getRouterId(), nullValue());
    assertThat(ospf.getNetworks(), hasSize(0));
    assertThat(ospf.getPassiveInterfaces(), hasSize(0));
  }

  @Test
  public void testOspfWithNetworksInDifferentProcesses() {
    String config =
        join(
            ROUTER_OSPF_100,
            ROUTER_ID_1_1_1_1,
            NETWORK_192_168_1_0_AREA_0,
            ROUTER_OSPF_200,
            ROUTER_ID_2_2_2_2,
            NETWORK_192_168_2_0_AREA_0,
            "router ospf 300",
            "  router-id 3.3.3.3",
            "  network 10.1.1.0 255.255.255.0 area 0");

    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getOspfProcesses().keySet(), hasSize(3));

    FtdOspfProcess ospf100 = vc.getOspfProcesses().get("100");
    assertThat(ospf100.getNetworks().get(0).getIp(), equalTo(Ip.parse("192.168.1.0")));

    FtdOspfProcess ospf200 = vc.getOspfProcesses().get("200");
    assertThat(ospf200.getNetworks().get(0).getIp(), equalTo(Ip.parse("192.168.2.0")));

    FtdOspfProcess ospf300 = vc.getOspfProcesses().get("300");
    assertThat(ospf300.getNetworks().get(0).getIp(), equalTo(Ip.parse("10.1.1.0")));
  }

  @Test
  public void testOspfWithInterfaces() {
    String config =
        join(
            "interface Ethernet1/0",
            "  ip address 192.168.1.1 255.255.255.0",
            "  nameif outside",
            ROUTER_OSPF_100,
            NETWORK_192_168_1_0_AREA_0,
            "  passive-interface outside");

    FtdConfiguration vc = parseVendorConfig(config);

    FtdOspfProcess ospf = vc.getOspfProcesses().get("100");
    assertThat(ospf.getPassiveInterfaces().contains("outside"), equalTo(true));
  }

  @Test
  public void testOspfVendorConversionMultipleProcesses() {
    String config =
        join(
            "router ospf 100",
            "  router-id 1.1.1.1",
            "  network 192.168.1.0 255.255.255.0 area 0",
            "router ospf 200",
            "  router-id 2.2.2.2",
            "  network 192.168.2.0 255.255.255.0 area 0");

    FtdConfiguration vc = parseVendorConfig(config);
    Configuration c = vc.toVendorIndependentConfigurations().get(0);

    // This should contain both processes
    assertThat(c.getDefaultVrf().getOspfProcesses(), hasKey("100"));
    assertThat(c.getDefaultVrf().getOspfProcesses(), hasKey("200"));
  }
}
