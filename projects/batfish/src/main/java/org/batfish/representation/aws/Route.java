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

  /**
   * BGP local-preference values applied on the TGW to routes received from a Direct Connect Gateway
   * peer. AWS exposes traffic-engineering communities that customers attach to BGP advertisements
   * over a VIF; AWS translates these to local-preference values applied on the AWS side when
   * selecting a path back to on-prem. See <a
   * href="https://docs.aws.amazon.com/directconnect/latest/UserGuide/routing-and-bgp.html">AWS DX
   * routing policies and BGP communities</a>.
   *
   * <ul>
   *   <li>{@code 7224:7300} (high) → {@link #DIRECT_CONNECT_HIGH_LOCAL_PREFERENCE}
   *   <li>{@code 7224:7200} (medium, default if no community is set) → {@link
   *       #DIRECT_CONNECT_MEDIUM_LOCAL_PREFERENCE}
   *   <li>{@code 7224:7100} (low) → {@link #DIRECT_CONNECT_LOW_LOCAL_PREFERENCE}
   * </ul>
   *
   * All three values are higher than the default BGP local-pref (100) used for VPN-propagated
   * routes, preserving AWS's documented "DX > VPN" preference even when a customer flags a DX path
   * as low priority. Within DX, HIGH > MEDIUM > LOW determines active/active vs active/passive
   * between multiple DX paths.
   */
  static final long DIRECT_CONNECT_HIGH_LOCAL_PREFERENCE = 300L;

  static final long DIRECT_CONNECT_MEDIUM_LOCAL_PREFERENCE = 200L;

  static final long DIRECT_CONNECT_LOW_LOCAL_PREFERENCE = 150L;

  /** Default DX local-preference: applied when no traffic-engineering community is set. */
  static final long DIRECT_CONNECT_LOCAL_PREFERENCE = DIRECT_CONNECT_MEDIUM_LOCAL_PREFERENCE;

  static final int DEFAULT_STATIC_ROUTE_COST = 0;

  protected final @Nonnull State _state;
  protected final @Nullable String _target;
  protected final @Nonnull TargetType _targetType;

  @JsonCreator
  private static Route create(
      @JsonProperty(JSON_KEY_DESTINATION_CIDR_BLOCK) @Nullable Prefix destinationCidrBlock,
      @JsonProperty(JSON_KEY_DESTINATION_IPV6_CIDR_BLOCK) @Nullable
          Prefix6 destinationIpv6CidrBlock,
      @JsonProperty(JSON_KEY_DESTINATION_PREFIX_LIST_ID) @Nullable String destinationPrefixListId,
      @JsonProperty(JSON_KEY_STATE) @Nullable String stateStr,
      @JsonProperty(JSON_KEY_TRANSIT_GATEWAY_ID) @Nullable String transitGatewayId,
      @JsonProperty(JSON_KEY_VPC_PEERING_CONNECTION_ID) @Nullable String vpcPeeringConnectionId,
      @JsonProperty(JSON_KEY_GATEWAY_ID) @Nullable String gatewayId,
      @JsonProperty(JSON_KEY_NAT_GATEWAY_ID) @Nullable String natGatewayId,
      @JsonProperty(JSON_KEY_NETWORK_INTERFACE_ID) @Nullable String networkInterfaceId,
      @JsonProperty(JSON_KEY_INSTANCE_ID) @Nullable String instanceId) {

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

  public @Nonnull State getState() {
    return _state;
  }

  public @Nullable String getTarget() {
    return _target;
  }

  public @Nonnull TargetType getTargetType() {
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
