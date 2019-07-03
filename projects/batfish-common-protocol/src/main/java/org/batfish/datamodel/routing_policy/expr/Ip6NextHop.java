package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.routing_policy.Environment;

@ParametersAreNonnullByDefault
public final class Ip6NextHop extends NextHopExpr {
  private static final String PROP_IPS = "ips";

  @Nonnull private final List<Ip6> _ips;

  @JsonCreator
  private static Ip6NextHop jsonCreator(@Nullable @JsonProperty(PROP_IPS) List<Ip6> ips) {
    return new Ip6NextHop(firstNonNull(ips, ImmutableList.of()));
  }

  public Ip6NextHop(List<Ip6> ips) {
    _ips = ips;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof Ip6NextHop)) {
      return false;
    }
    Ip6NextHop other = (Ip6NextHop) obj;
    return _ips.equals(other._ips);
  }

  @JsonProperty(PROP_IPS)
  @Nonnull
  public List<Ip6> getIps() {
    return _ips;
  }

  @Override
  public Ip getNextHopIp(Environment environment) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _ips.hashCode();
    return result;
  }
}
