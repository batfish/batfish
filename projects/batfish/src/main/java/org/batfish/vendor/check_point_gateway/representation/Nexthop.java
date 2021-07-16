package org.batfish.vendor.check_point_gateway.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Interface for Check Point gateway route nexthops. */
public class Nexthop implements Serializable {
  public Nexthop(NexthopTarget nexthopTarget) {
    _nexthopTarget = nexthopTarget;
  }

  public @Nonnull NexthopTarget getNexthopTarget() {
    return _nexthopTarget;
  }

  public @Nullable Integer getPriority() {
    return _priority;
  }

  public void setPriority(Integer priority) {
    _priority = priority;
  }

  @Nonnull private final NexthopTarget _nexthopTarget;
  @Nullable private Integer _priority;
}
