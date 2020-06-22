package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsConfiguration.LINK_LOCAL_IP;
import static org.batfish.representation.aws.TransitGateway.receiveSidePeeringInterfaceName;
import static org.batfish.representation.aws.TransitGateway.sendSidePeeringInterfaceName;
import static org.batfish.representation.aws.TransitGateway.vrfNameForRouteTable;
import static org.batfish.representation.aws.TransitGatewayPeeringConnector.connectSenderReceiver;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.batfish.common.Warnings;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.Vrf;
import org.batfish.representation.aws.TransitGateway.TransitGatewayOptions;
import org.batfish.representation.aws.TransitGatewayAttachment.Association;
import org.batfish.representation.aws.TransitGatewayAttachment.ResourceType;
import org.junit.Test;

public class TransitGatewayPeeringConnectorTest {

  @VisibleForTesting
  static boolean arePeers(
      TransitGatewayAttachment attachment1, TransitGatewayAttachment attachment2) {
    return attachment1.getGatewayOwnerId().equals(attachment2.getResourceOwnerId())
        && attachment1.getResourceOwnerId().equals(attachment2.getGatewayOwnerId())
        && attachment1.getGatewayId().equals(attachment2.getResourceId())
        && attachment1.getResourceId().equals(attachment2.getGatewayId())
        && attachment1.getId().equals(attachment2.getId());
  }

  @Test
  public void testArePeers() {
    TransitGatewayAttachment attachment1 =
        new TransitGatewayAttachment(
            "attach", "tgw", "myAcc", ResourceType.PEERING, "resId", "resAcc", null);

    assertTrue(
        "matching peers",
        arePeers(
            attachment1,
            new TransitGatewayAttachment(
                "attach", "resId", "resAcc", ResourceType.PEERING, "tgw", "myAcc", null)));

    assertFalse(
        "wrong attachment id",
        arePeers(
            attachment1,
            new TransitGatewayAttachment(
                "other", "resId", "resAcc", ResourceType.PEERING, "tgw", "myAcc", null)));
    assertFalse(
        "wrong gateway id",
        arePeers(
            attachment1,
            new TransitGatewayAttachment(
                "attach", "other", "resAcc", ResourceType.PEERING, "tgw", "myAcc", null)));
    assertFalse(
        "wrong gateway owner (acccount) id",
        arePeers(
            attachment1,
            new TransitGatewayAttachment(
                "attach", "resId", "other", ResourceType.PEERING, "tgw", "myAcc", null)));
    assertFalse(
        "wrong resource id ",
        arePeers(
            attachment1,
            new TransitGatewayAttachment(
                "attach", "resId", "resAcc", ResourceType.PEERING, "other", "myAcc", null)));
    assertFalse(
        "wrong resource owner id ",
        arePeers(
            attachment1,
            new TransitGatewayAttachment(
                "attach", "resId", "resAcc", ResourceType.PEERING, "tgw", "other", null)));
  }

  @Test
  public void testConnectSenderReceiver() {
    Configuration sendTgwCfg = Utils.newAwsConfiguration("tgw-send", "aws");
    Configuration recvTgwCfg = Utils.newAwsConfiguration("tgw-recv", "aws");

    TransitGatewayRouteTable sendRoutingTable1 =
        new TransitGatewayRouteTable("rtb-send-1", sendTgwCfg.getHostname(), true, true);
    TransitGatewayRouteTable sendRoutingTable2 =
        new TransitGatewayRouteTable("rtb-send-2", sendTgwCfg.getHostname(), true, true);
    String associatedRouteTableId = "rtb-recv-associated";

    TransitGatewayAttachment sendSideAttachment =
        new TransitGatewayAttachment(
            "attach",
            sendTgwCfg.getHostname(),
            "account-send",
            ResourceType.PEERING,
            recvTgwCfg.getHostname(),
            "account-recv",
            null);
    TransitGatewayAttachment recvSideAttachment =
        new TransitGatewayAttachment(
            sendSideAttachment.getId(),
            recvTgwCfg.getHostname(),
            sendSideAttachment.getResourceOwnerId(),
            ResourceType.PEERING,
            sendTgwCfg.getHostname(),
            sendSideAttachment.getGatewayOwnerId(),
            new Association(associatedRouteTableId, "associated"));

    // create VRFs and interfaces on the send side TGW
    Vrf.builder()
        .setOwner(sendTgwCfg)
        .setName(vrfNameForRouteTable(sendRoutingTable1.getId()))
        .build();
    Utils.newInterface(
        sendSidePeeringInterfaceName(sendRoutingTable1.getId(), sendSideAttachment.getId()),
        sendTgwCfg,
        vrfNameForRouteTable(sendRoutingTable1.getId()),
        LinkLocalAddress.of(LINK_LOCAL_IP),
        "test");
    Vrf.builder()
        .setOwner(sendTgwCfg)
        .setName(vrfNameForRouteTable(sendRoutingTable2.getId()))
        .build();
    Utils.newInterface(
        sendSidePeeringInterfaceName(sendRoutingTable2.getId(), sendSideAttachment.getId()),
        sendTgwCfg,
        vrfNameForRouteTable(sendRoutingTable2.getId()),
        LinkLocalAddress.of(LINK_LOCAL_IP),
        "test");

    // VRF on the receive side
    Vrf.builder()
        .setOwner(recvTgwCfg)
        .setName(vrfNameForRouteTable(associatedRouteTableId))
        .build();

    ConvertedConfiguration viConfigs = new ConvertedConfiguration();
    viConfigs.addNode(sendTgwCfg);
    viConfigs.addNode(recvTgwCfg);

    TransitGateway sendTgw =
        new TransitGateway(
            sendTgwCfg.getHostname(),
            new TransitGatewayOptions(1L, false, "rt", false, "rt", false),
            sendSideAttachment.getGatewayOwnerId(),
            ImmutableMap.of());

    Region sendSideRegion =
        Region.builder("r1")
            .setTransitGatewayRouteTables(
                ImmutableMap.of(
                    sendRoutingTable1.getId(),
                    sendRoutingTable1,
                    sendRoutingTable2.getId(),
                    sendRoutingTable2))
            .setTransitGateways(ImmutableMap.of(sendTgw.getId(), sendTgw))
            .build();

    AwsConfiguration vsConfig = new AwsConfiguration();
    vsConfig.addOrGetAccount(sendSideAttachment.getGatewayOwnerId()).addRegion(sendSideRegion);

    connectSenderReceiver(
        sendSideAttachment, recvSideAttachment, vsConfig, viConfigs, new Warnings());

    ImmutableList.of(sendRoutingTable1, sendRoutingTable2)
        .forEach(
            rt -> {
              String recvSideInterfaceName =
                  receiveSidePeeringInterfaceName(
                      rt.getId(), sendSideAttachment.getId(), associatedRouteTableId);
              assertTrue(recvTgwCfg.getAllInterfaces().containsKey(recvSideInterfaceName));
              assertTrue(
                  viConfigs
                      .getLayer1Edges()
                      .contains(
                          new Layer1Edge(
                              sendTgwCfg.getHostname(),
                              sendSidePeeringInterfaceName(rt.getId(), sendSideAttachment.getId()),
                              recvTgwCfg.getHostname(),
                              recvSideInterfaceName)));
            });
  }
}
