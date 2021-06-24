package org.batfish.representation.cisco_xr;

import javax.annotation.ParametersAreNonnullByDefault;

/** XR datamodel component containing configuration for pop-based ingress tag rewriting. */
@ParametersAreNonnullByDefault
public class TagRewritePop implements TagRewritePolicy {
  public int getCount() {
    return _count;
  }

  public boolean getSymmetric() {
    return _symmetric;
  }

  public TagRewritePop(int count, boolean symmetric) {
    _count = count;
    _symmetric = symmetric;
  }

  private final int _count;
  private final boolean _symmetric;
}
