package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;

/** Expression that extracts a route's destination network given a routing policy environment. */
public final class DestinationNetwork extends PrefixExpr {

  private static final long serialVersionUID = 1L;

  private static final DestinationNetwork INSTANCE = new DestinationNetwork();

  private DestinationNetwork() {}

  @Override
  public boolean equals(Object obj) {
    return this == obj || (obj instanceof DestinationNetwork);
  }

  @Override
  public Prefix evaluate(Environment env) {
    return env.getOriginalRoute().getNetwork();
  }

  public static DestinationNetwork instance() {
    return INSTANCE;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }
}
