package org.batfish.representation.azure;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;

/**
 * Represents Azure Container Group objects. <a
 * href="https://learn.microsoft.com/en-us/azure/templates/microsoft.containerinstance/containergroups?pivots=deployment-language-arm-template">Resource
 * link</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContainerGroup extends Instance implements Serializable {

  private final @Nonnull Properties _properties;

  @JsonCreator
  public ContainerGroup(
      @JsonProperty(AzureEntities.JSON_KEY_ID) @Nonnull String id,
      @JsonProperty(AzureEntities.JSON_KEY_NAME) @Nonnull String name,
      @JsonProperty(AzureEntities.JSON_KEY_TYPE) @Nonnull String type,
      @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) @Nonnull Properties properties) {
    super(name, id, type);
    checkArgument(properties != null, "properties must be provided");
    _properties = properties;
  }

  public @Nonnull Properties getProperties() {
    return _properties;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Properties implements Serializable {

    private final @Nonnull Set<ContainerInstance> _containers;
    private final @Nonnull IpAddress _ipAddress;
    private final @Nonnull Set<IdReference> _subnetIds;

    @JsonCreator
    public Properties(
        @JsonProperty(AzureEntities.JSON_KEY_CONTAINER_GROUP_CONTAINERS) @Nullable
            Set<ContainerInstance> containers,
        @JsonProperty(AzureEntities.JSON_KEY_CONTAINER_INSTANCE_IP_ADDRESS) @Nonnull
            IpAddress ipAddress,
        @JsonProperty(AzureEntities.JSON_KEY_CONTAINER_INSTANCE_SUBNET_IDS) @Nonnull
            Set<IdReference> subnetIds) {
      checkArgument(ipAddress != null, "ipAddress must be provided");
      checkArgument(subnetIds != null, "subnetIds must be provided");
      checkArgument(!subnetIds.isEmpty(), "at least one subnetId must be provided");

      _containers = Optional.ofNullable(containers).orElse(new HashSet<>());
      _ipAddress = ipAddress;
      _subnetIds = subnetIds;
    }

    public Set<ContainerInstance> getContainers() {
      return _containers;
    }

    public IpAddress getIpAddress() {
      return _ipAddress;
    }

    public Set<IdReference> getSubnetIds() {
      return _subnetIds;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class IpAddress implements Serializable {

    private final @Nonnull Ip _ip;
    private final @Nonnull Set<Port> _ports;

    public IpAddress(
        @JsonProperty(AzureEntities.JSON_KEY_CONTAINER_INSTANCE_IP) @Nonnull Ip ip,
        @JsonProperty(AzureEntities.JSON_KEY_CONTAINER_INSTANCE_PORTS) @Nullable Set<Port> ports) {
      checkArgument(ip != null, "ip must be provided");

      _ip = ip;
      _ports = Optional.ofNullable(ports).orElse(new HashSet<>());
    }

    public Ip getIp() {
      return _ip;
    }

    public Set<Port> getPorts() {
      return _ports;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Port implements Serializable {

    private final @Nonnull IpProtocol _protocol;
    private final int _port;

    public Port(
        @JsonProperty(AzureEntities.JSON_KEY_CONTAINER_INSTANCE_PORT_PROTOCOL) @Nonnull
            IpProtocol protocol,
        @JsonProperty(AzureEntities.JSON_KEY_CONTAINER_INSTANCE_PORT_NUMBER) @Nonnull
            Integer port) {
      checkArgument(protocol != null, "protocol must be defined");
      checkArgument(port != null, "port number must be defined");
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
        return (other.getPort() == getPort() && other.getProtocol().equals(getProtocol()));
      }
      return false;
    }

    @Override
    public int hashCode() {
      return _protocol.hashCode() + _port;
    }
  }

  /**
   * returns the associated subnet (id) with this containerGroup.
   *
   * @return ID
   */
  public @Nullable String getSubnetId() {
    return getProperties().getSubnetIds().stream().findAny().map(IdReference::getId).orElse(null);
  }

  /**
   * Returns the {@link Configuration} node for this ContainerGroup.
   *
   * <p>Creates ContainerGroup node which is connected to subnet node : toSubnet. Creates
   * ContainerInstance nodes which are connected to ContainerGroup node : toContainerInstances.
   * apply Transformations rules for handling container opened ports and source NAT.
   */
  @Override
  public Configuration toConfigurationNode(
      Region region, ConvertedConfiguration convertedConfiguration) {

    Configuration containerGroupNode =
        Configuration.builder()
            .setHostname(getCleanId())
            .setHumanName(getName())
            .setDomainName("azure")
            .setDeviceModel(DeviceModel.AZURE_CONTAINER_GROUP)
            .setDefaultInboundAction(LineAction.PERMIT)
            .setDefaultCrossZoneAction(LineAction.PERMIT)
            .setConfigurationFormat(ConfigurationFormat.AZURE)
            .build();

    Vrf.builder().setName(Configuration.DEFAULT_VRF_NAME).setOwner(containerGroupNode).build();

    Subnet subnet = region.findResource(getSubnetId(), Subnet.class);

    Interface toSubnet =
        Interface.builder()
            .setName("subnet")
            .setOwner(containerGroupNode)
            .setAddress(
                ConcreteInterfaceAddress.create(
                    getProperties().getIpAddress().getIp(),
                    subnet.getProperties().getAddressPrefix().getPrefixLength()))
            .setVrf(containerGroupNode.getDefaultVrf())
            .setType(InterfaceType.PHYSICAL)
            .build();

    subnet.connectToHost(region, convertedConfiguration, containerGroupNode, toSubnet);

    containerGroupNode
        .getDefaultVrf()
        .getStaticRoutes()
        .add(
            StaticRoute.builder()
                .setNetwork(Prefix.ZERO)
                .setNextHopInterface(toSubnet.getName())
                .setNextHopIp(subnet.computeInstancesIfaceIp())
                .setAdministrativeCost(0)
                .setMetric(0)
                .build());

    Ip toContainerInstancesIp = Ip.parse("172.17.0.1");
    int mask = 16;

    ConcreteInterfaceAddress toContainerInstancesAddress =
        ConcreteInterfaceAddress.create(toContainerInstancesIp, mask);
    Interface toContainerInstances =
        Interface.builder()
            .setAddress(toContainerInstancesAddress)
            .setOwner(containerGroupNode)
            .setName("to-containers")
            .setVrf(containerGroupNode.getDefaultVrf())
            .setType(InterfaceType.PHYSICAL)
            .build();

    long containerIpLong = toContainerInstancesIp.asLong() + 1L;

    // will be used to store every dnat Transformation (used for providing open ports to containers)
    Transformation dnatTransformationStack = null;

    for (ContainerInstance containerInstance : getProperties().getContainers()) {

      Ip containerIp = Ip.create(containerIpLong++);

      Configuration containerInstanceNode =
          containerInstance.toConfigurationNode(
              region, containerGroupNode, containerIp, toContainerInstancesIp);
      convertedConfiguration.addNode(containerInstanceNode);

      convertedConfiguration.addLayer1Edge(
          containerInstanceNode.getHostname(), containerInstance.getInterfaceName(),
          containerGroupNode.getHostname(), toContainerInstances.getName());

      dnatTransformationStack =
          getContainerDNat(
              containerInstance,
              containerIp,
              getProperties().getIpAddress().getIp(),
              dnatTransformationStack);
    }

    // regular S-NAT
    toSubnet.setIncomingTransformation(dnatTransformationStack);
    toSubnet.setOutgoingTransformation(
        Transformation.when(
                new MatchHeaderSpace(
                    HeaderSpace.builder()
                        .setSrcIps(toContainerInstancesAddress.getPrefix().toIpSpace())
                        .setIpProtocols(IpProtocol.TCP, IpProtocol.UDP, IpProtocol.ICMP)
                        .build()))
            .apply(
                TransformationStep.assignSourceIp(getProperties().getIpAddress().getIp()),
                TransformationStep.assignSourcePort(1024, 65525))
            .build());

    toSubnet.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(
            FirewallSessionInterfaceInfo.Action.FORWARD_OUT_IFACE,
            ImmutableList.of(toSubnet.getName()),
            null,
            null));

    return containerGroupNode;
  }

  private Transformation getContainerDNat(
      ContainerInstance containerInstance,
      Ip containerInstanceIp,
      Ip containerGroupIp,
      Transformation initialTransformation) {
    Transformation dnatTransformationStack = initialTransformation;

    // D-NAT
    Set<SubRange> tcpPorts =
        containerInstance.getProperties().getPorts().stream()
            .filter(port -> port.getProtocol().equals(IpProtocol.TCP))
            .map(port -> new SubRange(port.getPort()))
            .collect(Collectors.toSet());

    Set<SubRange> udpPorts =
        containerInstance.getProperties().getPorts().stream()
            .filter(port -> port.getProtocol().equals(IpProtocol.UDP))
            .map(port -> new SubRange(port.getPort()))
            .collect(Collectors.toSet());

    if (!tcpPorts.isEmpty()) {
      dnatTransformationStack =
          Transformation.when(
                  new MatchHeaderSpace(
                      HeaderSpace.builder()
                          .setDstIps(containerGroupIp.toIpSpace())
                          .setDstPorts(tcpPorts)
                          .setIpProtocols(IpProtocol.TCP)
                          .build()))
              .apply(TransformationStep.assignDestinationIp(containerInstanceIp))
              .setOrElse(dnatTransformationStack)
              .build();
    }
    if (!udpPorts.isEmpty()) {
      dnatTransformationStack =
          Transformation.when(
                  new MatchHeaderSpace(
                      HeaderSpace.builder()
                          .setDstIps(containerGroupIp.toIpSpace())
                          .setDstPorts(udpPorts)
                          .setIpProtocols(IpProtocol.UDP)
                          .build()))
              .apply(TransformationStep.assignDestinationIp(containerInstanceIp))
              .setOrElse(dnatTransformationStack)
              .build();
    }

    return dnatTransformationStack;
  }
}
