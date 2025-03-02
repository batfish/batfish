package org.batfish.representation.azure;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.Configuration;

import java.util.Set;

class NetworkProfile {
    private final Set<Instance.NetworkInterfaceId> _networkInterfaces;

    @JsonCreator
    NetworkProfile(
            @JsonProperty Set<Instance.NetworkInterfaceId> networkInterfaces
    ) {
        _networkInterfaces = networkInterfaces;
    }

    Set<Instance.NetworkInterfaceId> getNetworkInterfaces() {
        return _networkInterfaces;
    }
}

public abstract class Instance extends Resource {

    public Instance(String name, String id, String type) {
        super(name, id, type);
    }

    static class NetworkInterfaceId {
        private final String _id;

        @JsonCreator
        NetworkInterfaceId(
                @JsonProperty String id) {
            _id = id;
        }

        String getId() {
            return _id;
        }
    }

    public abstract Configuration toConfigurationNode(ResourceGroup rgp);
}
