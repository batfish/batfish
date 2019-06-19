package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;

/**
 * A condition contained by a route-map entry which a route must match for the entry to be applied
 * to the route.
 */
@ParametersAreNonnullByDefault
public interface RouteMapMatch extends Serializable {

  /**
   * Returns a vendor-independent routing-policy {@link BooleanExpr} corresponding to this match
   * statement.
   */
  @Nonnull
  BooleanExpr toBooleanExpr(Configuration c, F5BigipConfiguration vc, Warnings w);
}
