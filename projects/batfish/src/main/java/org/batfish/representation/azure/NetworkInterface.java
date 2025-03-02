package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkInterface extends Resource implements Serializable {

    private final Properties _properties;

    @JsonCreator
    public NetworkInterface(
            @JsonProperty(AzureEntities.JSON_KEY_NAME) String name,
            @JsonProperty(AzureEntities.JSON_KEY_ID) String id,
            @JsonProperty(AzureEntities.JSON_KEY_TYPE) String type,
            @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) Properties properties) {
        super(name, id, type);
        _properties = properties;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties implements Serializable {
        private final Set<IPConfiguration> _ipConfigurations;
        private final String _macAddress;
        private final IdReference _nsg;

        @JsonCreator
        public Properties(
                @JsonProperty(AzureEntities.JSON_KEY_VNET_IP_CONFIGURATIONS) Set<IPConfiguration> ipConfigurations,
                @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_MAC_ADDRESS) String macAddress,
                @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_NGS) IdReference nsg
        ) {
            _ipConfigurations = ipConfigurations;
            _macAddress = macAddress;
            _nsg = nsg;
        }

        public Set<IPConfiguration> getIPConfigurations() {
            return _ipConfigurations;
        }

        // useful for ipv6 ? (compute local link ipv6 from mac address)
        public String getMacAddress() {
            return _macAddress;
        }

        public String getNetworkSecurityGroupID() {
            if (_nsg == null) {return null;}
            return _nsg.getId();
        }
    }

    public Properties getProperties() {
        return _properties;
    }
}
