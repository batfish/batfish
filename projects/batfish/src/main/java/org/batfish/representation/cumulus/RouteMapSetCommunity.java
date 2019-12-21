package org.batfish.representation.cumulus;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.expr.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.statement.SetCommunity;
import org.batfish.datamodel.routing_policy.statement.Statement;

import java.util.stream.Collectors;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;

public class RouteMapSetCommunity implements RouteMapSet {
  private @Nonnull List<StandardCommunity> _communities;
  private boolean _isAdditive;

  public void set_isAdditive() {
    _isAdditive = true;
  }

  public boolean get_isAdditive() {
    return _isAdditive;
  }
  public RouteMapSetCommunity(Iterable<StandardCommunity> communities) {
    if (get_isAdditive()) {
      _communities.addAll(ImmutableList.copyOf(communities));
    } else {
      _communities = ImmutableList.copyOf(communities);
    }
  }

  @Nonnull
  @Override
  public Stream<Statement> toStatements(Configuration c, CumulusNcluConfiguration vc, Warnings w) {
    CommunitySetExpr communities = new LiteralCommunitySet(_communities);
    return Stream.of(new SetCommunity(communities));
  }

  public @Nonnull List<StandardCommunity> getCommunities() {
    return _communities;
  }

  public void setCommunities(List<StandardCommunity> communities) {
    ArrayList<StandardCommunity> commList =
            communities.stream().collect(Collectors.toCollection(ArrayList::new));
    if (get_isAdditive()) {
      commList.addAll(_communities);
    }
    _communities = commList;
  }
}
