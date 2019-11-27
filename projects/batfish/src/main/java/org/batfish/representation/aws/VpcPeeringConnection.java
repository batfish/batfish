package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.Utils.addStaticRoute;
import static org.batfish.representation.aws.Utils.suffixedInterfaceName;
import static org.batfish.representation.aws.Utils.toStaticRoute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;

/** Represents an AWS VPC peering connection */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class VpcPeeringConnection implements AwsVpcEntity, Serializable {

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static final class VpcInfo {
    @Nonnull private final String _vpcId;

    @Nonnull private final Prefix _cidrBlock;

    @JsonCreator
    private static VpcInfo create(
        @Nullable @JsonProperty(JSON_KEY_VPC_ID) String vpcId,
        @Nullable @JsonProperty(JSON_KEY_CIDR_BLOCK) Prefix cidrBlock) {
      checkArgument(vpcId != null, "VPC id cannot be null in VPC info");
      checkArgument(cidrBlock != null, "CIDR block cannot null in VPC info");
      return new VpcInfo(vpcId, cidrBlock);
    }

    private VpcInfo(String vpcId, Prefix cidrBlock) {
      _cidrBlock = cidrBlock;
      _vpcId = vpcId;
    }

    @Nonnull
    String getVpcId() {
      return _vpcId;
    }

    @Nonnull
    Prefix getCidrBlock() {
      return _cidrBlock;
    }
  }

  @Nonnull private final Prefix _accepterVpcCidrBlock;

  @Nonnull private final String _accepterVpcId;

  @Nonnull private final Prefix _requesterVpcCidrBlock;

  @Nonnull private final String _requesterVpcId;

  @Nonnull private final String _vpcPeeringConnectionId;

  @JsonCreator
  private static VpcPeeringConnection create(
      @Nullable @JsonProperty(JSON_KEY_VPC_PEERING_CONNECTION_ID) String vpcPeeringConnectionId,
      @Nullable @JsonProperty(JSON_KEY_ACCEPTER_VPC_INFO) VpcInfo accepterVpcInfo,
      @Nullable @JsonProperty(JSON_KEY_REQUESTER_VPC_INFO) VpcInfo requesterVpcInfo) {
    checkArgument(vpcPeeringConnectionId != null, "VPC peering connection Id cannot be null");
    checkArgument(
        accepterVpcInfo != null, "Accepter VPC info cannot be null in VPC peering connection Id");
    checkArgument(
        requesterVpcInfo != null, "Requester VPC info cannot be null in VPC peering connection Id");

    return new VpcPeeringConnection(
        vpcPeeringConnectionId,
        accepterVpcInfo.getVpcId(),
        accepterVpcInfo.getCidrBlock(),
        requesterVpcInfo.getVpcId(),
        requesterVpcInfo.getCidrBlock());
  }

  VpcPeeringConnection(
      String vpcPeeringConnectionId,
      String accepterVpcId,
      Prefix accepterVpcCidrBlock,
      String requesterVpcId,
      Prefix requesterVpcCidrBlock) {
    _vpcPeeringConnectionId = vpcPeeringConnectionId;
    _accepterVpcId = accepterVpcId;
    _accepterVpcCidrBlock = accepterVpcCidrBlock;
    _requesterVpcId = requesterVpcId;
    _requesterVpcCidrBlock = requesterVpcCidrBlock;
  }

  /**
   * Creates the VPC peering between the two VPCs. It creates the interfaces that link the two VPCs,
   * adds default routes in the right VRFs on both nodes that point to each other, and null routes
   * the cidr blocks that are local (so they are not sent over the connection -- more specific
   * routes that belong to subnets were inserted during subnet processing).
   */
  void createConnection(
      Map<String, Region> regions, ConvertedConfiguration awsConfiguration, Warnings warnings) {
    Vpc accepterVpc =
        regions.values().stream()
            .flatMap(r -> r.getVpcs().values().stream())
            .filter(vpc -> vpc.getId().equals(_accepterVpcId))
            .findFirst()
            .orElse(null);
    Vpc requesterVpc =
        regions.values().stream()
            .flatMap(r -> r.getVpcs().values().stream())
            .filter(vpc -> vpc.getId().equals(_accepterVpcId))
            .findFirst()
            .orElse(null);

    if (accepterVpc == null) {
      warnings.redFlag(
          String.format(
              "Accepter VPC %s not found for connection %s. Will not create the connection.",
              _accepterVpcId, _vpcPeeringConnectionId));
      return;
    }
    if (requesterVpc == null) {
      warnings.redFlag(
          String.format(
              "Requested VPC %s not found for connection %s. Will not create the connection.",
              _requesterVpcId, _vpcPeeringConnectionId));
      return;
    }

    Configuration accepterCfg =
        awsConfiguration.getConfigurationNodes().get(Vpc.nodeName(_accepterVpcId));
    Configuration requesterCfg =
        awsConfiguration.getConfigurationNodes().get(Vpc.nodeName(_requesterVpcId));

    String vrfName = Vpc.vrfNameForPeeeringConnection(_vpcPeeringConnectionId);
    // VRFs will not exist if none of the subnets were using the connection
    if (!accepterCfg.getVrfs().containsKey(vrfName)) {
      Vrf.builder().setOwner(accepterCfg).setName(vrfName).build();
    }
    // VRFs will not exist if none of the subnets were using the connection
    if (!requesterCfg.getVrfs().containsKey(vrfName)) {
      Vrf.builder().setOwner(requesterCfg).setName(vrfName).build();
    }

    String ifaceNameSuffix = _vpcPeeringConnectionId;
    Utils.connect(awsConfiguration, accepterCfg, vrfName, requesterCfg, vrfName, ifaceNameSuffix);

    addStaticRoute(
        accepterCfg.getVrfs().get(vrfName),
        toStaticRoute(
            _requesterVpcCidrBlock,
            Utils.getInterfaceIp(
                requesterCfg, suffixedInterfaceName(accepterCfg, ifaceNameSuffix))));
    addStaticRoute(
        requesterCfg.getVrfs().get(vrfName),
        toStaticRoute(
            _accepterVpcCidrBlock,
            Utils.getInterfaceIp(
                accepterCfg, suffixedInterfaceName(requesterCfg, ifaceNameSuffix))));
  }

  @Nonnull
  Prefix getAccepterVpcCidrBlock() {
    return _accepterVpcCidrBlock;
  }

  @Nonnull
  String getAccepterVpcId() {
    return _accepterVpcId;
  }

  @Override
  public String getId() {
    return _vpcPeeringConnectionId;
  }

  @Nonnull
  Prefix getRequesterVpcCidrBlock() {
    return _requesterVpcCidrBlock;
  }

  @Nonnull
  String getRequesterVpcId() {
    return _requesterVpcId;
  }

  @Nonnull
  String getVpcPeeringConnectionId() {
    return _vpcPeeringConnectionId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VpcPeeringConnection that = (VpcPeeringConnection) o;
    return Objects.equals(_accepterVpcCidrBlock, that._accepterVpcCidrBlock)
        && Objects.equals(_accepterVpcId, that._accepterVpcId)
        && Objects.equals(_requesterVpcCidrBlock, that._requesterVpcCidrBlock)
        && Objects.equals(_requesterVpcId, that._requesterVpcId)
        && Objects.equals(_vpcPeeringConnectionId, that._vpcPeeringConnectionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _accepterVpcCidrBlock,
        _accepterVpcId,
        _requesterVpcCidrBlock,
        _requesterVpcId,
        _vpcPeeringConnectionId);
  }
}
