package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;

/** Represents an AWS NAT gateway */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class NatGateway implements AwsVpcEntity, Serializable {

  @Nonnull private final List<NatGatewayAddress> _natGatewayAddresses;

  @Nonnull private final String _natGatewayId;

  @Nonnull private final String _subnetId;

  @Nonnull private final String _vpcId;

  @JsonCreator
  private static NatGateway create(
      @Nullable @JsonProperty(JSON_KEY_NAT_GATEWAY_ID) String natGatewayId,
      @Nullable @JsonProperty(JSON_KEY_SUBNET_ID) String subnetId,
      @Nullable @JsonProperty(JSON_KEY_VPC_ID) String vpcId,
      @Nullable @JsonProperty(JSON_KEY_NAT_GATEWAY_ADDRESSES)
          List<NatGatewayAddress> natGatewayAddresses) {
    checkArgument(natGatewayId != null, "NAT gateway id cannot be null");
    checkArgument(subnetId != null, "Subnet id cannot be null for nat gateway");
    checkArgument(vpcId != null, "VPC id cannot be null for nat gateway");
    checkArgument(natGatewayAddresses != null, "Nat gateway addresses cannot be null");

    return new NatGateway(natGatewayId, subnetId, vpcId, natGatewayAddresses);
  }

  NatGateway(
      String natGatewayId,
      String subnetId,
      String vpcId,
      List<NatGatewayAddress> natGatewayAddresses) {
    _natGatewayId = natGatewayId;
    _subnetId = subnetId;
    _vpcId = vpcId;
    _natGatewayAddresses = natGatewayAddresses;
  }

  @Override
  public String getId() {
    return _natGatewayId;
  }

  @Nonnull
  List<NatGatewayAddress> getNatGatewayAddresses() {
    return _natGatewayAddresses;
  }

  @Nonnull
  String getSubnetId() {
    return _subnetId;
  }

  @Nonnull
  public String getVpcId() {
    return _vpcId;
  }

  Configuration toConfigurationNode(
      AwsConfiguration awsConfiguration, Region region, Warnings warnings) {
    Configuration cfgNode = Utils.newAwsConfiguration(_natGatewayId, "aws");
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());

    // TODO: Configure forwarding for this NAT
    //    for (NatGatewayAddress natAddress : _natGatewayAddresses) {
    // foreach natgatewayaddress create interfaces for public and private IPs, configure NAT rules
    // also connect the nat to the VPC router
    //    }

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
    NatGateway that = (NatGateway) o;
    return Objects.equals(_natGatewayAddresses, that._natGatewayAddresses)
        && Objects.equals(_natGatewayId, that._natGatewayId)
        && Objects.equals(_subnetId, that._subnetId)
        && Objects.equals(_vpcId, that._vpcId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_natGatewayAddresses, _natGatewayId, _subnetId, _vpcId);
  }
}
