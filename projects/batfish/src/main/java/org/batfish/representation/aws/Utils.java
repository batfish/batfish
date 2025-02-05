package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Comparator.naturalOrder;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;
import static org.batfish.representation.aws.AwsConfiguration.AWS_BACKBONE_ASN;
import static org.batfish.representation.aws.AwsConfiguration.BACKBONE_FACING_INTERFACE_NAME;
import static org.batfish.representation.aws.AwsConfiguration.BACKBONE_PEERING_ASN;
import static org.batfish.representation.aws.AwsConfiguration.LINK_LOCAL_IP;
import static org.batfish.representation.aws.AwsVpcEntity.TAG_NAME;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.vendor_family.AwsFamily;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.GeneratedRefBookUtils;
import org.batfish.referencelibrary.GeneratedRefBookUtils.BookType;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.representation.aws.IpPermissions.AddressType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** A collection for utilities for AWS vendor model */
@ParametersAreNonnullByDefault
public final class Utils {
  // TODO: do any of these matter?
  private static final int DEFAULT_EBGP_ADMIN = 20;
  private static final int DEFAULT_IBGP_ADMIN = 200;
  private static final int DEFAULT_LOCAL_BGP_ADMIN = 200;

  static BgpProcess makeBgpProcess(Ip routerId, Vrf vrf) {
    return BgpProcess.builder()
        .setRouterId(routerId)
        .setVrf(vrf)
        .setEbgpAdminCost(DEFAULT_EBGP_ADMIN)
        .setIbgpAdminCost(DEFAULT_IBGP_ADMIN)
        .setLocalAdminCost(DEFAULT_LOCAL_BGP_ADMIN)
        // arbitrary values below since does not export from BGP RIB
        .setLocalOriginationTypeTieBreaker(LocalOriginationTypeTieBreaker.NO_PREFERENCE)
        .setNetworkNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
        .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
        .build();
  }

  static final Statement ACCEPT_ALL_BGP =
      new If(
          new MatchProtocol(RoutingProtocol.BGP),
          ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
          ImmutableList.of(Statements.ExitReject.toStaticStatement()));

  static final Statement ACCEPT_ALL_BGP_AND_STATIC =
      new If(
          new Disjunction(
              new MatchProtocol(RoutingProtocol.BGP), new MatchProtocol(RoutingProtocol.STATIC)),
          ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
          ImmutableList.of(Statements.ExitReject.toStaticStatement()));

  static void checkNonNull(@Nullable Object value, String fieldName, String objectType) {
    if (value == null) {
      throw new IllegalArgumentException(
          String.format("Field '%s' must exist for '%s", fieldName, objectType));
    }
  }

  static String publicIpAddressGroupName(NetworkInterface iface) {
    return String.format("%s (%s)", iface.getDescription(), iface.getId());
  }

  /** Creates a generated reference book with the public IPs of the network interfaces */
  static void createPublicIpsRefBook(
      Collection<NetworkInterface> networkInterfaces, Configuration cfgNode) {
    String publicIpBookName =
        GeneratedRefBookUtils.getName(cfgNode.getHostname(), BookType.PublicIps);
    checkArgument(
        !cfgNode.getGeneratedReferenceBooks().containsKey(publicIpBookName),
        "Generated reference book for public IPs already exists for node %s",
        cfgNode.getHostname());
    List<AddressGroup> publicIpAddressGroups =
        networkInterfaces.stream()
            .map(
                iface ->
                    new AddressGroup(
                        iface.getPrivateIpAddresses().stream()
                            .filter(privIp -> privIp.getPublicIp() != null)
                            .map(privIp -> privIp.getPublicIp().toString())
                            .collect(ImmutableSortedSet.toImmutableSortedSet(naturalOrder())),
                        publicIpAddressGroupName(iface)))
            .filter(ag -> !ag.getAddresses().isEmpty())
            .collect(ImmutableList.toImmutableList());
    if (!publicIpAddressGroups.isEmpty()) {
      cfgNode
          .getGeneratedReferenceBooks()
          .put(
              publicIpBookName,
              ReferenceBook.builder(publicIpBookName)
                  .setAddressGroups(publicIpAddressGroups)
                  .build());
    }
  }

  /** Prefer variants that provide {@link DeviceModel}. */
  @VisibleForTesting
  static Configuration newAwsConfiguration(String name, String domainName) {
    return newAwsConfiguration(name, domainName, Collections.emptyMap(), null);
  }

  static Configuration newAwsConfiguration(String name, String domainName, DeviceModel model) {
    return newAwsConfiguration(name, domainName, Collections.emptyMap(), model);
  }

  static Configuration newAwsConfiguration(
      String name, String domainName, Map<String, String> tags, @Nullable DeviceModel model) {
    Configuration c =
        Configuration.builder()
            .setHostname(name)
            .setDomainName(domainName)
            .setConfigurationFormat(ConfigurationFormat.AWS)
            .setDefaultInboundAction(LineAction.PERMIT)
            .setDefaultCrossZoneAction(LineAction.PERMIT)
            .setDeviceModel(model)
            .setHumanName(tags.get(TAG_NAME))
            .build();
    Vrf.builder().setName(Configuration.DEFAULT_VRF_NAME).setOwner(c).build();
    c.getVendorFamily().setAws(new AwsFamily());
    return c;
  }

  /** Creates a new interface on {@code c} with the provided name, address, and description. */
  static Interface newInterface(
      String name, Configuration c, @Nullable InterfaceAddress primaryAddress, String description) {
    return newInterface(name, c, c.getDefaultVrf().getName(), primaryAddress, description);
  }

  /**
   * Creates a new interface on {@code c} in the provided VRF name with the provided name, address,
   * and description.
   */
  static Interface newInterface(
      String name,
      Configuration c,
      String vrfName,
      @Nullable InterfaceAddress primaryAddress,
      String description) {
    checkArgument(
        c.getVrfs().containsKey(vrfName), "VRF %s does not exist on %s", vrfName, c.getHostname());
    return Interface.builder()
        .setName(name)
        .setOwner(c)
        .setVrf(c.getVrfs().get(vrfName))
        .setAddress(primaryAddress)
        .setDescription(description)
        .build();
  }

  /**
   * Parse the protocol string from an SG rule or a NACL rule into an {@link IpProtocol}, or {@code
   * null} if the protocol is "-1", which means "All traffic".
   *
   * @param ipProtocolAsString The protocol string, possible values are "tcp", "udp", "icmp",
   *     "icmpv6", "-1" or protocol numbers ranging 0-255.
   * @return {@link IpProtocol} or {@code null} parsed from the protocol string
   */
  public static @Nullable IpProtocol toIpProtocol(String ipProtocolAsString) {
    switch (ipProtocolAsString) {
      case "tcp":
        return IpProtocol.TCP;
      case "udp":
        return IpProtocol.UDP;
      case "icmp":
        return IpProtocol.ICMP;
      case "icmpv6":
        throw new IllegalStateException(
            "icmpv6 protocol should have been handled before calling this function.");
      case "-1":
        return null;
      default:
        try {
          int ipProtocolAsInt = Integer.parseInt(ipProtocolAsString);
          return IpProtocol.fromNumber(ipProtocolAsInt);
        } catch (NumberFormatException e) {
          throw new BatfishException(
              "Could not convert AWS IP protocol: \""
                  + ipProtocolAsString
                  + "\" to batfish Ip Protocol",
              e);
        }
    }
  }

  /** Adds a static route on {@code cfgNode} */
  static void addStaticRoute(Configuration cfgNode, StaticRoute staticRoute) {
    cfgNode.getDefaultVrf().getStaticRoutes().add(staticRoute);
  }

  /** Adds a static route on {@code vrf} */
  static void addStaticRoute(Vrf vrf, StaticRoute staticRoute) {
    vrf.getStaticRoutes().add(staticRoute);
  }

  static @Nonnull StaticRoute toStaticRoute(Prefix targetPrefix, Ip nextHopIp) {
    return toStaticRoute(targetPrefix, nextHopIp, false);
  }

  static @Nonnull StaticRoute toStaticRoute(
      Prefix targetPrefix, Ip nextHopIp, boolean nonForwarding) {
    return toStaticRoute(targetPrefix, null, nextHopIp, nonForwarding);
  }

  static @Nonnull StaticRoute toStaticRoute(Prefix targetPrefix, String nextHopInterfaceName) {
    return toStaticRoute(targetPrefix, nextHopInterfaceName, null, false);
  }

  static @Nonnull StaticRoute toStaticRoute(
      Prefix targetPrefix, String nextHopInterfaceName, boolean nonForwarding) {
    return toStaticRoute(targetPrefix, nextHopInterfaceName, null, nonForwarding);
  }

  static @Nonnull StaticRoute toStaticRoute(
      Prefix targetPrefix, String nextHopInterfaceName, Ip nextHopIp) {
    return toStaticRoute(targetPrefix, nextHopInterfaceName, nextHopIp, false);
  }

  static @Nonnull StaticRoute toStaticRoute(
      Prefix targetPrefix,
      @Nullable String nextHopInterfaceName,
      @Nullable Ip nextHopIp,
      boolean nonForwarding) {
    return StaticRoute.builder()
        .setNetwork(targetPrefix)
        .setNextHop(NextHop.legacyConverter(nextHopInterfaceName, nextHopIp))
        .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
        .setMetric(Route.DEFAULT_STATIC_ROUTE_COST)
        .setNonForwarding(nonForwarding)
        .build();
  }

  /**
   * Creates a subnet link between the two nodes represented by {@code cfgNode1} and {@code
   * cfgNode2}. Create a new interface on each node in the supplied VRF names and assigns it a name
   * that corresponds to the name of the other node plus {@code ifaceNameSuffix}
   */
  static void connect(
      ConvertedConfiguration awsConfiguration,
      Configuration cfgNode1,
      String vrfName1,
      Configuration cfgNode2,
      String vrfName2,
      String ifaceNameSuffix) {
    String ifaceName1 = interfaceNameToRemote(cfgNode2, ifaceNameSuffix);
    Utils.newInterface(
        ifaceName1, cfgNode1, vrfName1, LinkLocalAddress.of(LINK_LOCAL_IP), "To " + ifaceName1);

    String ifaceName2 = interfaceNameToRemote(cfgNode1, ifaceNameSuffix);
    Utils.newInterface(
        ifaceName2, cfgNode2, vrfName2, LinkLocalAddress.of(LINK_LOCAL_IP), "To " + ifaceName2);

    addLayer1Edge(
        awsConfiguration, cfgNode1.getHostname(), ifaceName1, cfgNode2.getHostname(), ifaceName2);
  }

  /**
   * Creates a subnet link between the two nodes represented by {@code cfgNode1} and {@code
   * cfgNode2}. Create a new interface on each node for this purpose and assigns it a name that
   * corresponds to the name of the other node.
   */
  static void connect(
      ConvertedConfiguration awsConfiguration, Configuration cfgNode1, Configuration cfgNode2) {
    connect(
        awsConfiguration,
        cfgNode1,
        cfgNode1.getDefaultVrf().getName(),
        cfgNode2,
        cfgNode2.getDefaultVrf().getName(),
        "");
  }

  /**
   * Connects VPC-level gateways such as Internet gateway, VPN gateway, VPC endpoint gateway to its
   * VPC and adds static routes on both nodes.
   *
   * <p>The VPC for such gateways must be in the same region and account, so the search for
   * connecting VPC is performed within that scope.
   *
   * @retruns The Interface on the gateway for the new link or null if the VPC is not found.
   */
  static @Nullable Interface connectGatewayToVpc(
      String gatewayId,
      Configuration gatewayCfg,
      String vpcId,
      ConvertedConfiguration awsConfiguration,
      Region region,
      Warnings warnings) {

    Vpc vpc = region.getVpcs().get(vpcId);
    if (vpc == null) {
      warnings.redFlagf("VPC with id %s not found in region %s", vpcId, region.getName());
      return null;
    }

    Configuration vpcCfg = awsConfiguration.getNode(Vpc.nodeName(vpc.getId()));
    if (vpcCfg == null) {
      warnings.redFlagf("Configuration for VPC with id %s not found", vpcId);
      return null;
    }

    String vrfNameOnVpc = Vpc.vrfNameForLink(gatewayId);
    if (!vpcCfg.getVrfs().containsKey(vrfNameOnVpc)) {
      warnings.redFlagf("VRF %s not found on VPC %s", vrfNameOnVpc, vpcId);
      return null;
    }

    connect(awsConfiguration, gatewayCfg, DEFAULT_VRF_NAME, vpcCfg, vrfNameOnVpc, "");

    addStaticRoute(
        vpcCfg.getVrfs().get(vrfNameOnVpc),
        toStaticRoute(
            Prefix.ZERO,
            Utils.interfaceNameToRemote(gatewayCfg),
            Utils.getInterfaceLinkLocalIp(gatewayCfg, Utils.interfaceNameToRemote(vpcCfg))));

    vpc.getCidrBlockAssociations()
        .forEach(
            prefix ->
                addStaticRoute(
                    gatewayCfg,
                    toStaticRoute(
                        prefix,
                        Utils.interfaceNameToRemote(vpcCfg),
                        Utils.getInterfaceLinkLocalIp(
                            vpcCfg, Utils.interfaceNameToRemote(gatewayCfg)))));

    return gatewayCfg.getAllInterfaces().get(Utils.interfaceNameToRemote(vpcCfg));
  }

  static String interfaceNameToRemote(Configuration remoteCfg) {
    return interfaceNameToRemote(remoteCfg.getHostname(), "");
  }

  static String interfaceNameToRemote(Configuration remoteCfg, String suffix) {
    return interfaceNameToRemote(remoteCfg.getHostname(), suffix);
  }

  static String interfaceNameToRemote(String remoteCfgHostname, String suffix) {
    return suffix.isEmpty() ? remoteCfgHostname : remoteCfgHostname + "-" + suffix;
  }

  /** Adds a bidirectional layer1 edge between the interfaces and nodes */
  static void addLayer1Edge(
      ConvertedConfiguration awsConfiguration,
      String node1,
      String iface1,
      String node2,
      String iface2) {
    awsConfiguration.addEdge(node1, iface1, node2, iface2);
    awsConfiguration.addEdge(node2, iface2, node1, iface1);
  }

  private static InterfaceAddress getInterfaceAddress(
      Configuration configuration, String ifaceName) {
    Interface iface = configuration.getAllInterfaces().get(ifaceName);
    checkArgument(
        iface != null,
        "Interface name '%s' not found on node %s",
        ifaceName,
        configuration.getHostname());
    return iface.getAddress();
  }

  /**
   * Returns the IP address of the interface with name {@code ifaceName} in {@code configuration}.
   * Throws an exception if the interface is not present or does not have an assigned address
   */
  static @Nonnull Ip getInterfaceLinkLocalIp(Configuration configuration, String ifaceName) {
    InterfaceAddress ifaceAddress = getInterfaceAddress(configuration, ifaceName);
    if (ifaceAddress instanceof LinkLocalAddress) {
      return ((LinkLocalAddress) ifaceAddress).getIp();
    }
    throw new IllegalArgumentException(
        String.format(
            "Interface %s on %s does not have a link local address",
            ifaceName, configuration.getHostname()));
  }

  /**
   * Adds a configuration node to subnet using its network interface.
   *
   * @return The vendor-independent interface that was created to make the connection.
   */
  static Interface addNodeToSubnet(
      Configuration cfgNode,
      NetworkInterface netInterface,
      Subnet subnet,
      ConvertedConfiguration awsConfiguration,
      Warnings warnings) {
    ImmutableSet.Builder<ConcreteInterfaceAddress> ifaceAddressesBuilder =
        new ImmutableSet.Builder<>();

    Prefix ifaceSubnet = subnet.getCidrBlock();
    Ip defaultGatewayAddress = subnet.computeInstancesIfaceIp();
    StaticRoute defaultRoute =
        StaticRoute.builder()
            .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
            .setMetric(Route.DEFAULT_STATIC_ROUTE_COST)
            .setNextHopIp(defaultGatewayAddress)
            .setNetwork(Prefix.ZERO)
            .build();
    cfgNode.getDefaultVrf().getStaticRoutes().add(defaultRoute);

    for (PrivateIpAddress privateIp : netInterface.getPrivateIpAddresses()) {
      if (!ifaceSubnet.containsIp(privateIp.getPrivateIp())) {
        warnings.pedantic(
            String.format(
                "Instance subnet \"%s\" does not contain private ip: \"%s\"",
                ifaceSubnet, privateIp));
        continue;
      }

      if (privateIp.getPrivateIp().equals(ifaceSubnet.getEndIp())) {
        warnings.pedantic(
            String.format(
                "Expected end address \"%s\" to be used by generated subnet node", privateIp));
        continue;
      }

      ConcreteInterfaceAddress address =
          ConcreteInterfaceAddress.create(privateIp.getPrivateIp(), ifaceSubnet.getPrefixLength());
      ifaceAddressesBuilder.add(address);
    }
    Set<ConcreteInterfaceAddress> ifaceAddresses = ifaceAddressesBuilder.build();
    if (ifaceAddresses.isEmpty()) {
      warnings.redFlagf("No valid address found for interface '%s'", netInterface.getId());
    }
    @Nullable
    ConcreteInterfaceAddress primaryAddress =
        ifaceAddresses.stream()
            .filter(addr -> addr.getIp().equals(netInterface.getPrimaryPrivateIp().getPrivateIp()))
            .findFirst()
            .orElseGet(
                () -> {
                  warnings.redFlagf(
                      "Primary address not found for interface '%s'. Using lowest address as"
                          + " primary",
                      netInterface.getId());
                  return ifaceAddresses.stream().min(naturalOrder()).orElse(null);
                });

    Interface iface =
        Utils.newInterface(
            netInterface.getId(), cfgNode, primaryAddress, netInterface.getDescription());
    iface.setAllAddresses(ifaceAddresses);

    Utils.addLayer1Edge(
        awsConfiguration,
        cfgNode.getHostname(),
        iface.getName(),
        Subnet.nodeName(subnet.getId()),
        Subnet.instancesInterfaceName(subnet.getId()));

    return iface;
  }

  /**
   * Creates an interface, BGP process, and BGP peer to connect to the backbone. {@code
   * exportPolicyName} is configured on the peer. This policy must be installed on the node
   * separately.
   */
  static void createBackboneConnection(Configuration cfgNode, Vrf vrf, String exportPolicyName) {
    Interface toBackbone =
        Utils.newInterface(
            BACKBONE_FACING_INTERFACE_NAME,
            cfgNode,
            vrf.getName(),
            LinkLocalAddress.of(LINK_LOCAL_IP),
            "To AWS backbone");
    toBackbone.updateInterfaceType(InterfaceType.PHYSICAL);
    BgpProcess bgpProcess = makeBgpProcess(LINK_LOCAL_IP, vrf);
    BgpUnnumberedPeerConfig.builder()
        .setPeerInterface(BACKBONE_FACING_INTERFACE_NAME)
        .setRemoteAs(AWS_BACKBONE_ASN)
        .setLocalIp(LINK_LOCAL_IP)
        .setLocalAs(BACKBONE_PEERING_ASN)
        .setBgpProcess(bgpProcess)
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder().setExportPolicy(exportPolicyName).build())
        .build();
  }

  /** Extracts the text content of the first element with {@code tag} within {@code element}. */
  static String textOfFirstXmlElementWithTag(Element element, String tag) {
    NodeList nodes = element.getElementsByTagName(tag);
    checkArgument(nodes.getLength() > 0, "Tag '%s' not found", tag);
    return nodes.item(0).getTextContent();
  }

  /**
   * Extracts the text content of the first element with {@code innerTag} within the first element
   * with {@code outerTag} within {@code element}.
   */
  static String textOfFirstXmlElementWithInnerTag(
      Element element, String outerTag, String innerTag) {
    NodeList outerNodes = element.getElementsByTagName(outerTag);
    checkArgument(outerNodes.getLength() > 0, "OuterTag '%s' not found", outerTag);
    return textOfFirstXmlElementWithTag((Element) outerNodes.item(0), innerTag);
  }

  public static TraceElement getTraceElementForRule(@Nullable String ruleDescription) {
    if (ruleDescription == null) {
      return TraceElement.of("Matched rule with no description");
    }
    return TraceElement.of(String.format("Matched rule with description %s", ruleDescription));
  }

  public static TraceElement getTraceElementForSecurityGroup(String securityGroupName) {
    return TraceElement.of(String.format("Matched security group %s", securityGroupName));
  }

  static TraceElement traceElementForAddress(
      String direction, String vsAddressStructure, AddressType addressType) {
    return TraceElement.of(traceTextForAddress(direction, vsAddressStructure, addressType));
  }

  static String traceTextForAddress(
      String direction, String vsAddressStructure, AddressType addressType) {
    return String.format("Matched %s address %s %s", direction, addressType, vsAddressStructure);
  }

  static TraceElement traceElementForProtocol(IpProtocol protocol) {
    return TraceElement.of(String.format("Matched protocol %s", protocol));
  }

  static TraceElement traceElementForDstPorts(int low, int high) {
    if (low == high) {
      return TraceElement.of(String.format("Matched destination port %s", low));
    }
    return TraceElement.of(String.format("Matched destination ports [%s-%s]", low, high));
  }

  static TraceElement traceElementEniPrivateIp(String eniDescription) {
    return TraceElement.of(String.format("Matched private IP of %s", eniDescription));
  }

  static TraceElement traceElementForIcmpType(int type) {
    assert type != -1;
    return TraceElement.of(String.format("Matched ICMP type %s", type));
  }

  static TraceElement traceElementForIcmpCode(int code) {
    assert code != -1;
    return TraceElement.of(String.format("Matched ICMP code %s", code));
  }

  private Utils() {}
}
