package org.batfish.symbolic.ainterpreter;

public class AbstractRib<T> {

  private T _bgpRib;

  private T _ospfRib;

  private T _staticRib;

  private T _connectedRib;

  private T _mainRib;

  public AbstractRib(T bgpRib, T ospfRib, T staticRib, T connectedRib, T mainRib) {
    this._bgpRib = bgpRib;
    this._ospfRib = ospfRib;
    this._staticRib = staticRib;
    this._connectedRib = connectedRib;
    this._mainRib = mainRib;
  }

  public T getBgpRib() {
    return _bgpRib;
  }

  public T getOspfRib() {
    return _ospfRib;
  }

  public T getStaticRib() {
    return _staticRib;
  }

  public T getConnectedRib() {
    return _connectedRib;
  }

  public T getMainRib() {
    return _mainRib;
  }
}
