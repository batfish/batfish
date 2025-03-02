package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents an Azure Network Interface which is used for {@link VirtualMachine} objects.
 * <a href="https://learn.microsoft.com/en-us/azure/templates/microsoft.network/networkinterfaces?pivots=deployment-language-arm-template">Resource Link</a>.
 * <p>
 * Partially implemented:
 * <li>ipv6 ipConfigurations not implemented</li>
 * </p>
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkInterface extends Resource implements Serializable {

    private final @Nonnull Properties _properties;

    @JsonCreator
    public NetworkInterface(
            @JsonProperty(AzureEntities.JSON_KEY_NAME) @Nullable String name,
            @JsonProperty(AzureEntities.JSON_KEY_ID) @Nullable String id,
            @JsonProperty(AzureEntities.JSON_KEY_TYPE) @Nullable String type,
            @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) @Nullable Properties properties) {
        super(name, id, type);
        checkArgument(properties != null, "properties must be provided");
        _properties = properties;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties implements Serializable {

        private final @Nonnull Set<IPConfiguration> _ipConfigurations;
        private final @Nullable String _macAddress;
        private final @Nullable IdReference _nsg;

        @JsonCreator
        public Properties(
                @JsonProperty(AzureEntities.JSON_KEY_VNET_IP_CONFIGURATIONS) @Nullable Set<IPConfiguration> ipConfigurations,
                @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_MAC_ADDRESS) @Nullable String macAddress,
                @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_NGS) @Nullable IdReference nsg
        ) {
            if (ipConfigurations == null) ipConfigurations = new HashSet<>();
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
