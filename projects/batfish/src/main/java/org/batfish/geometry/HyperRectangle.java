package org.batfish.geometry;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/*
 * High-dimensional hyperrectangle.
 * For a 2D version of the rectangle, the bounds will be
 * (xlow, xhigh, ylow, yhigh)
 */
public class HyperRectangle implements Comparable<HyperRectangle> {

  private long[] _bounds;
  private int _alphaIndex;

  HyperRectangle(long[] bounds) {
    this._bounds = bounds;
    this._alphaIndex = -1;
  }

  HyperRectangle(HyperRectangle other) {
    this._bounds = other._bounds.clone();
    this._alphaIndex = other._alphaIndex;
  }

  int getAlphaIndex() {
    return _alphaIndex;
  }

  void setAlphaIndex(int alphaIndex) {
    this._alphaIndex = alphaIndex;
  }

  long[] getBounds() {
    return _bounds;
  }

  void setBounds(long[] bounds) {
    this._bounds = bounds;
  }

  @Nullable
  HyperRectangle overlap(HyperRectangle other) {
    long[] bounds = new long[_bounds.length];
    for (int i = 0; i < _bounds.length; i += 2) {
      long x1 = _bounds[i];
      long x2 = _bounds[i + 1];
      long ox1 = other._bounds[i];
      long ox2 = other._bounds[i + 1];
      if (ox2 <= x1 || ox1 >= x2) {
        return null;
      }
      bounds[i] = Math.max(x1, ox1);
      bounds[i + 1] = Math.min(x2, ox2);
    }
    return new HyperRectangle(bounds);
  }

  private void divideRec(
      HyperRectangle other, int i, long[] boundsSoFar, Collection<HyperRectangle> added) {

    if (i >= boundsSoFar.length) {
      HyperRectangle r = new HyperRectangle(boundsSoFar.clone());
      added.add(r);
      return;
    }

    long x1 = _bounds[i];
    long x2 = _bounds[i + 1];
    long ox1 = other._bounds[i];
    long ox2 = other._bounds[i + 1];

    if (x1 != ox1) {
      boundsSoFar[i] = x1;
      boundsSoFar[i + 1] = ox1;
      divideRec(other, i + 2, boundsSoFar, added);
    }

    boundsSoFar[i] = ox1;
    boundsSoFar[i + 1] = ox2;
    divideRec(other, i + 2, boundsSoFar, added);

    if (x2 != ox2) {
      boundsSoFar[i] = ox2;
      boundsSoFar[i + 1] = x2;
      divideRec(other, i + 2, boundsSoFar, added);
    }
  }

  /*
   * Assume that they already overlap
   * That is, the other can not go outside this shape's bounds
   */
  @Nullable
  Collection<HyperRectangle> divide(HyperRectangle other) {
    // in each dimension we would do this:
    if (this.equals(other)) {
      return null;
    }
    List<HyperRectangle> newRects = new ArrayList<>();
    long[] boundsSoFar = new long[_bounds.length];
    divideRec(other, 0, boundsSoFar, newRects);
    return newRects;
  }

  boolean isSubsumedBy(HyperRectangle other) {
    for (int i = 0; i < _bounds.length; i += 2) {
      long x1 = _bounds[i];
      long x2 = _bounds[i + 1];
      long ox1 = other._bounds[i];
      long ox2 = other._bounds[i + 1];
      boolean subsumed = (ox1 <= x1 && ox2 >= x2);
      if (!subsumed) {
        return false;
      }
    }
    return true;
  }

  BigInteger volume() {
    BigInteger acc = BigInteger.ONE;
    for (int i = 0; i < _bounds.length; i += 2) {
      BigInteger b1 = BigInteger.valueOf(_bounds[0]);
      BigInteger b2 = BigInteger.valueOf(_bounds[1]);
      acc = acc.multiply(b2.subtract(b1));
    }
    return acc;
  }

  @Override
  public String toString() {
    String s = "HyperRectangle(";
    for (int i = 0; i < _bounds.length; i++) {
      s += _bounds[i];
      if (i != _bounds.length - 1) {
        s += ",";
      }
    }
    s += " with " + _alphaIndex + ")";
    return s;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HyperRectangle that = (HyperRectangle) o;
    return Arrays.equals(_bounds, that._bounds);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(_bounds);
  }

  @Override
  public int compareTo(@Nonnull HyperRectangle that) {
    for (int i = 0; i < _bounds.length; i++) {
      long cmp = _bounds[i] - that._bounds[i];
      if (cmp < 0) {
        return -1;
      }
      if (cmp > 0) {
        return 1;
      }
    }
    return 0;
  }
}
