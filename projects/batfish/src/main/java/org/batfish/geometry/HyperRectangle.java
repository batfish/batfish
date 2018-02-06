package org.batfish.geometry;

import java.util.BitSet;
import javax.annotation.Nullable;

class HyperRectangle {

  private long _x1;
  private long _x2;
  private int _alphaIndex;
  private BitSet _difference;

  HyperRectangle(long x1, long x2, int alphaIndex) {
    this._x1 = x1;
    this._x2 = x2;
    this._alphaIndex = alphaIndex;
    this._difference = new BitSet();
  }

  int getAlphaIndex() {
    return _alphaIndex;
  }

  void setAlphaIndex(int alphaIndex) {
    this._alphaIndex = alphaIndex;
  }

  long getX1() {
    return _x1;
  }

  long getX2() {
    return _x2;
  }

  BitSet getDifference() {
    return _difference;
  }

  @Nullable
  HyperRectangle overlap(HyperRectangle other) {
    if (other._x2 < _x1 || other._x1 > _x2) {
      return null;
    }
    long x1 = Math.max(_x1, other._x1);
    long x2 = Math.min(_x2, other._x2);
    return new HyperRectangle(x1, x2, -1);
  }

  boolean isSubsumedBy(HyperRectangle other) {
    return (other._x1 <= _x1) && (other._x2 >= _x2);
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    HyperRectangle that = (HyperRectangle) o;

    if (_x1 != that._x1) {
      return false;
    }
    if (_x2 != that._x2) {
      return false;
    }
    return _difference != null ? _difference.equals(that._difference) : that._difference == null;
  }

  @Override public String toString() {
    return "HyperRectangle{" + _x1 + "," + _x2 + '}';
  }

  @Override public int hashCode() {
    int result = (int) (_x1 ^ (_x1 >>> 32));
    result = 31 * result + (int) (_x2 ^ (_x2 >>> 32));
    result = 31 * result + (_difference != null ? _difference.hashCode() : 0);
    return result;
  }
}
