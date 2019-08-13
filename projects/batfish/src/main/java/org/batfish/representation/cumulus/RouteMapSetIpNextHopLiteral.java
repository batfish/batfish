package org.batfish.representation.cumulus;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.expr.IpNextHop;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.Statement;

public final class RouteMapSetIpNextHopLiteral implements RouteMapSet {

  private final @Nonnull List<Ip> _nextHops;

  public RouteMapSetIpNextHopLiteral(Iterable<Ip> nextHops) {
    _nextHops = ImmutableList.copyOf(nextHops);
  }

  public @Nonnull List<Ip> getNextHops() {
    return _nextHops;
  }

  @Nonnull
  @Override
  public Stream<Statement> toStatements(Configuration c, CumulusNcluConfiguration vc, Warnings w) {
    if (_nextHops.size() > 1) {
      // Applicable to PBR only (not routing)
      return Stream.empty();
    }
    assert !_nextHops.isEmpty();
    return Stream.of(new SetNextHop(new IpNextHop(_nextHops), false));
  }
}
