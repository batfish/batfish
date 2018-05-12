package org.batfish.symbolic.interpreter;

import net.sf.javabdd.BDD;

public class AbstractRib<T> {

  private T _bgpRib;

  private T _ospfRib;

  private T _staticRib;

  private T _connectedRib;

  private T _ribEntry;

  private BDD _headerspace;

  public AbstractRib(T bgpRib, T ospfRib, T staticRib, T connectedRib, T ribEntry, BDD h) {
    this._bgpRib = bgpRib;
    this._ospfRib = ospfRib;
    this._staticRib = staticRib;
    this._connectedRib = connectedRib;
    this._ribEntry = ribEntry;
    this._headerspace = h;
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

  public T getRibEntry() {
    return _ribEntry;
  }

  public BDD getHeaderspace() {
    return _headerspace;
  }
}
