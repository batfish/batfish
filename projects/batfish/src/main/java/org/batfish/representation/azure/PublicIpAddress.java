package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.Ip;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicIpAddress extends Resource implements Serializable {

    private final Properties _properties;

    @JsonCreator
    public PublicIpAddress(
            @JsonProperty(AzureEntities.JSON_KEY_ID) String id,
            @JsonProperty(AzureEntities.JSON_KEY_NAME) String name,
            @JsonProperty(AzureEntities.JSON_KEY_TYPE) String type,
            @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) Properties properties
    ){
        super(name, id, type);
        _properties = properties;
    }

    public Properties getProperties() {
        return _properties;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties implements Serializable {

        private final Ip _ipAddress;
        // todo type : can be static or dynamic

        @JsonCreator
        public Properties(
                @JsonProperty(AzureEntities.JSON_KEY_PUBLIC_IP_ADDRESS) Ip ipAddress
        ){
            _ipAddress = ipAddress;
        }

        public Ip getIpAddress() {
            return _ipAddress;
        }
    }
}
