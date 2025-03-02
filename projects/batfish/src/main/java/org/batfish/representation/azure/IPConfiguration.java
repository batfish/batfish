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
        private final Subnet _subnet;

        @JsonCreator
        IPConfigurationProperties(
                @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_PRIVATE_IP_ADDRESS) Ip privateIpAddress,
                @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_SUBNET) Subnet subnet
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Subnet{
        private final String _id;

        @JsonCreator
        Subnet(
                @JsonProperty(AzureEntities.JSON_KEY_ID) String id
        ){
            _id= id;
        }

        public String getId() {
            return _id;
        }
    }
}
