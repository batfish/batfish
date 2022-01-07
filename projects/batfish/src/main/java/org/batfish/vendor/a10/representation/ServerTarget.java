package org.batfish.vendor.a10.representation;

import java.io.Serializable;

/** Represents a target for a load balancer server, e.g. IP address or hostname */
public abstract class ServerTarget implements Serializable {
  public abstract <T> T accept(ServerTargetVisitor<T> visitor);

  // Force children to implement equals/hashcode
  @Override
  public abstract boolean equals(Object o);

  @Override
  public abstract int hashCode();
}
