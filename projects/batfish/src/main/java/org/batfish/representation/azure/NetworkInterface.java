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
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Vrf;

/**
 * Represents an Azure Network Interface which is used for {@link VirtualMachine} objects. <a
 * href="https://learn.microsoft.com/en-us/azure/templates/microsoft.network/networkinterfaces?pivots=deployment-language-arm-template">Resource
 * Link</a>.
 *
 * <p>Partially implemented:
 * <li>ipv6 ipConfigurations not implemented
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkInterface extends Resource implements Serializable {

  private final @Nonnull Properties _properties;

  @JsonCreator
  public NetworkInterface(
      @JsonProperty(AzureEntities.JSON_KEY_NAME) @Nonnull String name,
      @JsonProperty(AzureEntities.JSON_KEY_ID) @Nonnull String id,
      @JsonProperty(AzureEntities.JSON_KEY_TYPE) @Nonnull String type,
      @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) @Nonnull Properties properties) {
    super(name, id, type);
    checkArgument(properties != null, "properties must be provided");
    _properties = properties;
  }

  public @Nullable String getSubnetId() {
    for (IPConfiguration ipConfiguration : getProperties().getIPConfigurations()) {
      return ipConfiguration.getProperties().getSubnetId();
    }
    return null;
  }

  /**
   * Generates an {@link Interface} from its parsed configuration.
   *
   * <p>requires the default {@link Vrf} to be created on the owner.
   *
   * @param rgp: region
   * @param owner: the owner of the Interface
   * @return {@link Interface}
   */
  public Interface getInterface(Region rgp, Configuration owner) {
    Interface.Builder interfaceBuilder = Interface.builder();
    interfaceBuilder.setName(getCleanId());
    interfaceBuilder.setHumanName(getName());
    interfaceBuilder.setOwner(owner);

    ConcreteInterfaceAddress primaryAddress = null;
    List<ConcreteInterfaceAddress> secondaryInterfacesAddresses =
        new ArrayList<>(getProperties().getIPConfigurations().size() - 1);

    Subnet subnet = getSubnetId() == null ? null : rgp.getSubnets().get(getSubnetId());
    int mask = subnet == null ? 32 : subnet.getProperties().getAddressPrefix().getPrefixLength();

    for (IPConfiguration ipConfiguration : getProperties().getIPConfigurations()) {
      ConcreteInterfaceAddress currentAddress =
          ConcreteInterfaceAddress.create(
              ipConfiguration.getProperties().getPrivateIpAddress(), mask);

      if (ipConfiguration.getProperties().isPrimary()) {
        primaryAddress = currentAddress;
      } else {
        secondaryInterfacesAddresses.add(currentAddress);
      }
    }

    interfaceBuilder.setAddress(primaryAddress);
    interfaceBuilder.setSecondaryAddresses(secondaryInterfacesAddresses);
    interfaceBuilder.setVrf(owner.getDefaultVrf());

    return interfaceBuilder.build();
  }

  public void advertisePublicIpIfAny(
      Region region, ConvertedConfiguration convertedConfiguration, NatGateway natGateway) {
    for (IPConfiguration ipConfiguration : getProperties().getIPConfigurations()) {
      ipConfiguration.advertisePublicIpIfAny(region, convertedConfiguration, natGateway);
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Properties implements Serializable {

    private final @Nonnull Set<IPConfiguration> _ipConfigurations;
    private final @Nullable String _macAddress;
    private final @Nullable IdReference _nsg;

    @JsonCreator
    public Properties(
        @JsonProperty(AzureEntities.JSON_KEY_VNET_IP_CONFIGURATIONS) @Nullable
            Set<IPConfiguration> ipConfigurations,
        @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_MAC_ADDRESS) @Nullable String macAddress,
        @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_NGS) @Nullable IdReference nsg) {
      _ipConfigurations = Optional.ofNullable(ipConfigurations).orElseGet(HashSet::new);
      _macAddress = macAddress;
      _nsg = nsg;
    }

    public @Nonnull Set<IPConfiguration> getIPConfigurations() {
      return _ipConfigurations;
    }

    // useful for ipv6 ? (compute local link ipv6 from mac address)
    public @Nullable String getMacAddress() {
      return _macAddress;
    }

    public @Nullable String getNetworkSecurityGroupID() {
      if (_nsg == null) {
        return null;
      }
      return _nsg.getId();
    }
  }

  public @Nonnull Properties getProperties() {
    return _properties;
  }
}
