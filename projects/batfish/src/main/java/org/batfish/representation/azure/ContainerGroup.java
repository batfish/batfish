package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents Azure Container Group objects.
 * <a href="https://learn.microsoft.com/en-us/azure/templates/microsoft.containerinstance/containergroups?pivots=deployment-language-arm-template">Resource link</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContainerGroup extends Instance implements Serializable {

    private final @Nonnull Properties _properties;

    @JsonCreator
    public ContainerGroup(
            @JsonProperty(AzureEntities.JSON_KEY_ID) @Nullable String id,
            @JsonProperty(AzureEntities.JSON_KEY_NAME) @Nullable String name,
            @JsonProperty(AzureEntities.JSON_KEY_TYPE) @Nullable String type,
            @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) @Nullable Properties properties
    ){
        super(name, id, type);
        checkArgument(properties != null, "properties must be provided");
        _properties = properties;
    }

    public Properties getProperties() {
        return _properties;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties implements Serializable {

        private final @Nonnull Set<ContainerInstance> _containers;
        private final @Nonnull IpAddress _ipAddress;
        private final @Nonnull Set<IdReference> _subnetIds;


        @JsonCreator
        public Properties(
                @JsonProperty(AzureEntities.JSON_KEY_CONTAINER_GROUP_CONTAINERS) @Nullable Set<ContainerInstance> containers,
                @JsonProperty(AzureEntities.JSON_KEY_CONTAINER_INSTANCE_IP_ADDRESS) @Nullable IpAddress ipAddress,
                @JsonProperty(AzureEntities.JSON_KEY_CONTAINER_INSTANCE_SUBNET_IDS) @Nullable Set<IdReference> subnetIds
        ) {
            if(containers == null) containers = new HashSet<>();
            checkArgument(ipAddress != null, "ipAddress must be provided");
            checkArgument(subnetIds != null, "subnetIds must be provided");
            checkArgument(!subnetIds.isEmpty(), "at least one subnetId must be provided");

            _containers = containers;
            _ipAddress = ipAddress;
            _subnetIds = subnetIds;
        }

        public Set<ContainerInstance> getContainers(){
            return _containers;
        }
        public IpAddress getIpAddress(){
            return _ipAddress;
        }
        public Set<IdReference> getSubnetIds(){
            return _subnetIds;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IpAddress implements Serializable {

        private final @Nonnull Ip _ip;
        private final @Nonnull Set<Port> _ports;

        public IpAddress(
                @JsonProperty(AzureEntities.JSON_KEY_CONTAINER_INSTANCE_IP) @Nullable Ip ip,
                @JsonProperty(AzureEntities.JSON_KEY_CONTAINER_INSTANCE_PORTS) @Nullable Set<Port> ports
        ) {
            checkArgument(ip != null, "ip must be provided");
            if(ports == null) ports = new HashSet<>();
            _ip = ip;
            _ports = ports;
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
        private final @Nonnull int _port;

        public Port(
                @JsonProperty(AzureEntities.JSON_KEY_CONTAINER_INSTANCE_PORT_PROTOCOL) @Nullable IpProtocol protocol,
                @JsonProperty(AzureEntities.JSON_KEY_CONTAINER_INSTANCE_PORT_NUMBER) @Nullable Integer port
        ) {
            checkArgument(protocol != null, "protocol must be defined");
            checkArgument(port != null, "port number must be defined");
            _protocol = protocol;
            _port = port;
        }

        public IpProtocol getProtocol() {
            return _protocol;
        }
        public int getPort() {
            return _port;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Port other) {
                return (other.getPort() == getPort()
                && other.getProtocol().equals(getProtocol()));
            }
            return false;
        }

        @Override
        public int hashCode() {
            return _protocol.hashCode() + _port;
        }

    }

    /**
     * Returns the {@link Configuration} node for this ContainerGroup.
     * <p>Creates ContainerGroup node which is connected to subnet node : toSubnet.
     * Creates ContainerInstance nodes which are connected to ContainerGroup node : toContainerInstances.
     * apply Transformations rules for handling container opened ports and source NAT.</p>
     */
    public Configuration toConfigurationNode(Region region, ConvertedConfiguration convertedConfiguration) {

        Configuration containerGroupNode = Configuration.builder()
                .setHostname(getCleanId())
                .setHumanName(getName())
                .setDomainName("azure")
                .setDeviceModel(DeviceModel.AZURE_CONTAINER_GROUP)
                .setDefaultInboundAction(LineAction.PERMIT)
                .setDefaultCrossZoneAction(LineAction.PERMIT)
                .setConfigurationFormat(ConfigurationFormat.AZURE)
                .build();

        Vrf.builder()
                .setName(Configuration.DEFAULT_VRF_NAME)
                .setOwner(containerGroupNode)
                .build();

        String subnetId = null;
        for (IdReference idReference : getProperties().getSubnetIds()) {
            subnetId = idReference.getId();
        }
        Subnet subnet = region.getSubnets().get(subnetId);

        Interface toSubnet = Interface.builder()
                .setName("to-subnet")
                .setOwner(containerGroupNode)
                .setAddress(
                        ConcreteInterfaceAddress.create(
                                getProperties().getIpAddress().getIp(),
                                subnet.getProperties().getAddressPrefix().getPrefixLength()
                        )
                )
                .setVrf(containerGroupNode.getDefaultVrf())
                .build();

        convertedConfiguration.addLayer1Edge(
                containerGroupNode.getHostname(), toSubnet.getName(),
                subnet.getNodeName(), subnet.getToLanInterfaceName()
        );


        containerGroupNode.getDefaultVrf().getStaticRoutes().add(
                StaticRoute.builder()
                    .setNetwork(Prefix.ZERO)
                    .setNextHopInterface(toSubnet.getName())
                    .setNextHopIp(subnet.computeInstancesIfaceIp())
                    .setAdministrativeCost(0)
                    .setMetric(0)
                    .build()
        );

        Interface toContainerInstances = Interface.builder()
                .setAddress(ConcreteInterfaceAddress.create(Ip.parse("172.17.0.1"), 16))
                .setOwner(containerGroupNode)
                .setName("to-containers")
                .setVrf(containerGroupNode.getDefaultVrf())
                .build();
        assert toContainerInstances.getConcreteAddress() != null;


        long containerIp = Ip.parse("172.17.0.2").asLong();

        // will be used to store every dnat Transformation (used for providing open ports to containers)
        Transformation dnatTransformationStack = null;

        for(ContainerInstance containerInstance : getProperties().getContainers()) {

            Configuration containerInstanceNode = Configuration.builder()
                    .setHostname(getCleanId() + "_" + containerInstance.getName())
                    .setHumanName(getName())
                    .setDomainName("azure")
                    .setDeviceModel(DeviceModel.AZURE_CONTAINER_INSTANCE)
                    .setDefaultInboundAction(LineAction.PERMIT)
                    .setDefaultCrossZoneAction(LineAction.PERMIT)
                    .setConfigurationFormat(ConfigurationFormat.AZURE)
                    .build();

            convertedConfiguration.addNode(containerInstanceNode);

            Vrf.builder()
                    .setOwner(containerInstanceNode)
                    .setName(Configuration.DEFAULT_VRF_NAME)
                    .build();

            Ip containerInstanceIp = Ip.create(containerIp++);

            Interface toContainerGroup = Interface.builder()
                    .setAddress(
                            ConcreteInterfaceAddress.create(containerInstanceIp,
                            subnet.getProperties().getAddressPrefix().getPrefixLength()))
                    .setOwner(containerInstanceNode)
                    .setName("to-container-group")
                    .setVrf(containerInstanceNode.getDefaultVrf())
                    .build();

            containerInstanceNode.getDefaultVrf().getStaticRoutes().add(
                    StaticRoute.builder()
                            .setMetric(0)
                            .setAdministrativeCost(0)
                            .setNextHopIp(toContainerInstances.getConcreteAddress().getIp())
                            .setNetwork(Prefix.ZERO)
                            .build()
            );

            convertedConfiguration.addLayer1Edge(
                    containerInstanceNode.getHostname(), toContainerGroup.getName(),
                    containerGroupNode.getHostname(), toContainerInstances.getName()
            );

            // DNAT
            Set<SubRange> tcpPorts = new HashSet<>();
            Set<SubRange> udpPorts = new HashSet<>();
            for(ContainerInstance.Port port : containerInstance.getProperties().getPorts()) {
                if (port.getProtocol().equals(IpProtocol.TCP)) {
                    tcpPorts.add(new SubRange(port.getPort()));
                } else if (port.getProtocol().equals(IpProtocol.UDP)) {
                    udpPorts.add(new SubRange(port.getPort()));
                }
            }

            if(!tcpPorts.isEmpty()) {
                dnatTransformationStack = Transformation
                        .when(
                                new MatchHeaderSpace(
                                        HeaderSpace.builder()
                                                .setDstPorts(tcpPorts)
                                                .setIpProtocols(IpProtocol.TCP)
                                                .build()
                                ))
                        .apply(TransformationStep.assignDestinationIp(containerInstanceIp))
                        .setOrElse(dnatTransformationStack)
                        .build();
            }
            if(!udpPorts.isEmpty()) {
                dnatTransformationStack = Transformation
                        .when(
                                new MatchHeaderSpace(
                                        HeaderSpace.builder()
                                                .setDstPorts(udpPorts)
                                                .setIpProtocols(IpProtocol.UDP)
                                                .build()))
                        .apply(TransformationStep.assignDestinationIp(containerInstanceIp))
                        .setOrElse(dnatTransformationStack)
                        .build();
            }
        }

        toSubnet.setIncomingTransformation(dnatTransformationStack);
        toSubnet.setOutgoingTransformation(
                Transformation
                        .when(
                                new MatchHeaderSpace(HeaderSpace.builder()
                                        .setIpProtocols(IpProtocol.TCP, IpProtocol.UDP, IpProtocol.ICMP).build())
                        ).apply(
                                TransformationStep.assignSourceIp(getProperties().getIpAddress().getIp()),
                                TransformationStep.assignSourcePort(1024, 65525)
                        ).build()
        );

        return containerGroupNode;
    }
}
