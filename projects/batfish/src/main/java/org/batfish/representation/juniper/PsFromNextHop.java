package org.batfish.representation.juniper;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.IpPrefix;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NextHopIp;

/** Represents a "from next-hop" line in a {@link PsTerm} */
public class PsFromNextHop extends PsFrom {
  public static class Hop implements Serializable {
    private final @Nullable Ip _ip4;
    private final @Nullable Ip6 _ip6;

    private Hop(@Nullable Ip ip4, @Nullable Ip6 ip6) {
      _ip4 = ip4;
      _ip6 = ip6;
    }

    public static Hop of(Ip ip) {
      return new Hop(ip, null);
    }

    public static Hop of(Ip6 ip) {
      return new Hop(null, ip);
    }

    @Override
    public final boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Hop hop)) {
        return false;
      }
      return Objects.equals(_ip4, hop._ip4) && Objects.equals(_ip6, hop._ip6);
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

  private final Hop _hop;

  public PsFromNextHop(Hop hop) {
    _hop = hop;
  }

  public Hop getNextHop() {
    return _hop;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    if (_hop._ip4 != null) {
      return new MatchPrefixSet(
          new IpPrefix(NextHopIp.instance(), new LiteralInt(Prefix.MAX_PREFIX_LENGTH)),
          new ExplicitPrefixSet(new PrefixSpace(PrefixRange.fromPrefix(_hop._ip4.toPrefix()))));
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
    if (!(o instanceof PsFromNextHop that)) {
      return false;
    }
    return _hop.equals(that._hop);
  }

  @Override
  public int hashCode() {
    return _hop.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("hop", _hop).toString();
  }
}
