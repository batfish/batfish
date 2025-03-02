package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.route.nh.NextHop;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Subnet extends Resource implements Serializable {

    final private SubnetProperties _properties;

    @JsonCreator
    private static Subnet create(
         @JsonProperty(AzureEntities.JSON_KEY_ID) @Nullable String id,
         @JsonProperty(AzureEntities.JSON_KEY_NAME) @Nullable String name,
         @JsonProperty(AzureEntities.JSON_KEY_TYPE) @Nullable String type,
         @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) @Nullable SubnetProperties properties
    ){
        return new Subnet(id, name, type, properties);
    }

    public Subnet(String id, String name, String type, SubnetProperties properties) {
        super(name, id, type);
        _properties = properties;
    }

    public Ip computeInstancesIfaceIp(){
        // give first IP like a router ?
        long generatedIp = _properties.getAddressPrefix().getStartIp().asLong() + 1L;
        return Ip.create(generatedIp);
    }

    public String getNodeName(){
        return "subnet-node-" + getName().toLowerCase();
    }

    public String getToLanInterfaceName(){
        return "to-lan";
    }
    public String getToVnetInterfaceName(){
        return getName() + "-to-vnet" ;
    }

    public Configuration toConfigurationNode(Region rgp, ConvertedConfiguration convertedConfiguration){
        Configuration cfgNode = Configuration.builder()
                .setHumanName(getName())
                .setHostname(getNodeName())
                .setDomainName("azure")
                .setConfigurationFormat(ConfigurationFormat.AZURE)
                .setDefaultCrossZoneAction(LineAction.PERMIT)
                .setDefaultInboundAction(LineAction.PERMIT)
                .setDeviceModel(DeviceModel.AZURE_SUBNET)
                .build();

        Vrf.builder()
                .setName(Configuration.DEFAULT_VRF_NAME)
                .setOwner(cfgNode)
                .build();

        { // LAN
            Ip instancesIfaceIp = computeInstancesIfaceIp();
            ConcreteInterfaceAddress instancesIfaceAddress =
                    ConcreteInterfaceAddress.create(instancesIfaceIp, _properties.getAddressPrefix().getPrefixLength());

            Interface lanInterface = Interface.builder()
                    .setAddress(instancesIfaceAddress)
                    .setName(getToLanInterfaceName())
                    .setOwner(cfgNode)
                    .setVrf(cfgNode.getDefaultVrf())
                    .build();

            // ACL
            {
                String nsgId = _properties.getNetworkSecurityGroupId();
                if (nsgId != null) {
                    NetworkSecurityGroup nsg =
                            rgp.getNetworkSecurityGroups().get(nsgId);

                if (nsg == null) {
                    throw new BatfishException(String.format("Unable to apply the NSG %s on subnet %s.\n" +
                            "Missing nsg file !", nsgId, getCleanId()));
                }

                    nsg.applyToInterface(lanInterface);
                }
            }
        }

        { // VNet

            Interface.builder()
                    .setAddress(LinkLocalAddress.of(AzureConfiguration.LINK_LOCAL_IP))
                    .setVrf(cfgNode.getDefaultVrf())
                    .setOwner(cfgNode)
                    .setName(getToVnetInterfaceName())
                    .setDescription("to vnet interface")
                    .build();

            // static route set from Vnet object
        }

        { // Nat gateway
            Interface toNat = Interface.builder()
                    .setVrf(cfgNode.getDefaultVrf())
                    .setOwner(cfgNode)
                    .setName("nat-gateway")
                    .setAddress(LinkLocalAddress.of(AzureConfiguration.LINK_LOCAL_IP))
                    .setDescription("to nat gateway")
                    .build();

            Configuration natGatewayNode;
            String natGatewayId = getProperties().getNatGatewayId();
            if (natGatewayId != null) {
                natGatewayNode = convertedConfiguration.getNode(natGatewayId);

                if (natGatewayNode == null) {
                    throw new BatfishException("Gateway not found : " + natGatewayId);
                }

                convertedConfiguration.addLayer1Edge(
                        cfgNode.getHostname(), toNat.getName(),
                        natGatewayNode.getHostname(), "subnet"
                );

                StaticRoute st = StaticRoute.builder()
                        .setNetwork(getProperties().getAddressPrefix())
                        .setNonForwarding(false)
                        .setNextHop(NextHop.legacyConverter("subnet", AzureConfiguration.LINK_LOCAL_IP))
                        .setAdministrativeCost(0)
                        .setMetric(0)
                        .build();

                natGatewayNode.getDefaultVrf().getStaticRoutes().add(st);
            }
        }

        return cfgNode;
    }

    public SubnetProperties getProperties() {
        return _properties;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubnetProperties implements Serializable{
        final private Prefix _addressPrefix;
        final private NetworkSecurityGroupId _nsg;
        final private String _natGatewayId;
        final private List<IpConfiguration> _ipConfigurations;

        @JsonCreator
        public static SubnetProperties create(
                @JsonProperty("addressPrefix") @Nullable Prefix addressPrefix,
                @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_NGS) NetworkSecurityGroupId nsg,
                @JsonProperty("natGateway") String natGatewayId,
                @JsonProperty("ipConfigurations") List<IpConfiguration> ipConfigurations
        ){
            return new SubnetProperties(addressPrefix, nsg, natGatewayId, ipConfigurations);
        }

        SubnetProperties(Prefix addressPrefix, NetworkSecurityGroupId nsg, String natGatewayId, List<IpConfiguration> ipConfigurations) {
            _addressPrefix = addressPrefix;
            _nsg = nsg;
            _natGatewayId = natGatewayId;
            _ipConfigurations = ipConfigurations;
        }

        public Prefix getAddressPrefix() {
            return _addressPrefix;
        }

        public String getNetworkSecurityGroupId(){
            if (_nsg == null) return null;
            return _nsg.getId();
        }

        public String getNatGatewayId() {
            return _natGatewayId;
        }

        public List<IpConfiguration> getIpConfigurations() {
            return _ipConfigurations;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NetworkSecurityGroupId implements Serializable{
        private final String _id;

        @JsonCreator
        public NetworkSecurityGroupId(
                @JsonProperty(AzureEntities.JSON_KEY_ID) String id)
        {
            _id = id;
        }

        public String getId() {
            return _id;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IpConfiguration implements Serializable{
        private final String _id;

        public IpConfiguration(
                @JsonProperty(AzureEntities.JSON_KEY_ID) String id
        ) {
            _id = id;
        }

        public String getId() {
            return _id;
        }
    }

}
