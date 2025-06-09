package org.batfish.representation.azure;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.Configuration;

import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
class NetworkProfile {
    private final Set<NetworkInterfaceId> _networkInterfaces;

    @JsonCreator
    NetworkProfile(
            @JsonProperty(AzureEntities.JSON_KEY_NETWORK_INTERFACE_ID) Set<NetworkInterfaceId> networkInterfaces
    ) {

        if(networkInterfaces == null) {
            networkInterfaces = new HashSet<>();
        }
        _networkInterfaces = networkInterfaces;
    }

    Set<NetworkInterfaceId> getNetworkInterfaces() {
        return _networkInterfaces;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class NetworkInterfaceId {
    private final String _id;

    @JsonCreator
    NetworkInterfaceId(
            @JsonProperty(AzureEntities.JSON_KEY_ID) String id) {
        _id = id;
    }

    String getId() {
        return _id;
    }
}

public abstract class Instance extends Resource {

    public Instance(String name, String id, String type) {
        super(name, id, type);
    }



    public abstract Configuration toConfigurationNode(ResourceGroup rgp, ConvertedConfiguration convertedConfiguration);
}
