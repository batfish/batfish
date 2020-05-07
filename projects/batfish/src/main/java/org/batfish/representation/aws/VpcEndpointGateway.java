package org.batfish.representation.aws;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents an AWS VPC endpoint of type gateway */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class VpcEndpointGateway extends VpcEndpoint {

  VpcEndpointGateway(String id, String vpcId) {
    super(id, vpcId);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof VpcEndpointGateway)) {
      return false;
    }
    VpcEndpointGateway that = (VpcEndpointGateway) o;
    return _id.equals(that._id) && _vpcId.equals(that._vpcId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_id, _vpcId);
  }
}
