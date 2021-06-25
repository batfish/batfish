package org.batfish.representation.cisco_xr;

import javax.annotation.ParametersAreNonnullByDefault;

/** XR datamodel component containing configuration for pop-based ingress tag rewriting. */
@ParametersAreNonnullByDefault
public class TagRewritePop implements TagRewritePolicy {
  public int getPopCount() {
    return _popCount;
  }

  public boolean getSymmetric() {
    return _symmetric;
  }

  public TagRewritePop(int popCount, boolean symmetric) {
    _popCount = popCount;
    _symmetric = symmetric;
  }

  private final int _popCount;
  private final boolean _symmetric;
}
