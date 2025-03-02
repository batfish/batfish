package org.batfish.representation.azure;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.DeviceType;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VirtualMachine extends Instance{

    private final VirtualMachineProperties _properties;


    @JsonCreator
    public VirtualMachine(
            @JsonProperty(AzureEntities.JSON_KEY_ID) String id,
            @JsonProperty(AzureEntities.JSON_KEY_TYPE) String type,
            @JsonProperty(AzureEntities.JSON_KEY_NAME) String name,
            @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) VirtualMachineProperties properties) {
        super(name, id, type);
        _properties = properties;
    }


    @Override
    public Configuration toConfigurationNode(ResourceGroup rgp){
        Configuration cfgNode = Configuration.builder()
                .setHostname(getId().replace('/', '_'))
                .setHumanName(getName())
                .setDomainName("azure")
                .setDeviceModel(DeviceModel.AZURE_VM)
                .setDefaultInboundAction(LineAction.PERMIT)
                .setDefaultCrossZoneAction(LineAction.PERMIT)
                .setConfigurationFormat(ConfigurationFormat.AZURE)
                .build();

        cfgNode.setDeviceType(DeviceType.HOST);

        Vrf.builder()
                .setName(Configuration.DEFAULT_VRF_NAME)
                .setOwner(cfgNode)
                .build();

        for(NetworkInterfaceId networkInterfaceId : _properties.getNetworkProfile().getNetworkInterfaces()){
            NetworkInterface networkInterface =  rgp.getInterfaces().get(networkInterfaceId.getId());


            ConcreteInterfaceAddress concreteInterfaceAddress = null;
            for (IPConfiguration ipConfiguration : networkInterface.getProperties().getIPConfigurations()){
                concreteInterfaceAddress = ConcreteInterfaceAddress.create(
                        ipConfiguration.getProperties().getPrivateIpAddress(), 24);
            }

            // assign itself to this device through cfgNode
            Interface.builder()
                    .setName(networkInterface.getId().replace('/', '_'))
                    .setAddress(concreteInterfaceAddress)
                    .setHumanName(networkInterface.getName())
                    .setOwner(cfgNode)
                    .setVrf(cfgNode.getDefaultVrf())
                    .build();

            // default route
            StaticRoute st = StaticRoute.builder()
                    .setNextHopIp(concreteInterfaceAddress.getIp())
                    .setAdministrativeCost(0)
                    .setMetric(0)
                    .setNetwork(Prefix.ZERO)
                    .build();

            cfgNode.getDefaultVrf().getStaticRoutes().add(st);

            // draw edges toward other devices on the subnet
            // gateway like aws or one edge between each device ?
            // set a virtual switch ? (layer 2 node)

        }

        return cfgNode;
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VirtualMachineProperties{
        private final NetworkProfile _networkProfile;

        @JsonCreator
        VirtualMachineProperties(
                @JsonProperty(AzureEntities.JSON_KEY_NETWORK_PROFILE) NetworkProfile networkProfile
        ){
            _networkProfile = networkProfile;
        }

        NetworkProfile getNetworkProfile() {
            return _networkProfile;
        }
    }

}
