package org.batfish.representation.terraform;

import static org.batfish.representation.terraform.Constants.JSON_KEY_ARN;
import static org.batfish.representation.terraform.Constants.JSON_KEY_AVAILABILITY_ZONE;
import static org.batfish.representation.terraform.Constants.JSON_KEY_CIDR_BLOCK;
import static org.batfish.representation.terraform.Constants.JSON_KEY_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_OWNER_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_VPC_ID;
import static org.batfish.representation.terraform.Utils.getAwsResourceTags;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;
import org.batfish.representation.aws.Subnet;

/** Represents an AWS VPC */
@ParametersAreNonnullByDefault
class AwsSubnet extends AwsResource {

  private static final List<String> MANDATORY_ATTRIBUTES =
      ImmutableList.of(
          JSON_KEY_OWNER_ID,
          JSON_KEY_ID,
          JSON_KEY_CIDR_BLOCK,
          JSON_KEY_ARN,
          JSON_KEY_VPC_ID,
          JSON_KEY_AVAILABILITY_ZONE);

  @Nonnull private final String _arn;
  @Nonnull private final String _id;
  @Nonnull private final String _vpcId;
  @Nonnull private final String _ownerId;
  @Nonnull private final Prefix _cidrBlock;
  @Nonnull private final String _availabilityZone;
  @Nonnull private final Map<String, String> _tags;

  static AwsSubnet create(CommonResourceProperties common, Map<String, Object> attributes) {
    for (String key : MANDATORY_ATTRIBUTES) {
      if (!attributes.containsKey(key)) {
        throw new IllegalArgumentException(
            String.format("Attribute '%s' not found for VPC %s", key, common.getName()));
      }
    }
    return new AwsSubnet(
        common,
        attributes.get(JSON_KEY_ARN).toString(),
        attributes.get(JSON_KEY_ID).toString(),
        attributes.get(JSON_KEY_VPC_ID).toString(),
        attributes.get(JSON_KEY_OWNER_ID).toString(),
        Prefix.parse(attributes.get(JSON_KEY_CIDR_BLOCK).toString()),
        attributes.get(JSON_KEY_AVAILABILITY_ZONE).toString(),
        getAwsResourceTags(attributes));
  }

  public AwsSubnet(
      CommonResourceProperties common,
      String arn,
      String id,
      String vpcId,
      String ownerId,
      Prefix cidrBlock,
      String availabilityZone,
      Map<String, String> tags) {
    super(common);
    _arn = arn;
    _id = id;
    _vpcId = vpcId;
    _ownerId = ownerId;
    _cidrBlock = cidrBlock;
    _availabilityZone = availabilityZone;
    _tags = tags;
  }

  public Subnet convert() {
    return new Subnet(_cidrBlock, _ownerId, _arn, _id, _vpcId, _availabilityZone, _tags);
  }

  @Nonnull
  public String getArn() {
    return _arn;
  }

  @Nonnull
  public String getId() {
    return _id;
  }

  @Nonnull
  public String getVpcId() {
    return _vpcId;
  }

  @Nonnull
  public String getOwnerId() {
    return _ownerId;
  }

  @Nonnull
  public Prefix getCidrBlock() {
    return _cidrBlock;
  }

  @Nonnull
  public String getAvailabilityZone() {
    return _availabilityZone;
  }

  @Nonnull
  public Map<String, String> getTags() {
    return _tags;
  }
}
