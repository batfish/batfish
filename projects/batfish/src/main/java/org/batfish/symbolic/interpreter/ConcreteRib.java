package org.batfish.symbolic.interpreter;

import java.util.Map;
import org.batfish.datamodel.Prefix;

public class ConcreteRib<T> {

  private Map<Prefix, T> _bgpRib;

  private Map<Prefix, T> _ospfRib;

  private Map<Prefix, T> _staticRib;

  private Map<Prefix, T> _connectedRib;

  private Map<Prefix, T> _mainRib;

  public ConcreteRib(Map<Prefix, T> bgpRib, Map<Prefix, T> ospfRib, Map<Prefix, T> staticRib, Map<Prefix, T> connectedRib, Map<Prefix, T> mainRib) {
    this._bgpRib = bgpRib;
    this._ospfRib = ospfRib;
    this._staticRib = staticRib;
    this._connectedRib = connectedRib;
    this._mainRib = mainRib;
  }

  public Map<Prefix, T> getBgpRib() {
    return _bgpRib;
  }

  public Map<Prefix, T> getOspfRib() {
    return _ospfRib;
  }

  public Map<Prefix, T> getStaticRib() {
    return _staticRib;
  }

  public Map<Prefix, T> getConnectedRib() {
    return _connectedRib;
  }

  public Map<Prefix, T> getMainRib() {
    return _mainRib;
  }
}