package org.batfish.datamodel.bgp;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/**
 * A tunnel attribute that can be applied to BGP routes by routing policies. Currently assumed to be
 * type IP-IP as that is the only supported type.
 */
public class TunnelEncapsulationAttribute implements Serializable {
  private static final String PROP_REMOTE_ENDPOINT = "remoteEndpoint";
  private final @Nonnull Ip _remoteEndpoint;

  public TunnelEncapsulationAttribute(Ip remoteEndpoint) {
    _remoteEndpoint = remoteEndpoint;
  }

  @JsonCreator
  private static TunnelEncapsulationAttribute create(
      @JsonProperty(PROP_REMOTE_ENDPOINT) @Nullable Ip remoteEndpoint) {
    checkNotNull(remoteEndpoint, "%s cannot be null", PROP_REMOTE_ENDPOINT);
    return new TunnelEncapsulationAttribute(remoteEndpoint);
  }

  @JsonProperty(PROP_REMOTE_ENDPOINT)
  public @Nonnull Ip getRemoteEndpoint() {
    return _remoteEndpoint;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof TunnelEncapsulationAttribute)) {
      return false;
    }
    TunnelEncapsulationAttribute o = (TunnelEncapsulationAttribute) obj;
    return _remoteEndpoint.equals(o.getRemoteEndpoint());
  }

  @Override
  public int hashCode() {
    return _remoteEndpoint.hashCode();
  }

  @Override
  public String toString() {
    return String.format("Tunnel type: ipip, remote endpoint: %s", _remoteEndpoint);
  }
}
