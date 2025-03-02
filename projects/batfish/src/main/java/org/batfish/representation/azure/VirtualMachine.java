package org.batfish.representation.azure;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.DeviceType;

import java.util.Set;

class Properties{
    private final NetworkProfile _networkProfile;

    @JsonCreator
    Properties(
            @JsonProperty(required = true) NetworkProfile networkProfile
    ){
        _networkProfile = networkProfile;
    }

    NetworkProfile getNetworkProfile() {
        return _networkProfile;
    }
}

public class VirtualMachine extends Instance{

    private final Properties _properties;


    @JsonIgnoreProperties(ignoreUnknown = true)
    public VirtualMachine(
            @JsonProperty(AzureEntities.JSON_KEY_ID) String id,
            @JsonProperty(AzureEntities.JSON_KEY_TYPE) String type,
            @JsonProperty(AzureEntities.JSON_KEY_NAME) String name,
            @JsonProperty(AzureEntities.JSON_KEY_NAME) Properties properties) {
        super(id, type, name);
        _properties = properties;
    }


    @Override
    public Configuration toConfigurationNode(ResourceGroup rgp){
        Configuration cfgNode = Configuration.builder()
                .setHostname(getId())
                .setHumanName(getName())
                .build();

        cfgNode.setDeviceType(DeviceType.HOST);

        for(NetworkInterfaceId networkInterfaceId : _properties.getNetworkProfile().getNetworkInterfaces()){
            NetworkInterface concreteNetworkInterface =  rgp.getInterfaces().get(networkInterfaceId.getId());

        }

        return cfgNode;
    }


}
