package org.batfish.symbolic.ainterpreter;

import java.util.BitSet;

public class AbstractRib<T> {

  private T _underBgpRib;

  private T _overBgpRib;

  private T _underOspfRib;

  private T _overOspfRib;

  private T _staticRib;

  private T _connectedRib;

  private T _underMainRib;

  private T _overMainRib;

  private BitSet _aclIds;

  public AbstractRib(
      T underBgpRib,
      T overBgpRib,
      T underOspfRib,
      T overOspfRib,
      T staticRib,
      T connectedRib,
      T underMainRib,
      T overMainRib,
      BitSet acls) {
    this._underBgpRib = underBgpRib;
    this._overBgpRib = overBgpRib;
    this._underOspfRib = underOspfRib;
    this._overOspfRib = overOspfRib;
    this._staticRib = staticRib;
    this._connectedRib = connectedRib;
    this._underMainRib = underMainRib;
    this._overMainRib = overMainRib;
    this._aclIds = acls;
  }

  public T getUnderBgpRib() {
    return _underBgpRib;
  }

  public T getOverBgpRib() {
    return _overBgpRib;
  }

  public T getUnderOspfRib() {
    return _underOspfRib;
  }

  public T getOverOspfRib() {
    return _overOspfRib;
  }

  public T getStaticRib() {
    return _staticRib;
  }

  public T getConnectedRib() {
    return _connectedRib;
  }

  public T getUnderMainRib() {
    return _underMainRib;
  }

  public T getOverMainRib() {
    return _overMainRib;
  }

  public BitSet getAclIds() {
    return _aclIds;
  }
}
