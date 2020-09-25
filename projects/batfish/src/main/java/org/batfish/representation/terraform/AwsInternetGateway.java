package org.batfish.representation.terraform;

import static org.batfish.representation.terraform.Constants.JSON_KEY_ARN;
import static org.batfish.representation.terraform.Constants.JSON_KEY_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_VPC_ID;
import static org.batfish.representation.terraform.Utils.checkMandatoryAttributes;
import static org.batfish.representation.terraform.Utils.getAwsResourceTags;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.representation.aws.InternetGateway;

/** Represents an AWS VPC */
@ParametersAreNonnullByDefault
class AwsInternetGateway extends AwsResource {

  private static final List<String> MANDATORY_ATTRIBUTES =
      ImmutableList.of(JSON_KEY_ARN, JSON_KEY_ID, JSON_KEY_VPC_ID);

  @Nonnull private final String _arn;
  @Nonnull private final String _id;
  @Nonnull private final String _vpdId;
  @Nonnull private final Map<String, String> _tags;

  static AwsInternetGateway create(
      CommonResourceProperties common, Map<String, Object> attributes) {
    checkMandatoryAttributes(attributes, MANDATORY_ATTRIBUTES, common.getName());
    return new AwsInternetGateway(
        common,
        attributes.get(JSON_KEY_ARN).toString(),
        attributes.get(JSON_KEY_ID).toString(),
        attributes.get(JSON_KEY_VPC_ID).toString(),
        getAwsResourceTags(attributes));
  }

  public AwsInternetGateway(
      CommonResourceProperties common,
      String arn,
      String id,
      String vpcId,
      Map<String, String> tags) {
    super(common);
    _arn = arn;
    _id = id;
    _vpdId = vpcId;
    _tags = tags;
  }

  public InternetGateway convert() {
    return new InternetGateway(_id, ImmutableList.of(_vpdId), _tags);
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
    return _vpdId;
  }
}
