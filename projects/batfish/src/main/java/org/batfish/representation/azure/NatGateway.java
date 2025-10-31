package org.batfish.representation.azure;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.util.isp.IspModelingUtils.installRoutingPolicyAdvertiseStatic;
import static org.batfish.datamodel.Interface.NULL_INTERFACE_NAME;
import static org.batfish.datamodel.IpProtocol.ICMP;
import static org.batfish.datamodel.IpProtocol.TCP;
import static org.batfish.datamodel.IpProtocol.UDP;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;

/**
 * Represents an Azure Nat Gateway <a
 * href="https://learn.microsoft.com/en-us/azure/templates/microsoft.network/natgateways?pivots=deployment-language-arm-template">Resource
 * link</a>
 *
 * <p>The nat gateway also perform vm public ip management from private ip. In case subnet doesn't
 * have a nat gateway, it creates a default one, only for managing vm public ip s.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NatGateway extends Resource implements Serializable {

  static final @Nonnull List<IpProtocol> NAT_PROTOCOLS = ImmutableList.of(TCP, UDP, ICMP);
  private final @Nonnull Properties _properties;

  @JsonCreator
  public NatGateway(
      @JsonProperty(AzureEntities.JSON_KEY_ID) @Nullable String id,
      @JsonProperty(AzureEntities.JSON_KEY_NAME) @Nullable String name,
      @JsonProperty(AzureEntities.JSON_KEY_TYPE) @Nullable String type,
      @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) @Nullable Properties properties) {
    super(name, id, type);
    checkArgument(properties != null, "properties must be provided");
    _properties = properties;
  }

  public NatGateway(
      @Nonnull String id, @Nonnull String name, @Nonnull String type, @Nonnull String subnetId) {
    super(name, id, type);
    _properties = new Properties(null, null, Set.of(new IdReference(subnetId)));
  }

  public String getNodeName() {
    return getCleanId();
  }

  public String getBackboneIfaceName() {
    return AzureConfiguration.BACKBONE_FACING_INTERFACE_NAME;
  }

  /**
   * create new {@link Transformation} and apply it onto the transformationStack in order to add a
   * new source nat rule (private ip -> public ip) with another source port.
   */
  public Transformation applySnat(Transformation transformation, Ip privateIp, Ip publicIp) {
    return Transformation.when(
            new MatchHeaderSpace(
                HeaderSpace.builder()
                    .setIpProtocols(NatGateway.NAT_PROTOCOLS)
                    .setSrcIps(privateIp.toIpSpace())
                    .build()))
        .apply(TransformationStep.assignSourceIp(publicIp, publicIp))
        .setOrElse(transformation)
        .build();
  }

  /**
   * create new {@link Transformation} and apply it onto the transformationStack in order to add a
   * new destination nat rule (public ip -> private ip).
   */
  public Transformation applyDnat(Transformation elseTransformation, Ip privateIp, Ip publicIp) {
    return Transformation.when(
            new MatchHeaderSpace(
                HeaderSpace.builder()
                    .setDstIps(publicIp.toIpSpace())
                    .setIpProtocols(NatGateway.NAT_PROTOCOLS)
                    .build()))
        .apply(TransformationStep.assignDestinationIp(privateIp))
        .setOrElse(elseTransformation)
        .build();
  }

  /**
   * Returns the {@link Configuration} node for this NatGateway.
   *
   * <p>Creates for each subnet one interface through {@link LinkLocalAddress}. Creates toInternet
   * {@link Interface} which advertise through BGP the nat gateway's public ip and each {@link
   * IPConfiguration} public ip from each {@link Subnet} connected to this nat gateway
   */
  public Configuration toConfigurationNode(
      Region region, ConvertedConfiguration convertedConfiguration) {

    Configuration cfgNode =
        Configuration.builder()
            .setHostname(getNodeName())
            .setConfigurationFormat(ConfigurationFormat.AZURE)
            .setDefaultCrossZoneAction(LineAction.PERMIT)
            .setDefaultInboundAction(LineAction.PERMIT)
            .setDomainName("azure")
            .build();

    Vrf.builder().setName(Configuration.DEFAULT_VRF_NAME).setOwner(cfgNode).build();

    Interface toInternet =
        Interface.builder()
            .setName(getBackboneIfaceName())
            .setVrf(cfgNode.getDefaultVrf())
            .setOwner(cfgNode)
            .setType(InterfaceType.PHYSICAL)
            .setAddress(LinkLocalAddress.of(AzureConfiguration.LINK_LOCAL_IP))
            .build();

    BgpProcess process =
        BgpProcess.builder()
            .setRouterId(AzureConfiguration.LINK_LOCAL_IP)
            .setVrf(cfgNode.getDefaultVrf())
            .setEbgpAdminCost(20)
            .setIbgpAdminCost(200)
            .setLocalAdminCost(200)
            // arbitrary values below since does not export from BGP RIB
            .setLocalOriginationTypeTieBreaker(LocalOriginationTypeTieBreaker.NO_PREFERENCE)
            .setNetworkNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
            .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
            .build();

    BgpUnnumberedPeerConfig.builder()
        .setPeerInterface(AzureConfiguration.BACKBONE_FACING_INTERFACE_NAME)
        .setRemoteAs(AzureConfiguration.AZURE_BACKBONE_ASN)
        .setLocalIp(AzureConfiguration.LINK_LOCAL_IP)
        .setLocalAs(AzureConfiguration.AZURE_LOCAL_ASN)
        .setBgpProcess(process)
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setExportPolicy(AzureConfiguration.AZURE_SERVICES_GATEWAY_EXPORT_POLICY_NAME)
                .build())
        .build();

    PrefixSpace ps = new PrefixSpace();
    ps.addPrefixRange(PrefixRange.ALL);
    installRoutingPolicyAdvertiseStatic(
        AzureConfiguration.AZURE_SERVICES_GATEWAY_EXPORT_POLICY_NAME, cfgNode, ps);

    // todo: handle publicIpPrefix
    // todo: handle multiple ip addresses
    for (IdReference publicIpReference : getProperties().getPublicIpAddresses()) {
      PublicIpAddress publicIpAddress =
          region.findResource(publicIpReference, PublicIpAddress.class);

      cfgNode
          .getDefaultVrf()
          .getStaticRoutes()
          .add(
              StaticRoute.builder()
                  .setNextHopInterface(NULL_INTERFACE_NAME)
                  .setNetwork(publicIpAddress.getProperties().getIpAddress().toPrefix())
                  .setAdministrativeCost(0)
                  .setMetric(0)
                  .setNonForwarding(true)
                  .build());

      // default SNAT rule since all the others will apply first before this one (VM public ip for
      // instance).
      toInternet.setOutgoingTransformation(
          Transformation.when(
                  new MatchHeaderSpace(HeaderSpace.builder().setIpProtocols(NAT_PROTOCOLS).build()))
              .apply(
                  TransformationStep.assignSourceIp(
                      publicIpAddress.getProperties().getIpAddress(),
                      publicIpAddress.getProperties().getIpAddress()),
                  TransformationStep.assignSourcePort(1024, 65525))
              .build());

      // remember outgoing SNAT traffic for the response
      toInternet.setFirewallSessionInterfaceInfo(
          new FirewallSessionInterfaceInfo(
              FirewallSessionInterfaceInfo.Action.FORWARD_OUT_IFACE,
              ImmutableList.of(toInternet.getName()),
              null,
              null));

      // handle only the first ip for now
      // todo: handle multiple ips
      break;
    }

    return cfgNode;
  }

  /**
   * Connects a subnet to this NatGateway.
   *
   * @param convertedConfiguration
   * @param subnet the subnet you wish to connect
   * @param subnetNode the subnetNode
   * @param toNatGateway the subnet interface to connect to this nat gateway
   * @return created interface on nat gateway connected with subnet.
   */
  public Interface connectToSubnet(
      ConvertedConfiguration convertedConfiguration,
      Subnet subnet,
      Configuration subnetNode,
      Interface toNatGateway) {

    Configuration natGatewayNode = convertedConfiguration.getNode(getNodeName());

    Interface toSubnet =
        Interface.builder()
            .setName(subnet.getNodeName())
            .setVrf(natGatewayNode.getDefaultVrf())
            .setOwner(natGatewayNode)
            .setAddress(LinkLocalAddress.of(AzureConfiguration.LINK_LOCAL_IP))
            .build();

    convertedConfiguration.addLayer1Edge(
        natGatewayNode.getHostname(), toSubnet.getName(),
        subnetNode.getHostname(), toNatGateway.getName());

    natGatewayNode
        .getDefaultVrf()
        .getStaticRoutes()
        .add(
            StaticRoute.builder()
                .setNetwork(subnet.getProperties().getAddressPrefix())
                .setNextHopInterface(toSubnet.getName())
                .setNextHopIp(AzureConfiguration.LINK_LOCAL_IP)
                .setMetric(0)
                .setAdministrativeCost(0)
                .setNonForwarding(false)
                .build());

    return toSubnet;
  }

  /**
   * use this NatGateway to handle a host public ip (any endpoint ip in a connected subnet)
   *
   * <p>this nat gateway has to be connected with the host's subnet through connectToSubnet method
   * below.
   *
   * @param convertedConfiguration
   * @param publicIpAddress
   * @param privateAddress
   */
  public void handleHostPublicIp(
      ConvertedConfiguration convertedConfiguration,
      PublicIpAddress publicIpAddress,
      Ip privateAddress) {
    Configuration natGatewayNode = convertedConfiguration.getNode(getNodeName());
    Interface toInternet = natGatewayNode.getAllInterfaces().get(getBackboneIfaceName());
    if (toInternet == null) {
      throw new BatfishException(
          "internal error, unable to find NatGateway toInternet interface !");
    }
    Transformation currentSNatTransformation = toInternet.getOutgoingTransformation();
    Transformation currentDNatTransformation = toInternet.getIncomingTransformation();

    toInternet.setOutgoingTransformation(
        applySnat(
            currentSNatTransformation,
            privateAddress,
            publicIpAddress.getProperties().getIpAddress()));

    toInternet.setIncomingTransformation(
        applyDnat(
            currentDNatTransformation,
            privateAddress,
            publicIpAddress.getProperties().getIpAddress()));

    // the nat gateway needs a static route to advertise the public ip
    natGatewayNode
        .getDefaultVrf()
        .getStaticRoutes()
        .add(
            StaticRoute.builder()
                .setNetwork(publicIpAddress.getProperties().getIpAddress().toPrefix())
                .setNextHopInterface(NULL_INTERFACE_NAME)
                .setAdministrativeCost(0)
                .setMetric(0)
                .setNonForwarding(true)
                .build());
  }

  public Properties getProperties() {
    return _properties;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Properties implements Serializable {

    private final @Nonnull Set<IdReference> _publicIpAddresses;
    private final @Nonnull Set<IdReference> _publicIpPrefixes;
    private final @Nonnull Set<IdReference> _subnets;

    @JsonCreator
    public Properties(
        @JsonProperty(AzureEntities.JSON_KEY_NAT_GATEWAY_PUBLIC_IP_ADDRESSES) @Nullable
            Set<IdReference> publicIpAddresses,
        @JsonProperty(AzureEntities.JSON_KEY_NAT_GATEWAY_PUBLIC_IP_PREFIXES) @Nullable
            Set<IdReference> publicIpPrefixes,
        @JsonProperty(AzureEntities.JSON_KEY_NAT_GATEWAY_SUBNETS) @Nullable
            Set<IdReference> subnets) {
      _publicIpAddresses = Optional.ofNullable(publicIpAddresses).orElseGet(HashSet::new);
      _publicIpPrefixes = Optional.ofNullable(publicIpPrefixes).orElseGet(HashSet::new);
      _subnets = Optional.ofNullable(subnets).orElseGet(HashSet::new);
    }

    public @Nonnull Set<IdReference> getPublicIpAddresses() {
      return _publicIpAddresses;
    }

    public @Nonnull Set<IdReference> getPublicIpPrefixes() {
      return _publicIpPrefixes;
    }

    public @Nonnull Set<IdReference> getSubnets() {
      return _subnets;
    }
  }
}
