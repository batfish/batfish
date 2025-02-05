package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.Utils.addStaticRoute;
import static org.batfish.representation.aws.Utils.toStaticRoute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
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
  private static final class CidrBlock {
    private final @Nonnull Prefix _cidrBlock;

    @JsonCreator
    private static CidrBlock create(@JsonProperty(JSON_KEY_CIDR_BLOCK) @Nullable Prefix cidrBlock) {
      checkArgument(cidrBlock != null, "CidrBlock cannot null in CidrBlockSet");
      return new CidrBlock(cidrBlock);
    }

    private CidrBlock(Prefix cidrBlock) {
      _cidrBlock = cidrBlock;
    }

    @Nonnull
    Prefix getCidrBlock() {
      return _cidrBlock;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static final class VpcInfo {
    private final @Nonnull String _vpcId;

    private final @Nonnull List<Prefix> _cidrBlocks;

    @JsonCreator
    private static VpcInfo create(
        @JsonProperty(JSON_KEY_VPC_ID) @Nullable String vpcId,
        @JsonProperty(JSON_KEY_CIDR_BLOCK_SET) @Nullable List<CidrBlock> cidrBlockSet) {
      checkArgument(vpcId != null, "VPC id cannot be null in VPC info");
      checkArgument(cidrBlockSet != null, "CIDR block set cannot null in VPC info");
      return new VpcInfo(
          vpcId,
          cidrBlockSet.stream()
              .map(CidrBlock::getCidrBlock)
              .collect(ImmutableList.toImmutableList()));
    }

    private VpcInfo(String vpcId, List<Prefix> cidrBlockSet) {
      _cidrBlocks = cidrBlockSet;
      _vpcId = vpcId;
    }

    @Nonnull
    String getVpcId() {
      return _vpcId;
    }

    @Nonnull
    List<Prefix> getCidrBlocks() {
      return _cidrBlocks;
    }
  }

  private final @Nonnull List<Prefix> _accepterVpcCidrBlock;

  private final @Nonnull String _accepterVpcId;

  private final @Nonnull List<Prefix> _requesterVpcCidrBlock;

  private final @Nonnull String _requesterVpcId;

  private final @Nonnull String _vpcPeeringConnectionId;

  @JsonCreator
  private static VpcPeeringConnection create(
      @JsonProperty(JSON_KEY_VPC_PEERING_CONNECTION_ID) @Nullable String vpcPeeringConnectionId,
      @JsonProperty(JSON_KEY_ACCEPTER_VPC_INFO) @Nullable VpcInfo accepterVpcInfo,
      @JsonProperty(JSON_KEY_REQUESTER_VPC_INFO) @Nullable VpcInfo requesterVpcInfo) {
    checkArgument(vpcPeeringConnectionId != null, "VPC peering connection Id cannot be null");
    checkArgument(
        accepterVpcInfo != null, "Accepter VPC info cannot be null in VPC peering connection Id");
    checkArgument(
        requesterVpcInfo != null, "Requester VPC info cannot be null in VPC peering connection Id");

    return new VpcPeeringConnection(
        vpcPeeringConnectionId,
        accepterVpcInfo.getVpcId(),
        accepterVpcInfo.getCidrBlocks(),
        requesterVpcInfo.getVpcId(),
        requesterVpcInfo.getCidrBlocks());
  }

  VpcPeeringConnection(
      String vpcPeeringConnectionId,
      String accepterVpcId,
      List<Prefix> accepterVpcCidrBlocks,
      String requesterVpcId,
      List<Prefix> requesterVpcCidrBlocks) {
    _vpcPeeringConnectionId = vpcPeeringConnectionId;
    _accepterVpcId = accepterVpcId;
    _accepterVpcCidrBlock = accepterVpcCidrBlocks;
    _requesterVpcId = requesterVpcId;
    _requesterVpcCidrBlock = requesterVpcCidrBlocks;
  }

  /**
   * Creates the VPC peering between the two VPCs. It creates the interfaces that link the two VPCs,
   * adds default routes in the right VRFs on both nodes that point to each other, and null routes
   * the cidr blocks that are local (so they are not sent over the connection -- more specific
   * routes that belong to subnets were inserted during subnet processing).
   */
  void createConnection(ConvertedConfiguration awsConfiguration, Warnings warnings) {
    Configuration accepterCfg = awsConfiguration.getNode(Vpc.nodeName(_accepterVpcId));
    if (accepterCfg == null) {
      warnings.redFlagf(
          "Accepter VPC %s not found for connection %s. Will not create the connection.",
          _accepterVpcId, _vpcPeeringConnectionId);
      return;
    }
    Configuration requesterCfg = awsConfiguration.getNode(Vpc.nodeName(_requesterVpcId));
    if (requesterCfg == null) {
      warnings.redFlagf(
          "Requested VPC %s not found for connection %s. Will not create the connection.",
          _requesterVpcId, _vpcPeeringConnectionId);
      return;
    }

    String vrfName = Vpc.vrfNameForLink(_vpcPeeringConnectionId);

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

    _requesterVpcCidrBlock.forEach(
        prefix ->
            addStaticRoute(
                accepterCfg.getVrfs().get(vrfName),
                toStaticRoute(
                    prefix,
                    Utils.interfaceNameToRemote(requesterCfg, ifaceNameSuffix),
                    Utils.getInterfaceLinkLocalIp(
                        requesterCfg, Utils.interfaceNameToRemote(accepterCfg, ifaceNameSuffix)))));
    _accepterVpcCidrBlock.forEach(
        prefix ->
            addStaticRoute(
                requesterCfg.getVrfs().get(vrfName),
                toStaticRoute(
                    prefix,
                    Utils.interfaceNameToRemote(accepterCfg, ifaceNameSuffix),
                    Utils.getInterfaceLinkLocalIp(
                        accepterCfg, Utils.interfaceNameToRemote(requesterCfg, ifaceNameSuffix)))));
  }

  @Nonnull
  List<Prefix> getAccepterVpcCidrBlock() {
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
  List<Prefix> getRequesterVpcCidrBlock() {
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
