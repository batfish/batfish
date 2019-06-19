package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.Statement;

/**
 * A transformation contained by a route-map entry that is applied to any route to which the entry
 * applies.
 */
@ParametersAreNonnullByDefault
public interface RouteMapSet extends Serializable {

  /**
   * Returns a stream of vendor-independent routing-policy {@link Statement}s corresponding to this
   * set statement.
   */
  @Nonnull
  Stream<Statement> toStatements(Configuration c, F5BigipConfiguration vc, Warnings w);
}
