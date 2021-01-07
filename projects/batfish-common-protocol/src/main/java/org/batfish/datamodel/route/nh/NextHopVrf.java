package org.batfish.datamodel.route.nh;

import com.google.common.base.MoreObjects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Indicates that destinations matching this route must be <em>re-resolved</em> in a different VRF
 */
public final class NextHopVrf implements NextHop {

  /** Create new next hop, pointing to a given VRF name */
  public static NextHopVrf of(String vrfName) {
    return new NextHopVrf(vrfName);
  }

  /** VRF in which to resolve the destination */
  @Nonnull
  public String getVrfName() {
    return _vrfName;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NextHopVrf)) {
      return false;
    }
    NextHopVrf that = (NextHopVrf) o;
    return _vrfName.equals(that._vrfName);
  }

  @Override
  public int hashCode() {
    return _vrfName.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(NextHopVrf.class).add("_vrfName", _vrfName).toString();
  }

  @Override
  public <T> T accept(NextHopVisitor<T> visitor) {
    return visitor.visitNextHopVrf(this);
  }

  @Nonnull private final String _vrfName;

  private NextHopVrf(String vrfName) {
    _vrfName = vrfName;
  }
}
