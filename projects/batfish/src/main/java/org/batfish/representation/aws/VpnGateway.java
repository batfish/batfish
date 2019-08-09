package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.util.IspModelingUtils.installRoutingPolicyAdvertiseStatic;
import static org.batfish.datamodel.Interface.NULL_INTERFACE_NAME;
import static org.batfish.representation.aws.Utils.addStaticRoute;
import static org.batfish.representation.aws.Utils.toStaticRoute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

/** Represents an AWS VPN gateway */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class VpnGateway implements AwsVpcEntity, Serializable {

  static final String VGW_EXPORT_POLICY_NAME = "~vgw~export-policy~";
  static final String VGW_IMPORT_POLICY_NAME = "~vgw~import-policy~";

  static final Statement ACCEPT_ALL_BGP =
      new If(
          new MatchProtocol(RoutingProtocol.BGP),
          ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
          ImmutableList.of(Statements.ExitReject.toStaticStatement()));

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static class VpcAttachment {

    @Nonnull private final String _vpcId;

    @JsonCreator
    private static VpcAttachment create(@Nullable @JsonProperty(JSON_KEY_VPC_ID) String vpcId) {
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

  @Nonnull private final List<String> _attachmentVpcIds;

  @Nonnull private final String _vpnGatewayId;

  @JsonCreator
  private static VpnGateway create(
      @Nullable @JsonProperty(JSON_KEY_VPN_GATEWAY_ID) String vpnGatewayId,
      @Nullable @JsonProperty(JSON_KEY_VPC_ATTACHMENTS) List<VpcAttachment> vpcAttachments) {
    checkArgument(vpnGatewayId != null, "Id cannot be null for VPC gateway");
    checkArgument(vpcAttachments != null, "Vpc attachments cannot be nul for VPN gateway");

    return new VpnGateway(
        vpnGatewayId,
        vpcAttachments.stream()
            .map(VpcAttachment::getVpcId)
            .collect(ImmutableList.toImmutableList()));
  }

  VpnGateway(String vpnGatewayId, List<String> attachmentVpcIds) {
    _vpnGatewayId = vpnGatewayId;
    _attachmentVpcIds = attachmentVpcIds;
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
      AwsConfiguration awsConfiguration, Region region, Warnings warnings) {
    Configuration cfgNode = Utils.newAwsConfiguration(_vpnGatewayId, "aws");
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());

    // if this VGW has any BGP-based VPN connections, configure BGP on it
    boolean doBgp =
        region.getVpnConnections().values().stream()
            .filter(conn -> _vpnGatewayId.equals(conn.getVpnGatewayId()))
            .anyMatch(VpnConnection::isBgpConnection);

    if (doBgp) {
      String loopbackBgp = "loopbackBgp";
      ConcreteInterfaceAddress loopbackBgpAddress =
          ConcreteInterfaceAddress.create(
              awsConfiguration.getNextGeneratedLinkSubnet().getStartIp(), Prefix.MAX_PREFIX_LENGTH);
      Utils.newInterface(loopbackBgp, cfgNode, loopbackBgpAddress, "BGP loopback");

      BgpProcess proc =
          BgpProcess.builder()
              .setRouterId(loopbackBgpAddress.getIp())
              .setVrf(cfgNode.getDefaultVrf())
              .setAdminCostsToVendorDefaults(ConfigurationFormat.AWS)
              .build();
      proc.setMultipathEquivalentAsPathMatchMode(MultipathEquivalentAsPathMatchMode.EXACT_PATH);

      PrefixSpace originationSpace = new PrefixSpace();
      _attachmentVpcIds.stream()
          .flatMap(vpcId -> region.getVpcs().get(vpcId).getCidrBlockAssociations().stream())
          .forEach(
              pfx -> {
                originationSpace.addPrefix(pfx);
                addStaticRoute(cfgNode, toStaticRoute(pfx, NULL_INTERFACE_NAME));
              });

      installRoutingPolicyAdvertiseStatic(
          VGW_EXPORT_POLICY_NAME, cfgNode, originationSpace, new NetworkFactory());

      RoutingPolicy.builder()
          .setName(VGW_IMPORT_POLICY_NAME)
          .setOwner(cfgNode)
          .setStatements(Collections.singletonList(ACCEPT_ALL_BGP))
          .build();
    }

    return cfgNode;
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
        && Objects.equals(_vpnGatewayId, that._vpnGatewayId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_attachmentVpcIds, _vpnGatewayId);
  }
}
