package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.vendor.cisco_aci.representation.BgpProcess;
import org.batfish.vendor.cisco_aci.representation.BridgeDomain;
import org.batfish.vendor.cisco_aci.representation.Contract;
import org.batfish.vendor.cisco_aci.representation.ContractInterface;
import org.batfish.vendor.cisco_aci.representation.Epg;
import org.batfish.vendor.cisco_aci.representation.FabricLink;
import org.batfish.vendor.cisco_aci.representation.FilterModel;
import org.batfish.vendor.cisco_aci.representation.InterFabricConnection;
import org.batfish.vendor.cisco_aci.representation.L3OutPathAttachment;
import org.batfish.vendor.cisco_aci.representation.ManagementInfo;
import org.batfish.vendor.cisco_aci.representation.TabooContract;
import org.batfish.vendor.cisco_aci.representation.Tenant;
import org.batfish.vendor.cisco_aci.representation.TenantVrf;
import org.batfish.vendor.cisco_aci.representation.VpcPair;
import org.batfish.vendor.cisco_aci.representation.apic.AciAttributes;
import org.batfish.vendor.cisco_aci.representation.apic.AciBgpPeerP;
import org.batfish.vendor.cisco_aci.representation.apic.AciBgpRouteTargetProfile;
import org.batfish.vendor.cisco_aci.representation.apic.AciChild;
import org.batfish.vendor.cisco_aci.representation.apic.AciContract;
import org.batfish.vendor.cisco_aci.representation.apic.AciCtrlrInst;
import org.batfish.vendor.cisco_aci.representation.apic.AciFabricExplicitGEp;
import org.batfish.vendor.cisco_aci.representation.apic.AciFabricInterface;
import org.batfish.vendor.cisco_aci.representation.apic.AciFabricProtPol;
import org.batfish.vendor.cisco_aci.representation.apic.AciFilter;
import org.batfish.vendor.cisco_aci.representation.apic.AciL2Out;
import org.batfish.vendor.cisco_aci.representation.apic.AciL3LogicalNodeProfile;
import org.batfish.vendor.cisco_aci.representation.apic.AciPolUniInternal;
import org.batfish.vendor.cisco_aci.representation.apic.AciVrf;
import org.junit.Test;

/** Additional model coverage for recently renamed classes and APIC DTO deserialization. */
public final class AciRepresentationModelCoverageTest {

  @Test
  public void testFabricLinkModel() {
    FabricLink link = new FabricLink("201", "Ethernet1/50", "101", "Ethernet1/24");
    link.setLinkState("up");
    link.setNode1Id("202");
    link.setNode1Interface("Ethernet1/51");

    assertThat(link.getNode1Id(), equalTo("202"));
    assertThat(link.getNode1Interface(), equalTo("Ethernet1/51"));
    assertThat(link.getNode2Id(), equalTo("101"));
    assertThat(link.getNode2Interface(), equalTo("Ethernet1/24"));
    assertThat(link.getLinkState(), equalTo("up"));
  }

  @Test
  public void testVpcPairModel() {
    VpcPair pair = new VpcPair("10", "vpc10", "201", "202");
    pair.setVpcName("vpc-domain-10");

    assertThat(pair.getVpcId(), equalTo("10"));
    assertThat(pair.getVpcName(), equalTo("vpc-domain-10"));
    assertThat(pair.getPeer1NodeId(), equalTo("201"));
    assertThat(pair.getPeer2NodeId(), equalTo("202"));
  }

  @Test
  public void testManagementInfoModel() {
    ManagementInfo info = new ManagementInfo();
    info.setAddress("10.0.0.2/24");
    info.setGateway("10.0.0.1");
    info.setAddress6("2001:db8::2/64");
    info.setGateway6("2001:db8::1");

    assertThat(info.getAddress(), equalTo("10.0.0.2/24"));
    assertThat(info.getGateway(), equalTo("10.0.0.1"));
    assertThat(info.getAddress6(), equalTo("2001:db8::2/64"));
    assertThat(info.getGateway6(), equalTo("2001:db8::1"));
  }

  @Test
  public void testInterFabricConnectionModel() {
    InterFabricConnection connection =
        new InterFabricConnection("dc1", "dc2", "bgp", "inter-fabric");
    connection.addBgpPeer("192.0.2.1");
    connection.addSharedSubnet("10.10.0.0/16");

    assertThat(connection.getFabric1(), equalTo("dc1"));
    assertThat(connection.getFabric2(), equalTo("dc2"));
    assertThat(connection.getConnectionType(), equalTo("bgp"));
    assertThat(connection.getDescription(), equalTo("inter-fabric"));
    assertThat(connection.getBgpPeers(), hasSize(1));
    assertThat(connection.getBgpPeers().get(0), equalTo("192.0.2.1"));
    assertThat(connection.getSharedSubnets(), hasSize(1));
    assertThat(connection.getSharedSubnets().get(0), equalTo("10.10.0.0/16"));
  }

  @Test
  public void testAciPolUniInternalDeserializer() throws Exception {
    String json =
        "{\"attributes\":{\"name\":\"aci-fabric\"},\"children\":["
            + " {\"fvTenant\":{\"attributes\":{\"name\":\"tenant1\"}}},"
            + " {\"fabricInst\":{\"attributes\":{\"dn\":\"uni/fabric\"},   \"children\":[    "
            + " {\"fabricNodeIdentPol\":{\"children\":[      "
            + " {\"fabricNodeIdentP\":{\"attributes\":{\"nodeId\":\"101\",\"name\":\"leaf101\"}}}  "
            + "   ]}}   ] }}]}";

    AciPolUniInternal polUni =
        BatfishObjectMapper.mapper().readValue(json, AciPolUniInternal.class);
    assertThat(polUni, notNullValue());
    assertThat(polUni.getAttributes(), notNullValue());
    assertThat(polUni.getAttributes().getName(), equalTo("aci-fabric"));
    assertThat(polUni.getChildren(), hasSize(2));
    assertThat(polUni.getChildren().get(0).getFvTenant(), notNullValue());
    assertThat(
        polUni.getChildren().get(0).getFvTenant().getAttributes().getName(), equalTo("tenant1"));
    assertThat(polUni.getChildren().get(1).getFabricInst(), notNullValue());
  }

  @Test
  public void testL3OutPathAttachmentModel() {
    L3OutPathAttachment path = new L3OutPathAttachment();
    path.setTargetDn("topology/pod-1/paths-201/pathep-[eth1/1]");
    path.setEncapsulation("vlan-200");
    path.setAddress("192.0.2.1/31");
    path.setMac("00:11:22:33:44:55");
    path.setMode("regular");
    path.setInterfaceType("ext-svi");
    path.setNodeId("201");
    path.setInterfaceName("eth1/1");
    path.setDescription("uplink");

    assertThat(path.getTargetDn(), equalTo("topology/pod-1/paths-201/pathep-[eth1/1]"));
    assertThat(path.getEncapsulation(), equalTo("vlan-200"));
    assertThat(path.getAddress(), equalTo("192.0.2.1/31"));
    assertThat(path.getMac(), equalTo("00:11:22:33:44:55"));
    assertThat(path.getMode(), equalTo("regular"));
    assertThat(path.getInterfaceType(), equalTo("ext-svi"));
    assertThat(path.getNodeId(), equalTo("201"));
    assertThat(path.getInterfaceName(), equalTo("eth1/1"));
    assertThat(path.getDescription(), equalTo("uplink"));
  }

  @Test
  public void testTenantAndBgpProcessModels() {
    Tenant tenant = new Tenant("tenant1");
    tenant.setBridgeDomains(java.util.Map.of("bd1", new BridgeDomain("bd1")));
    tenant.setVrfs(java.util.Map.of("vrf1", new TenantVrf("vrf1")));
    tenant.setEpgs(java.util.Map.of("epg1", new Epg("epg1")));
    tenant.setContracts(java.util.Map.of("c1", new Contract("c1")));
    tenant.setContractInterfaces(java.util.Map.of("ci1", new ContractInterface("ci1")));
    tenant.setFilters(java.util.Map.of("f1", new FilterModel("f1")));
    tenant.setTabooContracts(java.util.Map.of("t1", new TabooContract("t1")));
    tenant.setApplicationProfiles(
        java.util.Map.of(
            "ap1", new org.batfish.vendor.cisco_aci.representation.ApplicationProfile("ap1")));
    tenant.getL3OutNames().add("l3out1");

    assertThat(tenant.getName(), equalTo("tenant1"));
    assertThat(tenant.getBridgeDomains().size(), equalTo(1));
    assertThat(tenant.getVrfs().size(), equalTo(1));
    assertThat(tenant.getEpgs().size(), equalTo(1));
    assertThat(tenant.getContracts().size(), equalTo(1));
    assertThat(tenant.getContractInterfaces().size(), equalTo(1));
    assertThat(tenant.getFilters().size(), equalTo(1));
    assertThat(tenant.getTabooContracts().size(), equalTo(1));
    assertThat(tenant.getApplicationProfiles().size(), equalTo(1));
    assertThat(tenant.getL3OutNames(), hasSize(1));

    BgpProcess bgp = new BgpProcess();
    bgp.setAs(65001L);
    bgp.setRouterId("1.1.1.1");
    bgp.setEbgpAdminCost(20);
    bgp.setIbgpAdminCost(200);
    bgp.setVrfAdminCost(220);
    bgp.setKeepalive(30);
    bgp.setHoldTime(90);

    assertThat(bgp.getAs(), equalTo(65001L));
    assertThat(bgp.getRouterId(), equalTo("1.1.1.1"));
    assertThat(bgp.getEbgpAdminCost(), equalTo(20));
    assertThat(bgp.getIbgpAdminCost(), equalTo(200));
    assertThat(bgp.getVrfAdminCost(), equalTo(220));
    assertThat(bgp.getKeepalive(), equalTo(30));
    assertThat(bgp.getHoldTime(), equalTo(90));
  }

  @Test
  public void testAciChildAndCtrlrInstModels() {
    AciAttributes attributes = new AciAttributes();
    attributes.put("name", "tenant1");
    AciChild nested = new AciChild();
    nested.setClassName("fvCtx");
    nested.setAttributes(attributes);
    AciChild parent = new AciChild();
    parent.setAttributes(attributes);
    parent.setChildren(ImmutableList.of(nested));
    parent.setAnyProperty("fvTenant", nested);

    assertThat(parent.getClassName(), equalTo("fvTenant"));
    assertThat(parent.getAttributes(), notNullValue());
    assertThat(parent.getChildren(), nullValue());

    AciCtrlrInst ctrlrInst = new AciCtrlrInst();
    AciCtrlrInst.CtrlrInstChild child = new AciCtrlrInst.CtrlrInstChild();
    child.setFabricNodeIdentPol(
        new org.batfish.vendor.cisco_aci.representation.apic.AciFabricNodeIdentPol());
    ctrlrInst.setChildren(ImmutableList.of(child));
    assertThat(ctrlrInst.getChildren(), hasSize(1));
    assertThat(ctrlrInst.getChildren().get(0).getFabricNodeIdentPol(), notNullValue());
  }

  @Test
  public void testApicModelWrappers() {
    AciFabricInterface.AciFabricInterfaceAttributes fia =
        new AciFabricInterface.AciFabricInterfaceAttributes();
    fia.setAnnotation("anno");
    fia.setDescription("desc");
    fia.setDistinguishedName("dn");
    fia.setId("id");
    fia.setName("eth1/1");
    fia.setNameAlias("alias");
    fia.setUserDomain("all");
    AciFabricInterface fi = new AciFabricInterface();
    fi.setAttributes(fia);
    assertThat(fi.getAttributes().getName(), equalTo("eth1/1"));

    AciL2Out.AciL2OutAttributes l2attrs = new AciL2Out.AciL2OutAttributes();
    l2attrs.setName("l2out");
    l2attrs.setAnnotation("anno");
    l2attrs.setDescription("desc");
    l2attrs.setDistinguishedName("dn");
    l2attrs.setNameAlias("alias");
    l2attrs.setOwnerKey("ok");
    l2attrs.setOwnerTag("ot");
    l2attrs.setTargetDscp("AF11");
    l2attrs.setUserDomain("all");
    AciL2Out l2out = new AciL2Out();
    l2out.setAttributes(l2attrs);
    l2out.setChildren(ImmutableList.of("child"));
    assertThat(l2out.getAttributes().getTargetDscp(), equalTo("AF11"));
    assertThat(l2out.getChildren(), hasSize(1));

    AciBgpRouteTargetProfile.AciBgpRouteTargetProfileAttributes rtAttrs =
        new AciBgpRouteTargetProfile.AciBgpRouteTargetProfileAttributes();
    rtAttrs.setName("rt-pol");
    rtAttrs.setAddressTypeControl("ctrl");
    rtAttrs.setAnnotation("anno");
    rtAttrs.setAttributeMap("map");
    rtAttrs.setControl("control");
    rtAttrs.setDescription("desc");
    rtAttrs.setDistinguishedName("dn");
    rtAttrs.setNameAlias("alias");
    rtAttrs.setOwnerKey("ok");
    rtAttrs.setOwnerTag("ot");
    rtAttrs.setUserDomain("all");
    AciBgpRouteTargetProfile rtProfile = new AciBgpRouteTargetProfile();
    rtProfile.setAttributes(rtAttrs);
    rtProfile.setChildren(ImmutableList.of("child"));
    assertThat(rtProfile.getAttributes().getAttributeMap(), equalTo("map"));
    assertThat(rtProfile.getChildren(), hasSize(1));

    AciFilter.AciFilterAttributes filterAttrs = new AciFilter.AciFilterAttributes();
    filterAttrs.setName("f1");
    filterAttrs.setAnnotation("anno");
    filterAttrs.setDescription("desc");
    filterAttrs.setDistinguishedName("dn");
    filterAttrs.setNameAlias("alias");
    filterAttrs.setOwnerKey("ok");
    filterAttrs.setOwnerTag("ot");
    filterAttrs.setUserDomain("all");
    AciFilter filter = new AciFilter();
    filter.setAttributes(filterAttrs);
    filter.setChildren(ImmutableList.of("entry"));
    assertThat(filter.getAttributes().getName(), equalTo("f1"));
    assertThat(filter.getChildren(), hasSize(1));

    AciBgpPeerP.AciBgpPeerPAttributes peerAttrs = new AciBgpPeerP.AciBgpPeerPAttributes();
    peerAttrs.setAction("restart");
    peerAttrs.setAnnotation("anno");
    peerAttrs.setDescription("desc");
    peerAttrs.setDistinguishedName("dn");
    peerAttrs.setMaxPrefixes("1000");
    peerAttrs.setName("peer-pol");
    peerAttrs.setNameAlias("alias");
    peerAttrs.setOwnerKey("ok");
    peerAttrs.setOwnerTag("ot");
    peerAttrs.setRestartTime("15");
    peerAttrs.setThreshold("80");
    peerAttrs.setUserDomain("all");
    AciBgpPeerP peerP = new AciBgpPeerP();
    peerP.setAttributes(peerAttrs);
    peerP.setChildren(ImmutableList.of("child"));
    assertThat(peerP.getAttributes().getThreshold(), equalTo("80"));
    assertThat(peerP.getChildren(), hasSize(1));

    AciFabricProtPol.AciFabricProtPolAttributes protAttrs =
        new AciFabricProtPol.AciFabricProtPolAttributes();
    protAttrs.setAnnotation("anno");
    protAttrs.setDistinguishedName("dn");
    protAttrs.setName("prot");
    protAttrs.setNameAlias("alias");
    protAttrs.setOwnerKey("ok");
    protAttrs.setOwnerTag("ot");
    protAttrs.setUserDomain("all");
    AciFabricProtPol.FabricProtPolChild protChild = new AciFabricProtPol.FabricProtPolChild();
    protChild.setFabricExplicitGEp(new AciFabricExplicitGEp());
    AciFabricProtPol protPol = new AciFabricProtPol();
    protPol.setAttributes(protAttrs);
    protPol.setChildren(ImmutableList.of(protChild));
    assertThat(protPol.getAttributes().getName(), equalTo("prot"));
    assertThat(protPol.getChildren(), hasSize(1));
    assertThat(protPol.getChildren().get(0).getFabricExplicitGEp(), notNullValue());

    AciVrf.AciVrfAttributes vrfAttrs = new AciVrf.AciVrfAttributes();
    vrfAttrs.setAnnotation("anno");
    vrfAttrs.setBdEnforcedEnable("yes");
    vrfAttrs.setDescription("desc");
    vrfAttrs.setDistinguishedName("dn");
    vrfAttrs.setIpDataPlaneLearning("enabled");
    vrfAttrs.setKnownMcastAction("flood");
    vrfAttrs.setName("vrf1");
    vrfAttrs.setNameAlias("alias");
    vrfAttrs.setOwnerKey("ok");
    vrfAttrs.setOwnerTag("ot");
    vrfAttrs.setPolicyEnforcementDirection("ingress");
    vrfAttrs.setPolicyEnforcementPreference("enforced");
    vrfAttrs.setUserDomain("all");
    vrfAttrs.setVrfIndex("123");
    AciVrf vrf = new AciVrf();
    vrf.setAttributes(vrfAttrs);
    vrf.setChildren(ImmutableList.of("child"));
    assertThat(vrf.getAttributes().getVrfIndex(), equalTo("123"));
    assertThat(vrf.getChildren(), hasSize(1));

    AciContract.AciContractAttributes contractAttrs = new AciContract.AciContractAttributes();
    contractAttrs.setAnnotation("anno");
    contractAttrs.setDescription("desc");
    contractAttrs.setDistinguishedName("dn");
    contractAttrs.setIntent("install");
    contractAttrs.setName("c1");
    contractAttrs.setNameAlias("alias");
    contractAttrs.setOwnerKey("ok");
    contractAttrs.setOwnerTag("ot");
    contractAttrs.setPriority("level1");
    contractAttrs.setScope("context");
    contractAttrs.setTargetDscp("CS1");
    contractAttrs.setUserDomain("all");
    AciContract contract = new AciContract();
    contract.setAttributes(contractAttrs);
    contract.setChildren(ImmutableList.of("subject"));
    assertThat(contract.getAttributes().getScope(), equalTo("context"));
    assertThat(contract.getChildren(), hasSize(1));

    AciL3LogicalNodeProfile.AciL3LogicalNodeProfileAttributes l3NodeAttrs =
        new AciL3LogicalNodeProfile.AciL3LogicalNodeProfileAttributes();
    l3NodeAttrs.setAnnotation("anno");
    l3NodeAttrs.setConfigIssues("none");
    l3NodeAttrs.setDescription("desc");
    l3NodeAttrs.setDistinguishedName("dn");
    l3NodeAttrs.setName("lnodep1");
    l3NodeAttrs.setNameAlias("alias");
    l3NodeAttrs.setOwnerKey("ok");
    l3NodeAttrs.setOwnerTag("ot");
    l3NodeAttrs.setTag("external");
    l3NodeAttrs.setTargetDscp("CS2");
    l3NodeAttrs.setUserDomain("all");
    AciL3LogicalNodeProfile l3NodeProfile = new AciL3LogicalNodeProfile();
    l3NodeProfile.setAttributes(l3NodeAttrs);
    l3NodeProfile.setChildren(ImmutableList.of("child"));
    assertThat(l3NodeProfile.getAttributes().getConfigIssues(), equalTo("none"));
    assertThat(l3NodeProfile.getAttributes().getTag(), equalTo("external"));
    assertThat(l3NodeProfile.getChildren(), hasSize(1));

    assertTrue(fi.getAttributes().getAnnotation().equals("anno"));
  }
}
