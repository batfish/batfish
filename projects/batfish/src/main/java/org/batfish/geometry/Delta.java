package org.batfish.geometry;

public class Delta {

  private HyperRectangle _old;

  private HyperRectangle _new;

  public Delta(HyperRectangle o, HyperRectangle n) {
    this._old = o;
    this._new = n;
  }

  public HyperRectangle getOld() {
    return _old;
  }

  public HyperRectangle getNew() {
    return _new;
  }
}
