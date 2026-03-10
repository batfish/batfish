package org.batfish.representation.frr;

import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.statement.SetTag;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** A {@link RouteMapSet} that sets a tag value on a route */
public class RouteMapSetTag implements RouteMapSet {
  private final long _tag;

  public RouteMapSetTag(long tag) {
    _tag = tag;
  }

  public long getTag() {
    return _tag;
  }

  @Override
  public @Nonnull Stream<Statement> toStatements(Configuration c, FrrConfiguration vc, Warnings w) {
    return Stream.of(new SetTag(new LiteralLong(_tag)));
  }
}
