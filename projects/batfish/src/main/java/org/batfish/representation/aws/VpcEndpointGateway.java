package org.batfish.representation.aws;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;

/** Represents an AWS VPC endpoint of type gateway */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class VpcEndpointGateway extends VpcEndpoint {

  VpcEndpointGateway(String id, String vpcId, Map<String, String> tags) {
    super(id, vpcId, tags);
  }

  @Override
  List<Configuration> toConfigurationNodes(
      ConvertedConfiguration awsConfiguration, Region region, Warnings warnings) {
    warnings.redFlag("VPC gateway endpoints are not currently supported");
    return ImmutableList.of();
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
    return _id.equals(that._id) && _vpcId.equals(that._vpcId) && _tags.equals(that._tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_id, _vpcId, _tags);
  }
}
