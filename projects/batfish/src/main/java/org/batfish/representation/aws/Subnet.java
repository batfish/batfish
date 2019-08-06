package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.Utils.addStaticRoute;
import static org.batfish.representation.aws.Utils.toStaticRoute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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

/**
 * Representation of an AWS subnet
 * https://docs.aws.amazon.com/cli/latest/reference/ec2/describe-subnets.html
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public class Subnet implements AwsVpcEntity, Serializable {

  @Nonnull private final Prefix _cidrBlock;

  @Nonnull private final String _subnetId;

  @Nonnull private final String _vpcId;

  @Nonnull private final Set<Long> _allocatedIps;

  private long _lastGeneratedIp;

  private transient String _internetGatewayId;

  private transient String _vpnGatewayId;

  @JsonCreator
  private static Subnet create(
      @Nullable @JsonProperty(JSON_KEY_CIDR_BLOCK) Prefix cidrBlock,
      @Nullable @JsonProperty(JSON_KEY_SUBNET_ID) String subnetId,
      @Nullable @JsonProperty(JSON_KEY_VPC_ID) String vpcId) {
    checkArgument(cidrBlock != null, "CIDR block cannot be null for subnet");
    checkArgument(subnetId != null, "Subnet id cannot be null for subnet");
    checkArgument(vpcId != null, "VPC id cannot be null for subnet");
    return new Subnet(cidrBlock, subnetId, vpcId);
  }

  Subnet(Prefix cidrBlock, String subnetId, String vpcId) {
    _cidrBlock = cidrBlock;
    _subnetId = subnetId;
    _vpcId = vpcId;

    _allocatedIps = new HashSet<>();
    // skipping (startIp+1) as it is used as the default gateway for instances in this subnet
    _lastGeneratedIp = _cidrBlock.getStartIp().asLong() + 1;
  }

  Set<Long> getAllocatedIps() {
    return _allocatedIps;
  }

  Ip getNextIp() {
    for (Long ipAsLong = _lastGeneratedIp + 1;
        ipAsLong < _cidrBlock.getEndIp().asLong();
        ipAsLong++) {
      if (!_allocatedIps.contains(ipAsLong)) {
        _allocatedIps.add(ipAsLong);
        _lastGeneratedIp = ipAsLong;
        return Ip.create(ipAsLong);
      }
    }
    // subnet's CIDR block out of IPs
    throw new BatfishException(String.format("%s subnet ran out of IPs", _subnetId));
  }

  Ip computeInstancesIfaceIp() {
    Long generatedIp = _cidrBlock.getStartIp().asLong() + 1L;
    _allocatedIps.add(generatedIp);
    _lastGeneratedIp = generatedIp;
    return Ip.create(generatedIp);
  }

  private NetworkAcl findMyNetworkAcl(Map<String, NetworkAcl> networkAcls) {
    List<NetworkAcl> matchingAcls =
        networkAcls.values().stream()
            .filter((NetworkAcl acl) -> acl.getVpcId().equals(_vpcId))
            .collect(Collectors.toList());

    if (matchingAcls.isEmpty()) {
      throw new BatfishException("Could not find a network ACL for subnet " + _subnetId);
    }

    if (matchingAcls.size() > 1) {
      List<String> aclIds =
          matchingAcls.stream().map(NetworkAcl::getId).collect(Collectors.toList());
      throw new BatfishException(
          String.format("Found multiple network ACLs %s for subnet %s", aclIds, _subnetId));
    }

    return matchingAcls.get(0);
  }

  // TODO: implement per-subnet routing
  @SuppressWarnings("unused")
  private RouteTable findMyRouteTable(Map<String, RouteTable> routeTables) {
    // All route tables for this VPC.
    List<RouteTable> sameVpcTables =
        routeTables.values().stream()
            .filter((RouteTable rt) -> rt.getVpcId().equals(_vpcId))
            .collect(Collectors.toList());

    // First we look for the unique route table with an association for this subnet.
    List<RouteTable> matchingRouteTables =
        sameVpcTables.stream()
            .filter(
                (RouteTable rt) ->
                    rt.getAssociations().stream()
                        .anyMatch(
                            (RouteTable.Association rtAssoc) ->
                                _subnetId.equals(rtAssoc.getSubnetId())))
            .collect(Collectors.toList());
    if (matchingRouteTables.size() > 1) {
      List<String> tableIds =
          matchingRouteTables.stream().map(RouteTable::getId).collect(Collectors.toList());
      throw new BatfishException(
          String.format(
              "Found multiple associated route tables %s for subnet %s", tableIds, _subnetId));
    }

    if (matchingRouteTables.size() == 1) {
      return matchingRouteTables.get(0);
    }

    // If no route table has an association with this subnet, find the unique main routing table.
    List<RouteTable> mainRouteTables =
        sameVpcTables.stream()
            .filter(
                (RouteTable rt) ->
                    rt.getAssociations().stream().anyMatch(RouteTable.Association::isMain))
            .collect(Collectors.toList());

    if (mainRouteTables.isEmpty()) {
      throw new BatfishException("Could not find a route table for subnet " + _subnetId);
    }

    if (mainRouteTables.size() > 1) {
      List<String> tableIds =
          mainRouteTables.stream().map(RouteTable::getId).collect(Collectors.toList());
      throw new BatfishException(
          String.format("Found multiple main route tables %s for subnet %s", tableIds, _subnetId));
    }

    return mainRouteTables.get(0);
  }

  @Nonnull
  public Prefix getCidrBlock() {
    return _cidrBlock;
  }

  @Override
  public String getId() {
    return _subnetId;
  }

  String getInternetGatewayId() {
    return _internetGatewayId;
  }

  public String getVpcId() {
    return _vpcId;
  }

  String getVpnGatewayId() {
    return _vpnGatewayId;
  }

  /**
   * Returns the {@link Configuration} node for this subnet.
   *
   * <p>We also do the work needed to connect to the VPC router here: Add an interface on the VPC
   * router and create the necessary static routes.
   */
  Configuration toConfigurationNode(
      AwsConfiguration awsConfiguration, Region region, Warnings warnings) {
    Configuration cfgNode = Utils.newAwsConfiguration(_subnetId, "aws");

    // add one interface that faces all instances (assumes a LAN)
    String instancesIfaceName = _subnetId;
    Ip instancesIfaceIp = computeInstancesIfaceIp();
    ConcreteInterfaceAddress instancesIfaceAddress =
        ConcreteInterfaceAddress.create(instancesIfaceIp, _cidrBlock.getPrefixLength());
    Interface subnetToInstances =
        Utils.newInterface(
            instancesIfaceName, cfgNode, instancesIfaceAddress, "To instances " + _subnetId);

    // generate a prefix for the link between the VPC router and the subnet
    Prefix vpcSubnetLinkPrefix = awsConfiguration.getNextGeneratedLinkSubnet();
    ConcreteInterfaceAddress subnetIfaceAddress =
        ConcreteInterfaceAddress.create(
            vpcSubnetLinkPrefix.getStartIp(), vpcSubnetLinkPrefix.getPrefixLength());
    ConcreteInterfaceAddress vpcIfaceAddress =
        ConcreteInterfaceAddress.create(
            vpcSubnetLinkPrefix.getEndIp(), vpcSubnetLinkPrefix.getPrefixLength());

    // add an interface that faces the VPC router
    String subnetIfaceName = _vpcId;
    Interface subnetToVpc =
        Utils.newInterface(subnetIfaceName, cfgNode, subnetIfaceAddress, "To VPC " + _vpcId);

    // add a corresponding interface on the VPC router facing the subnet
    Configuration vpcConfigNode = awsConfiguration.getConfigurationNodes().get(_vpcId);
    String vpcIfaceName = _subnetId;
    Utils.newInterface(vpcIfaceName, vpcConfigNode, vpcIfaceAddress, "To subnet " + _subnetId);

    // add a static route on the vpc router for this subnet
    addStaticRoute(vpcConfigNode, toStaticRoute(_cidrBlock, subnetIfaceAddress.getIp()));

    // Install a default static route towards the VPC router.
    addStaticRoute(cfgNode, toStaticRoute(Prefix.ZERO, vpcIfaceAddress.getIp()));

    // for public IPs in the subnet, add static routes on the VPC and Subnet nodes
    region.getNetworkInterfaces().values().stream()
        .filter(ni -> ni.getSubnetId().equals(_subnetId))
        .flatMap(ni -> ni.getPrivateIpAddresses().stream())
        .map(PrivateIpAddress::getPublicIp)
        .filter(Objects::nonNull)
        .forEach(
            pip -> {
              addStaticRoute(vpcConfigNode, toStaticRoute(pip, subnetIfaceAddress.getIp()));
              addStaticRoute(cfgNode, toStaticRoute(pip, subnetToInstances));
            });

    NetworkAcl myNetworkAcl = findMyNetworkAcl(region.getNetworkAcls());

    IpAccessList inAcl = myNetworkAcl.getIngressAcl();
    IpAccessList outAcl = myNetworkAcl.getEgressAcl();
    cfgNode.getIpAccessLists().put(inAcl.getName(), inAcl);
    cfgNode.getIpAccessLists().put(outAcl.getName(), outAcl);

    subnetToVpc.setIncomingFilter(inAcl);
    subnetToVpc.setOutgoingFilter(outAcl);

    cfgNode.getVendorFamily().getAws().setVpcId(_vpcId);
    cfgNode.getVendorFamily().getAws().setSubnetId(_subnetId);
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());

    return cfgNode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Subnet)) {
      return false;
    }
    Subnet subnet = (Subnet) o;
    return _lastGeneratedIp == subnet._lastGeneratedIp
        && Objects.equals(_cidrBlock, subnet._cidrBlock)
        && Objects.equals(_subnetId, subnet._subnetId)
        && Objects.equals(_vpcId, subnet._vpcId)
        && Objects.equals(_allocatedIps, subnet._allocatedIps)
        && Objects.equals(_internetGatewayId, subnet._internetGatewayId)
        && Objects.equals(_vpnGatewayId, subnet._vpnGatewayId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _cidrBlock,
        _subnetId,
        _vpcId,
        _allocatedIps,
        _lastGeneratedIp,
        _internetGatewayId,
        _vpnGatewayId);
  }
}
