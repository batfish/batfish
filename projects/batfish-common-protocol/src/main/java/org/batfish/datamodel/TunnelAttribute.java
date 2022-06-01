package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A tunnel attribute that can be applied to BGP routes by routing policies. Currently assumed to be
 * type IP-IP as that is the only supported type.
 */
public class TunnelAttribute implements Serializable {
  private static final String PROP_REMOTE_ENDPOINT = "remoteEndpoint";
  private final @Nonnull Ip _remoteEndpoint;

  public TunnelAttribute(Ip remoteEndpoint) {
    _remoteEndpoint = remoteEndpoint;
  }

  @JsonCreator
  private static TunnelAttribute create(
      @Nullable @JsonProperty(PROP_REMOTE_ENDPOINT) Ip remoteEndpoint) {
    checkNotNull(remoteEndpoint, "%s cannot be null", PROP_REMOTE_ENDPOINT);
    return new TunnelAttribute(remoteEndpoint);
  }

  @JsonProperty(PROP_REMOTE_ENDPOINT)
  public @Nonnull Ip getRemoteEndpoint() {
    return _remoteEndpoint;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof TunnelAttribute)) {
      return false;
    }
    TunnelAttribute o = (TunnelAttribute) obj;
    return _remoteEndpoint.equals(o.getRemoteEndpoint());
  }

  @Override
  public int hashCode() {
    return _remoteEndpoint.hashCode();
  }
}
