package org.batfish.representation.f5_bigip;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.statement.SetCommunity;
import org.batfish.datamodel.routing_policy.statement.Statement;

@ParametersAreNonnullByDefault
public final class RouteMapSetCommunity implements RouteMapSet {

  private static final long serialVersionUID = 1L;

  private final @Nonnull Set<Long> _communities;

  public RouteMapSetCommunity(Collection<Long> communities) {
    _communities = ImmutableSet.copyOf(communities);
  }

  public @Nonnull Set<Long> getCommunities() {
    return _communities;
  }

  @Override
  public @Nonnull Stream<Statement> toStatements(
      Configuration c, F5BigipConfiguration vc, Warnings w) {
    return Stream.of(new SetCommunity(new LiteralCommunitySet(_communities)));
  }
}
