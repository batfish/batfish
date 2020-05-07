package org.batfish.representation.aws;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents an AWS VPC endpoint of type gateway */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class VpcEndpointInterface extends VpcEndpoint {

  @Nonnull private final List<String> _networkInterfaceIds;

  @Nonnull private final List<String> _subnetIds;

  VpcEndpointInterface(
      String id, String vpcId, List<String> networkInterfaceIds, List<String> subnetIds) {
    super(id, vpcId);
    _networkInterfaceIds = networkInterfaceIds;
    _subnetIds = subnetIds;
  }

  @Nonnull
  public List<String> getNetworkInterfaceIds() {
    return _networkInterfaceIds;
  }

  @Nonnull
  public List<String> getSubnetIds() {
    return _subnetIds;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof VpcEndpointInterface)) {
      return false;
    }
    VpcEndpointInterface that = (VpcEndpointInterface) o;
    return _networkInterfaceIds.equals(that._networkInterfaceIds)
        && _subnetIds.equals(that._subnetIds)
        && _id.equals(that._id)
        && _vpcId.equals(that._vpcId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_networkInterfaceIds, _subnetIds, _id, _vpcId);
  }
}
