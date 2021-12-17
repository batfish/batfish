package org.batfish.vendor.sonic.representation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents device metadata: https://github.com/Azure/SONiC/wiki/Configuration#device-metadata */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceMetadata implements Serializable {

  private static final String PROP_HOSTNAME = "hostname";

  private @Nullable final String _hostname;

  public @Nonnull Optional<String> getHostname() {
    return Optional.ofNullable(_hostname);
  }

  public DeviceMetadata(@Nullable String hostname) {
    _hostname = hostname;
  }

  @JsonCreator
  private static DeviceMetadata create(@Nullable @JsonProperty(PROP_HOSTNAME) String hostname) {
    return new DeviceMetadata(hostname);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DeviceMetadata)) {
      return false;
    }
    DeviceMetadata that = (DeviceMetadata) o;
    return Objects.equals(_hostname, that._hostname);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).omitNullValues().add("hostname", _hostname).toString();
  }
}
