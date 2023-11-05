package org.batfish.representation.frr;

import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.statement.SetWeight;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** Clause of set weight in route map. */
public class RouteMapSetWeight implements RouteMapSet {

  private int _weight;

  public RouteMapSetWeight(int weight) {
    _weight = weight;
  }

  public int getWeight() {
    return _weight;
  }

  @Override
  public @Nonnull Stream<Statement> toStatements(Configuration c, FrrConfiguration vc, Warnings w) {
    return Stream.of(new SetWeight(new LiteralInt(_weight)));
  }
}
