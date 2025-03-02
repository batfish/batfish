package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.Ip;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkArgument;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IPConfiguration extends Resource {

    private final @Nonnull Properties _properties;

    @JsonCreator
    IPConfiguration(
            @JsonProperty(AzureEntities.JSON_KEY_NAME) @Nullable String name,
            @JsonProperty(AzureEntities.JSON_KEY_TYPE) @Nullable String type,
            @JsonProperty(AzureEntities.JSON_KEY_ID) @Nullable String id,
            @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) @Nullable Properties properties
            ) {
        super(name, id, type);
        checkArgument(properties != null, "properties must be provided");
        _properties = properties;
    }

    public Properties getProperties() {
        return _properties;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties implements Serializable {

        private final @Nonnull Ip _privateIpAddress;
        private final @Nonnull IdReference _subnet;
        private final @Nullable IdReference _publicIpAddress;
        private final boolean _primary;

        @JsonCreator
        public Properties(
                @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_PRIVATE_IP_ADDRESS) @Nullable Ip privateIpAddress,
                @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_SUBNET) @Nullable IdReference subnet,
                @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_PUBLIC_IP_ADDRESS) @Nullable IdReference publicIpAddress,
                @JsonProperty(AzureEntities.JSON_KEY_INTERFACE_PRIMARY) @Nullable Boolean primary
        ) {
            checkArgument(privateIpAddress != null, "privateIpAddress must be provided");
            checkArgument(subnet != null, "subnet must be provided");
            checkArgument(primary != null, "primary must be provided");
            _privateIpAddress = privateIpAddress;
            _subnet = subnet;
            _publicIpAddress = publicIpAddress;
            _primary = primary;
        }

        public Ip getPrivateIpAddress() {
            return _privateIpAddress;
        }
        public String getPublicIpAddressId() {
            return _publicIpAddress == null ? null : _publicIpAddress.getId();
        }
        public String getSubnetId(){
            return _subnet.getId();
        }
        public boolean isPrimary() {
            return _primary;
        }
    }
}
