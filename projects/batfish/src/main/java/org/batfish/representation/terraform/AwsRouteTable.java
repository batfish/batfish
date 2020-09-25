package org.batfish.representation.terraform;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.terraform.Constants.JSON_KEY_CIDR_BLOCK;
import static org.batfish.representation.terraform.Constants.JSON_KEY_GATEWAY_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_INSTANCE_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_IPV6_CIDR_BLOCK;
import static org.batfish.representation.terraform.Constants.JSON_KEY_LOCAL_GATEWAY_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_NAT_GATEWAY_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_NETWORK_INTERFACE_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_OWNER_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_ROUTE;
import static org.batfish.representation.terraform.Constants.JSON_KEY_TRANSIT_GATEWAY_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_VPC_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_VPC_PEERING_CONNECTION_ID;
import static org.batfish.representation.terraform.Utils.checkMandatoryAttributes;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.representation.aws.Route.RouteTarget;
import org.batfish.representation.aws.Route.State;
import org.batfish.representation.aws.RouteTable;
import org.batfish.representation.aws.RouteTable.Association;
import org.batfish.representation.aws.RouteV4;
import org.batfish.representation.aws.RouteV6;

/** Represents an AWS VPC */
@ParametersAreNonnullByDefault
class AwsRouteTable extends AwsResource {

  private static class Route implements Serializable {

    private static final List<String> MANDATORY_ATTRIBUTES =
        ImmutableList.of(
            JSON_KEY_CIDR_BLOCK,
            JSON_KEY_GATEWAY_ID,
            JSON_KEY_INSTANCE_ID,
            JSON_KEY_IPV6_CIDR_BLOCK,
            JSON_KEY_LOCAL_GATEWAY_ID,
            JSON_KEY_NAT_GATEWAY_ID,
            JSON_KEY_NETWORK_INTERFACE_ID,
            JSON_KEY_TRANSIT_GATEWAY_ID,
            JSON_KEY_VPC_PEERING_CONNECTION_ID);

    @Nullable private final Prefix _cidrBlock;
    @Nullable private final String _gatewayId;
    @Nullable private final String _instanceId;
    @Nullable private final Prefix6 _ipv6CidrBlock;
    @Nullable private final String _localGatewayId;
    @Nullable private final String _natGatewayId;
    @Nullable private final String _networkInterfaceId;
    @Nullable private final String _transitGatewayId;
    @Nullable private final String _vpcPeeringConnectionId;

    static Route create(Map<String, Object> attributes, String resourceDescription) {
      checkMandatoryAttributes(attributes, MANDATORY_ATTRIBUTES, resourceDescription);
      // these are empty strings when unset
      String cidrBlock = attributes.get(JSON_KEY_CIDR_BLOCK).toString();
      String ipv6CidrBlock = attributes.get(JSON_KEY_IPV6_CIDR_BLOCK).toString();

      return new Route(
          cidrBlock.equals("") ? null : Prefix.parse(cidrBlock),
          getNullIfEmpty(attributes.get(JSON_KEY_GATEWAY_ID).toString()),
          getNullIfEmpty(attributes.get(JSON_KEY_INSTANCE_ID).toString()),
          ipv6CidrBlock.equals("") ? null : Prefix6.parse(ipv6CidrBlock),
          getNullIfEmpty(attributes.get(JSON_KEY_LOCAL_GATEWAY_ID).toString()),
          getNullIfEmpty(attributes.get(JSON_KEY_NAT_GATEWAY_ID).toString()),
          getNullIfEmpty(attributes.get(JSON_KEY_NETWORK_INTERFACE_ID).toString()),
          getNullIfEmpty(attributes.get(JSON_KEY_TRANSIT_GATEWAY_ID).toString()),
          getNullIfEmpty(attributes.get(JSON_KEY_VPC_PEERING_CONNECTION_ID).toString()));
    }

    private static String getNullIfEmpty(String value) {
      return value.equals("") ? null : value;
    }

    Route(
        @Nullable Prefix cidrBlock,
        @Nullable String gatewayId,
        @Nullable String instanceId,
        @Nullable Prefix6 ipv6CidrBlock,
        @Nullable String localGatewayId,
        @Nullable String natGatewayId,
        @Nullable String networkInterfaceId,
        @Nullable String transitGatewayId,
        @Nullable String vpcPeeringConnectionId) {
      checkArgument(
          cidrBlock != null || ipv6CidrBlock != null,
          "At least one of v4 and v6 cidr block must not be null");
      checkArgument(
          cidrBlock == null || ipv6CidrBlock == null,
          "At least one of v4 and v6 cidr block must be null");
      _cidrBlock = cidrBlock;
      _gatewayId = gatewayId;
      _instanceId = instanceId;
      _ipv6CidrBlock = ipv6CidrBlock;
      _localGatewayId = localGatewayId;
      _natGatewayId = natGatewayId;
      _networkInterfaceId = networkInterfaceId;
      _transitGatewayId = transitGatewayId;
      _vpcPeeringConnectionId = vpcPeeringConnectionId;
    }

    org.batfish.representation.aws.Route convert() {
      RouteTarget routeTarget =
          RouteTarget.create(
              _transitGatewayId,
              _vpcPeeringConnectionId,
              _gatewayId,
              _natGatewayId,
              _networkInterfaceId,
              _instanceId,
              State.ACTIVE);
      return _cidrBlock != null
          ? new RouteV4(_cidrBlock, State.ACTIVE, routeTarget)
          : new RouteV6(_ipv6CidrBlock, State.ACTIVE, routeTarget);
    }
  }

  private static final List<String> MANDATORY_ATTRIBUTES =
      ImmutableList.of(JSON_KEY_ID, JSON_KEY_VPC_ID, JSON_KEY_OWNER_ID, JSON_KEY_ROUTE);

  @Nonnull private final String _id;
  @Nonnull private final String _vpcId;
  @Nonnull private final String _ownerId;
  @Nonnull private final List<Route> _routes;

  static AwsRouteTable create(CommonResourceProperties common, Map<String, Object> attributes) {
    checkMandatoryAttributes(attributes, MANDATORY_ATTRIBUTES, common.getName());
    return new AwsRouteTable(
        common,
        attributes.get(JSON_KEY_ID).toString(),
        attributes.get(JSON_KEY_VPC_ID).toString(),
        attributes.get(JSON_KEY_OWNER_ID).toString(),
        getRoutes(attributes.get(JSON_KEY_ROUTE), "routes for " + common.getName()));
  }

  @SuppressWarnings("unchecked")
  private static List<Route> getRoutes(Object routes, String resourceDescription) {
    return ((List<?>) routes)
        .stream()
            .map(m -> Route.create((Map<String, Object>) m, resourceDescription))
            .collect(ImmutableList.toImmutableList());
  }

  public AwsRouteTable(
      CommonResourceProperties common,
      String id,
      String vpcId,
      String ownerId,
      List<Route> routes) {
    super(common);
    _id = id;
    _vpcId = vpcId;
    _ownerId = ownerId;
    _routes = routes;
  }

  public RouteTable convert(List<Association> routeTableAssociations) {
    return new RouteTable(
        _id,
        _vpcId,
        routeTableAssociations,
        _routes.stream().map(Route::convert).collect(ImmutableList.toImmutableList()));
  }

  /** Given all resources in the data, returns this route table's subnet associations */
  public List<Association> findSubnetAssociations(List<AwsResource> awsResources) {
    return awsResources.stream()
        .filter(r -> r instanceof AwsRouteTableAssociation)
        .map(r -> (AwsRouteTableAssociation) r)
        .filter(rtassoc -> rtassoc.getRouteTableId().equals(_id))
        .map(AwsRouteTableAssociation::getSubnetId)
        .filter(Objects::nonNull)
        .map(subnetId -> new Association(false, subnetId))
        .collect(ImmutableList.toImmutableList());
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
}
