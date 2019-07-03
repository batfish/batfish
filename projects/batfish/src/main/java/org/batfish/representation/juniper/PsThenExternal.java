package org.batfish.representation.juniper;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** A {@link Statement} that sets the OSPF external metric type on a route. */
public final class PsThenExternal extends PsThen {

  private final OspfMetricType _type;

  public PsThenExternal(OspfMetricType type) {
    _type = type;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    statements.add(new SetOspfMetricType(_type));
  }

  public OspfMetricType getType() {
    return _type;
  }
}
