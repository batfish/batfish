package org.batfish.representation.azure;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.route.nh.NextHop;

/**
 * Represents an Azure VNet. <a
 * href="https://learn.microsoft.com/en-us/azure/templates/microsoft.network/virtualnetworks?pivots=deployment-language-arm-template">Resource
 * link</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VNet extends Resource {

  private final @Nonnull Properties _properties;

  @JsonCreator
  public VNet(
      @JsonProperty(AzureEntities.JSON_KEY_ID) @Nonnull String id,
      @JsonProperty(AzureEntities.JSON_KEY_NAME) @Nonnull String name,
      @JsonProperty(AzureEntities.JSON_KEY_TYPE) @Nonnull String type,
      @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) @Nonnull Properties properties) {
    super(name, id, type);
    checkArgument(properties != null, "properties must be provided");
    _properties = properties;
  }

  /**
   * Returns the {@link Configuration} node for this VNet.
   *
   * <p>Creates each subnet {@link Interface} and connect it this node through {@link
   * LinkLocalAddress}. Configure Static routes toward each subnet.
   */
  public Configuration toConfigurationNode(
      Region region, ConvertedConfiguration convertedConfiguration) {
    Configuration cfgNode =
        Configuration.builder()
            .setHostname(getCleanId())
            .setDeviceModel(DeviceModel.AZURE_VNET)
            .setDomainName("azure")
            .setConfigurationFormat(ConfigurationFormat.AZURE)
            .setDefaultCrossZoneAction(LineAction.PERMIT)
            .setDefaultInboundAction(LineAction.PERMIT)
            .setHumanName(getName())
            .build();

    Vrf.builder().setName(Configuration.DEFAULT_VRF_NAME).setOwner(cfgNode).build();

    for (Subnet subnet : _properties.getSubnets()) {

      Configuration subnetNode = convertedConfiguration.getNode(subnet.getNodeName());

      Interface.builder()
          .setName(subnet.getNodeName())
          .setAddress(LinkLocalAddress.of(AzureConfiguration.LINK_LOCAL_IP))
          .setVrf(cfgNode.getDefaultVrf())
          .setOwner(cfgNode)
          .build();

      convertedConfiguration.addLayer1Edge(
          cfgNode.getHostname(), subnet.getNodeName(),
          subnetNode.getHostname(), subnet.getToVnetInterfaceName());

      convertedConfiguration.addLayer1Edge(
          subnetNode.getHostname(), subnet.getToVnetInterfaceName(),
          cfgNode.getHostname(), subnet.getNodeName());

      // Add routes from subnet to VNet node (Address Space prefix)
      for (Prefix prefix : getProperties().getAddressSpace().getAddressPrefixes()) {
        subnetNode
            .getDefaultVrf()
            .getStaticRoutes()
            .add(
                StaticRoute.builder()
                    .setNetwork(prefix)
                    .setNextHop(
                        NextHop.legacyConverter(
                            subnet.getToVnetInterfaceName(), AzureConfiguration.LINK_LOCAL_IP))
                    .setAdministrativeCost(0)
                    .setMetric(0)
                    .setNonForwarding(false)
                    .build());
      }

      // Add route from VNet node to subnet node
      StaticRoute st =
          StaticRoute.builder()
              .setAdministrativeCost(1)
              .setNextHop(NextHop.legacyConverter(subnet.getNodeName(), Ip.parse("169.254.0.1")))
              .setNetwork(subnet.getProperties().getAddressPrefix())
              .setNonForwarding(false)
              .setMetric(0)
              .build();

      cfgNode.getDefaultVrf().getStaticRoutes().add(st);
    }

    return cfgNode;
  }

  public @Nonnull Properties getProperties() {
    return _properties;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Properties implements Serializable {

    private final @Nonnull AddressSpace _addressSpace;
    private final @Nonnull Set<Subnet> _subnets;

    @JsonCreator
    public Properties(
        @JsonProperty(AzureEntities.JSON_KEY_VNET_ADDRESS_SPACE) @Nonnull AddressSpace addressSpace,
        @JsonProperty(AzureEntities.JSON_KEY_VNET_SUBNETS) @Nullable Set<Subnet> subnets) {
      checkArgument(addressSpace != null, "addressSpace must be provided");

      _addressSpace = addressSpace;
      _subnets = Optional.ofNullable(subnets).orElseGet(HashSet::new);
    }

    public Set<Subnet> getSubnets() {
      return _subnets;
    }

    public AddressSpace getAddressSpace() {
      return _addressSpace;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AddressSpace implements Serializable {

    private final @Nonnull List<Prefix> _addressPrefixes;

    @JsonCreator
    public AddressSpace(
        @JsonProperty(AzureEntities.JSON_KEY_VNET_ADDRESS_PREFIX) @Nullable
            List<Prefix> addressPrefixes) {
      _addressPrefixes = Optional.ofNullable(addressPrefixes).orElseGet(ArrayList::new);
    }

    public @Nonnull List<Prefix> getAddressPrefixes() {
      return _addressPrefixes;
    }
  }
}
