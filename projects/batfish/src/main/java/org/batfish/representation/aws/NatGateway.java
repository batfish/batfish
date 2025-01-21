package org.batfish.representation.aws;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.IpProtocol.ICMP;
import static org.batfish.datamodel.IpProtocol.TCP;
import static org.batfish.datamodel.IpProtocol.UDP;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocols;
import static org.batfish.representation.aws.AwsLocationInfoUtils.INFRASTRUCTURE_LOCATION_INFO;
import static org.batfish.representation.aws.Subnet.findSubnetNetworkAcl;
import static org.batfish.representation.aws.Utils.addNodeToSubnet;
import static org.batfish.representation.aws.Utils.addStaticRoute;
import static org.batfish.representation.aws.Utils.connect;
import static org.batfish.representation.aws.Utils.createPublicIpsRefBook;
import static org.batfish.representation.aws.Utils.toStaticRoute;
import static org.batfish.specifier.Location.interfaceLinkLocation;
import static org.batfish.specifier.Location.interfaceLocation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.FirewallSessionInterfaceInfo.Action;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;

/**
 * Represents an AWS NAT gateway:
 * https://docs.aws.amazon.com/vpc/latest/userguide/vpc-nat-gateway.html
 *
 * <p>Design doc on modeling is at
 * https://docs.google.com/document/d/1QzwRM6XmTGQcJkmcbDfxSgNDGDIAhC7_B3w-JQy6RMM/, but the overall
 * approach is: 1) NatGateways become nodes in their subnet (duh!); 2) In addition to connecting to
 * their subnet node, they also connect to the VPC router directly; 3) On the VPC router the
 * interface that connects to the NAT gateway is in its own VRF; 4) Subnets that send traffic to
 * this NAT connect to the VPC on an interface that is also in this VRF. These subnet-to-VPC links
 * are in addition subnet-to-VPC links that exist to connect subnets to other subnets and gateways.
 * The VRF-based design helps isolate different flows at the VPC since different subnets send
 * different types of traffic to different gateways.
 *
 * <p>Packets to be NAT'd appear on the interface to the VPC, get transformed, and leave on the
 * interface to the subnet. On the return path, packets arrive at the subnet-facing interface, and
 * those that have a matching NAT session are forwarded to the VPC-facing interface.
 *
 * <p>The NAT does not provide service to instances within its own subnet. This limitation agrees
 * with testing.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class NatGateway implements AwsVpcEntity, Serializable {

  static final int NAT_PORT_LOWEST = 1024;

  static final int NAT_PORT_HIGHEST = 65535;

  static final List<IpProtocol> NAT_PROTOCOLS = ImmutableList.of(TCP, UDP, ICMP);

  /** AclLine that drops unsupported NAT protocols */
  static AclLine UNSUPPORTED_PROTOCOL_ACL_LINE =
      ExprAclLine.rejecting(
          TraceElement.of("Denied IP protocols NOT supported by the NAT gateway"),
          AclLineMatchExprs.not(matchIpProtocols(NAT_PROTOCOLS)));

  /**
   * Post transformation filter on the interface facing the subnet that drops all illegal packets
   * (those from within the subnet and those without an active NAT session).
   */
  static final String ILLEGAL_PACKET_FILTER_NAME = "~ILLEGAL~PACKET~FILTER~";

  /** Incoming filter on the interface facing the VPC */
  static final String INCOMING_NAT_FILTER_NAME = "~INCOMING~NAT~FILTER~";

  private final @Nonnull List<NatGatewayAddress> _natGatewayAddresses;

  private final @Nonnull String _natGatewayId;

  private final @Nonnull String _subnetId;

  private final @Nonnull Map<String, String> _tags;

  private final @Nonnull String _vpcId;

  @JsonCreator
  private static NatGateway create(
      @JsonProperty(JSON_KEY_NAT_GATEWAY_ID) @Nullable String natGatewayId,
      @JsonProperty(JSON_KEY_SUBNET_ID) @Nullable String subnetId,
      @JsonProperty(JSON_KEY_VPC_ID) @Nullable String vpcId,
      @JsonProperty(JSON_KEY_TAGS) @Nullable List<Tag> tags,
      @JsonProperty(JSON_KEY_NAT_GATEWAY_ADDRESSES) @Nullable
          List<NatGatewayAddress> natGatewayAddresses) {
    checkArgument(natGatewayId != null, "NAT gateway id cannot be null");
    checkArgument(subnetId != null, "Subnet id cannot be null for nat gateway");
    checkArgument(vpcId != null, "VPC id cannot be null for nat gateway");
    checkArgument(natGatewayAddresses != null, "Nat gateway addresses cannot be null");
    checkArgument(natGatewayAddresses.size() == 1, "Nat gateways must have exactly one address");

    return new NatGateway(
        natGatewayId,
        subnetId,
        vpcId,
        natGatewayAddresses,
        firstNonNull(tags, ImmutableList.<Tag>of()).stream()
            .collect(ImmutableMap.toImmutableMap(Tag::getKey, Tag::getValue)));
  }

  NatGateway(
      String natGatewayId,
      String subnetId,
      String vpcId,
      List<NatGatewayAddress> natGatewayAddresses,
      Map<String, String> tags) {
    _natGatewayId = natGatewayId;
    _subnetId = subnetId;
    _vpcId = vpcId;
    _natGatewayAddresses = natGatewayAddresses;
    _tags = tags;
  }

  @Override
  public String getId() {
    return _natGatewayId;
  }

  @Nonnull
  List<NatGatewayAddress> getNatGatewayAddresses() {
    return _natGatewayAddresses;
  }

  @Nonnull
  String getSubnetId() {
    return _subnetId;
  }

  public @Nonnull String getVpcId() {
    return _vpcId;
  }

  Configuration toConfigurationNode(
      ConvertedConfiguration awsConfiguration, Region region, Warnings warnings) {
    Configuration cfgNode =
        Utils.newAwsConfiguration(_natGatewayId, "aws", _tags, DeviceModel.AWS_NAT_GATEWAY);
    cfgNode.getVendorFamily().getAws().setVpcId(_vpcId);
    cfgNode.getVendorFamily().getAws().setSubnetId(_subnetId);
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());

    String networkInterfaceId = _natGatewayAddresses.get(0).getNetworkInterfaceId();
    NetworkInterface networkInterface = region.getNetworkInterfaces().get(networkInterfaceId);
    if (networkInterface == null) {
      warnings.redFlag(
          String.format(
              "Network interface %s not found for NAT gateway %s.",
              networkInterfaceId, _natGatewayId));
      return cfgNode;
    }
    createPublicIpsRefBook(Collections.singleton(networkInterface), cfgNode);

    Subnet subnet = region.getSubnets().get(_subnetId);
    if (subnet == null) {
      warnings.redFlagf("Subnet %s not found for NAT gateway %s.", _subnetId, _natGatewayId);
      return cfgNode;
    }
    Interface ifaceToSubnet =
        addNodeToSubnet(cfgNode, networkInterface, subnet, awsConfiguration, warnings);
    ifaceToSubnet.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(
            Action.FORWARD_OUT_IFACE, ImmutableList.of(ifaceToSubnet.getName()), null, null));

    // post transformation filter on the interface to the subnet
    IpAccessList postTransformationFilter =
        computePostTransformationIllegalPacketFilter(getPrivateIp());
    cfgNode.getIpAccessLists().put(postTransformationFilter.getName(), postTransformationFilter);
    ifaceToSubnet.setPostTransformationIncomingFilter(postTransformationFilter);

    Interface ifaceToVpc = connectToVpc(cfgNode, awsConfiguration, region, warnings);
    if (ifaceToVpc != null) {
      // packets coming into the NAT from instances outside the subnet and the reverse traffic going
      // to those instances should be subjected to the network ACL since they do not traverse the
      // subnet node where we usually place the ACL.
      List<NetworkAcl> networkAcls =
          findSubnetNetworkAcl(region.getNetworkAcls(), _vpcId, _subnetId);
      IpAccessList egressAcl = null;
      // we do not warn if network Acls are not found. toConfigurationNode in subnet will do that
      if (!networkAcls.isEmpty()) {
        installIncomingFilter(cfgNode, ifaceToVpc, networkAcls.get(0).getIngressAcl());

        egressAcl = networkAcls.get(0).getEgressAcl();
        cfgNode.getIpAccessLists().put(egressAcl.getName(), egressAcl);
        // no need to install this ACL as the outgoing filter on the interface to the VPC because we
        // install it below as the outgoing ACL in the FirewallSessionInterfaceInfo for the
        // interface, where it will hit all reverse traffic
      }

      ifaceToVpc.setIncomingTransformation(computeOutgoingNatTransformation(getPrivateIp()));
      ifaceToVpc.setFirewallSessionInterfaceInfo(
          new FirewallSessionInterfaceInfo(
              Action.FORWARD_OUT_IFACE,
              ImmutableList.of(ifaceToVpc.getName()),
              null,
              egressAcl == null ? null : egressAcl.getName()));
    }

    // Create LocationInfo the interface
    cfgNode.setLocationInfo(
        ImmutableMap.of(
            interfaceLocation(ifaceToSubnet),
            INFRASTRUCTURE_LOCATION_INFO,
            interfaceLinkLocation(ifaceToSubnet),
            INFRASTRUCTURE_LOCATION_INFO));

    return cfgNode;
  }

  /**
   * Connects the NAT gateway to its VPC. Creates the right VRF on the VPC if it is not there
   * already, creates the interfaces on both nodes for the link, and puts a static route to the NAT
   * in the right VPC VRF.
   *
   * @return the interface on the NAT gateway that connects to the VPC, or null if the VPC is not
   *     found
   */
  @VisibleForTesting
  @Nullable
  Interface connectToVpc(
      Configuration natGwCfg,
      ConvertedConfiguration awsConfiguration,
      Region region,
      Warnings warnings) {

    Vpc vpc = region.getVpcs().get(_vpcId);
    if (vpc == null) {
      warnings.redFlag(
          String.format(
              "VPC %s for NAT gateway %s not found in region %s",
              _vpcId, _natGatewayId, region.getName()));
      return null;
    }

    Configuration vpcCfg = awsConfiguration.getNode(Vpc.nodeName(vpc.getId()));
    if (vpcCfg == null) {
      warnings.redFlag(
          String.format(
              "Configuration for VPC %s not found while building the NAT gateway node %s",
              _vpcId, _natGatewayId));
      return null;
    }

    String vrfNameOnVpc = Vpc.vrfNameForLink(_natGatewayId);

    if (!vpcCfg.getVrfs().containsKey(vrfNameOnVpc)) {
      warnings.redFlagf("VRF %s not found on VPC %s", vrfNameOnVpc, _vpcId);
      return null;
    }

    connect(awsConfiguration, natGwCfg, DEFAULT_VRF_NAME, vpcCfg, vrfNameOnVpc, "");

    addStaticRoute(
        vpcCfg.getVrfs().get(vrfNameOnVpc),
        toStaticRoute(
            Prefix.ZERO,
            Utils.interfaceNameToRemote(natGwCfg),
            Utils.getInterfaceLinkLocalIp(natGwCfg, Utils.interfaceNameToRemote(vpcCfg))));

    return natGwCfg.getAllInterfaces().get(Utils.interfaceNameToRemote(vpcCfg));
  }

  /**
   * Transform the source address of all supported packets to the supplied *private* Ip.
   *
   * <p>The internet gateway will then transform the source to the public IP if the packet is headed
   * outside.
   */
  @VisibleForTesting
  static Transformation computeOutgoingNatTransformation(Ip privateIp) {
    return Transformation.when(
            new MatchHeaderSpace(HeaderSpace.builder().setIpProtocols(NAT_PROTOCOLS).build()))
        .apply(
            TransformationStep.assignSourceIp(privateIp, privateIp),
            TransformationStep.assignSourcePort(NAT_PORT_LOWEST, NAT_PORT_HIGHEST))
        .build();
  }

  @VisibleForTesting
  static IpAccessList computePostTransformationIllegalPacketFilter(Ip privateIp) {
    return IpAccessList.builder()
        .setName(ILLEGAL_PACKET_FILTER_NAME)
        .setLines(
            ExprAclLine.rejecting(
                TraceElement.of("Denied packets where source IP is the NAT gateway's private IP"),
                new MatchHeaderSpace(
                    HeaderSpace.builder()
                        .setSrcIps(ImmutableList.of(IpWildcard.create(privateIp)))
                        .build())),
            ExprAclLine.rejecting(
                TraceElement.of("Denied packets that did NOT match an active NAT session"),
                new MatchHeaderSpace(
                    HeaderSpace.builder()
                        .setDstIps(ImmutableList.of(IpWildcard.create(privateIp)))
                        .build())),
            ExprAclLine.accepting(
                TraceElement.of("Permitted packets transformed by the NAT gateway"),
                TrueExpr.INSTANCE))
        .build();
  }

  /**
   * Install an incoming filter on the interface to the VPC, where packets meant for NAT'ing first
   * appear. This filter is a combination of dropping unsupported protocols followed by the ingress
   * network ACL.
   */
  @VisibleForTesting
  static void installIncomingFilter(
      Configuration cfgNode, Interface ifaceToVpc, IpAccessList ingressAcl) {
    IpAccessList filter =
        IpAccessList.builder()
            .setName(INCOMING_NAT_FILTER_NAME)
            .setLines(
                UNSUPPORTED_PROTOCOL_ACL_LINE,
                new AclAclLine(ingressAcl.getName(), ingressAcl.getName()))
            .build();
    cfgNode.getIpAccessLists().put(ingressAcl.getName(), ingressAcl);
    cfgNode.getIpAccessLists().put(filter.getName(), filter);
    ifaceToVpc.setIncomingFilter(filter);
  }

  /**
   * Returns the private IP of this NAT gateway. Assumes that there is only one NatGatewayAddress
   */
  @Nonnull
  Ip getPrivateIp() {
    return _natGatewayAddresses.get(0).getPrivateIp();
  }

  /**
   * Returns the public IP of this NAT gateway. Assumes that there is only one NatGatewayAddress.
   */
  @Nonnull
  Ip getPublicIp() {
    return _natGatewayAddresses.get(0).getPublicIp();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NatGateway)) {
      return false;
    }
    NatGateway that = (NatGateway) o;
    return Objects.equals(_natGatewayAddresses, that._natGatewayAddresses)
        && Objects.equals(_natGatewayId, that._natGatewayId)
        && Objects.equals(_subnetId, that._subnetId)
        && Objects.equals(_tags, that._tags)
        && Objects.equals(_vpcId, that._vpcId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_natGatewayAddresses, _natGatewayId, _subnetId, _vpcId, _tags);
  }
}
