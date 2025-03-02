package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

import javax.annotation.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
class SubnetProperties {
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

@JsonIgnoreProperties(ignoreUnknown = true)
public class Subnet extends Resource {

    final private SubnetProperties properties;

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
        this.properties = properties;
    }

    private Ip computeInstancesIfaceIp(){
        long generatedIp = properties.getAddressPrefix().getStartIp().asLong() + 1L;
        return Ip.create(generatedIp);
    }

    public Configuration toConfigurationNode(){
        Configuration cfgNode = Configuration.builder()
                .setHumanName(getName())
                .setHostname(getId())
                .build();

        Ip instancesIfaceIp = computeInstancesIfaceIp();
        ConcreteInterfaceAddress instancesIfaceAddress =
                ConcreteInterfaceAddress.create(instancesIfaceIp, properties.getAddressPrefix().getPrefixLength());

        return cfgNode;
    }
}
