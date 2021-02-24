package org.batfish.representation.cumulus;

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

  @Nonnull
  @Override
  public Stream<Statement> toStatements(
      Configuration c, CumulusConcatenatedConfiguration vc, Warnings w) {
    switch (_metricType) {
      case TYPE_1:
        return Stream.of(new SetOspfMetricType(OspfMetricType.E1));

      case TYPE_2:
        return Stream.of(new SetOspfMetricType(OspfMetricType.E2));

      default:
        // should not happen
        return Stream.empty();
    }
  }

  private final @Nonnull RouteMapMetricType _metricType;
}
