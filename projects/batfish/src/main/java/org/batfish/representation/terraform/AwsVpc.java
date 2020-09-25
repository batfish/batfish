package org.batfish.representation.terraform;

import static org.batfish.representation.terraform.Constants.JSON_KEY_ARN;
import static org.batfish.representation.terraform.Constants.JSON_KEY_CIDR_BLOCK;
import static org.batfish.representation.terraform.Constants.JSON_KEY_ID;
import static org.batfish.representation.terraform.Utils.checkMandatoryAttributes;
import static org.batfish.representation.terraform.Utils.getArnAcccount;
import static org.batfish.representation.terraform.Utils.getAwsResourceTags;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;
import org.batfish.representation.aws.Vpc;

/** Represents an AWS VPC */
@ParametersAreNonnullByDefault
class AwsVpc extends AwsResource {

  private static final List<String> MANDATORY_ATTRIBUTES =
      ImmutableList.of(JSON_KEY_ARN, JSON_KEY_ID, JSON_KEY_CIDR_BLOCK);

  @Nonnull private final Prefix _cidrBlock;

  @Nonnull private final Map<String, String> _tags;

  @Nonnull private final String _id;

  @Nonnull private final String _arn;

  // TODO: check what happens with additional CIDR blocks

  static AwsVpc create(CommonResourceProperties common, Map<String, Object> attributes) {
    checkMandatoryAttributes(attributes, MANDATORY_ATTRIBUTES, common.getName());
    return new AwsVpc(
        common,
        attributes.get(JSON_KEY_ID).toString(),
        attributes.get(JSON_KEY_ARN).toString(),
        Prefix.parse(attributes.get(JSON_KEY_CIDR_BLOCK).toString()),
        getAwsResourceTags(attributes));
  }

  public AwsVpc(
      CommonResourceProperties common,
      String id,
      String arn,
      Prefix cidrBlock,
      Map<String, String> tags) {
    super(common);
    _id = id;
    _arn = arn;
    _cidrBlock = cidrBlock;
    _tags = tags;
  }

  public Vpc convert() {
    return new Vpc(getArnAcccount(_arn), _id, ImmutableSet.of(_cidrBlock), _tags);
  }

  @Nonnull
  public String getArn() {
    return _arn;
  }

  @Nonnull
  public String getId() {
    return _id;
  }
}
