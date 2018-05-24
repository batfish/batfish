package org.batfish.symbolic.ainterpreter;

import java.util.BitSet;

public class AbstractRib<T> {

  private T _bgpRib;

  private T _ospfRib;

  private T _staticRib;

  private T _connectedRib;

  private T _mainRib;

  private BitSet _aclIds;

  public AbstractRib(T bgpRib, T ospfRib, T staticRib, T connectedRib, T mainRib, BitSet acls) {
    this._bgpRib = bgpRib;
    this._ospfRib = ospfRib;
    this._staticRib = staticRib;
    this._connectedRib = connectedRib;
    this._mainRib = mainRib;
    this._aclIds = acls;
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

  public BitSet getAclIds() {
    return _aclIds;
  }
}
