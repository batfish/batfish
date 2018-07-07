package org.batfish.dataplane.topology;

import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpPeerConfigId;

/** Directional, reversible BGP edge pointing to two {@link BgpPeerConfigId}. */
public class BgpEdgeId implements Comparable<BgpEdgeId> {

  private BgpPeerConfigId _src;
  private BgpPeerConfigId _dst;

  public BgpEdgeId(BgpPeerConfigId src, BgpPeerConfigId dst) {
    _src = src;
    _dst = dst;
  }

  public BgpPeerConfigId src() {
    return _src;
  }

  public BgpPeerConfigId dst() {
    return _dst;
  }

  public BgpEdgeId reverse() {
    return new BgpEdgeId(_dst, _src);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BgpEdgeId bgpEdgeId = (BgpEdgeId) o;
    return Objects.equals(_src, bgpEdgeId._src) && Objects.equals(_dst, bgpEdgeId._dst);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_src, _dst);
  }

  @Override
  public int compareTo(@Nonnull BgpEdgeId o) {
    return Comparator.comparing(BgpEdgeId::src).thenComparing(BgpEdgeId::dst).compare(this, o);
  }
}
