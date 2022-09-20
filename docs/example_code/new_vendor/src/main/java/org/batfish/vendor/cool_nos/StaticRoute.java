package org.batfish.vendor.cool_nos;

import java.io.Serializable;
import javax.annotation.Nonnull;

public final class StaticRoute implements Serializable {

  public @Nonnull NextHop getNextHop() {
    return _nextHop;
  }

  public void setNextHop(NextHop nextHop) {
    _nextHop = nextHop;
  }

  private NextHop _nextHop;
}
