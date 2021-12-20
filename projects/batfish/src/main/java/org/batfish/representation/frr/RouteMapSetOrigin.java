package org.batfish.representation.frr;

import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** Clause of 'set origin' in route map. */
public class RouteMapSetOrigin implements RouteMapSet {

  private @Nonnull final OriginType _originType;

  public RouteMapSetOrigin(OriginType originType) {
    _originType = originType;
  }

  public @Nonnull OriginType getOriginType() {
    return _originType;
  }

  @Nonnull
  @Override
  public Stream<Statement> toStatements(Configuration c, FrrConfiguration vc, Warnings w) {
    return Stream.of(new SetOrigin(new LiteralOrigin(_originType, null)));
  }
}
