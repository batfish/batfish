package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VNet extends Resource {

    private final VNetProperties _properties;

    @JsonCreator
    public VNet(
            @JsonProperty(AzureEntities.JSON_KEY_ID) String id,
            @JsonProperty(AzureEntities.JSON_KEY_NAME) String name,
            @JsonProperty(AzureEntities.JSON_KEY_TYPE) String type,
            @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) VNetProperties properties) {
        super(name, id, type);
        _properties = properties;
    }

    public Configuration toConfigurationNode(Region region, ConvertedConfiguration convertedConfiguration) {
        Configuration cfgNode = Configuration.builder()
                .setHostname(getCleanId())
                .setDeviceModel(DeviceModel.AZURE_VNET)
                .setDomainName("azure")
                .setConfigurationFormat(ConfigurationFormat.AZURE)
                .setDefaultCrossZoneAction(LineAction.PERMIT)
                .setDefaultInboundAction(LineAction.PERMIT)
                .setHumanName(getName())
                .build();

        Vrf.builder()
                .setName(Configuration.DEFAULT_VRF_NAME)
                .setOwner(cfgNode)
                .build();


        for(Subnet subnet : _properties.getSubnets()){

            Configuration subnetNode = convertedConfiguration.getNode(subnet.getNodeName());

            Interface.builder()
                    .setName(subnet.getNodeName())
                    .setAddress(LinkLocalAddress.of(AzureConfiguration.LINK_LOCAL_IP))
                    .setVrf(cfgNode.getDefaultVrf())
                    .setOwner(cfgNode)
                    .build();

            convertedConfiguration.addLayer1Edge(
                    cfgNode.getHostname(), subnet.getNodeName(),
                    subnetNode.getHostname(), subnet.getToVnetInterfaceName()
            );

            convertedConfiguration.addLayer1Edge(
                    subnetNode.getHostname(), subnet.getToVnetInterfaceName(),
                    cfgNode.getHostname(), subnet.getNodeName()
            );

            // Add routes from subnet to VNet node (Address Space prefix)
            for(Prefix prefix : getProperties().getAddressSpace().getAddressPrefixes()) {
                subnetNode.getDefaultVrf().getStaticRoutes().add(
                        StaticRoute.builder()
                                .setNetwork(prefix)
                                .setNextHop(NextHop.legacyConverter(
                                        subnet.getToVnetInterfaceName(),
                                        AzureConfiguration.LINK_LOCAL_IP))
                                .setAdministrativeCost(0)
                                .setMetric(0)
                                .setNonForwarding(false)
                                .build()
                );
            }

            // Add route from VNet node to subnet node
            StaticRoute st = StaticRoute.builder()
                    .setAdministrativeCost(1)
                    .setNextHop(NextHop.legacyConverter(
                            subnet.getNodeName(),//subnet.getName() + "-to-vnet",
                            Ip.parse("169.254.0.1")))
                    .setNetwork(subnet.getProperties().getAddressPrefix())
                    .setNonForwarding(false)
                    .setMetric(0)
                    .build();

            cfgNode.getDefaultVrf().getStaticRoutes().add(st);

        }

        return cfgNode;
    }

    public VNetProperties getProperties() {
        return _properties;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VNetProperties implements Serializable {
        final private AddressSpace _addressSpace;
        final private Set<Subnet> _subnets;

        @JsonCreator
        public static VNetProperties create(
                @JsonProperty(AzureEntities.JSON_KEY_VNET_ADDRESS_SPACE) AddressSpace addressSpace,
                @JsonProperty(AzureEntities.JSON_KEY_VNET_SUBNETS) Set<Subnet> subnets
        ) {
            return new VNetProperties(addressSpace, subnets);
        }

        VNetProperties(AddressSpace addressSpace, Set<Subnet> subnets) {
            _addressSpace = addressSpace;
            _subnets = subnets;
        }

        public Set<Subnet> getSubnets() {
            return _subnets;
        }
        public AddressSpace getAddressSpace() { return _addressSpace; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddressSpace implements Serializable {
        final private List<Prefix> _addressPrefixes;
        // final private IpamPoolPrefixAllocations ipamPoolPrefixAllocations

        @JsonCreator
        public static AddressSpace create(
                @JsonProperty(AzureEntities.JSON_KEY_VNET_ADDRESS_PREFIX) @Nullable List<Prefix> addressPrefixes
        ) {
            return new AddressSpace(addressPrefixes);
        }

        AddressSpace(List<Prefix> addressPrefixes) {
            _addressPrefixes = addressPrefixes;
        }

        public List<Prefix> getAddressPrefixes() {
            return _addressPrefixes;
        }
    }

}




