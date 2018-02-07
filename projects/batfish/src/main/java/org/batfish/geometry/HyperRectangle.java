package org.batfish.geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class HyperRectangle implements Comparable<HyperRectangle> {

  private long _x1;
  private long _x2;
  private int _alphaIndex;

  HyperRectangle(long x1, long x2, int alphaIndex) {
    this._x1 = x1;
    this._x2 = x2;
    this._alphaIndex = alphaIndex;
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

  void setX1(long x1) {
    this._x1 = x1;
  }

  void setX2(long x2) {
    this._x2 = x2;
  }

  @Nullable
  HyperRectangle overlap(HyperRectangle other) {
    if (other._x2 <= _x1 || other._x1 >= _x2) {
      return null;
    }
    long x1 = Math.max(_x1, other._x1);
    long x2 = Math.min(_x2, other._x2);
    return new HyperRectangle(x1, x2, -1);
  }

  /*
   * Assume that they already overlap
   * That is, the other can not go outside this shape's bounds
   */
  Collection<HyperRectangle> divide(HyperRectangle other) {
    // in each dimension we would do this:
    List<HyperRectangle> newRects = new ArrayList<>();
    if (this.equals(other)) {
      return new ArrayList<>();
    }
    if (_x1 != other._x1) {
      HyperRectangle r = new HyperRectangle(_x1, other._x1, -1);
      newRects.add(r);
    }
    newRects.add(other);
    if (_x2 != other._x2) {
      HyperRectangle r = new HyperRectangle(other._x2, _x2, -1);
      newRects.add(r);
    }
    return newRects;
  }

  boolean isSubsumedBy(HyperRectangle other) {
    return (other._x1 <= _x1) && (other._x2 >= _x2);
  }

  @Override public String toString() {
    return "HyperRectangle{" + _x1 + "," + _x2 + " with " + _alphaIndex + '}';
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HyperRectangle that = (HyperRectangle) o;
    return _x1 == that._x1 && _x2 == that._x2;
  }

  @Override public int hashCode() {
    int result = (int) (_x1 ^ (_x1 >>> 32));
    result = 31 * result + (int) (_x2 ^ (_x2 >>> 32));
    return result;
  }

  @Override public int compareTo(@Nonnull HyperRectangle that) {
    if (this._x1 < that._x1) {
      return -1;
    } else if (this._x1 > that._x1) {
      return 1;
    }
    if (this._x2 < that._x2) {
      return -1;
    } else if (this._x2 > that._x2) {
      return 1;
    }
    return 0;
  }
}
