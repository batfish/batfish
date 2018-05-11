package org.batfish.symbolic.interpreter;

public class AbstractRib<T> {

  private T _bgpRib;

  private T _ospfRib;

  private T _connectedRib;

  private T _ribEntry;

  public AbstractRib(T bgpRib, T ospfRib, T connectedRib, T ribEntry) {
    this._bgpRib = bgpRib;
    this._ospfRib = ospfRib;
    this._connectedRib = connectedRib;
    this._ribEntry = ribEntry;
  }

  public T getBgpRib() {
    return _bgpRib;
  }

  public T getOspfRib() {
    return _ospfRib;
  }

  public T getConnectedRib() {
    return _connectedRib;
  }

  public T getRibEntry() {
    return _ribEntry;
  }
}
