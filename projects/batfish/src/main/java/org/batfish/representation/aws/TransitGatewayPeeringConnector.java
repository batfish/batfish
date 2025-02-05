package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsConfiguration.LINK_LOCAL_IP;
import static org.batfish.representation.aws.TransitGateway.receiveSidePeeringInterfaceName;
import static org.batfish.representation.aws.TransitGateway.sendSidePeeringInterfaceName;
import static org.batfish.representation.aws.TransitGateway.vrfNameForRouteTable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.representation.aws.TransitGatewayAttachment.ResourceType;

/** A utility class to make VI links for TGW peerings. */
@ParametersAreNonnullByDefault
class TransitGatewayPeeringConnector {

  /**
   * The main function that connects TGWs that peer with each other. Before it is called the
   * following must have been created:
   *
   * <ul>
   *   <li>VI nodes for TGWs
   *   <li>Interfaces for the send direction for traffic on each TGW. Each TGW is a sender and
   *       receiver for different directions of traffic. During TGW creation, we create the
   *       send-side interfaces.
   *   <li>Static routes pointing to these interfaces
   * </ul>
   *
   * This function will create the receive-side interfaces and connect the two ends.
   */
  public static void connect(
      AwsConfiguration vsConfigs, ConvertedConfiguration viConfigs, Warnings warnings) {
    Set<TransitGatewayAttachment> peeringAttachments =
        vsConfigs.getAccounts().stream()
            .flatMap(a -> a.getRegions().stream())
            .flatMap(r -> r.getTransitGatewayAttachments().values().stream())
            .filter(attachment -> attachment.getResourceType() == ResourceType.PEERING)
            .collect(ImmutableSet.toImmutableSet());

    peeringAttachments.forEach(
        sendSideAttachment -> {
          Set<TransitGatewayAttachment> peers =
              peeringAttachments.stream()
                  .filter(attachment -> arePeers(sendSideAttachment, attachment))
                  .collect(ImmutableSet.toImmutableSet());
          if (peers.isEmpty()) {
            warnings.redFlagf("Matching peer for TGW attachment %s not found", sendSideAttachment);
          } else if (peers.size() > 1) {
            warnings.redFlagf(
                "Multiple matching peers found for TGW attachment %s: %s. Connecting to all of"
                    + " them.",
                sendSideAttachment, peers);
          }
          peers.forEach(
              peer ->
                  connectSenderReceiver(sendSideAttachment, peer, vsConfigs, viConfigs, warnings));
        });
  }

  /**
   * Create what is needed for traffic from send side to flow from the receive side. This entails
   * creating receive-side interfaces and connecting them to send-side interfaces already created
   * (during TGW creation).
   */
  @VisibleForTesting
  static void connectSenderReceiver(
      TransitGatewayAttachment sendSideAttachment,
      TransitGatewayAttachment recvSideAttachment,
      AwsConfiguration vsConfigs,
      ConvertedConfiguration viConfigs,
      Warnings warnings) {
    Configuration sendSideTgw = viConfigs.getNode(sendSideAttachment.getGatewayId());
    if (sendSideTgw == null) {
      warnings.redFlagf("Configuration not found for TGW %s", sendSideAttachment.getGatewayId());
      return;
    }

    Configuration recvSideTgw = viConfigs.getNode(recvSideAttachment.getGatewayId());
    if (recvSideTgw == null) {
      warnings.redFlagf("Configuration not found for TGW %s", recvSideAttachment.getGatewayId());
      return;
    }

    String associatedRouteTableId =
        recvSideAttachment.getAssociation() == null
            ? null
            : recvSideAttachment.getAssociation().getRouteTableId();
    if (associatedRouteTableId == null) {
      warnings.redFlagf(
          "No route table associated for peering attachment %s on TGW %s",
          recvSideAttachment.getId(), recvSideAttachment.getGatewayId());
      return;
    }

    List<Region> sendSideRegions =
        vsConfigs.getAccounts().stream()
            .filter(a -> a.getId().equals(sendSideAttachment.getGatewayOwnerId()))
            .flatMap(a -> a.getRegions().stream())
            .filter(r -> r.getTransitGateways().containsKey(sendSideAttachment.getGatewayId()))
            .collect(ImmutableList.toImmutableList());
    if (sendSideRegions.isEmpty()) {
      warnings.redFlagf(
          "TGW %s not found in any region of account %s",
          sendSideAttachment.getGatewayId(), sendSideAttachment.getGatewayOwnerId());
      return;
    }
    if (sendSideRegions.size() > 1) {
      warnings.redFlagf(
          "TGW %s found in multiple regions for account %s. Skipping its peering attachments.",
          sendSideAttachment.getGatewayId(), sendSideAttachment.getGatewayOwnerId());
      return;
    }

    // find the route tables on the send side
    Set<TransitGatewayRouteTable> sendSideRouteTables =
        sendSideRegions.get(0).getTransitGatewayRouteTables().values().stream()
            .filter(
                routeTable -> routeTable.getGatewayId().equals(sendSideAttachment.getGatewayId()))
            .collect(ImmutableSet.toImmutableSet());

    sendSideRouteTables.forEach(
        rt -> {
          Interface sendSideIface =
              sendSideTgw
                  .getAllInterfaces()
                  .get(sendSidePeeringInterfaceName(rt.getId(), sendSideAttachment.getId()));
          if (sendSideIface == null) {
            warnings.redFlagf(
                "Interface for route table %s for peering attachment %s on TGW %s not found",
                rt.getId(), sendSideAttachment.getId(), sendSideAttachment.getGatewayId());
            return;
          }

          // create an interface on the receive side in the right VRF
          Interface recvSideIface =
              Utils.newInterface(
                  receiveSidePeeringInterfaceName(
                      rt.getId(), recvSideAttachment.getId(), associatedRouteTableId),
                  recvSideTgw,
                  vrfNameForRouteTable(associatedRouteTableId),
                  LinkLocalAddress.of(LINK_LOCAL_IP),
                  "From " + recvSideAttachment.getResourceId());

          Utils.addLayer1Edge(
              viConfigs,
              sendSideTgw.getHostname(),
              sendSideIface.getName(),
              recvSideTgw.getHostname(),
              recvSideIface.getName());
        });
  }

  /**
   * Determine if two TGW attachments are peers of each other.
   *
   * <p>For two attachments to be peers,
   *
   * <ul>
   *   <li>both attachments should be of type {@link ResourceType#PEERING} and their attachment ids
   *       should be the same, and
   *   <li>the gateway id and owner information (which describe the local TGW) at the first
   *       attachment should match the resource id and owner information (which describe the peer
   *       TGW) at the second attachment, and vice-versa.
   * </ul>
   */
  @VisibleForTesting
  static boolean arePeers(
      TransitGatewayAttachment attachment1, TransitGatewayAttachment attachment2) {
    return attachment1.getResourceType() == ResourceType.PEERING
        && attachment2.getResourceType() == ResourceType.PEERING
        && attachment1.getId().equals(attachment2.getId())
        && attachment1.getGatewayOwnerId().equals(attachment2.getResourceOwnerId())
        && attachment1.getResourceOwnerId().equals(attachment2.getGatewayOwnerId())
        && attachment1.getGatewayId().equals(attachment2.getResourceId())
        && attachment1.getResourceId().equals(attachment2.getGatewayId());
  }
}
