package org.batfish.representation.cisco_asa;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;

/** Implementation of route-map match extcommunity. */
public final class RouteMapMatchExtcommunityLine extends RouteMapMatchLine {

  private final @Nonnull Set<String> _lists;

  public RouteMapMatchExtcommunityLine(@Nonnull Collection<String> lists) {
    checkArgument(!lists.isEmpty(), "At least one list required");
    _lists = ImmutableSet.copyOf(lists);
  }

  @Override
  public BooleanExpr toBooleanExpr(Configuration c, AsaConfiguration cc, Warnings w) {
    assert !_lists.isEmpty(); // unused warning
    // TODO: unimplemented.
    return BooleanExprs.FALSE;
  }

  @Override
  public <T> T accept(RouteMapMatchLineVisitor<T> visitor) {
    return visitor.visitRouteMapMatchExtcommunityLine(this);
  }
}
