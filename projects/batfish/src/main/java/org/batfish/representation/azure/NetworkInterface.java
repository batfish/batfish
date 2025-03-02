package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.Ip;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
class IPConfigurationProperties {

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

@JsonIgnoreProperties(ignoreUnknown = true)
class IPConfiguration extends Resource {

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
}

@JsonIgnoreProperties(ignoreUnknown = true)
class NetworkInterfaceProperties {
    private final Set<IPConfiguration> _ipConfigurations;
    private final String _macAddress;

    @JsonCreator
    NetworkInterfaceProperties(
            @JsonProperty(AzureEntities.JSON_KEY_VNET_IP_CONFIGURATIONS) Set<IPConfiguration> ipConfigurations,
            @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_MAC_ADDRESS) String macAddress
    ) {
        _ipConfigurations = ipConfigurations;
        _macAddress = macAddress;
    }

    public Set<IPConfiguration> getIPConfigurations() {
        return _ipConfigurations;
    }

    public String getMacAddress() {
        return _macAddress;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkInterface extends Resource{

    private final NetworkInterfaceProperties _properties;

    @JsonCreator
    public NetworkInterface(
            @JsonProperty(AzureEntities.JSON_KEY_NAME) String name,
            @JsonProperty(AzureEntities.JSON_KEY_ID) String id,
            @JsonProperty(AzureEntities.JSON_KEY_TYPE) String type,
            @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) NetworkInterfaceProperties properties) {
        super(name, id, type);
        _properties = properties;
    }

    public NetworkInterfaceProperties getProperties() {
        return _properties;
    }
}
