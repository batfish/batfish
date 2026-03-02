package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.vendor.cisco_aci.representation.FabricLink;
import org.batfish.vendor.cisco_aci.representation.InterFabricConnection;
import org.batfish.vendor.cisco_aci.representation.ManagementInfo;
import org.batfish.vendor.cisco_aci.representation.VpcPair;
import org.batfish.vendor.cisco_aci.representation.apic.AciPolUniInternal;
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
}
