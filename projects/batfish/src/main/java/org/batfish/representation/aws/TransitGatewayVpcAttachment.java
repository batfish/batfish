package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents an AWS Transit Gateway Attachment
 * https://docs.aws.amazon.com/cli/latest/reference/ec2/describe-transit-gateway-attachments.html
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class TransitGatewayVpcAttachment implements AwsVpcEntity, Serializable {

  @Nonnull private final String _attachmentId;

  @Nonnull private final String _gatewayId;

  @Nonnull private final String _vpcId;

  @Nonnull private final List<String> _subnetIds;

  @JsonCreator
  private static TransitGatewayVpcAttachment create(
      @Nullable @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ATTACHMENT_ID) String attachmentId,
      @Nullable @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ID) String gatewayId,
      @Nullable @JsonProperty(JSON_KEY_VPC_ID) String vpcId,
      @Nullable @JsonProperty(JSON_KEY_SUBNET_IDS) List<String> subnetIds) {
    checkArgument(
        attachmentId != null, "Attachment id cannot be null for transit gateway VPC attachment");
    checkArgument(gatewayId != null, "Gateway id cannot be nul for transit gateway VPC attachment");
    checkArgument(vpcId != null, "VPC id cannot be nul for transit gateway VPC attachment");
    checkArgument(subnetIds != null, "Subnet Ids cannot be nul for transit gateway VPC attachment");

    return new TransitGatewayVpcAttachment(attachmentId, gatewayId, vpcId, subnetIds);
  }

  TransitGatewayVpcAttachment(
      String attachmentId, String gatewayId, String vpcId, List<String> subnetIds) {
    _attachmentId = attachmentId;
    _gatewayId = gatewayId;
    _vpcId = vpcId;
    _subnetIds = subnetIds;
  }

  @Nonnull
  @Override
  public String getId() {
    return _attachmentId;
  }

  @Nonnull
  public String getGatewayId() {
    return _gatewayId;
  }

  @Nonnull
  public String getVpcId() {
    return _vpcId;
  }

  @Nonnull
  public List<String> getSubnetIds() {
    return _subnetIds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TransitGatewayVpcAttachment)) {
      return false;
    }
    TransitGatewayVpcAttachment that = (TransitGatewayVpcAttachment) o;
    return Objects.equal(_attachmentId, that._attachmentId)
        && Objects.equal(_gatewayId, that._gatewayId)
        && Objects.equal(_vpcId, that._vpcId)
        && Objects.equal(_subnetIds, that._subnetIds);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_attachmentId, _gatewayId, _vpcId, _subnetIds);
  }
}
