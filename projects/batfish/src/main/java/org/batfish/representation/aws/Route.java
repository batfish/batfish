package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_DESTINATION_CIDR_BLOCK;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_DESTINATION_IPV6_CIDR_BLOCK;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_DESTINATION_PREFIX_LIST_ID;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_GATEWAY_ID;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_INSTANCE_ID;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_NAT_GATEWAY_ID;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_NETWORK_INTERFACE_ID;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_STATE;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_TRANSIT_GATEWAY_ID;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_VPC_PEERING_CONNECTION_ID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;

/** Representation of a route in AWS */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public abstract class Route implements Serializable {

  public enum State {
    ACTIVE,
    BLACKHOLE
  }

  public enum TargetType {
    Gateway,
    Instance,
    NatGateway,
    NetworkInterface,
    TransitGateway,
    Unavailable,
    VpcPeeringConnection,
    Unknown
  }

  /** A helper class that describes the target of a route */
  public static class RouteTarget implements Serializable {
    @Nonnull private final TargetType _targetType;
    @Nullable private final String _targetId;

    public static RouteTarget create(
        @Nullable String transitGatewayId,
        @Nullable String vpcPeeringConnectionId,
        @Nullable String gatewayId,
        @Nullable String natGatewayId,
        @Nullable String networkInterfaceId,
        @Nullable String instanceId,
        State state) {
      TargetType targetType;
      @Nullable String targetId;
      if (transitGatewayId != null) {
        targetType = TargetType.TransitGateway;
        targetId = transitGatewayId;
      } else if (vpcPeeringConnectionId != null) {
        targetType = TargetType.VpcPeeringConnection;
        targetId = vpcPeeringConnectionId;
      } else if (gatewayId != null) {
        targetType = TargetType.Gateway;
        targetId = gatewayId;
      } else if (natGatewayId != null) {
        targetType = TargetType.NatGateway;
        targetId = natGatewayId;
      } else if (networkInterfaceId != null) {
        targetType = TargetType.NetworkInterface;
        targetId = networkInterfaceId;
      } else if (instanceId != null) {
        // NOTE: so far in practice this branch is never reached after moving
        // networkInterfaceId above it!
        targetType = TargetType.Instance;
        targetId = instanceId;
      } else if (state == State.BLACKHOLE) {
        targetType = TargetType.Unavailable;
        targetId = null;
      } else {
        targetType = TargetType.Unknown;
        targetId = null;
      }
      return new RouteTarget(targetId, targetType);
    }

    public RouteTarget(@Nullable String targetId, TargetType targetType) {
      _targetType = targetType;
      _targetId = targetId;
    }

    @Nonnull
    public TargetType getTargetType() {
      return _targetType;
    }

    @Nullable
    public String getTargetId() {
      return _targetId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof RouteTarget)) {
        return false;
      }
      RouteTarget that = (RouteTarget) o;
      return _targetType == that._targetType && Objects.equals(_targetId, that._targetId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_targetType, _targetId);
    }
  }

  static final int DEFAULT_STATIC_ROUTE_ADMIN = 1;

  static final int DEFAULT_STATIC_ROUTE_COST = 0;

  @Nonnull protected final State _state;
  @Nonnull protected final RouteTarget _routeTarget;

  @JsonCreator
  private static Route create(
      @Nullable @JsonProperty(JSON_KEY_DESTINATION_CIDR_BLOCK) Prefix destinationCidrBlock,
      @Nullable @JsonProperty(JSON_KEY_DESTINATION_IPV6_CIDR_BLOCK)
          Prefix6 destinationIpv6CidrBlock,
      @Nullable @JsonProperty(JSON_KEY_DESTINATION_PREFIX_LIST_ID) String destinationPrefixListId,
      @Nullable @JsonProperty(JSON_KEY_STATE) String stateStr,
      @Nullable @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ID) String transitGatewayId,
      @Nullable @JsonProperty(JSON_KEY_VPC_PEERING_CONNECTION_ID) String vpcPeeringConnectionId,
      @Nullable @JsonProperty(JSON_KEY_GATEWAY_ID) String gatewayId,
      @Nullable @JsonProperty(JSON_KEY_NAT_GATEWAY_ID) String natGatewayId,
      @Nullable @JsonProperty(JSON_KEY_NETWORK_INTERFACE_ID) String networkInterfaceId,
      @Nullable @JsonProperty(JSON_KEY_INSTANCE_ID) String instanceId) {

    checkArgument(
        destinationCidrBlock != null
            || destinationIpv6CidrBlock != null
            || destinationPrefixListId != null,
        "At least one destination type (v4 CIDR, v6 CIDR, prefix list) must be present for a route");
    checkArgument(
        destinationCidrBlock == null
            || destinationIpv6CidrBlock == null
            || destinationPrefixListId == null,
        "At most one destination type (v4 CIDR, v6 CIDR, prefix list)  must be present for a route");
    checkArgument(stateStr != null, "State cannot be null for a route");

    State state = State.valueOf(stateStr.toUpperCase());
    RouteTarget routeTarget =
        RouteTarget.create(
            transitGatewayId,
            vpcPeeringConnectionId,
            gatewayId,
            natGatewayId,
            networkInterfaceId,
            instanceId,
            state);

    if (destinationCidrBlock != null) {
      return new RouteV4(destinationCidrBlock, state, routeTarget);
    } else if (destinationIpv6CidrBlock != null) {
      return new RouteV6(destinationIpv6CidrBlock, state, routeTarget);
    } else {
      return new RoutePrefixListId(destinationPrefixListId, state, routeTarget);
    }
  }

  protected Route(State state, RouteTarget routeTarget) {
    _state = state;
    _routeTarget = routeTarget;
  }

  @Nonnull
  public State getState() {
    return _state;
  }

  @Nonnull
  public RouteTarget getRouteTarget() {
    return _routeTarget;
  }

  @Nonnull
  public TargetType getTargetType() {
    return _routeTarget.getTargetType();
  }

  @Nullable
  public String getTarget() {
    return _routeTarget.getTargetId();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_state", _state)
        .add("_targetId", _routeTarget.getTargetId())
        .add("_targetType", _routeTarget.getTargetType())
        .toString();
  }
}
