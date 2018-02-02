package org.batfish.deltanet;

import javax.annotation.Nonnull;

public class HalfInterval implements Comparable<HalfInterval> {

  private long _rangeStart;

  private int _alphaIndex;

  public HalfInterval(long rangeStart, int alphaIndex) {
    this._rangeStart = rangeStart;
    this._alphaIndex = alphaIndex;
  }

  public long getRangeStart() {
    return _rangeStart;
  }

  public int getAlphaIndex() {
    return _alphaIndex;
  }

  public void setAlphaIndex(int alphaIndex) {
    this._alphaIndex = alphaIndex;
  }

  @Override
  public int compareTo(@Nonnull HalfInterval that) {
    if (this._rangeStart < that._rangeStart) {
      return -1;
    } else if (this._rangeStart > that._rangeStart) {
      return 1;
    }
    return 0;
  }
}
