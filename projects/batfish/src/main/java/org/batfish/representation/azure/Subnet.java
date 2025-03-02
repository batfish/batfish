package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Vrf;

import javax.annotation.Nullable;



@JsonIgnoreProperties(ignoreUnknown = true)
public class Subnet extends Resource {

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
        return "subnet-node-" + getName();
    }

    public String getInterfaceName(){
        return "subnet-interface-" + getName();
    }

    public Configuration toConfigurationNode(ResourceGroup rgp, ConvertedConfiguration convertedConfiguration){
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

        Ip instancesIfaceIp = computeInstancesIfaceIp();
        ConcreteInterfaceAddress instancesIfaceAddress =
                ConcreteInterfaceAddress.create(instancesIfaceIp, _properties.getAddressPrefix().getPrefixLength());

        Interface.builder()
                .setAddress(instancesIfaceAddress)
                .setName(getInterfaceName())
                .setOwner(cfgNode)
                .setVrf(cfgNode.getDefaultVrf())
                .build();

        return cfgNode;
    }

    public SubnetProperties getProperties() {
        return _properties;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubnetProperties {
        final private Prefix _addressPrefix;

        @JsonCreator
        public static SubnetProperties create(
                @JsonProperty("addressPrefix") @Nullable Prefix addressPrefix
        ){
            return new SubnetProperties(addressPrefix);
        }

        SubnetProperties(Prefix addressPrefix) {
            _addressPrefix = addressPrefix;
        }

        Prefix getAddressPrefix() {
            return _addressPrefix;
        }
    }
}
