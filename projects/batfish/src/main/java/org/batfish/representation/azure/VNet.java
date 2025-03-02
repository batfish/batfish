package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Prefix;

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
        super(id, name, type);
        _properties = properties;
    }

    public Configuration toConfigurationNode(){
        Configuration cfgNode = Configuration.builder()
                .setHostname(getCleanId())
                .setDeviceModel(DeviceModel.AZURE_VNET)
                .setConfigurationFormat(ConfigurationFormat.AZURE)
                .setHumanName(getName()).build();

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




