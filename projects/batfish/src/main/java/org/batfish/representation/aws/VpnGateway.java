package org.batfish.representation.aws;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.util.isp.IspModelingUtils.installRoutingPolicyAdvertiseStatic;
import static org.batfish.representation.aws.AwsConfiguration.LINK_LOCAL_IP;
import static org.batfish.representation.aws.Utils.ACCEPT_ALL_BGP;
import static org.batfish.representation.aws.Utils.connectGatewayToVpc;
import static org.batfish.representation.aws.Utils.makeBgpProcess;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

/** Represents an AWS VPN gateway */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class VpnGateway implements AwsVpcEntity, Serializable {

  static final String VGW_EXPORT_POLICY_NAME = "~vgw~export-policy~";
  static final String VGW_IMPORT_POLICY_NAME = "~vgw~import-policy~";

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static class VpcAttachment {

    private final @Nonnull String _vpcId;

    @JsonCreator
    private static VpcAttachment create(@JsonProperty(JSON_KEY_VPC_ID) @Nullable String vpcId) {
      checkArgument(vpcId != null, "Vpc id cannot be null for VPN attachment");
      return new VpcAttachment(vpcId);
    }

    private VpcAttachment(String vpcId) {
      _vpcId = vpcId;
    }

    @Nonnull
    String getVpcId() {
      return _vpcId;
    }
  }

  private final @Nonnull List<String> _attachmentVpcIds;

  private final @Nonnull String _vpnGatewayId;

  private final @Nonnull Map<String, String> _tags;

  private final long _amazonSideAsn;

  @JsonCreator
  private static VpnGateway create(
      @JsonProperty(JSON_KEY_VPN_GATEWAY_ID) @Nullable String vpnGatewayId,
      @JsonProperty(JSON_KEY_VPC_ATTACHMENTS) @Nullable List<VpcAttachment> vpcAttachments,
      @JsonProperty(JSON_KEY_TAGS) @Nullable List<Tag> tags,
      @JsonProperty(JSON_KEY_AMAZON_SIDE_ASN) @Nullable Long amazonSideAsn) {
    checkArgument(vpnGatewayId != null, "Id cannot be null for VPC gateway");
    checkArgument(vpcAttachments != null, "Vpc attachments cannot be null for VPN gateway");
    checkArgument(amazonSideAsn != null, "Amazon side ASN cannot be null for a VPN gateway");

    return new VpnGateway(
        vpnGatewayId,
        vpcAttachments.stream()
            .map(VpcAttachment::getVpcId)
            .collect(ImmutableList.toImmutableList()),
        firstNonNull(tags, ImmutableList.<Tag>of()).stream()
            .collect(ImmutableMap.toImmutableMap(Tag::getKey, Tag::getValue)),
        amazonSideAsn);
  }

  VpnGateway(
      String vpnGatewayId,
      List<String> attachmentVpcIds,
      Map<String, String> tags,
      Long amazonSideAsn) {
    _vpnGatewayId = vpnGatewayId;
    _attachmentVpcIds = attachmentVpcIds;
    _tags = tags;
    _amazonSideAsn = amazonSideAsn;
  }

  @Nonnull
  Long getAmazonSideAsn() {
    return _amazonSideAsn;
  }

  @Nonnull
  List<String> getAttachmentVpcIds() {
    return _attachmentVpcIds;
  }

  @Override
  public String getId() {
    return _vpnGatewayId;
  }

  /**
   * Creates a node for the VPN gateway. Other essential elements of this node are created
   * elsewhere. During subnet processing, we create links to the subnet and also add static routes
   * to the subnet. During VPN connection processing, we create BGP processes on the node with the
   * right policy.
   */
  Configuration toConfigurationNode(
      AwsConfiguration vsConfiguration,
      ConvertedConfiguration awsConfiguration,
      Region region,
      Warnings warnings) {
    Configuration cfgNode =
        Utils.newAwsConfiguration(_vpnGatewayId, "aws", _tags, DeviceModel.AWS_VPN_GATEWAY);
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());

    _attachmentVpcIds.forEach(
        vpcId ->
            connectGatewayToVpc(_vpnGatewayId, cfgNode, vpcId, awsConfiguration, region, warnings));

    // VIFs that terminate directly on this VGW (Private VIFs, VGW-attached). VIFs are global
    // resources in the AWS model and may be observed in any region; aggregate across all regions.
    Collection<DirectConnectVirtualInterface> vifs =
        vsConfiguration.getAccounts().stream()
            .flatMap(a -> a.getRegions().stream())
            .flatMap(r -> r.getDirectConnectVirtualInterfaces().values().stream())
            .filter(vif -> _vpnGatewayId.equals(vif.getVirtualGatewayId()))
            .collect(Collectors.toList());

    // True if any DXGW has an association whose AssociatedGateway is this VGW. Such an association
    // implies a BGP-unnumbered link between the DXGW and this VGW will be added later.
    boolean hasDxgwAssociation =
        vsConfiguration.getAccounts().stream()
            .flatMap(a -> a.getRegions().stream())
            .flatMap(r -> r.getDirectConnectGatewayAssociations().values().stream())
            .anyMatch(
                a ->
                    a.getAssociatedGateway().getType()
                            == DirectConnectGatewayAssociation.AssociatedGateway.GatewayType
                                .VIRTUAL_PRIVATE_GATEWAY
                        && _vpnGatewayId.equals(a.getAssociatedGateway().getId()));

    // if this VGW has any BGP-based VPN connections, configure BGP on it
    boolean doBgp =
        region.getVpnConnections().values().stream()
                .filter(conn -> _vpnGatewayId.equals(conn.getAwsGatewayId()))
                .anyMatch(VpnConnection::isBgpConnection)
            || !vifs.isEmpty()
            || hasDxgwAssociation;

    if (doBgp) {
      String loopbackBgp = "loopbackBgp";
      LinkLocalAddress loopbackBgpAddress = LinkLocalAddress.of(LINK_LOCAL_IP);
      Utils.newInterface(loopbackBgp, cfgNode, loopbackBgpAddress, "BGP loopback");

      BgpProcess proc = makeBgpProcess(loopbackBgpAddress.getIp(), cfgNode.getDefaultVrf());
      proc.setMultipathEquivalentAsPathMatchMode(MultipathEquivalentAsPathMatchMode.EXACT_PATH);

      PrefixSpace originationSpace = new PrefixSpace();
      _attachmentVpcIds.stream()
          .flatMap(vpcId -> region.getVpcs().get(vpcId).getCidrBlockAssociations().stream())
          .forEach(originationSpace::addPrefix);

      installRoutingPolicyAdvertiseStatic(VGW_EXPORT_POLICY_NAME, cfgNode, originationSpace);

      RoutingPolicy.builder()
          .setName(VGW_IMPORT_POLICY_NAME)
          .setOwner(cfgNode)
          .setStatements(Collections.singletonList(ACCEPT_ALL_BGP))
          .build();
    }

    // process all VPN connections
    Set<VpnConnection> vpnConnections =
        region.getVpnConnections().values().stream()
            .filter(c -> _vpnGatewayId.equals(c.getAwsGatewayId()))
            .collect(Collectors.toSet());

    if (!vpnConnections.isEmpty()) {
      VpnConnection.initVpnConnectionsInfrastructure(cfgNode);
      vpnConnections.forEach(
          c ->
              c.applyToGateway(
                  cfgNode,
                  cfgNode.getDefaultVrf(),
                  VGW_EXPORT_POLICY_NAME,
                  VGW_IMPORT_POLICY_NAME,
                  warnings));
    }

    // Wire customer-facing VIFs (Private VIFs, VGW-attached). The VIF interface and BGP peer live
    // on the VGW; operators add an L1 edge from on-prem to this interface.
    vifs.forEach(vif -> configureVifBgpSession(cfgNode, vif));

    return cfgNode;
  }

  /** Per-VIF DX community-to-localpref import policy name on the VGW. */
  static String vgwDxImportPolicyName(String vifId) {
    return String.format("~vgw~dx-import-policy~%s~", vifId);
  }

  private void configureVifBgpSession(Configuration cfgNode, DirectConnectVirtualInterface vif) {
    Ip amazonIp = vif.getAmazonIp();
    Ip customerIp = vif.getCustomerIp();

    Interface vifIface =
        Utils.newInterface(
            vif.getId(),
            cfgNode,
            vif.getAmazonAddress(),
            "Direct Connect VIF " + vif.getVirtualInterfaceName());
    vifIface.updateInterfaceType(InterfaceType.PHYSICAL);
    vifIface.setEncapsulationVlan(vif.getVlan());

    String importPolicy =
        DirectConnectGateway.installDxImportPolicy(cfgNode, vgwDxImportPolicyName(vif.getId()));

    BgpActivePeerConfig.builder()
        .setPeerAddress(customerIp)
        .setRemoteAsns(LongSpace.of(vif.getAsn()))
        .setLocalIp(amazonIp)
        .setLocalAs(_amazonSideAsn)
        .setBgpProcess(cfgNode.getDefaultVrf().getBgpProcess())
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setExportPolicy(VGW_EXPORT_POLICY_NAME)
                .setImportPolicy(importPolicy)
                .build())
        .build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof VpnGateway)) {
      return false;
    }
    VpnGateway that = (VpnGateway) o;
    return Objects.equals(_attachmentVpcIds, that._attachmentVpcIds)
        && Objects.equals(_tags, that._tags)
        && Objects.equals(_vpnGatewayId, that._vpnGatewayId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_attachmentVpcIds, _vpnGatewayId, _tags);
  }
}
