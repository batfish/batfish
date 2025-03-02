package org.batfish.representation.azure;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.BatfishException;
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

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VirtualMachine extends Instance implements Serializable {

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
    public Configuration toConfigurationNode(Region rgp, ConvertedConfiguration convertedConfiguration){
        Configuration cfgNode = Configuration.builder()
                .setHostname(getCleanId())
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

            // temporary (unclear information's about multi ips and subnet on a unique interface)
            // only 1 IP per endpoint (instance) for now
            Subnet subnet = null;
            ConcreteInterfaceAddress concreteInterfaceAddress = null;

            for (IPConfiguration ipConfiguration : networkInterface.getProperties().getIPConfigurations()){
                subnet = rgp.getSubnets().get(ipConfiguration.getProperties().getSubnetId());
                // TODO: need to draw edge between subnet and interface ip

                concreteInterfaceAddress = ConcreteInterfaceAddress.create(
                        ipConfiguration.getProperties().getPrivateIpAddress(),
                        subnet.getProperties().getAddressPrefix().getPrefixLength());
            }

            // security check for the trick above
            if (subnet == null) continue;

            // assign itself to this device through setOwner
            Interface currentInterface = Interface.builder()
                    .setName(networkInterface.getCleanId())
                    .setAddress(concreteInterfaceAddress)
                    .setHumanName(networkInterface.getName())
                    .setOwner(cfgNode)
                    .setVrf(cfgNode.getDefaultVrf())
                    .build();

            // default route to the subnet iface (to be clarified)
            StaticRoute st = StaticRoute.builder()
                    .setNextHopIp(subnet.computeInstancesIfaceIp())
                    .setAdministrativeCost(0)
                    .setMetric(0)
                    .setNetwork(Prefix.ZERO)
                    .build();

            cfgNode.getDefaultVrf().getStaticRoutes().add(st);

            // draw edges toward other devices on the subnet
            // gateway like aws or one edge between each device ?
            // set a virtual switch ? (layer 2 node)
            convertedConfiguration.addLayer1Edge(
                    cfgNode.getHostname(), networkInterface.getCleanId(),
                    subnet.getNodeName(), subnet.getToLanInterfaceName());

            // ACL
            {
                String nsgId = networkInterface.getProperties().getNetworkSecurityGroupID();
                if(nsgId != null) {
                    NetworkSecurityGroup nsg =
                            rgp.getNetworkSecurityGroups().get(nsgId);

                    if (nsg == null) {
                        throw new BatfishException(String.format("Unable to apply the NSG %s on subnet %s.\n" +
                                "Missing nsg file !", getCleanId(), nsgId));
                    }

                    nsg.applyToInterface(currentInterface);
                }
            }

        }

        return cfgNode;
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VirtualMachineProperties implements Serializable{
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
