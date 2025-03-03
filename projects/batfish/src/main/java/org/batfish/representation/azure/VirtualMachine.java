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
import org.batfish.common.BatfishException;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.DeviceType;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;

/**
 * Represents an Azure Virtual Machine. <a
 * href="https://learn.microsoft.com/en-us/azure/templates/microsoft.compute/virtualmachines?pivots=deployment-language-arm-template">Resource
 * link</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VirtualMachine extends Instance implements Serializable {

  private final @Nonnull Properties _properties;

  @JsonCreator
  public VirtualMachine(
      @JsonProperty(AzureEntities.JSON_KEY_ID) @Nonnull String id,
      @JsonProperty(AzureEntities.JSON_KEY_TYPE) @Nonnull String type,
      @JsonProperty(AzureEntities.JSON_KEY_NAME) @Nonnull String name,
      @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) @Nonnull Properties properties) {
    super(name, id, type);
    checkArgument(properties != null, "properties must be provided");
    _properties = properties;
  }

  /**
   * Returns the {@link Configuration} node for this Virtual Machine.
   *
   * <p>Creates {@link Interface}s attached to this VM. Connects each interfaces to its subnet node.
   * default StaticRoute to subnet node. {@link NetworkSecurityGroup} applied onto the interface if
   * any.
   */
  @Override
  public Configuration toConfigurationNode(
      Region rgp, ConvertedConfiguration convertedConfiguration) {
    Configuration cfgNode =
        Configuration.builder()
            .setHostname(getCleanId())
            .setHumanName(getName())
            .setDomainName("azure")
            .setDeviceModel(DeviceModel.AZURE_VM)
            .setDefaultInboundAction(LineAction.PERMIT)
            .setDefaultCrossZoneAction(LineAction.PERMIT)
            .setConfigurationFormat(ConfigurationFormat.AZURE)
            .build();

    cfgNode.setDeviceType(DeviceType.HOST);

    Vrf.builder().setName(Configuration.DEFAULT_VRF_NAME).setOwner(cfgNode).build();

    for (IdReference idReference : _properties.getNetworkProfile().getNetworkInterfaces()) {
      NetworkInterface networkInterface = rgp.getInterfaces().get(idReference.getId());

      Interface.Builder interfaceBuilder = Interface.builder();
      Subnet subnet = null;
      List<ConcreteInterfaceAddress> secondaryInterfacesAddresses =
          new ArrayList<>(networkInterface.getProperties().getIPConfigurations().size() - 1);

      for (IPConfiguration ipConfiguration :
          networkInterface.getProperties().getIPConfigurations()) {
        subnet = rgp.getSubnets().get(ipConfiguration.getProperties().getSubnetId());

        ConcreteInterfaceAddress concreteInterfaceAddress =
            ConcreteInterfaceAddress.create(
                ipConfiguration.getProperties().getPrivateIpAddress(),
                subnet.getProperties().getAddressPrefix().getPrefixLength());

        if (ipConfiguration.getProperties().isPrimary()) {
          interfaceBuilder.setAddress(concreteInterfaceAddress);
        } else {
          secondaryInterfacesAddresses.add(concreteInterfaceAddress);
        }
      }

      if (!secondaryInterfacesAddresses.isEmpty()) {
        interfaceBuilder.setSecondaryAddresses(secondaryInterfacesAddresses);
      }

      // security check for the trick above
      if (subnet == null) {
        continue;
      }

      // assign itself to this device through setOwner
      Interface currentInterface =
          interfaceBuilder
              .setName(networkInterface.getCleanId())
              .setHumanName(networkInterface.getName())
              .setOwner(cfgNode)
              .setVrf(cfgNode.getDefaultVrf())
              .build();

      // default route to the subnet iface
      StaticRoute st =
          StaticRoute.builder()
              .setNextHopIp(subnet.computeInstancesIfaceIp())
              .setAdministrativeCost(0)
              .setMetric(0)
              .setNetwork(Prefix.ZERO)
              .build();

      cfgNode.getDefaultVrf().getStaticRoutes().add(st);

      // draw edge toward the subnet node
      convertedConfiguration.addLayer1Edge(
          cfgNode.getHostname(), networkInterface.getCleanId(),
          subnet.getNodeName(), subnet.getToLanInterfaceName());

      // ACL
      {
        String nsgId = networkInterface.getProperties().getNetworkSecurityGroupID();
        if (nsgId != null) {
          NetworkSecurityGroup nsg = rgp.getNetworkSecurityGroups().get(nsgId);

          if (nsg == null) {
            throw new BatfishException(
                String.format(
                    "Unable to apply the NSG %s on subnet %s.\n" + "Missing nsg file !",
                    getCleanId(), nsgId));
          }

          nsg.applyToInterface(currentInterface);
        }
      }
    }

    return cfgNode;
  }

  public Properties getProperties() {
    return _properties;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Properties implements Serializable {

    private final @Nonnull NetworkProfile _networkProfile;

    @JsonCreator
    public Properties(
        @JsonProperty(AzureEntities.JSON_KEY_NETWORK_PROFILE) @Nonnull
            NetworkProfile networkProfile) {
      checkArgument(networkProfile != null, "networkProfile must be provided");
      _networkProfile = networkProfile;
    }

    public @Nonnull NetworkProfile getNetworkProfile() {
      return _networkProfile;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class NetworkProfile implements Serializable {

    private final @Nonnull Set<IdReference> _networkInterfaces;

    @JsonCreator
    public NetworkProfile(
        @JsonProperty(AzureEntities.JSON_KEY_NETWORK_INTERFACE_ID) @Nullable
            Set<IdReference> networkInterfaces) {
      _networkInterfaces = Optional.ofNullable(networkInterfaces).orElseGet(HashSet::new);
    }

    public @Nonnull Set<IdReference> getNetworkInterfaces() {
      return _networkInterfaces;
    }
  }
}
