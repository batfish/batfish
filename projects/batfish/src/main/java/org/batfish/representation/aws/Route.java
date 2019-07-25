package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_DESTINATION_CIDR_BLOCK;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_GATEWAY_ID;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_INSTANCE_ID;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_NAT_GATEWAY_ID;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_NETWORK_INTERFACE_ID;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_STATE;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_VPC_PEERING_CONNECTION_ID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;

/** Representation of a route in AWS */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class Route implements Serializable {

  enum State {
    ACTIVE,
    BLACKHOLE
  }

  enum TargetType {
    Gateway,
    Instance,
    NatGateway,
    NetworkInterface,
    Unavailable,
    VpcPeeringConnection
  }

  static final int DEFAULT_STATIC_ROUTE_ADMIN = 1;

  static final int DEFAULT_STATIC_ROUTE_COST = 0;

  @Nonnull private final Prefix _destinationCidrBlock;
  @Nonnull private final State _state;
  @Nullable private final String _target;
  @Nonnull private final TargetType _targetType;

  @JsonCreator
  private static Route create(
      @Nullable @JsonProperty(JSON_KEY_DESTINATION_CIDR_BLOCK) Prefix destinationCidrBlock,
      @Nullable @JsonProperty(JSON_KEY_STATE) String stateStr,
      @Nullable @JsonProperty(JSON_KEY_VPC_PEERING_CONNECTION_ID) String vpcPeeringConnectionId,
      @Nullable @JsonProperty(JSON_KEY_GATEWAY_ID) String gatewayId,
      @Nullable @JsonProperty(JSON_KEY_NAT_GATEWAY_ID) String natGatewayId,
      @Nullable @JsonProperty(JSON_KEY_NETWORK_INTERFACE_ID) String networkInterfaceId,
      @Nullable @JsonProperty(JSON_KEY_INSTANCE_ID) String instanceId) {

    checkArgument(
        destinationCidrBlock != null, "Destination CIDR block cannot be null for a route");
    checkArgument(stateStr != null, "State cannot be null for a route");

    State state = State.valueOf(stateStr.toUpperCase());
    String target;
    TargetType targetType;

    if (vpcPeeringConnectionId != null) {
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
      throw new IllegalArgumentException(
          "Unable to determine target type in route for " + destinationCidrBlock);
    }

    return new Route(destinationCidrBlock, state, target, targetType);
  }

  Route(Prefix destinationCidrBlock, State state, @Nullable String target, TargetType targetType) {
    _destinationCidrBlock = destinationCidrBlock;
    _state = state;
    _target = target;
    _targetType = targetType;
  }

  @Nullable
  StaticRoute toStaticRoute(
      AwsConfiguration awsConfiguration,
      Region region,
      Ip vpcAddress,
      @Nullable Ip igwAddress,
      @Nullable Ip vgwAddress,
      Subnet subnet,
      Configuration subnetCfgNode,
      Warnings warnings) {
    // setting the common properties
    StaticRoute.Builder srBuilder =
        StaticRoute.builder()
            .setNetwork(_destinationCidrBlock)
            .setAdministrativeCost(DEFAULT_STATIC_ROUTE_ADMIN)
            .setMetric(DEFAULT_STATIC_ROUTE_COST);

    if (_state == State.BLACKHOLE) {
      srBuilder.setNextHopInterface(Interface.NULL_INTERFACE_NAME);
    } else {
      switch (_targetType) {
        case Gateway:
          if (_target.equals("local")) {
            // send to the vpc router
            srBuilder.setNextHopIp(vpcAddress);
          } else {
            // send to the specified internet gateway
            // if it's not the igw or vgw set for this subnet (and VPC),
            // throw an
            // exception
            if (_target.equals(subnet.getInternetGatewayId())) {
              srBuilder.setNextHopIp(igwAddress);
            } else if (_target.equals(subnet.getVpnGatewayId())) {
              srBuilder.setNextHopIp(vgwAddress);
            } else {
              throw new BatfishException(
                  "Internet gateway \""
                      + _target
                      + "\" specified in this route not accessible from this subnet");
            }
          }
          break;

        case NatGateway:
          // TODO: it is NOT clear that this is the right thing to do
          // for NATs with multiple interfaces, we should probably match on private IPs?
          srBuilder.setNextHopIp(
              region.getNatGateways().get(_target).getNatGatewayAddresses().get(0).getPrivateIp());
          break;

        case NetworkInterface:
          NetworkInterface networkInterface = region.getNetworkInterfaces().get(_target);
          String networkInterfaceSubnetId = networkInterface.getSubnetId();
          if (networkInterfaceSubnetId.equals(subnet.getId())) {
            Set<Ip> networkInterfaceIps =
                new TreeSet<>(
                    networkInterface.getPrivateIpAddresses().stream()
                        .map(PrivateIpAddress::getPrivateIp)
                        .collect(ImmutableSet.toImmutableSet()));
            Ip lowestIp = networkInterfaceIps.toArray(new Ip[] {})[0];
            if (!subnet.getCidrBlock().containsIp(lowestIp)) {
              throw new BatfishException(
                  "Ip of network interface specified in static route not in containing subnet");
            }
            srBuilder.setNextHopIp(lowestIp);
          } else {
            String networkInterfaceVpcId =
                region.getSubnets().get(networkInterfaceSubnetId).getVpcId();
            String vpcId = subnet.getVpcId();
            if (!vpcId.equals(networkInterfaceVpcId)) {
              throw new BatfishException("Cannot peer with interface on different VPC");
            }
            // need to create a link between subnet on which route is created
            // and instance containing network interface
            String subnetIfaceName = _target;
            Prefix instanceLink = awsConfiguration.getNextGeneratedLinkSubnet();
            ConcreteInterfaceAddress subnetIfaceAddress =
                ConcreteInterfaceAddress.create(
                    instanceLink.getStartIp(), instanceLink.getPrefixLength());
            Utils.newInterface(
                subnetIfaceName, subnetCfgNode, subnetIfaceAddress, "To instance " + _targetType);

            // set up instance interface
            String instanceId = networkInterface.getAttachmentInstanceId();
            String instanceIfaceName = subnet.getId();
            Configuration instanceCfgNode =
                awsConfiguration.getConfigurationNodes().get(instanceId);
            ConcreteInterfaceAddress instanceIfaceAddress =
                ConcreteInterfaceAddress.create(
                    instanceLink.getEndIp(), instanceLink.getPrefixLength());
            Interface instanceIface =
                Utils.newInterface(
                    instanceIfaceName,
                    instanceCfgNode,
                    instanceIfaceAddress,
                    "To subnet " + subnet.getId());
            instanceIface.setIncomingFilter(
                instanceCfgNode
                    .getIpAccessLists()
                    .getOrDefault(
                        Region.SG_INGRESS_ACL_NAME,
                        IpAccessList.builder()
                            .setName(Region.SG_INGRESS_ACL_NAME)
                            .setLines(new LinkedList<>())
                            .build()));
            instanceIface.setOutgoingFilter(
                instanceCfgNode
                    .getIpAccessLists()
                    .getOrDefault(
                        Region.SG_EGRESS_ACL_NAME,
                        IpAccessList.builder()
                            .setName(Region.SG_EGRESS_ACL_NAME)
                            .setLines(new LinkedList<>())
                            .build()));
            Ip nextHopIp = instanceIfaceAddress.getIp();
            srBuilder.setNextHopIp(nextHopIp);
          }
          break;

        case VpcPeeringConnection:
          // create route for vpc peering connection
          String vpcPeeringConnectionid = _target;
          VpcPeeringConnection vpcPeeringConnection =
              region.getVpcPeeringConnections().get(vpcPeeringConnectionid);
          String localVpcId = subnet.getVpcId();
          String accepterVpcId = vpcPeeringConnection.getAccepterVpcId();
          String requesterVpcId = vpcPeeringConnection.getRequesterVpcId();
          String remoteVpcId = localVpcId.equals(accepterVpcId) ? requesterVpcId : accepterVpcId;
          Configuration remoteVpcCfgNode =
              awsConfiguration.getConfigurationNodes().get(remoteVpcId);
          if (remoteVpcCfgNode == null) {
            warnings.redFlag(
                "VPC \""
                    + localVpcId
                    + "\" cannot peer with non-existent VPC: \""
                    + remoteVpcId
                    + "\"");
            return null;
          }

          // set up subnet interface if necessary
          String subnetIfaceName = remoteVpcId;
          String remoteVpcIfaceName = subnet.getId();
          Ip remoteVpcIfaceIp;
          if (!subnetCfgNode.getDefaultVrf().getInterfaces().containsKey(subnetIfaceName)) {
            // create prefix on which subnet and remote vpc router will connect
            Prefix peeringLink = awsConfiguration.getNextGeneratedLinkSubnet();
            ConcreteInterfaceAddress subnetIfaceAddress =
                ConcreteInterfaceAddress.create(
                    peeringLink.getStartIp(), peeringLink.getPrefixLength());
            Utils.newInterface(
                subnetIfaceName, subnetCfgNode, subnetIfaceAddress, "To remote VPC " + remoteVpcId);

            // set up remote vpc router interface
            ConcreteInterfaceAddress remoteVpcIfaceAddress =
                ConcreteInterfaceAddress.create(
                    peeringLink.getEndIp(), peeringLink.getPrefixLength());
            Interface remoteVpcIface =
                Interface.builder().setName(remoteVpcIfaceName).setOwner(remoteVpcCfgNode).build();
            remoteVpcCfgNode.getAllInterfaces().put(remoteVpcIfaceName, remoteVpcIface);
            remoteVpcCfgNode
                .getDefaultVrf()
                .getInterfaces()
                .put(remoteVpcIfaceName, remoteVpcIface);
            remoteVpcIface.setAddress(remoteVpcIfaceAddress);
            remoteVpcIface.getAllConcreteAddresses().add(remoteVpcIfaceAddress);
          }
          // interface pair exists now, so just retrieve existing information
          remoteVpcIfaceIp =
              remoteVpcCfgNode
                  .getDefaultVrf()
                  .getInterfaces()
                  .get(remoteVpcIfaceName)
                  .getConcreteAddress()
                  .getIp();

          // initialize static route on new link
          srBuilder.setNextHopIp(remoteVpcIfaceIp);
          break;

        case Instance:
          // TODO: create route for instance
          warnings.redFlag(
              "Skipping creating route to "
                  + _destinationCidrBlock
                  + " for instance: \""
                  + _target
                  + "\"");
          return null;

        default:
          throw new BatfishException("Unsupported target type: " + _targetType);
      }
    }
    return srBuilder.build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Route)) {
      return false;
    }
    Route route = (Route) o;
    return Objects.equals(_destinationCidrBlock, route._destinationCidrBlock)
        && _state == route._state
        && Objects.equals(_target, route._target)
        && _targetType == route._targetType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_destinationCidrBlock, _state, _target, _targetType);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_destinationCidrBlock", _destinationCidrBlock)
        .add("_state", _state)
        .add("_target", _target)
        .add("_targetType", _targetType)
        .toString();
  }
}
