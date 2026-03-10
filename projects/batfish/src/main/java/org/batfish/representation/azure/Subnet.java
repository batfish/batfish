package org.batfish.representation.azure;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.route.nh.NextHop;

/**
 * Represents an Azure subnet (part of VNet object). <a
 * href="https://learn.microsoft.com/en-us/azure/templates/microsoft.network/virtualnetworks/subnets?pivots=deployment-language-arm-template">Resource
 * link</a> Do not support UDR.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Subnet extends Resource implements Serializable {

  private final @Nonnull Properties _properties;

  @JsonCreator
  public Subnet(
      @JsonProperty(AzureEntities.JSON_KEY_ID) @Nonnull String id,
      @JsonProperty(AzureEntities.JSON_KEY_NAME) @Nonnull String name,
      @JsonProperty(AzureEntities.JSON_KEY_TYPE) @Nonnull String type,
      @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) @Nonnull Properties properties) {
    super(name, id, type);
    checkArgument(properties != null, "properties must be provided");
    _properties = properties;
  }

  public Ip computeInstancesIfaceIp() {
    // first 3 ips are reserved in an azure subnet
    return _properties.getAddressPrefix().getFirstHostIp();
  }

  public @Nonnull String getNodeName() {
    return getCleanId();
  }

  public @Nonnull String getToLanInterfaceName(Interface facingInterface) {
    return "to-" + facingInterface.getName();
  }

  public @Nonnull String getToLanInterfaceName() {
    return "to-subnet";
  }

  public @Nonnull String getToVnetInterfaceName() {
    return "to-vnet";
  }

  public @Nonnull String getToNatInterfaceName() {
    return "nat-gateway";
  }

  /**
   * Returns the {@link Configuration} node for this subnet.
   *
   * <p>Creates Interfaces (with layer1Edges) to connect to {@link VNet}, {@link NatGateway} and
   * {@link Instance} objects. add StaticRoutes for VNet and Internet access. (If no natGateways
   * exists, creates one for handling VM's public ips).
   */
  public Configuration toConfigurationNode(
      Region rgp, ConvertedConfiguration convertedConfiguration) {

    Configuration cfgNode =
        Configuration.builder()
            .setHumanName(getName())
            .setHostname(getNodeName())
            .setDomainName("azure")
            .setConfigurationFormat(ConfigurationFormat.AZURE)
            .setDefaultCrossZoneAction(LineAction.PERMIT)
            .setDefaultInboundAction(LineAction.PERMIT)
            .setDeviceModel(DeviceModel.AZURE_SUBNET)
            .build();

    Vrf.builder().setName(Configuration.DEFAULT_VRF_NAME).setOwner(cfgNode).build();

    { // VNet
      Interface.builder()
          .setAddress(LinkLocalAddress.of(AzureConfiguration.LINK_LOCAL_IP))
          .setVrf(cfgNode.getDefaultVrf())
          .setOwner(cfgNode)
          .setName(getToVnetInterfaceName())
          .setDescription("to vnet interface")
          .setType(InterfaceType.PHYSICAL)
          .build();

      // static route set from Vnet object (no knowledge of Vnet addressSpace nor Vnet peering s)
    }

    { // Nat gateway

      // if subnet doesn't have a nat gateway.
      // generates one for handling vm's public ips.
      if (getProperties().getNatGatewayId() == null) {
        String natGatewayId = getId() + "/internet-gateway";

        NatGateway natGateway =
            new NatGateway(
                natGatewayId,
                getName() + "-internet-gateway",
                AzureEntities.JSON_TYPE_NAT_GATEWAY,
                getId());

        rgp.getNatGateways().put(natGatewayId, natGateway);

        getProperties().setNatGatewayId(natGatewayId);
        convertedConfiguration.addNode(natGateway.toConfigurationNode(rgp, convertedConfiguration));
      }

      Interface toNat =
          Interface.builder()
              .setVrf(cfgNode.getDefaultVrf())
              .setOwner(cfgNode)
              .setName(getToNatInterfaceName())
              .setAddress(LinkLocalAddress.of(AzureConfiguration.LINK_LOCAL_IP))
              .setDescription("to nat gateway")
              .setType(InterfaceType.PHYSICAL)
              .build();

      NatGateway natGateway = rgp.findResource(getProperties().getNatGatewayId(), NatGateway.class);
      natGateway.connectToSubnet(convertedConfiguration, this, cfgNode, toNat);

      cfgNode
          .getDefaultVrf()
          .getStaticRoutes()
          .add(
              StaticRoute.builder()
                  .setNetwork(Prefix.ZERO)
                  .setNonForwarding(false)
                  .setNextHop(
                      NextHop.legacyConverter(toNat.getName(), AzureConfiguration.LINK_LOCAL_IP))
                  .setAdministrativeCost(0)
                  .setMetric(0)
                  .build());
    }

    return cfgNode;
  }

  /**
   * Connects this subnet to a VNet
   *
   * <p>Creates an Interface onto this subnet node ({@link Configuration} * and connects it to the
   * given node on the specified interface.
   *
   * @param convertedConfiguration convertedConfiguration
   * @param virtualNetworkNode the VNet node
   * @param virtualNetwork the VNet you wish to connect to this subnet
   * @param toSubnet VNet interface to use for the connection
   * @return the created subnet interface connected to Vnet
   */
  public Interface connectToVnet(
      ConvertedConfiguration convertedConfiguration,
      Configuration virtualNetworkNode,
      VNet virtualNetwork,
      Interface toSubnet) {
    Configuration subnetNode = convertedConfiguration.getNode(getNodeName());

    Interface toVnet =
        Interface.builder()
            .setAddress(LinkLocalAddress.of(AzureConfiguration.LINK_LOCAL_IP))
            .setVrf(subnetNode.getDefaultVrf())
            .setOwner(subnetNode)
            .setName(getToVnetInterfaceName())
            .setDescription("to vnet interface")
            .setType(InterfaceType.PHYSICAL)
            .build();

    convertedConfiguration.addLayer1Edge(
        subnetNode.getHostname(), toVnet.getName(),
        virtualNetworkNode.getHostname(), toSubnet.getName());

    for (Prefix vnetPrefix :
        virtualNetwork.getProperties().getAddressSpace().getAddressPrefixes()) {
      subnetNode
          .getDefaultVrf()
          .getStaticRoutes()
          .add(
              StaticRoute.builder()
                  .setNetwork(vnetPrefix)
                  .setNextHopInterface(toVnet.getName())
                  .setNextHopIp(AzureConfiguration.LINK_LOCAL_IP)
                  .setAdministrativeCost(0)
                  .setMetric(0)
                  .build());
    }

    return toVnet;
  }

  /**
   * Connect a host to this subnet.
   *
   * <p>Creates an Interface onto this subnet node ({@link Configuration} and connects it to the
   * given node on the specified interface.
   */
  public Interface connectToHost(
      Region rgp,
      ConvertedConfiguration convertedConfiguration,
      @Nonnull Configuration hostNode,
      @Nonnull Interface hostInterface) {
    checkArgument(hostNode != null, "hostNode must be provided !");
    checkArgument(hostInterface != null, "hostInterface must be provided !");

    // retrieve this subnet configuration (this subnet's Configuration should be created already)
    Configuration subnetNode = convertedConfiguration.getNode(this.getNodeName());

    // every host should be on a different bridge (because every network stream should be filtered
    // by NSGs)
    // therefore on a different interface, but we enable ProxyArp so they can still communicate
    // even if on a different bridge.
    Interface subnetInterface =
        Interface.builder()
            .setAddress(
                ConcreteInterfaceAddress.create(
                    computeInstancesIfaceIp(), _properties.getAddressPrefix().getPrefixLength()))
            .setName(getToLanInterfaceName(hostInterface))
            .setOwner(subnetNode)
            .setVrf(subnetNode.getDefaultVrf())
            .setProxyArp(true)
            .setType(InterfaceType.PHYSICAL)
            .build();

    convertedConfiguration.addLayer1Edge(
        subnetNode.getHostname(), subnetInterface.getName(),
        hostNode.getHostname(), hostInterface.getName());

    // each host is connected on a different subnet interface (and bridge)
    // so we must indicate set a route to find the right host
    for (ConcreteInterfaceAddress hostAddress : hostInterface.getAllConcreteAddresses()) {

      subnetNode
          .getDefaultVrf()
          .getStaticRoutes()
          .add(
              StaticRoute.builder()
                  .setNetwork(Prefix.create(hostAddress.getIp(), 32))
                  .setNextHopInterface(subnetInterface.getName())
                  .setAdministrativeCost(0)
                  .setMetric(0)
                  .build());
    }

    // if the subnet has an NSG associated, the subnetInterface must filter it according
    // to the NSG
    if (this.getProperties().getNetworkSecurityGroupId() != null) {
      NetworkSecurityGroup nsg =
          rgp.findResource(getProperties().getNetworkSecurityGroupId(), NetworkSecurityGroup.class);
      nsg.applyToInterface(subnetInterface);
    }

    return subnetInterface;
  }

  public @Nonnull Properties getProperties() {
    return _properties;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Properties implements Serializable {

    private final @Nonnull Prefix _addressPrefix;
    private final @Nullable IdReference _nsg;
    private String _natGatewayId;
    private final @Nonnull Set<IdReference> _ipConfigurations;
    private final boolean _defaultOutboundAccess;

    @JsonCreator
    public Properties(
        @JsonProperty(AzureEntities.JSON_KEY_SUBNET_ADDRESS_PREFIX) @Nonnull Prefix addressPrefix,
        @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_NGS) @Nullable IdReference nsg,
        @JsonProperty(AzureEntities.JSON_KEY_SUBNET_NAT_GATEWAY) @Nullable String natGatewayId,
        @JsonProperty(AzureEntities.JSON_KEY_SUBNET_IP_CONFIGURATIONS) @Nullable
            Set<IdReference> ipConfigurations,
        @JsonProperty("defaultOutboundAccess") @Nullable Boolean defaultOutboundAccess) {
      checkArgument(addressPrefix != null, "addressPrefix must be provided");

      _addressPrefix = addressPrefix;
      _nsg = nsg;
      _natGatewayId = natGatewayId;
      _ipConfigurations = Optional.ofNullable(ipConfigurations).orElseGet(HashSet::new);
      _defaultOutboundAccess = Optional.ofNullable(defaultOutboundAccess).orElse(false);
    }

    public @Nonnull Prefix getAddressPrefix() {
      return _addressPrefix;
    }

    public @Nullable String getNetworkSecurityGroupId() {
      return Optional.ofNullable(_nsg).map(IdReference::getId).orElse(null);
    }

    public @Nullable String getNatGatewayId() {
      return _natGatewayId;
    }

    public void setNatGatewayId(@Nullable String natGatewayId) {
      _natGatewayId = natGatewayId;
    }

    public @Nonnull Set<IdReference> getIpConfigurations() {
      return _ipConfigurations;
    }

    // todo: handle vm default outbound access if this parameter is true
    // idea : insert a publicIp in every vm ipConfiguration (before node generation and after
    // parsing)
    public boolean getDefaultOutboundAccess() {
      return _defaultOutboundAccess;
    }
  }
}
