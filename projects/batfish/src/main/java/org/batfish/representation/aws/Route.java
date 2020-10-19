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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;

/** Representation of a route in AWS */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
abstract class Route implements Serializable {

  enum State {
    ACTIVE,
    BLACKHOLE
  }

  enum TargetType {
    Gateway,
    Instance,
    NatGateway,
    NetworkInterface,
    TransitGateway,
    Unavailable,
    VpcPeeringConnection,
    Unknown
  }

  static final int DEFAULT_STATIC_ROUTE_ADMIN = 1;

  static final int DEFAULT_STATIC_ROUTE_COST = 0;

  @Nonnull protected final State _state;
  @Nullable protected final String _target;
  @Nonnull protected final TargetType _targetType;

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
        "At least one destination type (v4 CIDR, v6 CIDR, prefix list) must be present for a"
            + " route");
    checkArgument(
        destinationCidrBlock == null
            || destinationIpv6CidrBlock == null
            || destinationPrefixListId == null,
        "At most one destination type (v4 CIDR, v6 CIDR, prefix list)  must be present for a"
            + " route");
    checkArgument(stateStr != null, "State cannot be null for a route");

    State state = State.valueOf(stateStr.toUpperCase());
    String target;
    TargetType targetType;

    if (transitGatewayId != null) {
      targetType = TargetType.TransitGateway;
      target = transitGatewayId;
    } else if (vpcPeeringConnectionId != null) {
      targetType = TargetType.VpcPeeringConnection;
      target = vpcPeeringConnectionId;
    } else if (gatewayId != null) {
      targetType = TargetType.Gateway;
      target = gatewayId;
    } else if (natGatewayId != null) {
      targetType = TargetType.NatGateway;
      target = natGatewayId;
    } else if (networkInterfaceId != null) {
      targetType = TargetType.NetworkInterface;
      target = networkInterfaceId;
    } else if (instanceId != null) {
      // NOTE: so far in practice this branch is never reached after moving
      // networkInterfaceId above it!
      targetType = TargetType.Instance;
      target = instanceId;
    } else if (state == State.BLACKHOLE) {
      targetType = TargetType.Unavailable;
      target = null;
    } else {
      targetType = TargetType.Unknown;
      target = null;
    }

    if (destinationCidrBlock != null) {
      return new RouteV4(destinationCidrBlock, state, target, targetType);
    } else if (destinationIpv6CidrBlock != null) {
      return new RouteV6(destinationIpv6CidrBlock, state, target, targetType);
    } else {
      return new RoutePrefixListId(destinationPrefixListId, state, target, targetType);
    }
  }

  protected Route(State state, @Nullable String target, TargetType targetType) {
    _state = state;
    _target = target;
    _targetType = targetType;
  }

  @Nonnull
  public State getState() {
    return _state;
  }

  @Nullable
  public String getTarget() {
    return _target;
  }

  @Nonnull
  public TargetType getTargetType() {
    return _targetType;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_state", _state)
        .add("_target", _target)
        .add("_targetType", _targetType)
        .toString();
  }
}
