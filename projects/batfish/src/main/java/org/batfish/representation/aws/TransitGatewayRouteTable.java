package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents an AWS Transit Gateway Route Table
 * https://docs.aws.amazon.com/cli/latest/reference/ec2/describe-transit-gateway-route-tables.html
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class TransitGatewayRouteTable implements AwsVpcEntity, Serializable {

  @Nonnull private final String _gatewayId;

  @Nonnull private final String _routeTableId;

  private final boolean _defaultAssociationRouteTable;

  private final boolean _defaultPropagationRouteTable;

  @JsonCreator
  private static TransitGatewayRouteTable create(
      @Nullable @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ID) String gatewayId,
      @Nullable @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ROUTE_TABLE_ID) String routeTableId,
      @Nullable @JsonProperty(JSON_KEY_DEFAULT_ASSOCIATION_ROUTE_TABLE)
          Boolean defaultAssociationRouteTable,
      @Nullable @JsonProperty(JSON_KEY_DEFAULT_PROPAGATION_ROUTE_TABLE)
          Boolean defaultPropagationRouteTable) {
    checkArgument(gatewayId != null, "Gateway id cannot be null for transit gateway route table");
    checkArgument(
        routeTableId != null, "Route table id cannot be null for transit gateway route table");
    checkArgument(
        defaultAssociationRouteTable != null,
        "Default association property cannot be null for transit gateway route table");
    checkArgument(
        defaultPropagationRouteTable != null,
        "Default propagation property cannot be null for transit gateway route table");

    return new TransitGatewayRouteTable(
        routeTableId, gatewayId, defaultAssociationRouteTable, defaultPropagationRouteTable);
  }

  TransitGatewayRouteTable(
      String routeTableId,
      String gatewayId,
      boolean defaultAssociationRouteTable,
      boolean defaultPropagationRouteTable) {
    _gatewayId = gatewayId;
    _routeTableId = routeTableId;
    _defaultAssociationRouteTable = defaultAssociationRouteTable;
    _defaultPropagationRouteTable = defaultPropagationRouteTable;
  }

  @Nonnull
  public String getGatewayId() {
    return _gatewayId;
  }

  @Override
  @Nonnull
  public String getId() {
    return _routeTableId;
  }

  public boolean isDefaultAssociationRouteTable() {
    return _defaultAssociationRouteTable;
  }

  public boolean isDefaultPropagationRouteTable() {
    return _defaultPropagationRouteTable;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TransitGatewayRouteTable)) {
      return false;
    }
    TransitGatewayRouteTable that = (TransitGatewayRouteTable) o;
    return _defaultAssociationRouteTable == that._defaultAssociationRouteTable
        && _defaultPropagationRouteTable == that._defaultPropagationRouteTable
        && Objects.equals(_gatewayId, that._gatewayId)
        && Objects.equals(_routeTableId, that._routeTableId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _gatewayId, _routeTableId, _defaultAssociationRouteTable, _defaultPropagationRouteTable);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_gatewayId", _gatewayId)
        .add("_routeTableId", _routeTableId)
        .add("_defaultAssociationRouteTable", _defaultAssociationRouteTable)
        .add("_defaultPropagationRouteTable", _defaultPropagationRouteTable)
        .toString();
  }
}
