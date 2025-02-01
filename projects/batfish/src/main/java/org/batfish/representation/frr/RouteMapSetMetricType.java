package org.batfish.representation.frr;

import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** A {@link RouteMapSet} that sets the metric-type for an OSPF route. */
public final class RouteMapSetMetricType implements RouteMapSet {

  public RouteMapSetMetricType(RouteMapMetricType metricType) {
    _metricType = metricType;
  }

  public @Nonnull RouteMapMetricType getMetricType() {
    return _metricType;
  }

  @Override
  public @Nonnull Stream<Statement> toStatements(Configuration c, FrrConfiguration vc, Warnings w) {
    return switch (_metricType) {
      case TYPE_1 -> Stream.of(new SetOspfMetricType(OspfMetricType.E1));
      case TYPE_2 -> Stream.of(new SetOspfMetricType(OspfMetricType.E2));
    };
  }

  private final @Nonnull RouteMapMetricType _metricType;
}
