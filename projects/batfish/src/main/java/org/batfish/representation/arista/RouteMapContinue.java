package org.batfish.representation.arista;

import java.io.Serializable;

public class RouteMapContinue implements Serializable {

  private final Integer _target;

  public RouteMapContinue(Integer target) {
    _target = target;
  }

  public Integer getTarget() {
    return _target;
  }
}
