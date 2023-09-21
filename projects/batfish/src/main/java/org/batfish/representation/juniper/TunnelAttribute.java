package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Represents a tunnel-attribute defined under policy-options */
public class TunnelAttribute implements Serializable {
  public enum Type {
    IPIP,
  }

  private @Nullable Ip _remoteEndPoint;
  private @Nullable Type _type;

  public @Nullable Ip getRemoteEndPoint() {
    return _remoteEndPoint;
  }

  public @Nullable Type getType() {
    return _type;
  }

  public void setRemoteEndPoint(@Nullable Ip remoteEndPoint) {
    _remoteEndPoint = remoteEndPoint;
  }

  public void setType(@Nullable Type type) {
    _type = type;
  }
}
