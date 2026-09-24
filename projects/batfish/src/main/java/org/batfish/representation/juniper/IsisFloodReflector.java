package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;

public final class IsisFloodReflector implements Serializable {

  public enum Role {
    CLIENT,
    REFLECTOR
  }

  private @Nullable Long _clusterId;
  private @Nullable Role _role;

  public @Nullable Long getClusterId() {
    return _clusterId;
  }

  public @Nullable Role getRole() {
    return _role;
  }

  public void setClusterId(long clusterId) {
    _clusterId = clusterId;
  }

  public void setRole(Role role) {
    _role = role;
  }
}
