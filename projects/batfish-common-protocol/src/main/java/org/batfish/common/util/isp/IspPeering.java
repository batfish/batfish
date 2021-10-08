package org.batfish.common.util.isp;

import java.util.Objects;
import javax.annotation.Nonnull;

/** Internal representation of inter-ISP peerings */
public class IspPeering {

  @Nonnull private final long _asn1;
  @Nonnull private final long _asn2;

  public IspPeering(long asn1, long asn2) {
    // canonicalize by using the lower number as asn1
    _asn1 = Math.min(asn1, asn2);
    _asn2 = Math.max(asn1, asn2);
  }

  public long getAsn1() {
    return _asn1;
  }

  public long getAsn2() {
    return _asn2;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IspPeering)) {
      return false;
    }
    IspPeering that = (IspPeering) o;
    return _asn1 == that._asn1 && _asn2 == that._asn2;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_asn1, _asn2);
  }
}
