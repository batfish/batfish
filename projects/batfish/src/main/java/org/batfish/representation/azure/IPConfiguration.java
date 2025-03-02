package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.Ip;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IPConfiguration extends Resource {

    private final IPConfigurationProperties _properties;

    @JsonCreator
    IPConfiguration(
            @JsonProperty(AzureEntities.JSON_KEY_NAME) String name,
            @JsonProperty(AzureEntities.JSON_KEY_TYPE) String type,
            @JsonProperty(AzureEntities.JSON_KEY_ID) String id,
            @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) IPConfigurationProperties properties
            ) {
        super(name, id, type);
        _properties = properties;
    }

    public IPConfigurationProperties getProperties() {
        return _properties;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IPConfigurationProperties {
        private final Ip _privateIpAddress;

        @JsonCreator
        IPConfigurationProperties(
                @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_PRIVATE_IP_ADDRESS) Ip privateIpAddress
        ) {
            _privateIpAddress = privateIpAddress;
        }

        public Ip getPrivateIpAddress() {
            return _privateIpAddress;
        }
    }
}
