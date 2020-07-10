package org.batfish.representation.aws;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.representation.aws.Utils.checkNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;

/** Represents an AWS VPC endpoint */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public abstract class VpcEndpoint implements AwsVpcEntity, Serializable {

  @Nonnull protected final String _id;

  @Nonnull protected final String _serviceName;

  @Nonnull protected final String _vpcId;

  @Nonnull protected final Map<String, String> _tags;

  @JsonCreator
  private static VpcEndpoint create(
      @Nullable @JsonProperty(JSON_KEY_VPC_ENDPOINT_ID) String vpcEndpointId,
      @Nullable @JsonProperty(JSON_KEY_SERVICE_NAME) String serviceName,
      @Nullable @JsonProperty(JSON_KEY_VPC_ID) String vpcId,
      @Nullable @JsonProperty(JSON_KEY_VPC_ENDPOINT_TYPE) String vpcEndpointType,
      @Nullable @JsonProperty(JSON_KEY_NETWORK_INTERFACE_IDS) List<String> networkInterfaceIds,
      @Nullable @JsonProperty(JSON_KEY_SUBNET_IDS) List<String> subnetIds,
      @Nullable @JsonProperty(JSON_KEY_TAGS) List<Tag> tags) {
    checkNonNull(vpcEndpointId, JSON_KEY_VPC_ENDPOINT_ID, "VPC endpoint");
    checkNonNull(serviceName, JSON_KEY_SERVICE_NAME, "VPC endpoint");
    checkNonNull(vpcId, JSON_KEY_VPC_ID, "VPC endpoint");
    checkNonNull(vpcEndpointType, JSON_KEY_VPC_ENDPOINT_TYPE, "VPC endpoint");

    ImmutableMap<String, String> tagMap =
        firstNonNull(tags, ImmutableList.<Tag>of()).stream()
            .collect(ImmutableMap.toImmutableMap(Tag::getKey, Tag::getValue));

    if (vpcEndpointType.equalsIgnoreCase("gateway")) {
      return new VpcEndpointGateway(vpcEndpointId, serviceName, vpcId, tagMap);
    } else if (vpcEndpointType.equalsIgnoreCase("interface")) {
      checkNonNull(networkInterfaceIds, JSON_KEY_NETWORK_INTERFACE_IDS, "Interface VPC endpoint");
      checkNonNull(subnetIds, JSON_KEY_SUBNET_IDS, "Interface VPC endpoint");
      return new VpcEndpointInterface(
          vpcEndpointId, serviceName, vpcId, networkInterfaceIds, subnetIds, tagMap);
    } else {
      throw new IllegalArgumentException("Unknown VPC endpoint type " + vpcEndpointType);
    }
  }

  VpcEndpoint(String id, String serviceName, String vpcId, Map<String, String> tags) {
    _id = id;
    _serviceName = serviceName;
    _vpcId = vpcId;
    _tags = tags;
  }

  abstract List<Configuration> toConfigurationNodes(
      ConvertedConfiguration awsConfiguration, Region region, Warnings warnings);

  @Nonnull
  @Override
  public String getId() {
    return _id;
  }

  @Nonnull
  public String getServiceName() {
    return _serviceName;
  }

  @Nonnull
  public String getVpcId() {
    return _vpcId;
  }
}
