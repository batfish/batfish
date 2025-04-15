package org.batfish.representation.juniper;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.MatchPeerAddress;

/** Represents a "from neighbor" line in a {@link PsTerm} */
public final class PsFromNeighbor extends PsFrom {
  public static final class Neighbor implements Serializable {
    private final @Nullable Ip _ip4;
    private final @Nullable Ip6 _ip6;

    private Neighbor(@Nullable Ip ip4, @Nullable Ip6 ip6) {
      _ip4 = ip4;
      _ip6 = ip6;
    }

    public static Neighbor of(Ip ip) {
      return new Neighbor(ip, null);
    }

    public static Neighbor of(Ip6 ip) {
      return new Neighbor(null, ip);
    }

    @Override
    public final boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Neighbor neighbor)) {
        return false;
      }
      return Objects.equals(_ip4, neighbor._ip4) && Objects.equals(_ip6, neighbor._ip6);
    }

    @Override
    public int hashCode() {
      int result = Objects.hashCode(_ip4);
      result = 31 * result + Objects.hashCode(_ip6);
      return result;
    }

    @Override
    public String toString() {
      if (_ip4 != null) {
        return _ip4.toString();
      }
      assert _ip6 != null;
      return _ip6.toString();
    }
  }

  private final Neighbor _neighbor;

  public PsFromNeighbor(Neighbor neighbor) {
    _neighbor = neighbor;
  }

  public Neighbor getNextHop() {
    return _neighbor;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    if (_neighbor._ip4 != null) {
      return new MatchPeerAddress(_neighbor._ip4);
    } else {
      // V6
      return BooleanExprs.FALSE;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PsFromNeighbor that)) {
      return false;
    }
    return _neighbor.equals(that._neighbor);
  }

  @Override
  public int hashCode() {
    return _neighbor.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("hop", _neighbor).toString();
  }
}
