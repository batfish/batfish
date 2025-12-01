package org.batfish.representation.azure;

import static com.google.common.base.Preconditions.checkArgument;

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
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;

/**
 * Represents ContainerInstance azure services. (Part of {@link ContainerGroup}). <a
 * href="https://learn.microsoft.com/en-us/azure/templates/microsoft.containerinstance/containergroups?pivots=deployment-language-arm-template">Resource
 * link</a>.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContainerInstance implements Serializable {

  private final @Nonnull String _name;
  private final @Nonnull Properties _properties;

  public ContainerInstance(
      @JsonProperty(AzureEntities.JSON_KEY_NAME) @Nonnull String name,
      @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) @Nonnull Properties properties) {
    checkArgument(name != null, "resource name must be provided");
    checkArgument(properties != null, "properties must be provided");
    _name = name;
    _properties = properties;
  }

  public String getName() {
    return _name;
  }

  public Properties getProperties() {
    return _properties;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Properties implements Serializable {

    private final @Nonnull Set<Port> _ports;

    public Properties(
        @JsonProperty(AzureEntities.JSON_KEY_CONTAINER_INSTANCE_PORTS) @Nullable Set<Port> ports) {
      _ports = Optional.ofNullable(ports).orElse(new HashSet<>());
    }

    public Set<Port> getPorts() {
      return _ports;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Port implements Serializable {
    private final @Nonnull IpProtocol _protocol;
    private final @Nonnull int _port;

    public Port(
        @JsonProperty(AzureEntities.JSON_KEY_CONTAINER_INSTANCE_PORT_PROTOCOL) @Nonnull
            IpProtocol protocol,
        @JsonProperty(AzureEntities.JSON_KEY_CONTAINER_INSTANCE_PORT_NUMBER) @Nonnull
            Integer port) {
      checkArgument(protocol != null, "protocol must be provided");
      checkArgument(port != null, "port must be provided");
      _protocol = protocol;
      _port = port;
    }

    public @Nonnull IpProtocol getProtocol() {
      return _protocol;
    }

    public int getPort() {
      return _port;
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof Port other) {
        return (other.getPort() == getPort() && other.getProtocol() == getProtocol());
      }
      return false;
    }

    @Override
    public int hashCode() {
      return _protocol.hashCode() + _port;
    }
  }

  public String getInterfaceName() {
    return "to-container-group";
  }

  public Configuration toConfigurationNode(
      Region region, Configuration containerGroup, Ip containerIp, Ip containerGroupIp) {

    Configuration containerInstanceNode =
        Configuration.builder()
            .setHostname(containerGroup.getHostname() + "_" + getName())
            .setHumanName(getName())
            .setDomainName("azure")
            .setDeviceModel(DeviceModel.AZURE_CONTAINER_INSTANCE)
            .setDefaultInboundAction(LineAction.PERMIT)
            .setDefaultCrossZoneAction(LineAction.PERMIT)
            .setConfigurationFormat(ConfigurationFormat.AZURE)
            .build();

    Vrf.builder().setOwner(containerInstanceNode).setName(Configuration.DEFAULT_VRF_NAME).build();

    Interface.builder()
        .setAddress(ConcreteInterfaceAddress.create(containerIp, 16))
        .setOwner(containerInstanceNode)
        .setName(getInterfaceName())
        .setVrf(containerInstanceNode.getDefaultVrf())
        .setType(InterfaceType.PHYSICAL)
        .build();

    containerInstanceNode
        .getDefaultVrf()
        .getStaticRoutes()
        .add(
            StaticRoute.builder()
                .setMetric(0)
                .setAdministrativeCost(0)
                .setNextHopIp(containerGroupIp)
                .setNetwork(Prefix.ZERO)
                .build());

    return containerInstanceNode;
  }
}
