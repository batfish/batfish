package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;

/** Represents an AWS VPC */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class Vpc implements AwsVpcEntity, Serializable {

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  private static final class CidrBlockAssociation implements Serializable {

    @Nonnull private final Prefix _block;

    @JsonCreator
    private static CidrBlockAssociation create(
        @Nullable @JsonProperty(JSON_KEY_CIDR_BLOCK) Prefix block) {
      checkArgument(block != null, "CIDR block cannot be null in CIDR block association");
      return new CidrBlockAssociation(block);
    }

    CidrBlockAssociation(Prefix block) {
      _block = block;
    }

    @Nonnull
    Prefix getBlock() {
      return _block;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      CidrBlockAssociation that = (CidrBlockAssociation) o;
      return Objects.equals(_block, that._block);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_block);
    }
  }

  @Nonnull private final Prefix _cidrBlock;

  @Nonnull private final Set<Prefix> _cidrBlockAssociations;

  @Nonnull private final String _vpcId;

  @Nullable private transient String _vpnGatewayId;

  @Nullable private transient String _internetGatewayId;

  @JsonCreator
  private static Vpc create(
      @Nullable @JsonProperty(JSON_KEY_VPC_ID) String vpcId,
      @Nullable @JsonProperty(JSON_KEY_CIDR_BLOCK) Prefix cidrBlock,
      @Nullable @JsonProperty(JSON_KEY_CIDR_BLOCK_ASSOCIATION_SET)
          Set<CidrBlockAssociation> cidrBlockAssociations) {
    checkArgument(vpcId != null, "VPC id cannot be null");
    checkArgument(cidrBlock != null, "CIDR block cannot be null for VPC");
    checkArgument(
        cidrBlockAssociations != null, "CIDR block association set cannot be null for VPC");
    return new Vpc(
        vpcId,
        cidrBlock,
        cidrBlockAssociations.stream()
            .map(CidrBlockAssociation::getBlock)
            .collect(ImmutableSet.toImmutableSet()));
  }

  Vpc(String vpcId, Prefix cidrBlock, Set<Prefix> cidrBlockAssociations) {
    _vpcId = vpcId;
    _cidrBlock = cidrBlock;
    _cidrBlockAssociations = cidrBlockAssociations;
  }

  @Nonnull
  Prefix getCidrBlock() {
    return _cidrBlock;
  }

  @Nonnull
  Set<Prefix> getCidrBlockAssociations() {
    return _cidrBlockAssociations;
  }

  @Override
  public String getId() {
    return _vpcId;
  }

  @Nullable
  String getInternetGatewayId() {
    return _internetGatewayId;
  }

  @Nullable
  String getVpnGatewayId() {
    return _vpnGatewayId;
  }

  void setInternetGatewayId(String internetGatewayId) {
    _internetGatewayId = internetGatewayId;
  }

  void setVpnGatewayId(String vpnGatewayId) {
    _vpnGatewayId = vpnGatewayId;
  }

  Configuration toConfigurationNode(
      AwsConfiguration awsConfiguration, Region region, Warnings warnings) {
    Configuration cfgNode = Utils.newAwsConfiguration(_vpcId, "aws");
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());
    cfgNode.getVendorFamily().getAws().setVpcId(_vpcId);
    cfgNode
        .getDefaultVrf()
        .getStaticRoutes()
        .add(
            StaticRoute.builder()
                .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
                .setMetric(Route.DEFAULT_STATIC_ROUTE_COST)
                .setNetwork(_cidrBlock)
                .setNextHopInterface(Interface.NULL_INTERFACE_NAME)
                .build());

    // we only create a node here
    // interfaces are added to this node as we traverse subnets and
    // internetgateways

    return cfgNode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Vpc vpc = (Vpc) o;
    return com.google.common.base.Objects.equal(_cidrBlock, vpc._cidrBlock)
        && com.google.common.base.Objects.equal(_cidrBlockAssociations, vpc._cidrBlockAssociations)
        && com.google.common.base.Objects.equal(_vpcId, vpc._vpcId)
        && com.google.common.base.Objects.equal(_vpnGatewayId, vpc._vpnGatewayId)
        && com.google.common.base.Objects.equal(_internetGatewayId, vpc._internetGatewayId);
  }

  @Override
  public int hashCode() {
    return com.google.common.base.Objects.hashCode(
        _cidrBlock, _cidrBlockAssociations, _vpcId, _vpnGatewayId, _internetGatewayId);
  }
}
