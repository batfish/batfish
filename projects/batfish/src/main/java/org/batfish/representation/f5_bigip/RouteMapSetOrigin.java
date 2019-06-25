package org.batfish.representation.f5_bigip;

import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** Route-map transformation that replaces the origin attribute of the route */
@ParametersAreNonnullByDefault
public class RouteMapSetOrigin implements RouteMapSet {

  private final @Nonnull OriginType _origin;

  public RouteMapSetOrigin(OriginType origin) {
    _origin = origin;
  }

  public @Nonnull OriginType getOrigin() {
    return _origin;
  }

  @Override
  public @Nonnull Stream<Statement> toStatements(
      Configuration c, F5BigipConfiguration vc, Warnings w) {
    return Stream.of(new SetOrigin(new LiteralOrigin(_origin, null)));
  }
}
