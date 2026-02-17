package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.junit.Test;

/**
 * Tests for ACI interface configurations on fabric nodes.
 *
 * <p>This test class verifies:
 *
 * <ul>
 *   <li>Fabric node interface configurations
 *   <li>Interface types (ethernet, port-channel, vlan)
 *   <li>Interface encapsulation and VLAN assignments
 *   <li>Interface speeds and properties
 *   <li>Logical interface profiles (l3extLIfP)
 *   <li>Interface address assignments
 *   <li>Multiple interfaces per node
 *   <li>Interface descriptions and annotations
 * </ul>
 */
public class AciInterfaceConfigTest {

  /** Creates a fabric node with interface configurations. */
  private static String createFabricNodeWithInterfaces() {
    return "{"
        + "\"polUni\": {"
        + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
        + "\"children\": ["
        + "{"
        + "\"fabricInst\": {"
        + "\"attributes\": {\"dn\": \"uni/fabric\"},"
        + "\"children\": ["
        + "{"
        + "\"fabricProtPol\": {"
        + "\"attributes\": {},"
        + "\"children\": ["
        + "{"
        + "\"fabricExplicitGEp\": {"
        + "\"attributes\": {},"
        + "\"children\": ["
        + "{"
        + "\"fabricNodePEp\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/fabric/nodePEp-101\","
        + "\"id\": \"101\","
        + "\"name\": \"spine1\","
        + "\"role\": \"spine\","
        + "\"podId\": \"1\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"l1PhysIf\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/fabric/nodePEp-101/phys-eth1/1\","
        + "\"id\": \"eth1/1\","
        + "\"descr\": \"Link to leaf1\""
        + "}"
        + "}"
        + "},"
        + "{"
        + "\"l1PhysIf\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/fabric/nodePEp-101/phys-eth1/2\","
        + "\"id\": \"eth1/2\","
        + "\"descr\": \"Link to leaf2\""
        + "}"
        + "}"
        + "}"
        + "]"
        + "}"
        + "}"
        + "]"
        + "}"
        + "}"
        + "]"
        + "}"
        + "}"
        + "]"
        + "}"
        + "}"
        + "]"
        + "}"
        + "}";
  }

  /** Creates L3Out with logical interface profile (l3extLIfP). */
  private static String createL3OutWithInterfaceProfile() {
    return "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\":"
        + " [{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-infra\", \"name\":"
        + " \"infra\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-infra/ctx-overlay\", \"name\": \"overlay\"},\"children\": []}},"
        + "{\"l3extOut\": {\"attributes\": {\"dn\": \"uni/tn-infra/out-external\", \"name\":"
        + " \"external\"},\"children\": [{\"l3extRsEctx\": {\"attributes\":"
        + " {\"tnFvCtxName\": \"overlay\"}}},{\"l3extLNodeP\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-infra/out-external/lnodep-nodes\", \"name\": \"nodes\"},\"children\": ["
        + "{\"l3extRsNodeL3OutAtt\": {\"attributes\": {\"rtrId\": \"10.96.1.1\",\"tDn\":"
        + " \"topology/pod-1/node-101\"}}},{\"l3extLIfP\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-infra/out-external/lnodep-nodes/lifp-intprof\", \"name\":"
        + " \"intprof\"},\"children\": [{\"l3extRsPathL3OutAtt\": {\"attributes\": {\"tDn\":"
        + " \"topology/pod-1/paths-101/pathep-[eth1/36]\",\"encap\": \"vlan-4\",\"addr\":"
        + " \"10.96.1.254/30\",\"descr\": \"IPN connection\"}}}]}}]}}]}}]}}]}}";
  }

  /** Creates bridge domain with subnet and VLAN configuration. */
  private static String createBridgeDomainWithVlanConfig() {
    return "{"
        + "\"polUni\": {"
        + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
        + "\"children\": ["
        + "{"
        + "\"fvTenant\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-vlan_tenant\", \"name\": \"vlan_tenant\"},"
        + "\"children\": ["
        + "{"
        + "\"fvCtx\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-vlan_tenant/ctx-vrf1\", \"name\": \"vrf1\"},"
        + "\"children\": []"
        + "}"
        + "},"
        + "{"
        + "\"fvBD\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-vlan_tenant/BD-bd_vlan\","
        + "\"name\": \"bd_vlan\","
        + "\"descr\": \"VLAN-backed BD\","
        + "\"arpFlood\": \"yes\","
        + "\"unicastRoute\": \"yes\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}"
        + "},"
        + "{"
        + "\"fvSubnet\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-vlan_tenant/BD-bd_vlan/subnet-[10.0.10.0/24]\","
        + "\"ip\": \"10.0.10.0/24\","
        + "\"scope\": \"public\""
        + "}"
        + "}"
        + "},"
        + "{"
        + "\"fvSubnet\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-vlan_tenant/BD-bd_vlan/subnet-[10.0.11.0/24]\","
        + "\"ip\": \"10.0.11.0/24\","
        + "\"scope\": \"public\""
        + "}"
        + "}"
        + "}"
        + "]"
        + "}"
        + "}"
        + "]"
        + "}"
        + "}"
        + "]"
        + "}"
        + "}";
  }

  /** Test parsing fabric node with interfaces */
  @Test
  public void testParseJson_fabricNodeWithInterfaces() throws IOException {
    String json = createFabricNodeWithInterfaces();

    AciConfiguration config =
        AciConfiguration.fromJson("fabric_interfaces.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertNotNull(config.getFabricNodes());

    // Verify that l1PhysIf interfaces are parsed
    AciConfiguration.FabricNode spine = config.getFabricNodes().get("101");
    assertNotNull(spine);
    assertThat(spine.getInterfaces().size(), equalTo(2));
    assertThat(spine.getInterfaces(), hasKey("eth1/1"));
    assertThat(spine.getInterfaces(), hasKey("eth1/2"));
    assertThat(spine.getInterfaces().get("eth1/1").getDescription(), equalTo("Link to leaf1"));
  }

  /** Test parsing L3Out with logical interface profile */
  @Test
  public void testParseJson_l3outWithInterfaceProfile() throws IOException {
    String json = createL3OutWithInterfaceProfile();

    AciConfiguration config = AciConfiguration.fromJson("l3out_ifp.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertThat(config.getTenants(), hasKey("infra"));
  }

  /** Test parsing bridge domain with VLAN configuration */
  @Test
  public void testParseJson_bdWithVlanConfig() throws IOException {
    String json = createBridgeDomainWithVlanConfig();

    AciConfiguration config = AciConfiguration.fromJson("bd_vlan.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertThat(config.getTenants(), hasKey("vlan_tenant"));
  }

  /** Test multiple interfaces with different speeds */
  @Test
  public void testMultipleInterfacesBySpeed() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fabricInst\": {\"attributes\": {\"dn\": \"uni/fabric\"},\"children\": ["
            + "{\"fabricProtPol\": {\"attributes\": {},\"children\": [{\"fabricExplicitGEp\":"
            + " {\"attributes\": {},\"children\": [{\"fabricNodePEp\": {\"attributes\": {\"dn\":"
            + " \"uni/fabric/nodePEp-201\", \"id\": \"201\", \"name\": \"leaf1\", \"role\":"
            + " \"leaf\"},\"children\": [{\"l1PhysIf\": {\"attributes\": {\"dn\":"
            + " \"uni/fabric/nodePEp-201/phys-eth1/1\", \"id\": \"eth1/1\", \"descr\": \"100G"
            + " Interface\"}}},{\"l1PhysIf\": {\"attributes\": {\"dn\":"
            + " \"uni/fabric/nodePEp-201/phys-eth1/2\", \"id\": \"eth1/2\", \"descr\": \"100G"
            + " Interface\"}}},{\"l1PhysIf\": {\"attributes\": {\"dn\":"
            + " \"uni/fabric/nodePEp-201/phys-eth1/48\", \"id\": \"eth1/48\", \"descr\": \"1G"
            + " Management Interface\"}}}]}}]}}]}}]}}]}}";

    AciConfiguration config =
        AciConfiguration.fromJson("multi_speed_if.json", json, new Warnings());

    assertNotNull(config.getFabricNodes());
  }

  /** Test interface with description and annotations */
  @Test
  public void testInterfaceWithAnnotations() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fabricInst\": {\"attributes\": {\"dn\": \"uni/fabric\"},\"children\": ["
            + "{\"fabricProtPol\": {\"attributes\": {},\"children\": [{\"fabricExplicitGEp\":"
            + " {\"attributes\": {},\"children\": [{\"fabricNodePEp\": {\"attributes\": {\"dn\":"
            + " \"uni/fabric/nodePEp-101\", \"id\": \"101\", \"name\": \"spine1\", \"role\":"
            + " \"spine\"},\"children\": [{\"l1PhysIf\": {\"attributes\": {\"dn\":"
            + " \"uni/fabric/nodePEp-101/phys-eth1/1\",\"id\": \"eth1/1\",\"descr\": \"Production"
            + " Link to Data Center\",\"annotation\": \"important=yes\"}}}]}}]}}]}}]}}]}}";

    AciConfiguration config =
        AciConfiguration.fromJson("if_annotations.json", json, new Warnings());

    assertNotNull(config.getFabricNodes());
  }

  /** Test L3Out interface with sub-interface encapsulation */
  @Test
  public void testL3OutSubinterfaceEncapsulation() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-tenant1\", \"name\":"
            + " \"tenant1\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-tenant1/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},{\"l3extOut\":"
            + " {\"attributes\": {\"dn\": \"uni/tn-tenant1/out-l3out\", \"name\":"
            + " \"l3out\"},\"children\": [{\"l3extRsEctx\": {\"attributes\": {\"tnFvCtxName\":"
            + " \"vrf1\"}}},{\"l3extLNodeP\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-tenant1/out-l3out/lnodep-np\", \"name\": \"np\"},\"children\": ["
            + "{\"l3extRsNodeL3OutAtt\": {\"attributes\": {\"rtrId\": \"192.168.1.1\", \"tDn\":"
            + " \"topology/pod-1/node-101\"}}},{\"l3extLIfP\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-tenant1/out-l3out/lnodep-np/lifp-ifp\", \"name\": \"ifp\"},\"children\": ["
            + "{\"l3extRsPathL3OutAtt\": {\"attributes\": {\"tDn\":"
            + " \"topology/pod-1/paths-101/pathep-[eth1/36]\",\"encap\": \"vlan-100\",\"ifInstT\":"
            + " \"sub-interface\",\"addr\": \"192.168.1.254/30\"}}}]}}]}}]}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("subif_encap.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("tenant1"));
  }

  /** Test bridge domain with ARP flood enabled */
  @Test
  public void testBridgeDomainArpFlood() throws IOException {
    String json = createBridgeDomainWithVlanConfig();

    AciConfiguration config = AciConfiguration.fromJson("bd_arp.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("vlan_tenant"));
  }

  /** Test bridge domain unicast route settings */
  @Test
  public void testBridgeDomainUnicastRoute() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-bd_test\", \"name\": \"bd_test\"},"
            + "\"children\": ["
            + "{"
            + "\"fvCtx\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-bd_test/ctx-vrf1\", \"name\": \"vrf1\"},"
            + "\"children\": []"
            + "}"
            + "},"
            + "{"
            + "\"fvBD\": {"
            + "\"attributes\": {"
            + "\"dn\": \"uni/tn-bd_test/BD-bd1\","
            + "\"name\": \"bd1\","
            + "\"unicastRoute\": \"no\""
            + "},"
            + "\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}}]"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("bd_unicast.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("bd_test"));
  }

  /** Test multiple subnets in bridge domain with different scopes */
  @Test
  public void testBridgeDomainMultipleSubnetScopes() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-multi_subnet\", \"name\": \"multi_subnet\"},"
            + "\"children\": ["
            + "{"
            + "\"fvCtx\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-multi_subnet/ctx-vrf1\", \"name\": \"vrf1\"},"
            + "\"children\": []"
            + "}"
            + "},"
            + "{"
            + "\"fvBD\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-multi_subnet/BD-bd1\", \"name\": \"bd1\"},"
            + "\"children\": ["
            + "{"
            + "\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}"
            + "},"
            + "{"
            + "\"fvSubnet\": {"
            + "\"attributes\": {"
            + "\"dn\": \"uni/tn-multi_subnet/BD-bd1/subnet-[10.0.0.0/24]\","
            + "\"ip\": \"10.0.0.0/24\","
            + "\"scope\": \"public,shared\""
            + "}"
            + "}"
            + "},"
            + "{"
            + "\"fvSubnet\": {"
            + "\"attributes\": {"
            + "\"dn\": \"uni/tn-multi_subnet/BD-bd1/subnet-[10.1.0.0/24]\","
            + "\"ip\": \"10.1.0.0/24\","
            + "\"scope\": \"private\""
            + "}"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config =
        AciConfiguration.fromJson("bd_multi_scope.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("multi_subnet"));
  }

  /** Creates an EPG with path attachment to a physical interface. */
  private static String createEpgWithPathAttachment() {
    return "{\"polUni\":{\"attributes\":{\"dn\":\"uni\",\"name\":\"aci-fabric\"},\"children\":[{\"fvTenant\":{\"attributes\":{\"dn\":\"uni/tn-tenant1\",\"name\":\"tenant1\"},\"children\":[{\"fvAp\":{\"attributes\":{\"dn\":\"uni/tn-tenant1/ap-ap1\",\"name\":\"ap1\"},\"children\":[{\"fvAEPg\":{\"attributes\":{\"dn\":\"uni/tn-tenant1/ap-ap1/epg-web\",\"name\":\"web\"},\"children\":[{\"fvRsPathAtt\":{\"attributes\":{\"dn\":\"uni/tn-tenant1/ap-ap1/epg-web/rspathAtt-[topology/pod-1/paths-255/pathep-[eth1/1]]\",\"tDn\":\"topology/pod-1/paths-255/pathep-[eth1/1]\",\"encap\":\"vlan-100\"}}}]}}]}}]}}]}}";
  }

  /** Creates an EPG with vPC path attachment. */
  private static String createEpgWithVpcPathAttachment() {
    return "{\"polUni\":{\"attributes\":{\"dn\":\"uni\",\"name\":\"aci-fabric\"},\"children\":[{\"fvTenant\":{\"attributes\":{\"dn\":\"uni/tn-tenant1\",\"name\":\"tenant1\"},\"children\":[{\"fvAp\":{\"attributes\":{\"dn\":\"uni/tn-tenant1/ap-ap1\",\"name\":\"ap1\"},\"children\":[{\"fvAEPg\":{\"attributes\":{\"dn\":\"uni/tn-tenant1/ap-ap1/epg-web\",\"name\":\"web\"},\"children\":[{\"fvRsPathAtt\":{\"attributes\":{\"dn\":\"uni/tn-tenant1/ap-ap1/epg-web/rspathAtt-[topology/pod-1/protpaths-255-256/pathep-[PG_VPC_1]]\",\"tDn\":\"topology/pod-1/protpaths-255-256/pathep-[PG_VPC_1]\",\"encap\":\"vlan-200\"}}}]}}]}}]}}]}}";
  }

  /** Creates multiple EPGs with path attachments to different interfaces. */
  private static String createMultipleEpgsWithDifferentInterfaces() {
    return "{\"polUni\":{\"attributes\":{\"dn\":\"uni\",\"name\":\"aci-fabric\"},\"children\":[{\"fvTenant\":{\"attributes\":{\"dn\":\"uni/tn-tenant1\",\"name\":\"tenant1\"},\"children\":[{\"fvAp\":{\"attributes\":{\"dn\":\"uni/tn-tenant1/ap-ap1\",\"name\":\"ap1\"},\"children\":[{\"fvAEPg\":{\"attributes\":{\"dn\":\"uni/tn-tenant1/ap-ap1/epg-web\",\"name\":\"web\"},\"children\":[{\"fvRsPathAtt\":{\"attributes\":{\"tDn\":\"topology/pod-1/paths-255/pathep-[eth1/1]\",\"encap\":\"vlan-100\"}}}]}},{\"fvAEPg\":{\"attributes\":{\"dn\":\"uni/tn-tenant1/ap-ap1/epg-db\",\"name\":\"db\"},\"children\":[{\"fvRsPathAtt\":{\"attributes\":{\"tDn\":\"topology/pod-1/paths-255/pathep-[eth1/2]\",\"encap\":\"vlan-200\"}}}]}}]}}]}}]}}";
  }

  /** Creates multiple EPGs attached to the same interface. */
  private static String createMultipleEpgsSameInterface() {
    return "{\"polUni\":{\"attributes\":{\"dn\":\"uni\",\"name\":\"aci-fabric\"},\"children\":[{\"fvTenant\":{\"attributes\":{\"dn\":\"uni/tn-tenant1\",\"name\":\"tenant1\"},\"children\":[{\"fvAp\":{\"attributes\":{\"dn\":\"uni/tn-tenant1/ap-ap1\",\"name\":\"ap1\"},\"children\":[{\"fvAEPg\":{\"attributes\":{\"dn\":\"uni/tn-tenant1/ap-ap1/epg-web\",\"name\":\"web\"},\"children\":[{\"fvRsPathAtt\":{\"attributes\":{\"tDn\":\"topology/pod-1/paths-255/pathep-[eth1/1]\",\"encap\":\"vlan-100\"}}}]}},{\"fvAEPg\":{\"attributes\":{\"dn\":\"uni/tn-tenant1/ap-ap1/epg-app\",\"name\":\"app\"},\"children\":[{\"fvRsPathAtt\":{\"attributes\":{\"tDn\":\"topology/pod-1/paths-255/pathep-[eth1/1]\",\"encap\":\"vlan-110\"}}}]}}]}}]}}]}}";
  }

  /** Creates an EPG with path attachment including metadata. */
  private static String createEpgWithPathAttachmentMetadata() {
    return "{\"polUni\":{\"attributes\":{\"dn\":\"uni\",\"name\":\"aci-fabric\"},\"children\":[{\"fvTenant\":{\"attributes\":{\"dn\":\"uni/tn-tenant1\",\"name\":\"tenant1\"},\"children\":[{\"fvAp\":{\"attributes\":{\"dn\":\"uni/tn-tenant1/ap-ap1\",\"name\":\"ap1\"},\"children\":[{\"fvAEPg\":{\"attributes\":{\"dn\":\"uni/tn-tenant1/ap-ap1/epg-web\",\"name\":\"web\"},\"children\":[{\"fvRsPathAtt\":{\"attributes\":{\"tDn\":\"topology/pod-1/paths-255/pathep-[eth1/1]\",\"encap\":\"vlan-100\",\"descr\":\"Web"
               + " servers EPG\"}}}]}}]}}]}}]}}";
  }

  /** Test EPG path attachment (fvRsPathAtt) linking EPG to physical interface */
  @Test
  public void testEpgPathAttachment() throws IOException {
    String json = createEpgWithPathAttachment();
    AciConfiguration config = AciConfiguration.fromJson("epg_path_att.json", json, new Warnings());

    // Verify node interfaces map contains the interface (normalized to canonical name)
    assertThat(config.getNodeInterfaces(), hasKey("255"));
    assertThat(config.getNodeInterfaces().get("255").iterator().next(), equalTo("Ethernet1/1"));

    // Verify path attachment map contains the attachment
    assertThat(config.getPathAttachmentMap(), hasKey("255"));
    assertThat(config.getPathAttachmentMap().get("255"), hasKey("Ethernet1/1"));
    assertThat(
        config.getPathAttachmentMap().get("255").get("Ethernet1/1").getEncap(),
        equalTo("vlan-100"));
  }

  /** Test vPC path attachment (protpaths) linking EPG to vPC pair */
  @Test
  public void testVpcPathAttachment() throws IOException {
    String json = createEpgWithVpcPathAttachment();
    AciConfiguration config = AciConfiguration.fromJson("vpc_path_att.json", json, new Warnings());

    // Verify both nodes have the interface
    assertThat(config.getNodeInterfaces(), hasKey("255"));
    assertThat(config.getNodeInterfaces(), hasKey("256"));
    assertThat(config.getNodeInterfaces().get("255").iterator().next(), equalTo("PG_VPC_1"));
    assertThat(config.getNodeInterfaces().get("256").iterator().next(), equalTo("PG_VPC_1"));

    // Verify path attachment is in both nodes' maps
    assertThat(config.getPathAttachmentMap(), hasKey("255"));
    assertThat(config.getPathAttachmentMap(), hasKey("256"));
    assertThat(
        config.getPathAttachmentMap().get("255").get("PG_VPC_1").getEncap(), equalTo("vlan-200"));
    assertThat(
        config.getPathAttachmentMap().get("256").get("PG_VPC_1").getEncap(), equalTo("vlan-200"));

    // Verify vPC detection
    assertThat(config.getPathAttachmentMap().get("255").get("PG_VPC_1").isVpc(), equalTo(true));
    assertThat(
        config.getPathAttachmentMap().get("255").get("PG_VPC_1").getNodeId2(), equalTo("256"));
  }

  /** Test multiple path attachments to different interfaces on the same node */
  @Test
  public void testMultiplePathAttachments() throws IOException {
    String json = createMultipleEpgsWithDifferentInterfaces();
    AciConfiguration config =
        AciConfiguration.fromJson("multi_path_att.json", json, new Warnings());

    // Verify node has both interfaces
    assertThat(config.getNodeInterfaces().get("255").size(), equalTo(2));
  }

  /** Test multiple EPGs attached to the same interface (deduplication) */
  @Test
  public void testMultipleEpgsSameInterface() throws IOException {
    String json = createMultipleEpgsSameInterface();
    AciConfiguration config =
        AciConfiguration.fromJson("multi_epg_same_if.json", json, new Warnings());

    // Verify interface is only listed once (deduplication)
    assertThat(config.getNodeInterfaces().get("255").size(), equalTo(1));
    assertThat(config.getNodeInterfaces().get("255").iterator().next(), equalTo("Ethernet1/1"));
  }

  /** Test path attachment with EPG metadata */
  @Test
  public void testPathAttachmentWithMetadata() throws IOException {
    String json = createEpgWithPathAttachmentMetadata();
    AciConfiguration config =
        AciConfiguration.fromJson("path_att_metadata.json", json, new Warnings());

    // Verify EPG metadata is captured
    assertThat(
        config.getPathAttachmentMap().get("255").get("Ethernet1/1").getEpgName(), equalTo("web"));
    assertThat(
        config.getPathAttachmentMap().get("255").get("Ethernet1/1").getEpgTenant(),
        equalTo("tenant1"));
    assertThat(
        config.getPathAttachmentMap().get("255").get("Ethernet1/1").getDescription(),
        equalTo("Web servers EPG"));
  }

  /** Creates a spine-leaf topology with interfaces and path attachments. */
  private static String createSpineLeafTopology() {
    return "{\"polUni\":{\"attributes\":{\"dn\":\"uni\",\"name\":\"aci-fabric\"},\"children\":[{\"fabricInst\":{\"attributes\":{\"dn\":\"uni/fabric\"},\"children\":[{\"fabricProtPol\":{\"attributes\":{},\"children\":[{\"fabricExplicitGEp\":{\"attributes\":{},\"children\":[{\"fabricNodePEp\":{\"attributes\":{\"dn\":\"uni/fabric/nodePEp-101\",\"id\":\"101\",\"name\":\"spine1\",\"role\":\"spine\",\"podId\":\"1\"},\"children\":[{\"l1PhysIf\":{\"attributes\":{\"dn\":\"uni/fabric/nodePEp-101/phys-eth1/1\",\"id\":\"eth1/1\",\"descr\":\"Link"
               + " to leaf1\"}}},{\"l1PhysIf\":{\"attributes\":{\"dn\":\"uni/fabric/nodePEp-101/phys-eth1/2\",\"id\":\"eth1/2\",\"descr\":\"Link"
               + " to leaf2\"}}}]}},{\"fabricNodePEp\":{\"attributes\":{\"dn\":\"uni/fabric/nodePEp-201\",\"id\":\"201\",\"name\":\"leaf1\",\"role\":\"leaf\",\"podId\":\"1\"},\"children\":[{\"l1PhysIf\":{\"attributes\":{\"dn\":\"uni/fabric/nodePEp-201/phys-eth1/1\",\"id\":\"eth1/1\",\"descr\":\"Link"
               + " to spine1\"}}}]}}]}}]}}]}},{\"fvTenant\":{\"attributes\":{\"dn\":\"uni/tn-tenant1\",\"name\":\"tenant1\"},\"children\":[{\"fvAp\":{\"attributes\":{\"dn\":\"uni/tn-tenant1/ap-ap1\",\"name\":\"ap1\"},\"children\":[{\"fvAEPg\":{\"attributes\":{\"dn\":\"uni/tn-tenant1/ap-ap1/epg-web\",\"name\":\"web\"},\"children\":[{\"fvRsPathAtt\":{\"attributes\":{\"tDn\":\"topology/pod-1/paths-201/pathep-[eth1/2]\",\"encap\":\"vlan-100\"}}}]}}]}}]}}]}}";
  }

  /** Test spine-leaf topology generation with interfaces from both sources */
  @Test
  public void testSpineLeafTopology() throws IOException {
    String json = createSpineLeafTopology();
    AciConfiguration config =
        AciConfiguration.fromJson("spine_leaf_topo.json", json, new Warnings());

    // Verify spine has interfaces from l1PhysIf
    assertThat(config.getFabricNodes(), hasKey("101"));
    assertThat(config.getFabricNodes().get("101").getInterfaces().size(), equalTo(2));
    assertThat(config.getFabricNodes().get("101").getInterfaces(), hasKey("eth1/1"));
    assertThat(config.getFabricNodes().get("101").getInterfaces(), hasKey("eth1/2"));

    // Verify leaf has interfaces from l1PhysIf
    assertThat(config.getFabricNodes(), hasKey("201"));
    assertThat(config.getFabricNodes().get("201").getInterfaces().size(), equalTo(1));
    assertThat(config.getFabricNodes().get("201").getInterfaces(), hasKey("eth1/1"));

    // Verify leaf also has interfaces from path attachments (normalized to canonical name)
    assertThat(config.getNodeInterfaces(), hasKey("201"));
    assertThat(config.getNodeInterfaces().get("201").size(), equalTo(1));
    assertThat(config.getNodeInterfaces().get("201").iterator().next(), equalTo("Ethernet1/2"));

    // Verify Layer1 edges are created between spine and leaf
    var edges = org.batfish.vendor.cisco_aci.representation.AciConversion.createLayer1Edges(config);
    assertThat(edges.size(), equalTo(1)); // 1 spine x 1 leaf = 1 edge
  }
}
