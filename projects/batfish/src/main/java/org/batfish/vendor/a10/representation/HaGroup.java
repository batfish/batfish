package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import javax.annotation.Nullable;

/** An ACOSv2 {@code ha group}. */
public final class HaGroup implements Serializable {

  public @Nullable Integer getPriority() {
    return _priority;
  }

  public void setPriority(@Nullable Integer priority) {
    _priority = priority;
  }

  private @Nullable Integer _priority;
}
