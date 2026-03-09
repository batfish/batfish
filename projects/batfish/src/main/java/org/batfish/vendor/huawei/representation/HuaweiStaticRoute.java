package org.batfish.vendor.huawei.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

/** Static route configuration. */
public class HuaweiStaticRoute implements Serializable {

  private static final long serialVersionUID = 1L;

  private final @Nonnull Ip _network;
  private final @Nonnull Ip _mask;
  private final @Nonnull Ip _nextHop;

  public HuaweiStaticRoute(@Nonnull Ip network, @Nonnull Ip mask, @Nonnull Ip nextHop) {
    _network = network;
    _mask = mask;
    _nextHop = nextHop;
  }

  public @Nonnull Ip getNetwork() {
    return _network;
  }

  public @Nonnull Ip getMask() {
    return _mask;
  }

  public @Nonnull Ip getNextHop() {
    return _nextHop;
  }
}
