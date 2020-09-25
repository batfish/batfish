package org.batfish.representation.terraform;

import static org.batfish.representation.terraform.Constants.JSON_KEY_GATEWAY_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_ROUTE_TABLE_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_SUBNET_ID;
import static org.batfish.representation.terraform.Utils.checkMandatoryAttributes;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents an AWS route table association */
@ParametersAreNonnullByDefault
class AwsRouteTableAssociation extends AwsResource {

  private static final List<String> MANDATORY_ATTRIBUTES =
      ImmutableList.of(JSON_KEY_ID, JSON_KEY_ROUTE_TABLE_ID);

  @Nonnull private final String _id;
  @Nonnull private final String _routeTableId;
  @Nullable private final String _gatewayId;
  @Nullable private final String _subnetId;

  static AwsRouteTableAssociation create(
      CommonResourceProperties common, Map<String, Object> attributes) {
    checkMandatoryAttributes(attributes, MANDATORY_ATTRIBUTES, common.getName());

    return new AwsRouteTableAssociation(
        common,
        attributes.get(JSON_KEY_ID).toString(),
        attributes.get(JSON_KEY_ROUTE_TABLE_ID).toString(),
        attributes.getOrDefault(JSON_KEY_GATEWAY_ID, null) == null
            ? null
            : attributes.get(JSON_KEY_GATEWAY_ID).toString(),
        attributes.getOrDefault(JSON_KEY_SUBNET_ID, null) == null
            ? null
            : attributes.get(JSON_KEY_SUBNET_ID).toString());
  }

  public AwsRouteTableAssociation(
      CommonResourceProperties common,
      String id,
      String routeTableId,
      @Nullable String gatewayId,
      @Nullable String subnetId) {
    super(common);
    _id = id;
    _routeTableId = routeTableId;
    _gatewayId = gatewayId;
    _subnetId = subnetId;
  }

  @Nonnull
  public String getId() {
    return _id;
  }

  @Nonnull
  public String getRouteTableId() {
    return _routeTableId;
  }

  @Nullable
  public String getGatewayId() {
    return _gatewayId;
  }

  @Nullable
  public String getSubnetId() {
    return _subnetId;
  }
}
