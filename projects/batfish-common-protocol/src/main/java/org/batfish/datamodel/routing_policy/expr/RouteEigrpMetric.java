package org.batfish.datamodel.routing_policy.expr;

import javax.annotation.Nullable;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.EigrpRoute;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.batfish.datamodel.routing_policy.Environment;

public class RouteEigrpMetric extends EigrpMetricExpr {

  private static final long serialVersionUID = 1L;

  @Override
  public boolean equals(Object obj) {
    return obj instanceof RouteEigrpMetric;
  }

  @Override
  @Nullable
  public EigrpMetric evaluate(Environment env) {
    AbstractRoute route = env.getOriginalRoute();
    if (route instanceof EigrpRoute) {
      return ((EigrpRoute) route).getEigrpMetric();
    }
    return null;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
