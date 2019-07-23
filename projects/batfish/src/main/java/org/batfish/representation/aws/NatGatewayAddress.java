package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_ALLOCATION_ID;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_NETWORK_INTERFACE_ID;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_PRIVATE_IP;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_PUBLIC_IP;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/** Represents an address for a NAT gateway */
@ParametersAreNonnullByDefault
final class NatGatewayAddress implements Serializable {

  @Nonnull private final String _allocationId;
  @Nonnull private final String _networkInterfaceId;
  @Nonnull private final Ip _privateIp;
  @Nonnull private final Ip _publicIp;

  @JsonCreator
  private static NatGatewayAddress create(
      @Nullable @JsonProperty(JSON_KEY_ALLOCATION_ID) String allocationId,
      @Nullable @JsonProperty(JSON_KEY_NETWORK_INTERFACE_ID) String networkInterfaceId,
      @Nullable @JsonProperty(JSON_KEY_PRIVATE_IP) Ip privateIp,
      @Nullable @JsonProperty(JSON_KEY_PUBLIC_IP) Ip publicIp) {
    checkArgument(allocationId != null, "Allocation id cannot be null for NAT gateway address");
    checkArgument(
        networkInterfaceId != null, "Network interface id cannot be null for NAT gateway address");
    checkArgument(privateIp != null, "Private IP cannot be null for NAT gateway address");
    checkArgument(publicIp != null, "Public IP cannot be null for NAT gateway address");

    return new NatGatewayAddress(allocationId, networkInterfaceId, privateIp, publicIp);
  }

  NatGatewayAddress(String allocationId, String networkInterfaceId, Ip privateIp, Ip publicIp) {
    _allocationId = allocationId;
    _networkInterfaceId = networkInterfaceId;
    _privateIp = privateIp;
    _publicIp = publicIp;
  }

  @Nonnull
  String getAllocationId() {
    return _allocationId;
  }

  @Nonnull
  String getNetworkInterfaceId() {
    return _networkInterfaceId;
  }

  @Nonnull
  Ip getPrivateIp() {
    return _privateIp;
  }

  @Nonnull
  Ip getPublicIp() {
    return _publicIp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NatGatewayAddress)) {
      return false;
    }
    NatGatewayAddress that = (NatGatewayAddress) o;
    return Objects.equals(_allocationId, that._allocationId)
        && Objects.equals(_networkInterfaceId, that._networkInterfaceId)
        && Objects.equals(_privateIp, that._privateIp)
        && Objects.equals(_publicIp, that._publicIp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_allocationId, _networkInterfaceId, _privateIp, _publicIp);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_allocationId", _allocationId)
        .add("_networkInterfaceId", _networkInterfaceId)
        .add("_privateIp", _privateIp)
        .add("_publicIp", _publicIp)
        .toString();
  }
}
