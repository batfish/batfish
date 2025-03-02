package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.IpProtocol;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents ContainerInstance azure services.
 * (Part of {@link ContainerGroup}).
 * <a href="https://learn.microsoft.com/en-us/azure/templates/microsoft.containerinstance/containergroups?pivots=deployment-language-arm-template">Resource link</a>.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContainerInstance implements Serializable {

    private final @Nonnull String _name;
    private final @Nonnull Properties _properties;

    public ContainerInstance(
             @JsonProperty(AzureEntities.JSON_KEY_NAME) @Nullable String name,
             @JsonProperty(AzureEntities.JSON_KEY_PROPERTIES) @Nullable Properties properties
    ) {
        checkArgument(name != null, "resource name must be provided");
        checkArgument(properties != null, "properties must be provided");
        _name = name;
        _properties = properties;
    }

    public String getName() {
        return _name;
    }
    public Properties getProperties() {
        return _properties;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties implements Serializable {

        private final @Nonnull Set<Port> _ports;

        public Properties(
                @JsonProperty(AzureEntities.JSON_KEY_CONTAINER_INSTANCE_PORTS) @Nullable Set<Port> ports
        ) {
            if(ports == null) ports = new HashSet<>();
            _ports = ports;
        }

        public Set<Port> getPorts() {
            return _ports;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Port implements Serializable {
        private final @Nonnull IpProtocol _protocol;
        private final @Nonnull int _port;

        public Port(
                @JsonProperty(AzureEntities.JSON_KEY_CONTAINER_INSTANCE_PORT_PROTOCOL) @Nullable IpProtocol protocol,
                @JsonProperty(AzureEntities.JSON_KEY_CONTAINER_INSTANCE_PORT_NUMBER) @Nullable Integer port
        ) {
            checkArgument(protocol != null, "protocol must be provided");
            checkArgument(port != null, "port must be provided");
            _protocol = protocol;
            _port = port;
        }

        public IpProtocol getProtocol() {
            return _protocol;
        }
        public int getPort() {
            return _port;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Port other) {
                return (other.getPort() == getPort()
                        && other.getProtocol().equals(getProtocol()));
            }
            return false;
        }

        @Override
        public int hashCode() {
            return _protocol.hashCode() + _port;
        }
    }
}
