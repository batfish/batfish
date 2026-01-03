package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.routing_policy.Environment;

@ParametersAreNonnullByDefault
public final class IpNextHop extends NextHopExpr {
  private static final String PROP_IPS = "ips";

  private final @Nonnull List<Ip> _ips;

  @JsonCreator
  private static IpNextHop jsonCreator(@JsonProperty(PROP_IPS) @Nullable List<Ip> ips) {
    return new IpNextHop(firstNonNull(ips, ImmutableList.of()));
  }

  public IpNextHop(List<Ip> ips) {
    _ips = ips;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof IpNextHop)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    IpNextHop other = (IpNextHop) obj;
    return _ips.equals(other._ips);
  }

  @JsonProperty(PROP_IPS)
  public @Nonnull List<Ip> getIps() {
    return _ips;
  }

  @Override
  public @Nonnull NextHop evaluate(Environment env) {
    if (_ips.size() == 1) {
      return NextHopIp.of(_ips.get(0));
    } else {
      throw new BatfishException("Do not currently support setting more than 1 next-hop-ip");
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _ips.hashCode();
    return result;
  }
}
