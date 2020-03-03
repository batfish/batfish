package org.batfish.representation.aws;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.IpProtocol.ICMP;
import static org.batfish.datamodel.IpProtocol.TCP;
import static org.batfish.datamodel.IpProtocol.UDP;
import static org.batfish.representation.aws.Subnet.findSubnetNetworkAcl;
import static org.batfish.representation.aws.Utils.addNodeToSubnet;
import static org.batfish.representation.aws.Utils.addStaticRoute;
import static org.batfish.representation.aws.Utils.connect;
import static org.batfish.representation.aws.Utils.toStaticRoute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;

/** Represents an AWS NAT gateway */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class NatGateway implements AwsVpcEntity, Serializable {

  static final int NAT_PORT_LOWEST = 1024;

  static final int NAT_PORT_HIGHEST = 65535;

  static final List<IpProtocol> NAT_PROTOCOLS = ImmutableList.of(TCP, UDP, ICMP);

  /** Filter that drops all packets that are not NAT'd */
  static final String UNSUPPORTED_PROTOCOL_FILTER_NAME = "~DENY~UNSUPPORTED~PROTOCOLS~";

  static final IpAccessList UNSUPPORTED_PROTOCOL_FILTER =
      IpAccessList.builder()
          .setName(UNSUPPORTED_PROTOCOL_FILTER_NAME)
          .setLines(
              ExprAclLine.accepting(
                  "Allow supported protocols",
                  new MatchHeaderSpace(
                      HeaderSpace.builder().setIpProtocols(NAT_PROTOCOLS).build())),
              ExprAclLine.rejecting("Deny unsupported protocols", TrueExpr.INSTANCE))
          .build();

  /** Filter for dropping all packets that do not match a session */
  static final String NON_SESSION_PACKET_FILTER_NAME = "~DENY~NON~SESSION~PACKETS~";

  static final IpAccessList NON_SESSION_PACKET_FILTER =
      IpAccessList.builder()
          .setName(NON_SESSION_PACKET_FILTER_NAME)
          .setLines(ExprAclLine.rejecting("Deny-non-NAT-session-packets", TrueExpr.INSTANCE))
          .build();

  @Nonnull private final List<NatGatewayAddress> _natGatewayAddresses;

  @Nonnull private final String _natGatewayId;

  @Nonnull private final String _subnetId;

  @Nonnull private final Map<String, String> _tags;

  @Nonnull private final String _vpcId;

  @JsonCreator
  private static NatGateway create(
      @Nullable @JsonProperty(JSON_KEY_NAT_GATEWAY_ID) String natGatewayId,
      @Nullable @JsonProperty(JSON_KEY_SUBNET_ID) String subnetId,
      @Nullable @JsonProperty(JSON_KEY_VPC_ID) String vpcId,
      @Nullable @JsonProperty(JSON_KEY_TAGS) List<Tag> tags,
      @Nullable @JsonProperty(JSON_KEY_NAT_GATEWAY_ADDRESSES)
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

  @Nonnull
  public String getVpcId() {
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
              "Network interface %s not found for nat gateway %s.",
              networkInterfaceId, _natGatewayId));
      return cfgNode;
    }
    Subnet subnet = region.getSubnets().get(_subnetId);
    if (subnet == null) {
      warnings.redFlag(
          String.format("Subnet %s not found for nat gateway %s.", _subnetId, _natGatewayId));
      return cfgNode;
    }
    Interface subnetIface =
        addNodeToSubnet(cfgNode, networkInterface, subnet, awsConfiguration, warnings);

    Interface vpcIface = connectToVpc(cfgNode, awsConfiguration, region, warnings);

    if (vpcIface != null) {
      vpcIface.setIncomingTransformation(computeNatTransformation(getPrivateIp()));
      subnetIface.setFirewallSessionInterfaceInfo(
          new FirewallSessionInterfaceInfo(
              false, ImmutableList.of(subnetIface.getName()), null, null));

      // packets coming in for NAT'ing pass through the network ACL here because they don't traverse
      // the subnet node. outgoing packets traverse the subnet node (and the egress NACL)
      List<NetworkAcl> networkAcls =
          findSubnetNetworkAcl(region.getNetworkAcls(), _vpcId, _subnetId);
      // we do not warn if network Acls are not found. toConfigurationNode in subnet will do that
      if (!networkAcls.isEmpty()) {
        IpAccessList ingressAcl = networkAcls.get(0).getIngressAcl();
        cfgNode.getIpAccessLists().put(ingressAcl.getName(), ingressAcl);
        vpcIface.setIncomingFilter(ingressAcl);
      }

      // drop all packets that were not NAT'd
      vpcIface.setPostTransformationIncomingFilter(UNSUPPORTED_PROTOCOL_FILTER);
      cfgNode.getIpAccessLists().put(UNSUPPORTED_PROTOCOL_FILTER_NAME, UNSUPPORTED_PROTOCOL_FILTER);
    }

    // any packet that comes in on subnetIface and is not reverse-transformed should be dropped
    subnetIface.setIncomingFilter(NON_SESSION_PACKET_FILTER);
    cfgNode.getIpAccessLists().put(NON_SESSION_PACKET_FILTER_NAME, NON_SESSION_PACKET_FILTER);

    return cfgNode;
  }

  @Nullable
  private Interface connectToVpc(
      Configuration natGwCfg,
      ConvertedConfiguration awsConfiguration,
      Region region,
      Warnings warnings) {

    Vpc vpc = region.getVpcs().get(_vpcId);
    if (vpc == null) {
      warnings.redFlag(
          String.format(
              "VPC %s for nat gateway %s not found in region %s",
              _vpcId, _natGatewayId, region.getName()));
      return null;
    }

    Configuration vpcCfg = awsConfiguration.getConfigurationNodes().get(Vpc.nodeName(vpc.getId()));

    String vrfNameOnVpc = Vpc.vrfNameForLink(_natGatewayId);

    if (!vpcCfg.getVrfs().containsKey(vrfNameOnVpc)) {
      Vrf vrf = Vrf.builder().setOwner(vpcCfg).setName(vrfNameOnVpc).build();
      vpc.initializeVrf(vrf);
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
  static Transformation computeNatTransformation(Ip privateIp) {
    return new Transformation(
        new MatchHeaderSpace(HeaderSpace.builder().setIpProtocols(NAT_PROTOCOLS).build()),
        ImmutableList.of(
            TransformationStep.assignSourceIp(privateIp, privateIp),
            TransformationStep.assignSourcePort(NAT_PORT_LOWEST, NAT_PORT_HIGHEST)),
        null,
        null);
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
