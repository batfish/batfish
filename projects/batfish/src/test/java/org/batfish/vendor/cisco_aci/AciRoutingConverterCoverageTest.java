package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Vrf;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.batfish.vendor.cisco_aci.representation.BgpPeer;
import org.batfish.vendor.cisco_aci.representation.BridgeDomain;
import org.batfish.vendor.cisco_aci.representation.ExternalEpg;
import org.batfish.vendor.cisco_aci.representation.FabricNode;
import org.batfish.vendor.cisco_aci.representation.L2Out;
import org.batfish.vendor.cisco_aci.representation.L3Out;
import org.batfish.vendor.cisco_aci.representation.OspfArea;
import org.batfish.vendor.cisco_aci.representation.OspfConfig;
import org.batfish.vendor.cisco_aci.representation.OspfInterface;
import org.batfish.vendor.cisco_aci.representation.StaticRoute;
import org.batfish.vendor.cisco_aci.representation.TenantVrf;
import org.junit.Test;

/** Coverage-focused tests for package-private {@code AciRoutingConverter}. */
public final class AciRoutingConverterCoverageTest {

  @Test
  public void testConvertL2Outs_coversEncapAndVrfSelection() {
    AciConfiguration aciConfig = new AciConfiguration();

    BridgeDomain bd = new BridgeDomain("tenant1:bd1");
    bd.setVrf("tenant1:vrf1");
    aciConfig.getBridgeDomains().put("tenant1:bd1", bd);
    aciConfig.getVrfs().put("tenant1:vrf1", new TenantVrf("tenant1:vrf1"));

    L2Out vlan = new L2Out("l2-vlan");
    vlan.setBridgeDomain("tenant1:bd1");
    vlan.setEncapsulation("vlan-100");
    vlan.setDescription("l2 vlan");

    L2Out vxlan = new L2Out("l2-vxlan");
    vxlan.setEncapsulation("vxlan-5000");

    L2Out invalid = new L2Out("l2-invalid");
    invalid.setEncapsulation("vlan-bad");

    L2Out duplicate = new L2Out("dup");
    duplicate.setEncapsulation("vlan-200");

    aciConfig
        .getL2Outs()
        .putAll(
            ImmutableMap.of(
                "tenant1:l2-vlan", vlan,
                "tenant1:l2-vxlan", vxlan,
                "tenant1:l2-invalid", invalid,
                "tenant1:dup", duplicate));

    Configuration c =
        Configuration.builder()
            .setHostname("leaf1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_ACI)
            .build();
    Vrf defaultVrf = Vrf.builder().setName("default").setOwner(c).build();
    Vrf tenantVrf = Vrf.builder().setName("tenant1:vrf1").setOwner(c).build();
    Warnings warnings = new Warnings(false, true, true);

    Map<String, Interface> interfaces = new TreeMap<>();
    interfaces.put(
        "L2Out-dup",
        Interface.builder()
            .setName("L2Out-dup")
            .setOwner(c)
            .setVrf(defaultVrf)
            .setType(InterfaceType.VLAN)
            .build());

    convertL2Outs(aciConfig, interfaces, defaultVrf, c, warnings);

    assertThat(interfaces, hasKey("L2Out-l2-vlan"));
    assertThat(interfaces, hasKey("L2Out-l2-vxlan"));
    assertFalse(interfaces.containsKey("L2Out-l2-invalid"));
    assertThat(interfaces.get("L2Out-l2-vlan").getVlan(), equalTo(100));
    assertThat(interfaces.get("L2Out-l2-vlan").getVrf().getName(), equalTo("tenant1:vrf1"));
    assertThat(interfaces.get("L2Out-l2-vxlan").getVlan(), equalTo((5000 % 4094) + 1));
    assertThat(interfaces.get("L2Out-l2-vxlan").getVrf().getName(), equalTo("default"));
    assertThat(interfaces.get("L2Out-dup").getVrf().getName(), equalTo("default"));
    assertThat(warnings.getRedFlagWarnings(), hasSize(1));
    assertThat(
        warnings.getRedFlagWarnings().first().getText(),
        containsString("Invalid VLAN encapsulation"));
  }

  @Test
  public void testConvertL3Outs_coversBgpStaticOspfAndExternal() {
    AciConfiguration aciConfig = new AciConfiguration();
    FabricNode node = new FabricNode();
    node.setNodeId("node-x");
    node.setName("leaf1");
    node.setRole("leaf");

    Configuration c =
        Configuration.builder()
            .setHostname("leaf1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_ACI)
            .build();
    Vrf defaultVrf = Vrf.builder().setName("default").setOwner(c).build();
    Warnings warnings = new Warnings(false, true, true);

    Map<String, Interface> interfaces = new TreeMap<>();
    interfaces.put(
        "eth1/1",
        Interface.builder()
            .setName("eth1/1")
            .setOwner(c)
            .setVrf(defaultVrf)
            .setType(InterfaceType.PHYSICAL)
            .setAddress(ConcreteInterfaceAddress.parse("192.0.2.1/24"))
            .build());

    L3Out l3Out = new L3Out("out1");
    l3Out.setVrf("missing-vrf");

    org.batfish.vendor.cisco_aci.representation.BgpProcess bgpProcess =
        new org.batfish.vendor.cisco_aci.representation.BgpProcess();
    bgpProcess.setAs(65000L);
    bgpProcess.setRouterId("not-an-ip");
    l3Out.setBgpProcess(bgpProcess);

    BgpPeer peer1 = new BgpPeer();
    peer1.setPeerAddress("192.0.2.2");
    peer1.setRemoteAs("bad-as");
    peer1.setLocalAs("bad-local-as");
    peer1.setUpdateSourceInterface("missing-if");
    peer1.setLocalPreference("bad-pref");
    peer1.setImportRouteMap("RM_IN");
    peer1.setExportRouteMap("RM_OUT");
    peer1.setNextHopSelf(true);
    peer1.setRouteReflectorClient(true);
    peer1.setEbgpMultihop(true);

    BgpPeer peer2 = new BgpPeer();
    peer2.setPeerAddress(null);

    l3Out.setBgpPeers(ImmutableList.of(peer1, peer2));

    StaticRoute validRoute = new StaticRoute();
    validRoute.setPrefix("10.10.0.0/24");
    validRoute.setNextHop("192.0.2.254");
    validRoute.setNextHopInterface("eth1/1");
    validRoute.setAdministrativeDistance("5");
    validRoute.setTag("99");
    validRoute.setTrack("trk1");

    StaticRoute nullPrefix = new StaticRoute();
    nullPrefix.setPrefix(null);

    StaticRoute badPrefix = new StaticRoute();
    badPrefix.setPrefix("bad-prefix");
    badPrefix.setNextHop("192.0.2.9");

    StaticRoute invalidHop = new StaticRoute();
    invalidHop.setPrefix("10.20.0.0/24");
    invalidHop.setNextHop("not-ip");
    invalidHop.setNextHopInterface("missing-if");
    invalidHop.setAdministrativeDistance("bad");
    invalidHop.setTag("bad-tag");

    l3Out.setStaticRoutes(ImmutableList.of(validRoute, nullPrefix, badPrefix, invalidHop));

    OspfConfig ospfConfig = new OspfConfig();
    ospfConfig.setProcessId("");
    ospfConfig.setAreaId("0.0.0.1");
    OspfArea badArea = new OspfArea();
    badArea.setAreaId("bad-area");
    badArea.setAreaType("stub");
    ospfConfig.getAreas().put("bad", badArea);
    OspfInterface ospfIface = new OspfInterface();
    ospfIface.setName("eth1/1");
    ospfIface.setCost(10);
    ospfIface.setHelloInterval(5);
    ospfIface.setDeadInterval(20);
    ospfIface.setNetworkType("nbma");
    ospfIface.setPassive(true);
    OspfInterface missingOspfIface = new OspfInterface();
    missingOspfIface.setName("missing-iface");
    ospfConfig.setOspfInterfaces(ImmutableList.of(ospfIface, missingOspfIface));
    l3Out.setOspfConfig(ospfConfig);

    ExternalEpg ext = new ExternalEpg("ext1");
    ext.setSubnets(ImmutableList.of("203.0.113.0/24", "bad-subnet"));
    ext.setNextHop("bad-next-hop");
    ext.setInterface("missing-if");
    l3Out.setExternalEpgs(ImmutableList.of(ext));

    aciConfig.getL3Outs().put("tenant1:out1", l3Out);
    convertL3Outs(node, aciConfig, interfaces, defaultVrf, c, warnings);

    assertFalse(defaultVrf.getStaticRoutes().isEmpty());
    assertThat(c.getRoutingPolicies(), hasKey("~BGP_IMPORT~out1~192.0.2.2"));
    assertThat(c.getRoutingPolicies(), hasKey("~BGP_EXPORT~out1~192.0.2.2"));
    assertFalse(defaultVrf.getOspfProcesses().isEmpty());
    assertTrue(interfaces.get("eth1/1").getOspfSettings().getPassive());

    String warningText =
        warnings.getRedFlagWarnings().stream()
            .map(Warning::getText)
            .collect(Collectors.joining(" || "));
    assertThat(warningText, containsString("VRF missing-vrf not found"));
    assertThat(warningText, containsString("Invalid router ID"));
    assertThat(warningText, containsString("Invalid remote AS"));
    assertThat(warningText, containsString("has no peer address"));
    assertThat(warningText, containsString("Invalid prefix"));
    assertThat(warningText, containsString("has no valid next hop"));
    assertThat(warningText, containsString("Invalid OSPF area ID"));
    assertThat(warningText, containsString("OSPF interface missing-iface"));
    assertThat(warningText, containsString("Invalid next hop bad-next-hop"));
    assertThat(warningText, containsString("Interface missing-if not found for external EPG"));
  }

  private static void convertL2Outs(
      AciConfiguration aciConfig,
      Map<String, Interface> interfaces,
      Vrf defaultVrf,
      Configuration c,
      Warnings warnings) {
    invoke(
        "convertL2Outs",
        new Class<?>[] {
          AciConfiguration.class, Map.class, Vrf.class, Configuration.class, Warnings.class
        },
        new Object[] {aciConfig, interfaces, defaultVrf, c, warnings});
  }

  private static void convertL3Outs(
      FabricNode node,
      AciConfiguration aciConfig,
      Map<String, Interface> interfaces,
      Vrf vrf,
      Configuration c,
      Warnings warnings) {
    invoke(
        "convertL3Outs",
        new Class<?>[] {
          FabricNode.class,
          AciConfiguration.class,
          Map.class,
          Vrf.class,
          Configuration.class,
          Warnings.class
        },
        new Object[] {node, aciConfig, interfaces, vrf, c, warnings});
  }

  private static void invoke(String methodName, Class<?>[] argTypes, Object[] args) {
    try {
      Class<?> clazz =
          Class.forName("org.batfish.vendor.cisco_aci.representation.AciRoutingConverter");
      Method m = clazz.getDeclaredMethod(methodName, argTypes);
      m.setAccessible(true);
      m.invoke(null, args);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError("Failed to invoke AciRoutingConverter." + methodName, e);
    }
  }
}
