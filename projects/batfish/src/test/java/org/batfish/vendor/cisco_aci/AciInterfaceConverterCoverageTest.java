package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.lang.reflect.Method;
import java.util.Map;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Vrf;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.batfish.vendor.cisco_aci.representation.FabricLink;
import org.batfish.vendor.cisco_aci.representation.FabricNode;
import org.batfish.vendor.cisco_aci.representation.FabricNodeInterface;
import org.batfish.vendor.cisco_aci.representation.ManagementInfo;
import org.batfish.vendor.cisco_aci.representation.PathAttachment;
import org.junit.Test;

/** Coverage tests for package-private {@code AciInterfaceConverter}. */
public final class AciInterfaceConverterCoverageTest {

  @Test
  public void testConvertInterfaces_enrichesPathAttachmentAndExplicitLinks() {
    AciConfiguration aciConfig = new AciConfiguration();
    FabricNode node = fabricNode("101", "leaf101", "leaf");

    FabricNodeInterface eth11 = new FabricNodeInterface();
    eth11.setName("Ethernet1/1");
    eth11.setType("ethernet");
    eth11.setEnabled(true);
    eth11.setDescription("server-link");
    node.getInterfaces().put("Ethernet1/1", eth11);

    aciConfig.getNodeInterfaces().put("101", ImmutableSet.of("Ethernet1/53"));
    PathAttachment pathAttachment = new PathAttachment();
    pathAttachment.setDescription("uplink-att");
    pathAttachment.setEpgTenant("tenant1");
    pathAttachment.setEpgName("epg1");
    pathAttachment.setEncapsulation("vlan-200");
    aciConfig.getPathAttachmentMap().put("101", ImmutableMap.of("Ethernet1/53", pathAttachment));

    aciConfig.setFabricLinks(ImmutableList.of(new FabricLink("101", "eth1/54", "201", "Eth1/49")));

    ManagementInfo mgmtInfo = new ManagementInfo();
    mgmtInfo.setAddress("10.0.0.2/24");
    mgmtInfo.setGateway("10.0.0.1");
    node.setManagementInfo(mgmtInfo);

    Configuration c =
        Configuration.builder()
            .setHostname("leaf101")
            .setConfigurationFormat(ConfigurationFormat.CISCO_ACI)
            .build();
    Vrf vrf = Vrf.builder().setName("default").setOwner(c).build();
    Warnings warnings = new Warnings();

    Map<String, Interface> ifaces = convertInterfaces(node, aciConfig, vrf, c, warnings);

    assertThat(ifaces, hasKey("Ethernet1/1"));
    assertThat(ifaces, hasKey("Ethernet1/53"));
    assertThat(ifaces, hasKey("Ethernet1/54"));
    assertThat(ifaces, hasKey("loopback0"));
    assertThat(ifaces, hasKey("mgmt0"));

    Interface pathIface = ifaces.get("Ethernet1/53");
    assertNotNull(pathIface.getDescription());
    assertThat(pathIface.getDescription(), containsString("uplink-att"));
    assertThat(pathIface.getDescription(), containsString("EPG: tenant1:epg1"));
    assertThat(pathIface.getDescription(), containsString("VLAN: vlan-200"));

    Interface mgmtIface = ifaces.get("mgmt0");
    assertNotNull(mgmtIface.getAddress());
    assertThat(mgmtIface.getDescription(), containsString("Gateway: 10.0.0.1"));
    assertTrue(warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testConvertInterfaces_badManagementAddressEmitsWarning() {
    AciConfiguration aciConfig = new AciConfiguration();
    FabricNode node = fabricNode("201", "spine201", "spine");

    FabricNodeInterface iface = new FabricNodeInterface();
    iface.setName("Ethernet1/10");
    iface.setType("ethernet");
    iface.setEnabled(true);
    node.getInterfaces().put("Ethernet1/10", iface);

    ManagementInfo mgmtInfo = new ManagementInfo();
    mgmtInfo.setAddress("not-an-ip");
    node.setManagementInfo(mgmtInfo);

    Configuration c =
        Configuration.builder()
            .setHostname("spine201")
            .setConfigurationFormat(ConfigurationFormat.CISCO_ACI)
            .build();
    Vrf vrf = Vrf.builder().setName("default").setOwner(c).build();
    Warnings warnings = new Warnings(false, true, true);

    Map<String, Interface> ifaces = convertInterfaces(node, aciConfig, vrf, c, warnings);

    assertThat(ifaces, hasKey("mgmt0"));
    assertThat(warnings.getRedFlagWarnings(), hasSize(1));
    assertThat(
        Iterables.getOnlyElement(warnings.getRedFlagWarnings()).getText(),
        containsString("Failed to parse management address"));
  }

  @Test
  public void testConvertInterfaces_roleNullDoesNotCreateFallbackInterfaces() {
    AciConfiguration aciConfig = new AciConfiguration();
    FabricNode node = fabricNode("301", "node301", null);
    Configuration c =
        Configuration.builder()
            .setHostname("node301")
            .setConfigurationFormat(ConfigurationFormat.CISCO_ACI)
            .build();
    Vrf vrf = Vrf.builder().setName("default").setOwner(c).build();
    Warnings warnings = new Warnings(false, true, true);

    Map<String, Interface> ifaces = convertInterfaces(node, aciConfig, vrf, c, warnings);

    assertThat(ifaces.keySet(), hasSize(1));
    assertThat(ifaces, hasKey("loopback0"));
    assertThat(warnings.getRedFlagWarnings(), hasSize(1));
    assertThat(
        Iterables.getOnlyElement(warnings.getRedFlagWarnings()).getText(),
        containsString("No interfaces defined"));
  }

  @Test
  public void testConvertInterfaces_fallbackRoleSpecificPorts() {
    AciConfiguration aciConfig = new AciConfiguration();
    Configuration cLeaf =
        Configuration.builder()
            .setHostname("leaf")
            .setConfigurationFormat(ConfigurationFormat.CISCO_ACI)
            .build();
    Configuration cSpine =
        Configuration.builder()
            .setHostname("spine")
            .setConfigurationFormat(ConfigurationFormat.CISCO_ACI)
            .build();
    Vrf leafVrf = Vrf.builder().setName("default").setOwner(cLeaf).build();
    Vrf spineVrf = Vrf.builder().setName("default").setOwner(cSpine).build();

    Map<String, Interface> leafIfaces =
        convertInterfaces(
            fabricNode("101", "leaf101", "service"), aciConfig, leafVrf, cLeaf, new Warnings());
    Map<String, Interface> spineIfaces =
        convertInterfaces(
            fabricNode("201", "spine201", "spine"), aciConfig, spineVrf, cSpine, new Warnings());

    assertThat(leafIfaces, hasKey("ethernet1/1"));
    assertThat(leafIfaces, hasKey("ethernet1/8"));
    assertThat(leafIfaces, hasKey("ethernet1/53"));
    assertThat(leafIfaces, hasKey("ethernet1/54"));

    assertThat(spineIfaces, hasKey("ethernet1/1"));
    assertThat(spineIfaces, hasKey("ethernet1/32"));
    assertFalse(spineIfaces.containsKey("ethernet1/53"));
  }

  @Test
  public void testConvertInterfaces_mapsInterfaceTypesAndFabricDescriptions() {
    AciConfiguration aciConfig = new AciConfiguration();
    FabricNode node = fabricNode("401", "node401", "spine");

    FabricNodeInterface vlan = new FabricNodeInterface();
    vlan.setName("Vlan100");
    vlan.setType("vlan");
    node.getInterfaces().put("Vlan100", vlan);

    FabricNodeInterface loopback = new FabricNodeInterface();
    loopback.setName("Loopback1");
    loopback.setType("loopback");
    node.getInterfaces().put("Loopback1", loopback);

    FabricNodeInterface portChannel = new FabricNodeInterface();
    portChannel.setName("port-channel10");
    portChannel.setType("portchannel");
    node.getInterfaces().put("port-channel10", portChannel);

    FabricNodeInterface aggregated = new FabricNodeInterface();
    aggregated.setName("agg20");
    aggregated.setType("aggregated");
    node.getInterfaces().put("agg20", aggregated);

    FabricNodeInterface unknown = new FabricNodeInterface();
    unknown.setName("eth1/10");
    unknown.setType("unknown");
    node.getInterfaces().put("eth1/10", unknown);

    Configuration c =
        Configuration.builder()
            .setHostname("node401")
            .setConfigurationFormat(ConfigurationFormat.CISCO_ACI)
            .build();
    Vrf vrf = Vrf.builder().setName("default").setOwner(c).build();

    Map<String, Interface> ifaces = convertInterfaces(node, aciConfig, vrf, c, new Warnings());

    assertThat(ifaces.get("Vlan100").getInterfaceType(), equalTo(InterfaceType.VLAN));
    assertThat(ifaces.get("Loopback1").getInterfaceType(), equalTo(InterfaceType.LOOPBACK));
    assertThat(ifaces.get("port-channel10").getInterfaceType(), equalTo(InterfaceType.AGGREGATED));
    assertThat(ifaces.get("agg20").getInterfaceType(), equalTo(InterfaceType.AGGREGATED));
    assertThat(ifaces.get("eth1/10").getInterfaceType(), equalTo(InterfaceType.PHYSICAL));
    assertNotNull(ifaces.get("eth1/10").getDescription());
    assertThat(
        ifaces.get("eth1/10").getDescription(), containsString("Fabric interface (IS-IS/Overlay)"));
  }

  private static Map<String, Interface> convertInterfaces(
      FabricNode node, AciConfiguration aciConfig, Vrf vrf, Configuration c, Warnings warnings) {
    try {
      Class<?> clazz =
          Class.forName("org.batfish.vendor.cisco_aci.representation.AciInterfaceConverter");
      Method m =
          clazz.getDeclaredMethod(
              "convertInterfaces",
              FabricNode.class,
              AciConfiguration.class,
              Vrf.class,
              Configuration.class,
              Warnings.class);
      m.setAccessible(true);
      @SuppressWarnings("unchecked")
      Map<String, Interface> ifaces =
          (Map<String, Interface>) m.invoke(null, node, aciConfig, vrf, c, warnings);
      return ifaces;
    } catch (ReflectiveOperationException e) {
      throw new AssertionError("Failed to invoke AciInterfaceConverter.convertInterfaces", e);
    }
  }

  private static FabricNode fabricNode(String nodeId, String name, String role) {
    FabricNode node = new FabricNode();
    node.setNodeId(nodeId);
    node.setName(name);
    node.setRole(role);
    node.setPodId("1");
    return node;
  }
}
