package org.batfish.vendor.sonic.representation;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.BatfishObjectMapper;

/** Represents PORT object: https://github.com/Azure/SONiC/wiki/Configuration#port */
@ParametersAreNonnullByDefault
public class PortDb implements ConfigDbObject {

  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_MTU = "mtu";
  private static final String PROP_ADMIN_STATUS = "admin_status";

  @ParametersAreNonnullByDefault
  public static class Port implements Serializable {
    private @Nonnull final Map<String, String> _properties;

    public Optional<Boolean> getAdminStatus() {
      return Optional.ofNullable(_properties.get(PROP_ADMIN_STATUS)).map("up"::equals);
    }

    public Optional<String> getDescription() {
      return Optional.ofNullable(_properties.get(PROP_DESCRIPTION));
    }

    public Optional<Integer> getMtu() {
      return Optional.ofNullable(_properties.get(PROP_MTU)).map(Integer::parseInt);
    }

    @JsonCreator
    private static Port create(@Nullable Map<String, String> properties) {
      return new Port(firstNonNull(properties, ImmutableMap.of()));
    }

    public Port(Map<String, String> properties) {
      _properties = properties;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Port)) {
        return false;
      }
      Port that = (Port) o;
      return Objects.equals(_properties, that._properties);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_properties);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).add("properties", _properties).toString();
    }
  }

  private @Nonnull final Map<String, Port> _ports;

  public PortDb(Map<String, Port> ports) {
    _ports = ImmutableMap.copyOf(ports);
  }

  @JsonCreator
  private static PortDb create(Map<String, JsonNode> ports) throws JsonProcessingException {
    Map<String, Port> portMap = new HashMap<>();
    for (String port : ports.keySet()) {
      portMap.put(
          port, BatfishObjectMapper.ignoreUnknownMapper().treeToValue(ports.get(port), Port.class));
    }
    return new PortDb(portMap);
  }

  @Nonnull
  public Map<String, Port> getPorts() {
    return _ports;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PortDb)) {
      return false;
    }
    PortDb that = (PortDb) o;
    return Objects.equals(_ports, that._ports);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ports);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("interfaces", _ports).toString();
  }
}
