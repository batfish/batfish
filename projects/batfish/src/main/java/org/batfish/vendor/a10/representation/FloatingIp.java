package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import javax.annotation.Nullable;

/** An ACOSv2 {@code floating-ip}. */
public final class FloatingIp implements Serializable {

  public @Nullable Integer getHaGroup() {
    return _haGroup;
  }

  public void setHaGroup(@Nullable Integer haGroup) {
    _haGroup = haGroup;
  }

  private @Nullable Integer _haGroup;
}
