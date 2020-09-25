package org.batfish.representation.terraform;

import static org.batfish.representation.terraform.Constants.TYPE_AWS_INSTANCE;
import static org.batfish.representation.terraform.Constants.TYPE_AWS_INTERNET_GATEWAY;
import static org.batfish.representation.terraform.Constants.TYPE_AWS_KEY_PAIR;
import static org.batfish.representation.terraform.Constants.TYPE_AWS_NETWORK_ACL;
import static org.batfish.representation.terraform.Constants.TYPE_AWS_ROUTE_TABLE;
import static org.batfish.representation.terraform.Constants.TYPE_AWS_ROUTE_TABLE_ASSOCIATION;
import static org.batfish.representation.terraform.Constants.TYPE_AWS_SECURITY_GROUP;
import static org.batfish.representation.terraform.Constants.TYPE_AWS_SUBNET;
import static org.batfish.representation.terraform.Constants.TYPE_AWS_VPC;

import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;

@ParametersAreNonnullByDefault
abstract class TerraformResource implements Serializable {

  @Nonnull private final CommonResourceProperties _common;

  TerraformResource(CommonResourceProperties common) {
    _common = common;
  }

  @Nullable
  static TerraformResource create(
      CommonResourceProperties common, Map<String, Object> attributes, Warnings warnings) {
    switch (common.getType()) {
      case TYPE_AWS_INSTANCE:
        return AwsInstance.create(common, attributes);
      case TYPE_AWS_INTERNET_GATEWAY:
        return AwsInternetGateway.create(common, attributes);
      case TYPE_AWS_NETWORK_ACL:
        return AwsNetworkAcl.create(common, attributes);
      case TYPE_AWS_ROUTE_TABLE:
        return AwsRouteTable.create(common, attributes);
      case TYPE_AWS_ROUTE_TABLE_ASSOCIATION:
        return AwsRouteTableAssociation.create(common, attributes);
      case TYPE_AWS_SECURITY_GROUP:
        return AwsSecurityGroup.create(common, attributes);
      case TYPE_AWS_SUBNET:
        return AwsSubnet.create(common, attributes);
      case TYPE_AWS_VPC:
        return AwsVpc.create(common, attributes);
      case TYPE_AWS_KEY_PAIR:
        // types we ignore without a warning
        return null;
      default:
        warnings.redFlag(
            String.format("Unhandled type %s for resource %s", common.getType(), common.getName()));
        return null;
    }
  }

  public CommonResourceProperties getCommon() {
    return _common;
  }

  public boolean IsAwsResource() {
    return _common.getProvider().startsWith("provider.aws");
  }
}
