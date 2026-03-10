package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;

/** Expression that extracts a route's destination network given a routing policy environment. */
public final class DestinationNetwork extends PrefixExpr {

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

  @JsonCreator
  public static DestinationNetwork instance() {
    return INSTANCE;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }
}
