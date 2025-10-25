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
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker;
import org.batfish.datamodel.route.nh.NextHop;
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
   * Returns the {@link Configuration} node for this VNet.
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

    Transformation snatTransformation = null;
    Transformation dnatTransformation = null;

    { // Internet
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

      if (getProperties() == null) {
        installRoutingPolicyAdvertiseStatic(
            AzureConfiguration.AZURE_SERVICES_GATEWAY_EXPORT_POLICY_NAME, cfgNode, ps);
        return cfgNode;
      }

      Ip startIp = null;
      Ip endIp = null;

      for (IdReference id : getProperties().getPublicIpAddresses()) {
        if (id == null) continue;
        PublicIpAddress publicIpAddress = region.getPublicIpAddresses().get(id.getId());
        if (publicIpAddress == null) {
          throw new BatfishException(
              "PublicIpAddress not found (did you include it ?). id: " + id.getId());
        }
        startIp = publicIpAddress.getProperties().getIpAddress();
        endIp = startIp;
      }

      // todo : handle multiple public ips not in the same range
      // todo : handle prefix
      if (startIp != null) {

        snatTransformation =
            Transformation.when(
                    new MatchHeaderSpace(
                        HeaderSpace.builder().setIpProtocols(NAT_PROTOCOLS).build()))
                .apply(
                    TransformationStep.assignSourceIp(startIp, endIp),
                    TransformationStep.assignSourcePort(1024, 65525))
                .build();

        // need to create a null route so we can advertise the prefix before sending traffic to the
        // right host
        StaticRoute st =
            StaticRoute.builder()
                .setNextHopInterface(NULL_INTERFACE_NAME)
                .setNetwork(startIp.toPrefix())
                .setAdministrativeCost(0)
                .setMetric(0)
                .setNonForwarding(true)
                .build();

        cfgNode.getDefaultVrf().getStaticRoutes().add(st);

        ps.addPrefix(startIp.toPrefix());
      }

      if (getProperties().getSubnets() == null) {
        installRoutingPolicyAdvertiseStatic(
            AzureConfiguration.AZURE_SERVICES_GATEWAY_EXPORT_POLICY_NAME, cfgNode, ps);
        return cfgNode;
      }

      for (IdReference subnetReference : getProperties().getSubnets()) {

        Subnet subnet = region.getSubnets().get(subnetReference.getId());

        if (subnet == null) {
          throw new BatfishException(
              "Subnet not found (did you include it ?). id : " + subnetReference.getId());
        }

        Transformation subnetSnatTransformation = snatTransformation;

        for (IdReference ipConfigurationReference : subnet.getProperties().getIpConfigurations()) {
          IPConfiguration ipConfiguration =
              region.getIpConfigurations().get(ipConfigurationReference.getId().toLowerCase());

          if (ipConfiguration == null) {
            throw new BatfishException(
                "referenced ipConfiguration not found (did you include it ?). id : "
                    + ipConfigurationReference.getId());
          }

          String publicIpAddressId = ipConfiguration.getProperties().getPublicIpAddressId();
          if (publicIpAddressId == null) continue;

          PublicIpAddress publicIpAddress = region.getPublicIpAddresses().get(publicIpAddressId);
          if (publicIpAddress == null) {
            throw new BatfishException(
                "Referenced Public Ip not found (did you include it ?). id : " + publicIpAddressId);
          }

          subnetSnatTransformation =
              applySnat(
                  subnetSnatTransformation,
                  ipConfiguration.getProperties().getPrivateIpAddress(),
                  publicIpAddress.getProperties().getIpAddress());

          dnatTransformation =
              applyDnat(
                  dnatTransformation,
                  ipConfiguration.getProperties().getPrivateIpAddress(),
                  publicIpAddress.getProperties().getIpAddress());

          StaticRoute st =
              StaticRoute.builder()
                  .setNetwork(publicIpAddress.getProperties().getIpAddress().toPrefix())
                  .setNextHopInterface(NULL_INTERFACE_NAME)
                  .setAdministrativeCost(0)
                  .setMetric(0)
                  .build();

          cfgNode.getDefaultVrf().getStaticRoutes().add(st);

          ps.addPrefix(publicIpAddress.getProperties().getIpAddress().toPrefix());
        }

        Interface toSubnet =
            Interface.builder()
                .setName(subnet.getNodeName())
                .setVrf(cfgNode.getDefaultVrf())
                .setOwner(cfgNode)
                .setAddress(LinkLocalAddress.of(AzureConfiguration.LINK_LOCAL_IP))
                .build();

        StaticRoute st =
            StaticRoute.builder()
                .setNetwork(subnet.getProperties().getAddressPrefix())
                .setNextHop(
                    NextHop.legacyConverter(toSubnet.getName(), AzureConfiguration.LINK_LOCAL_IP))
                .setMetric(0)
                .setAdministrativeCost(0)
                .setNonForwarding(false)
                .build();

        cfgNode.getDefaultVrf().getStaticRoutes().add(st);

        convertedConfiguration.addLayer1Edge(
            cfgNode.getHostname(), subnet.getNodeName(),
            subnet.getNodeName(), subnet.getToNatInterfaceName());

        toSubnet.setIncomingTransformation(subnetSnatTransformation);
      }

      toInternet.setIncomingTransformation(dnatTransformation);

      installRoutingPolicyAdvertiseStatic(
          AzureConfiguration.AZURE_SERVICES_GATEWAY_EXPORT_POLICY_NAME, cfgNode, ps);
    }

    return cfgNode;
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
      if (publicIpAddresses == null) publicIpAddresses = new HashSet<>();
      if (publicIpPrefixes == null) publicIpPrefixes = new HashSet<>();
      if (subnets == null) subnets = new HashSet<>();
      _publicIpAddresses = publicIpAddresses;
      _publicIpPrefixes = publicIpPrefixes;
      _subnets = subnets;
    }

    public Set<IdReference> getPublicIpAddresses() {
      return _publicIpAddresses;
    }

    public Set<IdReference> getPublicIpPrefixes() {
      return _publicIpPrefixes;
    }

    public Set<IdReference> getSubnets() {
      return _subnets;
    }
  }
}
