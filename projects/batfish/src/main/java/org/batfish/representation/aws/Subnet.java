package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.Utils.addStaticRoute;
import static org.batfish.representation.aws.Utils.connect;
import static org.batfish.representation.aws.Utils.getInterfaceIp;
import static org.batfish.representation.aws.Utils.suffixedInterfaceName;
import static org.batfish.representation.aws.Utils.toStaticRoute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
import org.batfish.datamodel.Vrf;
import org.batfish.representation.aws.NetworkAcl.NetworkAclAssociation;
import org.batfish.representation.aws.Route.State;

/**
 * Representation of an AWS subnet
 * https://docs.aws.amazon.com/cli/latest/reference/ec2/describe-subnets.html
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public class Subnet implements AwsVpcEntity, Serializable {

  @Nonnull private final String _availabilityZone;

  @Nonnull private final Prefix _cidrBlock;

  @Nonnull private final String _subnetId;

  @Nonnull private final String _vpcId;

  @Nonnull private final Set<Long> _allocatedIps;

  private long _lastGeneratedIp;

  @JsonCreator
  private static Subnet create(
      @Nullable @JsonProperty(JSON_KEY_CIDR_BLOCK) Prefix cidrBlock,
      @Nullable @JsonProperty(JSON_KEY_SUBNET_ID) String subnetId,
      @Nullable @JsonProperty(JSON_KEY_VPC_ID) String vpcId,
      @Nullable @JsonProperty(JSON_KEY_AVAILABILITY_ZONE) String availabilityZone) {
    checkArgument(cidrBlock != null, "CIDR block cannot be null for subnet");
    checkArgument(subnetId != null, "Subnet id cannot be null for subnet");
    checkArgument(vpcId != null, "VPC id cannot be null for subnet");
    checkArgument(availabilityZone != null, "Availability zone cannot be null for subnet");
    return new Subnet(cidrBlock, subnetId, vpcId, availabilityZone);
  }

  Subnet(Prefix cidrBlock, String subnetId, String vpcId, String availabilityZone) {
    _cidrBlock = cidrBlock;
    _subnetId = subnetId;
    _vpcId = vpcId;
    _availabilityZone = availabilityZone;

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

  @VisibleForTesting
  static List<NetworkAcl> findMyNetworkAcl(
      Map<String, NetworkAcl> networkAcls, String vpcId, String subnetId) {
    List<NetworkAcl> subnetAcls =
        networkAcls.values().stream()
            .filter(acl -> acl.getVpcId().equals(vpcId))
            .filter(
                acl ->
                    acl.getAssociations().stream()
                        .map(NetworkAclAssociation::getSubnetId)
                        .anyMatch(subnetId::equals))
            .collect(ImmutableList.toImmutableList());

    if (!subnetAcls.isEmpty()) {
      return subnetAcls;
    }

    /*
     use the default for the VPC if we don't find an explicit association. this is mostly a
     defensive move, as AWS appears to provide explicit associations at the moment
    */
    return networkAcls.values().stream()
        .filter(acl -> acl.getVpcId().equals(vpcId) && acl.isDefault())
        .collect(ImmutableList.toImmutableList());
  }

  @Nonnull
  public Prefix getCidrBlock() {
    return _cidrBlock;
  }

  @Override
  public String getId() {
    return _subnetId;
  }

  @Nonnull
  public String getVpcId() {
    return _vpcId;
  }

  @Nonnull
  public String getAvailabilityZone() {
    return _availabilityZone;
  }

  /**
   * Returns the {@link Configuration} node for this subnet.
   *
   * <p>We also do the work needed to connect to the VPC router here: Add an interface on the VPC
   * router and create the necessary static routes.
   */
  Configuration toConfigurationNode(
      ConvertedConfiguration awsConfiguration, Region region, Warnings warnings) {
    Configuration cfgNode = Utils.newAwsConfiguration(nodeName(_subnetId), "aws");

    // add one interface that faces all instances (assumes a LAN)
    String instancesIfaceName = instancesInterfaceName(_subnetId);
    Ip instancesIfaceIp = computeInstancesIfaceIp();
    ConcreteInterfaceAddress instancesIfaceAddress =
        ConcreteInterfaceAddress.create(instancesIfaceIp, _cidrBlock.getPrefixLength());
    Interface subnetToInstances =
        Utils.newInterface(
            instancesIfaceName, cfgNode, instancesIfaceAddress, "To instances " + _subnetId);

    // connect to the VPC
    Configuration vpcConfigNode =
        awsConfiguration.getConfigurationNodes().get(Vpc.nodeName(_vpcId));
    connect(awsConfiguration, cfgNode, vpcConfigNode);

    // add a static route on the vpc router for this subnet;
    addStaticRoute(vpcConfigNode, toStaticRoute(_cidrBlock, getInterfaceIp(cfgNode, _vpcId)));

    // add network acls on the subnet node
    List<NetworkAcl> myNetworkAcls = findMyNetworkAcl(region.getNetworkAcls(), _vpcId, _subnetId);
    if (!myNetworkAcls.isEmpty()) {
      if (myNetworkAcls.size() > 1) {
        List<String> aclIds =
            myNetworkAcls.stream().map(NetworkAcl::getId).collect(ImmutableList.toImmutableList());
        warnings.redFlag(
            String.format(
                "Found multiple network ACLs %s for subnet %s. Using %s.",
                aclIds, _subnetId, myNetworkAcls.get(0).getId()));
      }
      IpAccessList inAcl = myNetworkAcls.get(0).getIngressAcl();
      IpAccessList outAcl = myNetworkAcls.get(0).getEgressAcl();
      cfgNode.getIpAccessLists().put(inAcl.getName(), inAcl);
      cfgNode.getIpAccessLists().put(outAcl.getName(), outAcl);

      // add ACLs to interface facing the vpc
      Interface vpcIfaceOnSubnet = cfgNode.getAllInterfaces().get(vpcConfigNode.getHostname());
      vpcIfaceOnSubnet.setIncomingFilter(inAcl);
      vpcIfaceOnSubnet.setOutgoingFilter(outAcl);
    } else {
      warnings.redFlag("Could not find a network ACL for subnet " + _subnetId);
    }

    // 1. connect the vpn gateway to the subnet if one exists
    // 2. create appropriate static routes
    Optional<VpnGateway> optVpnGateway = region.findVpnGateway(_vpcId);
    if (optVpnGateway.isPresent()) {
      Configuration vgwConfig =
          awsConfiguration.getConfigurationNodes().get(optVpnGateway.get().getId());
      connect(awsConfiguration, cfgNode, vgwConfig);
      Ip nhipOnVgw = getInterfaceIp(cfgNode, vgwConfig.getHostname());
      addStaticRoute(vgwConfig, toStaticRoute(_cidrBlock, nhipOnVgw));
    }

    // 1. connect the internet gateway if one exists
    // 2. for public IPs in the subnet, add static routes to enable inbound traffic
    //  - on internet gateway toward the subnet
    //  - on the subnet toward instances
    List<Ip> publicIps = findMyPublicIps(region);
    Optional<InternetGateway> optInternetGateway = region.findInternetGateway(_vpcId);
    if (optInternetGateway.isPresent()) {
      Configuration igwConfig =
          awsConfiguration.getConfigurationNodes().get(optInternetGateway.get().getId());
      connect(awsConfiguration, cfgNode, igwConfig);
      Ip nhipOnIgw = getInterfaceIp(cfgNode, igwConfig.getHostname());
      publicIps.forEach(
          pip -> {
            addStaticRoute(igwConfig, toStaticRoute(pip, nhipOnIgw));
            addStaticRoute(cfgNode, toStaticRoute(pip, subnetToInstances));
          });
    } else if (!publicIps.isEmpty()) {
      warnings.redFlag(
          String.format(
              "Internet gateway not found for subnet %s in vpc %s with public IPs %s",
              _subnetId, _vpcId, publicIps));
    }

    // process route tables to get outbound traffic going
    Optional<RouteTable> routeTable = region.findRouteTable(_vpcId, _subnetId);
    if (!routeTable.isPresent()) {
      warnings.redFlag(
          String.format("Route table not found for subnet %s in vpc %s", _subnetId, _vpcId));
    } else {
      routeTable
          .get()
          .getRoutes()
          .forEach(
              route ->
                  processRoute(
                      cfgNode,
                      region,
                      route,
                      vpcConfigNode,
                      optInternetGateway.orElse(null),
                      optVpnGateway.orElse(null),
                      awsConfiguration,
                      warnings));
    }

    cfgNode.getVendorFamily().getAws().setVpcId(_vpcId);
    cfgNode.getVendorFamily().getAws().setSubnetId(_subnetId);
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());

    return cfgNode;
  }

  private List<Ip> findMyPublicIps(Region region) {
    return region.getNetworkInterfaces().values().stream()
        .filter(ni -> ni.getSubnetId().equals(_subnetId))
        .flatMap(ni -> ni.getPrivateIpAddresses().stream())
        .map(PrivateIpAddress::getPublicIp)
        .filter(Objects::nonNull)
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Processes a route entry corresponding to {@code route}. Assumes that the gateways and primary
   * VPC interface (i.e., not corresponding to a peering connection) are already connected to subnet
   * node. For VPC connections, creates the relevant interface on the VPC and then connects it.
   */
  @VisibleForTesting
  void processRoute(
      Configuration cfgNode,
      Region region,
      Route route,
      Configuration vpcNode,
      @Nullable InternetGateway igw,
      @Nullable VpnGateway vgw,
      ConvertedConfiguration awsConfiguration,
      Warnings warnings) {

    StaticRoute.Builder sr =
        StaticRoute.builder()
            .setNetwork(route.getDestinationCidrBlock())
            .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
            .setMetric(Route.DEFAULT_STATIC_ROUTE_COST);

    if (route.getState() == State.BLACKHOLE) {
      addStaticRoute(cfgNode, sr.setNextHopInterface(Interface.NULL_INTERFACE_NAME).build());
      return;
    }

    switch (route.getTargetType()) {
      case Gateway:
        if (route.getTarget() == null) {
          warnings.redFlag("Route target is null for target type Gateway");
          return;
        }
        if (route.getTarget().equals("local")) {
          // To VPC
          addStaticRoute(cfgNode, sr.setNextHopIp(getInterfaceIp(vpcNode, _subnetId)).build());
          return;
        }
        if (igw != null && route.getTarget().equals(igw.getId())) {
          // To IGW
          addStaticRoute(
              cfgNode,
              sr.setNextHopIp(
                      getInterfaceIp(
                          awsConfiguration.getConfigurationNodes().get(igw.getId()), _subnetId))
                  .build());
          return;
        }
        if (vgw != null && route.getTarget().equals(vgw.getId())) {
          // To VGW
          addStaticRoute(
              cfgNode,
              sr.setNextHopIp(
                      getInterfaceIp(
                          awsConfiguration.getConfigurationNodes().get(vgw.getId()), _subnetId))
                  .build());
          return;
        }
        warnings.redFlag(
            String.format(
                "Unknown target %s specified in this route not accessible from this subnet",
                route.getTarget()));
        return;
      case VpcPeeringConnection:
        String connectionId = route.getTarget();
        if (connectionId == null) {
          warnings.redFlag(
              String.format("Route target is null for a VPC peering connection type: %s", route));
          return;
        }
        initializeVpcLink(
            awsConfiguration, cfgNode, vpcNode, region.getVpcs().get(_vpcId), connectionId);
        addStaticRoute(
            cfgNode,
            sr.setNextHopIp(getInterfaceIp(vpcNode, suffixedInterfaceName(cfgNode, connectionId)))
                .build());
        return;
      case TransitGateway:
        assert route.getTarget() != null; // suppress warning
        TransitGatewayVpcAttachment attachment =
            region.findTransitGatewayVpcAttachment(_vpcId, route.getTarget()).orElse(null);
        if (attachment == null) {
          warnings.redFlag(
              String.format(
                  "Transit gateway VPC attachment between %s and %s not found. Needed for route: %s",
                  _vpcId, route.getTarget(), route));
          return;
        }
        // this attachment is not reachable if it is not present in our availability zone
        if (!attachment.getAvailabilityZones(region).contains(_availabilityZone)) {
          addStaticRoute(cfgNode, sr.setNextHopInterface(Interface.NULL_INTERFACE_NAME).build());
          return;
        }
        initializeVpcLink(
            awsConfiguration, cfgNode, vpcNode, region.getVpcs().get(_vpcId), attachment.getId());
        addStaticRoute(
            cfgNode,
            sr.setNextHopIp(
                    getInterfaceIp(vpcNode, suffixedInterfaceName(cfgNode, attachment.getId())))
                .build());
        return;

      default:
        warnings.redFlag("Unsupported target type: " + route.getTargetType());
    }
  }

  /**
   * Initializes what is needed on the VPC to allow a subnet to use its link to a remote entities
   * (e.g., a VPC peering connection or a transit gateway attachment)
   */
  private void initializeVpcLink(
      ConvertedConfiguration awsConfiguration,
      Configuration cfgNode,
      Configuration vpcNode,
      Vpc vpc,
      String linkId) {
    // do nothing if have we processed this link before
    if (cfgNode.getAllInterfaces().containsKey(suffixedInterfaceName(vpcNode, linkId))) {
      return;
    }
    // the interface on the VPC node is in the link-specific VRF
    String vrfNameOnVpc = Vpc.vrfNameForLink(linkId);

    // have we created a VRF for this link on the VPC node before?
    if (!vpcNode.getVrfs().containsKey(vrfNameOnVpc)) {
      Vrf vrf = Vrf.builder().setOwner(vpcNode).setName(vrfNameOnVpc).build();
      vpc.initializeVrf(vrf);
    }

    connect(
        awsConfiguration,
        cfgNode,
        cfgNode.getDefaultVrf().getName(),
        vpcNode,
        vrfNameOnVpc,
        linkId);

    addStaticRoute(
        vpcNode.getVrfs().get(vrfNameOnVpc),
        toStaticRoute(_cidrBlock, getInterfaceIp(cfgNode, suffixedInterfaceName(vpcNode, linkId))));
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
        && Objects.equals(_availabilityZone, subnet._availabilityZone)
        && Objects.equals(_allocatedIps, subnet._allocatedIps);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _cidrBlock, _subnetId, _vpcId, _availabilityZone, _allocatedIps, _lastGeneratedIp);
  }

  public static String nodeName(String subnetId) {
    return subnetId;
  }

  public static String instancesInterfaceName(String subnetId) {
    return subnetId;
  }
}
