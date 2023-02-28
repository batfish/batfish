package org.batfish.vendor.cool_nos;

import java.io.Serializable;
import javax.annotation.Nonnull;

public final class StaticRoute implements Serializable {

  public StaticRoute() {
    _enable = true;
  }

  public boolean getEnable() {
    return _enable;
  }

  public void setEnable(boolean enable) {
    _enable = enable;
  }

  public @Nonnull NextHop getNextHop() {
    return _nextHop;
  }

  public void setNextHop(NextHop nextHop) {
    _nextHop = nextHop;
  }

  private boolean _enable;
  private NextHop _nextHop;
}
