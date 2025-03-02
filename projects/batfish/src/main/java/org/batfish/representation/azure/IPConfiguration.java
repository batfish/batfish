package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.Ip;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IPConfiguration extends Resource {

    private final Properties _properties;

    @JsonCreator
    IPConfiguration(
            @JsonProperty(AzureEntities.JSON_KEY_NAME) String name,
            @JsonProperty(AzureEntities.JSON_KEY_TYPE) String type,
            @JsonProperty(AzureEntities.JSON_KEY_ID) String id,
            @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) Properties properties
            ) {
        super(name, id, type);
        _properties = properties;
    }

    public Properties getProperties() {
        return _properties;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties implements Serializable {
        private final Ip _privateIpAddress;
        private final IdReference _subnet;

        @JsonCreator
        public Properties(
                @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_PRIVATE_IP_ADDRESS) Ip privateIpAddress,
                @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_SUBNET) IdReference subnet
        ) {
            _privateIpAddress = privateIpAddress;
            _subnet = subnet;
        }

        public Ip getPrivateIpAddress() {
            return _privateIpAddress;
        }
        public String getSubnetId(){
            return _subnet.getId();
        }
    }
}
